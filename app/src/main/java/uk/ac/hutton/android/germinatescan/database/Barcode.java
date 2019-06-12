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

package uk.ac.hutton.android.germinatescan.database;

import android.content.*;
import android.location.*;
import android.preference.*;

import java.text.*;
import java.util.*;

import uk.ac.hutton.android.germinatescan.*;
import uk.ac.hutton.android.germinatescan.activity.*;
import uk.ac.hutton.android.germinatescan.util.*;

/**
 * {@link uk.ac.hutton.android.germinatescan.database.Barcode} is a simple bean class for a bar code. It contains the code itself, a time stamp, a
 * geographic location).
 *
 * @author Sebastian Raubach
 */
public class Barcode extends DatabaseObject
{
	public static final  String           FIELD_TIMESTAMP        = "time";
	public static final  String           FIELD_LATITUDE         = "latitude";
	public static final  String           FIELD_LONGITUDE        = "longitude";
	public static final  String           FIELD_ALTITUDE         = "altitude";
	public static final  String           FIELD_BARCODE          = "barcode";
	public static final  String           FIELD_ROW              = "row";
	public static final  String           FIELD_COL              = "col";
	public static final  SimpleDateFormat DATE_FORMAT            = new SimpleDateFormat(BarcodeReader.INSTANCE.getString(R.string.general_date_format), Locale.getDefault());
	private static final DecimalFormat    DECIMAL_FORMAT         = new DecimalFormat("###.####");
	private static final int              INVALID_LOCATION_VALUE = -300000;

	private String timestamp;
	private Double latitude  = null;
	private Double longitude = null;
	private Double altitude  = null;
	private String barcode;
	private int    row;
	private int    col;

	private boolean isNullBarcode = true;

	private List<Image> images = new ArrayList<>();

	public Barcode()
	{
	}

	public Barcode(Long id)
	{
		super(id);
	}

	/**
	 * Creates a new {@link Barcode} with the given bar code string
	 *
	 * @param barcode The bar code String
	 */
	public Barcode(String barcode)
	{
		this.barcode = barcode;

		isNullBarcode = StringUtils.isEmpty(barcode);
	}

	/**
	 * Creates a new {@link Barcode} with the given bar code string
	 *
	 * @param id      The id of the {@link Barcode}
	 * @param barcode The bar code String
	 */
	public Barcode(Long id, String barcode)
	{
		super(id);
		this.barcode = barcode;
		isNullBarcode = StringUtils.isEmpty(barcode);
	}

	public boolean isNullBarcode()
	{
		return isNullBarcode;
	}

	public String getFormattedTimestamp()
	{
		if (isNullBarcode)
		{
			return "";
		}
		else
		{
			long time = Long.parseLong(timestamp);
			return DATE_FORMAT.format(new Date(time));
		}
	}

	/**
	 * Returns the time stamp as it is stored in the database
	 *
	 * @return The time stamp as it is stored in the database
	 */
	public String getTimestamp()
	{
		return timestamp;
	}

	/**
	 * Sets the time stamp
	 *
	 * @param timestamp The time stamp
	 */
	public Barcode setTimestamp(String timestamp)
	{
		this.timestamp = timestamp;
		return this;
	}

	/**
	 * Returns the latitude of the bar code
	 * <p/>
	 * <b>IMPORTANT: If there is no valid location, this will return {@link Double#NaN}</b>
	 *
	 * @return The latitude of the bar code
	 */
	public double getLatitude()
	{
		if (latitude == INVALID_LOCATION_VALUE)
		{
			return Double.NaN;
		}
		else
		{
			return latitude;
		}
	}

	public Barcode setLatitude(Double latitude)
	{
		this.latitude = latitude;
		return this;
	}

	public Barcode setLatitude(Location location)
	{
		if (location != null)
		{
			this.latitude = location.getLatitude();
		}
		else
		{
			this.latitude = Double.NaN;
		}

		return this;
	}

	public void setLocation(Location location)
	{
		setLatitude(location);
		setLongitude(location);
		setAltitude(location);
	}

	/**
	 * Returns the value to store in the database for the latitude
	 * <p/>
	 * <b>IMPORTANT: If there is no valid location, this will return {@link #INVALID_LOCATION_VALUE} ({@value #INVALID_LOCATION_VALUE})</b>
	 *
	 * @return The latitude value to store in the database
	 */
	public double getLatitudeForDatabase()
	{
		if (latitude == null || Double.isNaN(latitude))
		{
			return INVALID_LOCATION_VALUE;
		}
		else
		{
			return latitude;
		}
	}

	public Barcode setLongitude(Double longitude)
	{
		this.longitude = longitude;
		return this;
	}

	public Barcode setAltitude(Double altitude)
	{
		this.altitude = altitude;
		return this;
	}

	/**
	 * Returns the longitude of the bar code
	 * <p/>
	 * <b>IMPORTANT: If there is no valid location, this will return {@link Double#NaN}</b>
	 *
	 * @return The longitude of the bar code
	 */
	public double getLongitude()
	{
		if (longitude == INVALID_LOCATION_VALUE)
		{
			return Double.NaN;
		}
		else
		{
			return longitude;
		}
	}

	public void setLongitude(Location location)
	{
		if (location != null)
		{
			this.longitude = location.getLongitude();
		}
		else
		{
			this.longitude = Double.NaN;
		}
	}

	/**
	 * Returns the value to store in the database for the longitude
	 * <p/>
	 * <b>IMPORTANT: If there is no valid location, this will return {@link #INVALID_LOCATION_VALUE} ({@value #INVALID_LOCATION_VALUE})</b>
	 *
	 * @return The longitude value to store in the database
	 */
	public double getLongitudeForDatabase()
	{
		if (longitude == null || Double.isNaN(longitude))
		{
			return INVALID_LOCATION_VALUE;
		}
		else
		{
			return longitude;
		}
	}

	/**
	 * Returns the altitude of the bar code
	 * <p/>
	 * <b>IMPORTANT: If there is no valid location, this will return {@link Double#NaN}</b>
	 *
	 * @return The altitude of the bar code
	 */
	public double getAltitude()
	{
		if (altitude == INVALID_LOCATION_VALUE)
		{
			return Double.NaN;
		}
		else
		{
			return altitude;
		}
	}

	public void setAltitude(Location location)
	{
		if (location != null)
		{
			this.altitude = location.getAltitude();
		}
		else
		{
			this.altitude = Double.NaN;
		}
	}

	/**
	 * Returns the value to store in the database for the altitude
	 * <p/>
	 * <b>IMPORTANT: If there is no valid location, this will return {@link #INVALID_LOCATION_VALUE} ({@value #INVALID_LOCATION_VALUE})</b>
	 *
	 * @return The longitude value to store in the database
	 */
	public double getAltitudeForDatabase()
	{
		if (altitude == null || Double.isNaN(altitude))
		{
			return INVALID_LOCATION_VALUE;
		}
		else
		{
			return altitude;
		}
	}

	public void addImage(Image image)
	{
		images.add(image);
	}

	public List<Image> getImages()
	{
		return images;
	}

	public Barcode setImages(List<Image> images)
	{
		this.images = images;

		return this;
	}

	public String getBarcode()
	{
		return barcode;
	}

	public Barcode setBarcode(String barcode)
	{
		this.barcode = barcode;
		isNullBarcode = StringUtils.isEmpty(barcode);

		return this;
	}

	public int getRow()
	{
		return row;
	}

	public Barcode setRow(int row)
	{
		this.row = row;
		return this;
	}

	public int getCol()
	{
		return col;
	}

	public Barcode setCol(int col)
	{
		this.col = col;
		return this;
	}

	public boolean hasValidPosition()
	{
		return (longitude != INVALID_LOCATION_VALUE && latitude != INVALID_LOCATION_VALUE && altitude != INVALID_LOCATION_VALUE);
	}

	@Override
	public String toString()
	{
		if (isNullBarcode)
		{
			return "";
		}
		else
		{
			return "Barcode [id=" + id + ", timestamp=" + getFormattedTimestamp() + ", location=" + getLocationString() + ", barcode=" + barcode + ", row=" + row + ", col=" + col + "]";
		}
	}

	/**
	 * Returns the {@link Barcode} in the format used for data export. This will contain all fields separated by {@link BarcodeReader#DELIMITER}
	 *
	 * @return The {@link Barcode} in the format used for data export
	 */
	public String toStringForExport(boolean justBarcode)
	{
		if (isNullBarcode())
		{
			return BarcodeReader.INSTANCE.getString(R.string.barcode_property_null_token);
		}

		List<BarcodeProperty> props = BarcodeProperty.getUsedProperties();

		/* Check if props is empty to prevent IndexOutOfBoundsException later on */
		if (props.isEmpty())
		{
			return "";
		}

		if (justBarcode)
		{
			if (props.contains(BarcodeProperty.BARCODE))
				return getBarcodeProperty(BarcodeProperty.BARCODE);
			else
				return "";
		}
		else
		{
			/* Start with the first one here, so we don't need to have any 'if's in the for loop */
			StringBuilder result = new StringBuilder(getBarcodeProperty(props.get(0)));

			for (int i = 1; i < props.size(); i++)
			{
				/* Append the rest with the delimiter in between */
				result.append(BarcodeReader.DELIMITER)
					  .append(getBarcodeProperty(props.get(i)));
			}

			return result.toString();
		}
	}

	public String getStringForImageTag()
	{
		return barcode + " | " + getFormattedTimestamp() + " | " + getLocationString();
	}

	/**
	 * Returns the location formatted readable for humans
	 *
	 * @return The location formatted readable for humans
	 */
	public String getLocationString()
	{
		return getLatitudeString() + " " + getLongitudeString();
	}

	/**
	 * Returns the location formatted readable for humans
	 *
	 * @return The location formatted readable for humans
	 */
	public String getLongitudeString()
	{
		if (isNullBarcode || longitude == null)
		{
			return "";
		}
		else
		{
			String longitudeString;

			if (longitude < -180 || longitude > 180 || Double.isNaN(longitude))
			{
				longitudeString = BarcodeReader.INSTANCE.getString(R.string.location_unknown);
			}
			else
			{
				longitudeString = DECIMAL_FORMAT.format(longitude);
			}

			return BarcodeReader.INSTANCE.getString(R.string.longitude_value, longitudeString);
		}
	}

	/**
	 * Returns the location formatted readable for humans
	 *
	 * @return The location formatted readable for humans
	 */
	public String getLatitudeString()
	{
		if (isNullBarcode || latitude == null)
		{
			return "";
		}
		else
		{
			String latitudeString;

			if (latitude < -90 || latitude > 90 || Double.isNaN(latitude))
			{
				latitudeString = BarcodeReader.INSTANCE.getString(R.string.location_unknown);
			}
			else
			{
				latitudeString = DECIMAL_FORMAT.format(latitude);
			}

			return BarcodeReader.INSTANCE.getString(R.string.latitude_value, latitudeString);
		}
	}

	public String getBarcodeProperty(BarcodeProperty prop)
	{
		switch (prop)
		{
			case ID:
				return Long.toString(id);
			case TIMESTAMP:
				return getFormattedTimestamp();
			case LATITUDE:
				return hasValidPosition() ? Double.toString(latitude) : "?";
			case LONGITUDE:
				return hasValidPosition() ? Double.toString(longitude) : "?";
			case ALTITUDE:
				return hasValidPosition() ? Double.toString(altitude) : "?";
			case BARCODE:
				return barcode;
			default:
				return "";
		}
	}

	/**
	 * @author Sebastian Raubach
	 */
	public enum BarcodeProperty
	{
		ID(R.string.barcode_property_id, R.string.barcode_example_id),
		TIMESTAMP(R.string.barcode_property_timestamp, -1),
		LATITUDE(R.string.barcode_property_latitute, R.string.barcode_example_latitute),
		LONGITUDE(R.string.barcode_property_longitude, R.string.barcode_example_longitude),
		ALTITUDE(R.string.barcode_property_altitude, R.string.barcode_example_altitude),
		BARCODE(R.string.barcode_property_barcode, R.string.barcode_example_barcode);

		private int    nameResource;
		private String displayName;

		private int    exampleResource;
		private String displayExample;

		BarcodeProperty(int nameResource, int exampleResource)
		{
			this.nameResource = nameResource;
			this.exampleResource = exampleResource;
		}

		public static String joinNames(List<BarcodeProperty> items, String delimiter)
		{
			if (CollectionUtils.isEmpty(items))
			{
				return "";
			}

			StringBuilder builder = new StringBuilder(items.get(0).name());

			for (int i = 1; i < items.size(); i++)
			{
				builder.append(delimiter)
					   .append(items.get(i).name());
			}

			return builder.toString();
		}

		public static String getDefaultAsString()
		{
			return StringUtils.join(",", BARCODE.name(), TIMESTAMP.name(), LATITUDE.name(), LONGITUDE.name(), ALTITUDE.name());
		}

		public static List<BarcodeProperty> getUsedProperties()
		{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BarcodeReader.INSTANCE);
			List<String> itemStrings = CollectionUtils.parseList(prefs.getString(PreferenceUtils.PREFS_EXPORT_BARCODE_PROPERTIES, ""), ",");
			List<BarcodeProperty> items = new ArrayList<>();

			for (String string : itemStrings)
			{
				try
				{
					items.add(BarcodeProperty.valueOf(string));
				}
				catch (IllegalArgumentException e)
				{
					/* Do nothing here */
				}
			}

			return items;
		}

		public static List<BarcodeProperty> getUnusedProperties()
		{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BarcodeReader.INSTANCE);
			List<String> itemStrings = CollectionUtils.parseList(prefs.getString(PreferenceUtils.PREFS_EXPORT_BARCODE_PROPERTIES, ""), ",");
			List<BarcodeProperty> items = new ArrayList<>(Arrays.asList(values()));

			for (String string : itemStrings)
			{
				try
				{
					items.remove(BarcodeProperty.valueOf(string));
				}
				catch (IllegalArgumentException e)
				{
					/* Do nothing here */
				}
			}

			return items;
		}

		/**
		 * Returns an example for this property
		 *
		 * @return An example for this property
		 */
		public String getExample()
		{
			/* Check if we already got the string resource before */
			if (StringUtils.isEmpty(displayExample))
			{
				if (exampleResource == -1)
				{
					displayExample = DATE_FORMAT.format(new Date(System.currentTimeMillis()));
				}
				else
				{
					displayExample = BarcodeReader.INSTANCE.getString(exampleResource);
				}
			}

			return displayExample;
		}

		@Override
		public String toString()
		{
			/* Check if we already got the string resource before */
			if (StringUtils.isEmpty(displayName))
			{
				displayName = BarcodeReader.INSTANCE.getString(nameResource);
			}

			return displayName;
		}
	}

	public static class BarcodeMap extends TreeMap<Integer, List<Barcode>>
	{

	}
}
