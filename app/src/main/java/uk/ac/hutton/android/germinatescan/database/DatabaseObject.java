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

import java.io.*;
import java.util.*;

public abstract class DatabaseObject implements Serializable
{
	public static final String FIELD_ID         = "id";
	public static final String FIELD_CREATED_ON = "created_on";
	public static final String FIELD_UPDATED_ON = "updated_on";
	protected Long id;
	protected Date createdOn = new Date(System.currentTimeMillis());
	protected Date updatedOn = new Date(System.currentTimeMillis());;

	public DatabaseObject()
	{
	}

	public DatabaseObject(Long id)
	{
		this.id = id;
	}

	public DatabaseObject(Long id, Date createdOn, Date updatedOn)
	{
		this.id = id;
		this.createdOn = createdOn;
		this.updatedOn = updatedOn;
	}

	public Long getId()
	{
		return this.id;
	}

	public DatabaseObject setId(Long id)
	{
		this.id = id;
		return this;
	}

	public Date getCreatedOn()
	{
		return this.createdOn;
	}

	public DatabaseObject setCreatedOn(Date createdOn)
	{
		this.createdOn = createdOn;
		return this;
	}

	public Date getUpdatedOn()
	{
		return this.updatedOn;
	}

	public DatabaseObject setUpdatedOn(Date updatedOn)
	{
		this.updatedOn = updatedOn;
		return this;
	}

	public boolean hasId()
	{
		return id != null && id != -1L;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DatabaseObject that = (DatabaseObject) o;

		return id != null ? id.equals(that.id) : that.id == null;
	}

	@Override
	public int hashCode()
	{
		return id != null ? id.hashCode() : 0;
	}

	public String toString()
	{
		return "DatabaseObject{id=" + this.id + ", createdOn=" + this.createdOn + ", updatedOn=" + this.updatedOn + '}';
	}
}