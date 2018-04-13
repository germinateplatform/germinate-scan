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

package uk.ac.hutton.android.germinatescan.util.barcodehandler;

import android.location.*;

import java.util.*;

import uk.ac.hutton.android.germinatescan.activity.*;
import uk.ac.hutton.android.germinatescan.database.*;

/**
 * @author Sebastian Raubach
 */

public abstract class BarcodeHandler
{
	protected GerminateScanActivity context;

	public BarcodeHandler(GerminateScanActivity context)
	{
		this.context = context;
	}

	public abstract List<Barcode> handle(String input, Location location, int index);

	public abstract void deleteBarcode();

	public abstract void deleteRow();
}
