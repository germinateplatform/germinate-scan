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

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import java.util.*;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.*;
import butterknife.*;
import uk.ac.hutton.android.germinatescan.R;
import uk.ac.hutton.android.germinatescan.adapter.StringReorderDeleteAdapter;
import uk.ac.hutton.android.germinatescan.database.Dataset;
import uk.ac.hutton.android.germinatescan.database.manager.DatasetManager;
import uk.ac.hutton.android.germinatescan.util.*;

/**
 * @author Sebastian Raubach
 */
public class PhenotypeActivity extends ThemedActivity
{
	public static final String EXTRA_DATASET_ID = "DATASET_ID";

	@BindView(R.id.phenotype_text)
	EditText     phenotypeInput;
	@BindView(R.id.phenotype_add_button)
	ImageButton  addButton;
	@BindView(R.id.phenotype_list)
	RecyclerView phenotypeList;

	private ItemTouchHelper            helper;
	private StringReorderDeleteAdapter adapter;
	private DatasetManager             datasetManager;
	private Dataset                    dataset;

	@Override
	protected Integer getLayoutId()
	{
		return R.layout.activity_load_phenotypes;
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

		Bundle extras = getIntent().getExtras();

		if (extras != null)
		{
			long datasetId = extras.getLong(EXTRA_DATASET_ID, -1L);

			if (datasetId != -1)
			{
				datasetManager = new DatasetManager(getApplicationContext(), datasetId);
				dataset = datasetManager.getById(datasetId);
			}
		}

		adapter = new StringReorderDeleteAdapter(this, new ArrayList<String>(), new StringReorderDeleteAdapter.OnStartDragListener()
		{
			@Override
			public void onStartDrag(RecyclerView.ViewHolder viewHolder)
			{
				helper.startDrag(viewHolder);
			}
		});

		ItemTouchHelper.Callback callback = new StringReorderDeleteAdapter.SimpleItemTouchHelperCallback(adapter);
		helper = new ItemTouchHelper(callback);
		helper.attachToRecyclerView(phenotypeList);

		phenotypeList.setAdapter(adapter);
		phenotypeList.setLayoutManager(new LinearLayoutManager(this));

		/* Listen for ENTER key presses */
		phenotypeInput.setOnKeyListener(new View.OnKeyListener()
		{
			@Override
			public boolean onKey(View view, int keyCode, KeyEvent event)
			{
				if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER))
				{
					adapter.addItem(phenotypeInput.getText().toString());
					phenotypeInput.setText("");
					return true;
				}

				return false;
			}
		});

		addButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				adapter.addItem(phenotypeInput.getText().toString());

				phenotypeInput.setText("");
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.phenotype_menu, menu);

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

			case R.id.menu_import_phenotypes:
				importPhenotypes();
				break;

			case R.id.menu_import_finish:
				finishImport();
				break;
		}

		return true;
	}

	private void finishImport()
	{
		Intent data = new Intent();

		List<String> items = adapter.getItems();

		if (!CollectionUtils.isEmpty(items))
		{
			dataset.setPreloadedPhenotypes(items);
		}
		else
		{
			dataset.setPreloadedPhenotypes(null);
		}
		dataset.setCurrentPhenotype(0);
		datasetManager.update(dataset);

		if (getParent() == null)
		{
			setResult(Activity.RESULT_OK, data);
		}
		else
		{
			getParent().setResult(Activity.RESULT_OK, data);
		}
		finish();
	}

	private void importPhenotypes()
	{
		new AlertDialog.Builder(this)
				.setTitle(R.string.dialog_title_phenotype)
				.setMessage(R.string.dialog_message_phenotype)
				.setPositiveButton(R.string.general_yes, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
						ClipData data = clipboard.getPrimaryClip();
						if (data != null && data.getItemCount() > 0)
						{
							ClipData.Item item = data.getItemAt(0);
							String value = item.getText().toString();

							String[] values = value.split("\n");

							for (String v : values)
							{
								if (!StringUtils.isEmpty(v))
									adapter.addItem(v);
							}
						}
					}
				})
				.setNegativeButton(R.string.general_no, null)
				.show();
	}
}
