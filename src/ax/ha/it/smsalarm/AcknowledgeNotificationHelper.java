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

/**
 * Helper class to build up and show notifications, also creates pending
 * <code>intents</code> for the <code>notification</code>. Contains some
 * deprecated functionality, this is to support <code>Android SDK</code>
 * versions below 11.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.1
 * @since 1.2.1-SE
 * @date 2013-06-30
 */
public class AcknowledgeNotificationHelper extends IntentService {

	// Log tag string
	private final String LOG_TAG = "AcknowledgeNotificationHelper";

	// Objects needed for logging and shared preferences handling
	private LogHandler logger = LogHandler.getInstance();
	private PreferencesHandler prefHandler = PreferencesHandler.getInstance();

	/**
	 * Mandatory constructor calling it's <code>super class</code>.
	 * 
	 * @see #onHandleIntent(Intent)
	 * @see {@link LogHandler#logCat(ax.ha.it.smsalarm.LogHandler.LogPriorities, String, String)}
	 */
	public AcknowledgeNotificationHelper() {
		// Note: MUST call the super() constructor with an (arbitrary) string
		super("AcknowledgeNotificationHelper");

		// Log message for debugging/information purpose
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":AcknowledgeNotificationHelper()", "NotificationHelper constructor called");
	}

	/**
	 * Overridden method to handle <code>intent</code>, build up and show
	 * <code>notification</code>. Contains some deprecated functionality just to
	 * support <code>Android SDK</code> versions below 11.
	 * 
	 * @param Intent
	 *            Intent for notification
	 * @Override
	 * @deprecated
	 * 
	 * @see #AcknowledgeNotificationHelper()
	 * @see {@link LogHandler#logCat(ax.ha.it.smsalarm.LogHandler.LogPriorities, String, String)}
	 * @see {@link PreferencesHandler#getPrefs(String, String, int, Context)}
	 * 
	 * @Override
	 */
	@Override
	protected void onHandleIntent(Intent i) {
		// Log information
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onHandleIntent()", "Start retrieving shared preferences needed by class AcknowledgeNotificationHelper");

		// Get some values from the sharedprefs
		String message = (String) this.prefHandler.getPrefs(this.prefHandler.getSHARED_PREF(), this.prefHandler.getMESSAGE_KEY(), 1, this);

		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onHandleIntent()", "Shared preferences retrieved");

		// Set intent to AcknowledgeHandler
		Intent notificationIntent = new Intent(this, AcknowledgeHandler.class);
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onHandleIntent()", "Intent has been set");

		// Setup a notification, directly from android development site
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// Variables for notifications text and icon
		CharSequence tickerText = this.getString(R.string.alarm);
		CharSequence contentTitle = this.getString(R.string.alarm);
		int icon = android.R.drawable.ic_delete;

		// Log
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onHandleIntent()", "Notification has been set for a primary alarm with acknowledgement");

		// To get a unique refresh id for the intents
		long REFRESH_ID = System.currentTimeMillis();
		long when = System.currentTimeMillis();

		// Create notification
		Notification notification = new Notification(icon, tickerText, when);

		// Get application context
		Context context = getApplicationContext();

		// Setup message and pendingintent
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