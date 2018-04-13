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

package uk.ac.hutton.android.germinatescan.util;

import uk.ac.hutton.android.germinatescan.*;
import uk.ac.hutton.android.germinatescan.activity.*;

/**
 * @author Sebastian Raubach
 */
public enum ImagePreferences
{
	TAG_TIMESTAMP(R.string.dialog_list_save_image_timestamp, PreferenceUtils.PREFS_SAVE_IMAGE_TAG_TIMESTAMP),
	TAG_LOCATION(R.string.dialog_list_save_image_location, PreferenceUtils.PREFS_SAVE_IMAGE_TAG_LOCATION),
	USE_BARCODE_AS_FILENAME(R.string.dialog_list_save_image_barcode, PreferenceUtils.PREFS_SAVE_IMAGE_TAG_BARCODE),
	USE_FIRST_BARCODE_IN_ROW_AS_FILENAME(R.string.dialog_list_save_image_barcode_first, PreferenceUtils.PREFS_SAVE_IMAGE_TAG_BARCODE_FIRST);

	private int    nameResource;
	private String displayName;
	private String preferenceKey;

	ImagePreferences(int nameResource, String preferenceKey)
	{
		this.nameResource = nameResource;
		this.preferenceKey = preferenceKey;
	}

	@Override
	public String toString()
	{
			/* Check if we already got the string resource before */
		if (StringUtils.isEmpty(displayName))
		{
			displayName = BarcodeReader.INSTANCE.getString(nameResource);
		}

		return displayName;
	}

	public String getPreferenceKey()
	{
		return preferenceKey;
	}

	public String getDisplayName()
	{
		return toString();
	}
}
