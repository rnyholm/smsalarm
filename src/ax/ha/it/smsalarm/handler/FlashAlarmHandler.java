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
import ax.ha.it.smsalarm.receiver.FlashAlarmReceiver;

/**
 * Class containing functionality for <b><i>starting</i></b> and <b><i>stopping</i></b> a <b><i>Flash Alarm</i></b>.<br>
 * A Flash Alarm is simply a kind of notification where the devices {@link Camera} flash flashes when the application receives an alarm.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 0.9beta
 */
public class FlashAlarmHandler {
	// Time until first camera flash and then the interval between them
	private static final int FIRST_FLASH_DELAY = 500;
	private static final int FLASH_INTERVAL = 500;

	/**
	 * To <b><i>Start</i></b> a <b><i>Flash Alarm</i></b>.
	 * 
	 * @param context
	 *            Context in which system services are taken.
	 */
	public static void flashAlarmStart(Context context) {
		// Create the alarm manager, setup intents and pending intents
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, FlashAlarmReceiver.class);

		// It's wanted to update any existing intent, hence requestCode = 0 and PendingIntent.FLAG_CANCEL_CURRENT
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

		// Calendar instance is needed to figure out start time of first alarm triggering
		Calendar time = Calendar.getInstance();
		time.setTimeInMillis(System.currentTimeMillis());
		time.add(Calendar.MILLISECOND, FIRST_FLASH_DELAY);

		// Set alarm manager to repeat
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), FLASH_INTERVAL, pendingIntent);
	}

	/**
	 * To <b><i>Stop</i></b> a <b><i>Flash Alarm</i></b>.
	 * 
	 * @param context
	 *            Context in which system services are taken.
	 */
	public static void flashAlarmStop(Context context) {
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, FlashAlarmReceiver.class);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

		// Cancel alarms and release the camera - IMPORTANT
		alarmManager.cancel(pendingIntent);
		CameraHandler.getInstance(context).releaseCamera();
	}
}
