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
import android.widget.Toast;

/**
 * {@link uk.ac.hutton.android.germinatescan.util.ToastUtils} contains utility functions for showing {@link Toast}s. Calling one of these functions
 * will cancel the currently shown {@link Toast} (if any) and show the new one.
 *
 * @author Sebastian Raubach
 */
public class ToastUtils
{
	public static final int LENGTH_SHORT = Toast.LENGTH_SHORT;
	public static final int LENGTH_LONG  = Toast.LENGTH_LONG;

	private static Toast toast;

	/**
	 * Creates a new toast message while canceling all old ones
	 *
	 * @param context  The context to use. Usually your Application or Activity object.
	 * @param text     The text to show. Can be formatted text.
	 * @param duration How long to display the message. Either LENGTH_SHORT or LENGTH_LONG
	 */
	public static void createToast(Context context, CharSequence text, int duration)
	{
		/* If there's already a toast, cancel it */
		if (toast != null)
		{
			toast.cancel();
		}

		/* Create and show the toast */
		toast = Toast.makeText(context, text, duration);
		toast.show();
	}

	/**
	 * Creates a new toast message while canceling all old ones
	 *
	 * @param context  The context to use. Usually your Application or Activity object.
	 * @param text     The resource id of the string resource to use. Can be formatted text.
	 * @param duration How long to display the message. Either LENGTH_SHORT or LENGTH_LONG
	 */
	public static void createToast(Context context, int text, int duration)
	{
		/* If there's already a toast, cancel it */
		if (toast != null)
		{
			toast.cancel();
		}

		/* Create and show the toast */
		toast = Toast.makeText(context, text, duration);
		toast.show();
	}
}
