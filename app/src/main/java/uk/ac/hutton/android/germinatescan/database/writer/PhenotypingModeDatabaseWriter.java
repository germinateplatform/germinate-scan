/*
 *  Copyright 2019 Information and Computational Sciences,
 *  The James Hutton Institute.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package uk.ac.hutton.android.germinatescan.database.writer;

import java.io.*;
import java.util.*;

import uk.ac.hutton.android.germinatescan.*;
import uk.ac.hutton.android.germinatescan.activity.*;
import uk.ac.hutton.android.germinatescan.database.*;
import uk.ac.hutton.android.germinatescan.database.manager.*;
import uk.ac.hutton.android.germinatescan.util.*;

/**
 * @author Sebastian Raubach
 */
public class PhenotypingModeDatabaseWriter extends DatabaseWriter
{
	private GerminateScanActivity context;

	public PhenotypingModeDatabaseWriter(GerminateScanActivity context, String delimiter)
	{
		super(context, delimiter);
		this.context = context;
	}

	@Override
	public File write() throws IOException
	{
		long datasetId = new PreferenceUtils(context).getLong(PreferenceUtils.PREFS_SELECTED_DATASET_ID, -1);
		BarcodeManager barcodeManager = new BarcodeManager(context, datasetId);
		DatasetManager dsManager = new DatasetManager(context, datasetId);
		Dataset dataset = dsManager.getById(datasetId);
		String datasetName = dataset.getName().replace(" ", "-");
		List<String> preloadedPhenotypes = dataset.getPreloadedPhenotypes();

		List<Barcode> barcodes = barcodeManager.getAll();

		if (barcodes == null || barcodes.size() < 1)
		{
			ToastUtils.createToast(context, R.string.toast_no_data, ToastUtils.LENGTH_SHORT);
			return null;
		}

		File file = FileUtils.createFile(context, datasetId, FileUtils.ReferenceFolder.output, FileUtils.FileExtension.txt, datasetName);

		boolean singleTimeGps = new PreferenceUtils(context).getBoolean(PreferenceUtils.PREFS_EXPORT_MATRIX_SINGLE_TIME_GPS_PERROW, true);

		BufferedWriter bw = null;
		try
		{
			List<Barcode.BarcodeProperty> props = Barcode.BarcodeProperty.getUsedProperties();

			if (props == null)
				props = new ArrayList<>();

			bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));

			bw.write("Plant");

			if (props.contains(Barcode.BarcodeProperty.TIMESTAMP))
				bw.write(delimiter + Barcode.BarcodeProperty.TIMESTAMP.toString());
			if (props.contains(Barcode.BarcodeProperty.LATITUDE))
				bw.write(delimiter + Barcode.BarcodeProperty.LATITUDE.toString());
			if (props.contains(Barcode.BarcodeProperty.LONGITUDE))
				bw.write(delimiter + Barcode.BarcodeProperty.LONGITUDE.toString());
			if (props.contains(Barcode.BarcodeProperty.ALTITUDE))
				bw.write(delimiter + Barcode.BarcodeProperty.ALTITUDE.toString());

			for (String trait : preloadedPhenotypes)
				bw.write(delimiter + trait);

			if (!CollectionUtils.isEmpty(barcodes))
			{
				String currentPlant = null;
				int traitIndex = 0;
				int col = 0;

				for (Barcode barcode : barcodes)
				{
					if (col == 0 && !barcode.getBarcode().equals(currentPlant))
					{
						bw.newLine();
						currentPlant = barcode.getBarcode();
						traitIndex = 0;

						bw.write(currentPlant);
						if (props.contains(Barcode.BarcodeProperty.TIMESTAMP))
							bw.write(delimiter + barcode.getFormattedTimestamp());
						if (props.contains(Barcode.BarcodeProperty.LATITUDE))
							bw.write(delimiter + barcode.getLatitudeOrEmpty());
						if (props.contains(Barcode.BarcodeProperty.LONGITUDE))
							bw.write(delimiter + barcode.getLongitudeOrEmpty());
						if (props.contains(Barcode.BarcodeProperty.ALTITUDE))
							bw.write(delimiter + barcode.getAltitudeOrEmpty());
					}
					else if (col == 1)
					{
						if (!barcode.getBarcode().equals(preloadedPhenotypes.get(traitIndex)))
							throw new IOException("Mismatch between preloaded trait order and recorded data.");
					}
					else if (col == 2)
					{
						bw.write(delimiter + barcode.getBarcode());
						traitIndex++;
					}

					col = (col + 1) % 3;
				}
			}

			bw.close();

			return file;
		}
		catch (IOException e)
		{
			try
			{
				if (bw != null)
				{
					bw.close();
				}
			}
			catch (IOException e1)
			{
				GoogleAnalyticsUtils.trackEvent(context, context.getTracker(GerminateScanActivity.TrackerName.APP_TRACKER), context.getString(R.string.ga_event_category_exception), e1.getLocalizedMessage());
				e1.printStackTrace();
			}

			GoogleAnalyticsUtils.trackEvent(context, context.getTracker(GerminateScanActivity.TrackerName.APP_TRACKER), context.getString(R.string.ga_event_category_exception), e.getLocalizedMessage());
			e.printStackTrace();

			throw e;
		}
	}
}
