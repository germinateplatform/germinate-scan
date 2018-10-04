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

import com.google.android.gms.analytics.*;

/**
 * Utility class for Google Analytics. Contains methods to track events.
 *
 * @author Sebastian Raubach
 */
public class GoogleAnalyticsUtils
{
	/**
	 * Sends an event to the Google Analytics server
	 *
	 * @param context  The calling {@link Context}
	 * @param tracker  The {@link Tracker}
	 * @param category The category
	 * @param action   The action
	 */
	public static void trackEvent(Context context, Tracker tracker, String category, String action)
	{
		trackEvent(context, tracker, category, action, null);
	}

	/**
	 * Sends an event to the Google Analytics server
	 *
	 * @param context  The calling {@link Context}
	 * @param tracker  The {@link Tracker}
	 * @param category The category
	 * @param action   The action
	 * @param label    The label
	 */
	public static void trackEvent(Context context, Tracker tracker, String category, String action, String label)
	{
		trackEvent(context, tracker, category, action, label, null);
	}

	/**
	 * Sends an event to the Google Analytics server
	 *
	 * @param context  The calling {@link Context}
	 * @param tracker  The {@link Tracker}
	 * @param category The category
	 * @param action   The action
	 * @param label    The label
	 * @param value    The value
	 */
	public static void trackEvent(Context context, Tracker tracker, String category, String action, String label, Long value)
	{
		if (tracker == null)
		{
			return;
		}

        /* If we're using the debug version, don't track to Google Analytics */
		String packageName = context.getPackageName();
		/* Also, if the user disabled tracking, we don't track to Google Analytics */
		if (packageName != null && packageName.endsWith(".debug") || !new PreferenceUtils(context).getBoolean(PreferenceUtils.PREFS_GA_OPT_OUT, true))
		{
			return;
		}

		HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder().setCategory(category).setAction(action);

		if (!StringUtils.isEmpty(label))
		{
			builder.setLabel(label);
		}

		if (value != null)
		{
			builder.setValue(value);
		}

        /* Build and send an Event */
		tracker.send(builder.build());
	}
}
