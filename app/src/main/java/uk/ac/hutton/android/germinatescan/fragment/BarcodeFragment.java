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

import android.os.Bundle;
import android.view.*;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import butterknife.*;
import uk.ac.hutton.android.germinatescan.R;
import uk.ac.hutton.android.germinatescan.activity.*;
import uk.ac.hutton.android.germinatescan.adapter.BarcodeExampleAdapter;
import uk.ac.hutton.android.germinatescan.util.GridSpacingItemDecoration;

/**
 * The {@link BarcodeFragment} shows information about all the libraries that are used by this app and their licenses.
 *
 * @author Sebastian Raubach
 */
public class BarcodeFragment extends Fragment
{
	@BindView(R.id.intro_barcodes_text)
	TextView text;

	@BindView(R.id.intro_barcodes_recyclerview)
	RecyclerView recyclerView;

	private Unbinder unbinder;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_barcodes, container, false);

		unbinder = ButterKnife.bind(this, view);

		recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
		recyclerView.setAdapter(new BarcodeExampleAdapter(getActivity(), this));
		int valueInPixels = (int) getResources().getDimension(R.dimen.activity_vertical_margin) / 2;
		recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, valueInPixels, valueInPixels, valueInPixels));

		if (getActivity() instanceof IntroductionActivity)
			text.setTextColor(ContextCompat.getColor(getActivity(), android.R.color.white));

		return view;
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();

		unbinder.unbind();
	}

	public void onBarcodeSelected(int value)
	{
		if (getActivity() instanceof BarcodeSelectionActivity)
			((BarcodeSelectionActivity) getActivity()).onBarcodeSelected(value);
		else if (getActivity() instanceof IntroductionActivity)
			((IntroductionActivity) getActivity()).onBarcodeSelected(value);
	}
}
