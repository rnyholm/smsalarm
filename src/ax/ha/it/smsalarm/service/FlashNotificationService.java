/**
 * Copyright (c) 2015 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import ax.ha.it.smsalarm.handler.CameraHandler;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.DataType;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.PrefKey;

/**
 * Service containing functionality for toggling a <b><i>Flash Notification</i></b> using a {@link Handler} and {@link Runnable}. The service also
 * contains a few convenience methods for <b><i>starting</i></b> and <b><i>stopping</i></b> a Flash Notification.<br>
 * A Flash Notification is simply a kind of notification where the devices {@link Camera} flash flashes when the application receives an alarm.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class FlashNotificationService extends Service {
	// Time until first camera flash and then the interval between them
	private static final int FIRST_FLASH_DELAY = 1000;
	private static final int FLASH_INTERVAL = 500;

	// Must keep track on if this service is running or not
	private boolean serviceRunning = false;

	// Need handler and runnable for the actual "toggling"
	private Handler handler = new Handler();
	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			CameraHandler.getInstance(getApplicationContext()).toggleCameraFlash();
			handler.postDelayed(this, FLASH_INTERVAL);
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();

		// Flag service isn't running yet
		serviceRunning = false;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Service isn't running, go on and kick off handler
		if (!serviceRunning) {
			handler.postDelayed(runnable, FIRST_FLASH_DELAY);

			// Flag service as running
			serviceRunning = true;
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Remove runnable from callback and nullify the handler
		handler.removeCallbacks(runnable);
		handler = null;

		// Release the camera
		CameraHandler.getInstance(getApplicationContext()).releaseCamera();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * To <b><i>Start</i></b> a <b><i>Flash Notification</i></b>.<br>
	 * Under "the hood" this is done by starting {@link Service} - {@link FlashNotificationService} which handles the actual "flashing" using the
	 * {@link CameraHandler}.
	 * <p>
	 * <b><i>Note.The Flash Notification will be started only if the application is set to us it</i></b>.
	 * 
	 * @param context
	 *            Context in which the <code>FlashNotificationService</code> should be started.
	 */
	public static void startFlashNotificationService(Context context) {
		if ((Boolean) SharedPreferencesHandler.getInstance().fetchPrefs(PrefKey.SHARED_PREF, PrefKey.USE_FLASH_NOTIFICATION, DataType.BOOLEAN, context)) {
			context.startService(new Intent(context, FlashNotificationService.class));
		}
	}

	/**
	 * To <b><i>Stop</i></b> a <b><i>Flash Notification</i></b>.<br>
	 * Under "the hood" this is done by stopping {@link Service} - {@link FlashNotificationService} which handles the actual "flashing" using the
	 * {@link CameraHandler}.
	 * 
	 * @param context
	 *            Context in which the <code>FlashNotificationService</code> should be stopped.
	 */
	public static void stopFlashNotificationService(Context context) {
		context.stopService(new Intent(context, FlashNotificationService.class));
	}
}