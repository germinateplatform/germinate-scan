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

import android.content.*;
import android.content.SharedPreferences.*;
import android.preference.*;
import android.text.*;

import com.google.android.gms.analytics.*;
import com.google.gson.*;

import java.util.*;

import uk.ac.hutton.android.germinatescan.database.*;

/**
 * {@link PreferenceUtils} holds all the information required to access shared preferences
 *
 * @author Sebastian Raubach
 */
public class PreferenceUtils
{
	public static final String PREF_THEME                         = "Prefs.Appearance.Theme";
	public static final String PREF_BARCODES                      = "Prefs.Barcodes.Nr";
	public static final String PREFS_DELETE_ROW_TOKEN             = "Prefs.Delete.Row.Token";
	public static final String PREFS_DELETE_CELL_TOKEN            = "Prefs.Delete.Cell.Token";
	public static final String PREFS_NULL_TOKEN                   = "Prefs.Null.Token";
	public static final String PREFS_SHOW_EULA                    = "Prefs.Show.Eula";
	public static final String PREFS_EULA_ACCEPTED                = "Prefs.Eula.Accepted";
	public static final String PREFS_EULA_TYPE                    = "Prefs.Eula.Type";
	public static final String PREFS_SHOW_CHANGELOG               = "Prefs.Show.Changelog";
	public static final String PREFS_MAP_TYPE                     = "Prefs.Map.Type";
	public static final String PREFS_MAP_FOLLOW_ME                = "Prefs.Map.Follow.Me";
	public static final String PREFS_EXPORT_BARCODE_PROPERTIES    = "Prefs.Export.Barcode.Properties";
	public static final String PREFS_EXPORT_MATRIX_FORMAT         = "Prefs.Export.Format.Matrix";
	public static final String PREFS_VOICE_FEEDBACK               = "Prefs.Voice.Feedback";
	public static final String PREFS_VOICE_FEEDBACK_WARNING       = "Prefs.Voice.Feedback.Warning";
	public static final String PREFS_GA_OPT_OUT                   = "Prefs.Google.Analytics.Opt.Out";
	public static final String PREFS_URL_OPEN_EXTERNAL            = "Prefs.Url.Open.External";
	public static final String PREFS_PRELOADED_PHENOTYPES         = "Prefs.Preloaded.Phenotypes";
	public static final String PREFS_PRELOADED_PHENOTYPES_COUNTER = "Prefs.Preloaded.Phenotypes.Counter";
	public static final String PREFS_SELECTED_DATASET_ID          = "Prefs.Dataset.Id";

	public static final String PREFS_LAST_VERSION = "Prefs.Last.Version.Code";

	public static final String PREFS_SAVE_IMAGE_TAG_LOCATION      = "Prefs.Save.Image.Tag.Location";
	public static final String PREFS_SAVE_IMAGE_TAG_TIMESTAMP     = "Prefs.Save.Image.Tag.Timestamp";
	public static final String PREFS_SAVE_IMAGE_TAG_BARCODE       = "Prefs.Save.Image.Tag.Barcode";
	public static final String PREFS_SAVE_IMAGE_TAG_BARCODE_FIRST = "Prefs.Save.Image.Tag.Barcode.First";

	public static final boolean DEFAULT_PREF_THEME       = false;
	public static final boolean DEFAULT_PREF_MAP_FOLLOW  = true;
	public static final int     DEFAULT_PREF_NR_BARCODES = 3;

	private SharedPreferences preferences;
	private Context           context;

	public PreferenceUtils(Context context)
	{
		this.context = context;
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	/**
	 * Sets the defaults for all preferences that don't have a value yet
	 */
	public void setDefaults()
	{
		GoogleAnalytics.getInstance(context).setAppOptOut(!getBoolean(PREFS_GA_OPT_OUT, true));

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = preferences.edit();

		if (!preferences.contains(PREFS_GA_OPT_OUT))
		{
			editor.putBoolean(PREFS_GA_OPT_OUT, true);
		}
		if (!preferences.contains(PREF_THEME))
		{
			editor.putBoolean(PREF_THEME, DEFAULT_PREF_THEME);
		}
//		if (!preferences.contains(PREF_BARCODES))
//		{
//			editor.putInt(PREF_BARCODES, DEFAULT_PREF_NR_BARCODES);
//		}
//		if (!preferences.contains(PREFS_MAP_TYPE))
//		{
//			editor.putInt(PREFS_MAP_TYPE, MapActivity.DEFAULT_MAP_TYPE);
//		}
		if (!preferences.contains(PREFS_MAP_FOLLOW_ME))
		{
			editor.putBoolean(PREFS_MAP_FOLLOW_ME, DEFAULT_PREF_MAP_FOLLOW);
		}
		if (!preferences.contains(PREFS_EXPORT_BARCODE_PROPERTIES))
		{
			editor.putString(PREFS_EXPORT_BARCODE_PROPERTIES, Barcode.BarcodeProperty.getDefaultAsString());
		}
		if (!preferences.contains(PREFS_EXPORT_MATRIX_FORMAT))
		{
			editor.putBoolean(PREFS_EXPORT_MATRIX_FORMAT, false);
		}
		if (!preferences.contains(PREFS_SAVE_IMAGE_TAG_LOCATION))
		{
			editor.putBoolean(PREFS_SAVE_IMAGE_TAG_LOCATION, true);
		}
		if (!preferences.contains(PREFS_SAVE_IMAGE_TAG_TIMESTAMP))
		{
			editor.putBoolean(PREFS_SAVE_IMAGE_TAG_TIMESTAMP, true);
		}
		if (!preferences.contains(PREFS_SAVE_IMAGE_TAG_BARCODE))
		{
			editor.putBoolean(PREFS_SAVE_IMAGE_TAG_BARCODE, true);
		}
		if (!preferences.contains(PREFS_SAVE_IMAGE_TAG_BARCODE_FIRST))
		{
			editor.putBoolean(PREFS_SAVE_IMAGE_TAG_BARCODE_FIRST, false);
		}
		if (!preferences.contains(PREFS_VOICE_FEEDBACK))
		{
			editor.putBoolean(PREFS_VOICE_FEEDBACK, false);
		}
		if (!preferences.contains(PREFS_VOICE_FEEDBACK_WARNING))
		{
			editor.putBoolean(PREFS_VOICE_FEEDBACK_WARNING, false);
		}
		if (!preferences.contains(PREFS_URL_OPEN_EXTERNAL))
		{
			editor.putBoolean(PREFS_URL_OPEN_EXTERNAL, false);
		}

		editor.apply();
	}

	/**
	 * Returns the value of the given preference
	 *
	 * @param pref The preference key as specified by one of the class constants
	 * @return The value of the preference
	 */
	public String getString(String pref)
	{
		return preferences.getString(pref, "");
	}

	/**
	 * Returns the value of the given preference as an integer
	 *
	 * @param pref     The preference key as specified by one of the class constants as an integer
	 * @param fallback The fallback value if the property isn't set
	 * @return The value of the preference
	 */
	public int getInt(String pref, int fallback)
	{
		return preferences.getInt(pref, fallback);
	}

	/**
	 * Returns the value of the given preference as a long
	 *
	 * @param pref     The preference key as specified by one of the class constants as a long
	 * @param fallback The fallback value if the property isn't set
	 * @return The value of the preference
	 */
	public long getLong(String pref, long fallback)
	{
		return preferences.getLong(pref, fallback);
	}

	/**
	 * Returns the value of the given preference as a boolean
	 *
	 * @param pref     The preference key as specified by one of the class constants as a boolean
	 * @param fallback The fallback value if the property isn't set
	 * @return The value of the preference
	 */
	public boolean getBoolean(String pref, boolean fallback)
	{
		return preferences.getBoolean(pref, fallback);
	}

	/**
	 * Sets the given value to the given preference
	 *
	 * @param pref  The preference key
	 * @param value The preference value
	 */
	public void putString(String pref, String value)
	{
		preferences.edit()
				   .putString(pref, value)
				   .apply();
	}

	/**
	 * Sets the given value to the given preference as an int
	 *
	 * @param pref  The preference key
	 * @param value The preference value (int)
	 */
	public void putInt(String pref, int value)
	{
		preferences.edit()
				   .putInt(pref, value)
				   .apply();
	}

	/**
	 * Sets the given value to the given preference as a long
	 *
	 * @param pref  The preference key
	 * @param value The preference value (long)
	 */
	public void putLong(String pref, long value)
	{
		preferences.edit()
				   .putLong(pref, value)
				   .apply();
	}

	/**
	 * Sets the given value to the given preference as a boolean
	 *
	 * @param pref  The preference key
	 * @param value The preference value (boolean)
	 */
	public void putBoolean(String pref, boolean value)
	{
		preferences.edit()
				   .putBoolean(pref, value)
				   .apply();
	}

	/**
	 * Returns the stored check-state of the given {@link uk.ac.hutton.android.germinatescan.util.ImagePreferences}.
	 *
	 * @param prefs The preference keys
	 * @return The stored check-state of the given {@link uk.ac.hutton.android.germinatescan.util.ImagePreferences}.
	 */
	public boolean[] getImagePreferences(ImagePreferences... prefs)
	{
		if (prefs == null || prefs.length < 1)
		{
			return null;
		}

		boolean[] result = new boolean[prefs.length];

		for (int i = 0; i < prefs.length; i++)
		{
			result[i] = getBoolean(prefs[i].getPreferenceKey(), true);
		}

		return result;
	}

	public void remove(String pref)
	{
		preferences.edit()
				   .remove(pref)
				   .apply();
	}

	/**
	 * Put ArrayList of String into SharedPreferences with 'key' and save
	 *
	 * @param key        SharedPreferences key
	 * @param stringList ArrayList of String to be added
	 */
	public void putListString(String key, List<String> stringList)
	{
		String[] myStringList = stringList.toArray(new String[stringList.size()]);
		preferences.edit().putString(key, TextUtils.join("‚‗‚", myStringList)).apply();
	}

	public <T> void putListObject(String key, List<T> objArray)
	{
		Gson gson = new Gson();
		ArrayList<String> objStrings = new ArrayList<>();
		for (T obj : objArray)
		{
			objStrings.add(gson.toJson(obj));
		}
		putListString(key, objStrings);
	}

	/**
	 * Get parsed ArrayList of String from SharedPreferences at 'key'
	 *
	 * @param key SharedPreferences key
	 * @return ArrayList of String
	 */
	public List<String> getListString(String key)
	{
		return new ArrayList<>(Arrays.asList(TextUtils.split(preferences.getString(key, ""), "‚‗‚")));
	}

	public <T> List<T> getListObject(String key, Class<T> mClass)
	{
		Gson gson = new Gson();

		List<String> objStrings = getListString(key);
		ArrayList<T> objects = new ArrayList<>();

		for (String jObjString : objStrings)
		{
			T value = gson.fromJson(jObjString, mClass);
			objects.add(value);
		}
		return objects;
	}
}
