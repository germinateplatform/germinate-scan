/*
 *  Copyright 2018 Information and Computational Sciences,
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
public class MatrixDatabaseWriter extends DatabaseWriter
{
	private GerminateScanActivity context;
	private long datasetId;

	public MatrixDatabaseWriter(GerminateScanActivity context, long datasetId, String delimiter)
	{
		super(context, delimiter);
		this.context = context;
		this.datasetId = datasetId;
	}

	@Override
	public File write()
	{
		int nrOfColumns = new DatasetManager(context, datasetId).getById(datasetId).getBarcodesPerRow();
		long datasetId = new PreferenceUtils(context).getLong(PreferenceUtils.PREFS_SELECTED_DATASET_ID, -1);
		BarcodeManager barcodeManager = new BarcodeManager(context, datasetId);
		DatasetManager dsManager = new DatasetManager(context, datasetId);
		String datasetName = dsManager.getById(datasetId).getName().replace(" ", "-");

		Barcode.BarcodeMap map = barcodeManager.getAllAsRows(nrOfColumns);

		if (map == null || map.size() < 1)
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

			if (props.size() < 1)
			{
				ToastUtils.createToast(context, context.getString(R.string.toast_no_export_properties), ToastUtils.LENGTH_SHORT);
				return null;
			}

			bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));

            /* Write the headers */
			boolean first = true;
			if (singleTimeGps)
			{
				// Write all properties for the first column
				for (int i = 0; i < props.size(); i++)
				{
					if (first)
						bw.write(props.get(i).toString());
					else
						bw.write(delimiter + props.get(i).toString());

					first = false;
				}

				// Only the barcode for the remaining columns
				if (props.contains(Barcode.BarcodeProperty.BARCODE))
				{
					for (int col = 1; col < nrOfColumns; col++)
						bw.write(delimiter + Barcode.BarcodeProperty.BARCODE.toString());
				}
			}
			else
			{
				for (int col = 0; col < nrOfColumns; col++)
				{
					for (int i = 0; i < props.size(); i++)
					{
						if (first)
							bw.write(props.get(i).toString());
						else
							bw.write(delimiter + props.get(i).toString());

						first = false;
					}
				}
			}

			bw.newLine();

            /* Write each Barcode */
			for (Integer row : map.keySet())
			{
				List<Barcode> barcodes = map.get(row);

				if (!CollectionUtils.isEmpty(barcodes))
				{
					bw.write(barcodes.get(0).toStringForExport(false));
					for (int i = 1; i < barcodes.size(); i++)
					{
						bw.write(delimiter + barcodes.get(i).toStringForExport(singleTimeGps));
					}

					bw.newLine();
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
		}

		return null;
	}
}
