/**
 * Copyright (c) 2015 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.media.AudioManager;
import android.os.Vibrator;
import android.util.Log;
import ax.ha.it.smsalarm.activity.SmsAlarm;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.DataType;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.PrefKey;
import ax.ha.it.smsalarm.pojo.Alarm.AlarmType;

/**
 * Class responsible for all vibration handling, this means responsible for all interactions with the {@link Vibrator} in this application.<br>
 * <b><i> {@link VibrationHandler} is a singleton, eagerly initialized to avoid concurrent modification.</i></b>
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class VibrationHandler {
	// Singleton instance of this class, eagerly initialized
	private static final VibrationHandler INSTANCE = new VibrationHandler();

	private static final String LOG_TAG = VibrationHandler.class.getSimpleName();

	// Different names for the patterns
	public static final String VIBRATION_PATTERN_SMS_ALARM = "Sms Alarm"; // This pattern is used as default in case it isn't set
	private static final String VIBRATION_PATTERN_SOS = "SOS";
	private static final String VIBRATION_PATTERN_LONG_ON_LONG = "Long on Long";
	private static final String VIBRATION_PATTERN_SHORTIES = "Shorties";

	// Need to access some shared preferences
	private SharedPreferencesHandler prefHandler = SharedPreferencesHandler.getInstance();

	// Map containing all different vibration patterns
	private final HashMap<String, long[]> vibrationPatterns = new HashMap<String, long[]>();

	// Must have object of Vibrator
	private Vibrator vibrator;

	/**
	 * Creates a new instance of {@link VibrationHandler}.
	 */
	private VibrationHandler() {
		if (INSTANCE != null) {
			if (SmsAlarm.DEBUG) {
				Log.e(LOG_TAG + ":VibrationHandler()", "VibrationHandler already instantiated");
			}
		}

		// Put the vibration patterns to the map containing vibration name and pattern
		vibrationPatterns.put(VIBRATION_PATTERN_SMS_ALARM, new long[] { 0, 5000, 500, 5000, 500, 5000, 500, 5000 });
		vibrationPatterns.put(VIBRATION_PATTERN_SOS, new long[] { 0, 200, 200, 200, 200, 200, 500, 500, 200, 500, 200, 500, 500, 200, 200, 200, 200, 200, 1000, 200, 200, 200, 200, 200, 500, 500, 200, 500, 200, 500, 500, 200, 200, 200, 200, 200 });
		vibrationPatterns.put(VIBRATION_PATTERN_LONG_ON_LONG, new long[] { 0, 10000, 500, 10000 });
		vibrationPatterns.put(VIBRATION_PATTERN_SHORTIES, new long[] { 0, 500, 250, 500, 250, 500, 250, 500, 250, 500, 250, 500, 250, 500, 250, 500, 250, 500, 250, 500, 250, 500, 250, 500, 250, 500, 250, 500, 250, 500, 250, 500, 250, 500, 250, 500, 250, 500, 250, 500, 250, 500, 250, 500, 250, 500, 250, 500, 250 });
	}

	/**
	 * To get the <b><i>singleton</i></b> instance of {@link VibrationHandler}.
	 * 
	 * @return Instance of <code>VibrationHandler</code>
	 */
	public static VibrationHandler getInstance() {
		return INSTANCE;
	}

	/**
	 * To vibrate given <code>vibrationPattern</code> with this {@link VibrationHandler}'s instance of {@link Vibrator}.
	 * 
	 * @param context
	 *            The Context in which {@link VibrationHandler} runs.
	 * @param vibrationPattern
	 *            The vibration pattern to be vibrated.
	 */
	public void previewVibration(Context context, String vibrationPattern) {
		// Only want to vibrate if device isn't in ringer mode silent
		if (SoundHandler.getRingerMode(context) != AudioManager.RINGER_MODE_SILENT) {
			// Check that given vibration pattern exists
			if (vibrationPatterns.containsKey(vibrationPattern)) {
				// First of all stop the Vibrator
				cancelVibrator();

				// Ensure we got a fresh Vibrator object
				vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

				// Start vibrate
				vibrator.vibrate(vibrationPatterns.get(vibrationPattern), -1);
			}
		} else {
			if (SmsAlarm.DEBUG) {
				Log.e(LOG_TAG + ":previewVibration()", "Device is in RINGER_MODE_SILENT and should not vibrate");
			}
		}
	}

	/**
	 * To vibrate the actual <b><i>Alarm</i></b>. This method is similar to {@link #previewVibration(Context, String)} but the main difference is that
	 * it resolves what <b><i>Alarm Vibration</i></b> that should be vibrated depending on given {@link AlarmType}. <br>
	 * 
	 * @param context
	 *            The Context in which {@link VibrationHandler} runs.
	 * @param alarmType
	 *            Type of alarm from which correct alarm vibration is resolved.
	 * @see #previewVibration(Context, String)
	 */
	public void alarm(Context context, AlarmType alarmType) {
		// Only do further handling if given AlarmType is supported
		if (AlarmType.PRIMARY.equals(alarmType) || AlarmType.SECONDARY.equals(alarmType)) {
			// If SmsAlarm is setup to follow the devices sound settings or not
			boolean useOsSoundSettings = (Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.USE_OS_SOUND_SETTINGS_KEY, DataType.BOOLEAN, context);

			// Only if device is not in ringer mode silent or if SmsAlarm is set to not follow the devices sound settings
			if (SoundHandler.getRingerMode(context) != AudioManager.RINGER_MODE_SILENT || !useOsSoundSettings) {
				// To store resolved vibration into
				String vibration = "";

				// Resolve correct alarm signal id
				if (AlarmType.PRIMARY.equals(alarmType)) {
					vibration = (String) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_ALARM_VIBRATION_KEY, DataType.STRING, context, VIBRATION_PATTERN_SMS_ALARM);
				} else {
					vibration = (String) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_ALARM_VIBRATION_KEY, DataType.STRING, context, VIBRATION_PATTERN_SMS_ALARM);
				}

				// Check if the vibration patterns map contains the resolved vibration, if not set it to default
				if (!vibrationPatterns.containsKey(vibration)) {
					vibration = VIBRATION_PATTERN_SMS_ALARM;
				}

				// In case Vibrator is already vibrating stop it
				cancelVibrator();

				// Ensure we got a fresh Vibrator object
				vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

				// Start vibrate
				vibrator.vibrate(vibrationPatterns.get(vibration), -1);
			}
		} else {
			// This is weird, log this case
			if (SmsAlarm.DEBUG) {
				Log.e(LOG_TAG + ":alarm()", "Method called with the unsupported AlarmType: \"" + alarmType.toString() + "\", check why. However application can't decide how to handle this case");
			}
		}
	}

	/**
	 * To cancel this {@link VibrationHandler}'s instance of {@link Vibrator} if it's not <code>null</code>.<br>
	 * The <code>Vibrator</code> will be fully released by invoking following method:
	 * <ul>
	 * <li>{@link Vibrator#cancel()}</li>
	 * </ul>
	 * <p>
	 * At last the <code>Vibrator</code> object will be <code>nullified</code>.
	 */
	public void cancelVibrator() {
		if (vibrator != null) {
			vibrator.cancel();
			vibrator = null;
		}
	}

	/**
	 * To get a list of all available <b><i>VibrationPatterns</i></b>.
	 * 
	 * @return All available vibration patterns.
	 */
	public List<String> getVibrationPatterns() {
		return new ArrayList<String>(vibrationPatterns.keySet());
	}
}
