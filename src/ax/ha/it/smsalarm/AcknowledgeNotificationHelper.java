/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import ax.ha.it.smsalarm.activity.Acknowledge;
import ax.ha.it.smsalarm.handler.PreferencesHandler;
import ax.ha.it.smsalarm.handler.PreferencesHandler.DataType;
import ax.ha.it.smsalarm.handler.PreferencesHandler.PrefKey;

/**
 * Helper to build up and show {@link Notification}, also creates {@link PendingIntent}'s for the <code>notification</code>.<br>
 * <b><i>NOTE. Contains some deprecated functionality, this is to support <code>Android SDK</code> versions below 11.</b></i>
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 1.2.1-SE
 */
public class AcknowledgeNotificationHelper extends IntentService {
	private final PreferencesHandler prefHandler = PreferencesHandler.getInstance();

	/**
	 * To create a new instance of {@link AcknowledgeNotificationHelper}.<br>
	 * A constructor must be implemented and call it's <code>superclass</code>, {@link IntentService}, constructor with an <b><i>arbitrary</i></b>
	 * <code>String</code> as argument.
	 */
	public AcknowledgeNotificationHelper() {
		// Note: MUST call super() constructor with an arbitrary string
		super("AcknowledgeNotificationHelper");
	}

	/**
	 * To handle {@link Intent}, builds up and dispatches a {@link Notification}. Contains some deprecated functionality just to support
	 * <code>Android SDK</code> versions below 11.
	 * 
	 * @param i
	 *            Intent for notification
	 * @deprecated
	 */
	@SuppressLint("DefaultLocale")
	@Deprecated
	@Override
	protected void onHandleIntent(Intent i) {
		// Fetch some values from the shared preferences
		String contentText = (String) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.MESSAGE_KEY, DataType.STRING, this);
		String rescueService = (String) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.RESCUE_SERVICE_KEY, DataType.STRING, this);

		// Set intent to AcknowledgeHandler
		Intent notificationIntent = new Intent(this, Acknowledge.class);

		// Setup a notification, directly from Android developer site
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// To get a unique refresh id for the intents, to avoid notifications from writing over each other
		long REFRESH_ID = System.currentTimeMillis();
		long when = System.currentTimeMillis();

		// Resolve ticker text
		String tickerText = "";
		if (!"".equals(rescueService)) {
			tickerText = rescueService + " " + getString(R.string.PRIMARY_ALARM);
		} else {
			tickerText = getString(R.string.PRIMARY_ALARM);
		}

		// Create notification
		Notification notification = new Notification(android.R.drawable.ic_delete, tickerText, when);

		// Setup message and pending intent
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(getApplicationContext(), getString(R.string.PRIMARY_ALARM), contentText, contentIntent);

		// This flag auto cancels the notification when clicked and indicating that devices LED should light up
		notification.flags = Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL;
		// Configure LED
		// @formatter:off
		notification.ledARGB = 0xFFff0000; 	// Red
		notification.ledOnMS = 100; 		// On time
		notification.ledOffMS = 100;		// Off time
		// @formatter:on

		// Dispatch the notification
		notificationManager.notify((int) REFRESH_ID, notification);
	}
}