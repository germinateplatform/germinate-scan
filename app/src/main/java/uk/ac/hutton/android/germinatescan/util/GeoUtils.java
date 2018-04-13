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

import android.location.*;
import android.media.*;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * {@link uk.ac.hutton.android.germinatescan.util.GeoUtils} contains methods for geo-tagging
 *
 * @author Sebastian Raubach
 */
public class GeoUtils
{
	public static final String TAG_USER_COMMENT = "UserComment";

	private static final SimpleDateFormat DATE_TIME = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
	private static final SimpleDateFormat DATE      = new SimpleDateFormat("yyyy:MM:dd");
	private static final SimpleDateFormat TIME      = new SimpleDateFormat("HH:mm:ss");

	/**
	 * Tags the image with the geographic location information
	 *
	 * @param filename The image file
	 * @param location The {@link Location}
	 */
	public static void geoTag(String filename, Location location, Date time, String description)
	{
		try
		{
			ExifInterface exif = new ExifInterface(filename);

			if (location != null)
			{
				double latitude = location.getLatitude();
				double longitude = location.getLongitude();
				/* Add the geographic information to the exif object */
				exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, decimalToDMS(latitude));
				exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, decimalToDMS(longitude));

                /* Set the orientation */
				if (latitude > 0)
				{
					exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
				}
				else
				{
					exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "S");
				}

				if (longitude > 0)
				{
					exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
				}
				else
				{
					exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W");
				}
			}

            /* Add additional information */
			if (time != null)
			{
				exif.setAttribute(ExifInterface.TAG_DATETIME, DATE_TIME.format(time));
				exif.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, DATE.format(time));
				exif.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, TIME.format(time));
			}

			if (!StringUtils.isEmpty(description))
			{
				exif.setAttribute(TAG_USER_COMMENT, description);
			}

            /* Save everything */
			exif.saveAttributes();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Converts decimal latitude/longitude values to DMS format
	 *
	 * @param coord The latitude/longitude value
	 * @return The input value in DMS format
	 */
	public static String decimalToDMS(double coord)
	{
		String output, degrees, minutes, seconds;

		double mod = coord % 1;
		int intPart = (int) coord;

		degrees = String.valueOf(Math.abs(intPart));

		coord = mod * 60;
		mod = coord % 1;
		intPart = (int) coord;

		minutes = String.valueOf(Math.abs(intPart));

		coord = mod * 60;
		intPart = (int) coord;

		seconds = String.valueOf(Math.abs(intPart));

		output = degrees + "/1," + minutes + "/1," + seconds + "/1";

		return output;
	}
}
