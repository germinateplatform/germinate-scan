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

package uk.ac.hutton.android.germinatescan.view;

import android.content.*;
import android.content.res.*;
import android.os.*;
import android.preference.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import uk.ac.hutton.android.germinatescan.*;

/**
 * A {@link DialogPreference} that provides a user with the means to select an integer from a {@link NumberPicker}, and persist it.
 *
 * @author lukehorvat
 */
public class NumberPickerDialogPreference extends DialogPreference
{
	private static final int DEFAULT_MIN_VALUE = 0;
	private static final int DEFAULT_MAX_VALUE = 100;
	private static final int DEFAULT_VALUE     = 0;

	private int          mMinValue;
	private int          mMaxValue;
	private int          mValue;
	private NumberPicker mNumberPicker;

	public NumberPickerDialogPreference(Context context)
	{
		this(context, null);
	}

	public NumberPickerDialogPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		// get attributes specified in XML
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.NumberPickerDialogPreference, 0, 0);
		try
		{
			setMinValue(a.getInteger(R.styleable.NumberPickerDialogPreference_min, DEFAULT_MIN_VALUE));
			setMaxValue(a.getInteger(R.styleable.NumberPickerDialogPreference_android_max, DEFAULT_MAX_VALUE));
		}
		finally
		{
			a.recycle();
		}

		// set layout
		setDialogLayoutResource(R.layout.preference_number_picker_dialog);
		setPositiveButtonText(android.R.string.ok);
		setNegativeButtonText(android.R.string.cancel);
		setDialogIcon(null);
	}

	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue)
	{
		setValue(restore ? getPersistedInt(DEFAULT_VALUE) : (Integer) defaultValue);
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		return a.getInt(index, DEFAULT_VALUE);
	}

	@Override
	protected void onBindDialogView(View view)
	{
		super.onBindDialogView(view);

		TextView dialogMessageText = (TextView) view.findViewById(R.id.text_dialog_message);
		dialogMessageText.setText(getDialogMessage());

		mNumberPicker = (NumberPicker) view.findViewById(R.id.number_picker);
		mNumberPicker.setMinValue(mMinValue);
		mNumberPicker.setMaxValue(mMaxValue);
		mNumberPicker.setValue(mValue);
	}

	public int getMinValue()
	{
		return mMinValue;
	}

	public void setMinValue(int minValue)
	{
		mMinValue = minValue;
		setValue(Math.max(mValue, mMinValue));
	}

	public int getMaxValue()
	{
		return mMaxValue;
	}

	public void setMaxValue(int maxValue)
	{
		mMaxValue = maxValue;
		setValue(Math.min(mValue, mMaxValue));
	}

	public int getValue()
	{
		return mValue;
	}

	public void setValue(int value)
	{
		value = Math.max(Math.min(value, mMaxValue), mMinValue);

		if (value != mValue)
		{
			mValue = value;
			persistInt(value);
			notifyChanged();
		}
	}

	@Override
	protected void onDialogClosed(boolean positiveResult)
	{
		super.onDialogClosed(positiveResult);

		// when the user selects "OK", persist the new value
		if (positiveResult)
		{
			int numberPickerValue = mNumberPicker.getValue();
			if (callChangeListener(numberPickerValue))
			{
				setValue(numberPickerValue);
			}
		}
	}

	@Override
	protected Parcelable onSaveInstanceState()
	{
		// save the instance state so that it will survive screen orientation
		// changes and other events that may temporarily destroy it
		final Parcelable superState = super.onSaveInstanceState();

		// set the state's value with the class member that holds current
		// setting value
		final SavedState myState = new SavedState(superState);
		myState.minValue = getMinValue();
		myState.maxValue = getMaxValue();
		myState.value = getValue();

		return myState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state)
	{
		// check whether we saved the state in onSaveInstanceState()
		if (state == null || !state.getClass().equals(SavedState.class))
		{
			// didn't save the state, so call superclass
			super.onRestoreInstanceState(state);
			return;
		}

		// restore the state
		SavedState myState = (SavedState) state;
		setMinValue(myState.minValue);
		setMaxValue(myState.maxValue);
		setValue(myState.value);

		super.onRestoreInstanceState(myState.getSuperState());
	}

	private static class SavedState extends BaseSavedState
	{
		@SuppressWarnings("unused")
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>()
		{
			@Override
			public SavedState createFromParcel(Parcel in)
			{
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(int size)
			{
				return new SavedState[size];
			}
		};
		int minValue;
		int maxValue;
		int value;

		public SavedState(Parcelable superState)
		{
			super(superState);
		}

		public SavedState(Parcel source)
		{
			super(source);

			minValue = source.readInt();
			maxValue = source.readInt();
			value = source.readInt();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags)
		{
			super.writeToParcel(dest, flags);

			dest.writeInt(minValue);
			dest.writeInt(maxValue);
			dest.writeInt(value);
		}
	}
}