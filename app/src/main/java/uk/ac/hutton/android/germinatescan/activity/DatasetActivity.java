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
import android.os.Bundle;
import android.text.InputType;
import android.view.*;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import androidx.recyclerview.widget.*;
import butterknife.*;
import uk.ac.hutton.android.germinatescan.R;
import uk.ac.hutton.android.germinatescan.adapter.DatasetAdapter;
import uk.ac.hutton.android.germinatescan.database.Dataset;
import uk.ac.hutton.android.germinatescan.database.manager.DatasetManager;
import uk.ac.hutton.android.germinatescan.util.*;

/**
 * @author Sebastian Raubach
 */

public class DatasetActivity extends ThemedActivity
{
	private static final int REQUEST_CODE_BARCODE = 1 << 10;

	@BindView(R.id.dataset_recycler_view)
	RecyclerView recyclerView;

	private DatasetManager datasetManager;

	private String                    newDatasetName;
	private GridSpacingItemDecoration decorator;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		ButterKnife.bind(this);

		update();
	}

	public void update()
	{
		if (decorator != null)
			recyclerView.removeItemDecoration(decorator);

		datasetManager = new DatasetManager(this, -1L);
		List<Dataset> datasets = datasetManager.getAll();

		int valueInPixels = (int) getResources().getDimension(R.dimen.activity_vertical_margin) / 2;
		decorator = new GridSpacingItemDecoration(1, valueInPixels, valueInPixels, valueInPixels);
		recyclerView.addItemDecoration(decorator);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setItemAnimator(null);

		recyclerView.setAdapter(new DatasetAdapter(this, datasets));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.datasets_menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menu_add_dataset:
				final EditText name = new EditText(this);
				name.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

				new AlertDialog.Builder(this)
						.setTitle(R.string.dialog_title_add_dataset)
						.setMessage(R.string.dialog_message_add_dataset)
						.setView(name)
						.setPositiveButton(R.string.general_add, new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int whichButton)
							{
								newDatasetName = name.getText().toString();
								startActivityForResult(new Intent(getApplicationContext(), BarcodeSelectionActivity.class), REQUEST_CODE_BARCODE);
							}
						})
						.setNegativeButton(R.string.general_cancel, new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int whichButton)
							{
							}
						})
						.show();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed()
	{
		SnackbarUtils.showError(getSnackbarParentView(), getString(R.string.dataset_select), Snackbar.LENGTH_LONG);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == REQUEST_CODE_BARCODE)
		{
			if (resultCode == Activity.RESULT_OK && data.getExtras() != null)
			{
				int nrOfBarcodes = data.getExtras().getInt(BarcodeSelectionActivity.EXTRA_NR_OF_BARCODES);

				Dataset dataset = new Dataset(newDatasetName);
				dataset.setBarcodesPerRow(nrOfBarcodes);
				datasetManager.add(dataset);
				update();
			}

			newDatasetName = null;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected Integer getLayoutId()
	{
		return R.layout.activity_datasets;
	}

	@Override
	protected Integer getToolbarId()
	{
		return R.id.toolbar;
	}
}
