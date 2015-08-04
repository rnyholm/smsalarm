/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.handler;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 * Class responsible for all {@link SharedPreferences} handling.<br>
 * <b><i>PreferencesHandler is a singleton, eagerly initialized to avoid concurrent modification.</i></b>
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.0
 */
public class SharedPreferencesHandler {
	/**
	 * The different valid data types, for storing and fetching {@link SharedPreferences}.
	 * 
	 * @author Robert Nyholm <robert.nyholm@aland.net>
	 * @version 2.3.1
	 * @since 2.1
	 */
	public enum DataType {
		INTEGER, STRING, BOOLEAN, LIST;
	}

	/**
	 * The different valid {@link SharedPreferences}.
	 * 
	 * @author Robert Nyholm <robert.nyholm@aland.net>
	 * @version 2.3.1
	 * @since 2.1
	 */
	public enum PrefKey {
		// @formatter:off
		SHARED_PREF("smsAlarmPrefs"),  
		PRIMARY_LISTEN_NUMBER_KEY("primaryListenNumberKey"), 			// Not used after version code 8
		PRIMARY_LISTEN_NUMBERS_KEY("primaryListenNumbersKey"), 
		SECONDARY_LISTEN_NUMBERS_KEY("secondaryListenNumbersKey"), 
		PRIMARY_LISTEN_FREE_TEXTS_KEY("primaryListenFreeTextsKey"), 
		SECONDARY_LISTEN_FREE_TEXTS_KEY("secondaryListenFreeTextsKey"), 
		PRIMARY_MESSAGE_TONE_KEY("primaryMessageToneKey"), 				// Not used after version code 13
		SECONDARY_MESSAGE_TONE_KEY("secondaryMessageToneKey"), 			// Not used after version code 13
		ENABLE_ACK_KEY("enableAckKey"), 
		ACK_NUMBER_KEY("ackNumber"), 
		ACK_MESSAGE_KEY("ackMessageKey"),
		ACK_METHOD_KEY("ackMethodKey"),
		USE_OS_SOUND_SETTINGS_KEY("useOsSoundSettings"), 
		PLAY_TONE_TWICE_KEY("playToneTwice"),							// Not used after version code 13
		PLAY_ALARM_SIGNAL_TWICE_KEY("playAlarmSignalTwice"),
		PLAY_ALARM_SIGNAL_REPEATEDLY_KEY("playAlarmSignalRepeatedly"),
		ENABLE_SMS_ALARM_KEY("enableSmsAlarm"), 
		RESCUE_SERVICE_KEY("rescueService"), 							// Not used after version code 19
		ORGANIZATION_KEY("organization"),
		HAS_CALLED_KEY("hasCalled"), 
		END_USER_LICENSE_AGREED("userLicenseAgreed"), 
		VERSION_CODE("versionCode"),
		USE_FLASH_NOTIFICATION("useFlashNotification"),
		PRIMARY_ALARM_SIGNAL_KEY("primaryAlarmSignalKey"),
		SECONDARY_ALARM_SIGNAL_KEY("secondaryAlarmSignalKey"),
		USER_ADDED_ALARM_SIGNALS_KEY("userAddedAlarmSignalsKey"),
		PRIMARY_ALARM_VIBRATION_KEY("primaryAlarmVibrationKey"),
		SECONDARY_ALARM_VIBRATION_KEY("secondaryAlarmVibrationKey"),
		ENABLE_SMS_DEBUG_LOGGING("enableSmsDebugLogging");
		// @formatter:on

		// The actual key to which data will be stored and fetched from
		private final String key;

		/**
		 * Creates a new {@link PrefKey}.
		 * 
		 * @param key
		 *            Value(key) to associate with the PrefKey enumeration. This key is the actual key that data will be stored to or fetched from
		 *            {@link SharedPreferences} with.
		 */
		private PrefKey(String key) {
			this.key = key;
		}

		/**
		 * To get the actual <code>Key</code> with which data is stored to and fetched from {@link SharedPreferences} with.
		 * 
		 * @return The <code>Key</code>.
		 */
		public String getKey() {
			return key;
		}
	}

	// Singleton instance of this class, eagerly initialized
	private static SharedPreferencesHandler INSTANCE = new SharedPreferencesHandler();

	private static final String LOG_TAG = SharedPreferencesHandler.class.getSimpleName();

	// Variables needed for retrieving shared preferences
	private SharedPreferences sharedPref;
	private Editor prefsEditor;

	/**
	 * Creates a new instance of {@link SharedPreferencesHandler}.
	 */
	private SharedPreferencesHandler() {
		if (INSTANCE != null) {
			Log.e(LOG_TAG + ":SharedPreferencesHandler()", "SharedPreferencesHandler already instantiated");
		}
	}

	/**
	 * To get the <b><i>singleton</i></b> instance of {@link SharedPreferencesHandler}.
	 * 
	 * @return Instance of <code>PreferencesHandler</code>.
	 */
	public static SharedPreferencesHandler getInstance() {
		return INSTANCE;
	}

	/**
	 * To fetch values in {@link SharedPreferences}. It fetches different values depending on input parameters.<br>
	 * Returns fetched value if all is fine else an {@link IllegalArgumentException} is thrown.
	 * <p>
	 * Example usage:<br>
	 * <code>String s = fetchPrefs(PrefKey.SHARED_PREF, PrefKey.ACK_NUMBER_KEY, 
	 * DataType.STRING, context)</code>, this will retrieve a <code>String</code> from key <code>ACK_NUMBER_KEY</code> within given
	 * <code>context</code>.
	 * 
	 * @param sharedPreferences
	 *            <code>Shared Preferences</code> from which the values are fetched from.
	 * @param sharedPreferencesKey
	 *            <code>Key</code> in which <code>Shared Preferences</code> is stored.
	 * @param type
	 *            Which type of data that's supposed to be fetched.
	 * @param context
	 *            Context in which <code>Shared Preferences</code> handling is done.
	 * @return Returns an object with the fetched value. This object can be an instance of <b><i>Integer</i></b>, <b><i>String</i></b>,
	 *         <b><i>Boolean</i></b> or a <b><i>List of Strings</i></b>.
	 * @see #fetchPrefs(PrefKey, PrefKey, DataType, Context, Object)
	 * @see #storePrefs(PrefKey, PrefKey, Object, Context)
	 */
	public Object fetchPrefs(PrefKey sharedPreferences, PrefKey sharedPreferencesKey, DataType type, Context context) {
		switch (type) {
			case INTEGER:
				return fetchPrefs(sharedPreferences, sharedPreferencesKey, type, context, 0);
			case STRING:
				return fetchPrefs(sharedPreferences, sharedPreferencesKey, type, context, "");
			case BOOLEAN:
				return fetchPrefs(sharedPreferences, sharedPreferencesKey, type, context, false);
			case LIST:
				// The default value when fetching a List will not be taken into consideration
				return fetchPrefs(sharedPreferences, sharedPreferencesKey, type, context, "");
			default:
				// Throw in some crap here, an IllegalArgumentException will be thrown anyways
				return fetchPrefs(sharedPreferences, sharedPreferencesKey, type, context, "");
		}
	}

	/**
	 * To fetch values in {@link SharedPreferences}. It fetches different values depending on input parameters. This method also takes a default value
	 * to be fetched from <code>Shared Preferences</code> if no value exist. <br>
	 * Returns fetched value if all is fine else an {@link IllegalArgumentException} is thrown.
	 * <p>
	 * Example usage:<br>
	 * <code>String s = fetchPrefs(PrefKey.SHARED_PREF, PrefKey.ACK_NUMBER_KEY, 
	 * DataType.STRING, context, "empty")</code>, this will retrieve a <code>String</code> from key <code>ACK_NUMBER_KEY</code> within given
	 * <code>context</code>.
	 * <p>
	 * <b><i>NOTE. The default value does only work for data types INTEGER, STRING and BOOLEAN. A default value for data type LIST will not be taken
	 * in consideration</i></b>. <br>
	 * 
	 * @param sharedPreferences
	 *            <code>Shared Preferences</code> from which the values are fetched from.
	 * @param sharedPreferencesKey
	 *            <code>Key</code> in which <code>Shared Preferences</code> is stored.
	 * @param type
	 *            Which type of data that's supposed to be fetched.
	 * @param context
	 *            Context in which <code>Shared Preferences</code> handling is done.
	 * @param defaultObject
	 *            Default value to be fetched from <code>Shared Preferences</code> if no previous value exist.
	 * @return Returns an object with the fetched value. This object can be an instance of <b><i>Integer</i></b>, <b><i>String</i></b>,
	 *         <b><i>Boolean</i></b> or a <b><i>List of Strings</i></b>.
	 * @see #fetchPrefs(PrefKey, PrefKey, DataType, Context)
	 * @see #storePrefs(PrefKey, PrefKey, Object, Context)
	 */
	public Object fetchPrefs(PrefKey sharedPreferences, PrefKey sharedPreferencesKey, DataType type, Context context, Object defaultObject) {
		// Set shared preferences from context
		sharedPref = context.getSharedPreferences(sharedPreferences.getKey(), Context.MODE_PRIVATE);

		switch (type) {
			case INTEGER:
				// Check that defaultObject is of correct instance else collect "hard coded" default value of 0
				if (defaultObject instanceof Integer) {
					return sharedPref.getInt(sharedPreferencesKey.getKey(), (Integer) defaultObject);
				} else {
					return sharedPref.getInt(sharedPreferencesKey.getKey(), 0);
				}
			case STRING:
				// Check that defaultObject is of correct instance else collect "hard coded" default value of ""
				if (defaultObject instanceof String) {
					return sharedPref.getString(sharedPreferencesKey.getKey(), (String) defaultObject);
				} else {
					return sharedPref.getString(sharedPreferencesKey.getKey(), "");
				}
			case BOOLEAN:
				// Check that defaultObject is of correct instance else collect "hard coded" default value of false
				if (defaultObject instanceof Boolean) {
					return sharedPref.getBoolean(sharedPreferencesKey.getKey(), (Boolean) defaultObject);
				} else {
					return sharedPref.getBoolean(sharedPreferencesKey.getKey(), false);
				}
			case LIST:
				// Retrieve JSON string
				String json = sharedPref.getString(sharedPreferencesKey.getKey(), "");
				// List of Strings containing
				List<String> list = new ArrayList<String>();

				// If JSON string is not empty
				if (json != "") {
					try {
						// Create a JSONArray from JSON string and retrieve strings from it and and them to a List<String>
						JSONArray a = new JSONArray(json);

						for (int i = 0; i < a.length(); i++) {
							String secondaryListenNumber = a.optString(i);
							list.add(secondaryListenNumber);
						}

						return list;
					} catch (JSONException e) {
						Log.e(LOG_TAG + ":fetchPrefs()", "Failed to retrieve List<String> from shared preferences: \"" + sharedPreferences.getKey() + "\", with key: \"" + sharedPreferencesKey.getKey() + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\"", e);
					}
				} else { // <--If JSON string is empty, return empty List
					return list;
				}
				break;
			default:
				// DO NOTHING, EXCEPTION WILL BE THROWN LATER!
		}

		// If application end up here then some error has occurred
		IllegalArgumentException exception = new IllegalArgumentException("Failed to fetch shared preferences: \"" + sharedPreferences.getKey() + "\", with key: \"" + sharedPreferencesKey.getKey() + "\", data type: \"" + type.name() + "\" and context: \"" + context.toString() + "\". Cause: \"Data type given as argument is unsupported\", valid data types are: \"INTEGER\", \"STRING\", \"BOOLEAN\" and \"LIST\"");
		Log.e(LOG_TAG + ":fetchPrefs()", "An exception occurred while fetching shared preferences", exception);

		throw exception;
	}

	/**
	 * To store values to {@link SharedPreferences}. It set's different values of different instances depending on input parameters.<br>
	 * Example usage:
	 * <p>
	 * <code>storePrefs(PrefKey.SHARED_PREF, PrefKey.ACKNUMBER_KEY, "0457 0000 000", context)</code>
	 * 
	 * @param sharedPreference
	 *            <code>Shared Preferences</code> to which given object is stored to.
	 * @param sharedPreferencesKey
	 *            <code>Key</code> to which <code>Shared Preference</code> the object is going to be stored to.
	 * @param object
	 *            Object to be stored to <code>Shared Preference</code>, supported instances are <code>Integer</code>, <code>String</code>,
	 *            <code>Boolean</code> and <code>List(containing instances of String)</code>.
	 * @param context
	 *            Context in which <code>Shared Preferences</code> handling is done.
	 * @see #fetchPrefs(PrefKey, PrefKey, DataType, Context)
	 * @see #fetchPrefs(PrefKey, PrefKey, DataType, Context, Object)
	 */
	@SuppressWarnings("unchecked")
	public void storePrefs(PrefKey sharedPreference, PrefKey sharedPreferencesKey, Object object, Context context) {
		// Set shared preferences from context
		sharedPref = context.getSharedPreferences(sharedPreference.getKey(), Context.MODE_PRIVATE);
		prefsEditor = sharedPref.edit();

		if (object instanceof Integer) {
			// Put shared preferences as Integer
			prefsEditor.putInt(sharedPreferencesKey.getKey(), (Integer) object);
			prefsEditor.commit();
		} else if (object instanceof String) {
			// Put shared preferences as String
			prefsEditor.putString(sharedPreferencesKey.getKey(), (String) object);
			prefsEditor.commit();
		} else if (object instanceof Boolean) {
			// Put shared preferences as Boolean
			prefsEditor.putBoolean(sharedPreferencesKey.getKey(), (Boolean) object);
			prefsEditor.commit();
		} else if (object instanceof List<?>) {
			// Create a new list and store object to it
			List<String> list = new ArrayList<String>();
			list = (List<String>) object;

			// Use JSON to serialize ArrayList containing strings
			JSONArray a = new JSONArray();

			// Iterate through each element in list and add object in JSON object
			for (int i = 0; i < list.size(); i++) {
				a.put(list.get(i));
			}

			// If list is not empty add it to shared preferences, if empty add empty string to preferences
			if (!list.isEmpty()) {
				prefsEditor.putString(sharedPreferencesKey.getKey(), a.toString());
				prefsEditor.commit();
			} else {
				prefsEditor.putString(sharedPreferencesKey.getKey(), "");
				prefsEditor.commit();
			}
		} else {
			// If application end up here then some error has occurred
			IllegalArgumentException exception = new IllegalArgumentException("Failed to store object to shared preferences: \"" + sharedPreference.getKey() + "\", with key: \"" + sharedPreferencesKey.getKey() + "\" and context: \"" + context.toString() + "\". Cause: \"Object of unsupported instance was given as argument\", given object is instance of: \"" + object.getClass().getSimpleName() + "\", valid instances are: \"int\", \"String\", \"boolean\" and \"List<String>\"");
			Log.e(LOG_TAG + ":storePrefs()", "An exception occurred while setting shared preferences", exception);

			throw exception;
		}
	}
}
