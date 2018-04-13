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

import android.content.*;
import android.os.*;

import java.io.*;
import java.text.*;
import java.util.*;

import uk.ac.hutton.android.germinatescan.*;

/**
 * {@link uk.ac.hutton.android.germinatescan.util.FileUtils} contains methods to easily access specific predefined locations and to create files.
 *
 * @author Sebastian Raubach
 */
public class FileUtils
{
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss", Locale.getDefault());

	/**
	 * Returns the formatted date of the {@link Long} obtained from {@link System#currentTimeMillis()}.
	 *
	 * @return The formatted date of the {@link Long} obtained from {@link System#currentTimeMillis()}.
	 */
	public static synchronized String getDate()
	{
		return SDF.format(new Date(System.currentTimeMillis()));
	}

	public static void deleteDirectoryRecursively(File folder)
	{
		if (!folder.exists() || !folder.isDirectory())
			return;

		File[] files = folder.listFiles();

		if (files != null)
		{
			for (File file : files)
			{
				if (file.isFile())
				{
					file.delete();
				}
				else
				{
					deleteDirectoryRecursively(file);
				}
			}
		}

		folder.delete();
	}

	/**
	 * Returns the {@link File} representing the {@link ReferenceFolder}
	 *
	 * @param activ           The {@link Context}
	 * @param referenceFolder The {@link ReferenceFolder}
	 * @return The {@link File} representing the {@link ReferenceFolder}
	 */
	public static File getPathToReferenceFolder(Context activ, Long datasetId, ReferenceFolder referenceFolder)
	{
		File folder = new File(new File(new File(Environment.getExternalStorageDirectory(), activ.getString(R.string.app_name_without_spaces)), Long.toString(datasetId)), referenceFolder.name());
		if (!folder.exists())
		{
			folder.mkdirs();
		}

		return folder;
	}

	/**
	 * Creates a new file with the given parameters
	 *
	 * @param activ  The {@link Context}
	 * @param folder The {@link ReferenceFolder} to save the file in
	 * @param ext    The {@link FileExtension} to use
	 * @return The {@link File} representing the created file
	 */
	public static File createFile(Context activ, Long datasetId, ReferenceFolder folder, FileExtension ext)
	{
		return createFile(activ, datasetId, folder, ext, null);
	}

	/**
	 * Creates a new file with the given parameters
	 *
	 * @param activ   The {@link Context}
	 * @param folder  The {@link ReferenceFolder} to save the file in
	 * @param ext     The {@link FileExtension} to use
	 * @param postfix The (optional) postfix to use
	 * @return The {@link File} representing the created file
	 */
	public static File createFile(Context activ, Long datasetId, ReferenceFolder folder, FileExtension ext, String postfix)
	{
		return createFile(activ, getDate(), datasetId, folder, ext, postfix);
	}

	/**
	 * Creates a new file with the given parameters
	 *
	 * @param activ    The {@link Context}
	 * @param filename The filename to use (without extension)
	 * @param folder   The {@link ReferenceFolder} to save the file in
	 * @param ext      The {@link FileExtension} to use
	 * @return The {@link File} representing the created file
	 */
	public static File createFile(Context activ, String filename, Long datasetId, ReferenceFolder folder, FileExtension ext)
	{
		if (StringUtils.isEmpty(filename))
			filename = getDate();

		return createFile(activ, filename, datasetId, folder, ext, null);
	}

	/**
	 * Creates a new file with the given parameters
	 *
	 * @param activ    The {@link Context}
	 * @param filename The filename to use (without extension)
	 * @param folder   The {@link ReferenceFolder} to save the file in
	 * @param ext      The {@link FileExtension} to use
	 * @param postfix  The (optional) postfix to use
	 * @return The {@link File} representing the created file
	 */
	public static File createFile(Context activ, String filename, Long datasetId, ReferenceFolder folder, FileExtension ext, String postfix)
	{
		filename = filename + (postfix == null ? "" : "_" + postfix);
		File file = new File(getPathToReferenceFolder(activ, datasetId, folder), filename + "." + ext.name());

		int counter = 1;
		while (file.exists())
		{
			file = new File(getPathToReferenceFolder(activ, datasetId, folder), filename + "_" + (counter++) + "." + ext.name());
		}

		return file;
	}

	/**
	 * The supported file extensions for data export
	 *
	 * @author Sebastian Raubach
	 */
	public enum FileExtension
	{
		csv,
		tsv,
		txt,
		jpg
	}

	/**
	 * The available sub-folders of the GerminateScan folder on the external drive
	 *
	 * @author Sebastian Raubach
	 */
	public enum ReferenceFolder
	{
		images,
		output
	}
}
