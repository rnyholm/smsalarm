/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm;

import java.io.File;
import java.io.IOException;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.RemoteViews;
import ax.ha.it.smsalarm.LogHandler.LogPriorities;
import ax.ha.it.smsalarm.PreferencesHandler.DataTypes;
import ax.ha.it.smsalarm.PreferencesHandler.PrefKeys;

/**
 * Provider class for the app widgets. This class is responsible for all
 * updates, data population, data presentation and so on for a widget. 
 * This implementation should be safe with more than one instances of
 * the Sms Alarm widget.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.2
 * @since 2.1
 */
public class WidgetProvider extends AppWidgetProvider {
	// Log tag string
	private String LOG_TAG = getClass().getSimpleName();

	// Objects needed for logging and shared preferences
	private LogHandler logger = LogHandler.getInstance();
	private PreferencesHandler prefHandler = PreferencesHandler.getInstance();

	// Object to handle database access and methods
	private DatabaseHandler db;

	// Max length of the latest alarm length in widget
	private static final int ALARM_TEXT_MAX_LENGTH = 125;

	// Strings representing different intents used to run different methods from intent
	public static final String TOGGLE_ENABLE_SMS_ALARM = "ax.ha.it.smsalarm.TOGGLE_SMS_ALARM_ENABLE";
	public static final String TOGGLE_USE_OS_SOUND_SETTINGS = "ax.ha.it.smsalarm.TOGGLE_USE_OS_SOUND_SETTINGS";
	public static final String SHOW_RECEIVED_ALARMS = "ax.ha.it.smsalarm.SHOW_RECEIVED_ALARMS";
	public static final String UPDATE_WIDGETS = "ax.ha.it.smsalarm.UPDATE_WIDGETS";

	// Some booleans for retrieving preferences into
	private boolean useOsSoundSettings = false;
	private boolean enableSmsAlarm = false;
	private boolean endUserLicenseAgreed = false;

	/**
	 * To receive intents broadcasted throughout the operating system. If it receives any
	 * intents that this method listens on the method takes proper action. <br><br>
	 * The method listens on following intents:
	 * <ul>
	 * <li>ax.ha.it.smsalarm.TOGGLE_SMS_ALARM_ENABLE</li>
	 * <li>ax.ha.it.smsalarm.TOGGLE_USE_OS_SOUND_SETTINGS</li>
	 * <li>ax.ha.it.smsalarm.SHOW_RECEIVED_ALARMS</li>
	 * <li>ax.ha.it.smsalarm.UPDATE_WIDGETS</li>
	 * </ul>
	 * 
	 * @see #onUpdate(Context, AppWidgetManager, int[])
	 * @see #getWidgetPrefs(Context)
	 * @see #setEnableSmsAlarmPref(Context, boolean)
	 * @see #setUseOsSoundSettingsPref(Context, boolean)
	 * @see #WidgetProvider()
	 * @see ax.ha.it.smsalarm.LogHandler#getAlarmLogPath()
	 * 		ax.ha.it.smsalarm.LogHandler#getAlarmLogPath()
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 *      logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String, String, Throwable)
	 *      logCatTxt(LogPriorities, String, String, Throwable)
	 * @see ax.ha.it.smsalarm.DatabaseHandler ax.ha.it.smsalarm.DatabaseHandler 
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		// Get AppWidgetManager from context
		AppWidgetManager manager = AppWidgetManager.getInstance(context);

		// Get Shared preferences needed by widget
		getWidgetPrefs(context);

		// If statements to "catch" intent we looking for
		if (intent.getAction().equals(TOGGLE_ENABLE_SMS_ALARM)) {
			// Some logging for information and debugging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onReceive()", "Received intent:\"" + TOGGLE_ENABLE_SMS_ALARM + "\"");

			// Set shared preferences depending on current preferences
			if (enableSmsAlarm) {
				setEnableSmsAlarmPref(context, false);
			} else {
				setEnableSmsAlarmPref(context, true);
			}

			// Update widget
			WidgetProvider.updateWidgets(context);
		} else if (intent.getAction().equals(TOGGLE_USE_OS_SOUND_SETTINGS)) {
			// Some logging for information and debugging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onReceive()", "Received intent:\"" + TOGGLE_USE_OS_SOUND_SETTINGS + "\"");

			if (useOsSoundSettings) {
				setUseOsSoundSettingsPref(context, false);
			} else {
				setUseOsSoundSettingsPref(context, true);
			}

			WidgetProvider.updateWidgets(context);
		} else if (intent.getAction().equals(SHOW_RECEIVED_ALARMS)) {
			// Some logging for information and debugging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onReceive()", "Received intent:\"" + SHOW_RECEIVED_ALARMS + "\"");

			try {
				// Get full file path to alarm log file
				String alarmLogFilePath = logger.getAlarmLogPath();

				// Create an intent for opening the alarm log file
				Intent showReceivedAlarmsIntent = new Intent();
				showReceivedAlarmsIntent.setAction(android.content.Intent.ACTION_VIEW);
				showReceivedAlarmsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Create new task
				
				// File object for the alarm log file
				File file = new File(alarmLogFilePath);

				// Set mime type for deciding which file format file is in
				MimeTypeMap mime = MimeTypeMap.getSingleton();
				String ext = file.getName().substring(file.getName().indexOf(".") + 1);
				String type = mime.getMimeTypeFromExtension(ext);

				// Set data and type, in this case alarm log file and html
				showReceivedAlarmsIntent.setDataAndType(Uri.fromFile(file), type);
				
				// Start new activity from context
				context.startActivity(showReceivedAlarmsIntent);
				
				// Some logging for information and debugging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onReceive()", "Successfully started activity to view alarm log file");
			} catch (IOException e) {
				// Log error
				logger.logCatTxt(LogPriorities.WARN, LOG_TAG + ":onReceive()", "An IOException occurred while retrieving path to alarm log file", e);
			}
		} else if (intent.getAction().equals(UPDATE_WIDGETS)) {
			// Some logging for information and debugging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onReceive()", "Received intent:\"" + UPDATE_WIDGETS + "\"");
			
			// Call onUpdate to update the widget instances
			onUpdate(context, manager, AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context.getPackageName(), getClass().getName())));
		}

		// Needs to call superclass's onReceive
		super.onReceive(context, intent);
	}

	/**
	 * To update all instances of the Sms Alarm widget. 
	 * 
	 * @see #onReceive(Context, Intent)
	 * @see #getWidgetPrefs(Context)
	 * @see #setWidgetTextViews(RemoteViews, Context)
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 *      logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.DatabaseHandler ax.ha.it.smsalarm.DatabaseHandler 
	 */
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// Some logging for information and debugging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onUpdate()", "Start updating widget");

		// Get Shared preferences needed by widget
		getWidgetPrefs(context);
		// Initialize database handler object from context
		db = new DatabaseHandler(context);
		// RemoteViews object needed to configure layout of widget
		RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);

		// Update each of the apps widgets with the remote adapter
		for (int i = 0; i < appWidgetIds.length; ++i) {
			// Set intent to start sms alarm and wrap it into a pending intent, rest of the intents are configured in the same way
			Intent smsAlarmIntent = new Intent(context, Splash.class);
			smsAlarmIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
			PendingIntent smsAlarmPendingIntent = PendingIntent.getActivity(context, 0, smsAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			Intent enableSmsAlarmIntent = new Intent(context, WidgetProvider.class);
			enableSmsAlarmIntent.setAction(WidgetProvider.TOGGLE_ENABLE_SMS_ALARM);
			enableSmsAlarmIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
			PendingIntent enableSmsAlarmPendingIntent = PendingIntent.getBroadcast(context, 0, enableSmsAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			Intent useOsSoundSettingsIntent = new Intent(context, WidgetProvider.class);
			useOsSoundSettingsIntent.setAction(WidgetProvider.TOGGLE_USE_OS_SOUND_SETTINGS);
			useOsSoundSettingsIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
			PendingIntent useOsSoundSettingsPendingIntent = PendingIntent.getBroadcast(context, 0, useOsSoundSettingsIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			Intent showReceivedAlarmsIntent = new Intent(context, WidgetProvider.class);
			showReceivedAlarmsIntent.setAction(WidgetProvider.SHOW_RECEIVED_ALARMS);
			showReceivedAlarmsIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
			PendingIntent showReceivedAlarmsPendingIntent = PendingIntent.getBroadcast(context, 0, showReceivedAlarmsIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			// Set widget texts
			setWidgetTextViews(rv, context);

			// Set onclick pending intent to start Sms Alarm, this is always set
			rv.setOnClickPendingIntent(R.id.widget_logo_iv, smsAlarmPendingIntent);

			// If user has agreed the end user license, set the rest of the on click pending intents also
			if (endUserLicenseAgreed) {
				rv.setOnClickPendingIntent(R.id.widget_smsalarm_status_tv, enableSmsAlarmPendingIntent);
				rv.setOnClickPendingIntent(R.id.widget_soundsettings_status_tv, useOsSoundSettingsPendingIntent);
				rv.setOnClickPendingIntent(R.id.widget_latest_received_alarm_tv, showReceivedAlarmsPendingIntent);
			}

			// Update widget
			appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
		}

		// Call to super class onUpdate method, so the os can run it's native methods
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		// Some logging for information and debugging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onUpdate()", "Widget has been updated");
	}

	/**
	 * To set appropriate text's to the different <code>TextView</code>'s. The
	 * text's are set depending if user has agreed the end user license, and if
	 * any <code>Alarm</code>'s has been received, status of
	 * SmsAlarm(enabled/disabled) and status of use devices sound
	 * settings(enabled/disabled).
	 * 
	 * @param rv
	 *            <code>RemoteViews</code> that texts are set to
	 * @param context
	 *            <code>Context</code> from which resources are retrieved
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 *      logCat(LogPriorities, String, String)
	 * @see #getLatestAlarm(Context)
	 */
	private void setWidgetTextViews(RemoteViews rv, Context context) {
		// Some logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setWidgetTextViews()", "TextViews of widget are about to be set");

		// If user has agreed end user license, we fill in the textviews with "real" data
		if (endUserLicenseAgreed) {
			// Check if Sms Alarm is enabled or not and set textview depending on that
			if (enableSmsAlarm) {
				rv.setTextViewText(R.id.widget_smsalarm_status_tv, context.getString(R.string.SMS_ALARM_STATUS_ENABLED));
			} else {
				rv.setTextViewText(R.id.widget_smsalarm_status_tv, context.getString(R.string.SMS_ALARM_STATUS_DISABLED));
			}

			// Check if use devices sound settings is enabled or not and set textview depending on that
			if (useOsSoundSettings) {
				rv.setTextViewText(R.id.widget_soundsettings_status_tv, context.getString(R.string.SOUND_SETTINGS_STATUS_ENABLED));
			} else {
				rv.setTextViewText(R.id.widget_soundsettings_status_tv, context.getString(R.string.SOUND_SETTINGS_STATUS_DISABLED));
			}

			// Set the (shortened) alarm to textview
			rv.setTextViewText(R.id.widget_latest_received_alarm_tv, getLatestAlarm(context));

			// Set correct dividers to widget
			rv.setImageViewResource(R.id.widget_divider2_iv, R.drawable.gradient_divider_widget);
			rv.setImageViewResource(R.id.widget_divider3_iv, R.drawable.gradient_divider_widget);
		} else { // User has not agreed end user license, hide dividers and textviews, set text to one textview telling user whats wrong
			rv.setTextViewText(R.id.widget_smsalarm_status_tv, context.getString(R.string.WIDGET_NOT_AGREED_EULA));
			rv.setImageViewResource(R.id.widget_divider2_iv, android.R.color.transparent);
			rv.setTextViewText(R.id.widget_soundsettings_status_tv, context.getString(R.string.EMPTY_STRING));
			rv.setImageViewResource(R.id.widget_divider3_iv, android.R.color.transparent);
			rv.setTextViewText(R.id.widget_latest_received_alarm_tv, context.getString(R.string.EMPTY_STRING));
		}
	}

	/**
	 * To get the latest <code>Alarm</code> from database as <code>String</code>
	 * . If no <code>Alarm</code> exist or the <code>Alarm</code> is empty an
	 * appropriate <code>String</code> is returned instead.
	 * 
	 * @param context
	 *            Context from which resources are retrieved
	 * @return String with appropriate text depending on if any alarms exists or
	 *         not in database
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 *      logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.Alarm ax.ha.it.smsalarm.Alarm
	 * @see ax.ha.it.smsalarm.DatabaseHandler ax.ha.it.smsalarm.DatabaseHandler 
	 */
	private String getLatestAlarm(Context context) {
		// Some logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getLatestAlarm()", "Getting latest alarm from database");

		// Check if there exists alarms in database
		if (db.getAlarmsCount() > 0) {
			// Some logging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getLatestAlarm()", "Database is not empty, continue retrieving alarm");
			// To store latest alarm into
			Alarm alarm = db.getLatestAlarm();
			// To build up string into
			StringBuilder alarmInfo = new StringBuilder();
			StringBuilder alarmMessage = new StringBuilder();

			// Sanity check to see whether alarm object is empty or not
			if (!alarm.isEmpty()) {
				// Some logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getLatestAlarm()", "Retrieved alarm object wasn't empty, start build up alarm string");

				// Build up the string representing the latest alarm from alarm object
				alarmInfo.append(context.getString(R.string.HTML_WIDGET_RECEIVED));
				alarmInfo.append(context.getString(R.string.COLON));
				alarmInfo.append(alarm.getReceived());
				alarmInfo.append(context.getString(R.string.NEW_LINE));

				alarmInfo.append(context.getString(R.string.HTML_WIDGET_SENDER));
				alarmInfo.append(context.getString(R.string.COLON));
				alarmInfo.append(alarm.getSender());
				alarmInfo.append(context.getString(R.string.NEW_LINE));
				
				alarmInfo.append(context.getString(R.string.HTML_WIDGET_TRIGGER_TEXT));
				alarmInfo.append(context.getString(R.string.COLON));
				alarmInfo.append(alarm.getTriggerText());
				alarmInfo.append(context.getString(R.string.NEW_LINE));
				
				// Build up the alarm message in separate stringbuilder so we can shorten it if we need
				alarmMessage.append(context.getString(R.string.HTML_WIDGET_LARM));
				alarmMessage.append(context.getString(R.string.COLON));
				alarmMessage.append(alarm.getMessage());
				
				// Check if alarm message is longer than the limits for the textview
				if (alarmMessage.length() > ALARM_TEXT_MAX_LENGTH) {
					// Some logging
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getLatestAlarm()", "Lates Alarm message is longer than " + Integer.toString(ALARM_TEXT_MAX_LENGTH) + " characters, message is shortened");
					
					// If longer than textview limit shorten it of and add dots to it
					alarmMessage.substring(0, (ALARM_TEXT_MAX_LENGTH - 3));
					alarmMessage.append("...");
				}
				
				alarmInfo.append(alarmMessage.toString());
				alarmInfo.append(context.getString(R.string.NEW_LINE));

				alarmInfo.append(context.getString(R.string.HTML_WIDGET_ACK));
				alarmInfo.append(context.getString(R.string.COLON));
				alarmInfo.append(alarm.getAcknowledged());

				// Return latest alarm as string
				return alarmInfo.toString();
			} else {
				// Some logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getLatestAlarm()", "Retrieved alarm object was empty");
				// An error occurred while retrieving alarm from database,
				// return error message
				return context.getString(R.string.ERROR_RETRIEVING_FROM_DB);
			}
		} else {
			// Some logging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getLatestAlarm()", "No alarms exists in database");
			// No alarms exists in database, return appropriate string
			return context.getString(R.string.NO_RECEIVED_ALARMS_EXISTS);
		}
	}

	/**
	 * Method used to get all shared preferences needed by class WidgetProvider
	 * 
	 * @param context
	 *            Context
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 *      logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String,
	 *      String, Throwable) logCatTxt(LogPriorities, String, String,
	 *      Throwable)
	 * @see ax.ha.it.smsalarm.PreferencesHandler#getPrefs(PrefKeys, PrefKeys,
	 *      DataTypes, Context) getPrefs(PrefKeys, PrefKeys, DataTypes, Context)
	 * @see ax.ha.it.smsalarm.PreferencesHandler#getPrefs(PrefKeys, PrefKeys,
	 *      DataTypes, Context, Object) getPrefs(PrefKeys, PrefKeys, DataTypes,
	 *      Context, Object)
	 */
	private void getWidgetPrefs(Context context) {
		// Some logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getWidgetPrefs()", "Start retrieving shared preferences needed by class " + getClass().getSimpleName());

		try {
			// Get shared preferences needed by WidgetProvider
			enableSmsAlarm = (Boolean) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.ENABLE_SMS_ALARM_KEY, DataTypes.BOOLEAN, context, true);
			useOsSoundSettings = (Boolean) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.USE_OS_SOUND_SETTINGS_KEY, DataTypes.BOOLEAN, context);
			endUserLicenseAgreed = (Boolean) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.END_USER_LICENSE_AGREED, DataTypes.BOOLEAN, context, false);
		} catch (IllegalArgumentException e) {
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":getWidgetPrefs()", "An unsupported datatype was given as argument to PreferencesHandler.getPrefs()", e);
		}

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getWidgetPrefs()", "Shared preferences retrieved");
	}

	/**
	 * To set enable sms alarm preference to Shared Preferences.
	 * 
	 * @param context Context
	 * @param enabled Boolean indicating whether or not Sms Alarm is enabled
	 * 
	 * @see #setUseOsSoundSettingsPref(Context, boolean)
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 * 		ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context)
	 * 		ax.ha.it.smsalarm.PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context)
	 */
	private void setEnableSmsAlarmPref(Context context, boolean enabled) {
		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setEnableSmsAlarmPref()", "Setting enable SmsAlarm to:\"" + enabled + "\"");

		try {
			prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.ENABLE_SMS_ALARM_KEY, enabled, context);
		} catch (IllegalArgumentException e) {
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":setEnableSmsAlarmPref()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
		}
	}
	
	/**
	 * To set use operating systems sound settings preference to Shared Preferences.
	 * 
	 * @param context Context
	 * @param enabled Boolean indicating whether or not Sms Alarm should use the 
	 *		  operating systems sound settings is enabled
	 * 
	 * @see #setUseOsSoundSettingsPref(Context, boolean)
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 * 		ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context)
	 * 		ax.ha.it.smsalarm.PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context)
	 */
	private void setUseOsSoundSettingsPref(Context context, boolean enabled) {
		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setUseOsSoundSettingsPref()", "Setting use OS sound settings to:\"" + enabled + "\"");

		try {
			prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.USE_OS_SOUND_SETTINGS_KEY, enabled, context);
		} catch (IllegalArgumentException e) {
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":setUseOsSoundSettingsPref()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
		}
	}

	/**
	 * To update all <code>Widget</code>'s associated to this application.
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 *      logCat(LogPriorities, String, String)
	 */
	public static void updateWidgets(Context context) {
		// Object needed for logging
		LogHandler logger = LogHandler.getInstance();

		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, WidgetProvider.class.getSimpleName() + ":updateWidgets()", "Updating widgets");

		// Create intent from WidgetProvider and set action to update widget
		Intent intent = new Intent(context, WidgetProvider.class);
		intent.setAction(WidgetProvider.UPDATE_WIDGETS);

		// Send the broadcast
		context.sendBroadcast(intent);
	}
}
