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

/**
 * This class is responsible for all Shared Preferences handling.<br>
 * <b><i>PreferencesHandler is a singleton.</i></b>
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.1
 * @since 2.0
 * @date 2013-06-30
 */
public class PreferencesHandler {
	// Singleton instance of this class
	private static PreferencesHandler INSTANCE;

	// Log tag
	private final String LOG_TAG = "PreferencesHandler";

	// Constants representing different datatypes
	private final int INTEGER = 0;
	private final int STRING = 1;
	private final int BOOLEAN = 2;
	private final int LIST = 3;

	// Shared preferences constants
	private final String SHARED_PREF = "smsAlarmPrefs";
	private final String NOT_KEY = "notificationPref";
	private final String PRIMARY_LISTEN_NUMBER_KEY = "primaryListenNumberKey";
	private final String SECONDARY_LISTEN_NUMBERS_KEY = "secondaryListenNumbersKey";
	private final String PRIMARY_MESSAGE_TONE_KEY = "primaryMessageToneKey";
	private final String SECONDARY_MESSAGE_TONE_KEY = "secondaryMessageToneKey";
	private final String MESSAGE_KEY = "messageKey";
	private final String FULL_MESSAGE_KEY = "fullMessageKey";
	private final String ENABLE_ACK_KEY = "enableAckKey";
	private final String ACK_NUMBER_KEY = "ackNumber";
	private final String USE_OS_SOUND_SETTINGS_KEY = "useOsSoundSettings";
	private final String LARM_TYPE_KEY = "larmType";
	private final String PLAY_TONE_TWICE_KEY = "playToneTwice";
	private final String ENABLE_SMS_ALARM_KEY = "enableSmsAlarm";
	private final String RESCUE_SERVICE_KEY = "rescueService";
	private final String HAS_CALLED_KEY = "hasCalled";

	// Variable used to log messages
	private LogHandler logger;

	// Variables needed for retrieving shared preferences
	private SharedPreferences sharedPref;
	private Editor prefsEditor;

	/**
	 * Private constructor, is private due to it's singleton pattern.
	 */
	private PreferencesHandler() {
		// Get instance of logger
		this.logger = LogHandler.getInstance();

		// Log information
		logger.logCatTxt(logger.getINFO(), this.LOG_TAG + ":PreferencesHandler()", "New instance of PreferencesHandler created");
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
	 * Getter for the shared preferences constant SHARED_PREF
	 * 
	 * @return The SHARED_PREF constant
	 */
	public String getSHARED_PREF() {
		return SHARED_PREF;
	}

	/**
	 * Getter for the shared preferences constant NOT_KEY
	 * 
	 * @return The NOT_KEY constant
	 */
	public String getNOT_KEY() {
		return NOT_KEY;
	}

	/**
	 * Getter for the shared preferences constant PRIMARY_LISTEN_NUMBER_KEY
	 * 
	 * @return The PRIMARY_LISTEN_NUMBER_KEY constant
	 */
	public String getPRIMARY_LISTEN_NUMBER_KEY() {
		return PRIMARY_LISTEN_NUMBER_KEY;
	}

	/**
	 * Getter for the shared preferences constant SECONDARY_LISTEN_NUMBERS_KEY
	 * 
	 * @return The SECONDARY_LISTEN_NUMBERS_KEY constant
	 */
	public String getSECONDARY_LISTEN_NUMBERS_KEY() {
		return SECONDARY_LISTEN_NUMBERS_KEY;
	}

	/**
	 * Getter for the shared preferences constant PRIMARY_MESSAGE_TONE_KEY
	 * 
	 * @return The PRIMARY_MESSAGE_TONE_KEY constant
	 */
	public String getPRIMARY_MESSAGE_TONE_KEY() {
		return PRIMARY_MESSAGE_TONE_KEY;
	}

	/**
	 * Getter for the shared preferences constant SECONDARY_MESSAGE_TONE_KEY
	 * 
	 * @return The SECONDARY_MESSAGE_TONE_KEY constant
	 */
	public String getSECONDARY_MESSAGE_TONE_KEY() {
		return SECONDARY_MESSAGE_TONE_KEY;
	}

	/**
	 * Getter for the shared preferences constant MESSAGE_KEY
	 * 
	 * @return The MESSAGE_KEY constant
	 */
	public String getMESSAGE_KEY() {
		return MESSAGE_KEY;
	}

	/**
	 * Getter for the shared preferences constant FULL_MESSAGE_KEY
	 * 
	 * @return The FULL_MESSAGE_KEY constant
	 */
	public String getFULL_MESSAGE_KEY() {
		return FULL_MESSAGE_KEY;
	}

	/**
	 * Getter for the shared preferences constant ENABLE_ACK_KEY
	 * 
	 * @return The ENABLE_ACK_KEY constant
	 */
	public String getENABLE_ACK_KEY() {
		return ENABLE_ACK_KEY;
	}

	/**
	 * Getter for the shared preferences constant ACK_NUMBER_KEY
	 * 
	 * @return The ACK_NUMBER_KEY constant
	 */
	public String getACK_NUMBER_KEY() {
		return ACK_NUMBER_KEY;
	}

	/**
	 * Getter for the shared preferences constant USE_OS_SOUND_SETTINGS_KEY
	 * 
	 * @return The USE_OS_SOUND_SETTINGS_KEY constant
	 */
	public String getUSE_OS_SOUND_SETTINGS_KEY() {
		return USE_OS_SOUND_SETTINGS_KEY;
	}

	/**
	 * Getter for the shared preferences constant LARM_TYPE_KEY
	 * 
	 * @return The LARM_TYPE_KEY constant
	 */
	public String getLARM_TYPE_KEY() {
		return LARM_TYPE_KEY;
	}

	/**
	 * Getter for the shared preferences constant PLAY_TONE_TWICE_KEY
	 * 
	 * @return The PLAY_TONE_TWICE_KEY constant
	 */
	public String getPLAY_TONE_TWICE_KEY() {
		return PLAY_TONE_TWICE_KEY;
	}

	/**
	 * Getter for the shared preferences constant ENABLE_SMS_ALARM_KEY
	 * 
	 * @return The ENABLE_SMS_ALARM_KEY constant
	 */
	public String getENABLE_SMS_ALARM_KEY() {
		return ENABLE_SMS_ALARM_KEY;
	}

	/**
	 * Getter for the shared preferences constant RESCUE_SERVICE_KEY
	 * 
	 * @return The RESCUE_SERVICE_KEY constant
	 */
	public String getRESCUE_SERVICE_KEY() {
		return RESCUE_SERVICE_KEY;
	}

	/**
	 * Getter for the shared preferences constant HAS_CALLED_KEY
	 * 
	 * @return The HAS_CALLED_KEY constant
	 */
	public String getHAS_CALLED_KEY() {
		return HAS_CALLED_KEY;
	}

	/**
	 * Method to get values in <b>Shared Preferences</b>. It retrieves different
	 * values depending on input parameters. Returns retrieved value if all is
	 * fine else it returns a String "ERROR".<br>
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
	 * @exception JSONException
	 *                if an error occur retrieving String from JSON Array
	 * 
	 * @see #setPrefs(String, String, Object, Context)
	 * @see #getPrefs(String, String, int, Context)
	 */
	public Object getPrefs(String sharedPreferences, String key, int type, Context context) {
		// A String indicating an error occurred while retrieving shared preferences
		final String ERROR = "ERROR";

		// Set shared preferences from context
		sharedPref = context.getSharedPreferences(sharedPreferences, Context.MODE_PRIVATE);

		switch (type) {
		case (INTEGER): // <-- 0
			this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":getPrefs()", "Integer with value \"" + Integer.toString(sharedPref.getInt(key, 0)) + "\" retrieved from shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + Integer.toString(type) + "\" and context: \"" + context.toString() + "\"");
			return sharedPref.getInt(key, 0);
		case (STRING): // <-- 1
			this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":getPrefs()", "String with value \"" + sharedPref.getString(key, "") + "\" retrieved from shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + Integer.toString(type) + "\" and context: \"" + context.toString() + "\"");
			return sharedPref.getString(key, "");
		case (BOOLEAN): // <-- 2
			this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":getPrefs()", "Boolean with value \"" + sharedPref.getBoolean(key, false) + "\" retrieved from shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + Integer.toString(type) + "\" and context: \"" + context.toString() + "\"");
			return sharedPref.getBoolean(key, false);
		case (LIST): // <-- 3
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
					this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":getPrefs()", "List<String> with value(s) \"" + json + "\"  retrieved from shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + Integer.toString(type) + "\" and context: \"" + context.toString() + "\"");
					// Return the list
					return list;
				} catch (JSONException e) {
					e.printStackTrace();
					this.logger.logCatTxt(this.logger.getERROR(), this.LOG_TAG + ":getPrefs()", "Failed to retrieve List<String> from shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + Integer.toString(type) + "\" and context: \"" + context.toString() + "\"", e);
				}
			} else { // <--If JSON string is empty, return empty List
				this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":getPrefs()", "List<String> with value(s) \"" + json + "\"  retrieved from shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + Integer.toString(type) + "\" and context: \"" + context.toString() + "\"");
				// Return the list
				return list;
			}
			break;
		default:
			this.logger.logCatTxt(this.logger.getWARN(), this.LOG_TAG + ":getPrefs()", "Unsupported data type was givien as parameter. Shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + Integer.toString(type) + "\" and context: \"" + context.toString() + "\"");
		}

		// We should never reach this far but if we do an error has occurred and  we return the ERROR string
		return ERROR;
	}

	/**
	 * Method to get values in <b>Shared Preferences</b>. It retrieves different
	 * values depending on input parameters. It's also possible to set default
	 * value to retrieve from shared preferences if no value exist, <b><i>NB.
	 * ONLY WORKING FOR DATATYPES Integer, String AND Boolean</i></b> Returns
	 * retrieved value if all is fine else it returns a String "ERROR".<br>
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
	 * @exception JSONException
	 *                if an error occur retrieving String from JSON Array
	 * 
	 * @see #setPrefs(String, String, Object, Context)
	 * @see #getPrefs(String, String, int, Context, Object)
	 */
	public Object getPrefs(String sharedPreferences, String key, int type, Context context, Object defaultObject) {
		// A String indicating an error occurred while retrieving shared
		// preferences
		final String ERROR = "ERROR";

		// Set shared preferences from context
		sharedPref = context.getSharedPreferences(sharedPreferences, Context.MODE_PRIVATE);

		switch (type) {
		case (INTEGER): // <-- 0
			// Check that defaultObject is of correct instance else collect "hardcoded" default value of 0
			if (defaultObject instanceof Integer) {
				this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":getPrefs()",
						"Integer with value \"" + Integer.toString(sharedPref.getInt(key, (Integer) defaultObject)) + "\" retrieved from shared preferences: \"" + sharedPreferences + ", with key: " + key + "\", type: \"" + Integer.toString(type) + "\", context: \"" + context.toString() + "\" and default value: \"" + Integer.toString((Integer) defaultObject) + "\"");
				return sharedPref.getInt(key, (Integer) defaultObject);
			} else {
				this.logger.logCatTxt(this.logger.getERROR(), this.LOG_TAG + ":getPrefs()",
						"Default value couldn't be set because of instance mismatch, hardcoded default value of 0 is used. However Integer with value \"" + Integer.toString(sharedPref.getInt(key, 0)) + "\" retrieved from shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + Integer.toString(type) + "\", context: \"" + context.toString()
								+ "\" and default value: \"" + Integer.toString((Integer) defaultObject) + "\"");
				return sharedPref.getInt(key, 0);
			}
		case (STRING): // <-- 1
			// Check that defaultObject is of correct instance else collect "hardcoded" default value of ""
			if (defaultObject instanceof String) {
				this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":getPrefs()", "String with value \"" + sharedPref.getString(key, (String) defaultObject) + "\" retrieved from shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + Integer.toString(type) + "\" and context: \"" + context.toString() + "\" and default value: \"" + (String) defaultObject
						+ "\"");
				return sharedPref.getString(key, (String) defaultObject);
			} else {
				this.logger.logCatTxt(this.logger.getERROR(), this.LOG_TAG + ":getPrefs()", "Default value couldn't be set because of instance mismatch, hardcoded default value of \"\" is used. However String with value\"" + sharedPref.getString(key, "") + "\" retrieved from shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + Integer.toString(type)
						+ "\" and context: " + context.toString() + "\"");
				return sharedPref.getString(key, "");
			}
		case (BOOLEAN): // <-- 2
			// Check that defaultObject is of correct instance else collect "hardcoded" default value of false
			if (defaultObject instanceof Boolean) {
				this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":getPrefs()", "Boolean with value \"" + sharedPref.getBoolean(key, (Boolean) defaultObject) + "\" retrieved from shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + Integer.toString(type) + "\" and context: \"" + context.toString() + "\" and default value: \""
						+ (Boolean) defaultObject + "\"");
				return sharedPref.getBoolean(key, (Boolean) defaultObject);
			} else {
				this.logger.logCatTxt(this.logger.getERROR(), this.LOG_TAG + ":getPrefs()", "Default value couldn't be set because of instance mismatch, hardcoded default value of false is used. However Boolean with value \"" + sharedPref.getBoolean(key, false) + "\" retrieved from shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + Integer.toString(type)
						+ "\" and context: \"" + context.toString() + "\"");
				return sharedPref.getBoolean(key, false);
			}
		case (LIST): // <-- 3
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
					this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":getPrefs()", "List<String> with value(s) \"" + json + "\"  retrieved from shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + Integer.toString(type) + "\" and context: \"" + context.toString() + "\"");
					// Return the list
					return list;
				} catch (JSONException e) {
					e.printStackTrace();
					this.logger.logCatTxt(this.logger.getERROR(), this.LOG_TAG + ":getPrefs()", "Failed to retrieve List<String> from shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + Integer.toString(type) + "\" and context: \"" + context.toString() + "\"", e);
				}
			} else { // <--If JSON string is empty, return empty List
				this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":getPrefs()", "List<String> with value(s) \"" + json + "\"  retrieved from shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + Integer.toString(type) + "\" and context: \"" + context.toString() + "\"");
				// Return the list
				return list;
			}
			break;
		default:
			this.logger.logCatTxt(this.logger.getWARN(), this.LOG_TAG + ":getPrefs()", "Unsupported data type was givien as parameter. Shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", type: \"" + Integer.toString(type) + "\" and context: \"" + context.toString() + "\"");
		}

		// We should never reach this far but if we do an error has occurred and we return the ERROR string
		return ERROR;
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
	 * @see #getPrefs(String, String, int, Context)
	 */
	@SuppressWarnings("unchecked")
	public void setPrefs(String sharedPreferences, String key, Object object, Context context) {
		// Set shared preferences from context
		this.sharedPref = context.getSharedPreferences(sharedPreferences, Context.MODE_PRIVATE);
		this.prefsEditor = sharedPref.edit();

		if (object instanceof Integer) {
			// Put shared preferences as Integer
			this.prefsEditor.putInt(key, (Integer) object);
			this.prefsEditor.commit();
			// Log information
			this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":setPrefs()", "Instance of Integer stored to shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", value: \"" + Integer.toString((Integer) object) + "\" and context: \"" + context.toString() + "\"");
		} else if (object instanceof String) {
			// Put shared preferences as String
			this.prefsEditor.putString(key, (String) object);
			this.prefsEditor.commit();
			this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":setPrefs()", "Instance of String stored to shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", value: \"" + (String) object.toString() + "\" and context: \"" + context.toString() + "\"");
		} else if (object instanceof Boolean) {
			// Put shared preferences as Boolean
			this.prefsEditor.putBoolean(key, (Boolean) object);
			this.prefsEditor.commit();
			this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":setPrefs()", "Instance of Boolean stored to shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", value: \"" + (Boolean) object + "\" and context: \"" + context.toString() + "\"");
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
				this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":setPrefs()", "Instance of List<String> stored as String to shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", value: \"" + a.toString() + "\" and context: \"" + context.toString() + "\"");
			} else {
				this.prefsEditor.putString(key, "");
				this.prefsEditor.commit();
				this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":setPrefs()", "Instance of List<String> stored as String to shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\", value: \"" + "" + "\" and context: \"" + context.toString() + "\"");
			}
		} else {
			// Unsupported data type, log it
			this.logger.logCatTxt(this.logger.getWARN(), this.LOG_TAG + ":setPrefs()", "Failed to store data to shared preferences: \"" + sharedPreferences + "\", with key: \"" + key + "\" and context: \"" + context.toString() + "\". Cause Unsupported datatype");
		}
	}
}
