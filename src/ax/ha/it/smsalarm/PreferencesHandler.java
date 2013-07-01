/*
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */

package ax.ha.it.smsalarm;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import ax.ha.it.smsalarm.LogHandler.LogPriorities;

/**
 * This class is responsible for all Shared Preferences handling.<br>
 * <b><i>PreferencesHandler is a singleton.</i></b>
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.1
 * @since 2.0
 * @date 2013-07-01
 */
public class PreferencesHandler {
	/**
	 * Enumeration for different datatypes needed when retrieving shared preferences.
	 * 
	 * @author Robert Nyholm <robert.nyholm@aland.net>
	 * @version 2.1
	 * @since 2.1
	 * @date 2013-07-01
	 */
	public enum DataTypes {
		INTEGER, STRING, BOOLEAN, LIST;
	}

	/**
	 * Enumeration for the shared preferences keys.
	 * 
	 * @author Robert Nyholm <robert.nyholm@aland.net>
	 * @version 2.1
	 * @since 2.1
	 * @date 2013-07-01
	 */
	public enum PrefKeys {
		HARED_PREF("smsAlarmPrefs"),
		NOT_KEY("notificationPref"),
		PRIMARY_LISTEN_NUMBER_KEY("primaryListenNumberKey"),
		SECONDARY_LISTEN_NUMBERS_KEY("secondaryListenNumbersKey"),
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
		HAS_CALLED_KEY("hasCalled");
		
		// Value in which enumerations values are stored
		private final String key;
		
		/**
		 * Constructor for this enumeration.
		 * 
		 * @param key Value to associate with the constructed enumeration as String
		 */
		PrefKeys(String key) {
			this.key = key;
		}
		
		/**
		 * To return the value associated with a PrefKeys enumeration.
		 * 
		 * @return Value associated with PrefKey as String
		 */
		public String getKey() {
			return this.key;
		}
	}
	
	// Singleton instance of this class
	private static PreferencesHandler INSTANCE;

	// Log tag
	private final String LOG_TAG = "PreferencesHandler";

	// Variable used to log messages
	private LogHandler logger;

	// Variables needed for retrieving shared preferences
	private SharedPreferences sharedPref;
	private Editor prefsEditor;

	/**
	 * Private constructor, is private due to it's singleton pattern.
	 * 
	 * @see {@link LogHandler#logCat(LogPriorities, String, String)}
	 */
	private PreferencesHandler() {
		// Get instance of logger
		this.logger = LogHandler.getInstance();

		// Log information
		logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":PreferencesHandler()", "New instance of PreferencesHandler created");
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
	 * Method to get values in <b>Shared Preferences</b>. It retrieves different
	 * values depending on input parameters. Returns retrieved value if all is
	 * fine else an IllegalArgumentsException are thrown.<br>
	 * Example usage:<br>
	 * <code>String s = getPrefs(SmsAlarm.SHARED_PREF, ACK_NUMBER_KEY, 1, this.context)</code>
	 * , this will retrieve a string(<code>type = 1</code>) from key
	 * <code>ACK_NUMBER_KEY</code> and with the given context.
	 * 
	 * @param sharedPreferences
	 *            SharedPreferences from which the values are retrieved from
	 * @param key
	 *            Key in which Shared Preference is stored
	 * @param type
	 *            Different type of values is retrieved from Shared Preferences,
	 *            indicated by this value. 0 = Integer, 1 = String, 2 = Boolean
	 *            and 3 = List of Strings
	 * @param context
	 *            Context from which the Shared Preferences should be retrieved
	 * 
	 * @return Returns an object with the retrieved values. This object can be
	 *         an instance of Integer, String, Boolean or a List of Strings
	 *                
	 * @exception IllegalArgumentException
	 * 				  if an incorrect datatype was given as parameter
	 * 
	 * @see #setPrefs(String, String, Object, Context)
	 * @see #getPrefs(String, String, int, Context)
	 * @see {@link LogHandler#logCat(LogPriorities, String, String)}
	 * @see {@link LogHandler#logCatTxt(LogPriorities, String, String)}
	 * @see {@link LogHandler#logCatTxt(LogPriorities, String, String, Throwable)}
	 */
	public Object getPrefs(String sharedPreferences, String key, DataTypes type, Context context) throws IllegalArgumentException {
		// Set shared preferences from context
		sharedPref = context.getSharedPreferences(sharedPreferences, Context.MODE_PRIVATE);

		switch (type) {
		case INTEGER:
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":getPrefs()", "Integer with value \"" + Integer.toString(sharedPref.getInt(key, 0)) + "\" retrieved from shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\"");
			return sharedPref.getInt(key, 0);
		case STRING:
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":getPrefs()", "String with value \"" + sharedPref.getString(key, "") + "\" retrieved from shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\"");
			return sharedPref.getString(key, "");
		case BOOLEAN:
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":getPrefs()", "Boolean with value \"" + sharedPref.getBoolean(key, false) + "\" retrieved from shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\"");
			return sharedPref.getBoolean(key, false);
		case LIST:
			// Retrieve secondaryListenNumbers to json string and clear secondaryListenNumbers List just to be sure that it's empty
			String json = sharedPref.getString(key, "");
			// List of Strings containing
			List<String> list = new ArrayList<String>();

			// If json string is not empty
			if (!json.equals("")) {
				try {
					// Create a JSONArray from json string and retrieve strings from it and and them to secondaryListenNumbers List
					JSONArray a = new JSONArray(json);
					for (int i = 0; i < a.length(); i++) {
						String secondaryListenNumber = a.optString(i);
						list.add(secondaryListenNumber);
					}
					this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":getPrefs()", "List<String> with value(s) \"" + json + "\"  retrieved from shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\"");
					// Return the list
					return list;
				} catch (JSONException e) {
					// Log JSONException
					this.logger.logCatTxt(LogPriorities.ERROR, this.LOG_TAG + ":getPrefs()", "Failed to retrieve List<String> from shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\"", e);
				}
			} else { // <--If JSON string is empty, return empty List
				this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":getPrefs()", "List<String> with value(s) \"" + json + "\"  retrieved from shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\"");
				// Return the list
				return list;
			}
			break;
		default:
			// DO NOTHING!
		}
		
		// We should never reach this far but if we do an error has occurred and an exception is thrown
		throw new IllegalArgumentException("Unsupported data type was givien as parameter. Shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\". Valid DataTypes are: INTEGER, STRING, BOOLEAN and LIST");		
	}

	/**
	 * Method to get values in <b>Shared Preferences</b>. It retrieves different
	 * values depending on input parameters. It's also possible to set default
	 * value to retrieve from shared preferences if no value exist, <b><i>NB.
	 * ONLY WORKING FOR DATATYPES Integer, String AND Boolean</i></b> Returns
	 * retrieved value if all is fine else an IllegalArgumentsException are thrown.<br>
	 * Example usage:<br>
	 * <code>String s = getPrefs(SmsAlarm.SHARED_PREF, ACK_NUMBER_KEY, 1, this.context, "empty")</code>
	 * , this will retrieve a string(<code>type = 1</code>) from key
	 * <code>ACK_NUMBER_KEY</code> and with the given context.
	 * 
	 * @param sharedPreferences
	 *            SharedPreferences from which the values are retrieved from
	 * @param key
	 *            Key in which Shared Preference is stored
	 * @param type
	 *            Different type of values is retrieved from Shared Preferences,
	 *            indicated by this value. 0 = Integer, 1 = String, 2 = Boolean
	 *            and 3 = List of Strings
	 * @param context
	 *            Context from which the Shared Preferences should be retrieved
	 * 
	 * @param defaultValue
	 *            Default value to be retrieved from shared preference if no
	 *            previous value exist
	 * 
	 * @return Returns an object with the retrieved values. This object can be
	 *         an instance of Integer, String, Boolean or a List of Strings
	 * 
	 * @exception IllegalArgumentException
	 * 				  if an incorrect datatype was given as parameter
	 * 
	 * @see #setPrefs(String, String, Object, Context)
	 * @see #getPrefs(String, String, int, Context, Object)
	 * @see {@link LogHandler#logCat(LogPriorities, String, String)}
	 * @see {@link LogHandler#logCatTxt(LogPriorities, String, String)}
	 * @see {@link LogHandler#logCatTxt(LogPriorities, String, String, Throwable)}
	 */
	public Object getPrefs(String sharedPreferences, String key, DataTypes type, Context context, Object defaultObject) throws IllegalArgumentException {
		// Set shared preferences from context
		sharedPref = context.getSharedPreferences(sharedPreferences, Context.MODE_PRIVATE);

		switch (type) {
		case INTEGER:
			// Check that defaultObject is of correct instance else collect "hardcoded" default value of 0
			if (defaultObject instanceof Integer) {
				this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":getPrefs()",
						"Integer with value \"" + Integer.toString(sharedPref.getInt(key, (Integer) defaultObject)) + "\" retrieved from shared preferences: \"" + sharedPreferences + ", with key: " + key + "\", type: \"" + type.name() + "\", context: \"" + context.toString() + "\" and default value: \"" + Integer.toString((Integer) defaultObject) + "\"");
				return sharedPref.getInt(key, (Integer) defaultObject);
			} else {
				this.logger.logCatTxt(LogPriorities.ERROR, this.LOG_TAG + ":getPrefs()",
						"Default value couldn't be set because of instance mismatch, hardcoded default value of 0 is used. However Integer with value \"" + Integer.toString(sharedPref.getInt(key, 0)) + "\" retrieved from shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + type.name() + "\", context: \"" + context.toString()
								+ "\" and default value: \"" + Integer.toString((Integer) defaultObject) + "\"");
				return sharedPref.getInt(key, 0);
			}
		case STRING:
			// Check that defaultObject is of correct instance else collect "hardcoded" default value of ""
			if (defaultObject instanceof String) {
				this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":getPrefs()", "String with value \"" + sharedPref.getString(key, (String) defaultObject) + "\" retrieved from shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\" and default value: \"" + (String) defaultObject
						+ "\"");
				return sharedPref.getString(key, (String) defaultObject);
			} else {
				this.logger.logCatTxt(LogPriorities.ERROR, this.LOG_TAG + ":getPrefs()", "Default value couldn't be set because of instance mismatch, hardcoded default value of \"\" is used. However String with value\"" + sharedPref.getString(key, "") + "\" retrieved from shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + type.name()
						+ "\" and context: " + context.toString() + "\"");
				return sharedPref.getString(key, "");
			}
		case BOOLEAN:
			// Check that defaultObject is of correct instance else collect "hardcoded" default value of false
			if (defaultObject instanceof Boolean) {
				this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":getPrefs()", "Boolean with value \"" + sharedPref.getBoolean(key, (Boolean) defaultObject) + "\" retrieved from shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\" and default value: \""
						+ (Boolean) defaultObject + "\"");
				return sharedPref.getBoolean(key, (Boolean) defaultObject);
			} else {
				this.logger.logCatTxt(LogPriorities.ERROR, this.LOG_TAG + ":getPrefs()", "Default value couldn't be set because of instance mismatch, hardcoded default value of false is used. However Boolean with value \"" + sharedPref.getBoolean(key, false) + "\" retrieved from shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + type.name()
						+ "\" and context: \"" + context.toString() + "\"");
				return sharedPref.getBoolean(key, false);
			}
		case LIST:
			// Retrieve secondaryListenNumbers to json string and clear secondaryListenNumbers List just to be sure that it's empty
			String json = sharedPref.getString(key, "");
			// List of Strings containing
			List<String> list = new ArrayList<String>();

			// If json string is not empty
			if (json != "") {
				try {
					// Create a JSONArray from json string and retrieve strings from it and and them to secondaryListenNumbers List
					JSONArray a = new JSONArray(json);
					for (int i = 0; i < a.length(); i++) {
						String secondaryListenNumber = a.optString(i);
						list.add(secondaryListenNumber);
					}
					this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":getPrefs()", "List<String> with value(s) \"" + json + "\"  retrieved from shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\"");
					// Return the list
					return list;
				} catch (JSONException e) {
					// Log JSONException
					this.logger.logCatTxt(LogPriorities.ERROR, this.LOG_TAG + ":getPrefs()", "Failed to retrieve List<String> from shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\"", e);
				}
			} else { // <--If JSON string is empty, return empty List
				this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":getPrefs()", "List<String> with value(s) \"" + json + "\"  retrieved from shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\"");
				// Return the list
				return list;
			}
			break;
		default:
			// DO NOTHING!
		}

		// We should never reach this far but if we do an error has occurred and an exception is thrown
		throw new IllegalArgumentException("Unsupported data type was givien as argument. Shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + type.name() + "\" and context: \"" + context.toString() + "\". Valid DataTypes are: INTEGER, STRING, BOOLEAN and LIST");
	}

	/**
	 * Method to set values to <b>Shared Preferences<b>. It sets different
	 * values of different instances depending on input parameters.<br>
	 * Example usage:<br>
	 * <code>setPrefs(SmsAlarm.SHARED_PREF, ACKNUMBER_KEY, "0457 0000 000", this.context</code>
	 * 
	 * @param sharedPreferences
	 *            SharedPreferences from which the values are retrieved from
	 * @param key
	 *            Key in which Shared Preference is stored
	 * @param object
	 *            Object to be stored to Shared Preferences, supported instances
	 *            are Integer, String, Boolean and List<String>
	 * @param context
	 *            Context from which the Shared Preferences should be retrieved
	 *            
	 * @exception IllegalArgumentException
	 * 				  if an incorrect datatype was given as parameter           
	 * 
	 * @see #getPrefs(String, String, int, Context)
	 * @see {@link LogHandler#logCat(LogPriorities, String, String)}
	 */
	@SuppressWarnings("unchecked")
	public void setPrefs(String sharedPreferences, String key, Object object, Context context) throws IllegalArgumentException {
		// Set shared preferences from context
		this.sharedPref = context.getSharedPreferences(sharedPreferences, Context.MODE_PRIVATE);
		this.prefsEditor = sharedPref.edit();

		if (object instanceof Integer) {
			// Put shared preferences as Integer
			this.prefsEditor.putInt(key, (Integer) object);
			this.prefsEditor.commit();
			// Log information
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":setPrefs()", "Instance of Integer stored to shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", value: \"" + Integer.toString((Integer) object) + "\" and context: \"" + context.toString() + "\"");
		} else if (object instanceof String) {
			// Put shared preferences as String
			this.prefsEditor.putString(key, (String) object);
			this.prefsEditor.commit();
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":setPrefs()", "Instance of String stored to shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", value: \"" + (String) object.toString() + "\" and context: \"" + context.toString() + "\"");
		} else if (object instanceof Boolean) {
			// Put shared preferences as Boolean
			this.prefsEditor.putBoolean(key, (Boolean) object);
			this.prefsEditor.commit();
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":setPrefs()", "Instance of Boolean stored to shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", value: \"" + (Boolean) object + "\" and context: \"" + context.toString() + "\"");
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

			// If list is not empty add it to shared preferences, if empty add empty string to preferences
			if (!list.isEmpty()) {
				this.prefsEditor.putString(key, a.toString());
				this.prefsEditor.commit();
				this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":setPrefs()", "Instance of List<String> stored as String to shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", value: \"" + a.toString() + "\" and context: \"" + context.toString() + "\"");
			} else {
				this.prefsEditor.putString(key, "");
				this.prefsEditor.commit();
				this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":setPrefs()", "Instance of List<String> stored as String to shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", value: \"" + "" + "\" and context: \"" + context.toString() + "\"");
			}
		} else {
			// Unsupported data type was given as argument throw exception
			throw new IllegalArgumentException("Failed to store data to shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\" and context: \"" + context.toString() + "\". Cause: \"Unsupported datatype\", valid DataTypes are: INTEGER, STRING, BOOLEAN and LIST");
		}
	}
}
