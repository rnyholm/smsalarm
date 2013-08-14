/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import ax.ha.it.smsalarm.LogHandler.LogPriorities;
import ax.ha.it.smsalarm.PreferencesHandler.DataTypes;
import ax.ha.it.smsalarm.PreferencesHandler.PrefKeys;

/**
 * Provider class for the app widgets. This class is responsible for all updates,
 * data population, data presentation and so on for a widget.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.1
 * @since 2.1
 * @date 2013-08-14
 * 
 * @see #onCreate(Bundle)
 */
public class WidgetProvider extends AppWidgetProvider {
	// Log tag string
	private String LOG_TAG = this.getClass().getSimpleName();
	
	// Objects needed for logging and shared preferences
	private LogHandler logger = LogHandler.getInstance();
	private PreferencesHandler prefHandler = PreferencesHandler.getInstance();
	
	// Object to handle database access and methods
	private DatabaseHandler db;
	
	// Strings representing different intents used to run different methods from intent
	public static final String TOGGLE_ENABLE_SMS_ALARM = "ax.ha.it.smsalarm.TOGGLE_SMS_ALARM_ENABLE";	
	public static final String TOGGLE_USE_OS_SOUND_SETTINGS = "ax.ha.it.smsalarm.TOGGLE_USE_OS_SOUND_SETTINGS";	
	public static final String SHOW_RECEIVED_ALARMS = "ax.ha.it.smsalarm.SHOW_RECEIVED_ALARMS";
	public static final String UPDATE_WIDGETS = "ax.ha.it.smsalarm.UPDATE_WIDGETS";
	
    // Some booleans for retrieving preferences into
    private boolean useOsSoundSettings = false;
    private boolean enableSmsAlarm = true;
	private boolean endUserLicenseAgreed = false;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// Get AppWidgetManager from context
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		
		if (intent.getAction().equals(TOGGLE_ENABLE_SMS_ALARM)) {
			// Some logging for information and debugging
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onReceive()", "Received intent:\"" + TOGGLE_ENABLE_SMS_ALARM + "\"");		
			if (this.enableSmsAlarm) {
				this.setEnableSmsAlarmPref(context, false);
			} else {
				this.setEnableSmsAlarmPref(context, true);
			}
			WidgetProvider.updateWidgets(context);
		} else if (intent.getAction().equals(TOGGLE_USE_OS_SOUND_SETTINGS)) {
			// Some logging for information and debugging
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onReceive()", "Received intent:\"" + TOGGLE_USE_OS_SOUND_SETTINGS + "\"");		
		} else if (intent.getAction().equals(SHOW_RECEIVED_ALARMS)) {
			// Some logging for information and debugging
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onReceive()", "Received intent:\"" + SHOW_RECEIVED_ALARMS + "\"");		
		} else if (intent.getAction().equals(UPDATE_WIDGETS)) {
			// Some logging for information and debugging
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onReceive()", "Received intent:\"" + UPDATE_WIDGETS + "\"");
			this.onUpdate(context, manager, AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context.getPackageName(), getClass().getName())));
		}
		
		super.onReceive(context, intent);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// Some logging for information and debugging
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onUpdate()", "Start updating widget");
		
		// Get Shared preferences needed by widget
    	this.getWidgetPrefs(context);
		// Initialize database handler object from context
		this.db = new DatabaseHandler(context);
		// RemoteViews object needed to configure layout of widget
		RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);
		
        // Update each of the app widgets with the remote adapter
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
        	this.setWidgetTextViews(rv, context);
        	
        	// Set onclick pending intent to start Sms Alarm, this is always set
        	rv.setOnClickPendingIntent(R.id.widget_logo_iv, smsAlarmPendingIntent);
        	
        	// If user has agreed the end user licens, set the rest of the on click pending intens also
        	if (this.endUserLicenseAgreed) {        	
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
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onUpdate()", "Widget has been updated");
	}
	
	/**
	 * To set appropriate text's to the different <code>TextView</code>'s. The text's are set depending
	 * if user has agreed the end user license, and if any <code>Alarm</code>'s has been received, status
	 * of SmsAlarm(enabled/disabled) and status of use devices sound settings(enabled/disabled).
	 * 
	 * @param rv <code>RemoteViews</code> that texts are set to
	 * @param context <code>Context</code> from which resources are retrieved
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see #getLatestAlarm(Context)
	 */
	private void setWidgetTextViews(RemoteViews rv, Context context) {
		// Some logging
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":setWidgetTextViews()", "TextViews of widget are about to be set");
		
		// If user has agreed end user license, we fill in the textviews with "real" data
		if (this.endUserLicenseAgreed) {
			// Check if Sms Alarm is enabled or not and set textview depending on that
			if (this.enableSmsAlarm) {
				rv.setTextViewText(R.id.widget_smsalarm_status_tv, context.getString(R.string.SMS_ALARM_STATUS_ENABLED));
			} else {
				rv.setTextViewText(R.id.widget_smsalarm_status_tv, context.getString(R.string.SMS_ALARM_STATUS_DISABLED));
			}
			
			// Check if use devices sound settings is enabled or not and set textview depending on that
			if (this.useOsSoundSettings) {
				rv.setTextViewText(R.id.widget_soundsettings_status_tv, context.getString(R.string.SOUND_SETTINGS_STATUS_ENABLED));
			} else {
				rv.setTextViewText(R.id.widget_soundsettings_status_tv, context.getString(R.string.SOUND_SETTINGS_STATUS_DISABLED));
			}
			
			// Get latest alarm as string and set it to textview
			rv.setTextViewText(R.id.widget_latest_received_alarm_tv, this.getLatestAlarm(context));
			
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
	 * To get the latest <code>Alarm</code> from database as <code>String</code>. If no <code>Alarm</code> exist
	 * or the <code>Alarm</code> is empty an appropriate <code>String</code> is returned instead.
	 * 
	 * @param context Context from which resources are retrieved
	 * @return String with appropriate text depending on if any alarms exists or not in database
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.Alarm ax.ha.it.smsalarm.Alarm
	 */
	private String getLatestAlarm(Context context) {	
		// Some logging
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":getLatestAlarm()", "Getting latest alarm from database");
		
		// Check if there exists alarms in database
		if (this.db.getAlarmsCount() > 0) {
			// Some logging
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":getLatestAlarm()", "Database is not empty, continue retrieving alarm");
			// To store latest alarm into
			Alarm alarm = db.getLatestAlarm();
			// To build upp string into
			StringBuilder sb  = new StringBuilder();
			
			// Sanity check to see whether alarm object is empty or not
			if (!alarm.isEmpty()) {
				// Some logging
				this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":getLatestAlarm()", "Retrieved alarm object wasn't empty, start build up alarm string");
				
				// Build up the string representing the latest alarm from alarm object
				sb.append(context.getString(R.string.HTML_WIDGET_RECEIVED));
				sb.append(context.getString(R.string.COLON));
				sb.append(alarm.getReceived());
				sb.append(context.getString(R.string.NEW_LINE));
				
				sb.append(context.getString(R.string.HTML_WIDGET_SENDER));
				sb.append(context.getString(R.string.COLON));
				sb.append(alarm.getSender());
				sb.append(context.getString(R.string.NEW_LINE));

				sb.append(context.getString(R.string.HTML_WIDGET_LARM));
				sb.append(context.getString(R.string.COLON));
				sb.append(alarm.getMessage());
				sb.append(context.getString(R.string.NEW_LINE));
				
				sb.append(context.getString(R.string.HTML_WIDGET_ACK));
				sb.append(context.getString(R.string.COLON));
				sb.append(alarm.getAcknowledged());
				
				// Return latest alarm as string
				return sb.toString();
			} else {
				// Some logging
				this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":getLatestAlarm()", "Retrieved alarm object was empty");
				// An error occurred while retrieving alarm from database, return error message
				return context.getString(R.string.ERROR_RETRIEVING_FROM_DB);
			}
		} else {
			// Some logging
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":getLatestAlarm()", "No alarms exists in database");
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
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String, String, Throwable) logCatTxt(LogPriorities, String, String, Throwable)
	 * @see ax.ha.it.smsalarm.PreferencesHandler#getPrefs(PrefKeys, PrefKeys, DataTypes, Context) getPrefs(PrefKeys, PrefKeys, DataTypes, Context)
	 * @see ax.ha.it.smsalarm.PreferencesHandler#getPrefs(PrefKeys, PrefKeys, DataTypes, Context, Object) getPrefs(PrefKeys, PrefKeys, DataTypes, Context, Object)
	 */
	private void getWidgetPrefs(Context context) {
		// Some logging
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":getWidgetPrefs()", "Start retrieving shared preferences needed by class " + this.getClass().getSimpleName());
		
		try {
			// Get shared preferences needed by WidgetProvider
			this.enableSmsAlarm = (Boolean) this.prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.ENABLE_SMS_ALARM_KEY, DataTypes.BOOLEAN, context, true);
			this.useOsSoundSettings = (Boolean) this.prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.USE_OS_SOUND_SETTINGS_KEY, DataTypes.BOOLEAN, context);
			this.endUserLicenseAgreed = (Boolean) this.prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.END_USER_LICENSE_AGREED, DataTypes.BOOLEAN, context, false);
		} catch(IllegalArgumentException e) {
			logger.logCatTxt(LogPriorities.ERROR, this.LOG_TAG + ":getWidgetPrefs()", "An unsupported datatype was given as argument to PreferencesHandler.getPrefs()", e);
		} 

		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":getWidgetPrefs()", "Shared preferences retrieved");
	}
	
	private void setEnableSmsAlarmPref(Context context, boolean enabled) {
		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setEnableSmsAlarmPref()", "Setting enable SmsAlarm to:\"" + enabled + "\""); 
		
		try {
			this.prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.ENABLE_SMS_ALARM_KEY, enabled, context);
		} catch(IllegalArgumentException e) {
			this.logger.logCatTxt(LogPriorities.ERROR, this.LOG_TAG + ":setEnableSmsAlarmPref()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
		} 
	}
	
    /**
     * To update all <code>Widget</code>'s associated to this application.
     * 
     * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
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
