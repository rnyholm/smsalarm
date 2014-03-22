/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm;

import java.util.List;

import android.app.Application;
import android.content.pm.PackageManager.NameNotFoundException;
import ax.ha.it.smsalarm.LogHandler.LogPriorities;
import ax.ha.it.smsalarm.PreferencesHandler.DataTypes;
import ax.ha.it.smsalarm.PreferencesHandler.PrefKeys;

/**
 * Responsible for any <b><i>update actions</i></b>. In other words if some changes has been made in
 * current release of the application that needs to have some code executed(action) done after
 * update that code is managed by this class. Code that needs to be executed after update is placed
 * in {@link #onCreate()} method.<br>
 * To figure out if the application has been updated the <b><i>version code</i></b> is stored in
 * <code>SharedPreferences</code>, and at the end of the {@link #onCreate()} method the current
 * version code is <b><i>always</i></b> stored to shared preferences, this way that code within
 * {@link #onCreate()} is executed more than one time.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.2.1
 * @since 2.2.1
 */
public class UpdateHandler extends Application {
	// Log tag string
	private final String LOG_TAG = getClass().getSimpleName();

	// Objects needed for logging, shared preferences and noise handling
	private final LogHandler logger = LogHandler.getInstance();
	private final PreferencesHandler prefHandler = PreferencesHandler.getInstance();

	// To store both the current and old version code in
	private int currentVersionCode;
	private int oldVersionCode;

	// To indicate if update actions has been made
	private boolean updateActionMade = false;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate() {
		// Very, very important to call onCreate()
		super.onCreate();

		try {
			// Get both the current and the old version codes, if it's a new installation(no old
			// version code exists)
			// the default value to be fetched from the shared preferences is set to the current
			// version code
			currentVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
			oldVersionCode = (Integer) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.VERSION_CODE, DataTypes.INTEGER, this, -1);

			// Only if old version number is less than 9, in version 9 a
			// list of primary listen numbers is used instead of a string
			if (oldVersionCode < 9) {
				// Some logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate()", "Old version code is lower than 9, need to move existing PRIMARY sms number to the new list of PRIMARY sms numbers. Old version code is: \"" + oldVersionCode + "\"");

				// Get the primary sms number from the shared preferences, also get the new list of
				// primary sms numbers from the shared preferences
				String primarySmsNumber = (String) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_LISTEN_NUMBER_KEY, DataTypes.STRING, this);
				List<String> primarySmsNumbers = (List<String>) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_LISTEN_NUMBERS_KEY, DataTypes.LIST, this);

				// If there is an existing number, add it to the list of primary sms numbers
				// and set an empty string to the primary sms number as it's not used any more
				if (primarySmsNumber.length() != 0) {
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate()", "The existing PRIMARY sms number: \"" + primarySmsNumber + "\" is about to be moved to the new list of PRIMARY sms numbers");
					primarySmsNumbers.add(primarySmsNumber);
					primarySmsNumber = "";
				}

				// Store both the list of primary sms numbers and the empty string of primary sms
				// number not in use anymore
				prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_LISTEN_NUMBER_KEY, primarySmsNumber, this);
				prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_LISTEN_NUMBERS_KEY, primarySmsNumbers, this);

				// At last flag the boolean to true to indicate that update actions has been
				// successfully made
				updateActionMade = true;
			}

			// Only update version code in shared preferences if any update actions has been made
			if (updateActionMade) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate()", "Update actions has been made and the current version code: \"" + currentVersionCode + "\" is about to be stored to the shared preferences");
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
