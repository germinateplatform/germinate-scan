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

import com.google.gson.*;
import com.google.gson.reflect.*;

import java.util.*;

import uk.ac.hutton.android.germinatescan.util.*;

/**
 * @author Sebastian Raubach
 */

public class Dataset extends DatabaseObject
{
	public static final String FIELD_NAME                 = "name";
	public static final String FIELD_BARCODES_PER_ROW     = "barcodes_per_row";
	public static final String FIELD_PRELOADED_PHENOTYPES = "preloaded_phenotypes";
	public static final String FIELD_CURRENT_PHENOTYPE    = "current_phenotype";

	private String       name;
	private int          barcodesPerRow;
	private List<String> preloadedPhenotypes;
	private Integer      currentPhenotype;

	public Dataset(String name)
	{
		this.name = name;
	}

	public Dataset(Long id)
	{
		super(id);
	}

	public Dataset(Long id, Date createdOn, Date updatedOn)
	{
		super(id, createdOn, updatedOn);
	}

	public Dataset(Long id, String name)
	{
		super(id);
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public Dataset setName(String name)
	{
		this.name = name;
		return this;
	}

	public int getBarcodesPerRow()
	{
		return barcodesPerRow;
	}

	public Dataset setBarcodesPerRow(int barcodesPerRow)
	{
		this.barcodesPerRow = barcodesPerRow;
		return this;
	}

	public List<String> getPreloadedPhenotypes()
	{
		return preloadedPhenotypes;
	}

	public Dataset setPreloadedPhenotypes(List<String> preloadedPhenotypes)
	{
		this.preloadedPhenotypes = preloadedPhenotypes;
		return this;
	}

	public String getPreloadedPhenotypesAsString()
	{
		if (!CollectionUtils.isEmpty(preloadedPhenotypes))
			return new Gson().toJson(preloadedPhenotypes);
		else
			return null;
	}

	public Dataset setPreloadedPhenotypesAsString(String preloadedPhenotypesAsString)
	{
		if (!StringUtils.isEmpty(preloadedPhenotypesAsString))
		{
			try
			{
				preloadedPhenotypes = new Gson().fromJson(preloadedPhenotypesAsString, new TypeToken<List<String>>()
				{
				}.getType());
			}
			catch (JsonParseException e)
			{
				preloadedPhenotypes = null;
			}
		}

		return this;
	}

	public Integer getCurrentPhenotype()
	{
		return currentPhenotype;
	}

	public Dataset setCurrentPhenotype(Integer currentPhenotype)
	{
		this.currentPhenotype = currentPhenotype;
		return this;
	}
}
