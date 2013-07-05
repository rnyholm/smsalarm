/*
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
 * @version 2.0
 * @since 0.9beta
 * @date 2013-07-04
 */
public class NotificationHelper extends IntentService {

	// Log tag string
	private final String LOG_TAG = "NotificationHelper";

	// Objects needed for logging and shared preferences handling
	private LogHandler logger = LogHandler.getInstance();
	private PreferencesHandler prefHandler = PreferencesHandler.getInstance();

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
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":NotificationHelper()", "NotificationHelper constructor called");
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
	@Override
	protected void onHandleIntent(Intent i) {
		// Log information
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onHandleIntent()", "Start retrieving shared preferences needed by class NotificationHelper");
		
		// To store alarm type and message in
		String larmType = "";
		String message = "";
		
		try {
			// Get some values from the sharedprefs
			message = (String) this.prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.MESSAGE_KEY, DataTypes.STRING, this);
			larmType = (String) this.prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.LARM_TYPE_KEY, DataTypes.STRING, this);
		} catch(IllegalArgumentException e) {
			logger.logCatTxt(LogPriorities.ERROR, this.LOG_TAG + ":onHandleIntent()", "An unsupported datatype was given as argument to PreferencesHandler.getPrefs()", e);
		} 			

		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onHandleIntent()", "Shared preferences retrieved");

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

		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onHandleIntent()", "Intent has been set");

		// Setup a notification, directly from android development site
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// Variables for notifications text and icon
		CharSequence tickerText = "";
		CharSequence contentTitle = "";
		int icon = 0;

		// To get a unique refresh id for the intents
		long REFRESH_ID = System.currentTimeMillis();
		long when = System.currentTimeMillis();

		// Configure notification depending on type
		if ("primary".equals(larmType)) {
			// Set icon
			icon = android.R.drawable.ic_delete;
			// Set ticker text
			tickerText = this.getString(R.string.alarm);
			// Set content title
			contentTitle = this.getString(R.string.alarm);
			// Log
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onHandleIntent()", "Notification has been set for a primary alarm");
		} else if ("secondary".equals(larmType)) {
			icon = android.R.drawable.ic_menu_close_clear_cancel;
			tickerText = this.getString(R.string.secondaryAlarm);
			contentTitle = this.getString(R.string.secondaryAlarm);
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onHandleIntent()", "Notification has been set for a secondary alarm");
		} else { // <--If this happens, something really weird is going on
			this.logger.logCatTxt(LogPriorities.ERROR, this.LOG_TAG + ":onHandleIntent()", "Alarm type couldn't be find when configuring notification");
		}

		// Create notification
		Notification notification = new Notification(icon, tickerText, when);

		// Get application context
		Context context = getApplicationContext();

		// Setup message and pending intent
		CharSequence contentText = message;
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		// This flag auto cancels the notification when clicked
		notification.flags = Notification.FLAG_AUTO_CANCEL;

		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onHandleIntent()", "Notification and it's intent has been configured and are ready to be shown");

		// Show the notification
		mNotificationManager.notify((int) REFRESH_ID, notification);
	}
}