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

package uk.ac.hutton.android.germinatescan.adapter;

import android.app.*;
import android.content.*;
import android.net.Uri;
import android.text.method.LinkMovementMethod;
import android.view.*;
import android.widget.*;

import com.transitionseverywhere.*;

import androidx.recyclerview.widget.RecyclerView;
import butterknife.*;
import uk.ac.hutton.android.germinatescan.R;
import uk.ac.hutton.android.germinatescan.activity.ApacheLicenseActivity;
import uk.ac.hutton.android.germinatescan.util.StringUtils;

/**
 * The {@link LicenseAdapter} takes care of all the {@link License}s.
 *
 * @author Sebastian Raubach
 */
public class LicenseAdapter extends RecyclerView.Adapter<LicenseAdapter.ViewHolder>
{
	private Activity     context;
	private RecyclerView parent;

	private int expandedPosition = -1;

	public LicenseAdapter(Activity context, RecyclerView parent)
	{
		this.context = context;
		this.parent = parent;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		/* Create a new view from the layout file */
		final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.license_view, parent, false);

		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position)
	{
		final License item = License.values()[position];

		final boolean isExpanded = position == expandedPosition;

		/* Set the content */
		holder.name.setText(item.name);
		holder.author.setText(item.author);
		holder.description.setText(item.description);
		holder.licenseText.setText(StringUtils.fromHtml(item.licenseText));
		/* Make sure to respect hyperlinks in the content */
		holder.licenseText.setMovementMethod(LinkMovementMethod.getInstance());

		/* Show or hide the content */
		holder.licenseText.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
		/* Activate/deactivate the item */
		holder.itemView.setActivated(isExpanded);
		/* On click change the state */
		holder.view.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (expandedPosition != -1)
					notifyItemChanged(expandedPosition);

				expandedPosition = isExpanded ? -1 : holder.getAdapterPosition();

				/* Set a new transition */
				ChangeBounds transition = new ChangeBounds();
				/* For 150 ms */
				transition.setDuration(150);
				/* And start it */
				TransitionManager.beginDelayedTransition(parent, transition);

				/* Let the parent view know that something changed and that it needs to re-layout */
				if (expandedPosition != -1)
					notifyItemChanged(expandedPosition);
			}
		});

		/* Add a click listener */
		holder.homepage.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				/* If this is our app, give the option to show the license as well */
				if (item == License.GERMINATE_SCAN)
				{
					final CharSequence[] options = new CharSequence[]{context.getString(R.string.license_view_option_view_license), context.getString(R.string.license_view_option_view_github)};

					new AlertDialog.Builder(context)
							.setTitle(R.string.license_view_option_title)
							.setItems(options, new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog, int which)
								{
									switch (which)
									{
										case 0:
											context.startActivity(new Intent(context, ApacheLicenseActivity.class));
											break;
										case 1:
											context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(item.url)));
											break;
									}
								}
							})
							.show();
				}
				/* Else, just open the url */
				else
				{
					context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(item.url)));
				}
			}
		});
	}

	@Override
	public int getItemCount()
	{
		return License.values().length;
	}

	/**
	 * {@link License} is an enum holding all the libraries we use in this app with their license information
	 *
	 * @author Sebastian Raubach
	 */
	private enum License
	{
		GERMINATE_SCAN("Germinate Scan", "Information & Computational Sciences, The James Hutton Institute", "Apache License, Version 2.0", "https://github.com/germinateplatform/germinate-scan", "<p>Copyright 2018 Information & Computational Sciences, The James Hutton Institute</p><p>Licensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at</p><p><a href=\"http://www.apache.org/licenses/LICENSE-2.0\">http://www.apache.org/licenses/LICENSE-2.0</a></p><p>Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.</p>"),
		ANDROID_SUPPORT("Android Support Library", "The Android Open Source Project", "Apache License, Version 2.0", "https://github.com/android/platform_frameworks_support", "<p>Copyright (C) 2011 The Android Open Source Project</p><p>Licensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at</p><p><a href=\"http://www.apache.org/licenses/LICENSE-2.0\">http://www.apache.org/licenses/LICENSE-2.0</a></p><p>Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.</p>"),
		BUTTERKNIFE("Butter Knife", "Jake Wharton", "Apache License, Version 2.0", "https://github.com/JakeWharton/butterknife", "<p>Copyright 2013 Jake Wharton</p><p>Licensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at</p><p><a href=\"http://www.apache.org/licenses/LICENSE-2.0\">http://www.apache.org/licenses/LICENSE-2.0</a></p><p>Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.</p>"),
		CHANGELOGLIB("ChangeLog", "Gabriele Mariotti", "Apache License, Version 2.0", "https://github.com/gabrielemariotti/changeloglib", "<p>Copyright 2013-2015 Gabriele Mariotti</p><p>Licensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at</p><p><a href=\"http://www.apache.org/licenses/LICENSE-2.0\">http://www.apache.org/licenses/LICENSE-2.0</a></p><p>Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.</p>"),
		GOOGLE_GSON("google-gson", "Google Inc.", "Apache License, Version 2.0", "https://github.com/google/gson", "<p>Copyright 2008 Google Inc.</p><p>Licensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at</p><p><a href=\"http://www.apache.org/licenses/LICENSE-2.0\">http://www.apache.org/licenses/LICENSE-2.0</a></p><p>Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.</p>"),
		MATERIAL_DESIGN_ICONS("Material design icons", "Material Design Authors", "Apache License, Version 2.0", "https://github.com/google/material-design-icons", "<p>Copyright (C) 2015 Material Design Authors</p><p>Licensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at</p><p><a href=\"http://www.apache.org/licenses/LICENSE-2.0\">http://www.apache.org/licenses/LICENSE-2.0</a></p><p>Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.</p>"),
		MATERIAL_INTRO("material-intro", "Heinrich Reimer", "Apache License, Version 2.0", "https://github.com/HeinrichReimer/material-intro", "<p>Copyright 2016 Heinrich Reimer</p><p>Licensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at</p><p><a href=\"http://www.apache.org/licenses/LICENSE-2.0\">http://www.apache.org/licenses/LICENSE-2.0</a></p><p>Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.</p>"),
		TRANSITIONS_EVERYWHERE("Transitions-Everywhere", "andkulikov", "Apache License, Version 2.0", "https://github.com/andkulikov/Transitions-Everywhere", "<p>Copyright 2014 andkulikov</p><p>Licensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at</p><p><a href=\"http://www.apache.org/licenses/LICENSE-2.0\">http://www.apache.org/licenses/LICENSE-2.0</a></p><p>Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.</p>"),
		ZXING("zxing", "ZXing authors", "Apache License, Version 2.0", "https://github.com/zxing/zxing", "<p>Copyright (C) 2011 ZXing authors</p><p>Licensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at</p><p><a href=\"http://www.apache.org/licenses/LICENSE-2.0\">http://www.apache.org/licenses/LICENSE-2.0</a></p><p>Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.</p>"),
		ZXING_ANDROID_EMBEDDED("ZXing Android Embedded", "ZXing authors, Journey Mobile", "Apache License, Version 2.0", "https://github.com/zxing/zxing", "<p>Copyright (C) 2012-2017 ZXing authors, Journey Mobile</p><p>Licensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at</p><p><a href=\"http://www.apache.org/licenses/LICENSE-2.0\">http://www.apache.org/licenses/LICENSE-2.0</a></p><p>Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.</p>");

		String name;
		String author;
		String description;
		String url;
		String licenseText;

		License(String name, String author, String description, String url, String licenseText)
		{
			this.name = name;
			this.author = author;
			this.description = description;
			this.url = url;
			this.licenseText = licenseText;
		}
	}

	static class ViewHolder extends RecyclerView.ViewHolder
	{
		View view;
		@BindView(R.id.license_view_name)
		TextView  name;
		@BindView(R.id.license_view_author)
		TextView  author;
		@BindView(R.id.license_view_homepage)
		ImageView homepage;
		@BindView(R.id.license_view_description)
		TextView  description;
		@BindView(R.id.license_view_license_text)
		TextView  licenseText;

		ViewHolder(View v)
		{
			super(v);

			view = v;
			ButterKnife.bind(this, v);
		}
	}
}