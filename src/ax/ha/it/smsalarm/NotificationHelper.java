/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import ax.ha.it.smsalarm.LogHandler.LogPriorities;
import ax.ha.it.smsalarm.PreferencesHandler.DataTypes;
import ax.ha.it.smsalarm.PreferencesHandler.PrefKeys;

/**
 * Helper class to build up and show notifications, also creates
 * <code>pending intent</code> for the <code>notification</code>. Contains some
 * deprecated functionality, this is to support <code>Android SDK</code>
 * versions below 11.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.2
 * @since 0.9beta
 */
public class NotificationHelper extends IntentService {

	// Log tag string
	private final String LOG_TAG = getClass().getSimpleName();

	// Objects needed for logging and shared preferences handling
	private final LogHandler logger = LogHandler.getInstance();
	private final PreferencesHandler prefHandler = PreferencesHandler.getInstance();
	
	// Variables for notifications text and icon
	private String tickerText = "";
	private String contentTitle = "";
	private String contentText = "";
	private int icon = 0;

	/**
	 * Mandatory constructor calling it's <code>super class</code>.
	 * 
	 * @see #onHandleIntent(Intent)
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogHandler.LogPriorities, String, String) logCat(LogHandler.LogPriorities, String, String)
	 */
	public NotificationHelper() {
		// Note: MUST call the super() constructor with an (arbitrary) string
		super("NotificationHelper");

		// Log message for debugging/information purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":NotificationHelper()", "NotificationHelper constructor called");
	}

	/**
	 * Overridden method to handle <code>intent</code>, build up and show
	 * <code>notification</code>. Contains some deprecated functionality just to
	 * support <code>Android SDK</code> versions below 11.
	 * 
	 * @param i
	 *            Intent for notification
	 * 
	 * @see #NotificationHelper()
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String, String) logCatTxt(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String, String, Throwable) logCatTxt(LogPriorities, String, String, Throwable)
	 * @see ax.ha.it.smsalarm.PreferencesHandler#getPrefs(PrefKeys, PrefKeys, DataTypes, Context) getPrefs(PrefKeys, PrefKeys, DataTypes, Context)
	 * 
	 * @deprecated 
	 */
	@Deprecated
	@Override
	protected void onHandleIntent(Intent i) {
		// Log information
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onHandleIntent()", "Start retrieving shared preferences needed by class NotificationHelper");
		
		// To store alarm type, message and rescue service in
		String message = "";
		String rescueService = "";
		AlarmTypes alarmType = AlarmTypes.UNDEFINED;
		
		try {
			// Get some values from the sharedprefs
			message = (String) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.MESSAGE_KEY, DataTypes.STRING, this);
			alarmType = AlarmTypes.of((Integer) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.LARM_TYPE_KEY, DataTypes.INTEGER, this));
			rescueService = (String) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.RESCUE_SERVICE_KEY, DataTypes.STRING, this);
		} catch(IllegalArgumentException e) {
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":onHandleIntent()", "An unsupported datatype was given as argument to PreferencesHandler.getPrefs()", e);
		} 			

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onHandleIntent()", "Shared preferences retrieved");

		/*
		 * This string and intent opens the messaging directory on phone,
		 * however due to this page;
		 * http://stackoverflow.com/questions/3708737/go-to-inbox-in-android
		 * fetched 21.10-11, this way of achieve the "go to messaging dir" is
		 * highly unrecommended.Thats because this method uses undocumented API
		 * and is not part of the Android core.This may or may not work on some
		 * devices and versions!
		 */
		String SMS_MIME_TYPE = "vnd.android-dir/mms-sms";
		Intent notificationIntent = new Intent(Intent.ACTION_MAIN); // Don't want to start any activity!
		notificationIntent.setType(SMS_MIME_TYPE);
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onHandleIntent()", "Intent has been set");

		// Setup a notification, directly from android development site
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// To get a unique refresh id for the intents
		long REFRESH_ID = System.currentTimeMillis();
		long when = System.currentTimeMillis();

		// Configure notification depending on type
		if (alarmType.equals(AlarmTypes.PRIMARY)) {
			// Set proper texts and icon to notification
			setNotificationTexts(android.R.drawable.ic_delete, getString(R.string.PRIMARY_ALARM), 
									  rescueService.toUpperCase(), getString(R.string.PRIMARY_ALARM), message);		
			// Log
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onHandleIntent()", "Notification has been set for a PRIMARY alarm");
		} else if (alarmType.equals(AlarmTypes.SECONDARY)) {			
			// Set proper texts and icon to notification
			setNotificationTexts(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.SECONDARY_ALARM), 
									  rescueService.toUpperCase(), getString(R.string.SECONDARY_ALARM), message);	
			// Log
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onHandleIntent()", "Notification has been set for a SECONDARY alarm");
		} else { // <--If this happens, something really weird is going on
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":onHandleIntent()", "Alarm type couldn't be find when configuring notification");
		}

		// Create notification
		Notification notification = new Notification(icon, tickerText, when);

		// Get application context
		Context context = getApplicationContext();

		// Setup message and pending intent
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		// This flag auto cancels the notification when clicked and indicating that devices LED should light up
		notification.flags = Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL;
		// Configurate LED
		notification.ledARGB = 0xFFff0000; 	// Red
		notification.ledOnMS = 100; 		// On time
		notification.ledOffMS = 100;		// Off time

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onHandleIntent()", "Notification and it's intent has been configured and are ready to be shown");

		// Show the notification
		mNotificationManager.notify((int) REFRESH_ID, notification);
	}
	
	/**
	 * To set texts and icon for a notification. The ticker text is built up 
	 * dynamically depending on argument <code>rescueService</code>
	 * 
	 * @param icon Icon as integer value, use <code>android.R.drawable.*</code>
	 * @param tickerText Notifications ticker text
	 * @param rescueService String that may or may not contain rescue service's name
	 * @param contentTitle Notification contents title
	 * @param contentText Notifications content
	 */
	private void setNotificationTexts(int icon, String tickerText, String rescueService, String contentTitle, String contentText) {
		// Log
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setNotificationTexts()", "Setting texts and icon to notification");
		// Set icon for notification
		this.icon = icon;
		// Set ticker text, with rescue service name if it exists
		if (!"".equals(rescueService)) {
			this.tickerText = rescueService + " " + tickerText;
		} else {
			this.tickerText = tickerText;
		}
		// Set content title
		this.contentTitle = contentTitle;	
		// Set message to notification
		this.contentText = contentText;
	}
}