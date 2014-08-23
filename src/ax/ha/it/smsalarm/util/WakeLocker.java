/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.util;

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.PowerManager;
import ax.ha.it.smsalarm.handler.LogHandler;
import ax.ha.it.smsalarm.handler.LogHandler.LogPriorities;

/**
 * Class containing static methods to declare and acquire a <code>WakeLock</code> and for releasing
 * it.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.1
 * @since 2.1
 */
public abstract class WakeLocker {
	// Log tag
	private final static String LOG_TAG = "WakeLocker";
	// Variable used to log messages
	private static LogHandler logger = LogHandler.getInstance();

	// WakeLock variable used to wake up screen
	private static PowerManager.WakeLock wakeLock;

	/**
	 * Static method to acquire WakeLock with from given <code>Context</code>.<br/>
	 * <b><i>Should be null safe</i></b>.
	 * 
	 * @param context
	 *            Context to acquire <code>SystemService</code> from
	 * 
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities,
	 *      String, String)
	 */
	@SuppressWarnings("deprecation")
	@SuppressLint("Wakelock")
	public static void acquire(Context context) {
		// Debug logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":acquire()", "Begin to acquire wakelock");

		// Null check, so we don't release wakeLock if it's not necessary
		if (wakeLock != null) {
			wakeLock.release();
		}

		// Declare and initialize PowerManager and WakeLock with correct flags
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, LOG_TAG + ":acquire()");

		// Debug logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":acquire()", "WakeLock has been initialized as following:\"" + wakeLock.toString() + "\"");

		// Acquire wakelock
		wakeLock.acquire();

		// Debug logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":acquire()", "WakeLock has been acquired");
	}

	/**
	 * Static method to release WakeLock.<br/>
	 * <b><i>Should be null safe</i></b>.
	 * 
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities,
	 *      String, String)
	 */
	public static void release() {
		// Null check, so we don't release wakeLock if it's not necessary
		if (wakeLock != null) {
			// Release wakelock
			wakeLock.release();
			// Debug logging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":release()", "WakeLock has been released");
		}
		wakeLock = null;
	}

	/**
	 * To acquire a WakeLock and release it after given time in milliseconds.
	 * 
	 * @param context
	 *            Context to acquire <code>SystemService</code> from
	 * @param releaseTime
	 *            Time in milliseconds until the acquired WakeLock is released
	 * 
	 * @see #acquire(Context)
	 * @see #release()
	 */
	public static void acquireAndRelease(Context context, int releaseTime) {
		// Debug logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":acquireAndRelease()", "WakeLock is to acquired and it will be released after:\"" + releaseTime + "\"milliseconds");
		// To release acquired wakelock after a specific time
		Timer releaseTimer = new Timer();
		// Acquire wakelock
		acquire(context);

		/*
		 * Setup new timertask and start it. This timertask releases acquired wakelock efter given
		 * time in milliseconds.
		 */
		releaseTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				release();
			}
		}, releaseTime);
	}
}
