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

/**
 * Helper class to build up and show notifications, also creates
 * <code>pending intent</code> for the <code>notification</code>. Contains some
 * deprecated functionality, this is to support <code>Android SDK</code>
 * versions below 11.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.0
 * @since 0.9beta
 * @date 2013-04-22
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
	 */
	public NotificationHelper() {
		// Note: MUST call the super() constructor with an (arbitrary) string
		super("NotificationHelper");

		// Log message for debugging/information purpose
		this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG
				+ ":NotificationHelper()",
				"NotificationHelper constructor called");
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
	 * @see #NotificationHelper()
	 */
	@Override
	protected void onHandleIntent(Intent i) {
		// Log information
		this.logger
				.logCatTxt(this.logger.getINFO(), this.LOG_TAG
						+ ":onHandleIntent()",
						"Start retrieving shared preferences needed by class NotificationHelper");

		// Get some values from the sharedprefs
		String message = (String) this.prefHandler.getPrefs(
				this.prefHandler.getSHARED_PREF(),
				this.prefHandler.getMESSAGE_KEY(), 1, this);
		String larmType = (String) this.prefHandler.getPrefs(
				this.prefHandler.getSHARED_PREF(),
				this.prefHandler.getLARM_TYPE_KEY(), 1, this);

		this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG
				+ ":onHandleIntent()", "Shared preferences retrieved");

		/*
		 * This string and intent opens the messaging directory on phone,
		 * however due to this page;
		 * http://stackoverflow.com/questions/3708737/go-to-inbox-in-android
		 * fetched 21.10-11, thisway of achieve the "go to messaging dir" is
		 * highly unrecommended.Thats because this method uses undocumented API
		 * and is not part of the Android core.This may or may not work on some
		 * devices and versions!
		 */
		String SMS_MIME_TYPE = "vnd.android-dir/mms-sms";
		Intent notificationIntent = new Intent(Intent.ACTION_MAIN); // Don't
																	// want to
																	// start any
																	// activity!
		notificationIntent.setType(SMS_MIME_TYPE);

		this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG
				+ ":onHandleIntent()", "Intent has been set");

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
		if (larmType.equals("primary")) {
			// Set icon
			icon = android.R.drawable.ic_delete;
			// Set ticker text
			tickerText = this.getString(R.string.alarm);
			// Set content title
			contentTitle = this.getString(R.string.alarm);
			// Log
			this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG
					+ ":onHandleIntent()",
					"Notification has been set for a primary alarm");
		} else if (larmType.equals("secondary")) {
			icon = android.R.drawable.ic_menu_close_clear_cancel;
			tickerText = this.getString(R.string.secondaryAlarm);
			contentTitle = this.getString(R.string.secondaryAlarm);
			this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG
					+ ":onHandleIntent()",
					"Notification has been set for a secondary alarm");
		} else { // <--If this happens, something really weird is going on
			this.logger
					.logCatTxt(this.logger.getERROR(), this.LOG_TAG
							+ ":onHandleIntent()",
							"Alarm type couldn't be find when configuring notification");
		}

		// Create notification
		Notification notification = new Notification(icon, tickerText, when);

		// Get application context
		Context context = getApplicationContext();

		// Setup message and pending intent
		CharSequence contentText = message;
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);

		// This flag auto cancels the notification when clicked
		notification.flags = Notification.FLAG_AUTO_CANCEL;

		this.logger
				.logCatTxt(this.logger.getINFO(), this.LOG_TAG
						+ ":onHandleIntent()",
						"Notification and it's intent has been configured and are ready to be shown");

		// Show the notification
		mNotificationManager.notify((int) REFRESH_ID, notification);
	}
}