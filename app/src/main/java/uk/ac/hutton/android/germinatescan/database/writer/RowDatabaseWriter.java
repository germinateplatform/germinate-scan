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
public class RowDatabaseWriter extends DatabaseWriter
{
	public RowDatabaseWriter(GerminateScanActivity context, String delimiter)
	{
		super(context, delimiter);
	}

	@Override
	public File write()
	{
		long datasetId = new PreferenceUtils(context).getInt(PreferenceUtils.PREFS_SELECTED_DATASET_ID, -1);
		BarcodeManager barcodeManager = new BarcodeManager(context, datasetId);

		List<Barcode> items = barcodeManager.getAll();

		if (items == null || items.size() < 1)
		{
			ToastUtils.createToast(context, R.string.toast_no_data, ToastUtils.LENGTH_SHORT);
			return null;
		}

		File file = FileUtils.createFile(context, datasetId, FileUtils.ReferenceFolder.output, FileUtils.FileExtension.tsv);

		BufferedWriter bw = null;
		try
		{
			List<Barcode.BarcodeProperty> props = Barcode.BarcodeProperty.getUsedProperties();

			if (props.size() < 1)
			{
				ToastUtils.createToast(context, context.getString(R.string.toast_no_export_properties), ToastUtils.LENGTH_SHORT);
				return null;
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			bw = new BufferedWriter(fw);

            /* Write the headers */
			bw.write(props.get(0).toString());
			for (int i = 1; i < props.size(); i++)
			{
				bw.write(delimiter + props.get(i).toString());
			}

			bw.newLine();

            /* Write each Barcode */
			for (Barcode item : items)
			{
				bw.write(item.toStringForExport());
				bw.newLine();
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
