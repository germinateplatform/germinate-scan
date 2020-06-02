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

import android.content.pm.*;
import android.os.Bundle;
import android.view.MenuItem;

import uk.ac.hutton.android.germinatescan.R;
import uk.ac.hutton.android.germinatescan.util.PreferenceUtils;

/**
 * @author Sebastian Raubach
 */
public class ChangelogActivity extends ThemedActivity
{
	@Override
	protected Integer getLayoutId()
	{
		return R.layout.activity_changelog;
	}

	@Override
	protected Integer getToolbarId()
	{
		return R.id.toolbar;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Get the versionCode of the Package, which must be different (incremented) in each release on the market in the AndroidManifest.xml
		final PackageInfo packageInfo;
		try
		{
			packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
			new PreferenceUtils(this).putInt(PreferenceUtils.PREFS_LAST_VERSION, packageInfo.versionCode);
		}
		catch (PackageManager.NameNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				onBackPressed();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
