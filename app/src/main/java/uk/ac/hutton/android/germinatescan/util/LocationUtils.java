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

import android.content.*;
import android.location.*;
import android.os.*;

import java.util.*;

import uk.ac.hutton.android.germinatescan.*;
import uk.ac.hutton.android.germinatescan.activity.*;

/**
 * {@link uk.ac.hutton.android.germinatescan.util.LocationUtils} provides methods to track user locations
 *
 * @author Sebastian Raubach
 */
public class LocationUtils
{
	/**
	 * {@link java.util.Set} of the registered {@link uk.ac.hutton.android.germinatescan.util.LocationUtils.LocationChangeListener}s
	 */
	private static final Set<LocationChangeListener> listeners = new HashSet<>();

	private static LocationManager  locationManager;
	private static LocationListener locationListener;

	/**
	 * The last known {@link android.location.Location}
	 */
	private static Location lastKnownLocation;

	private static int counter = 0;

	/**
	 * Initializes the location tracking.
	 * <p/>
	 * <b>IMPORTANT:</b> {@link #unload(Context)} HAS to be called before the calling {@link android.app.Activity} is stopped. {@link
	 * android.app.Activity#onPause()} is a good point to do that. Re-load this in {@link android.app.Activity#onResume()}.
	 *
	 * @param context The calling {@link android.content.Context}
	 * @return <code>true</code> if the location tracking has been enabled, <code>false</code> if either tracking is disabled or if it's already
	 * running
	 */
	public static synchronized boolean load(GerminateScanActivity context) throws SecurityException
	{
		boolean result = false;

		if (locationManager == null)
		{
			/* Acquire a reference to the system Location Manager */
			locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            /* Define a listener that responds to location updates */
			locationListener = new LocationListener()
			{
				@Override
				public void onLocationChanged(Location location)
				{
					lastKnownLocation = location;
					for (LocationChangeListener listener : listeners)
					{
						if (listener != null)
						{
							listener.onLocationChanged(location);
						}
					}
				}

				@Override
				public void onStatusChanged(String provider, int status, Bundle extras)
				{
				}

				@Override
				public void onProviderEnabled(String provider)
				{
				}

				@Override
				public void onProviderDisabled(String provider)
				{
				}
			};

            /* Define the criteria how to select the location provider -> use
			 * default */
			Criteria criteria = new Criteria();
			String provider = locationManager.getBestProvider(criteria, true);

			locationManager.requestLocationUpdates(provider, 1000, 5, locationListener);

			result = true;
		}
		else
		{
			/* Define the criteria how to select the location provider -> use
			 * default */
			Criteria criteria = new Criteria();
			String provider = locationManager.getBestProvider(criteria, true);

            /* Register the listener with the Location Manager to receive
			 * location updates */
			locationManager.requestLocationUpdates(provider, 1000, 5, locationListener);
		}

		counter++;

		if (context instanceof LocationChangeListener)
		{
			register((LocationChangeListener) context);
		}

		return result;
	}

	/**
	 * Called to remove status updates.
	 */
	public static synchronized void unload(Context context)
	{
		/* If the caller is the last one, release the resource */
		if (locationManager != null && counter == 1)
		{
			reset();
		}

		if (context instanceof LocationChangeListener)
		{
			unregister((LocationChangeListener) context);
		}
	}

	/**
	 * Resets the {@link LocationUtils} by resetting all stored information. This includes {@link #lastKnownLocation}, {@link #listeners}, {@link
	 * #locationManager} and {@link #locationListener}
	 */
	public static synchronized void reset() throws SecurityException
	{
		listeners.clear();
		counter = 0;
		lastKnownLocation = null;
		if (locationManager != null)
		{
			locationManager.removeUpdates(locationListener);
			locationManager = null;
		}

		locationListener = null;
	}

	/**
	 * Registers a new {@link uk.ac.hutton.android.germinatescan.util.LocationUtils.LocationChangeListener} for updates.
	 * <p/>
	 * If there is location information at the point of registration, the {@link uk.ac.hutton.android.germinatescan.util.LocationUtils.LocationChangeListener}
	 * will be fired immediately.
	 *
	 * @param listener The new {@link uk.ac.hutton.android.germinatescan.util.LocationUtils.LocationChangeListener}
	 */
	public static void register(LocationChangeListener listener)
	{
		listeners.add(listener);

		if (lastKnownLocation != null)
		{
			listener.onLocationChanged(lastKnownLocation);
		}
	}

	/**
	 * Unregisters the given {@link uk.ac.hutton.android.germinatescan.util.LocationUtils.LocationChangeListener} from the update set
	 *
	 * @param listener The {@link uk.ac.hutton.android.germinatescan.util.LocationUtils.LocationChangeListener} to unregister
	 */
	public static void unregister(LocationChangeListener listener)
	{
		listeners.remove(listener);
	}

	public static boolean isEmpty(String input)
	{
		return StringUtils.isEmpty(input) || input.endsWith(BarcodeReader.INSTANCE.getString(R.string.location_unknown));
	}

	/**
	 * {@link uk.ac.hutton.android.germinatescan.util.LocationUtils.LocationChangeListener} is an interface used to get notified if the location
	 * changes.
	 *
	 * @author Sebastian Raubach
	 */
	public interface LocationChangeListener
	{
		void onLocationChanged(Location location);
	}
}
