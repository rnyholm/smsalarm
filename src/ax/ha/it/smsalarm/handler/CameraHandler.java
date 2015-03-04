/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.handler;

import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
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
@SuppressWarnings("deprecation")
public class CameraHandler {
	private static final String LOG_TAG = CameraHandler.class.getSimpleName();

	// Singleton instance of this class, eagerly initialized
	private static final CameraHandler INSTANCE = new CameraHandler();

	// Context is needed in order to get the PackageManager
	private static Context context;

	private Camera camera = null;

	// To store supported FlashMode to use
	private Optional<String> flashModeToUse = Optional.<String> absent();

	/**
	 * Creates a new instance of {@link CameraHandler}.
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

		// Only if device supports support camera and flash
		if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA) && packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
			// Initialize if null
			if (camera == null) {
				try {
					Optional<Integer> optionalCameraId = findBackFacingCamera();

					// If device got any BackFacing camera
					if (optionalCameraId.isPresent()) {
						camera = Camera.open(optionalCameraId.get());

						// Figure out correct FlashMode
						flashModeToUse = resolveFlashMode();
					}
				} catch (RuntimeException e) {
					if (SmsAlarm.DEBUG) {
						Log.e(LOG_TAG + ":toggleCameraFlash()", "An error occurred while initializing the camera", e);
					}
				}
			}

			// In case of camera failed to initialize, could be that device has no camera, FlashMode must also be successfully
			// resolved(Parameters.FLASH_MODE_TORCH or Parameters.FLASH_MODE_ON)
			if (camera != null && flashModeToUse.isPresent()) {
				// Get the parameters of from the camera, and especially the FlashMode
				Parameters parameters = camera.getParameters();
				String currentFlashMode = parameters.getFlashMode();

				// If FlashMode hasn't been set or it's off then switch it On and startPreview - IMPORTANT!
				if (currentFlashMode == null || Parameters.FLASH_MODE_OFF.equals(currentFlashMode)) {
					// Torch mode as we wan't the camera flash to light as bright as possible
					parameters.setFlashMode(flashModeToUse.get());
					camera.setParameters(parameters);
				} else if (flashModeToUse.get().equals(currentFlashMode)) {
					parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
					camera.setParameters(parameters);
				}

				// Stopping preview in order to start it correctly again
				camera.stopPreview();

				// Try to start preview again, surrounded within try catch clause because this can fail sometimes
				try {
					camera.startPreview();

					// Set AutoFocus even if we don't use it, recommended according to
					// http://http://stackoverflow.com/questions/5503480/use-camera-flashlight-in-android
					camera.autoFocus(new AutoFocusCallback() {
						@Override
						public void onAutoFocus(boolean success, Camera camera) {
						}
					});
				} catch (RuntimeException re) {
					if (SmsAlarm.DEBUG) {
						Log.e(LOG_TAG + ":toggleCameraFlash()", "An error occurred while starting preview", re);
					}
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
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	}

	/**
	 * To figure out which camera id the <b><i>BackFacing</i></b> camera has, if device has any camera facing back.
	 * 
	 * @return An {@link Optional} containing the id of the BackFacing camera, if device got any. Else an <code>absent</code> optional is returned.
	 */
	public static Optional<Integer> findBackFacingCamera() {
		// Iterate through each camera and check if it's facing backwards
		for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
			CameraInfo cameraInfo = new CameraInfo();
			Camera.getCameraInfo(i, cameraInfo);

			// Check if current camera is facing back
			if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
				return Optional.<Integer> of(i);
			}
		}

		// No camera was found...
		return Optional.<Integer> absent();
	}

	/**
	 * To resolve correct <b><i>FlashMode</i></b>. This can be either <b><i>Parameters.FLASH_MODE_TORCH</i></b> or
	 * <b><i>Parameters.FLASH_MODE_ON</i></b>, Parameters.FLASH_MODE_TORCH will have precedence over Parameters.FLASH_MODE_ON.<br>
	 * If none of these are supported, no flash will be used.
	 */
	private Optional<String> resolveFlashMode() {
		// Only if camera has been initialized
		if (camera != null) {
			// Figure out the supported FlashModes of this devices camera
			Parameters parameters = camera.getParameters();
			List<String> supportedFlashModes = parameters.getSupportedFlashModes();

			// Could be null
			if (supportedFlashModes != null) {
				// Prioritize Parameters.FLASH_MODE_TORCH
				if (supportedFlashModes.contains(Parameters.FLASH_MODE_TORCH)) {
					return Optional.<String> of(Parameters.FLASH_MODE_TORCH);
				} else if (supportedFlashModes.contains(Parameters.FLASH_MODE_ON)) {
					return Optional.<String> of(Parameters.FLASH_MODE_ON);
				}
			}
		}

		return Optional.<String> absent();
	}
}
