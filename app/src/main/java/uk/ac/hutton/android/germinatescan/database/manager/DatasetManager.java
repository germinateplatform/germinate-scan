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

import java.io.*;
import java.text.*;
import java.util.*;

import uk.ac.hutton.android.germinatescan.database.*;
import uk.ac.hutton.android.germinatescan.util.*;

/**
 * The {@link DatasetManager} extends {@link AbstractManager} and can be used to obtain {@link Dataset}s from the database.
 *
 * @author Sebastian Raubach
 */
public class DatasetManager extends AbstractManager<Dataset>
{
	public static final String TABLE_MAIN     = "main";
	public static final String TABLE_IMAGES   = "images";
	public static final String TABLE_DATASETS = "datasets";

	private static final String CREATE_MAIN_TABLE     = "CREATE TABLE IF NOT EXISTS " + TABLE_MAIN + " (" + Barcode.FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Barcode.FIELD_TIMESTAMP + " TEXT, " + Barcode.FIELD_LATITUDE + " REAL, "
			+ Barcode.FIELD_LONGITUDE + " REAL, " + Barcode.FIELD_ALTITUDE + " REAL, " + Barcode.FIELD_BARCODE + " TEXT, " + Barcode.FIELD_ROW + " INTEGER, " + Barcode.FIELD_COL + " INTEGER)";
	private static final String CREATE_IMAGE_TABLE    = "CREATE TABLE IF NOT EXISTS " + TABLE_IMAGES + " (" + Image.FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Image.FIELD_PATH + " TEXT, " + Image.FIELD_MAIN_ID
			+ " INTEGER, FOREIGN KEY (" + Image.FIELD_MAIN_ID + ") REFERENCES " + TABLE_MAIN + " (" + Barcode.FIELD_ID + "));" + ")";
	private static final String CREATE_DATASETS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_DATASETS + " (" + Dataset.FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Dataset.FIELD_NAME + " TEXT, " + Dataset.FIELD_BARCODES_PER_ROW + " INTEGER, " + Dataset.FIELD_CREATED_ON + " datetime DEFAULT NULL, " + Dataset.FIELD_UPDATED_ON + " timestamp NULL DEFAULT NULL )";

	public DatasetManager(Context context, long datasourceId)
	{
		super(context, datasourceId);
	}

	@Override
	protected DatabaseObjectParser<Dataset> getDefaultParser()
	{
		return Parser.Inst.get();
	}

	@Override
	protected String getTableName()
	{
		return TABLE_DATASETS;
	}

	/**
	 * This method deviates from the implementation of the {@link AbstractManager}. It doesn't actually query any internal database, but rather just
	 * checks which local Sqlite files are available. It'll then connect to them and get the {@link Dataset} from each.
	 *
	 * @return The {@link List} of {@link Dataset}s that are installed locally.
	 */
	@Override
	public List<Dataset> getAll()
	{
		List<Dataset> result = new ArrayList<>();

		if (datasetId == -1)
		{
			File dataFolder = new File(context.getFilesDir(), "data");

			File[] folders = dataFolder.listFiles(new FileFilter()
			{
				@Override
				public boolean accept(File pathname)
				{
					return pathname.isDirectory();
				}
			});

			if (folders != null)
			{
				for (File folder : folders)
				{
					try
					{
						int id = Integer.parseInt(folder.getName());

						DatasetManager m = new DatasetManager(context, id);
						Dataset ds = m.getById(id);

						if (ds != null)
							result.add(ds);
					}
					catch (NumberFormatException e)
					{
					}
				}
			}
		}
		else
		{
			return super.getAll();
		}

		return result;
	}

	/**
	 * Updates the given dataset to the database. If the entry doesn't exist, insert it instead
	 *
	 * @param dataset The dataset to update to the database
	 */
	public void update(Dataset dataset)
	{
		if (dataset.getId() != null)
		{
			try
			{
				open();

				ContentValues values = new ContentValues();
				values.put(Dataset.FIELD_NAME, dataset.getName());
				values.put(Dataset.FIELD_BARCODES_PER_ROW, dataset.getBarcodesPerRow());
				values.put(Dataset.FIELD_CREATED_ON, dataset.getCreatedOn() != null ? dataset.getCreatedOn().getTime() : null);
				values.put(Dataset.FIELD_UPDATED_ON, dataset.getUpdatedOn() != null ? dataset.getUpdatedOn().getTime() : null);

				database.update(getTableName(), values, Barcode.FIELD_ID + " = ?", new String[]{Long.toString(dataset.getId())});
			}
			finally
			{
				close();
			}
		}
		else
		{
			add(dataset);
		}
	}

	/**
	 * Adds the given {@link Dataset} to the database
	 *
	 * @param dataset The {@link Dataset} to add to the database
	 */
	public void add(Dataset dataset)
	{
		if (dataset == null || dataset.hasId())
		{
			return;
		}

		try
		{
			long id = getMaxId();

			create(id);
			dataset.setId(id);

			database.execSQL("DROP TABLE IF EXISTS " + TABLE_MAIN);
			database.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGES);
			database.execSQL("DROP TABLE IF EXISTS " + TABLE_DATASETS);

			database.execSQL(CREATE_MAIN_TABLE);
			database.execSQL(CREATE_IMAGE_TABLE);
			database.execSQL(CREATE_DATASETS_TABLE);

			ContentValues values = new ContentValues();
			values.put(Dataset.FIELD_ID, dataset.getId());
			values.put(Dataset.FIELD_NAME, dataset.getName());
			values.put(Dataset.FIELD_BARCODES_PER_ROW, dataset.getBarcodesPerRow());
			values.put(Dataset.FIELD_CREATED_ON, dataset.getCreatedOn() != null ? dataset.getCreatedOn().getTime() : null);
			values.put(Dataset.FIELD_UPDATED_ON, dataset.getUpdatedOn() != null ? dataset.getUpdatedOn().getTime() : null);

			// Inserting Row
			database.insert(getTableName(), null, values);
		}
		finally
		{
			close();
		}
	}

	private long getMaxId()
	{
		List<Dataset> datasets = getAll();

		long max = 0;

		for (Dataset dataset : datasets)
			max = Math.max(max, dataset.getId());

		return max + 1;
	}

	/**
	 * Removes this datasource from the device
	 *
	 * @throws IOException Thrown if the file deletion fails
	 */
	public void remove() throws IOException
	{
		File dataFolder = new File(new File(context.getFilesDir(), "data"), Long.toString(datasetId));

		if (dataFolder.exists() && dataFolder.isDirectory())
		{
			FileUtils.deleteDirectoryRecursively(dataFolder);
		}
	}

	public void reset()
	{
		try
		{
			List<Dataset> datasets = getAll();
			Dataset dataset;

			if (CollectionUtils.isEmpty(datasets))
			{
				dataset = new Dataset(1L, "Initial dataset");
			}
			else
			{
				dataset = datasets.get(0);
			}

			completelyDelete(dataset.getId());

			create(dataset.getId());

			database.execSQL(CREATE_MAIN_TABLE);
			database.execSQL(CREATE_IMAGE_TABLE);
			database.execSQL(CREATE_DATASETS_TABLE);

			ContentValues values = new ContentValues();
			values.put(Dataset.FIELD_ID, dataset.getId());
			values.put(Dataset.FIELD_NAME, dataset.getName());
			values.put(Dataset.FIELD_BARCODES_PER_ROW, dataset.getBarcodesPerRow());

			// Inserting Row
			database.insert(getTableName(), null, values);
		}
		finally
		{
			close();
		}

//		try
//		{
//			open();
//
//			List<Dataset> datasets = getAll();
//			Dataset dataset;
//
//			if (CollectionUtils.isEmpty(datasets))
//			{
//				dataset = new Dataset(1L, "Initial dataset");
//			}
//			else
//			{
//				dataset = datasets.get(0);
//			}
//
//			database.execSQL("DROP TABLE IF EXISTS " + TABLE_MAIN);
//			database.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGES);
//			database.execSQL("DROP TABLE IF EXISTS " + TABLE_DATASETS);
//
//			database.execSQL(CREATE_MAIN_TABLE);
//			database.execSQL(CREATE_IMAGE_TABLE);
//			database.execSQL(CREATE_DATASETS_TABLE);
//
//			ContentValues values = new ContentValues();
//			values.put(Dataset.FIELD_ID, dataset.getId());
//			values.put(Dataset.FIELD_NAME, dataset.getName());
//
//			// Inserting Row
//			database.insert(getTableName(), null, values);
//		}
//		finally
//		{
//			close();
//		}
	}

	private static class Parser extends DatabaseObjectParser<Dataset>
	{
		@Override
		public Dataset parse(Context context, long datasourceId, DatabaseInternal.AdvancedCursor cursor) throws ParseException
		{
			return new Dataset(cursor.getLong(Dataset.FIELD_ID), new Date(cursor.getLong(Dataset.FIELD_CREATED_ON)), new Date(cursor.getLong(Dataset.FIELD_UPDATED_ON)))
					.setName(cursor.getString(Dataset.FIELD_NAME))
					.setBarcodesPerRow(cursor.getInt(Dataset.FIELD_BARCODES_PER_ROW));
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
