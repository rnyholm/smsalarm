package ax.ha.it.smsalarm.handler;

/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import ax.ha.it.smsalarm.activity.SmsAlarm;

/**
 * Utility class for safely acquire a {@link Camera} object, handle it and releasing it.<br>
 * <b><i>{@link CameraHandler} is a singleton.</i></b>
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class CameraHandler {
	private static final String LOG_TAG = CameraHandler.class.getSimpleName();

	// Singleton instance of this class
	private static CameraHandler INSTANCE = null;

	// Delay until the camera is being released from method being call to release it
	private static final int RELEASE_DELAY = 1000;

	private Camera camera = null;

	// Context is needed in order to get the PackageManager
	private Context context;;

	/**
	 * Creates a new instance of {@link CameraHandler} with given context.
	 * 
	 * @param context
	 *            The context in which this CameraHandler runs in.
	 */
	private CameraHandler(Context context) {
		this.context = context;
	}

	/**
	 * To get the <b><i>singleton</i></b> instance of {@link CameraHandler}.
	 * 
	 * @param context
	 *            The context in which this CameraHandler runs in.
	 * @return Instance of <code>CamerHandler</code>.
	 */
	public static CameraHandler getInstance(Context context) {
		// If instance of this object is null create a new one
		if (INSTANCE == null) {
			INSTANCE = new CameraHandler(context);
		}

		return INSTANCE;
	}

	/**
	 * To safely initialize the {@link Camera} object within {@link CameraHandler} if needed. This decision is done with a null check of the Camera
	 * object in CameraHandler. If null, it's being initialized, else not.
	 */
	private void initializeCamera() {
		// Camera needs to be initialized
		if (camera == null) {
			PackageManager pm = context.getPackageManager();

			// See if device has a camera feature
			if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
				try {
					camera = Camera.open();

					if (SmsAlarm.DEBUG) {
						Log.i(LOG_TAG + ":initializeCamera()", "Camera successfully initialized");
					}
				} catch (RuntimeException e) {
					if (SmsAlarm.DEBUG) {
						Log.e(LOG_TAG + ":initializeCamera()", "An error occurred while initializing the camera, maybe application is running in a emulator", e);
					}
				}
			}
		}
	}

	/**
	 * To toggle {@link CameraHandler}'s {@link Camera} objects <b><i>Flash Light</i></b> on or off. If it's currently being on ON then it's switched
	 * OFF and vice versa.
	 */
	public void toggleCameraFlash() {
		initializeCamera();

		// Get the parameters of from the camera, and especially the FlashMode
		Parameters parameters = camera.getParameters();
		String flashMode = parameters.getFlashMode();

		// If FlashMode hasn't been set or it's off then switch it On and startPreview - IMPORTANT!
		if (flashMode == null || Parameters.FLASH_MODE_OFF.equals(flashMode)) {
			// This debug statement needs to be here as this can be tricky to test on an emulator else
			if (SmsAlarm.DEBUG) {
				Log.d(LOG_TAG + ":toggleCameraFlash()", "Switching camera flash LED ON");
			}

			// Torch mode as we wan't the camera flash to light as bright as possible
			parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
			camera.setParameters(parameters);
			camera.startPreview();
		} else if (Parameters.FLASH_MODE_TORCH.equals(flashMode)) {
			if (SmsAlarm.DEBUG) {
				Log.d(LOG_TAG + ":toggleCameraFlash()", "Switching camera flash LED OFF");
			}

			parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
			camera.setParameters(parameters);
			camera.stopPreview();
		}
	}

	/**
	 * To release {@link CameraHandler}'s {@link Camera} object if it has been initialized.
	 * <p>
	 * <b><i>Note. </i></b>A small delay is added before the actual {@link Camera#release()} call is made, this little delay gives the Camera some
	 * time to finish up any current handling.
	 */
	public void releaseCamera() {
		if (camera != null) {
			// Need to know when called this method
			long startTimeMillis = System.currentTimeMillis();

			// Wait for a while to let ongoing camera handling finish
			while (System.currentTimeMillis() < (startTimeMillis + RELEASE_DELAY))
				;

			// Also set camera to null
			camera.release();
			camera = null;

			if (SmsAlarm.DEBUG) {
				Log.i(LOG_TAG + ":releaseCamera()", "Camera released");
			}
		}
	}
}
