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
 * This class is responsible for all Shared Preferences handling.<br>
 * <b><i>PreferencesHandler is a singleton.</i></b>
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.2.1
 * @since 2.0
 */
public class PreferencesHandler {
	/**
	 * Enumeration for different datatypes needed when retrieving shared preferences.
	 * 
	 * @author Robert Nyholm <robert.nyholm@aland.net>
	 * @version 2.1
	 * @since 2.1
	 */
	public enum DataTypes {
		INTEGER, STRING, BOOLEAN, LIST;
	}

	/**
	 * Enumeration for the shared preferences keys.
	 * 
	 * @author Robert Nyholm <robert.nyholm@aland.net>
	 * @version 2.2
	 * @since 2.1
	 */
	public enum PrefKeys {
		SHARED_PREF("smsAlarmPrefs"), NOT_KEY("notificationPref"), PRIMARY_LISTEN_NUMBER_KEY("primaryListenNumberKey"), PRIMARY_LISTEN_NUMBERS_KEY("primaryListenNumbersKey"), SECONDARY_LISTEN_NUMBERS_KEY("secondaryListenNumbersKey"), PRIMARY_LISTEN_FREE_TEXTS_KEY("primaryListenFreeTextsKey"), SECONDARY_LISTEN_FREE_TEXTS_KEY("secondaryListenFreeTextsKey"), PRIMARY_MESSAGE_TONE_KEY("primaryMessageToneKey"), SECONDARY_MESSAGE_TONE_KEY("secondaryMessageToneKey"), MESSAGE_KEY("messageKey"), FULL_MESSAGE_KEY(
				"fullMessageKey"), ENABLE_ACK_KEY("enableAckKey"), ACK_NUMBER_KEY("ackNumber"), USE_OS_SOUND_SETTINGS_KEY("useOsSoundSettings"), LARM_TYPE_KEY("larmType"), PLAY_TONE_TWICE_KEY("playToneTwice"), ENABLE_SMS_ALARM_KEY("enableSmsAlarm"), RESCUE_SERVICE_KEY("rescueService"), HAS_CALLED_KEY("hasCalled"), END_USER_LICENSE_AGREED("userLicenseAgreed"), VERSION_CODE("versionCode");

		// Value in which enumerations values are stored
		private final String key;

		/**
		 * Constructor for this enumeration.
		 * 
		 * @param key
		 *            Value to associate with the constructed enumeration as String
		 */
		PrefKeys(String key) {
			this.key = key;
		}

		/**
		 * Overridden <code>toString()</code> to return the value associated with a PrefKeys enumeration.
		 * 
		 * @return Value associated with PrefKey as String
		 */
		@Override
		public String toString() {
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
	 * 
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	private PreferencesHandler() {
		// Get instance of logger
		logger = LogHandler.getInstance();

		// Log information
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":PreferencesHandler()", "New instance of PreferencesHandler created");
	}

	/**
	 * Method to get the singleton instance of this class.
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
	 * Method to get values in <b>Shared Preferences</b>. It retrieves different values depending on input parameters. Returns retrieved value if all
	 * is fine else an IllegalArgumentsException are thrown.<br>
	 * Example usage:<br>
	 * <code>String s = getPrefs(PrefKeys.SHARED_PREF, PrefKeys.ACK_NUMBER_KEY, 
	 * DataTypes.STRING, context)</code>, this will retrieve a String from key <code>ACK_NUMBER_KEY</code> and with the given context.
	 * 
	 * @param sharedPreferences
	 *            SharedPreferences from which the values are retrieved from
	 * @param key
	 *            Key in which Shared Preference is stored
	 * @param type
	 *            Different type of values is retrieved from Shared Preferences, datatype is decided depending on this given DataType
	 * @param context
	 *            Context from which the Shared Preferences should be retrieved
	 * @return Returns an object with the retrieved values. This object can be an instance of Integer, String, Boolean or a List of Strings
	 * @exception IllegalArgumentException
	 *                if an incorrect datatype was given as parameter
	 * @see #setPrefs(PrefKeys, PrefKeys, Object, Context)
	 * @see #getPrefs(PrefKeys, PrefKeys, DataTypes, Context)
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCatTxt(LogPriorities, String, String) logCatTxt(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCatTxt(LogPriorities, String, String, Throwable) logCatTxt(LogPriorities, String, String,
	 *      Throwable)
	 */
	public Object getPrefs(PrefKeys sharedPreferences, PrefKeys key, DataTypes type, Context context) throws IllegalArgumentException {
		// Set shared preferences from context
		sharedPref = context.getSharedPreferences(sharedPreferences.toString(), Context.MODE_PRIVATE);

		switch (type) {
			case INTEGER:
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getPrefs()", "Integer with value \"" + Integer.toString(sharedPref.getInt(key.toString(), 0)) + "\" retrieved from shared preferences: \"" + sharedPreferences.toString() + "\", with key: \"" + key.toString() + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\"");
				return sharedPref.getInt(key.toString(), 0);
			case STRING:
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getPrefs()", "String with value \"" + sharedPref.getString(key.toString(), "") + "\" retrieved from shared preferences: \"" + sharedPreferences.toString() + "\", with key: \"" + key.toString() + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\"");
				return sharedPref.getString(key.toString(), "");
			case BOOLEAN:
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getPrefs()", "Boolean with value \"" + sharedPref.getBoolean(key.toString(), false) + "\" retrieved from shared preferences: \"" + sharedPreferences.toString() + "\", with key: \"" + key.toString() + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\"");
				return sharedPref.getBoolean(key.toString(), false);
			case LIST:
				// Retrieve secondaryListenNumbers to json string and clear secondaryListenNumbers
				// List just to be sure that it's empty
				String json = sharedPref.getString(key.toString(), "");
				// List of Strings containing
				List<String> list = new ArrayList<String>();

				// If json string is not empty
				if (!json.equals("")) {
					try {
						// Create a JSONArray from json string and retrieve strings from it and and
						// them to secondaryListenNumbers List
						JSONArray a = new JSONArray(json);
						for (int i = 0; i < a.length(); i++) {
							String secondaryListenNumber = a.optString(i);
							list.add(secondaryListenNumber);
						}
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getPrefs()", "List<String> with value(s) \"" + json + "\"  retrieved from shared preferences: \"" + sharedPreferences.toString() + "\", with key: \"" + key.toString() + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\"");
						// Return the list
						return list;
					} catch (JSONException e) {
						// Log JSONException
						logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":getPrefs()", "Failed to retrieve List<String> from shared preferences: \"" + sharedPreferences.toString() + "\", with key: \"" + key.toString() + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\"", e);
					}
				} else { // <--If JSON string is empty, return empty List
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getPrefs()", "List<String> with value(s) \"" + json + "\"  retrieved from shared preferences: \"" + sharedPreferences.toString() + "\", with key: \"" + key.toString() + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\"");
					// Return the list
					return list;
				}
				break;
			default:
				// DO NOTHING!
		}

		// We should never reach this far but if we do an error has occurred and an exception is
		// thrown
		throw new IllegalArgumentException("Unsupported data type was givien as parameter. Shared preferences: \"" + sharedPreferences.toString() + "\", with key: \"" + key.toString() + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\". Valid DataTypes are: INTEGER, STRING, BOOLEAN and LIST");
	}

	/**
	 * Method to get values in <b>Shared Preferences</b>. It retrieves different values depending on input parameters. It's also possible to set
	 * default value to retrieve from shared preferences if no value exist, <b><i>NB. ONLY WORKING FOR DATATYPES Integer, String AND Boolean</i></b>
	 * Returns retrieved value if all is fine else an IllegalArgumentsException are thrown.<br>
	 * Example usage:<br>
	 * <code>String s = getPrefs(PrefKeys.SHARED_PREF, PrefKeys.ACK_NUMBER_KEY, 
	 * DataTypes.STRING, context, "empty")</code>, this will retrieve a String from key <code>ACK_NUMBER_KEY</code> and with the given context.
	 * 
	 * @param sharedPreferences
	 *            SharedPreferences from which the values are retrieved from
	 * @param key
	 *            Key in which Shared Preference is stored
	 * @param type
	 *            Different type of values is retrieved from Shared Preferences, datatype is decided depending on this given DataType
	 * @param context
	 *            Context from which the Shared Preferences should be retrieved
	 * @param defaultObject
	 *            Default value to be retrieved from shared preference if no previous value exist
	 * @return Returns an object with the retrieved values. This object can be an instance of Integer, String, Boolean or a List of Strings
	 * @exception IllegalArgumentException
	 *                if an incorrect datatype was given as parameter
	 * @see #setPrefs(PrefKeys, PrefKeys, Object, Context)
	 * @see #getPrefs(PrefKeys, PrefKeys, DataTypes, Context, Object)
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCatTxt(LogPriorities, String, String) logCatTxt(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCatTxt(LogPriorities, String, String, Throwable) logCatTxt(LogPriorities, String, String,
	 *      Throwable)
	 */
	public Object getPrefs(PrefKeys sharedPreferences, PrefKeys key, DataTypes type, Context context, Object defaultObject) throws IllegalArgumentException {
		// Set shared preferences from context
		sharedPref = context.getSharedPreferences(sharedPreferences.toString(), Context.MODE_PRIVATE);

		switch (type) {
			case INTEGER:
				// Check that defaultObject is of correct instance else collect "hardcoded" default
				// value of 0
				if (defaultObject instanceof Integer) {
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getPrefs()", "Integer with value \"" + Integer.toString(sharedPref.getInt(key.toString(), (Integer) defaultObject)) + "\" retrieved from shared preferences: \"" + sharedPreferences.toString() + ", with key: " + key.toString() + "\", type: \"" + type.name() + "\", context: \"" + context.toString() + "\" and default value: \"" + Integer.toString((Integer) defaultObject) + "\"");
					return sharedPref.getInt(key.toString(), (Integer) defaultObject);
				} else {
					logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":getPrefs()",
							"Default value couldn't be set because of instance mismatch, hardcoded default value of 0 is used. However Integer with value \"" + Integer.toString(sharedPref.getInt(key.toString(), 0)) + "\" retrieved from shared preferences: \"" + sharedPreferences.toString() + "\", with key: \"" + key.toString() + "\", type: \"" + type.name() + "\", context: \"" + context.toString() + "\" and default value: \"" + Integer.toString((Integer) defaultObject) + "\"");
					return sharedPref.getInt(key.toString(), 0);
				}
			case STRING:
				// Check that defaultObject is of correct instance else collect "hardcoded" default
				// value of ""
				if (defaultObject instanceof String) {
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getPrefs()", "String with value \"" + sharedPref.getString(key.toString(), (String) defaultObject) + "\" retrieved from shared preferences: \"" + sharedPreferences.toString() + "\", with key: \"" + key.toString() + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\" and default value: \"" + (String) defaultObject + "\"");
					return sharedPref.getString(key.toString(), (String) defaultObject);
				} else {
					logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":getPrefs()", "Default value couldn't be set because of instance mismatch, hardcoded default value of \"\" is used. However String with value\"" + sharedPref.getString(key.toString(), "") + "\" retrieved from shared preferences: \"" + sharedPreferences.toString() + "\", with key: \"" + key.toString() + "\", type: \"" + type.name() + "\" and context: " + context.toString() + "\"");
					return sharedPref.getString(key.toString(), "");
				}
			case BOOLEAN:
				// Check that defaultObject is of correct instance else collect "hardcoded" default
				// value of false
				if (defaultObject instanceof Boolean) {
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getPrefs()", "Boolean with value \"" + sharedPref.getBoolean(key.toString(), (Boolean) defaultObject) + "\" retrieved from shared preferences: \"" + sharedPreferences.toString() + "\", with key: \"" + key.toString() + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\" and default value: \"" + defaultObject + "\"");
					return sharedPref.getBoolean(key.toString(), (Boolean) defaultObject);
				} else {
					logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":getPrefs()", "Default value couldn't be set because of instance mismatch, hardcoded default value of false is used. However Boolean with value \"" + sharedPref.getBoolean(key.toString(), false) + "\" retrieved from shared preferences: \"" + sharedPreferences.toString() + "\", with key: \"" + key.toString() + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\"");
					return sharedPref.getBoolean(key.toString(), false);
				}
			case LIST:
				// Retrieve secondaryListenNumbers to json string and clear secondaryListenNumbers
				// List just to be sure that it's empty
				String json = sharedPref.getString(key.toString(), "");
				// List of Strings containing
				List<String> list = new ArrayList<String>();

				// If json string is not empty
				if (json != "") {
					try {
						// Create a JSONArray from json string and retrieve strings from it and and
						// them to secondaryListenNumbers List
						JSONArray a = new JSONArray(json);
						for (int i = 0; i < a.length(); i++) {
							String secondaryListenNumber = a.optString(i);
							list.add(secondaryListenNumber);
						}
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getPrefs()", "List<String> with value(s) \"" + json + "\"  retrieved from shared preferences: \"" + sharedPreferences.toString() + "\", with key: \"" + key.toString() + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\"");
						// Return the list
						return list;
					} catch (JSONException e) {
						// Log JSONException
						logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":getPrefs()", "Failed to retrieve List<String> from shared preferences: \"" + sharedPreferences.toString() + "\", with key: \"" + key.toString() + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\"", e);
					}
				} else { // <--If JSON string is empty, return empty List
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getPrefs()", "List<String> with value(s) \"" + json + "\"  retrieved from shared preferences: \"" + sharedPreferences.toString() + "\", with key: \"" + key.toString() + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\"");
					// Return the list
					return list;
				}
				break;
			default:
				// DO NOTHING!
		}

		// We should never reach this far but if we do an error has occurred and an exception is
		// thrown
		throw new IllegalArgumentException("Unsupported data type was givien as argument. Shared preferences: \"" + sharedPreferences.toString() + "\", with key: \"" + key.toString() + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\". Valid DataTypes are: INTEGER, STRING, BOOLEAN and LIST");
	}

	/**
	 * Method to set values to <b>Shared Preferences<b>. It sets different values of different instances depending on input parameters.<br>
	 * Example usage:<br>
	 * <code>setPrefs(PrefKeys.SHARED_PREF, PrefKey.ACKNUMBER_KEY, "0457 0000 000", context</code>
	 * 
	 * @param sharedPreferences
	 *            SharedPreferences from which the values are retrieved from
	 * @param key
	 *            Key in which Shared Preference is stored
	 * @param object
	 *            Object to be stored to Shared Preferences, supported instances are Integer, String, Boolean and List<String>
	 * @param context
	 *            Context from which the Shared Preferences should be retrieved
	 * @exception IllegalArgumentException
	 *                if an incorrect datatype was given as parameter
	 * @see #getPrefs(PrefKeys, PrefKeys, DataTypes, Context)
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	@SuppressWarnings("unchecked")
	public void setPrefs(PrefKeys sharedPreferences, PrefKeys key, Object object, Context context) throws IllegalArgumentException {
		// Set shared preferences from context
		sharedPref = context.getSharedPreferences(sharedPreferences.toString(), Context.MODE_PRIVATE);
		prefsEditor = sharedPref.edit();

		if (object instanceof Integer) {
			// Put shared preferences as Integer
			prefsEditor.putInt(key.toString(), (Integer) object);
			prefsEditor.commit();
			// Log information
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setPrefs()", "Instance of Integer stored to shared preferences: \"" + sharedPreferences.toString() + "\", with key: \"" + key.toString() + "\", value: \"" + Integer.toString((Integer) object) + "\" and context: \"" + context.toString() + "\"");
		} else if (object instanceof String) {
			// Put shared preferences as String
			prefsEditor.putString(key.toString(), (String) object);
			prefsEditor.commit();
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setPrefs()", "Instance of String stored to shared preferences: \"" + sharedPreferences.toString() + "\", with key: \"" + key.toString() + "\", value: \"" + object.toString() + "\" and context: \"" + context.toString() + "\"");
		} else if (object instanceof Boolean) {
			// Put shared preferences as Boolean
			prefsEditor.putBoolean(key.toString(), (Boolean) object);
			prefsEditor.commit();
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setPrefs()", "Instance of Boolean stored to shared preferences: \"" + sharedPreferences.toString() + "\", with key: \"" + key.toString() + "\", value: \"" + object + "\" and context: \"" + context.toString() + "\"");
		} else if (object instanceof List<?>) {
			// Create a new list and store object to it
			List<String> list = new ArrayList<String>();
			list = (List<String>) object;

			// Use JSON to serialize arraylist containing the secondary alarm numbers to a string
			JSONArray a = new JSONArray();

			// Iterate through each element in list and add object in JSON object
			for (int i = 0; i < list.size(); i++) {
				a.put(list.get(i));
			}

			// If list is not empty add it to shared preferences, if empty add empty string to
			// preferences
			if (!list.isEmpty()) {
				prefsEditor.putString(key.toString(), a.toString());
				prefsEditor.commit();
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setPrefs()", "Instance of List<String> stored as String to shared preferences: \"" + sharedPreferences.toString() + "\", with key: \"" + key.toString() + "\", value: \"" + a.toString() + "\" and context: \"" + context.toString() + "\"");
			} else {
				prefsEditor.putString(key.toString(), "");
				prefsEditor.commit();
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setPrefs()", "Instance of List<String> stored as String to shared preferences: \"" + sharedPreferences.toString() + "\", with key: \"" + key.toString() + "\", value: \"" + "" + "\" and context: \"" + context.toString() + "\"");
			}
		} else {
			// Unsupported data type was given as argument throw exception
			throw new IllegalArgumentException("Failed to store data to shared preferences: \"" + sharedPreferences.toString() + "\", with key: \"" + key.toString() + "\" and context: \"" + context.toString() + "\". Cause: \"Unsupported datatype\", valid DataTypes are: INTEGER, STRING, BOOLEAN and LIST");
		}
	}
}