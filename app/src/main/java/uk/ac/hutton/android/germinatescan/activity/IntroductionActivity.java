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

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import com.heinrichreimersoftware.materialintro.app.*;
import com.heinrichreimersoftware.materialintro.slide.*;

import uk.ac.hutton.android.germinatescan.R;
import uk.ac.hutton.android.germinatescan.fragment.*;
import uk.ac.hutton.android.germinatescan.util.PreferenceUtils;

/**
 * The {@link IntroductionActivity} is shown on first start. It guides the user though the initial data source selection and will show the EULA.
 *
 * @author Sebastian Raubach
 */
public class IntroductionActivity extends IntroActivity
{
	private int nrOfbarcodes = PreferenceUtils.DEFAULT_PREF_NR_BARCODES;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		final PreferenceUtils prefs = new PreferenceUtils(this);

		/* Set some preferences that are used for the navigation here initially to false */
		prefs.putBoolean(PreferenceUtils.PREFS_EULA_ACCEPTED, false);

		setButtonBackVisible(false);

		/* Welcome slide */
		addSlide(new SimpleSlide.Builder()
				.title(R.string.introduction_welcome_title)
				.description(R.string.introduction_welcome_text)
				.image(R.mipmap.ic_launcher_3x)
				.background(R.color.color_primary)
				.backgroundDark(R.color.color_primary_dark)
				.permission(Manifest.permission.INTERNET)
				.build());

		addSlide(new FragmentSlide.Builder()
				.background(R.color.color_primary)
				.backgroundDark(R.color.color_primary_dark)
				.fragment(new EulaFragment())
				.build());

		addSlide(new FragmentSlide.Builder()
				.background(R.color.color_primary)
				.backgroundDark(R.color.color_primary_dark)
				.fragment(new BarcodeFragment())
				.build());

		addSlide(new SimpleSlide.Builder()
				.title(R.string.introduction_settings_title)
				.description(R.string.introduction_settings_text)
				.background(R.color.color_primary)
				.backgroundDark(R.color.color_primary_dark)
				.build());

		setNavigationPolicy(new NavigationPolicy()
		{
			@Override
			public boolean canGoForward(int position)
			{
				if (position == 1)
				{
					return prefs.getBoolean(PreferenceUtils.PREFS_EULA_ACCEPTED, false);
				}
				else
				{
					return true;
				}
			}

			@Override
			public boolean canGoBackward(int position)
			{
				return position > 1;
			}
		});
	}

	@Override
	public Intent onSendActivityResult(int result)
	{
		Intent intent = new Intent();
		intent.putExtra(BarcodeSelectionActivity.EXTRA_NR_OF_BARCODES, nrOfbarcodes);
		return intent;
	}

	public void onBarcodeSelected(int value)
	{
		nrOfbarcodes = value;
	}
}