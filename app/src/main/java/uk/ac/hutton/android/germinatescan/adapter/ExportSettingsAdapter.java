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

package uk.ac.hutton.android.germinatescan.adapter;

import android.content.*;
import android.preference.PreferenceManager;
import android.view.*;
import android.widget.*;

import java.util.List;

import uk.ac.hutton.android.germinatescan.R;
import uk.ac.hutton.android.germinatescan.activity.BarcodeReader;
import uk.ac.hutton.android.germinatescan.database.Barcode;
import uk.ac.hutton.android.germinatescan.util.PreferenceUtils;

/**
 * @author Sebastian Raubach
 */
public class ExportSettingsAdapter extends ArrayAdapter<Barcode.BarcodeProperty>
{
	private final Context                       context;
	private final List<Barcode.BarcodeProperty> items;

	public ExportSettingsAdapter(Context context, int textViewResourceId, List<Barcode.BarcodeProperty> items)
	{
		super(context, textViewResourceId, items);
		this.context = context;
		this.items = items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		LinearLayout theView = (LinearLayout) convertView;

		/* Check if we have to create a new View */
		boolean createNew = convertView == null;
		if (createNew)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			theView = (LinearLayout) inflater.inflate(R.layout.helper_settings_item, parent, false);
			theView.setOrientation(LinearLayout.HORIZONTAL);
		}

		TextView text = (TextView) theView.findViewById(R.id.export_item_text);
		ImageView img = (ImageView) theView.findViewById(R.id.export_item_delete);

		final Barcode.BarcodeProperty property = items.get(position);

		text.setText(property.toString());

		img.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				items.remove(property);
				ExportSettingsAdapter.this.notifyDataSetChanged();
			}
		});

		return theView;
	}

	@Override
	public void notifyDataSetChanged()
	{
		super.notifyDataSetChanged();

		SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(BarcodeReader.INSTANCE).edit();
		edit.putString(PreferenceUtils.PREFS_EXPORT_BARCODE_PROPERTIES, Barcode.BarcodeProperty.joinNames(items, ","));
		edit.apply();
	}
}
