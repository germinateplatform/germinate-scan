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

import android.app.*;
import android.content.*;
import android.database.*;
import android.os.*;
import android.preference.*;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.*;

import java.util.*;

import uk.ac.hutton.android.germinatescan.*;
import uk.ac.hutton.android.germinatescan.adapter.*;
import uk.ac.hutton.android.germinatescan.database.*;
import uk.ac.hutton.android.germinatescan.util.*;

/**
 * {@link uk.ac.hutton.android.germinatescan.activity.ExportSettingsActivity} allows the user to select the barcode options they want so be exported.
 * They can add/remove/reorder them.
 *
 * @author Sebastian Raubach
 */
public class ExportSettingsActivity extends ThemedActivity
{
	/** The ListView containing the selected properties */
	private ListView list;
	/** The TextView showing an example of the selected export format */
	private TextView example;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		list = findViewById(R.id.export_settings_list);
		example = findViewById(R.id.export_settings_example);
		/* The SwitchCompat used to switch between matrix and row export format */
		SwitchCompat matrixSwitch = findViewById(R.id.matrix_export_switch);
		SwitchCompat timeGpsPerRow = findViewById(R.id.single_time_gps_perrow_switch);

		final PreferenceUtils prefs = new PreferenceUtils(this);

		matrixSwitch.setChecked(prefs.getBoolean(PreferenceUtils.PREFS_EXPORT_MATRIX_FORMAT, true));
		matrixSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				prefs.putBoolean(PreferenceUtils.PREFS_EXPORT_MATRIX_FORMAT, isChecked);
			}
		});
		timeGpsPerRow.setChecked(prefs.getBoolean(PreferenceUtils.PREFS_EXPORT_MATRIX_SINGLE_TIME_GPS_PERROW, true));
		timeGpsPerRow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				prefs.putBoolean(PreferenceUtils.PREFS_EXPORT_MATRIX_SINGLE_TIME_GPS_PERROW, isChecked);
			}
		});

        /* Get selected properties */
		List<Barcode.BarcodeProperty> properties = Barcode.BarcodeProperty.getUsedProperties();
		final ArrayAdapter adapter = new ExportSettingsAdapter(this, R.layout.helper_settings_item, properties);

        /* Update the example */
		Barcode.BarcodeProperty[] items = properties.toArray(new Barcode.BarcodeProperty[properties.size()]);
		updateExample(items);

        /* Listen for dataset changes */
		adapter.registerDataSetObserver(new DataSetObserver()
		{
			@Override
			public void onChanged()
			{
				super.onChanged();

                /* Get the selected items */
				Barcode.BarcodeProperty[] items = new Barcode.BarcodeProperty[adapter.getCount()];

				for (int i = 0; i < items.length; i++)
				{
					items[i] = (Barcode.BarcodeProperty) adapter.getItem(i);
				}

                /* Update the example */
				updateExample(items);
			}
		});

		list.setAdapter(adapter);
	}

	/**
	 * Updates the example barcode format
	 *
	 * @param items The array of BarcodeProperties
	 */
	private void updateExample(Barcode.BarcodeProperty[] items)
	{
		String exampleText = "";

		if (items != null && items.length > 0)
		{
			StringBuilder builder = new StringBuilder(items[0].getExample());

			for (int i = 1; i < items.length; i++)
			{
				builder.append(BarcodeReader.DELIMITER)
					   .append(items[i].getExample());
			}

			exampleText = builder.toString();
		}

		example.setText(exampleText);
	}

	@Override
	protected Integer getLayoutId()
	{
		return R.layout.activity_export_settings;
	}

	@Override
	protected Integer getToolbarId()
	{
		return R.id.toolbar;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.export_settings_menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				super.onOptionsItemSelected(item);
				break;

			case R.id.menu_export_add:
				/* Show the add properties dialog */
				showAddDialog();
				break;

			case R.id.menu_export_clear:
				/* Clear the user selection */
				SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
				edit.putString(PreferenceUtils.PREFS_EXPORT_BARCODE_PROPERTIES, "");
				edit.apply();

				ArrayAdapter adapter = (ArrayAdapter) list.getAdapter();
				adapter.clear();
				break;
		}

		return true;
	}

	/**
	 * Opens an AlertDialog with the unused barcode properties, so that the user can select the ones s/he is interested in
	 */
	private void showAddDialog()
	{
		/* Get the unused properties */
		final List<Barcode.BarcodeProperty> unusedItems = Barcode.BarcodeProperty.getUnusedProperties();

        /* Convert to Strings */
		List<String> items = new ArrayList<>(unusedItems.size());

		for (int i = 0; i < unusedItems.size(); i++)
		{
			items.add(unusedItems.get(i).toString());
		}

        /* Create a new adapter for the dialog */
		final ArrayAdapter<String> dialogAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, items);
		/* Get the adapter of the ListView */
		final ArrayAdapter<Barcode.BarcodeProperty> listAdapter = (ArrayAdapter) list.getAdapter();

		ListView lv = new ListView(this);
		lv.setAdapter(dialogAdapter);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> arg0, View view, int pos, long id)
			{
				/* Whenever the user selects a new item */
				Barcode.BarcodeProperty item = unusedItems.get(pos);

                /* Add it to the ListView's Adapter */
				listAdapter.add(item);

                /* Remove it from the local Adapter */
				dialogAdapter.remove(dialogAdapter.getItem(pos));
				unusedItems.remove(item);
			}
		});

        /* Show the AlertDialog */
		new AlertDialog.Builder(this)
				.setTitle(R.string.dialog_title_add_export_property)
				.setView(lv)
				.setPositiveButton(R.string.general_close, null)
				.show();
	}
}
