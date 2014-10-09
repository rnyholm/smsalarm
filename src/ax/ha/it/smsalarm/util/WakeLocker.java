/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.util;

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

/**
 * Utility class that makes handling with {@link WakeLock} a bit easier.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.1
 */
public abstract class WakeLocker {
	private static final String LOG_TAG = WakeLocker.class.getSimpleName();

	// To handle wake lock
	private static PowerManager.WakeLock wakeLock;

	/**
	 * To acquire a {@link WakeLock}.<br/>
	 * 
	 * @param context
	 *            The Context from which {@link PowerManager} is fetched.
	 */
	@SuppressWarnings("deprecation")
	@SuppressLint("Wakelock")
	public static void acquire(Context context) {
		// Null check, so we don't release wakeLock if it's not necessary
		if (wakeLock != null) {
			wakeLock.release();
		}

		// Declare and initialize PowerManager and WakeLock with correct flags
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, LOG_TAG + ":acquire()");

		// Acquire WakeLock
		wakeLock.acquire();
	}

	/**
	 * To release a {@link WakeLock}.<br/>
	 */
	public static void release() {
		// Null check, so we don't release wakeLock if it's not necessary
		if (wakeLock != null) {
			wakeLock.release();
		}
		wakeLock = null;
	}

	/**
	 * To acquire a {@link WakeLock} and release it after given time in milliseconds.
	 * 
	 * @param context
	 *            The Context from which {@link PowerManager} is fetched.
	 * @param releaseTime
	 *            Time in milliseconds until the acquired WakeLock is released.
	 * @see #acquire(Context)
	 * @see #release()
	 */
	public static void acquireAndRelease(Context context, int releaseTime) {
		// To release acquired WakeLock after a specific time
		Timer releaseTimer = new Timer();

		// Acquire WakeLock
		acquire(context);

		// Setup a TimerTask and start it, after given time in milliseconds has passed the WakeLock will be released
		releaseTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				release();
			}
		}, releaseTime);
	}
}
