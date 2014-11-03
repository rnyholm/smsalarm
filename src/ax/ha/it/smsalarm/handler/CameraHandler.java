/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.handler;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.os.Build;
import android.util.Log;
import ax.ha.it.smsalarm.activity.SmsAlarm;

import com.google.common.base.Optional;

/**
 * Utility class for safely acquire a {@link Camera} object, handle it and releasing it.<br>
 * <b><i>{@link CameraHandler} is a singleton, eagerly initialized to avoid concurrent modification.</i></b>
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class CameraHandler {
	private static final String LOG_TAG = CameraHandler.class.getSimpleName();

	// Singleton instance of this class, eagerly initialized
	private static final CameraHandler INSTANCE = new CameraHandler();

	// Context is needed in order to get the PackageManager
	private static Context context;

	private Camera camera = null;

	/**
	 * Creates a new instance of {@link CameraHandler} with given context.
	 */
	private CameraHandler() {
		if (INSTANCE != null) {
			if (SmsAlarm.DEBUG) {
				Log.e(LOG_TAG + ":CameraHandler()", "CameraHandler already instantiated");
			}
		}
	}

	/**
	 * To get the <b><i>singleton</i></b> instance of {@link CameraHandler}.
	 * 
	 * @param context
	 *            The context in which this CameraHandler runs in.
	 * @return Instance of <code>CamerHandler</code>.
	 */
	public static CameraHandler getInstance(Context ctx) {
		// If no context exists, set it
		if (context == null) {
			context = ctx;
		}

		return INSTANCE;
	}

	/**
	 * To toggle {@link CameraHandler}'s {@link Camera} objects <b><i>Flash Light</i></b> on or off. If it's currently being on ON then it's switched
	 * OFF and vice versa.
	 */
	public void toggleCameraFlash() {
		// Get the package manager in order to figure out if system service exists
		PackageManager packageManager = context.getPackageManager();

		// Only if device supports support camera flash
		if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			// Initialize if null
			if (camera == null) {
				try {
					Optional<Integer> optionalCameraId = findBackFacingCamera();

					// If device got any BackFacing camera
					if (optionalCameraId.isPresent()) {
						camera = Camera.open(optionalCameraId.get());

						if (SmsAlarm.DEBUG) {
							Log.i(LOG_TAG + ":toggleCameraFlash()", "Backfacing camera successfully initialized");
						}
					}
				} catch (RuntimeException e) {
					if (SmsAlarm.DEBUG) {
						Log.e(LOG_TAG + ":toggleCameraFlash()", "An error occurred while initializing the camera", e);
					}
				}
			}

			// In case of camera failed to initialize, could be that device has no camera
			if (camera != null) {
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
			camera.release();
			camera = null;

			if (SmsAlarm.DEBUG) {
				Log.i(LOG_TAG + ":releaseCamera()", "Camera released");
			}
		}
	}

	/**
	 * To figure out which camera id the <b><i>BackFacing</i></b> camera has, if device has any camera facing back.
	 * 
	 * @return An {@link Optional} containing the id of the BackFacing camera, if device got any. Else an <code>absent</code> optional is returned.
	 */
	private Optional<Integer> findBackFacingCamera() {
		// Iterate through each camera and check if it's facing backwards
		for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
			CameraInfo cameraInfo = new CameraInfo();
			Camera.getCameraInfo(i, cameraInfo);

			// Check if current camera is facing back
			if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
				if (SmsAlarm.DEBUG) {
					Log.d(LOG_TAG + ":findBackFacingCamera()", "Backfacing camera found with camera id: \"" + i + "\"");
					return Optional.<Integer> of(i);
				}
			}
		}

		// No camera was found...
		return Optional.<Integer> absent();
	}
}
