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
import ax.ha.it.smsalarm.handler.LogHandler.LogPriorities;

/**
 * Class responsible for all {@link SharedPreferences} handling.<br>
 * <b><i>PreferencesHandler is a singleton.</i></b>
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.0
 */
public class PreferencesHandler {
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
	 * The different valid {@link SharedPreferences} keys.
	 * 
	 * @author Robert Nyholm <robert.nyholm@aland.net>
	 * @version 2.3.1
	 * @since 2.1
	 */
	public enum PrefKey {
		// @formatter:off
		SHARED_PREF("smsAlarmPrefs"), 
		NOT_KEY("notificationPref"), 
		PRIMARY_LISTEN_NUMBER_KEY("primaryListenNumberKey"), 
		PRIMARY_LISTEN_NUMBERS_KEY("primaryListenNumbersKey"), 
		SECONDARY_LISTEN_NUMBERS_KEY("secondaryListenNumbersKey"), 
		PRIMARY_LISTEN_FREE_TEXTS_KEY("primaryListenFreeTextsKey"), 
		SECONDARY_LISTEN_FREE_TEXTS_KEY("secondaryListenFreeTextsKey"), 
		PRIMARY_MESSAGE_TONE_KEY("primaryMessageToneKey"), 
		SECONDARY_MESSAGE_TONE_KEY("secondaryMessageToneKey"), 
		MESSAGE_KEY("messageKey"), 
		FULL_MESSAGE_KEY("fullMessageKey"), 
		ENABLE_ACK_KEY("enableAckKey"), 
		ACK_NUMBER_KEY("ackNumber"), 
		USE_OS_SOUND_SETTINGS_KEY("useOsSoundSettings"), 
		LARM_TYPE_KEY("larmType"), 
		PLAY_TONE_TWICE_KEY("playToneTwice"), 
		ENABLE_SMS_ALARM_KEY("enableSmsAlarm"), 
		RESCUE_SERVICE_KEY("rescueService"), 
		HAS_CALLED_KEY("hasCalled"), 
		END_USER_LICENSE_AGREED("userLicenseAgreed"), 
		VERSION_CODE("versionCode");
		// @formatter:on

		// The actual key to which data will be stored and fetched from
		private final String key;

		/**
		 * Creates a new {@link PrefKey}.
		 * 
		 * @param key
		 *            Value(key) to associate with the <code>PrefKey</code> enumeration. This key is the actual key that data will be stored to or
		 *            fetched from
		 */
		private PrefKey(String key) {
			this.key = key;
		}

		/**
		 * To get the actual <code>Key</code> to which data is stored to and fetched from in the {@link SharedPreferences}.
		 * 
		 * @return the <code>Key</code>
		 */
		public String getKey() {
			return key;
		}
	}

	// Singleton instance of this class
	private static PreferencesHandler INSTANCE;

	// Log tag
	private final String LOG_TAG = getClass().getSimpleName();

	// Variable used to log messages
	private final LogHandler logger;

	// Variables needed for retrieving shared preferences
	private SharedPreferences sharedPref;
	private Editor prefsEditor;

	/**
	 * Private constructor, is private due to it's singleton pattern.
	 */
	private PreferencesHandler() {
		logger = LogHandler.getInstance();
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":PreferencesHandler()", "New instance of PreferencesHandler created");
	}

	/**
	 * To get the singleton instance of this class.
	 * 
	 * @return Singleton instance of PreferencesHandler
	 */
	public static PreferencesHandler getInstance() {
		// If instance of this object is null create a new one
		if (INSTANCE == null) {
			INSTANCE = new PreferencesHandler();
		}

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
	 *            <code>Shared Preferences</code> from which the values are fetched from
	 * @param sharedPreferencesKey
	 *            <code>Key</code> in which <code>Shared Preferences</code> is stored
	 * @param type
	 *            Which type of data that's supposed to be fetched
	 * @param context
	 *            Context in which <code>Shared Preferences</code> handling is done
	 * @return Returns an object with the fetched value. This object can be an instance of <b><i>Integer</i></b>, <b><i>String</i></b>,
	 *         <b><i>Boolean</i></b> or a <b><i>List of Strings</i></b>
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
	 *            <code>Shared Preferences</code> from which the values are fetched from
	 * @param sharedPreferencesKey
	 *            <code>Key</code> in which <code>Shared Preferences</code> is stored
	 * @param type
	 *            Which type of data that's supposed to be fetched
	 * @param context
	 *            Context in which <code>Shared Preferences</code> handling is done
	 * @param defaultObject
	 *            Default value to be fetched from <code>Shared Preferences</code> if no previous value exist
	 * @return Returns an object with the fetched value. This object can be an instance of <b><i>Integer</i></b>, <b><i>String</i></b>,
	 *         <b><i>Boolean</i></b> or a <b><i>List of Strings</i></b>
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
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":fetchPrefs()", "Integer with value \"" + Integer.toString(sharedPref.getInt(sharedPreferencesKey.getKey(), (Integer) defaultObject)) + "\" retrieved from shared preferences: \"" + sharedPreferences.getKey() + ", with key: " + sharedPreferencesKey.getKey() + "\", type: \"" + type.name() + "\", context: \"" + context.toString() + "\" and default value: \"" + Integer.toString((Integer) defaultObject) + "\"");
					return sharedPref.getInt(sharedPreferencesKey.getKey(), (Integer) defaultObject);
				} else {
					logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":fetchPrefs()", "Default value couldn't be set because of instance mismatch, hardcoded default value of 0 is used. However Integer with value \"" + Integer.toString(sharedPref.getInt(sharedPreferencesKey.getKey(), 0)) + "\" retrieved from shared preferences: \"" + sharedPreferences.getKey() + "\", with key: \"" + sharedPreferencesKey.getKey() + "\", type: \"" + type.name() + "\", context: \"" + context.toString()
							+ "\" and default value: \"" + Integer.toString((Integer) defaultObject) + "\"");
					return sharedPref.getInt(sharedPreferencesKey.getKey(), 0);
				}
			case STRING:
				// Check that defaultObject is of correct instance else collect "hard coded" default value of ""
				if (defaultObject instanceof String) {
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":fetchPrefs()", "String with value \"" + sharedPref.getString(sharedPreferencesKey.getKey(), (String) defaultObject) + "\" retrieved from shared preferences: \"" + sharedPreferences.getKey() + "\", with key: \"" + sharedPreferencesKey.getKey() + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\" and default value: \"" + (String) defaultObject + "\"");
					return sharedPref.getString(sharedPreferencesKey.getKey(), (String) defaultObject);
				} else {
					logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":fetchPrefs()", "Default value couldn't be set because of instance mismatch, hardcoded default value of \"\" is used. However String with value\"" + sharedPref.getString(sharedPreferencesKey.getKey(), "") + "\" retrieved from shared preferences: \"" + sharedPreferences.getKey() + "\", with key: \"" + sharedPreferencesKey.getKey() + "\", type: \"" + type.name() + "\" and context: " + context.toString() + "\"");
					return sharedPref.getString(sharedPreferencesKey.getKey(), "");
				}
			case BOOLEAN:
				// Check that defaultObject is of correct instance else collect "hard coded" default value of false
				if (defaultObject instanceof Boolean) {
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":fetchPrefs()", "Boolean with value \"" + sharedPref.getBoolean(sharedPreferencesKey.getKey(), (Boolean) defaultObject) + "\" retrieved from shared preferences: \"" + sharedPreferences.getKey() + "\", with key: \"" + sharedPreferencesKey.getKey() + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\" and default value: \"" + defaultObject + "\"");
					return sharedPref.getBoolean(sharedPreferencesKey.getKey(), (Boolean) defaultObject);
				} else {
					logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":fetchPrefs()", "Default value couldn't be set because of instance mismatch, hardcoded default value of false is used. However Boolean with value \"" + sharedPref.getBoolean(sharedPreferencesKey.getKey(), false) + "\" retrieved from shared preferences: \"" + sharedPreferences.getKey() + "\", with key: \"" + sharedPreferencesKey.getKey() + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\"");
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

						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":fetchPrefs()", "List<String> with value(s) \"" + json + "\"  retrieved from shared preferences: \"" + sharedPreferences.getKey() + "\", with key: \"" + sharedPreferencesKey.getKey() + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\"");
						return list;
					} catch (JSONException e) {
						logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":fetchPrefs()", "Failed to retrieve List<String> from shared preferences: \"" + sharedPreferences.getKey() + "\", with key: \"" + sharedPreferencesKey.getKey() + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\"", e);
					}
				} else { // <--If JSON string is empty, return empty List
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":fetchPrefs()", "List<String> with value(s) \"" + json + "\"  retrieved from shared preferences: \"" + sharedPreferences.getKey() + "\", with key: \"" + sharedPreferencesKey.getKey() + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\"");
					return list;
				}
				break;
			default:
				// DO NOTHING, EXCEPTION WILL BE THROWN LATER!
		}

		// If application end up here then some error has occurred
		IllegalArgumentException exception = new IllegalArgumentException("Failed to fetch shared preferences: \"" + sharedPreferences.getKey() + "\", with key: \"" + sharedPreferencesKey.getKey() + "\", data type: \"" + type.name() + "\" and context: \"" + context.toString() + "\". Cause: \"Data type given as argument is unsupported\", valid data types are: \"INTEGER\", \"STRING\", \"BOOLEAN\" and \"LIST\"");
		logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":fetchPrefs()", "An exception occurred while fetching shared preferences", exception);
		throw exception;
	}

	/**
	 * To store values to {@link SharedPreferences}. It set's different values of different instances depending on input parameters.<br>
	 * Example usage:
	 * <p>
	 * <code>storePrefs(PrefKey.SHARED_PREF, PrefKey.ACKNUMBER_KEY, "0457 0000 000", context)</code>
	 * 
	 * @param sharedPreference
	 *            <code>Shared Preferences</code> to which given object is stored to
	 * @param sharedPreferencesKey
	 *            <code>Key</code> to which <code>Shared Preference</code> the object is going to be stored to
	 * @param object
	 *            Object to be stored to <code>Shared Preference</code>, supported instances are <code>Integer</code>, <code>String</code>,
	 *            <code>Boolean</code> and <code>List(containing instances of String)</code>
	 * @param context
	 *            Context in which <code>Shared Preferences</code> handling is done
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
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":storePrefs()", "Instance of Integer stored to shared preferences: \"" + sharedPreference.getKey() + "\", with key: \"" + sharedPreferencesKey.getKey() + "\", value: \"" + Integer.toString((Integer) object) + "\" and context: \"" + context.toString() + "\"");
		} else if (object instanceof String) {
			// Put shared preferences as String
			prefsEditor.putString(sharedPreferencesKey.getKey(), (String) object);
			prefsEditor.commit();
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":storePrefs()", "Instance of String stored to shared preferences: \"" + sharedPreference.getKey() + "\", with key: \"" + sharedPreferencesKey.getKey() + "\", value: \"" + object.toString() + "\" and context: \"" + context.toString() + "\"");
		} else if (object instanceof Boolean) {
			// Put shared preferences as Boolean
			prefsEditor.putBoolean(sharedPreferencesKey.getKey(), (Boolean) object);
			prefsEditor.commit();
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":storePrefs()", "Instance of Boolean stored to shared preferences: \"" + sharedPreference.getKey() + "\", with key: \"" + sharedPreferencesKey.getKey() + "\", value: \"" + object + "\" and context: \"" + context.toString() + "\"");
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
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":storePrefs()", "Instance of List<String> stored as String to shared preferences: \"" + sharedPreference.getKey() + "\", with key: \"" + sharedPreferencesKey.getKey() + "\", value: \"" + a.toString() + "\" and context: \"" + context.toString() + "\"");
			} else {
				prefsEditor.putString(sharedPreferencesKey.getKey(), "");
				prefsEditor.commit();
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":storePrefs()", "Instance of List<String> stored as String to shared preferences: \"" + sharedPreference.getKey() + "\", with key: \"" + sharedPreferencesKey.getKey() + "\", value: \"" + "" + "\" and context: \"" + context.toString() + "\"");
			}
		} else {
			// If application end up here then some error has occurred
			IllegalArgumentException exception = new IllegalArgumentException("Failed to store object to shared preferences: \"" + sharedPreference.getKey() + "\", with key: \"" + sharedPreferencesKey.getKey() + "\" and context: \"" + context.toString() + "\". Cause: \"Object of unsupported instance was given as argument\", given object is instance of: \"" + object.getClass().getSimpleName() + "\", valid instances are: \"int\", \"String\", \"boolean\" and \"List<String>\"");
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":storePrefs()", "An exception occurred while setting shared preferences", exception);
			throw exception;
		}
	}
}
