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

/**
 * {@link uk.ac.hutton.android.germinatescan.database.Image} is a helper class representing an image stored in the database
 *
 * @author Sebastian Raubach
 */
public class Image extends DatabaseObject
{
	public static String FIELD_PATH = "path";
	public static String FIELD_MAIN_ID = "main_id";

	private String path = null;

	public Image(Long id)
	{
		super(id);
	}

	public Image(Long id, String path)
	{
		super(id);
		this.path = path;
	}

	public String getPath()
	{
		return path;
	}

	public Image setPath(String path)
	{
		this.path = path;

		return this;
	}

	@Override
	public String toString()
	{
		return "Image{" +
				"id=" + id +
				", path='" + path + '\'' +
				'}';
	}
}
