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

/**
 * {@link uk.ac.hutton.android.germinatescan.util.MathUtils} includes common mathematical operations
 *
 * @author Sebastian Raubach
 */
public class MathUtils
{
	/**
	 * Calculates the (non-negative) modulo of the dividend and the divisor
	 *
	 * @param dividend The dividend of the modulo operation (a in "a % x")
	 * @param divisor  The divisor of the modulo operation (x in "a % x")
	 * @return The remainder of the modulo operation
	 */
	public static int modulo(int dividend, int divisor)
	{
		int i = dividend % divisor;

		if (i < 0) i += divisor;

		return i;
	}

	/**
	 * Calculates the (non-negative) modulo of the dividend and the divisor
	 *
	 * @param dividend The dividend of the modulo operation (a in "a % x")
	 * @param divisor  The divisor of the modulo operation (x in "a % x")
	 * @return The remainder of the modulo operation
	 */
	public static long modulo(long dividend, long divisor)
	{
		long i = dividend % divisor;

		if (i < 0) i += divisor;

		return i;
	}
}
