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

import android.app.*;
import android.content.*;
import android.view.*;
import android.widget.*;

import uk.ac.hutton.android.germinatescan.R;

/**
 * {@link uk.ac.hutton.android.germinatescan.util.DialogUtils} contains methods to easily create {@link AlertDialog}s.
 *
 * @author Sebastian Raubach
 */
public class DialogUtils
{
	/**
	 * Creates an {@link AlertDialog} with the given title and message.
	 *
	 * @param context The current {@link Activity}
	 * @param title   The title resource to use
	 * @param error   The error resource to use
	 * @param finish  Finish the {@link Activity} after showing the {@link AlertDialog}?
	 */
	public static void showDialog(Activity context, int title, int error, boolean finish)
	{
		showDialog(context, title, context.getString(error), finish);
	}

	/**
	 * Creates an {@link AlertDialog} with the given title and message.
	 *
	 * @param context  The current {@link Activity}
	 * @param title    The title resource to use
	 * @param error    The error resource to use (prepared for formatting via String.format())
	 * @param finish   Finish the {@link Activity} after showing the {@link AlertDialog}?
	 * @param addition The Strings to use within String.format() on the dialog message
	 */
	public static void showDialog(Activity context, int title, int error, boolean finish, String... addition)
	{
		String resource = context.getString(error);
		resource = String.format(resource, (Object[]) addition);

		showDialog(context, title, resource, finish);
	}

	/**
	 * Creates an {@link AlertDialog} with the given title and message.
	 *
	 * @param context The current {@link Activity}
	 * @param title   The title resource to use
	 * @param message The message String to use
	 * @param finish  Finish the {@link Activity} after showing the {@link AlertDialog}?
	 */
	public static void showDialog(final Activity context, int title, String message, final boolean finish)
	{
		new AlertDialog.Builder(context)
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						if (finish)
						{
							context.finish();
						}
					}
				})
				.setCancelable(!finish)
				.show();
	}

	public static void showOptions(final Context context, int title, final UserOption[] options, boolean cancelable, final OnUserDecisionListener listener)
	{
		new AlertDialog.Builder(context)
				.setTitle(title)
				.setAdapter(new UserOptionAdapter(context, R.layout.list_view_icon, options), new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						listener.onUserDecision(which);
					}
				})
				.setCancelable(cancelable)
				.show();
	}

	public interface OnUserDecisionListener
	{
		void onUserDecision(int index);
	}

	public static class UserOption
	{
		public final String text;
		public final int    icon;

		public UserOption(String text, int icon)
		{
			this.text = text;
			this.icon = icon;
		}
	}

	private static class UserOptionAdapter extends ArrayAdapter<UserOption>
	{
		ViewHolder holder;
		private UserOption[] options;

		public UserOptionAdapter(Context context, int resource, UserOption[] options)
		{
			super(context, resource, options);
			this.options = options;
		}

		public View getView(int position, View convertView, ViewGroup parent)
		{
			final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			if (convertView == null)
			{
				convertView = inflater.inflate(R.layout.list_view_icon, null);

				holder = new ViewHolder();
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				holder.title = (TextView) convertView.findViewById(R.id.title);
				convertView.setTag(holder);
			}
			else
			{
				// view already defined, retrieve view holder
				holder = (ViewHolder) convertView.getTag();
			}

			holder.title.setText(options[position].text);

			holder.icon.setImageResource(options[position].icon);
			return convertView;
		}

		class ViewHolder
		{
			ImageView icon;
			TextView  title;
		}
	}
}
