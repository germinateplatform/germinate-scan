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

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.preference.*;
import android.webkit.*;
import android.widget.*;

import java.io.*;

import uk.ac.hutton.android.germinatescan.*;
import uk.ac.hutton.android.germinatescan.activity.*;

/**
 * @author Sebastian Raubach
 */
public class EulaUtils
{
	public static void showEula(final GerminateScanActivity activity, final boolean hasToAccept, final OnAcceptCallback callback)
	{
		String[] licenseTypes = {activity.getString(EulaType.CONSUMER.textResource), activity.getString(EulaType.COMMERCIAL.textResource)};

		new AlertDialog.Builder(activity)
				.setTitle(R.string.dialog_title_eula_type)
				.setAdapter(new ArrayAdapter<>(activity, android.R.layout.select_dialog_item, licenseTypes), new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						switch (which)
						{
							case 0:
								/* Consumer */
								showEulaForType(activity, hasToAccept, EulaType.CONSUMER, callback);
								break;
							case 1:
								/* Commercial */
								showEulaForType(activity, hasToAccept, EulaType.COMMERCIAL, callback);
								break;
						}
					}
				})
				.setCancelable(false)
				.show();
	}

	private static void showEulaForType(final GerminateScanActivity activity, final boolean hasToAccept, final EulaType type, final OnAcceptCallback callback)
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(activity);
		alert.setTitle(R.string.dialog_title_disclaimer);

		WebView wv = new WebView(activity);

		int resource = -1;
		switch (type)
		{
			case COMMERCIAL:
				resource = R.raw.eula_commercial;
				break;
			case CONSUMER:
				resource = R.raw.eula_consumer;
				break;
		}

		String prompt = "";
		try
		{
			InputStream inputStream = activity.getResources().openRawResource(resource);
			byte[] buffer = new byte[inputStream.available()];
			inputStream.read(buffer);
			prompt = new String(buffer);
			inputStream.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		wv.loadDataWithBaseURL(null, prompt, "text/html", "utf-8", null);

		wv.setWebViewClient(new WebViewClient()
		{
			@SuppressWarnings("deprecation")
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
				return handleUri(url);
			}

			@TargetApi(Build.VERSION_CODES.N)
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request)
			{
				return handleUri(request.getUrl().toString());
			}

			private boolean handleUri(String url)
			{
				if (url.startsWith("http:") || url.startsWith("https:"))
				{
					return false;
				}

				try
				{
					// Otherwise allow the OS to handle things like tel, mailto, etc.
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					activity.startActivity(intent);
					return true;
				}
				catch (ActivityNotFoundException e)
				{
					return false;
				}
			}
		});

//		switch (type)
//		{
//			case COMMERCIAL:
//				wv.loadUrl("file:///android_res/raw/eula_commercial.html");
//				break;
//			case CONSUMER:
//				wv.loadUrl("file:///android_res/raw/eula_consumer.html");
//				break;
//		}
//
//		wv.setWebViewClient(new WebViewClient()
//		{
//			@Override
//			public boolean shouldOverrideUrlLoading(WebView view, String url)
//			{
//				if (url.startsWith("http:") || url.startsWith("https:"))
//				{
//					return false;
//				}
//
//				try
//				{
//					// Otherwise allow the OS to handle things like tel, mailto, etc.
//					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//					activity.startActivity(intent);
//					return true;
//				}
//				catch (ActivityNotFoundException e)
//				{
//					return false;
//				}
//			}
//		});

		final long startTime = System.currentTimeMillis();
		alert.setView(wv);
		alert.setPositiveButton(hasToAccept ? R.string.general_accept : android.R.string.ok, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialogInterface, int i)
			{
				long finishTime = System.currentTimeMillis() - startTime;

				/* Only track selection if it's the initial decision */
				if (hasToAccept)
				{
					switch (type)
					{
						case COMMERCIAL:
							GoogleAnalyticsUtils.trackEvent(activity, activity.getTracker(GerminateScanActivity.TrackerName.APP_TRACKER), activity.getString(R.string.ga_event_category_eula), activity.getString(R.string.ga_event_action_eula_type), activity.getString(R.string.ga_event_label_eula_type_commercial));
							break;
						case CONSUMER:
							GoogleAnalyticsUtils.trackEvent(activity, activity.getTracker(GerminateScanActivity.TrackerName.APP_TRACKER), activity.getString(R.string.ga_event_category_eula), activity.getString(R.string.ga_event_action_eula_type), activity.getString(R.string.ga_event_label_eula_type_consumer));
							break;
					}
				}

				GoogleAnalyticsUtils.trackEvent(activity, activity.getTracker(GerminateScanActivity.TrackerName.APP_TRACKER), activity.getString(R.string.ga_event_category_eula), activity.getString(R.string.ga_event_action_accept), activity.getString(R.string.ga_event_unit_ms), finishTime);

				SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(BarcodeReader.INSTANCE).edit();
				edit.putBoolean(PreferenceUtils.PREFS_EULA_ACCEPTED, true);
				edit.apply();

				if (callback != null)
					callback.onAccept();
			}
		});

		if (hasToAccept)
		{
			alert.setNegativeButton(R.string.general_cancel, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialogInterface, int i)
				{
					long finishTime = System.currentTimeMillis() - startTime;

					GoogleAnalyticsUtils.trackEvent(activity, activity.getTracker(GerminateScanActivity.TrackerName.APP_TRACKER), activity.getString(R.string.ga_event_category_eula), activity.getString(R.string.ga_event_action_declined), activity.getString(R.string.ga_event_unit_ms), finishTime);

					activity.finish();
				}
			});
		}

		alert.setCancelable(!hasToAccept);
		alert.show();
	}

	public enum EulaType
	{
		COMMERCIAL(R.string.label_eula_commercial),
		CONSUMER(R.string.label_eula_consumer);

		int textResource;

		EulaType(int textResource)
		{
			this.textResource = textResource;
		}

		public int getTextResource()
		{
			return textResource;
		}
	}

	public interface OnAcceptCallback
	{
		void onAccept();
	}
}
