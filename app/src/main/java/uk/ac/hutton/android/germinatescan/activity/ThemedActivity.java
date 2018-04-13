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

package uk.ac.hutton.android.germinatescan.activity;

import android.content.res.*;
import android.graphics.*;
import android.os.*;

import uk.ac.hutton.android.germinatescan.util.*;

/**
 * @author Sebastian Raubach
 */
public abstract class ThemedActivity extends GerminateScanActivity
{
	private boolean themeChanged = false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		/* Apply the theme */
		ThemeUtils.applyTheme(this);

		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume()
	{
		super.onResume();

        /* Check if the theme has been changed in the meantime */
		if (themeChanged)
		{
			themeChanged = false;
			this.recreate();
		}
	}

	/**
	 * Indicate that the theme has been changed
	 */
	public void onThemeChanged()
	{
		themeChanged = true;
	}

	protected int getThemedAttributeColor(int attr)
	{
		int[] attrs = new int[]{attr};
		TypedArray ta = obtainStyledAttributes(attrs);
		int color = ta.getColor(0, Color.WHITE);
		ta.recycle();
		return color;
	}
}
