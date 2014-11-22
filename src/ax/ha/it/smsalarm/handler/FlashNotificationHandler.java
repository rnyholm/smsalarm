/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.handler;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.DataType;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.PrefKey;
import ax.ha.it.smsalarm.receiver.FlashNotificationReceiver;

/**
 * Class containing functionality for <b><i>starting</i></b> and <b><i>stopping</i></b> a <b><i>Flash Notification</i></b>.<br>
 * A Flash Notification is simply a kind of notification where the devices {@link Camera} flash flashes when the application receives an alarm.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 0.9beta
 * @see FlashNotificationReceiver
 */
public class FlashNotificationHandler {
	// Time until first camera flash and then the interval between them
	private static final int FIRST_FLASH_DELAY = 500;
	private static final int FLASH_INTERVAL = 500;

	/**
	 * To <b><i>Start</i></b> a <b><i>Flash Notification</i></b>.
	 * 
	 * @param context
	 *            Context in which system services are taken.
	 */
	public static void startFlashNotification(Context context) {
		// Need to figure out if Flash Notification is to be used or not
		SharedPreferencesHandler prefHandler = SharedPreferencesHandler.getInstance();

		// Only start AlarmManager if it's set that the flash notification should be used. Support is checked in the OtherSettingsFragment class
		if ((Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.USE_FLASH_NOTIFICATION, DataType.BOOLEAN, context)) {
			// Create the alarm manager, setup intents and pending intents
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Intent intent = new Intent(context, FlashNotificationReceiver.class);

			// It's wanted to update any existing intent, hence requestCode = 0 and PendingIntent.FLAG_CANCEL_CURRENT
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

			// Calendar instance is needed to figure out start time of first alarm triggering
			Calendar time = Calendar.getInstance();
			time.setTimeInMillis(System.currentTimeMillis());
			time.add(Calendar.MILLISECOND, FIRST_FLASH_DELAY);

			// Set alarm manager to repeat
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), FLASH_INTERVAL, pendingIntent);
		}
	}

	/**
	 * To <b><i>Stop</i></b> a <b><i>Flash Notification</i></b>.
	 * 
	 * @param context
	 *            Context in which system services are taken.
	 */
	public static void stopFlashNotification(Context context) {
		SharedPreferencesHandler prefHandler = SharedPreferencesHandler.getInstance();

		// Only stop AlarmManager if it's set that the flash notification should be used. Support is checked in the OtherSettingsFragment class
		if ((Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.USE_FLASH_NOTIFICATION, DataType.BOOLEAN, context)) {
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Intent intent = new Intent(context, FlashNotificationReceiver.class);

			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

			// Cancel alarms and release the camera - IMPORTANT
			alarmManager.cancel(pendingIntent);
			CameraHandler.getInstance(context).releaseCamera();
		}
	}
}
