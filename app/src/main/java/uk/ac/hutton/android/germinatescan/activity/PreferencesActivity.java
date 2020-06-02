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

import android.app.AlertDialog;
import android.content.*;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.*;
import android.preference.*;
import android.speech.tts.TextToSpeech;
import android.view.*;
import android.widget.*;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.zxing.integration.android.*;

import uk.ac.hutton.android.germinatescan.R;
import uk.ac.hutton.android.germinatescan.util.*;

/**
 * The {@link uk.ac.hutton.android.germinatescan.activity.PreferencesActivity} shows all the customizable preferences of Germinate Scan.
 *
 * @author Sebastian Raubach
 */
public class PreferencesActivity extends ThemedActivity
{
	private static FirebaseAnalytics tracker;
	private        PrefsFragment     prefsFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		tracker = getTracker();

		prefsFragment = new PrefsFragment();

		/* Show the fragment */
		getFragmentManager().beginTransaction()
							.replace(R.id.preferences_layout, prefsFragment)
							.commit();

		getWindow().getDecorView().setBackgroundColor(getThemedAttributeColor(R.attr.drawerBackgroundColor));
	}

	@Override
	protected Integer getLayoutId()
	{
		return R.layout.activity_preferences;
	}

	@Override
	protected Integer getToolbarId()
	{
		return R.id.toolbar;
	}

	@Override
	public void onBackPressed()
	{
		setResult(RESULT_OK);
		super.onBackPressed();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		prefsFragment.onActivityResult(requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}

	public static class PrefsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener
	{
		private static final int             TTS_DATA_CHECK_CODE = 7;
		private static final int             REQUEST_BARCODES    = 10001;
		private              int             nrOfBarcodes;
		private              String          preferenceKey;
		private              PreferenceUtils prefs;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			View v = super.onCreateView(inflater, container, savedInstanceState);
			if (v != null)
			{
				ListView lv = (ListView) v.findViewById(android.R.id.list);
				lv.setPadding(0, 0, 0, 0);
			}
			return v;
		}

		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);

			prefs = new PreferenceUtils(getActivity());

			/* Load the preferences from an XML resource */
			addPreferencesFromResource(R.xml.prefs);

			/* Update the summary */
			findPreference(PreferenceUtils.PREFS_DELETE_ROW_TOKEN).setSummary(getString(R.string.preferences_delete_row_token_summary, prefs.getString(PreferenceUtils.PREFS_DELETE_ROW_TOKEN)));
			findPreference(PreferenceUtils.PREFS_DELETE_CELL_TOKEN).setSummary(getString(R.string.preferences_delete_cell_token_summary, prefs.getString(PreferenceUtils.PREFS_DELETE_CELL_TOKEN)));
			findPreference(PreferenceUtils.PREFS_NULL_TOKEN).setSummary(getString(R.string.preferences_null_token_summary, prefs.getString(PreferenceUtils.PREFS_NULL_TOKEN)));

			findPreference(PreferenceUtils.PREFS_DELETE_ROW_TOKEN).setOnPreferenceClickListener(this);
			findPreference(PreferenceUtils.PREFS_DELETE_CELL_TOKEN).setOnPreferenceClickListener(this);
			findPreference(PreferenceUtils.PREFS_NULL_TOKEN).setOnPreferenceClickListener(this);
			findPreference(PreferenceUtils.PREFS_SHOW_EULA).setOnPreferenceClickListener(this);
			findPreference(PreferenceUtils.PREFS_SHOW_CHANGELOG).setOnPreferenceClickListener(this);
			findPreference(PreferenceUtils.PREFS_EXPORT_BARCODE_PROPERTIES).setOnPreferenceClickListener(this);
		}

		@Override
		public void onResume()
		{
			super.onResume();
			/* Listen to changes to the preferences */
			getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onPause()
		{
			getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
			super.onPause();
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences pref, String key)
		{
			/* If the theme was changed */
			switch (key)
			{
				case PreferenceUtils.PREFS_GA_OPT_OUT:
					/* Disable GA tracking */
//					GoogleAnalytics.getInstance(getActivity().getApplicationContext()).setAppOptOut(!prefs.getBoolean(PreferenceUtils.PREFS_GA_OPT_OUT, true));
					break;

				case PreferenceUtils.PREF_THEME:
					/* Recreate the preferences screen to show the changes */
					GoogleAnalyticsUtils.track(getActivity(), tracker, FirebaseAnalytics.Event.SELECT_CONTENT, getString(R.string.ga_event_category_preferences), getString(R.string.ga_event_action_theme_changed));
					ThemeUtils.notifyListeners();

					new Handler().postDelayed(new Runnable()
					{
						@Override
						public void run()
						{
							getActivity().recreate();
						}
					}, 1);
					break;

				case PreferenceUtils.PREFS_VOICE_FEEDBACK:
					final SwitchPreference voiceFeedbackPreference = (SwitchPreference) findPreference(key);

					/* Check if the user has already been informed that a data download may be necessary */
					final boolean hasBeenWarned = prefs.getBoolean(PreferenceUtils.PREFS_VOICE_FEEDBACK_WARNING, false);
					final boolean voiceFeedbackEnabled = prefs.getBoolean(PreferenceUtils.PREFS_VOICE_FEEDBACK, false);

					if (!hasBeenWarned && voiceFeedbackEnabled)
					{
						new AlertDialog.Builder(getActivity())
								.setTitle(R.string.dialog_title_download_warning)
								.setMessage(R.string.dialog_message_download_warning)
								.setPositiveButton(R.string.general_yes, (dialog, which) -> {
									Intent checkIntent = new Intent();
									checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
									startActivityForResult(checkIntent, TTS_DATA_CHECK_CODE);
								})
								.setNegativeButton(R.string.general_no, (dialog, which) -> voiceFeedbackPreference.setChecked(!voiceFeedbackPreference.isChecked()))
								.show();
					}

					GoogleAnalyticsUtils.track(getActivity(), tracker, FirebaseAnalytics.Event.SELECT_CONTENT, getString(R.string.ga_event_category_preferences), getString(R.string.ga_event_action_read_back_changed));
					break;
			}
		}

		@Override
		public boolean onPreferenceClick(Preference preference)
		{
			final String key = preference.getKey();

			switch (key)
			{
				case PreferenceUtils.PREFS_DELETE_ROW_TOKEN:
				case PreferenceUtils.PREFS_DELETE_CELL_TOKEN:
				case PreferenceUtils.PREFS_NULL_TOKEN:
					ArrayAdapter<String> dialogAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.select_dialog_item);
					dialogAdapter.add(getString(R.string.dialog_list_token_type));
					dialogAdapter.add(getString(R.string.dialog_list_token_scan));

					new AlertDialog.Builder(getActivity()).setAdapter(dialogAdapter, (dialog, which) -> {
						preferenceKey = key;
						switch (which)
						{
							case 0:
								final EditText token = new EditText(getActivity());

								new AlertDialog.Builder(getActivity())
										.setTitle(R.string.dialog_title_token)
										.setMessage(R.string.dialog_message_token)
										.setView(token)
										.setPositiveButton(android.R.string.ok, new OnClickListener()
										{
											public void onClick(DialogInterface dialog, int whichButton)
											{
												String value = token.getText().toString();
												updatePreference(value);
											}
										})
										.setNegativeButton(android.R.string.cancel, new OnClickListener()
										{
											public void onClick(DialogInterface dialog, int whichButton)
											{
											}
										})
										.show();
								break;
							case 1:
								IntentIntegrator integrator = new IntentIntegrator(getActivity());
								integrator.initiateScan();
								break;
						}
					}).show();

					return true;

				case PreferenceUtils.PREFS_EXPORT_BARCODE_PROPERTIES:
					startActivity(new Intent(getActivity(), ExportSettingsActivity.class));
					return true;

				case PreferenceUtils.PREFS_SHOW_EULA:
					EulaUtils.showEula((GerminateScanActivity) getActivity(), false, null);

					GoogleAnalyticsUtils.track(getActivity(), tracker, FirebaseAnalytics.Event.SELECT_CONTENT, getString(R.string.ga_event_category_preferences), getString(R.string.ga_event_action_show_eula));
					return true;

				case PreferenceUtils.PREFS_SHOW_CHANGELOG:
					startActivity(new Intent(getActivity(), ChangelogActivity.class));

					GoogleAnalyticsUtils.track(getActivity(), tracker, FirebaseAnalytics.Event.SELECT_CONTENT, getString(R.string.ga_event_category_preferences), getString(R.string.ga_event_action_show_changelog));
					return true;
			}

			return false;
		}

		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data)
		{
			if (requestCode == TTS_DATA_CHECK_CODE)
			{
				if (resultCode != TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
				{
					/* Missing data, install it */
					Intent installIntent = new Intent();
					installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
					startActivity(installIntent);
				}
			}
			else
			{
				IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
				/* See if it's the camera barcode scanner */
				if (scanResult != null)
				{
					String code = scanResult.getContents();

					if (!StringUtils.isEmpty(code))
					{
						updatePreference(code);
					}
				}
				else
				{
					super.onActivityResult(requestCode, resultCode, data);
				}
			}
		}

		private void updatePreference(String code)
		{
			// Set the text
			String key = null;
			String value = null;

			if (PreferenceUtils.PREFS_DELETE_ROW_TOKEN.equals(preferenceKey))
			{
				key = PreferenceUtils.PREFS_DELETE_ROW_TOKEN;
				value = getString(R.string.preferences_delete_row_token_summary, code);

				GoogleAnalyticsUtils.track(getActivity(), tracker, FirebaseAnalytics.Event.SELECT_CONTENT, getString(R.string.ga_event_category_preferences), getString(R.string.ga_event_action_delete_row_token));
			}
			else if (PreferenceUtils.PREFS_DELETE_CELL_TOKEN.equals(preferenceKey))
			{
				key = PreferenceUtils.PREFS_DELETE_CELL_TOKEN;
				value = getString(R.string.preferences_delete_cell_token_summary, code);

				GoogleAnalyticsUtils.track(getActivity(), tracker, FirebaseAnalytics.Event.SELECT_CONTENT,  getString(R.string.ga_event_category_preferences), getString(R.string.ga_event_action_delete_row_token));
			}
			else if (PreferenceUtils.PREFS_NULL_TOKEN.equals(preferenceKey))
			{
				key = PreferenceUtils.PREFS_NULL_TOKEN;
				value = getString(R.string.preferences_null_token_summary, code);

				GoogleAnalyticsUtils.track(getActivity(), tracker, FirebaseAnalytics.Event.SELECT_CONTENT, getString(R.string.ga_event_category_preferences), getString(R.string.ga_event_action_null_token));
			}

			if (!StringUtils.isEmpty(key, value))
			{
				findPreference(key).setSummary(value);
				prefs.putString(key, code);
			}
		}
	}
}
