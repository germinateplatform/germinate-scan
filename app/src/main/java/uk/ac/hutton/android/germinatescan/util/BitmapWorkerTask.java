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

import android.graphics.*;
import android.os.*;
import android.support.v4.content.*;
import android.support.v4.util.*;
import android.support.v7.graphics.*;
import android.widget.*;

import java.io.*;
import java.lang.ref.*;

import uk.ac.hutton.android.germinatescan.*;
import uk.ac.hutton.android.germinatescan.database.*;

public class BitmapWorkerTask extends AsyncTask<Image, Void, Bitmap>
{
	private static final LongSparseArray<Integer> vibrantColorMap = new LongSparseArray<>();
	private static final LongSparseArray<Integer> mutedColorMap   = new LongSparseArray<>();

	private final WeakReference<ImageView> imageViewReference;
	private final WeakReference<TextView>  textViewReference;

	private Image image;

	public BitmapWorkerTask(ImageView imageView, TextView textView)
	{
		/* Use a WeakReference to ensure the ImageView can be garbage collected */
		imageViewReference = new WeakReference<>(imageView);
		textViewReference = new WeakReference<>(textView);
	}

	public static void resetCache()
	{
		vibrantColorMap.clear();
		mutedColorMap.clear();
	}

	@Override
	protected Bitmap doInBackground(Image... params)
	{
		this.image = params[0];
		File file = new File(params[0].getPath());

		if (!file.exists())
		{
			return null;
		}

        /* Decode image in background */
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 2;
		return BitmapFactory.decodeFile(params[0].getPath(), options);
	}

	@Override
	protected void onPostExecute(Bitmap bitmap)
	{
		if (isCancelled())
		{
			bitmap = null;
		}

        /* Once complete, see if ImageView is still around and set bitmap */
		if (bitmap != null)
		{
			final ImageView imageView = imageViewReference.get();
			if (imageView != null)
			{
				imageView.setImageBitmap(bitmap);

				if (textViewReference.get() != null)
				{
					/* Check if we have the colors in the cache */
					Integer background = vibrantColorMap.get(image.getId());
					Integer foreground = mutedColorMap.get(image.getId());

                    /* If so, set them */
					if (background != null && foreground != null)
					{
						TextView textView = textViewReference.get();

						if (textView != null)
						{
							textView.setBackgroundColor(background);
							textView.setTextColor(foreground);
						}
					}
					/* Otherwise, create a new palette (async) and then set the colors */
					else
					{
						Palette.from(bitmap)
							   .clearFilters()
							   .generate(new Palette.PaletteAsyncListener()
							   {
								   @Override
								   public void onGenerated(Palette palette)
								   {
									   TextView textView = textViewReference.get();

									   if (textView != null)
									   {
										   int background = ContextCompat.getColor(textView.getContext(), R.color.image_detail_text_background);

										   int color;
										   int r = Color.red(background);
										   int g = Color.green(background);
										   int b = Color.blue(background);

										   if ((r + g + b) / 3 > 128)
										   {
											   color = Color.BLACK;
										   }
										   else
										   {
											   color = Color.WHITE;
										   }

										   textView.setBackgroundColor(background);
										   textView.setTextColor(palette.getLightMutedColor(color));

										   vibrantColorMap.put(image.getId(), background);
										   mutedColorMap.put(image.getId(), palette.getLightMutedColor(color));
									   }
								   }
							   });
					}
				}
			}
		}
	}
}