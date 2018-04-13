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

import android.annotation.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.support.design.widget.*;
import android.support.v4.app.*;
import android.view.*;
import android.webkit.*;
import android.widget.*;

import java.io.*;

import butterknife.*;
import uk.ac.hutton.android.germinatescan.R;
import uk.ac.hutton.android.germinatescan.activity.*;
import uk.ac.hutton.android.germinatescan.util.*;

/**
 * The {@link EulaFragment} shows information about all the libraries that are used by this app and their licenses.
 *
 * @author Sebastian Raubach
 */
public class EulaFragment extends Fragment
{
	@BindView(R.id.eula_list)
	ListView          list;
	@BindView(R.id.eula_webview)
	WebView           webView;
	@BindView(R.id.eula_button_accept)
	Button            accept;
	@BindView(R.id.eula_button_cancel)
	Button            cancel;
	@BindView(R.id.eula_button_bar)
	CoordinatorLayout buttonBar;
	private Unbinder unbinder;

	private EulaUtils.EulaType type = null;

	private PreferenceUtils prefs;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		prefs = new PreferenceUtils(getActivity());

		View view = inflater.inflate(R.layout.fragment_eula, container, false);

		unbinder = ButterKnife.bind(this, view);

		String[] licenseTypes = {getActivity().getString(EulaUtils.EulaType.CONSUMER.getTextResource()), getActivity().getString(EulaUtils.EulaType.COMMERCIAL.getTextResource())};

		ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.helper_list_view_text_color, licenseTypes);

		list.setAdapter(adapter);

		return view;
	}

	@OnItemClick(R.id.eula_list)
	public void onItemClicked(AdapterView<?> parent, View view, int position, long id)
	{
		switch (position)
		{
			case 0:
				/* Consumer */
				showEulaForType(EulaUtils.EulaType.CONSUMER);
				break;
			case 1:
				/* Commercial */
				showEulaForType(EulaUtils.EulaType.COMMERCIAL);
				break;
		}
	}

	@OnClick(R.id.eula_button_accept)
	public void onAcceptClicked()
	{
		GoogleAnalyticsUtils.trackEvent(getActivity(), GerminateScanActivity.getTracker(getActivity(), GerminateScanActivity.TrackerName.APP_TRACKER), getActivity().getString(R.string.ga_event_category_eula), getActivity().getString(R.string.ga_event_action_eula_accepted), type.name());

		prefs.putBoolean(PreferenceUtils.PREFS_EULA_ACCEPTED, true);
		prefs.putString(PreferenceUtils.PREFS_EULA_TYPE, type.name());
		((IntroductionActivity) getActivity()).nextSlide();
	}

	@OnClick(R.id.eula_button_cancel)
	public void onCancelClicked()
	{
		GoogleAnalyticsUtils.trackEvent(getActivity(), GerminateScanActivity.getTracker(getActivity(), GerminateScanActivity.TrackerName.APP_TRACKER), getActivity().getString(R.string.ga_event_category_eula), getActivity().getString(R.string.ga_event_action_eula_declined), type.name());

		getActivity().finish();
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();

		unbinder.unbind();
	}

	private void showEulaForType(EulaUtils.EulaType type)
	{
		this.type = type;

		webView.setVisibility(View.VISIBLE);

		int resource = -1;
		switch (type)
		{
			case COMMERCIAL:
				resource = R.raw.eula_commercial;
				break;
			case CONSUMER:
				resource = R.raw.eula_consumer;
				break;
		}

		String prompt = "";
		try
		{
			InputStream inputStream = getResources().openRawResource(resource);
			byte[] buffer = new byte[inputStream.available()];
			inputStream.read(buffer);
			prompt = new String(buffer);
			inputStream.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		webView.loadDataWithBaseURL(null, prompt, "text/html", "utf-8", null);

		webView.setWebViewClient(new WebViewClient()
		{
			public void onPageFinished(WebView view, String url)
			{
				buttonBar.setVisibility(View.VISIBLE);
			}

			@SuppressWarnings("deprecation")
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
				return handleUri(url);
			}

			@TargetApi(Build.VERSION_CODES.N)
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request)
			{
				return handleUri(request.getUrl().toString());
			}

			private boolean handleUri(String url)
			{
				if (url.startsWith("http:") || url.startsWith("https:"))
				{
					return false;
				}

				try
				{
					// Otherwise allow the OS to handle things like tel, mailto, etc.
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					getActivity().startActivity(intent);
					return true;
				}
				catch (ActivityNotFoundException e)
				{
					return false;
				}
			}
		});
	}
}
