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

package uk.ac.hutton.android.germinatescan.util.barcodehandler;

import android.content.*;
import android.location.*;
import android.net.*;
import android.util.*;

import java.util.*;

import uk.ac.hutton.android.germinatescan.activity.*;
import uk.ac.hutton.android.germinatescan.database.*;
import uk.ac.hutton.android.germinatescan.util.*;

/**
 * @author Sebastian Raubach
 */

public abstract class DefaultBarcodeHandler extends BarcodeHandler
{
	private PreferenceUtils prefs;

	public DefaultBarcodeHandler(GerminateScanActivity context)
	{
		super(context);
		prefs = new PreferenceUtils(context);
	}

	@Override
	public List<Barcode> handle(String input, Location location, int index)
	{
		if (input.equals(prefs.getString(PreferenceUtils.PREFS_DELETE_ROW_TOKEN)))
		{
			deleteRow();
		}
		else if (input.equals(prefs.getString(PreferenceUtils.PREFS_DELETE_CELL_TOKEN)))
		{
			deleteBarcode();
		}
		/* Validate the input */
		else
		{
			/* If the user wants URLs to open in the browser and it actually is a URL, then do it */
			if (prefs.getBoolean(PreferenceUtils.PREFS_URL_OPEN_EXTERNAL, false) && Patterns.WEB_URL.matcher(input).matches())
			{
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(input));
				context.startActivity(browserIntent);
			}
			if (input.equals(prefs.getString(PreferenceUtils.PREFS_NULL_TOKEN)))
			{
				input = "";
			}

			Barcode barcode = new Barcode();
			barcode.setBarcode(input);
			barcode.setTimestamp(Long.toString(System.currentTimeMillis()));
			barcode.setLatitude(location);
			barcode.setLongitude(location);
			barcode.setAltitude(location);

			return Collections.singletonList(barcode);
		}

		return null;
	}
}
