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

import android.os.*;
import android.text.*;

/**
 * {@link uk.ac.hutton.android.germinatescan.util.StringUtils} contains methods to manipulate/check {@link String}s.
 *
 * @author Sebastian Raubach
 */
public class StringUtils
{
	public static Spanned fromHtml(String html)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
		{
			return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
		}
		else
		{
			return Html.fromHtml(html);
		}
	}

	/**
	 * Checks if the given {@link String} is either <code>null</code> or empty after calling {@link String#trim()}.
	 *
	 * @param input The {@link String} to check
	 * @return <code>true</code> if the given {@link String} is <code>null</code> or empty after calling {@link String#trim()}.
	 */
	public static boolean isEmpty(String input)
	{
		if (input == null)
		{
			return true;
		}
		else
		{
			return input.trim().isEmpty();
		}
	}

	/**
	 * Checks if the given {@link String}s are either <code>null</code> or empty after calling {@link String#trim()}.
	 *
	 * @param input The {@link String}s to check
	 * @return <code>true</code> if any of the given {@link String}s is <code>null</code> or empty after calling {@link String#trim()}.
	 */
	public static boolean isEmpty(String... input)
	{
		if (input == null)
		{
			return true;
		}
		for (String text : input)
		{
			if (text == null)
			{
				return true;
			}
			else if (text.trim().isEmpty())
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Joins the given parts to a String separated by the delimiter. {@link #isEmpty(String)} will be called on each part and the part will only be
	 * added if the result is <code>true</code>.
	 *
	 * @param delimiter The delimiter to use
	 * @param parts     The parts to join
	 * @return The joined parts or an empty {@link String} if either <code>parts.length == 0</code> or there is no part that returns
	 * <code>false</code> for {@link #isEmpty(String)}
	 */
	public static String join(String delimiter, String... parts)
	{
		if (parts.length == 0)
		{
			return "";
		}

		StringBuilder builder = new StringBuilder();

		boolean atLeastOne = false;
		for (String part : parts)
		{
			if (!isEmpty(part))
			{
				atLeastOne = true;
				builder.append(part)
					   .append(delimiter);
			}
		}

        /* Remove the last delimiter. We have to do it this way, because we
		 * don't know how many (of any) parts pass the check */
		if (atLeastOne)
		{
			int startIndex = builder.lastIndexOf(delimiter);

			if (startIndex != -1)
			{
				builder.delete(startIndex, startIndex + delimiter.length());
			}
		}

		return builder.toString();
	}

	/**
	 * Checks if the given parts are pairwise equal, i.e. calls {@link String#equals(Object)} on each adjacent pair.
	 *
	 * @param parts The parts to compare
	 * @return <code>true</code> if all parts are equal, <code>false</code> otherwise
	 */
	public static boolean areEqual(String... parts)
	{
		for (int i = 0; i < parts.length - 1; i++)
		{
			if (!parts[i].equals(parts[i + 1]))
			{
				return false;
			}
		}

		return true;
	}
}
