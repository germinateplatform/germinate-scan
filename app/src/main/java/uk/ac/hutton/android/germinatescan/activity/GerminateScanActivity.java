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

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.*;

import com.google.android.gms.common.*;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import uk.ac.hutton.android.germinatescan.R;
import uk.ac.hutton.android.germinatescan.*;
import uk.ac.hutton.android.germinatescan.util.*;
import uk.ac.hutton.android.germinatescan.util.LocationUtils.LocationChangeListener;

/**
 * {@link uk.ac.hutton.android.germinatescan.activity.GerminateScanActivity} is the main {@link android.app.Activity} type of Germinate Scan. All
 * activities should subclass this class or a child of this class.
 *
 * @author Sebastian Raubach
 */
public abstract class GerminateScanActivity extends AppCompatActivity
{
	private static final int REQUEST_CODE_LOCATION_PERMISSIONS = 1;
	private static final int REQUEST_GOOGLE_PLAY_SERVICES      = 1001;

	protected static Set<String> deniedPermissions = Collections.newSetFromMap(new ConcurrentHashMap<>());

	private FirebaseAnalytics mFirebaseAnalytics;
	private Snackbar          snackbar;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		/* Don't forget to call the parent class */
		super.onCreate(savedInstanceState);

		/* Get the layout id from sub-class */
		Integer layoutId = getLayoutId();
		Integer toolbarId = getToolbarId();

		if (layoutId != null)
		{
			/* Set the content */
			setContentView(layoutId);
		}
		if (toolbarId != null)
		{
			androidx.appcompat.widget.Toolbar toolbar = findViewById(toolbarId);
			setSupportActionBar(toolbar);
		}
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		/* Handle uncaught exceptions */
		if (!BuildConfig.DEBUG)
		{
			// Obtain the FirebaseAnalytics instance.
			mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
		}
	}

	protected FirebaseAnalytics getTracker()
	{
		return mFirebaseAnalytics;
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

		if (code != ConnectionResult.SUCCESS)
			GoogleApiAvailability.getInstance().getErrorDialog(this, code, REQUEST_GOOGLE_PLAY_SERVICES, dialog -> GerminateScanActivity.this.finish());

		startLocationTracking();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		switch (requestCode)
		{
			case REQUEST_CODE_LOCATION_PERMISSIONS:

				if (snackbar != null)
				{
					snackbar.dismiss();
					snackbar = null;
				}

				for (String permission : permissions)
				{
					if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
						deniedPermissions.add(permission);
				}

				if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
				{
					/* Permission Granted */
					LocationUtils.load(this);
				}
				break;
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	protected boolean isEulaAccepted()
	{
		return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PreferenceUtils.PREFS_EULA_ACCEPTED, false);
	}

	protected void setEulaAccepted()
	{
		startLocationTracking();
	}

	private void startLocationTracking()
	{
		if (this instanceof LocationChangeListener && isEulaAccepted())
		{
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
			{
				/* Request the permission */
				if (!deniedPermissions.contains(Manifest.permission.ACCESS_FINE_LOCATION) || !deniedPermissions.contains(Manifest.permission.ACCESS_COARSE_LOCATION))
				{
					snackbar = Snackbar.make(getSnackbarParentView(), R.string.toast_permission_explain_gps, Snackbar.LENGTH_INDEFINITE);
					customizeSnackbar(snackbar);
					snackbar.show();
					ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSIONS);
				}

				return;
			}

			/* Make sure the location information is available */
			LocationUtils.load(this);
		}
	}

	protected void customizeSnackbar(Snackbar snackbar)
	{
		View view = snackbar.getView();
		TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
		tv.setTextColor(Color.WHITE);
		view.setBackgroundColor(ContextCompat.getColor(this, R.color.snackbar_red));
	}

	/**
	 * The {@link uk.ac.hutton.android.germinatescan.activity.GerminateScanActivity} will call {@link #setContentView(int)} with the result of this
	 * function.
	 * <p/>
	 * Child classes MUST NOT call {@link #setContentView(int)} themselves, but rather let {@link uk.ac.hutton.android.germinatescan.activity.GerminateScanActivity}
	 * take care of it.
	 *
	 * @return The layout id of the child class. <code>null</code> can be returned to indicate that the child doesn't use a layout.
	 */
	protected abstract Integer getLayoutId();

	/**
	 * The {@link uk.ac.hutton.android.germinatescan.activity.GerminateScanActivity} will call {@link #setSupportActionBar(androidx.appcompat.widget.Toolbar)}
	 * with the {@link Toolbar} associated with the returned id.
	 * <p/>
	 * If <code>null</code> is returned, not support action bar will be set up.
	 *
	 * @return The id of the {@link Toolbar} to be used as the support action bar
	 */
	protected abstract Integer getToolbarId();

	protected View getSnackbarParentView()
	{
		return findViewById(android.R.id.content);
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		if (this instanceof LocationChangeListener)
		{
			/* Release resources */
			LocationUtils.unload(this);
		}
	}

	public enum TrackerName
	{
		/* Tracker used only in this app */
		APP_TRACKER,
		/* Tracker used by all the apps from a company. eg: roll-up tracking */
		GLOBAL_TRACKER,
		/* Tracker used by all ecommerce transactions from a company */
		ECOMMERCE_TRACKER
	}
}
