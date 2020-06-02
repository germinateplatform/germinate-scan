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

import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;
import uk.ac.hutton.android.germinatescan.R;

/**
 * @author Sebastian Raubach
 */
public class SnackbarUtils
{
	public static void showSuccess(View snackbarParentView, String message, int length)
	{
		Snackbar snackbar = Snackbar.make(snackbarParentView, message, length);
		setColors(snackbar, ContextCompat.getColor(snackbarParentView.getContext(), android.R.color.white), ContextCompat.getColor(snackbarParentView.getContext(), R.color.snackbar_green));
		snackbar.show();
	}

	public static void showError(View snackbarParentView, String message, int length)
	{
		Snackbar snackbar = Snackbar.make(snackbarParentView, message, length);
		setColors(snackbar, ContextCompat.getColor(snackbarParentView.getContext(), android.R.color.white), ContextCompat.getColor(snackbarParentView.getContext(), R.color.snackbar_red));
		snackbar.show();
	}

	public static void setColors(Snackbar snackbar, @ColorInt int textColor, @ColorInt int backgroundColor)
	{
		View view = snackbar.getView();
		TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
		tv.setTextColor(textColor);
		view.setBackgroundColor(backgroundColor);
	}
}
