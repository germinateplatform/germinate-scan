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
import android.database.*;
import android.database.sqlite.*;

import java.io.*;
import java.util.*;

/**
 * {@link DatabaseHandler} extends {@link SQLiteOpenHelper} and takes care of all database interactions within Germinate Scan.
 *
 * @author Sebastian Raubach
 */
public class DatabaseHandler extends SQLiteOpenHelper
{
	private static final int    DATABASE_VERSION = 2;
	private static final String DATABASE_NAME    = "BarcodeReader";
	private static final String TABLE_MAIN         = "main";
	private static final String TABLE_IMAGES       = "images";
	private static final String DROP_MAIN_TABLE    = "DROP TABLE IF EXISTS " + TABLE_MAIN;
	private static final String DROP_IMAGE_TABLE   = "DROP TABLE IF EXISTS " + TABLE_IMAGES;
	private static final String KEY_MAIN_ID        = "id";
	private static final String KEY_MAIN_TIME      = "time";
	private static final String KEY_MAIN_LAT       = "latitude";
	private static final String KEY_MAIN_LNG       = "longitude";
	private static final String KEY_MAIN_ALT       = "altitude";
	private static final String KEY_MAIN_BARCODE   = "barcode";
	private static final String KEY_MAIN_ROW       = "row";
	private static final String KEY_MAIN_COL       = "col";
	private static final String KEY_IMAGES_ID      = "id";
	private static final String KEY_IMAGES_PATH    = "path";
	private static final String KEY_IMAGES_MAIN_ID = "main_id";
	private static final String CREATE_MAIN_TABLE  = "CREATE TABLE IF NOT EXISTS " + TABLE_MAIN + " (" + KEY_MAIN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_MAIN_TIME + " TEXT, " + KEY_MAIN_LAT + " REAL, "
			+ KEY_MAIN_LNG + " REAL, " + KEY_MAIN_ALT + " REAL, " + KEY_MAIN_BARCODE + " TEXT, " + KEY_MAIN_ROW + " INTEGER, " + KEY_MAIN_COL + " INTEGER)";
	private static final String CREATE_IMAGE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_IMAGES + " (" + KEY_IMAGES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_IMAGES_PATH + " TEXT, " + KEY_IMAGES_MAIN_ID
			+ " INTEGER, FOREIGN KEY (" + KEY_IMAGES_MAIN_ID + ") REFERENCES " + TABLE_MAIN + " (" + KEY_MAIN_ID + "));" + ")";
	private Context context;


	public DatabaseHandler(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	/**
	 * Generates a SQL placeholder String of the form: "?,?,?,?" for the given size.
	 *
	 * @param size The number of placeholders to generate
	 * @return The generated String
	 */
	public static String generateSqlPlaceholderString(int size)
	{
		if (size < 1)
		{
			return "";
		}

		StringBuilder builder = new StringBuilder();

		builder.append("?");

		for (int i = 1; i < size; i++)
		{
			builder.append(",?");
		}

		return builder.toString();
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		/* Create the two tables */
		db.execSQL(CREATE_MAIN_TABLE);
		db.execSQL(CREATE_IMAGE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		switch (oldVersion)
		{
			case 1:
				/* If we're using the database version 1, we need to add the new images table */
				db.execSQL(DROP_IMAGE_TABLE);
				db.execSQL(CREATE_IMAGE_TABLE);
				break;
			default:
				throw new IllegalStateException("onUpgrade() with unknown newVersion " + newVersion);
		}
	}

	/**
	 * Clears the database
	 */
	public void deleteDatabase()
	{
		context.deleteDatabase(DATABASE_NAME);
	}

	/**
	 * Returns a list of all Barcode entries
	 *
	 * @return A list of all Barcode entries
	 */
	public List<Barcode> getAllBarcodes()
	{
		List<Barcode> result = new ArrayList<>();

		String str = "SELECT * FROM " + TABLE_MAIN;
		Cursor cursor = getReadableDatabase().rawQuery(str, null);
		if (cursor.moveToFirst())
		{
			do
			{
				int i = 0;
				Barcode barcode = new Barcode();
				barcode.setId(cursor.getLong(i++));
				barcode.setTimestamp(cursor.getString(i++));
				barcode.setLatitude(getSuitableValue(cursor.getDouble(i++)));
				barcode.setLongitude(getSuitableValue(cursor.getDouble(i++)));
				barcode.setAltitude(getSuitableValue(cursor.getDouble(i++)));
				barcode.setBarcode(cursor.getString(i++));
				barcode.setRow(cursor.getInt(i++));
				barcode.setCol(cursor.getInt(i++));

				result.add(barcode);
			} while (cursor.moveToNext());
		}
		cursor.close();

		for (Barcode barcode : result)
		{
			addImages(barcode);
		}

		return result;
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

	/**
	 * Requests the associated images from the database and links them to the {@link uk.ac.hutton.android.germinatescan.database.Barcode} instance
	 *
	 * @param item The {@link uk.ac.hutton.android.germinatescan.database.Barcode} for which to get the images
	 */
	private void addImages(Barcode item)
	{
		Set<String> toDelete = new HashSet<>();

		Cursor cursor = getReadableDatabase().query(TABLE_IMAGES, new String[]{KEY_IMAGES_ID, KEY_IMAGES_PATH}, KEY_IMAGES_MAIN_ID + " = ?", new String[]{Long.toString(item.getId())}, null, null, null);
		if (cursor.moveToFirst())
		{
			do
			{
				int i = 0;
				Image image = new Image(cursor.getLong(i++), cursor.getString(i++));

				File file = new File(image.getPath());

				if (!file.exists() || !file.isFile())
				{
					toDelete.add(Long.toString(image.getId()));
				}
				else
				{
					item.addImage(image);
				}
			} while (cursor.moveToNext());
		}

		/* Delete the entries that do not link to existing images anymore */
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_MAIN, KEY_MAIN_ID + " IN (" + generateSqlPlaceholderString(toDelete.size()) + ")", toDelete.toArray(new String[toDelete.size()]));
		db.close();
	}
}
