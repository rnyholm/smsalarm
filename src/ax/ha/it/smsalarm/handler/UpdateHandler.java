/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.handler;

import java.util.List;

import android.app.Application;
import android.content.pm.PackageManager.NameNotFoundException;
import ax.ha.it.smsalarm.handler.LogHandler.LogPriorities;
import ax.ha.it.smsalarm.handler.PreferencesHandler.DataTypes;
import ax.ha.it.smsalarm.handler.PreferencesHandler.PrefKeys;

/**
 * Responsible for any <b><i>update actions</i></b>. In other words if some changes has been made in current release of the application that needs to
 * have some code executed(action) done after update that code is managed by this class. Code that needs to be executed after update is placed in
 * {@link #onCreate()} method.<br>
 * To figure out if the application has been updated the <b><i>version code</i></b> is stored in <code>SharedPreferences</code>, and depending on
 * logic in the {@link #onCreate()} method correct updates are made, if any are needed.<br>
 * If it's a <b><i>new installation</i></b> of Sms Alarm the current version code is stored into shared preferences.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.2.1
 */
public class UpdateHandler extends Application {
	// Log tag string
	private final String LOG_TAG = getClass().getSimpleName();

	// Objects needed for logging, shared preferences and noise handling
	private final LogHandler logger = LogHandler.getInstance();
	private final PreferencesHandler prefHandler = PreferencesHandler.getInstance();

	// Base value for a fresh installation
	private final int NEW_INSTALLATION = -1;

	// Different update code levels, named as code level limit for update and a short description
	private final int LVL_9_CHANGE_DATATYPE = 9;

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
			oldVersionCode = (Integer) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.VERSION_CODE, DataTypes.INTEGER, this, NEW_INSTALLATION);

			// If it's not a new installation
			if (oldVersionCode != NEW_INSTALLATION) {
				// Only if old version number is less than 9, in version 9 a list of primary listen numbers is used instead of a string
				if (oldVersionCode < LVL_9_CHANGE_DATATYPE) {
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate()", "Old version code is lower than 9, need to move existing PRIMARY sms number to the new list of PRIMARY sms numbers. Old version code is: \"" + oldVersionCode + "\"");

					// Get the primary sms number from the shared preferences, also get the new list of primary sms numbers from the shared
					// preferences
					String primarySmsNumber = (String) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_LISTEN_NUMBER_KEY, DataTypes.STRING, this);
					List<String> primarySmsNumbers = (List<String>) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_LISTEN_NUMBERS_KEY, DataTypes.LIST, this);

					// If there is an existing number, add it to the list of primary sms numbers and set an empty string to
					// the primary sms number as it's not used any more
					if (primarySmsNumber.length() != 0) {
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate()", "The existing PRIMARY sms number: \"" + primarySmsNumber + "\" is about to be moved to the new list of PRIMARY sms numbers");
						primarySmsNumbers.add(primarySmsNumber);
						primarySmsNumber = "";
					}

					// Store both the list of primary sms numbers and the empty string of primary sms number not in use anymore
					prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_LISTEN_NUMBER_KEY, primarySmsNumber, this);
					prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_LISTEN_NUMBERS_KEY, primarySmsNumbers, this);

					// Store the update code level to shared preferences, in this way if we have update on higher code level that will be run next
					// time this method is executed
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate()", "Update actions has been made and the update level version code: \"" + LVL_9_CHANGE_DATATYPE + "\" is about to be stored to the shared preferences");
					prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.VERSION_CODE, LVL_9_CHANGE_DATATYPE, this);

					// Setting old version code to this update code level, this let's the next update action to take place
					oldVersionCode = LVL_9_CHANGE_DATATYPE;
				}

				// No update actions needed except for storing the latest version code
				// The old version code is larger than the latest update level code, this tells us that all updates has been done
				if (oldVersionCode >= LVL_9_CHANGE_DATATYPE && oldVersionCode < currentVersionCode) {
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate()", "All updates went through or no updates were needed, storing the current version code: \"" + currentVersionCode + "\" is about to be stored to the shared preferences");
					prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.VERSION_CODE, currentVersionCode, this);
				}
			} else {
				// It's a new installation, just store the recent versioncode.
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate()", "No updates needed as this installation is a new one, the current version code: \"" + currentVersionCode + "\" is about to be stored to the shared preferences");
				prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.VERSION_CODE, currentVersionCode, this);
			}

		} catch (NameNotFoundException e) {
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":onCreate()", "Name of application package could not be found", e);
		} catch (IllegalArgumentException e) {
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":onCreate()", "An unsupported datatype was given as argument to PreferencesHandler.getPrefs()", e);
		} catch (Exception e) {
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":onCreate()", "An error occurred while handling update actions, version code is not updated. Current version code is: \"" + currentVersionCode + "\", old version code is: \"" + oldVersionCode + "\"", e);
		}
	}
}
