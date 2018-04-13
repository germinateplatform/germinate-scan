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
import android.view.*;
import android.widget.*;

import java.util.*;

import uk.ac.hutton.android.germinatescan.*;
import uk.ac.hutton.android.germinatescan.activity.*;
import uk.ac.hutton.android.germinatescan.database.*;
import uk.ac.hutton.android.germinatescan.database.manager.*;
import uk.ac.hutton.android.germinatescan.util.*;

/**
 * @author Sebastian Raubach
 */
public class RecyclerGridAdapter extends RecyclerView.Adapter<RecyclerGridAdapter.ViewHolder>
{
	private final Context        context;
	private final List<Barcode>  items;
	private final int            nrOfColumns;
	private       BarcodeManager barcodeManager;

	public RecyclerGridAdapter(Context context, long datasetId, List<Barcode> items)
	{
		this.context = context;
		this.items = items;
		barcodeManager = new BarcodeManager(context, datasetId);
		this.nrOfColumns = new DatasetManager(context, datasetId).getById(datasetId).getBarcodesPerRow();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_view_item, parent, false));
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position)
	{
		final Barcode theBarcode = items.get(position);

		if (theBarcode == null || theBarcode.isNullBarcode())
		{
			holder.barcode.setText(null);
			holder.date.setText(null);
			holder.lat.setText(null);
			holder.lng.setText(null);
		}
		else
		{
			holder.barcode.setText(theBarcode.getBarcode());
			holder.date.setText(theBarcode.getFormattedTimestamp());
			holder.lat.setText(theBarcode.getLatitudeString());
			holder.lng.setText(theBarcode.getLongitudeString());
		}

		holder.lat.setVisibility(LocationUtils.isEmpty(holder.lat.getText().toString()) ? View.GONE : View.VISIBLE);
		holder.lng.setVisibility(LocationUtils.isEmpty(holder.lng.getText().toString()) ? View.GONE : View.VISIBLE);

		holder.itemView.setTag(theBarcode);
	}

	@Override
	public int getItemCount()
	{
		return items.size();
	}

	public Barcode getItem(int position)
	{
		return items.get(position);
	}

	public List<Barcode> getItems()
	{
		return Collections.unmodifiableList(items);
	}

	public int getColumnIndex(Barcode barcode)
	{
		int index = items.indexOf(barcode);

		if (index == -1)
		{
			return -1;
		}
		else
		{
			return MathUtils.modulo(index, nrOfColumns);
		}
	}

	public int getCurrentColumnIndex()
	{
		int index = items.size() - 1;

		return MathUtils.modulo(index, nrOfColumns);
	}

	public List<Barcode> getItemsInRow(Barcode barcode)
	{
		int size = items.size();
		int columnIndex = getColumnIndex(barcode);
		int index = items.indexOf(barcode);

		if (columnIndex == -1 || index == -1 || size == 0)
		{
			return new ArrayList<>();
		}

		int startIndex = index - columnIndex;
		int stopIndex = Math.min(size, index + (nrOfColumns - columnIndex));

		return new ArrayList<>(items.subList(startIndex, stopIndex));
	}

	public void removeAll(List<Barcode> toDelete)
	{
		for (int i = toDelete.size() - 1; i >= 0; i--)
		{
			remove(toDelete.get(i));
		}
	}

	public void add(Barcode barcode)
	{
		barcodeManager.add(barcode);

		items.add(barcode);

		notifyItemInserted(items.size() - 1);
	}

	public void remove(Barcode barcode)
	{
		int index = items.indexOf(barcode);
		int size = items.size();

        /* Convert it to a null/skip token first */
		barcode.setBarcode("");

        /* Then get the whole row */
		List<Barcode> row = getItemsInRow(barcode);

		if (row.size() < 1)
			return;

        /* And check if they are all null/skip tokens */
		boolean emptyRow = true;

		for (Barcode b : row)
		{
			if (!b.isNullBarcode())
			{
				emptyRow = false;
				break;
			}
		}

		/* If the whole row consists of null/skip tokens remove the whole row */
		if (emptyRow)
		{
			int startPosition = items.indexOf(row.get(0));
			int deleteSize = row.size();
			for (int i = deleteSize - 1; i >= 0; i--)
			{
				barcodeManager.delete(row.get(i));
				items.remove(row.get(i));
			}
			notifyItemRangeRemoved(startPosition, deleteSize);
		}
		/* If it's the last item in the grid, delete it */
		else if (index == size - 1)
		{
			barcodeManager.delete(barcode);
			items.remove(barcode);
			notifyItemRemoved(index);
		}
		/* Otherwise, just update the current item */
		else
		{
			barcodeManager.update(barcode);
			notifyItemChanged(index);
		}
	}

	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener
	{
		private final TextView barcode;
		private final TextView date;
		private final TextView lat;
		private final TextView lng;

		public ViewHolder(final View itemView)
		{
			super(itemView);
			barcode = (TextView) itemView.findViewById(R.id.barcode_item_barcode);
			date = (TextView) itemView.findViewById(R.id.barcode_item_date);
			lat = (TextView) itemView.findViewById(R.id.barcode_item_lat);
			lng = (TextView) itemView.findViewById(R.id.barcode_item_lng);

			itemView.setOnLongClickListener(this);
		}

		@Override
		public boolean onLongClick(final View v)
		{
			int position = getAdapterPosition();
			final Barcode item = getItem(position);

			boolean hasImages = !CollectionUtils.isEmpty(item.getImages());

			ArrayAdapter<String> dialogAdapter = new ArrayAdapter<>(context, android.R.layout.select_dialog_item);
			dialogAdapter.add(context.getString(R.string.dialog_list_delete_barcode));
			dialogAdapter.add(context.getString(R.string.dialog_list_delete_row));

			if (hasImages)
			{
				dialogAdapter.add(context.getString(R.string.dialog_list_show_images));
			}

			new AlertDialog.Builder(context).setAdapter(dialogAdapter, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					switch (which)
					{
						case 0:
							remove(item);
							break;

						case 1:
							removeAll(getItemsInRow(item));
							break;

						case 2:
							BarcodeReader.INSTANCE.showImages(v, item);
							break;
					}
				}
			}).show();

			return true;
		}
	}
}
