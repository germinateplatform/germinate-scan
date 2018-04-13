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

public class ImageManager extends AbstractManager<Image>
{
	public ImageManager(Context context, long datasourceId)
	{
		super(context, datasourceId);
	}

	@Override
	protected DatabaseObjectParser<Image> getDefaultParser()
	{
		return Parser.Inst.get();
	}

	@Override
	protected String getTableName()
	{
		return DatasetManager.TABLE_IMAGES;
	}

	/**
	 * Inserts the given {@link Image} into the database associated with the given {@link
	 * Barcode}
	 *
	 * @param code  The associated {@link Barcode}
	 * @param image The {@link Image}
	 */
	public void add(Barcode code, Image image)
	{
		try
		{
			open();

			ContentValues values = new ContentValues();
			values.put(Image.FIELD_PATH, image.getPath());
			if (code != null)
			{
				values.put(Image.FIELD_MAIN_ID, code.getId());
			}
			else
			{
				values.putNull(Image.FIELD_MAIN_ID);
			}

			long id = database.insert(getTableName(), null, values);

			image.setId(id);
		}
		finally
		{
			close();
		}
	}

	public List<Image> getForBarcode(Long barcode)
	{
		List<Image> result = new ArrayList<>();

		try
		{
			open();

			Cursor cursor = database.rawQuery("SELECT * FROM " + getTableName() + " WHERE " + Image.FIELD_MAIN_ID + " = ?", new String[]{Long.toString(barcode)});
			cursor.moveToFirst();
			while (!cursor.isAfterLast())
			{
				try
				{
					result.add(getDefaultParser().parse(context, datasetId, new DatabaseInternal.AdvancedCursor(cursor)));
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

	private static class Parser extends DatabaseObjectParser<Image>
	{
		@Override
		public Image parse(Context context, long datasourceId, DatabaseInternal.AdvancedCursor cursor) throws ParseException
		{
			return new Image(cursor.getLong(Image.FIELD_ID))
					.setPath(cursor.getString(Image.FIELD_PATH));
		}

		static final class Inst
		{
			public static ImageManager.Parser get()
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
				private static final ImageManager.Parser INSTANCE = new ImageManager.Parser();
			}
		}
	}
}
