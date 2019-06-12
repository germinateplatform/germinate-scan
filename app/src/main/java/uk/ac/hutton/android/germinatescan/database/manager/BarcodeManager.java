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

package uk.ac.hutton.android.germinatescan.database.manager;

import android.content.*;
import android.database.*;

import java.text.*;
import java.util.*;

import uk.ac.hutton.android.germinatescan.database.*;

/**
 * @author Sebastian Raubach
 */

public class BarcodeManager extends AbstractManager<Barcode>
{
	private DatasetManager datasetManager;

	public BarcodeManager(Context context, long datasourceId)
	{
		super(context, datasourceId);

		this.datasetManager = new DatasetManager(context, datasourceId);
	}

	@Override
	protected DatabaseObjectParser<Barcode> getDefaultParser()
	{
		return Parser.Inst.get();
	}

	@Override
	protected String getTableName()
	{
		return DatasetManager.TABLE_MAIN;
	}

	@Override
	public void delete(Barcode item)
	{
		super.delete(item);

		datasetManager.setUpdatedOn(datasetId, new Date());
	}

	/**
	 * Updates the given barcode to the database. If the entry doesn't exist, insert it instead
	 *
	 * @param barcode The barcode to update to the database
	 */
	public void update(Barcode barcode)
	{
		if (barcode.getId() != null)
		{
			try
			{
				open();

				ContentValues values = new ContentValues();
				values.put(Barcode.FIELD_TIMESTAMP, barcode.getTimestamp());
				values.put(Barcode.FIELD_LATITUDE, barcode.getLatitudeForDatabase());
				values.put(Barcode.FIELD_LONGITUDE, barcode.getLongitudeForDatabase());
				values.put(Barcode.FIELD_ALTITUDE, barcode.getAltitudeForDatabase());
				values.put(Barcode.FIELD_BARCODE, barcode.getBarcode());
				values.put(Barcode.FIELD_ROW, barcode.getRow());
				values.put(Barcode.FIELD_COL, barcode.getCol());

				database.update(getTableName(), values, Barcode.FIELD_ID + " = ?", new String[]{Long.toString(barcode.getId())});
			}
			finally
			{
				close();
			}
		}
		else
		{
			add(barcode);
		}

		datasetManager.setUpdatedOn(datasetId, new Date());
	}

	/**
	 * Adds the given {@link Barcode} to the database
	 *
	 * @param barcode The {@link Barcode} to add to the database
	 */
	public void add(Barcode barcode)
	{
		if (barcode.hasId())
		{
			return;
		}

		try
		{
			open();

			ContentValues values = new ContentValues();
			values.put(Barcode.FIELD_TIMESTAMP, barcode.getTimestamp());
			values.put(Barcode.FIELD_LATITUDE, barcode.getLatitudeForDatabase());
			values.put(Barcode.FIELD_LONGITUDE, barcode.getLongitudeForDatabase());
			values.put(Barcode.FIELD_ALTITUDE, barcode.getAltitudeForDatabase());
			values.put(Barcode.FIELD_BARCODE, barcode.getBarcode());
			values.put(Barcode.FIELD_ROW, barcode.getRow());
			values.put(Barcode.FIELD_COL, barcode.getCol());

			// Inserting Row
			long theId = database.insert(getTableName(), null, values);
			barcode.setId(theId);

		}
		finally
		{
			close();
		}

		datasetManager.setUpdatedOn(datasetId, new Date());
	}

	public Barcode.BarcodeMap getAllAsRows(int nrOfColumns)
	{
		List<Barcode> barcodes = getAll();
		Barcode.BarcodeMap result = new Barcode.BarcodeMap();

		int rowIndex = 0;
		int columnIndex = 0;
		for (Barcode barcode : barcodes)
		{
			List<Barcode> barcodeRow = result.get(rowIndex);

			if (barcodeRow == null)
				barcodeRow = new ArrayList<>();

			barcodeRow.add(barcode);

			result.put(rowIndex, barcodeRow);

			if (++columnIndex == nrOfColumns)
			{
				columnIndex = 0;
				rowIndex++;
			}
		}

		Comparator<Barcode> columnComparator = new Comparator<Barcode>()
		{
			@Override
			public int compare(Barcode lhs, Barcode rhs)
			{
				return (int) Math.signum(lhs.getCol() - rhs.getCol());
			}
		};

		for (List<Barcode> row : result.values())
		{
			Collections.sort(row, columnComparator);
		}

		return result;
	}

	public List<Barcode> getAll()
	{
		List<Barcode> result = new ArrayList<>();
		ImageManager imageManager = new ImageManager(context, datasetId);

		try
		{
			open();

			Cursor cursor = database.rawQuery("SELECT * FROM " + getTableName(), null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast())
			{
				try
				{
					Barcode barcode = getDefaultParser().parse(context, datasetId, new DatabaseInternal.AdvancedCursor(cursor));

					barcode.setImages(imageManager.getForBarcode(barcode.getId()));

					result.add(barcode);
				}
				catch (ParseException e)
				{
					e.printStackTrace();
				}

				cursor.moveToNext();
			}

			cursor.close();
		}
		finally
		{
			close();
		}

		return result;
	}

	private static class Parser extends DatabaseObjectParser<Barcode>
	{
		@Override
		public Barcode parse(Context context, long datasourceId, DatabaseInternal.AdvancedCursor cursor) throws ParseException
		{
			return new Barcode(cursor.getLong(Barcode.FIELD_ID))
					.setTimestamp(cursor.getString(Barcode.FIELD_TIMESTAMP))
					.setLatitude(getSuitableValue(cursor.getDouble(Barcode.FIELD_LATITUDE)))
					.setLongitude(getSuitableValue(cursor.getDouble(Barcode.FIELD_LONGITUDE)))
					.setAltitude(getSuitableValue(cursor.getDouble(Barcode.FIELD_ALTITUDE)))
					.setBarcode(cursor.getString(Barcode.FIELD_BARCODE))
					.setRow(cursor.getInt(Barcode.FIELD_ROW))
					.setCol(cursor.getInt(Barcode.FIELD_COL));
		}

		/**
		 * Determines a suitable value for the given double (NaN -> null)
		 *
		 * @param d The double to convert
		 * @return The converted double (<code>null</code> if the double is NaN)
		 */
		private Double getSuitableValue(Double d)
		{
			return Double.isNaN(d) ? null : d;
		}

		static final class Inst
		{
			public static Parser get()
			{
				return InstanceHolder.INSTANCE;
			}

			/**
			 * {@link InstanceHolder} is loaded on the first execution of {@link Inst#get()} or the first access to {@link InstanceHolder#INSTANCE},
			 * not before.
			 * <p/>
			 * This solution (<a href= "http://en.wikipedia.org/wiki/Initialization_on_demand_holder_idiom" >Initialization-on-demand holder
			 * idiom</a>) is thread-safe without requiring special language constructs (i.e. <code>volatile</code> or <code>synchronized</code>).
			 *
			 * @author Sebastian Raubach
			 */
			private static final class InstanceHolder
			{
				private static final Parser INSTANCE = new Parser();
			}
		}
	}
}
