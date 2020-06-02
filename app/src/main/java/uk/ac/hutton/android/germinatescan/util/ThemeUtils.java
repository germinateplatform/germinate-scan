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
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import java.lang.ref.WeakReference;
import java.util.*;

import uk.ac.hutton.android.germinatescan.R;
import uk.ac.hutton.android.germinatescan.activity.*;

/**
 * {@link uk.ac.hutton.android.germinatescan.util.ThemeUtils} contains utility functions for handling the application theme.
 *
 * @author Sebastian Raubach
 */
public class ThemeUtils
{
	public static final  int                                DARK_THEME    = R.style.AppThemeDark;
	public static final  int                                LIGHT_THEME   = R.style.AppThemeLight;
	public static final  int                                DEFAULT_THEME = LIGHT_THEME;
	private static final Set<WeakReference<ThemedActivity>> LISTENERS     = new HashSet<>();

	/**
	 * Returns <code>true</code> if the current theme is {@link #DARK_THEME}
	 *
	 * @return <code>true</code> if the current theme is {@link #DARK_THEME}
	 */
	public static boolean isDarkTheme(Context activ)
	{
		int currentTheme = getCurrentTheme(activ);

		return currentTheme == DARK_THEME;
	}

	public static int getCurrentTheme(Context activ)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activ);
		return preferences.getBoolean(PreferenceUtils.PREF_THEME, isDefaultThemeDark()) ? DARK_THEME : LIGHT_THEME;
	}

	public static boolean hasCurrentTheme(GerminateScanActivity activ)
	{
		try
		{
			return activ.getPackageManager().getActivityInfo(activ.getComponentName(), 0).theme == getCurrentTheme(activ);
		}
		catch (PackageManager.NameNotFoundException e)
		{
			return true;
		}
	}

	/**
	 * Applies the current theme to the given Activity. <p> Registers the {@link GerminateScanActivity} for theme change notifications </p>
	 *
	 * @param activ The activity
	 * @see #notifyListeners()
	 */
	public static void applyTheme(ThemedActivity activ)
	{
		LISTENERS.add(new WeakReference<>(activ));
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activ);
		activ.setTheme(preferences.getBoolean(PreferenceUtils.PREF_THEME, isDefaultThemeDark()) ? DARK_THEME : LIGHT_THEME);
	}

	/**
	 * Applies the current theme to the given Activity
	 *
	 * @param activ The activity
	 */
	public static void applyTheme(Context activ)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activ);
		activ.setTheme(preferences.getBoolean(PreferenceUtils.PREF_THEME, isDefaultThemeDark()) ? DARK_THEME : LIGHT_THEME);
	}

	/**
	 * Returns <code>true</code> if {@link #DARK_THEME} is set as the default
	 *
	 * @return <code>true</code> if {@link #DARK_THEME} is set as the default
	 */
	public static boolean isDefaultThemeDark()
	{
		return getDefaultTheme() == DARK_THEME;
	}

	/**
	 * Returns the resource id if the default theme
	 *
	 * @return The resource id if the default theme
	 */
	public static int getDefaultTheme()
	{
		return DEFAULT_THEME;
	}

	/**
	 * Notifies the {@link ThemedActivity} listeners that the theme changed
	 */
	public static void notifyListeners()
	{
		for (WeakReference<ThemedActivity> ref : LISTENERS)
		{
			ThemedActivity activity = ref.get();
			if (activity != null)
			{
				activity.onThemeChanged();
			}
		}
	}
}
