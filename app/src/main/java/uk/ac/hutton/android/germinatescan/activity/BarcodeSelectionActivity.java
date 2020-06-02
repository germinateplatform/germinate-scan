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

import android.content.Intent;
import android.os.*;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import butterknife.*;
import uk.ac.hutton.android.germinatescan.R;

/**
 * @author Sebastian Raubach
 */
public class BarcodeSelectionActivity extends ThemedActivity
{
	public static final String EXTRA_NR_OF_BARCODES = "extra_nr_of_barcodes";

	@BindView(R.id.toolbar)
	Toolbar toolbar;

	@Override
	protected Integer getLayoutId()
	{
		return R.layout.activity_barcode_selection;
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

		ButterKnife.bind(this);

		setSupportActionBar(toolbar);

		/* Set the toolbar as the action bar */
		if (getSupportActionBar() != null)
		{
			/* Set the title */
			getSupportActionBar().setTitle(R.string.title_activity_barcode_selection);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setHomeButtonEnabled(true);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.color_primary_dark));
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

	public void onBarcodeSelected(int value)
	{
		Intent intent = new Intent();
		intent.putExtra(EXTRA_NR_OF_BARCODES, value);
		setResult(RESULT_OK, intent);
		finish();
	}
}
