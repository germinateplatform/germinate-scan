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

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * {@link GridSpacingItemDecoration} handles item spacing for {@link RecyclerView} items.
 *
 * @author Sebastian Raubach
 */
public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration
{
	private int left;
	private int right;
	private int top;
	private int bottom;
	private int spanCount;
	private int horizontalSpacing;
	private int verticalSpacing;

	/**
	 * Creates a new instance with the given column count, horizontal and vertical margin and spacing between items.
	 *
	 * @param spanCount        The number of columns
	 * @param horizontalMargin The horizontal margin for edge items (left, right)
	 * @param verticalMargin   The vertical margin for edge items (top, bottom)
	 * @param spacing          The spacing between items
	 */
	public GridSpacingItemDecoration(int spanCount, int horizontalMargin, int verticalMargin, int spacing)
	{
		this.left = horizontalMargin;
		this.right = horizontalMargin;
		this.top = verticalMargin;
		this.bottom = verticalMargin;

		this.spanCount = spanCount;
		this.horizontalSpacing = spacing;
		this.verticalSpacing = spacing;
	}

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
	{
		int position = parent.getChildAdapterPosition(view); // item position

		if (position < 0)
			return;

		int column = position % spanCount; // item column
		int row = position / spanCount; // item row
		int count = parent.getAdapter().getItemCount(); // total count
		int maxRow = (count - 1) / spanCount; // index of last row

		int x = left + right + (spanCount - 1) * horizontalSpacing;
		int y = top + bottom + (maxRow) * verticalSpacing;

		int xPad = x / spanCount / 2;

		/* If this is the first AND last column, add the left and right margin */
		if (column == 0 && column == spanCount - 1)
		{
			outRect.left = left;
			outRect.right = right;
		}
		/* If this is the first column, add the left margin, plus a fraction of the right padding */
		else if (column == 0)
		{
			outRect.left = left;
			outRect.right = xPad / 2;// Math.max(0, xPad - left);
		}
		/* If this is the last column, add the right margin, plus a fraction of the left padding */
		else if (column == spanCount - 1)
		{
			outRect.right = right;
			outRect.left = xPad / 2;//Math.max(0, xPad - right);
		}
		/* Else, it's an item in the middle, add fraction of left and right padding*/
		else
		{
			outRect.left = xPad / 2;
			outRect.right = xPad / 2;
		}
		/* If this is the first row, add the top margin */
		if (position < spanCount)
		{
			outRect.top = top;
		}
		/* Else, add top spacing */
		else
		{
			outRect.top = verticalSpacing;
		}
		/* If this is the last row, add bottom margin */
		if (row == maxRow)
		{
			outRect.bottom = bottom;
		}
	}
}