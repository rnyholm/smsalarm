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
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;
import ax.ha.it.smsalarm.application.SmsAlarmApplication.GoogleAnalyticsHandler;
import ax.ha.it.smsalarm.application.SmsAlarmApplication.GoogleAnalyticsHandler.ReportRule;

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
		SHARED_PREF("smsAlarmPrefs", ReportRule.NO_REPORT, "Shared preferences main key"),  
		PRIMARY_LISTEN_NUMBER_KEY("primaryListenNumberKey", ReportRule.NO_REPORT, "Primary listen number"), 								// Not used after version code 8
		PRIMARY_LISTEN_NUMBERS_KEY("primaryListenNumbersKey", ReportRule.REPORT_ANONYMIZE, "Primary alarm triggering phone numbers used"), 
		SECONDARY_LISTEN_NUMBERS_KEY("secondaryListenNumbersKey", ReportRule.REPORT_ANONYMIZE, "Secondary alarm triggering phone numbers used"), 
		PRIMARY_LISTEN_FREE_TEXTS_KEY("primaryListenFreeTextsKey", ReportRule.REPORT_ANONYMIZE, "Primary alarm triggering words used"), 
		SECONDARY_LISTEN_FREE_TEXTS_KEY("secondaryListenFreeTextsKey", ReportRule.REPORT_ANONYMIZE, "Secondary alarm triggering words used"), 
		PRIMARY_MESSAGE_TONE_KEY("primaryMessageToneKey", ReportRule.NO_REPORT, "Primary message tone"), 									// Not used after version code 13
		SECONDARY_MESSAGE_TONE_KEY("secondaryMessageToneKey", ReportRule.NO_REPORT, "Secondary message tone"), 								// Not used after version code 13
		ENABLE_ACK_KEY("enableAckKey", ReportRule.REPORT_RAW, "Enable acknowledgement"), 
		ACK_NUMBER_KEY("ackNumber", ReportRule.NO_REPORT, "Phone number for acknowledgement"), 
		ACK_MESSAGE_KEY("ackMessageKey", ReportRule.NO_REPORT, "Message for acknowledgement"),
		ACK_METHOD_KEY("ackMethodKey", ReportRule.REPORT_RAW, "Method for acknowledgement"),
		USE_OS_SOUND_SETTINGS_KEY("useOsSoundSettings", ReportRule.REPORT_RAW, "Use operating systems sound settings"), 
		PLAY_TONE_TWICE_KEY("playToneTwice", ReportRule.NO_REPORT, "Play tone twice"),														// Not used after version code 13
		PLAY_ALARM_SIGNAL_TWICE_KEY("playAlarmSignalTwice", ReportRule.REPORT_RAW, "Play alarm signal twice"),
		PLAY_ALARM_SIGNAL_REPEATEDLY_KEY("playAlarmSignalRepeatedly", ReportRule.REPORT_RAW, "Play alarm signal repeatedly"),
		ENABLE_SMS_ALARM_KEY("enableSmsAlarm", ReportRule.REPORT_RAW, "Enable Sms Alarm"), 
		RESCUE_SERVICE_KEY("rescueService", ReportRule.NO_REPORT, "Rescue service name used"), 												// Not used after version code 19
		ORGANIZATION_KEY("organization", ReportRule.REPORT_ANONYMIZE, "Organization name used"),
		HAS_CALLED_KEY("hasCalled", ReportRule.NO_REPORT, "Has called"), 
		END_USER_LICENSE_AGREED("userLicenseAgreed", ReportRule.NO_REPORT, "End user license agreed"), 
		VERSION_CODE("versionCode", ReportRule.NO_REPORT, "Version code"),
		USE_FLASH_NOTIFICATION("useFlashNotification", ReportRule.REPORT_RAW, "Use flash notification"),
		PRIMARY_ALARM_SIGNAL_KEY("primaryAlarmSignalKey", ReportRule.NO_REPORT, "Primary alarm signal"),
		SECONDARY_ALARM_SIGNAL_KEY("secondaryAlarmSignalKey", ReportRule.NO_REPORT, "Secondary alarm signal"),
		USER_ADDED_ALARM_SIGNALS_KEY("userAddedAlarmSignalsKey", ReportRule.REPORT_ANONYMIZE, "User added alarm signals used"),
		PRIMARY_ALARM_VIBRATION_KEY("primaryAlarmVibrationKey", ReportRule.NO_REPORT, "Primary alarm vibration"),
		SECONDARY_ALARM_VIBRATION_KEY("secondaryAlarmVibrationKey", ReportRule.NO_REPORT, "Secondary alarm vibration"),
		ENABLE_SMS_DEBUG_LOGGING("enableSmsDebugLogging", ReportRule.REPORT_RAW, "Enable SMS debugging"),
		PRIMARY_LISTEN_REGULAR_EXPRESSIONS_KEY("primaryListenRegularExpressionsKey", ReportRule.REPORT_ANONYMIZE, "Primary alarm triggering regular expressions used"),
		SECONDARY_LISTEN_REGULAR_EXPRESSIONS_KEY("secondaryListenRegularExpressionsKey", ReportRule.REPORT_ANONYMIZE, "Secondary alarm triggering regular expressions used"),
		UNDEFINED_KEY("undefinedKey", ReportRule.NO_REPORT, "Undefined setting");
		// @formatter:on

		// The actual key to which data will be stored and fetched from
		private final String key;

		// Text of key written in a more readable way used only when data is sent to Google Analytics
		private final String reportText;

		// Rule which decide how the shared preference associated to this PrefKey should be reported to Google Analytics, if it should be reported
		private final ReportRule reportRule;

		/**
		 * Creates a new {@link PrefKey}.
		 * 
		 * @param key
		 *            Value(key) to associate with the PrefKey enumeration. This key is the actual key that data will be stored to or fetched from
		 *            {@link SharedPreferences} with.
		 * @param reportRule
		 *            Rule which decide how the shared preference associated to this PrefKey should be reported to Google Analytics, if it should be
		 *            reported.
		 * @param reportText
		 *            Text of shared preference used when PrefKey is reported to Google Analytics.
		 */
		private PrefKey(String key, ReportRule reportRule, String reportText) {
			this.key = key;
			this.reportRule = reportRule;
			this.reportText = reportText;
		}

		/**
		 * To get the actual <code>Key</code> with which data is stored to and fetched from {@link SharedPreferences} with.
		 * 
		 * @return The <code>Key</code>.
		 */
		public String getKey() {
			return key;
		}

		/**
		 * To get the text of this {@link PrefKey} which should be sent of to Google Analytics.
		 * 
		 * @return The <code>reportText</code>.
		 */
		public String getReportText() {
			return reportText;
		}

		/**
		 * To get the {@link ReportRule} associated with the {@link SharedPreferences} stored with this {@link PrefKey}'s key.
		 * 
		 * @return The <code>ReportRule</code>.
		 */
		public ReportRule getReportRule() {
			return reportRule;
		}

		/**
		 * To resolve a {@link PrefKey} from given key as {@link String}.
		 * 
		 * @param key
		 *            Key from which a <code>PrefKey</code> will be resolved from.
		 * @return Resolved <code>PrefKey</code>, if no <code>PrefKey</code> can be resolved then {@link PrefKey#UNDEFINED_KEY} will be returned.
		 */
		public static PrefKey of(String key) {
			PrefKey resolvedPrefKey = UNDEFINED_KEY;

			for (PrefKey prefKey : PrefKey.values()) {
				if (prefKey.getKey().equals(key)) {
					resolvedPrefKey = prefKey;
				}
			}

			return resolvedPrefKey;
		}
	}

	// Singleton instance of this class, eagerly initialized
	private static SharedPreferencesHandler INSTANCE = new SharedPreferencesHandler();

	private static final String LOG_TAG = SharedPreferencesHandler.class.getSimpleName();

	// Variables needed for retrieving shared preferences
	private SharedPreferences sharedPref;
	private Editor prefsEditor;

	// Need a listener to discover changes made in the shared preferences
	private final OnSharedPreferenceChangeListener listener;

	/**
	 * Creates a new instance of {@link SharedPreferencesHandler}.
	 */
	private SharedPreferencesHandler() {
		if (INSTANCE != null) {
			Log.e(LOG_TAG + ":SharedPreferencesHandler()", "SharedPreferencesHandler already instantiated");
		}

		// Set up the listener only once as it should have the same behavior all the time
		listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
				// Report the change
				GoogleAnalyticsHandler.sendSettingsChangedEvent(prefs, key);
			}
		};
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
		// Set shared preferences from context and set listener to it
		sharedPref = context.getSharedPreferences(sharedPreference.getKey(), Context.MODE_PRIVATE);
		sharedPref.registerOnSharedPreferenceChangeListener(listener);

		// Resolve editor for the shared preferences
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
			// Unregister listener when shit hits the fan also
			sharedPref.unregisterOnSharedPreferenceChangeListener(listener);

			// If application end up here then some error has occurred
			IllegalArgumentException exception = new IllegalArgumentException("Failed to store object to shared preferences: \"" + sharedPreference.getKey() + "\", with key: \"" + sharedPreferencesKey.getKey() + "\" and context: \"" + context.toString() + "\". Cause: \"Object of unsupported instance was given as argument\", given object is instance of: \"" + object.getClass().getSimpleName() + "\", valid instances are: \"int\", \"String\", \"boolean\" and \"List<String>\"");
			Log.e(LOG_TAG + ":storePrefs()", "An exception occurred while setting shared preferences", exception);

			throw exception;
		}

		// Remember to unregister the listener
		sharedPref.unregisterOnSharedPreferenceChangeListener(listener);
	}
}
