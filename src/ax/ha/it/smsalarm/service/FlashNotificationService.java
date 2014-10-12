/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.service;

import java.util.Calendar;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import ax.ha.it.smsalarm.handler.CameraHandler;
import ax.ha.it.smsalarm.receiver.FlashAlarmReceiver;

/**
 * Class responsible for the <b><i>Camera Flash Notifications</i></b>. This is done by listening at events in the <b><i>Notification Bar</i></b>, more
 * exactly these events:<br>
 * - {@link NotificationListenerService#onNotificationPosted(StatusBarNotification)}<br>
 * - {@link NotificationListenerService#onNotificationRemoved(StatusBarNotification)}<br>
 * <p>
 * If the {@link StatusBarNotification} originated from application package <code>ax.ha.it.smsalarm</code>, <b><i>then and only then</i></b>, it's
 * handled by this service.
 * <p>
 * <b><i>Note.</i></b> This functionality only works on devices running Android Jelly Bean MR2 (API Level 18) and higher.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class FlashNotificationService extends NotificationListenerService {
	// Time until first camera flash and then the interval between them
	private static final int FIRST_FLASH_DELAY = 1000;
	private static final int FLASH_INTERVAL = 1000;

	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {

		// Only want to catch notifications posted to notification bar from our own applications package ax.ha.it.smsalarm
		if (sbn.getPackageName().equals(getPackageName())) {
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
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {
		// Only want to catch notifications removed from notification bar from our own applications package ax.ha.it.smsalarm
		if (sbn.getPackageName().equals(getPackageName())) {
			AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			Intent intent = new Intent(this, FlashAlarmReceiver.class);

			PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

			// Cancel alarms and release the camera - IMPORTANT
			alarmManager.cancel(pendingIntent);
			CameraHandler.getInstance(this).releaseCamera();
		}
	}
}
