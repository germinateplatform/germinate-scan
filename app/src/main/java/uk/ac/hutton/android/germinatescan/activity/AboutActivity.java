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

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.ImageView;

import com.google.android.material.appbar.*;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.*;
import androidx.viewpager.widget.ViewPager;
import butterknife.*;
import uk.ac.hutton.android.germinatescan.R;
import uk.ac.hutton.android.germinatescan.fragment.*;

/**
 * The {@link AboutActivity} shows the about {@link Fragment}s: {@link AboutInformationFragment}, {@link AboutDeveloperFragment} and {@link
 * AboutLicenseFragment}.
 *
 * @author Sebastian Raubach
 */
public class AboutActivity extends ThemedActivity
{
	@BindView(R.id.about_viewpager)
	ViewPager               viewPager;
	@BindView(R.id.about_image)
	ImageView               aboutImage;
	@BindView(R.id.about_tabs)
	TabLayout               tabLayout;
	@BindView(R.id.about_collapsingtoolbarlayout)
	CollapsingToolbarLayout collapsingToolbarLayout;
	@BindView(R.id.about_appbarlayout)
	AppBarLayout            appBarLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		ButterKnife.bind(this);

		final Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		/* Set the toolbar as the action bar */
		if (getSupportActionBar() != null)
		{
			/* Set the title */
			getSupportActionBar().setTitle(" ");
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setHomeButtonEnabled(true);
		}

		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		getWindow().setStatusBarColor(Color.TRANSPARENT);

		/* Get the view pager and set the fragment adapter */
		viewPager.setAdapter(new AboutFragmentPagerAdapter(getSupportFragmentManager(), this));
		tabLayout.setupWithViewPager(viewPager);

		final float heightDp = getResources().getDisplayMetrics().heightPixels / 2f;
		final CollapsingToolbarLayout.LayoutParams lp = (CollapsingToolbarLayout.LayoutParams) aboutImage.getLayoutParams();

		aboutImage.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				aboutImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);

				if (aboutImage.getHeight() > heightDp)
					lp.height = (int) heightDp;
			}
		});

		/* Get the CollapsingToolbarLayout and listen for offset change events to show/hide the toolbar title, i.e. it'll only be shown when the toolbar is fully collapsed */
		appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener()
		{
			boolean show = false;
			int scrollRange = -1;

			@Override
			public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset)
			{
				if (scrollRange == -1)
				{
					scrollRange = appBarLayout.getTotalScrollRange();
				}
				if (scrollRange + verticalOffset == 0)
				{
					collapsingToolbarLayout.setTitle(getString(R.string.activity_about));
					show = true;
				}
				else if (show)
				{
					collapsingToolbarLayout.setTitle(" "); // careful there should a space between double quote otherwise it wont work
					show = false;
				}
			}
		});
	}

	@Override
	protected Integer getLayoutId()
	{
		return R.layout.activity_about;
	}

	@Override
	protected Integer getToolbarId()
	{
		return null;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				onBackPressed();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * The {@link AboutFragmentPagerAdapter} takes care of the actual {@link Fragment}s in the {@link ViewPager}.
	 */
	public class AboutFragmentPagerAdapter extends FragmentPagerAdapter
	{
		private String[] titles;

		AboutFragmentPagerAdapter(FragmentManager fm, Context context)
		{
			super(fm);

			this.titles = new String[]{context.getString(R.string.about_tab_information), context.getString(R.string.about_tab_team), context.getString(R.string.about_tab_license)};
		}

		@Override
		public CharSequence getPageTitle(int position)
		{
			return titles[position];
		}

		@Override
		public int getCount()
		{
			return 3;
		}

		@Override
		public Fragment getItem(int position)
		{
			switch (position)
			{
				case 0:
					return new AboutInformationFragment();
				case 1:
					return new AboutDeveloperFragment();
				case 2:
					return new AboutLicenseFragment();
			}

			return null;
		}
	}
}
