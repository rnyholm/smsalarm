/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.service.notification.NotificationListenerService;
import android.util.Log;
import ax.ha.it.smsalarm.activity.SmsAlarm;

/**
 * Class responsible for switching the <b><i>Camera Flash Light</i></b> on and off for a certain amount of time, according to received intent.
 * <p>
 * <b><i>Note.</i></b> This functionality will implicitly only work on devices running Android Jelly Bean MR2 (API Level 18) and higher, as the
 * invoking class is a subclass of {@link NotificationListenerService}.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class FlashAlarmReceiver extends BroadcastReceiver {
	private static final String LOG_TAG = FlashAlarmReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		CameraHandler.getInstance(context).toggleCameraFlash();
	}

	public static class CameraHandler {
		private static final String LOG_TAG = CameraHandler.class.getSimpleName();

		// Singleton instance of this class
		private static CameraHandler INSTANCE = null;
		private Camera camera = null;

		private Context context;

		/**
		 * Creates a new instance of {@link CameraHandler}.
		 */
		private CameraHandler(Context context) {
			this.context = context;
		}

		/**
		 * To get the <b><i>singleton</i></b> instance of {@link CameraHandler}.
		 * 
		 * @return Instance of <code>CamerHandler</code>.
		 */
		public static CameraHandler getInstance(Context context) {
			// If instance of this object is null create a new one
			if (INSTANCE == null) {
				INSTANCE = new CameraHandler(context);
			}

			return INSTANCE;
		}

		private void initializeCamera() {
			PackageManager pm = context.getPackageManager();

			// See if device has a camera feature
			if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
				try {
					camera = Camera.open();
				} catch (RuntimeException e) {
					if (SmsAlarm.DEBUG) {
						Log.e(LOG_TAG + ":initializeCamera()", "An error occurred while handling with the camera, maybe application is running on emulator", e);
					}
				}
			}
		}

		public void toggleCameraFlash() {
			if (camera == null) {
				initializeCamera();
			}

			Parameters parameters = camera.getParameters();

			if (parameters.getFlashMode() == null || Parameters.FLASH_MODE_OFF.equals(parameters.getFlashMode())) {
				parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
				camera.setParameters(parameters);
				camera.startPreview();

				Log.d(LOG_TAG + ":toggleCameraFlash()", "Turning flash ON");
			} else if (Parameters.FLASH_MODE_TORCH.equals(parameters.getFlashMode())) {
				parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
				camera.setParameters(parameters);
				camera.stopPreview();

				Log.d(LOG_TAG + ":toggleCameraFlash()", "Turning flash OFF");
			}
		}

		public void releaseCamera() {
			if (camera != null) {
				camera.release();
				camera = null;

				Log.d(LOG_TAG + ":releaseCamera()", "Camera released");
			}
		}
	}
}
