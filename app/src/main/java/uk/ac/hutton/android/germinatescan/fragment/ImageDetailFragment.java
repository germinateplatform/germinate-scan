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

package uk.ac.hutton.android.germinatescan.fragment;

import android.os.*;
import android.support.v4.app.*;
import android.view.*;
import android.widget.*;

import java.util.*;

import uk.ac.hutton.android.germinatescan.*;
import uk.ac.hutton.android.germinatescan.activity.*;
import uk.ac.hutton.android.germinatescan.database.*;

/**
 * @author Sebastian Raubach
 */
public class ImageDetailFragment extends Fragment
{
	private static final String IMAGE_DATA_EXTRA = "resId";
	private static Map<Image, ImageDetailFragment> CACHE = new HashMap<>();
	private Image     image;
	private ImageView mImageView;
	private TextView  textView;

	/* Empty constructor, required as per Fragment docs */
	public ImageDetailFragment()
	{
	}

	public static ImageDetailFragment newInstance(Image image)
	{
		ImageDetailFragment f = CACHE.get(image);

		if (f == null || !f.isDetached())
		{
			f = new ImageDetailFragment();
			Bundle args = new Bundle();
			args.putSerializable(IMAGE_DATA_EXTRA, image);
			f.setArguments(args);

			CACHE.put(image, f);
		}

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		image = getArguments() != null ? (Image) getArguments().getSerializable(IMAGE_DATA_EXTRA) : null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		// image_detail_fragment.xml contains just an ImageView
		final View v = inflater.inflate(R.layout.image_detail_fragment, container, false);
		mImageView = (ImageView) v.findViewById(R.id.imageView);
		textView = (TextView) v.findViewById(R.id.imageTextView);
		textView.setText(image.getPath());
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		if (ImageDetailActivity.class.isInstance(getActivity()))
		{
			// Call out to ImageDetailActivity to load the bitmap in a background thread
			((ImageDetailActivity) getActivity()).loadBitmap(image, mImageView, textView);
		}
	}
}
