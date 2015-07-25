/**
 * Copyright (c) 2015 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.util;

import android.os.Parcel;
import android.os.Parcelable;
import ax.ha.it.smsalarm.R.string;

/**
 * A small container which holds two separate copies of a {@link String}. They will both be the same upon instantiation of an object of this class,
 * but one copy of the string this object is instantiated with will always stay the same during this objects lifetime. However the "current" value can
 * be changed during it's lifetime.
 *
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class InitializableString implements Parcelable {
	private String initialValue;
	private String value;

	/**
	 * Creates a new instance of {@link InitializableString}. The {@link string} this object is instantiated with will always be stored within this
	 * object as an <b><i>initial value</i></b> and cannot be changed during it's lifetime.
	 * 
	 * @param initialValue
	 *            String to create this object with, cannot be <code>null</code>.
	 */
	public InitializableString(String initialValue) {
		if (initialValue == null) {
			throw new IllegalArgumentException("This object cannot be instantiated with a null value");
		}

		this.initialValue = initialValue;
		value = initialValue;
	}

	/**
	 * Creates a new instance of {@link InitializableString}.
	 * 
	 * @param in
	 *            {@link Parcelable} containing all information needed to create a new instance of <code>InitializableString</code>.
	 */
	public InitializableString(Parcel in) {
		initialValue = in.readString();
		value = in.readString();
	}

	public static final Parcelable.Creator<InitializableString> CREATOR = new Parcelable.Creator<InitializableString>() {
		@Override
		public InitializableString createFromParcel(Parcel source) {
			return new InitializableString(source);
		}

		@Override
		public InitializableString[] newArray(int size) {
			return new InitializableString[size];
		}
	};

	/**
	 * To get the "untouched" {@link String} that this object was created with.
	 * 
	 * @return Initial String of this object.
	 */
	public String getInitialValue() {
		return initialValue;
	}

	/**
	 * To get the "current" {@link String} of this object.
	 * 
	 * @return Current String of this object.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * To set the "current" {@link String} of this object.
	 * 
	 * @param value
	 *            New value of this object.
	 */
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(initialValue);
		dest.writeString(value);
	}

	@Override
	public String toString() {
		return InitializableString.class.getSimpleName() + " [initialValue: \"" + initialValue + "\", value: \"" + value + "\"]";
	}
}
