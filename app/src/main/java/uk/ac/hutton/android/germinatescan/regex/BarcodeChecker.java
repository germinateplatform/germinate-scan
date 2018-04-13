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

package uk.ac.hutton.android.germinatescan.regex;

/**
 * {@link BarcodeChecker} is used to check barcodes against pre-defined formats <p> You can find some examples below: <ul>
 * <li>"[a-zA-Z]{4}\\s[\\d]{3}" (four letters, then a space and then three numbers)</li> <li>"[A-Z]{1}[\\d]{2}-[\\d]{4}" (a capital letter, then two
 * numbers, a dash and four numbers</li> </ul> </p>
 *
 * @author Sebastian Raubach
 */
public class BarcodeChecker
{
	/**
	 * Regex accepting barcodes in the form: <code>"[a-zA-Z]{4}\s[\d]{3}"</code>
	 */
	public static final String FIRST_CODE        = "[a-zA-Z]{4}\\s[\\d]{3}";
	/**
	 * Regex accepting barcodes in the form: <code>"[A-Z]{1}[\d]{2}-[\d]{4}"</code>
	 */
	public static final String SECOND_CODE       = "[A-Z]{1}[\\d]{2}-[\\d]{4}";
	/**
	 * Regex accepting every barcode
	 */
	public static final String ACCEPT_EVERYTHING = ".*";

	/**
	 * Checks the given input against the regex
	 *
	 * @param input The input to check
	 * @param regex The regex to check against
	 * @return <code>true</code> if <code>input.matches(regex)</code> is <code>true</code>
	 */
	public static boolean check(String input, String regex)
	{
		return input.matches(regex);
	}
}
