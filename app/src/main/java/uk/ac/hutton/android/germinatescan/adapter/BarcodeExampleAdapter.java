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

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.*;
import uk.ac.hutton.android.germinatescan.R;
import uk.ac.hutton.android.germinatescan.activity.*;
import uk.ac.hutton.android.germinatescan.fragment.BarcodeFragment;
import uk.ac.hutton.android.germinatescan.util.PreferenceUtils;

/**
 * The {@link BarcodeExampleAdapter} takes care of all the {@link BarcodeExample}s.
 *
 * @author Sebastian Raubach
 */
public class BarcodeExampleAdapter extends RecyclerView.Adapter<BarcodeExampleAdapter.ViewHolder>
{
	private Activity        context;
	private BarcodeFragment listener;
	private int             nrOfBarcodes;

	public BarcodeExampleAdapter(Activity context, BarcodeFragment listener)
	{
		this.context = context;
		this.listener = listener;
		this.nrOfBarcodes = PreferenceUtils.DEFAULT_PREF_NR_BARCODES;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		/* Create a new view from the layout file */
		final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.barcode_intro_view, parent, false);

		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position)
	{
		final BarcodeExample item = BarcodeExample.values()[position];

		holder.name.setText(item.getName(context));
		holder.description.setText(item.getDescription(context));
		holder.image.setImageDrawable(item.getImage(context));

		if (context instanceof BarcodeSelectionActivity)
		{
			TypedValue typedValue = new TypedValue();
			Resources.Theme theme = context.getTheme();
			theme.resolveAttribute(R.attr.textColor, typedValue, true);
			@ColorInt int color = typedValue.data;

			holder.name.setTextColor(color);
			holder.description.setTextColor(color);

			holder.activeButton.setVisibility(View.GONE);
			holder.selectButton.setVisibility(View.GONE);
		}
		else if (context instanceof IntroductionActivity && nrOfBarcodes == position + 1)
		{
			holder.activeButton.setVisibility(View.VISIBLE);
			holder.selectButton.setVisibility(View.GONE);
		}
		else
		{
			holder.activeButton.setVisibility(View.GONE);
			holder.selectButton.setVisibility(View.VISIBLE);
		}

		if (context instanceof BarcodeSelectionActivity)
		{
			holder.view.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					if (listener != null)
						listener.onBarcodeSelected(holder.getAdapterPosition() + 1);

					nrOfBarcodes = holder.getAdapterPosition() + 1;
					notifyDataSetChanged();
				}
			});
		}
		else
		{
			holder.selectButton.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (listener != null)
						listener.onBarcodeSelected(holder.getAdapterPosition() + 1);

					nrOfBarcodes = holder.getAdapterPosition() + 1;
					notifyDataSetChanged();
				}
			});
		}
	}

	@Override
	public int getItemCount()
	{
		return BarcodeExample.values().length;
	}

	/**
	 * {@link BarcodeExample} is an enum holding all the libraries we use in this app with their license information
	 *
	 * @author Sebastian Raubach
	 */
	public static enum BarcodeExample
	{
		ONE(R.string.introduction_barcodes_example_1_title, R.string.introduction_barcodes_example_1_text, R.drawable.barcode_example_one),
		TWO(R.string.introduction_barcodes_example_2_title, R.string.introduction_barcodes_example_2_text, R.drawable.barcode_example_two),
		THREE(R.string.introduction_barcodes_example_3_title, R.string.introduction_barcodes_example_3_text, R.drawable.barcode_example_three),
		FOUR(R.string.introduction_barcodes_example_4_title, R.string.introduction_barcodes_example_4_text, R.drawable.barcode_example_four),
		FIVE(R.string.introduction_barcodes_example_5_title, R.string.introduction_barcodes_example_5_text, R.drawable.barcode_example_five);

		private int      name;
		private int      description;
		private int      image;
		private String   resolvedName;
		private String   resolvedDescription;
		private Drawable resolvedImage;

		BarcodeExample(@StringRes int name, @StringRes int description, @DrawableRes int image)
		{
			this.name = name;
			this.description = description;
			this.image = image;
		}

		public static BarcodeExample getForNumber(int value)
		{
			return values()[value - 1];
		}

		public String getName(Context context)
		{
			if (resolvedName == null)
				resolvedName = context.getString(name);

			return resolvedName;
		}

		private String getDescription(Context context)
		{
			if (resolvedDescription == null)
				resolvedDescription = context.getString(description);

			return resolvedDescription;
		}

		private Drawable getImage(Context context)
		{
			if (resolvedImage == null)
				resolvedImage = ContextCompat.getDrawable(context, image);

			return resolvedImage;
		}
	}

	static class ViewHolder extends RecyclerView.ViewHolder
	{
		View view;
		@BindView(R.id.barcode_name_view)
		TextView  name;
		@BindView(R.id.barcode_description_view)
		TextView  description;
		@BindView(R.id.barcode_image_view)
		ImageView image;
		@BindView(R.id.barcode_select_button)
		Button    selectButton;
		@BindView(R.id.barcode_active_button)
		Button    activeButton;

		ViewHolder(View v)
		{
			super(v);

			view = v;
			ButterKnife.bind(this, v);
		}
	}
}