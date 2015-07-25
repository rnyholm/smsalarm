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
import android.util.Log;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.alarm.Alarm;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.DataType;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.PrefKey;
import ax.ha.it.smsalarm.receiver.NotificationReceiver;
import ax.ha.it.smsalarm.util.Utils;

/**
 * Helper to build up and show {@link Notification}, also creates {@link PendingIntent}'s for the notification.<br>
 * <b><i>NOTE. Contains some deprecated functionality, this is to support <code>Android SDK</code> versions below 11.</b></i>
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 0.9beta
 */
public class NotificationService extends IntentService {
	private static final String LOG_TAG = NotificationService.class.getSimpleName();

	private final SharedPreferencesHandler prefHandler = SharedPreferencesHandler.getInstance();

	// @formatter:off
	// Different variables needed to build up a correct notification
	private String tickerText = "";		// Scrolling text in notification bar
	private String contentTitle = "";	// Title in expanded notification
	// @formatter:on

	private int icon = 0; // Icon in notification bar

	/**
	 * Creates a new instance of {@link NotificationService}.<br>
	 * A constructor must be implemented and call it's <code>superclass</code>, {@link IntentService}, constructor with an <b><i>arbitrary</i></b>
	 * <code>String</code> as argument.
	 */
	public NotificationService() {
		// Note: MUST call super() constructor with an arbitrary string
		super("NotificationHelper");
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
	protected void onHandleIntent(Intent intent) {
		// Get the alarm passed on from SmsReceiver
		Alarm alarm = (Alarm) intent.getParcelableExtra(Alarm.TAG);

		// Fetch organization from the shared preferences
		String organization = (String) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.ORGANIZATION_KEY, DataType.STRING, this);

		// Setup a notification, directly from Android developer site
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// To get a unique refresh id for the intents, to avoid notifications from writing over each other
		long REFRESH_ID = System.currentTimeMillis();
		long when = System.currentTimeMillis();

		// Configure notification depending on alarm type
		switch (alarm.getAlarmType()) {
			case PRIMARY:
				// Set proper texts and icon to notification
				configureNotification(R.drawable.ic_primary_alarm, getString(R.string.PRIMARY_ALARM), organization.toUpperCase(), getString(R.string.PRIMARY_ALARM));
				break;
			case SECONDARY:
				configureNotification(R.drawable.ic_secondary_alarm, getString(R.string.SECONDARY_ALARM), organization.toUpperCase(), getString(R.string.SECONDARY_ALARM));
				break;
			default: // If this happens, something really weird is going on
				Log.e(LOG_TAG + ":onHandleIntent()", "Alarm type couldn't be find when configuring notification");
		}

		// Setup intents for pressing notification and dismissing it, doesn't need to pass over alarm in this intent
		Intent notificationPressedIntent = new Intent(this, NotificationReceiver.class);
		notificationPressedIntent.setAction(NotificationReceiver.ACTION_OPEN_INBOX);

		Intent notificationDismissedIntent = new Intent(this, NotificationReceiver.class);

		// Setup pending intents for notification pressed and dismissed events
		PendingIntent notificationPressedPendingIntent = PendingIntent.getBroadcast(this, 0, notificationPressedIntent, 0);
		PendingIntent notificationDismissedPendingIntent = PendingIntent.getBroadcast(this, 0, notificationDismissedIntent, 0);

		// Create notification using builder
		// @formatter:off
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
			.setSmallIcon(icon)
			.setTicker(tickerText)
			.setWhen(when)
			.setContentTitle(contentTitle)
			.setContentText(Utils.cleanAlarmCentralAXMessage(alarm.getMessage()))
			.setContentIntent(notificationPressedPendingIntent)
			.setDeleteIntent(notificationDismissedPendingIntent)
			.setAutoCancel(true)
			.setLights(0xFFff0000, 100, 100);
		// @formatter:off

		// Dispatch the notification
		notificationManager.notify((int) REFRESH_ID, builder.getNotification());

		// Start the flash notification
		FlashNotificationService.startFlashNotificationService(this);
	}

	/**
	 * To configure the notification that's about to be dispatched correctly. The ticker text is built up dynamically depending on argument
	 * <code>organization</code>.
	 * 
	 * @param icon
	 *            Icon as integer value, use <code>android.R.drawable.*</code>.
	 * @param tickerText
	 *            Notifications ticker text.
	 * @param organization
	 *            Organizations name, if it exists.
	 * @param contentTitle
	 *            Notification> contents title.
	 */
	private void configureNotification(int icon, String tickerText, String organization, String contentTitle) {
		// Set icon for notification
		this.icon = icon;

		// Set ticker text, with organization name if it exists
		if (!"".equals(organization)) {
			this.tickerText = organization + " " + tickerText;
		} else {
			this.tickerText = tickerText;
		}

		// Set content title
		this.contentTitle = contentTitle;
	}
}