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

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import butterknife.*;
import uk.ac.hutton.android.germinatescan.R;
import uk.ac.hutton.android.germinatescan.adapter.LicenseAdapter;
import uk.ac.hutton.android.germinatescan.util.GridSpacingItemDecoration;

/**
 * The {@link AboutLicenseFragment} shows information about all the libraries that are used by this app and their licenses.
 *
 * @author Sebastian Raubach
 */
public class AboutLicenseFragment extends Fragment
{
	@BindView(R.id.license_recyclerview)
	RecyclerView recyclerView;
	private Unbinder unbinder;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_about_license, container, false);

		unbinder = ButterKnife.bind(this, view);

		int valueInPixels = (int) getResources().getDimension(R.dimen.activity_vertical_margin) / 2;

		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		recyclerView.setItemAnimator(null);

		int horizontalMargin = (int) getResources().getDimension(R.dimen.activity_horizontal_margin);
		int verticalMargin = (int) getResources().getDimension(R.dimen.activity_vertical_margin);

		recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, horizontalMargin, verticalMargin, valueInPixels));
		recyclerView.setAdapter(new LicenseAdapter(getActivity(), recyclerView));

		return view;
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();

		unbinder.unbind();
	}
}
