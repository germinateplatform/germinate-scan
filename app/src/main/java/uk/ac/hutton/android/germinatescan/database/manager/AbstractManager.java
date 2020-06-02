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

import android.content.Context;
import android.database.*;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.text.ParseException;
import java.util.*;

import uk.ac.hutton.android.germinatescan.database.*;

/**
 * The {@link AbstractManager} handles interactions with the internal Sqlite database. It will handle opening and closing of the database as well as
 * very basic queries like {@link #getAll()} and {@link #getById(long)}.
 *
 * @author Sebastian Raubach
 */
public abstract class AbstractManager<T extends DatabaseObject>
{
	SQLiteDatabase database;
	Context        context;
	long           datasetId;
	private DatabaseInternal databaseHelper;

	AbstractManager(Context context, long datasetId)
	{
		this.databaseHelper = new DatabaseInternal(context, datasetId);
		this.context = context;
		this.datasetId = datasetId;
	}

	void open() throws SQLException
	{
		database = databaseHelper.openFromFile();
	}

	void create(long datasetId)
	{
		File folder = new File(new File(context.getFilesDir(), "data"), Long.toString(datasetId));
		folder.mkdirs();

		database = SQLiteDatabase.openOrCreateDatabase(new File(folder, datasetId + ".sqlite"), null);
	}

	void completelyDelete(long datasetId)
	{
		File file = new File(new File(new File(context.getFilesDir(), "data"), Long.toString(datasetId)), datasetId + ".sqlite");
		file.delete();
		create(datasetId);
	}

	void close()
	{
		if (database != null)
			database.close();
		if (databaseHelper != null)
			databaseHelper.close();
	}

	/**
	 * Returns all the {@link DatabaseObject}s for this type of {@link AbstractManager}. Uses {@link #getDefaultParser()} and {@link #getTableName()}
	 * to get the data from the database into the Java classes.
	 *
	 * @return The {@link List} of {@link DatabaseObject}s.
	 */
	public List<T> getAll()
	{
		try
		{
			open();

			List<T> result = new ArrayList<>();

			Cursor cursor = database.query(getTableName(), new String[]{getTableName() + ".*"}, null, null, null, null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast())
			{
				try
				{
					T item = getDefaultParser().parse(context, datasetId, new DatabaseInternal.AdvancedCursor(cursor));
					result.add(item);
				}
				catch (ParseException e)
				{
					e.printStackTrace();
				}

				cursor.moveToNext();
			}

			cursor.close();

			return result;
		}
		finally
		{
			close();
		}
	}

	/**
	 * Deletes the given {@link Barcode} from the database
	 *
	 * @param item The {@link Barcode} to delete
	 */
	public void delete(T item)
	{
		if (item == null)
		{
			return;
		}

		try
		{
			open();

			database.delete(getTableName(), DatabaseObject.FIELD_ID + " = ?", new String[]{String.valueOf(item.getId())});
		}
		finally
		{
			close();
		}
	}

	/**
	 * Returns the {@link DatabaseObject} for this type of {@link AbstractManager}. Uses {@link #getDefaultParser()} and {@link #getTableName()} to
	 * get the data from the database into the Java classes.
	 *
	 * @param id The id of the {@link DatabaseObject}
	 * @return The {@link DatabaseObject}.
	 */
	public T getById(long id)
	{
		try
		{
			open();

			T result = null;

			Cursor cursor = database.query(getTableName(), new String[]{getTableName() + ".*"}, DatabaseObject.FIELD_ID + " = ?", new String[]{Long.toString(id)}, null, null, null);
			cursor.moveToFirst();
			if (!cursor.isAfterLast())
			{
				try
				{
					result = getDefaultParser().parse(context, datasetId, new DatabaseInternal.AdvancedCursor(cursor));
				}
				catch (ParseException e)
				{
					e.printStackTrace();
				}
			}

			cursor.close();

			return result;
		}
		finally
		{
			close();
		}
	}

	/**
	 * Returns the default {@link DatabaseObjectParser} for this type of {@link DatabaseObject}.
	 *
	 * @return The default {@link DatabaseObjectParser} for this type of {@link DatabaseObject}.
	 */
	protected abstract DatabaseObjectParser<T> getDefaultParser();

	/**
	 * Returns the database table for this type of {@link DatabaseObject}.
	 *
	 * @return The database table for this type of {@link DatabaseObject}.
	 */
	protected abstract String getTableName();
}
