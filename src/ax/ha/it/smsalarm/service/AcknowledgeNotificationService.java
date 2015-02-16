/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.service;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.handler.FlashNotificationHandler;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.DataType;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.PrefKey;
import ax.ha.it.smsalarm.receiver.NotificationReceiver;

/**
 * Helper to build up and show {@link Notification}, also creates {@link PendingIntent}'s for the notification.<br>
 * <b><i>NOTE. Contains some deprecated functionality, this is to support <code>Android SDK</code> versions below 11.</b></i>
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 1.2.1-SE
 */
public class AcknowledgeNotificationService extends IntentService {
	private final SharedPreferencesHandler prefHandler = SharedPreferencesHandler.getInstance();

	/**
	 * Creates a new instance of {@link AcknowledgeNotificationService}.<br>
	 * A constructor must be implemented and call it's <code>superclass</code>, {@link IntentService}, constructor with an <b><i>arbitrary</i></b>
	 * <code>String</code> as argument.
	 */
	public AcknowledgeNotificationService() {
		// Note: MUST call super() constructor with an arbitrary string
		super("AcknowledgeNotificationHelper");
	}

	/**
	 * To handle {@link Intent}, builds up and dispatches a notification.
	 * 
	 * @param i
	 *            Intent for notification.
	 * @deprecated
	 */
	@SuppressLint("DefaultLocale")
	@Deprecated
	@Override
	protected void onHandleIntent(Intent i) {
		// Reset Shared Preference HAS_CALLED, to ensure that activity acknowledge not will place a acknowledge call onResume()
		// This is done here because this is only relevant if application is set to acknowledge, and here intent for acknowledge is loaded
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.HAS_CALLED_KEY, false, this);

		// Fetch some values from the shared preferences
		String contentText = (String) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.MESSAGE_KEY, DataType.STRING, this);
		String rescueService = (String) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.RESCUE_SERVICE_KEY, DataType.STRING, this);

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

		// Setup intents for pressing notification and dismissing it
		Intent notificationPressedIntent = new Intent(this, NotificationReceiver.class);
		notificationPressedIntent.setAction(NotificationReceiver.ACTION_ACKNOWLEDGE);

		Intent notificationDismissedIntent = new Intent(this, NotificationReceiver.class);

		// Setup pending intents for notification pressed and dismissed events
		PendingIntent notificationPressedPendingIntent = PendingIntent.getBroadcast(this, 0, notificationPressedIntent, 0);
		PendingIntent notificationDismissedPendingIntent = PendingIntent.getBroadcast(this, 0, notificationDismissedIntent, 0);

		// Create notification using builder
		// @formatter:off
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.ic_primary_alarm)
			.setTicker(tickerText)
			.setWhen(when)
			.setContentTitle(getString(R.string.PRIMARY_ALARM))
			.setContentText(contentText)
			.setContentIntent(notificationPressedPendingIntent)
			.setDeleteIntent(notificationDismissedPendingIntent)
			.setAutoCancel(true)
			.setLights(0xFFff0000, 100, 100);
		// @formatter:off
		
		// Dispatch the notification
		notificationManager.notify((int) REFRESH_ID, builder.getNotification());
		
		// Start the flash notification
		FlashNotificationHandler.startFlashNotification(this);
	}
}