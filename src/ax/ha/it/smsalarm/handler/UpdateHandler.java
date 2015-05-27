/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.handler;

import java.util.List;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import ax.ha.it.smsalarm.activity.Acknowledge.AcknowledgeMethod;
import ax.ha.it.smsalarm.activity.SmsAlarm;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.DataType;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.PrefKey;

/**
 * Responsible for any <b><i>update actions</i></b>. In other words if some changes has been made in current release of the application that needs to
 * have some code executed(action) done after update that code is managed by this class. Code that needs to be executed after update is placed in
 * {@link #onCreate()} method.<br>
 * To figure out if the application has been updated the <b><i>version code</i></b> is stored in {@link SharedPreferences}, and depending on logic in
 * the {@link #onCreate()} method correct updates are made, if any are needed.<br>
 * If it's a <b><i>new installation</i></b> of Sms Alarm the current version code is stored into shared preferences.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.2.1
 */
public class UpdateHandler extends Application {
	private static final String LOG_TAG = UpdateHandler.class.getSimpleName();

	// To handle shared preferences
	private final SharedPreferencesHandler prefHandler = SharedPreferencesHandler.getInstance();

	// Base value for a fresh installation
	private static final int NEW_INSTALLATION = -1;

	// Different update code levels, named as code level limit for update and a short description
	private static final int LVL_9_CHANGE_DATATYPE = 9;
	private static final int LVL_15_CHANGE_DATATYPE_RENAME_SHARED_PREFERENCES = 15;
	private static final int LVL_19_EXTENDED_ACKNOWLEDGE_FUNCTIONALTIY = 19;
	private static final int LVL_20_RENAME_SHARED_PREFERENCES = 20;

	// To store both the current and old version code in
	private int currentVersionCode;
	private int oldVersionCode;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate() {
		// Very, very important to call onCreate()
		super.onCreate();

		try {
			// Get both the current and the old version codes, if it's a new installation(no old version code exists)
			// the default value to be fetched from the shared preferences is set to the current version code
			currentVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
			oldVersionCode = (Integer) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.VERSION_CODE, DataType.INTEGER, this, NEW_INSTALLATION);

			// If it's not a new installation
			if (oldVersionCode != NEW_INSTALLATION) {
				// Only if old version number is less than 9, in version 9 a list of primary listen numbers is used instead of a string
				if (oldVersionCode < LVL_9_CHANGE_DATATYPE) {
					// Get the primary SMS number from the shared preferences, also get the new list of primary SMS numbers from the shared
					// preferences
					String primarySmsNumber = (String) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_NUMBER_KEY, DataType.STRING, this);
					List<String> primarySmsNumbers = (List<String>) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_NUMBERS_KEY, DataType.LIST, this);

					// If there is an existing number, add it to the list of primary SMS numbers and set an empty string to
					// the primary SMS number as it's not used any more
					if (primarySmsNumber.length() != 0) {
						primarySmsNumbers.add(primarySmsNumber);
						primarySmsNumber = "";
					}

					// Store both the list of primary SMS numbers and the empty string of primary SMS number not in use anymore
					prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_NUMBER_KEY, primarySmsNumber, this);
					prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_NUMBERS_KEY, primarySmsNumbers, this);

					// Store the update code level to shared preferences, in this way if we have update on higher code level that will be run next
					// time this method is executed
					prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.VERSION_CODE, LVL_9_CHANGE_DATATYPE, this);

					// Setting old version code to this update code level, this let's the next update action to take place
					oldVersionCode = LVL_9_CHANGE_DATATYPE;
				}

				// Only if old version number is less than 15, in version 15 strings is used to identify selected alarm signals instead of integer
				if (oldVersionCode < LVL_15_CHANGE_DATATYPE_RENAME_SHARED_PREFERENCES) {
					// Resolve the old alarm signal Id's and store them
					int primaryAlarmSignalId = (Integer) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_MESSAGE_TONE_KEY, DataType.INTEGER, this);
					int secondaryAlarmSignalId = (Integer) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_MESSAGE_TONE_KEY, DataType.INTEGER, this);

					prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_ALARM_SIGNAL_KEY, SoundHandler.getInstance().resolveAlarmSignal(this, primaryAlarmSignalId), this);
					prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_ALARM_SIGNAL_KEY, SoundHandler.getInstance().resolveAlarmSignal(this, secondaryAlarmSignalId), this);

					// Reset the old once
					prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_MESSAGE_TONE_KEY, 0, this);
					prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_MESSAGE_TONE_KEY, 0, this);

					// Resolve the old play alarm signal twice and store it in new shared preferences
					boolean playAlarmSignalTwice = (Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.PLAY_TONE_TWICE_KEY, DataType.BOOLEAN, this);

					prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PLAY_ALARM_SIGNAL_TWICE_KEY, playAlarmSignalTwice, this);

					// Reset the old one
					prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PLAY_TONE_TWICE_KEY, false, this);

					prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.VERSION_CODE, LVL_15_CHANGE_DATATYPE_RENAME_SHARED_PREFERENCES, this);

					oldVersionCode = LVL_15_CHANGE_DATATYPE_RENAME_SHARED_PREFERENCES;
				}

				// Only if old version code is less than 19, in version 19 acknowledge functionality was extended and existing settings must be taken
				// care of
				if (oldVersionCode < LVL_19_EXTENDED_ACKNOWLEDGE_FUNCTIONALTIY) {
					// If acknowledge of alarm is used, set the acknowledge method to CALL, as it's the only old way of acknowledge an alarm, don't
					// care about the phone number as it's using the same key as before this extension of acknowledgement
					if ((Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.ENABLE_ACK_KEY, DataType.BOOLEAN, this)) {
						prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.ACK_METHOD_KEY, AcknowledgeMethod.CALL.ordinal(), this);
					}

					oldVersionCode = LVL_19_EXTENDED_ACKNOWLEDGE_FUNCTIONALTIY;
				}

				// Only if old version code is less than 20, in version 20 shared preferences RESCUE_SERVICE_NAME is renamed to ORGANIZATION_KEY
				if (oldVersionCode < LVL_20_RENAME_SHARED_PREFERENCES) {
					// Just fetch the existing value for rescue service and store it into organization instead
					String rescueService = (String) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.RESCUE_SERVICE_KEY, DataType.STRING, this);
					prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.ORGANIZATION_KEY, rescueService, this);

					oldVersionCode = LVL_20_RENAME_SHARED_PREFERENCES;
				}

				// The old version code is larger than or equal the latest update level code, this tells us that all updates has been done or no
				// update actions needed, store the latest version code
				if (oldVersionCode >= LVL_19_EXTENDED_ACKNOWLEDGE_FUNCTIONALTIY || oldVersionCode < currentVersionCode) {
					prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.VERSION_CODE, currentVersionCode, this);
				}
			} else {
				// It's a new installation, just store the recent version code.
				prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.VERSION_CODE, currentVersionCode, this);
			}
		} catch (NameNotFoundException e) {
			if (SmsAlarm.DEBUG) {
				Log.e(LOG_TAG + ":onCreate()", "Name of application package could not be found", e);
			}
		} catch (Exception e) {
			if (SmsAlarm.DEBUG) {
				Log.e(LOG_TAG + ":onCreate()", "An error occurred while handling update actions, version code is not updated. Current version code is: \"" + currentVersionCode + "\", old version code is: \"" + oldVersionCode + "\"", e);
			}
		}
	}
}
