/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.service;

import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.activity.SmsAlarm;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.DataType;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.PrefKey;
import ax.ha.it.smsalarm.pojo.Alarm.AlarmType;
import ax.ha.it.smsalarm.receiver.FlashAlarmReceiver;
import ax.ha.it.smsalarm.receiver.NotificationReceiver;

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

	public static final String NOTIFICATION_ACTION = "notificationAction";
	public static final int NOTIFICATION_PRESSED = 0;
	public static final int NOTIFICATION_DISMISSED = 1;

	// Time until first camera flash and then the interval between them
	private static final int FIRST_FLASH_DELAY = 1000;
	private static final int FLASH_INTERVAL = 1000;

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
	 * To handle {@link Intent}, builds up and dispatches a notification. Contains some deprecated functionality just to support
	 * <code>Android SDK</code> versions below 11.
	 * 
	 * @param i
	 *            Intent for notification.
	 * @deprecated
	 */
	@SuppressLint("DefaultLocale")
	@Deprecated
	@Override
	protected void onHandleIntent(Intent i) {
		// Fetch some values from the shared preferences
		String contentText = (String) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.MESSAGE_KEY, DataType.STRING, this);
		String rescueService = (String) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.RESCUE_SERVICE_KEY, DataType.STRING, this);
		AlarmType alarmType = AlarmType.of((Integer) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.LARM_TYPE_KEY, DataType.INTEGER, this));

		// This string and intent opens the messaging directory on the Android device, however due to this page;
		// http://stackoverflow.com/questions/3708737/go-to-inbox-in-android fetched 21.10-11, this way of achieve the "go to messaging directory" is
		// highly unrecommended.Thats because this method uses undocumented API and is not part of the Android core.This may or may not work on some
		// devices and versions!
//		String SMS_MIME_TYPE = "vnd.android-dir/mms-sms";
//		Intent notificationIntent = new Intent(Intent.ACTION_MAIN); // Don't want to start any activity!
//		notificationIntent.setType(SMS_MIME_TYPE);
		Intent notificationPressedIntent = new Intent(this, NotificationReceiver.class);
		notificationPressedIntent.putExtra(NOTIFICATION_ACTION, NOTIFICATION_PRESSED);

		Intent notificationDismissedIntent = new Intent(this, NotificationReceiver.class);
		notificationDismissedIntent.putExtra(NOTIFICATION_ACTION, NOTIFICATION_DISMISSED);

		// Setup a notification, directly from Android developer site
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// To get a unique refresh id for the intents, to avoid notifications from writing over each other
		long REFRESH_ID = System.currentTimeMillis();
		long when = System.currentTimeMillis();

		// Configure notification depending on alarm type
		switch (alarmType) {
			case PRIMARY:
				// Set proper texts and icon to notification
				configureNotification(R.drawable.ic_primary_alarm, getString(R.string.PRIMARY_ALARM), rescueService.toUpperCase(), getString(R.string.PRIMARY_ALARM));
				break;
			case SECONDARY:
				configureNotification(R.drawable.ic_secondary_alarm, getString(R.string.SECONDARY_ALARM), rescueService.toUpperCase(), getString(R.string.SECONDARY_ALARM));
				break;
			default: // If this happens, something really weird is going on
				if (SmsAlarm.DEBUG) {
					Log.e(LOG_TAG + ":onHandleIntent()", "Alarm type couldn't be find when configuring notification");
				}
		}

		// Setup pending intents for notification pressed and dismissed events
		PendingIntent notificationPressedPendingIntent = PendingIntent.getBroadcast(this, 0, notificationPressedIntent, 0);
//		notification.setLatestEventInfo(getApplicationContext(), contentTitle, contentText, notificationPressedPendingIntent);

		// Setup pending intent for dismissed notification
		PendingIntent notificationDismissedPendingIntent = PendingIntent.getBroadcast(this, 0, notificationDismissedIntent, 0);

		// Create notification
//		Notification notification = new Notification(icon, tickerText, when);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setSmallIcon(icon);
		builder.setTicker(tickerText);
		builder.setWhen(when);
		builder.setContentTitle(contentTitle);
		builder.setContentText(contentText);
		builder.setContentIntent(notificationPressedPendingIntent);
		builder.setDeleteIntent(notificationDismissedPendingIntent);
		builder.setAutoCancel(true);
		builder.setLights(0xFFff0000, 100, 100);

		// This flag auto cancels the notification when clicked and indicating that devices LED should light up
//		notification.flags = Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL;

		// @formatter:off
		// Configure LED
//		notification.ledARGB = 0xFFff0000; 	// Red
//		notification.ledOnMS = 100;			// On time
//		notification.ledOffMS = 100; 		// Off time
		// @formatter:on

		Notification notification = builder.getNotification();

		// Dispatch the notification
		notificationManager.notify((int) REFRESH_ID, notification);

		// Create the alarm manager, setup intents and pending intents
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(this, FlashAlarmReceiver.class);

		// It's wanted to update any existing intent, hence requestCode = 0 and PendingIntent.FLAG_CANCEL_CURRENT
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

		// Calendar instance is needed to figure out start time of first alarm triggering
		Calendar time = Calendar.getInstance();
		time.setTimeInMillis(System.currentTimeMillis());
		time.add(Calendar.MILLISECOND, FIRST_FLASH_DELAY);

		// Set alarm manager to repeat
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), FLASH_INTERVAL, pendingIntent);
	}

	/**
	 * To configure the notification that's about to be dispatched correctly. The ticker text is built up dynamically depending on argument
	 * <code>rescueService</code>.
	 * 
	 * @param icon
	 *            Icon as integer value, use <code>android.R.drawable.*</code>.
	 * @param tickerText
	 *            Notifications ticker text.
	 * @param rescueService
	 *            Rescue service's/organizations name, if it exists.
	 * @param contentTitle
	 *            Notification> contents title.
	 */
	private void configureNotification(int icon, String tickerText, String rescueService, String contentTitle) {
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
	}
}