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

import android.app.*;
import android.content.*;
import android.support.v7.widget.*;
import android.text.*;
import android.view.*;
import android.widget.*;

import java.io.*;
import java.util.*;

import butterknife.*;
import uk.ac.hutton.android.germinatescan.R;
import uk.ac.hutton.android.germinatescan.activity.*;
import uk.ac.hutton.android.germinatescan.database.*;
import uk.ac.hutton.android.germinatescan.database.manager.*;
import uk.ac.hutton.android.germinatescan.util.*;

/**
 * The {@link DatasetAdapter} takes care of all the {@link Dataset}s.
 *
 * @author Sebastian Raubach
 */
public class DatasetAdapter extends RecyclerView.Adapter<DatasetAdapter.ViewHolder>
{
	private Activity        context;
	private PreferenceUtils utils;
	private List<Dataset>   datasets;
	private PreferenceUtils prefs;

	public DatasetAdapter(Activity context, List<Dataset> datasets)
	{
		this.context = context;
		this.prefs = new PreferenceUtils(context);
		utils = new PreferenceUtils(context);
		this.datasets = datasets;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		/* Create a new view from the layout file */
		final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.dataset_view, parent, false);

		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, final int position)
	{
		final Dataset item = datasets.get(position);

		holder.name.setText(item.getName());
		holder.updatedOn.setText(Barcode.DATE_FORMAT.format(item.getUpdatedOn()));
		holder.mode.setText(BarcodeExampleAdapter.BarcodeExample.getForNumber(item.getBarcodesPerRow()).getName(context));

		holder.view.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				utils.putLong(PreferenceUtils.PREFS_SELECTED_DATASET_ID, datasets.get(holder.getAdapterPosition()).getId());

				context.setResult(Activity.RESULT_OK);
				context.finish();
			}
		});
		holder.view.setOnLongClickListener(new View.OnLongClickListener()
		{
			@Override
			public boolean onLongClick(View view)
			{
				final EditText name = new EditText(context);
				name.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
				name.setText(item.getName());
				new AlertDialog.Builder(context)
						.setTitle(R.string.dialog_title_rename_dataset)
						.setView(name)
						.setPositiveButton(R.string.general_save, new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialogInterface, int i)
							{
								String newName = name.getText().toString();

								if (!StringUtils.isEmpty(newName))
								{
									item.setName(newName);
									new DatasetManager(context, item.getId()).update(item);
									notifyItemChanged(holder.getAdapterPosition());
								}
							}
						})
						.setNegativeButton(R.string.general_cancel, null)
						.show();
				return true;
			}
		});
		holder.deleteButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				new AlertDialog.Builder(context)
						.setTitle(R.string.dialog_title_delete_dataset)
						.setMessage(context.getString(R.string.dialog_message_delete_dataset, item.getName()))
						.setPositiveButton(R.string.general_yes, new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialogInterface, int i)
							{
								try
								{
									long id = item.getId();
									new DatasetManager(context, id).remove();

									long selectedId = prefs.getLong(PreferenceUtils.PREFS_SELECTED_DATASET_ID, -1L);

									if (id == selectedId)
									{
										datasets.remove(item);
										if (datasets.size() > 0)
											prefs.putLong(PreferenceUtils.PREFS_SELECTED_DATASET_ID, datasets.get(0).getId());
										else
											prefs.remove(PreferenceUtils.PREFS_SELECTED_DATASET_ID);
									}
								}
								catch (IOException e)
								{
									e.printStackTrace();
								}

								if (context instanceof DatasetActivity)
									((DatasetActivity) context).update();
							}
						})
						.setNegativeButton(R.string.general_no, null)
						.show();
			}
		});
	}

	@Override
	public int getItemCount()
	{
		return datasets.size();
	}

	static class ViewHolder extends RecyclerView.ViewHolder
	{
		View view;
		@BindView(R.id.dataset_name)
		TextView name;
		@BindView(R.id.dataset_mode)
		TextView mode;
		@BindView(R.id.dataset_updated_on)
		TextView updatedOn;
		@BindView(R.id.dataset_delete_button)
		Button   deleteButton;

		ViewHolder(View v)
		{
			super(v);

			view = v;
			ButterKnife.bind(this, v);
		}
	}
}