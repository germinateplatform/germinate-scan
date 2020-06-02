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
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import java.io.File;
import java.util.List;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.*;
import androidx.viewpager.widget.ViewPager;
import uk.ac.hutton.android.germinatescan.*;
import uk.ac.hutton.android.germinatescan.database.*;
import uk.ac.hutton.android.germinatescan.database.manager.ImageManager;
import uk.ac.hutton.android.germinatescan.fragment.ImageDetailFragment;
import uk.ac.hutton.android.germinatescan.util.*;

/**
 * The {@link uk.ac.hutton.android.germinatescan.activity.ImageDetailActivity} shows the images associated with a barcode row
 */
public class ImageDetailActivity extends ThemedActivity
{
	public static final String EXTRA_ROW        = "extra_barcode";
	public static final String EXTRA_DATASET_ID = "extra_dataset_id";

	private ImagePagerAdapter adapter;
	private ViewPager         pager;

	private TextView    goAwayMessage;
	private ProgressBar progressIndicator;

	private Barcode barcode;

	private boolean imagesEdited = false;

	private ImageManager imageManager;

	/**
	 * Starts a new {@link uk.ac.hutton.android.germinatescan.util.BitmapWorkerTask} to load the image
	 *
	 * @param image     The Image to load
	 * @param imageView The ImageView
	 */
	public void loadBitmap(final Image image, ImageView imageView, TextView textView)
	{
		new BitmapWorkerTask(imageView, textView)
				.execute(image);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();

		if (extras != null)
		{
			barcode = (Barcode) extras.getSerializable(EXTRA_ROW);
			int datasetId = extras.getInt(EXTRA_DATASET_ID, -1);

			if (datasetId != -1)
				imageManager = new ImageManager(this, datasetId);
		}

		goAwayMessage = findViewById(R.id.emptyAdapterMessage);
		progressIndicator = findViewById(R.id.adapterBackgroundProgress);
		adapter = new ImagePagerAdapter(getSupportFragmentManager(), barcode);
		pager = findViewById(R.id.pager);
		pager.setAdapter(adapter);

		adapter.registerDataSetObserver(new DataSetObserver()
		{
			@Override
			public void onChanged()
			{
				super.onChanged();
				pager.setAdapter(adapter);

				progressIndicator.setVisibility(adapter.getCount() > 0 ? View.VISIBLE : View.GONE);
				goAwayMessage.setVisibility(adapter.getCount() < 1 ? View.VISIBLE : View.GONE);
			}
		});
	}

	@Override
	public void finish()
	{
		int result = imagesEdited ? Activity.RESULT_OK : Activity.RESULT_CANCELED;

		if (getParent() == null)
		{
			setResult(result, null);
		}
		else
		{
			getParent().setResult(result, null);
		}

		super.finish();
	}

	@Override
	protected Integer getLayoutId()
	{
		return R.layout.image_detail_pager;
	}

	@Override
	protected Integer getToolbarId()
	{
		return R.id.toolbar;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.images_menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (adapter.getCount() < 1)
			return true;

		final Image image = adapter.getImage(pager.getCurrentItem());

		final String path = image.getPath();

		switch (item.getItemId())
		{
			case android.R.id.home:
				super.onOptionsItemSelected(item);
				break;

			case R.id.menu_open_image:
				/* Tell Android that you want to view the image */
				Intent intent = new Intent(Intent.ACTION_VIEW);
				Uri uri = FileProvider.getUriForFile(ImageDetailActivity.this, BuildConfig.APPLICATION_ID + ".provider", new File(path));
				intent.setDataAndType(uri, "image/*");
				intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

				View view = pager.getChildAt(pager.getCurrentItem());
				Bundle bundle = ActivityOptions.makeScaleUpAnimation(view, (int) view.getX(), (int) view.getY(), view.getWidth(), view.getHeight()).toBundle();
				startActivity(intent, bundle);
				break;

			case R.id.menu_delete_image:
				/* Ask the user for confirmation */
				new AlertDialog.Builder(this)
						.setPositiveButton(getString(R.string.general_yes), new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialogInterface, int i)
							{
								/* Delete the image file */
								File file = new File(path);
								if (file.exists() && !file.isDirectory() && file.getName().endsWith("." + FileUtils.FileExtension.jpg.name()))
								{
									file.delete();
								}

								/* Delete the item from the database */
								imageManager.delete(image);

								/* Delete the item from the adapter */
								adapter.removeItem(pager.getCurrentItem());

								/* Remember that we changed stuff */
								imagesEdited = true;
							}
						})
						.setNegativeButton(getString(R.string.general_no), null)
						.setTitle(R.string.dialog_title_delete_image)
						.setMessage(getString(R.string.dialog_message_delete_image))
						.show();
				break;
		}

		return true;
	}

	public static class ImagePagerAdapter extends FragmentStatePagerAdapter
	{
		private List<Image> images;

		public ImagePagerAdapter(androidx.fragment.app.FragmentManager fm, Barcode barcode)
		{
			super(fm);

			images = barcode.getImages();
		}

		public Image getImage(int position)
		{
			return images.get(position);
		}

		public void removeItem(int index)
		{
			images.remove(index);
			notifyDataSetChanged();
		}

		@Override
		public int getCount()
		{
			return images.size();
		}

		@Override
		public Fragment getItem(int position)
		{
			return ImageDetailFragment.newInstance(images.get(position));
		}
	}
}