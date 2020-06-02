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

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import uk.ac.hutton.android.germinatescan.BuildConfig;

/**
 * Utility class for Google Analytics. Contains methods to track events.
 *
 * @author Sebastian Raubach
 */
public class GoogleAnalyticsUtils
{
	public static void track(Context context, FirebaseAnalytics tracker, String event, String category, String content)
	{
		if (tracker == null)
		{
			return;
		}

		/* Also, if the user disabled tracking, we don't track to Google Analytics */
		if (BuildConfig.DEBUG || !new PreferenceUtils(context).getBoolean(PreferenceUtils.PREFS_GA_OPT_OUT, true))
			return;

		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, category);
		bundle.putString(FirebaseAnalytics.Param.CONTENT, content);
		tracker.logEvent(event, bundle);
	}
}
