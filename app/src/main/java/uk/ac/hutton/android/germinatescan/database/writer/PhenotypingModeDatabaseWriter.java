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

import uk.ac.hutton.android.germinatescan.R;
import uk.ac.hutton.android.germinatescan.activity.GerminateScanActivity;
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
				Barcode currentPlantBarcode = barcodes.get(0);
				String currentPlant = currentPlantBarcode.getBarcode();
				String currentTrait = null;
				int col = 0;

				Map<String, String> forPlant = new HashMap<>();
				for (int i = 1; i < barcodes.size(); i++)
				{
					col = i % 3;

					if (col == 0)
					{
						Barcode barcode = barcodes.get(i);
						String plant = barcode.getBarcode();

						if (!StringUtils.areEqual(plant, currentPlant) || forPlant.size() == preloadedPhenotypes.size())
						{
							writeForPlant(bw, currentPlantBarcode, props, preloadedPhenotypes, forPlant);
							currentPlantBarcode = barcode;
							currentPlant = plant;
							forPlant = new HashMap<>();
						}
					}
					else if (col == 1)
					{
						currentTrait = barcodes.get(i).getBarcode();
					}
					else if (col == 2)
					{
						String value = barcodes.get(i).getBarcode();

						forPlant.put(currentTrait, value);
					}
				}

				if (forPlant.size() > 0)
					writeForPlant(bw, currentPlantBarcode, props, preloadedPhenotypes, forPlant);
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
				e1.printStackTrace();
			}

			e.printStackTrace();

			throw e;
		}
	}

	private void writeForPlant(BufferedWriter bw, Barcode currentPlant, List<Barcode.BarcodeProperty> props, List<String> preloadedPhenotypes, Map<String, String> traitToValue) throws IOException
	{
		bw.newLine();
		bw.write(currentPlant.getBarcode());
		if (props.contains(Barcode.BarcodeProperty.TIMESTAMP))
			bw.write(delimiter + currentPlant.getFormattedTimestamp());
		if (props.contains(Barcode.BarcodeProperty.LATITUDE))
			bw.write(delimiter + currentPlant.getLatitudeOrEmpty());
		if (props.contains(Barcode.BarcodeProperty.LONGITUDE))
			bw.write(delimiter + currentPlant.getLongitudeOrEmpty());
		if (props.contains(Barcode.BarcodeProperty.ALTITUDE))
			bw.write(delimiter + currentPlant.getAltitudeOrEmpty());

		for (String trait : preloadedPhenotypes)
		{
			String value = traitToValue.get(trait);

			if (StringUtils.isEmpty(value))
				bw.write(delimiter);
			else
				bw.write(delimiter + value);
		}
	}
}
