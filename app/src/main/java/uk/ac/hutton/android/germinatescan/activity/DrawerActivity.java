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

import android.content.*;
import android.content.res.*;
import android.net.*;
import android.os.*;
import android.support.design.widget.*;
import android.support.v4.view.*;
import android.support.v4.widget.*;
import android.support.v7.app.*;
import android.view.*;

import uk.ac.hutton.android.germinatescan.*;

/**
 * {@link DrawerActivity} extends {@link GerminateScanActivity}
 * and adds a {@link android.support.v4.widget.DrawerLayout} to the {@link android.app.Activity}.
 * <p/>
 * This drawer is used as the main menu of Germinate Scan. All subclasses will have this drawer. Make sure to include the correct layout in the layout
 * .xml file. Refer to activity_main.xml to see an example.
 *
 * @author Sebastian Raubach
 */
public abstract class DrawerActivity extends ThemedActivity
{
	protected static final int REQUEST_PREFS       = 1 << 1;
	protected static final int REQUEST_DATA_SOURCE = 1 << 2;

	private ActionBarDrawerToggle drawerToggle;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (getSupportActionBar() != null)
		{
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setHomeButtonEnabled(true);
		}

		DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		NavigationView navigationView = (NavigationView) findViewById(R.id.drawer_menu);

		drawerLayout.setDrawerShadow(R.drawable.shadow_drawer, GravityCompat.START);

		navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
		{
			@Override
			public boolean onNavigationItemSelected(MenuItem menuItem)
			{
				return onNavigation(menuItem);
			}
		});

		        /* ActionBarDrawerToggle ties together the the proper interactions
		 * between the sliding drawer and the action bar app icon */
		drawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
				drawerLayout, /* DrawerLayout object */
				R.string.drawer_open, /* "open drawer" description for accessibility */
				R.string.drawer_close /* "close drawer" description for accessibility */
		);
		drawerLayout.addDrawerListener(drawerToggle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during onPostCreate() and onConfigurationChanged()...
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		/* Sync the toggle state after onRestoreInstanceState has occurred. */
		drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		/* Pass any configuration change to the drawer toggles */
		drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (drawerToggle.onOptionsItemSelected(item))
		{
			return true;
		}
		else
		{
			return super.onOptionsItemSelected(item);
		}
	}

	private boolean onNavigation(MenuItem item)
	{
		/* Depending on the Android version, handle things differently */
		switch (item.getItemId())
		{
			case R.id.drawer_menu_visit_homepage:
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_url))));
				break;
			case R.id.drawer_menu_online_help:
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_url_online_help))));
				break;
			case R.id.drawer_menu_datasets:
				startActivityForResult(new Intent(getApplicationContext(), DatasetActivity.class), REQUEST_DATA_SOURCE);
				break;
			case R.id.drawer_menu_settings:
				startActivityForResult(new Intent(getApplicationContext(), PreferencesActivity.class), REQUEST_PREFS);
				break;
			case R.id.drawer_menu_map:
				startActivity(new Intent(getApplicationContext(), MapActivity.class));
				break;
			case R.id.drawer_menu_about:
				startActivity(new Intent(getApplicationContext(), AboutActivity.class));
				break;
			default:
		}
		return super.onOptionsItemSelected(item);
	}
}
