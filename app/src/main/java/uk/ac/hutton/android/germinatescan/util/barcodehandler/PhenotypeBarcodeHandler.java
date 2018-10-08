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
import uk.ac.hutton.android.germinatescan.util.*;

/**
 * @author Sebastian Raubach
 */

public abstract class PhenotypeBarcodeHandler extends BarcodeHandler
{
	private final List<String> preloadedPhenotypes;

	private String currentPlant;
	private int counter = 0;

	private PreferenceUtils pref;

	public PhenotypeBarcodeHandler(GerminateScanActivity context, List<String> preloadedPhenotypes, String currentPlant, int counter)
	{
		super(context);
		this.preloadedPhenotypes = preloadedPhenotypes;
		this.currentPlant = currentPlant;
		this.counter = counter;

		pref = new PreferenceUtils(context);
		pref.putListObject(PreferenceUtils.PREFS_PRELOADED_PHENOTYPES, preloadedPhenotypes);
	}

	@Override
	public List<Barcode> handle(String input, Location location, int index)
	{
		if (input.equals(pref.getString(PreferenceUtils.PREFS_DELETE_ROW_TOKEN)))
		{
			deleteRowLocal();
		}
		else if (input.equals(pref.getString(PreferenceUtils.PREFS_DELETE_CELL_TOKEN)))
		{
			deleteRowLocal();
		}
		/* Validate the input */
		else
		{
			List<Barcode> result = new ArrayList<>();

			if (index == 1) // 1 = last in row
			{
				Barcode value = new Barcode(input);
				value.setTimestamp(Long.toString(System.currentTimeMillis()));
				value.setLocation(location);

				result.add(value);

				if (counter < preloadedPhenotypes.size())
				{
					counter %= preloadedPhenotypes.size();

					Barcode plant = new Barcode(currentPlant);
					plant.setTimestamp(Long.toString(System.currentTimeMillis()));
					plant.setLocation(location);

					result.add(plant);

					Barcode phenotype = new Barcode(preloadedPhenotypes.get(counter++));
					phenotype.setTimestamp(Long.toString(System.currentTimeMillis()));
					phenotype.setLocation(location);

					result.add(phenotype);
				}
				else
				{
					counter %= preloadedPhenotypes.size();
					onPlantComplete();
				}
			}

			if (index == 2) // 2 = first in row...
			{
				counter %= preloadedPhenotypes.size();

				currentPlant = input;
				Barcode plant = new Barcode(currentPlant);
				plant.setTimestamp(Long.toString(System.currentTimeMillis()));
				plant.setLocation(location);

				result.add(plant);

				Barcode phenotype = new Barcode(preloadedPhenotypes.get(counter++));
				phenotype.setTimestamp(Long.toString(System.currentTimeMillis()));
				phenotype.setLocation(location);

				result.add(phenotype);
			}

			pref.putInt(PreferenceUtils.PREFS_PRELOADED_PHENOTYPES_COUNTER, counter);

			return result;
		}

		return null;
	}

	private void deleteRowLocal()
	{
		deleteRow();
		deleteBarcode();

		Barcode barcode = getCurrentPlant();

		if (barcode != null && !barcode.getBarcode().equals(currentPlant))
		{
			currentPlant = barcode.getBarcode();
		}

		counter -= 2;
		counter = MathUtils.modulo(counter, preloadedPhenotypes.size()) + 1;

		pref.putInt(PreferenceUtils.PREFS_PRELOADED_PHENOTYPES_COUNTER, counter);
	}

	protected abstract Barcode getCurrentPlant();

	protected abstract void onPlantComplete();
}