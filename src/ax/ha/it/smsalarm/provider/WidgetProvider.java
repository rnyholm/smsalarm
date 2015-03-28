/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.provider;

import java.io.File;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.RemoteViews;
import android.widget.TextView;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.activity.Splash;
import ax.ha.it.smsalarm.alarm.Alarm;
import ax.ha.it.smsalarm.handler.DatabaseHandler;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.DataType;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.PrefKey;
import ax.ha.it.smsalarm.util.AlarmLogger;

/**
 * Provider class for the application widgets. This class is responsible for all updates, data population, data presentation and so on for a widget.<br>
 * This implementation should be safe with more than one instances of the Sms Alarm widget.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.1
 */
public class WidgetProvider extends AppWidgetProvider {
	// To get access to shared preferences and database
	private final SharedPreferencesHandler prefHandler = SharedPreferencesHandler.getInstance();
	private DatabaseHandler db;

	// Max length of the latest alarm length in widget
	private static final int ALARM_TEXT_MAX_LENGTH = 100;

	// Strings representing different intents used to run different methods from intent
	private static final String TOGGLE_ENABLE_SMS_ALARM = "ax.ha.it.smsalarm.TOGGLE_SMS_ALARM_ENABLE";
	private static final String TOGGLE_USE_OS_SOUND_SETTINGS = "ax.ha.it.smsalarm.TOGGLE_USE_OS_SOUND_SETTINGS";
	private static final String SHOW_RECEIVED_ALARMS = "ax.ha.it.smsalarm.SHOW_RECEIVED_ALARMS";
	private static final String UPDATE_WIDGETS = "ax.ha.it.smsalarm.UPDATE_WIDGETS";

	// Some booleans for retrieving preferences into
	private boolean useOsSoundSettings = false;
	private boolean enableSmsAlarm = false;
	private boolean endUserLicenseAgreed = false;

	/**
	 * To receive intents broadcasted throughout the operating system. If it receives any intents that it listens on the method takes proper action.<br>
	 * The method listens on following intents:
	 * <ul>
	 * <li>ax.ha.it.smsalarm.TOGGLE_SMS_ALARM_ENABLE</li>
	 * <li>ax.ha.it.smsalarm.TOGGLE_USE_OS_SOUND_SETTINGS</li>
	 * <li>ax.ha.it.smsalarm.SHOW_RECEIVED_ALARMS</li>
	 * <li>ax.ha.it.smsalarm.UPDATE_WIDGETS</li>
	 * </ul>
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		// Get AppWidgetManager from context
		AppWidgetManager manager = AppWidgetManager.getInstance(context);

		// Get Shared preferences needed by widget
		fetchSharedPrefs(context);

		// If statements to "catch" intent we looking for
		if (TOGGLE_ENABLE_SMS_ALARM.equals(intent.getAction())) {
			// Set shared preferences depending on current preferences
			if (enableSmsAlarm) {
				setEnableSmsAlarmPref(context, false);
			} else {
				setEnableSmsAlarmPref(context, true);
			}

			// Update widget
			WidgetProvider.updateWidgets(context);
		} else if (TOGGLE_USE_OS_SOUND_SETTINGS.equals(intent.getAction())) {
			if (useOsSoundSettings) {
				setUseOsSoundSettingsPref(context, false);
			} else {
				setUseOsSoundSettingsPref(context, true);
			}

			WidgetProvider.updateWidgets(context);
		} else if (SHOW_RECEIVED_ALARMS.equals(intent.getAction())) {
			// Get full file path to alarm log file
			String alarmLogFilePath = AlarmLogger.getInstance().getAlarmLogPath();

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

			// Set data and type, in this case alarm log file and HTML
			showReceivedAlarmsIntent.setDataAndType(Uri.fromFile(file), type);

			// Start new activity from context
			context.startActivity(showReceivedAlarmsIntent);
		} else if (UPDATE_WIDGETS.equals(intent.getAction())) {
			// Call onUpdate to update the widget instances
			onUpdate(context, manager, AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context.getPackageName(), getClass().getName())));
		}

		// Needs to call superclass's onReceive
		super.onReceive(context, intent);
	}

	/**
	 * To update all instances of the Sms Alarm widget.
	 */
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// Get Shared preferences needed by widget
		fetchSharedPrefs(context);
		// Initialize database handler object from context
		db = new DatabaseHandler(context);
		// RemoteViews object needed to configure layout of widget
		RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);

		// Update each of the application widgets with the remote adapter
		for (int i = 0; i < appWidgetIds.length; ++i) {
			// Set intent to start Sms Alarm and wrap it into a pending intent, rest of the intents
			// are configured in the same way
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

			// Set onClick pending intent to start Sms Alarm, this is always set
			rv.setOnClickPendingIntent(R.id.widget_logo_iv, smsAlarmPendingIntent);

			// If user has agreed the end user license, set the rest of the on click pending intents
			// also
			if (endUserLicenseAgreed) {
				rv.setOnClickPendingIntent(R.id.widget_smsalarm_status_tv, enableSmsAlarmPendingIntent);
				rv.setOnClickPendingIntent(R.id.widget_soundsettings_status_tv, useOsSoundSettingsPendingIntent);
				rv.setOnClickPendingIntent(R.id.widget_latest_received_alarm_tv, showReceivedAlarmsPendingIntent);
			}

			// Update widget
			appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
		}

		// Call to super class onUpdate method, so the Operating System can run it's native methods
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	/**
	 * To set appropriate text's to the different {@link TextView}'s. The text's are set depending if user has agreed the end user license, and if any
	 * {@link Alarm}'s has been received, status of SmsAlarm(enabled/disabled) and status of use devices sound settings(enabled/disabled).
	 * 
	 * @param rv
	 *            {@link RemoteViews} that texts should be set to.
	 * @param context
	 *            The Context in which the provider is running.
	 */
	private void setWidgetTextViews(RemoteViews rv, Context context) {
		// If user has agreed end user license, we fill in the TextViews with "real" data
		if (endUserLicenseAgreed) {
			// Check if Sms Alarm is enabled or not and set TextView depending on that
			if (enableSmsAlarm) {
				rv.setTextViewText(R.id.widget_smsalarm_status_tv, context.getString(R.string.SMS_ALARM_STATUS_ENABLED));
			} else {
				rv.setTextViewText(R.id.widget_smsalarm_status_tv, context.getString(R.string.SMS_ALARM_STATUS_DISABLED));
			}

			// Check if use devices sound settings is enabled or not and set TextView depending on that
			if (useOsSoundSettings) {
				rv.setTextViewText(R.id.widget_soundsettings_status_tv, context.getString(R.string.SOUND_SETTINGS_STATUS_ENABLED));
			} else {
				rv.setTextViewText(R.id.widget_soundsettings_status_tv, context.getString(R.string.SOUND_SETTINGS_STATUS_DISABLED));
			}

			// Set the shortened alarm to TextView
			rv.setTextViewText(R.id.widget_latest_received_alarm_tv, getLatestAlarm(context));

			// Set correct dividers to widget
			rv.setImageViewResource(R.id.widget_divider2_iv, R.drawable.gradient_divider_widget);
			rv.setImageViewResource(R.id.widget_divider3_iv, R.drawable.gradient_divider_widget);
		} else { // User has not agreed end user license, hide dividers and TextView, set text to one TextView telling user what's wrong
			rv.setTextViewText(R.id.widget_smsalarm_status_tv, context.getString(R.string.WIDGET_NOT_AGREED_EULA));
			rv.setImageViewResource(R.id.widget_divider2_iv, android.R.color.transparent);
			rv.setTextViewText(R.id.widget_soundsettings_status_tv, context.getString(R.string.EMPTY_STRING));
			rv.setImageViewResource(R.id.widget_divider3_iv, android.R.color.transparent);
			rv.setTextViewText(R.id.widget_latest_received_alarm_tv, context.getString(R.string.EMPTY_STRING));
		}
	}

	/**
	 * To get the latest {@link Alarm} from database as a <code>String</code>. If no <code>Alarm</code> exist or the <code>Alarm</code> is empty an
	 * appropriate <code>String</code> is returned instead.
	 * 
	 * @param context
	 *            The Context in which the provider is running.
	 * @return String with appropriate text depending on if any alarms exists or not in database.
	 */
	private String getLatestAlarm(Context context) {
		// Check if there exists alarms in database
		if (db.getAlarmsCount() > 0) {
			// To store latest alarm into
			Alarm alarm = db.fetchLatestAlarm();
			// To build up string into
			StringBuilder alarmInfo = new StringBuilder();
			StringBuilder alarmMessage = new StringBuilder();

			// Sanity check to see whether alarm holds valid info or not
			if (alarm.holdsValidInfo()) {
				// Build up the string representing the latest alarm from alarm object
				alarmInfo.append(context.getString(R.string.TITLE_ALARM_INFO_RECEIVED));
				alarmInfo.append(context.getString(R.string.COLON));
				alarmInfo.append(alarm.getReceivedLocalized());
				alarmInfo.append(context.getString(R.string.NEW_LINE));

				alarmInfo.append(context.getString(R.string.TITLE_ALARM_INFO_SENDER));
				alarmInfo.append(context.getString(R.string.COLON));
				alarmInfo.append(alarm.getSender());
				alarmInfo.append(context.getString(R.string.NEW_LINE));

				alarmInfo.append(context.getString(R.string.TITLE_ALARM_INFO_TRIGGER_TEXT));
				alarmInfo.append(context.getString(R.string.COLON));
				alarmInfo.append(alarm.getTriggerText());
				alarmInfo.append(context.getString(R.string.NEW_LINE));

				// Build up the alarm message in separate StringBuilder so we can shorten it if we need
				alarmMessage.append(context.getString(R.string.TITLE_ALARM_INFO_MESSAGE));
				alarmMessage.append(context.getString(R.string.COLON));
				alarmMessage.append(alarm.getMessage());

				// Check if alarm message is longer than the limits for the TextView
				if (alarmMessage.length() > ALARM_TEXT_MAX_LENGTH) {
					// If longer than TextView limit shorten it of and add dots to it
					alarmMessage = new StringBuilder(alarmMessage.substring(0, (ALARM_TEXT_MAX_LENGTH - 3)));
					alarmMessage.append("...");
				}

				alarmInfo.append(alarmMessage.toString());
				alarmInfo.append(context.getString(R.string.NEW_LINE));

				alarmInfo.append(context.getString(R.string.TITLE_ALARM_INFO_ACKNOWLEDGED));
				alarmInfo.append(context.getString(R.string.COLON));
				alarmInfo.append(alarm.getAcknowledgedLocalized());

				// Return latest alarm as string
				return alarmInfo.toString();
			} else {
				// An error occurred while retrieving alarm from database, return error message
				return context.getString(R.string.ERROR_RETRIEVING_FROM_DB);
			}
		} else {
			// No alarms exists in database, return appropriate string
			return context.getString(R.string.NO_RECEIVED_ALARMS_EXISTS);
		}
	}

	/**
	 * To fetch all {@link SharedPreferences} used by {@link WidgetProvider} class.
	 * 
	 * @param context
	 *            The Context in which the provider is running.
	 */
	private void fetchSharedPrefs(Context context) {
		// Get shared preferences needed by WidgetProvider
		enableSmsAlarm = (Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.ENABLE_SMS_ALARM_KEY, DataType.BOOLEAN, context, true);
		useOsSoundSettings = (Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.USE_OS_SOUND_SETTINGS_KEY, DataType.BOOLEAN, context);
		endUserLicenseAgreed = (Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.END_USER_LICENSE_AGREED, DataType.BOOLEAN, context, false);
	}

	/**
	 * To set enable Sms Alarm setting to {@link SharedPreferences}.
	 * 
	 * @param context
	 *            The Context in which the provider is running.
	 * @param enabled
	 *            Boolean indicating whether or not Sms Alarm is enabled.
	 */
	private void setEnableSmsAlarmPref(Context context, boolean enabled) {
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.ENABLE_SMS_ALARM_KEY, enabled, context);
	}

	/**
	 * To set use operating systems sound settings to {@link SharedPreferences}.
	 * 
	 * @param context
	 *            The Context in which the provider is running.
	 * @param enabled
	 *            Boolean indicating whether or not Sms Alarm should use the operating systems sound settings or not.
	 */
	private void setUseOsSoundSettingsPref(Context context, boolean enabled) {
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.USE_OS_SOUND_SETTINGS_KEY, enabled, context);
	}

	/**
	 * To update all <code>Widget</code>'s associated to Sms Alarm.
	 */
	public static void updateWidgets(Context context) {
		// Create intent from WidgetProvider and set action to update widget
		Intent intent = new Intent(context, WidgetProvider.class);
		intent.setAction(WidgetProvider.UPDATE_WIDGETS);

		// Send the broadcast
		context.sendBroadcast(intent);
	}
}
