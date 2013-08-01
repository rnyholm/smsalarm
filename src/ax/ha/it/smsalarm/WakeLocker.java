/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.PowerManager;
import ax.ha.it.smsalarm.LogHandler.LogPriorities;

/**
 * Class containing static methods to declare and acquire a <code>WakeLock</code>
 * and for releasing it.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.1
 * @since 2.1
 * @date 2013-08-01
 */
public abstract class WakeLocker {
	// Log tag
	private final static String LOG_TAG = "WakeLocker";
	// Variable used to log messages
	private static LogHandler logger;

	// WakeLock variable used to wake up screen
    private static PowerManager.WakeLock wakeLock;

    /**
     * Static method to acquire WakeLock with from given <code>Context</code>.<br/>
     * <b><i>Should be null safe</i></b>.
     * 
     * @param context Context to acquire <code>SystemService</code> from
     * 
     * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
     */
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
     * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
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
}
