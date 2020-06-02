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

import android.content.Context;
import android.graphics.Canvas;
import android.view.*;
import android.widget.*;

import java.util.*;

import androidx.recyclerview.widget.*;
import uk.ac.hutton.android.germinatescan.R;
import uk.ac.hutton.android.germinatescan.util.*;

/**
 * @author Sebastian Raubach
 */

public class StringReorderDeleteAdapter extends RecyclerView.Adapter<StringReorderDeleteAdapter.ItemViewHolder>
		implements ItemTouchHelperAdapter
{
	private final OnStartDragListener mDragStartListener;
	private       List<String>        mItems = new ArrayList<>();

	public StringReorderDeleteAdapter(Context context, List<String> items, OnStartDragListener dragStartListener)
	{
		mDragStartListener = dragStartListener;
		mItems = items;
	}

	@Override
	public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.helper_list_item_reorder, parent, false);
		return new ItemViewHolder(view);
	}

	@Override
	public void onBindViewHolder(final ItemViewHolder holder, int position)
	{
		holder.textView.setText(mItems.get(position));

		// Start a drag whenever the handle view it touched
		holder.handleView.setOnTouchListener(new View.OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if (event.getActionMasked() == MotionEvent.ACTION_DOWN)
				{
					mDragStartListener.onStartDrag(holder);
				}
				return false;
			}
		});
	}

	@Override
	public void onItemDismiss(int position)
	{
		mItems.remove(position);
		notifyItemRemoved(position);
	}

	@Override
	public boolean onItemMove(int fromPosition, int toPosition)
	{
		Collections.swap(mItems, fromPosition, toPosition);
		notifyItemMoved(fromPosition, toPosition);
		return true;
	}

	@Override
	public int getItemCount()
	{
		return mItems.size();
	}

	public List<String> getItems()
	{
		return mItems;
	}

	public void addItem(String item)
	{
		if (item.endsWith("\n"))
			item = item.replace("\n", "");
		mItems.add(item);
		notifyItemInserted(mItems.size() - 1);
	}

	/**
	 * Listener for manual initiation of a drag.
	 */
	public interface OnStartDragListener
	{

		/**
		 * Called when a view is requesting a start of a drag.
		 *
		 * @param viewHolder The holder of the view to drag.
		 */
		void onStartDrag(RecyclerView.ViewHolder viewHolder);
	}

	/**
	 * Simple example of a view holder that implements {@link ItemTouchHelperViewHolder} and has a "handle" view that initiates a drag event when
	 * touched.
	 */
	static class ItemViewHolder extends RecyclerView.ViewHolder implements
			ItemTouchHelperViewHolder
	{

		public final TextView  textView;
		public final ImageView handleView;

		public ItemViewHolder(View itemView)
		{
			super(itemView);
			textView = (TextView) itemView.findViewById(R.id.list_reorder_item_text);
			handleView = (ImageView) itemView.findViewById(R.id.list_reorder_item_handle);
		}

		@Override
		public void onItemSelected()
		{
//			itemView.setBackgroundColor(Color.LTGRAY);
		}

		@Override
		public void onItemClear()
		{
			itemView.setBackgroundColor(0);
		}
	}

	/**
	 * An implementation of {@link ItemTouchHelper.Callback} that enables basic drag & drop and swipe-to-dismiss. Drag events are automatically
	 * started by an item long-press.<br/> </br/> Expects the <code>RecyclerView.Adapter</code> to listen for {@link ItemTouchHelperAdapter} callbacks
	 * and the <code>RecyclerView.ViewHolder</code> to implement {@link ItemTouchHelperViewHolder}.
	 *
	 * @author Paul Burke (ipaulpro)
	 */
	public static class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback
	{

		public static final float ALPHA_FULL = 1.0f;

		private final ItemTouchHelperAdapter mAdapter;

		public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter)
		{
			mAdapter = adapter;
		}

		@Override
		public boolean isLongPressDragEnabled()
		{
			return true;
		}

		@Override
		public boolean isItemViewSwipeEnabled()
		{
			return true;
		}

		@Override
		public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder)
		{
			// Set movement flags based on the layout manager
			if (recyclerView.getLayoutManager() instanceof GridLayoutManager)
			{
				final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
				final int swipeFlags = 0;
				return makeMovementFlags(dragFlags, swipeFlags);
			}
			else
			{
				final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
				final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
				return makeMovementFlags(dragFlags, swipeFlags);
			}
		}

		@Override
		public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target)
		{
			if (source.getItemViewType() != target.getItemViewType())
			{
				return false;
			}

			// Notify the adapter of the move
			mAdapter.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
			return true;
		}

		@Override
		public void onSwiped(RecyclerView.ViewHolder viewHolder, int i)
		{
			// Notify the adapter of the dismissal
			mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
		}

		@Override
		public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive)
		{
			if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE)
			{
				// Fade out the view as it is swiped out of the parent's bounds
				final float alpha = ALPHA_FULL - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
				viewHolder.itemView.setAlpha(alpha);
				viewHolder.itemView.setTranslationX(dX);
			}
			else
			{
				super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
			}
		}

		@Override
		public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState)
		{
			// We only want the active item to change
			if (actionState != ItemTouchHelper.ACTION_STATE_IDLE)
			{
				if (viewHolder instanceof ItemTouchHelperViewHolder)
				{
					// Let the view holder know that this item is being moved or dragged
					ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
					itemViewHolder.onItemSelected();
				}
			}

			super.onSelectedChanged(viewHolder, actionState);
		}

		@Override
		public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder)
		{
			super.clearView(recyclerView, viewHolder);

			viewHolder.itemView.setAlpha(ALPHA_FULL);

			if (viewHolder instanceof ItemTouchHelperViewHolder)
			{
				// Tell the view holder it's time to restore the idle state
				ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
				itemViewHolder.onItemClear();
			}
		}
	}
}
