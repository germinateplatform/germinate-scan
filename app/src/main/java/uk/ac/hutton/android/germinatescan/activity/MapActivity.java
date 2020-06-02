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

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.GoogleMap.*;
import com.google.android.gms.maps.model.*;

import java.util.*;

import androidx.core.content.ContextCompat;
import uk.ac.hutton.android.germinatescan.R;
import uk.ac.hutton.android.germinatescan.database.Barcode;
import uk.ac.hutton.android.germinatescan.database.manager.BarcodeManager;
import uk.ac.hutton.android.germinatescan.util.LocationUtils.LocationChangeListener;
import uk.ac.hutton.android.germinatescan.util.*;

/**
 * {@link MapActivity} shows the current location of the user as well as the location of scanned barcodes and taken images.
 *
 * @author Sebastian Raubach
 */
public class MapActivity extends ThemedActivity implements InfoWindowAdapter, LocationChangeListener, OnMapReadyCallback
{
	public static final int DEFAULT_MAP_TYPE = GoogleMap.MAP_TYPE_NORMAL;

	private static final int MARKER_PADDING = 25;

	private Map<Marker, Barcode> mapping = new HashMap<>();
	private GoogleMap            map;
	private Marker               currentLocation;

	private PreferenceUtils prefs;

	private BarcodeManager barcodeManager;

	private boolean followMe = true;

	@Override
	protected Integer getLayoutId()
	{
		return R.layout.activity_map;
	}

	@Override
	protected Integer getToolbarId()
	{
		return R.id.toolbar;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		prefs = new PreferenceUtils(this);

		long id = prefs.getLong(PreferenceUtils.PREFS_SELECTED_DATASET_ID, -1);

		if (id != -1)
			barcodeManager = new BarcodeManager(this, id);

		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}

	@Override
	public void onMapReady(GoogleMap googleMap)
	{
		map = googleMap;

		/* Set the map type based on the shared preference */
		updateMapType();

		/* Get the marker image */
		Drawable drawable = ContextCompat.getDrawable(this, R.drawable.marker_barcode);
		int padding = 0;

		if (drawable != null)
			padding = Math.max(drawable.getIntrinsicHeight(), drawable.getIntrinsicWidth());

		BitmapDescriptor img = BitmapDescriptorFactory.fromResource(R.drawable.marker_barcode);

		map.setPadding(0, padding, 0, 0);

		/* Set up a builder to keep track of the bounds */
		final LatLngBounds.Builder builder = new LatLngBounds.Builder();

		/* Get all the barcodes */
		if (barcodeManager != null)
		{
			List<Barcode> locations = barcodeManager.getAll();

			for (Barcode location : locations)
			{
				if (!location.hasValidPosition())
				{
					/* Skip invalid locations */
					continue;
				}

				LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
				MarkerOptions opts = new MarkerOptions()
						.position(position)
						.title(location.getBarcode())
						.icon(img);

				Marker marker = map.addMarker(opts);

				mapping.put(marker, location);

				builder.include(position);
			}
		}

		if (mapping.size() < 1)
		{
			ToastUtils.createToast(this, R.string.toast_no_barcodes_with_location, ToastUtils.LENGTH_SHORT);
		}
		else
		{
			final int p = padding;
			/* Zoom in to the bounds of the markers */
			map.setOnMapLoadedCallback(new OnMapLoadedCallback()
			{
				@Override
				public void onMapLoaded()
				{
					LatLngBounds bounds = builder.build();
					CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, p + MARKER_PADDING);

					/* Zoom in, animating the camera */
					map.animateCamera(cu);
				}
			});
		}

		map.setInfoWindowAdapter(this);

		/* Set up the current location marker */
		BitmapDescriptor userIcon = BitmapDescriptorFactory.fromResource(R.drawable.marker_user);
		MarkerOptions options = new MarkerOptions()
				.title(getString(R.string.map_current_location))
				.visible(false)
				.icon(userIcon)
				.position(new LatLng(0, 0));

		currentLocation = map.addMarker(options);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.map_menu, menu);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		int mapType = prefs.getInt(PreferenceUtils.PREFS_MAP_TYPE, DEFAULT_MAP_TYPE);
		followMe = prefs.getBoolean(PreferenceUtils.PREFS_MAP_FOLLOW_ME, PreferenceUtils.DEFAULT_PREF_MAP_FOLLOW);

		if (currentLocation != null)
			currentLocation.setVisible(followMe);

		switch (mapType)
		{
			case GoogleMap.MAP_TYPE_SATELLITE:
				menu.findItem(R.id.menu_map_type_satellite).setChecked(true);
				break;

			default:
				menu.findItem(R.id.menu_map_type_map).setChecked(true);
				break;
		}

		Drawable resource;
		String text;

		/* Determine the drawable and title */
		if (followMe)
		{
			resource = ContextCompat.getDrawable(this, R.drawable.menu_map_follow);
			text = getString(R.string.menu_map_follow_me);
		}
		else
		{
			resource = ContextCompat.getDrawable(this, R.drawable.menu_map_not_follow);
			text = getString(R.string.menu_map_not_follow_me);
		}

		/* Set it */
		MenuItem item = menu.findItem(R.id.map_follow_me);
		item.setIcon(resource);
		item.setTitle(text);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				super.onOptionsItemSelected(item);
				break;

			case R.id.menu_map_type_map:
				prefs.putInt(PreferenceUtils.PREFS_MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL);
				updateMapType();
				invalidateOptionsMenu();
				break;

			case R.id.menu_map_type_satellite:
				prefs.putInt(PreferenceUtils.PREFS_MAP_TYPE, GoogleMap.MAP_TYPE_SATELLITE);
				updateMapType();
				invalidateOptionsMenu();
				break;

			case R.id.map_follow_me:
				followMe = prefs.getBoolean(PreferenceUtils.PREFS_MAP_FOLLOW_ME, PreferenceUtils.DEFAULT_PREF_MAP_FOLLOW);
				prefs.putBoolean(PreferenceUtils.PREFS_MAP_FOLLOW_ME, !followMe);

				invalidateOptionsMenu();
				break;
		}

		return true;
	}

	protected void updateMapType()
	{
		int mapType = prefs.getInt(PreferenceUtils.PREFS_MAP_TYPE, DEFAULT_MAP_TYPE);

		map.setMapType(mapType);
	}

	@Override
	public View getInfoWindow(Marker marker)
	{
		return null;
	}

	@Override
	public View getInfoContents(Marker marker)
	{
		if (marker.equals(currentLocation))
		{
			TextView view = new TextView(this);
			view.setText(R.string.map_current_location);
			return view;
		}
		else
		{
			/* Layout the popup info window of the markers */
			View view = getLayoutInflater().inflate(R.layout.helper_marker, null);

			ImageView image = (ImageView) view.findViewById(R.id.marker_image);
			TextView latitude = (TextView) view.findViewById(R.id.marker_latitude);
			TextView longitude = (TextView) view.findViewById(R.id.marker_longitude);
			TextView altitude = (TextView) view.findViewById(R.id.marker_altitude);
			TextView description = (TextView) view.findViewById(R.id.marker_description);
			TextView time = (TextView) view.findViewById(R.id.marker_time);

			/* Fill it */
			Barcode location = mapping.get(marker);

			image.setImageResource(R.drawable.marker_barcode);
			latitude.setText(getString(R.string.location_latitude, location.getLatitude()));
			longitude.setText(getString(R.string.location_longitude, location.getLongitude()));
			altitude.setText(getString(R.string.location_altitude, location.getAltitude()));
			description.setText(getString(R.string.location_description, location.getBarcode()));
			time.setText(getString(R.string.location_time, location.getFormattedTimestamp()));

			return view;
		}
	}

	@Override
	public void onLocationChanged(android.location.Location location)
	{
		if (followMe)
		{
			if (location != null && currentLocation != null)
			{
				LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
				currentLocation.setPosition(latLng);
				currentLocation.setVisible(true);

				map.animateCamera(CameraUpdateFactory.newLatLng(latLng), 2000, null);
			}
		}
		else
		{
			currentLocation.setVisible(false);
		}
	}
}
