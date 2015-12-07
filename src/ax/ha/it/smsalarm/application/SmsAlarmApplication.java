/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.application;

import java.util.List;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.v4.app.Fragment;
import android.util.Log;
import ax.ha.it.smsalarm.activity.Acknowledge.AcknowledgeMethod;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.DataType;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.PrefKey;
import ax.ha.it.smsalarm.handler.SoundHandler;

/**
 * Application class, which have two main purposes:
 * <p>
 * <b><i>1.</i></b> Handle any eventual events upon update of application, in practice some code are executed.<br>
 * <b><i>2.</i></b> Serve as a handler for the Google Analytics tracking services. By setting up the Google Analytics services within the application it can be accessible through over the application.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.2.1
 * @see GoogleAnalyticsHandler
 */
public class SmsAlarmApplication extends Application {
	private static final String LOG_TAG = SmsAlarmApplication.class.getSimpleName();

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

	/**
	 * Creates a new instance of {@link SmsAlarmApplication}.
	 */
	public SmsAlarmApplication() {
		super();
	}

	@Override
	public void onCreate() {
		// Very, very important to call onCreate()
		super.onCreate();

		// Initialize google analytics handler as soon as possible in case any exceptions occurs at an early stage
		GoogleAnalyticsHandler.initialize(this);

		// Handle updates if needed
		handleUpdates();
	}

	/**
	 * Responsible for any <b><i>update actions</i></b>. In other words if some changes has been made in current release of the application that needs to have some code executed(action) done after
	 * update that code is managed by this method. Code that needs to be executed after update is placed within this method.<br>
	 * To figure out if the application has been updated the <b><i>version code</i></b> is stored in {@link SharedPreferences}, and depending on logic in this method correct updates are made, if any
	 * are needed.<br>
	 * If it's a <b><i>new installation</i></b> of Sms Alarm the current version code is stored into shared preferences.
	 */
	@SuppressWarnings("unchecked")
	private void handleUpdates() {
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
					// Just fetch the existing value for rescue service and store it into organization instead, also set the rescue service name pref
					// to empty
					String rescueService = (String) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.RESCUE_SERVICE_KEY, DataType.STRING, this);
					prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.RESCUE_SERVICE_KEY, "", this);
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
			Log.e(LOG_TAG + ":onCreate()", "Name of application package could not be found", e);
		} catch (Exception e) {
			Log.e(LOG_TAG + ":onCreate()", "An error occurred while handling update actions, version code is not updated. Current version code is: \"" + currentVersionCode + "\", old version code is: \"" + oldVersionCode + "\"", e);
		}
	}

	/**
	 * A handler/utility class for all interaction with Google Analytics. This class should be <code>null</code> safe.<br>
	 * <b><i>Note.</i></b> It's very important to initialize this class before usage, a hard failure is guaranteed in all other cases!
	 * 
	 * @author Robert Nyholm <robert.nyholm@aland.net>
	 * @version 2.3.1
	 * @since 2.3.1
	 */
	public static class GoogleAnalyticsHandler {
		/**
		 * Different possible report rules which can be used in combination with any other more complex you wan't. This report rule decides whether or not the associated object should be reported, get
		 * it's data anonymized or get the data reported straight of.
		 * 
		 * @author Robert Nyholm <robert.nyholm@aland.net>
		 * @version 2.3.1
		 * @since 2.3.1
		 */
		public enum ReportRule {
			NO_REPORT, REPORT_ANONYMIZE, REPORT_RAW;
		}

		/**
		 * Different possible categories for events. Use this one when sending events to Google Analytics using the {@link Tracker}.<br>
		 * Each category holds a separate report name, a more readable name. It's this name that get's sent to Google Analytics.
		 * 
		 * @author Robert Nyholm <robert.nyholm@aland.net>
		 * @version 2.3.1
		 * @since 2.3.1
		 */
		public static enum EventCategory {
			// @formatter:off
			ACKNOWLEDGE("Acknowledge"), ALARM("Alarm"), SETTINGS("Settings"), USER_INTERFACE("User interface");
			// @formatter:on

			// Report text of this category
			private final String reportText;

			/**
			 * Creates a new {@link EventCategory} with given report text.
			 * 
			 * @param reportText
			 *            Report text of <code>EventCategory</code>.
			 */
			private EventCategory(String reportText) {
				this.reportText = reportText;
			}

			/**
			 * Get the report text of this {@link EventCategory}.
			 * 
			 * @return The report text.
			 */
			public String getReportText() {
				return reportText;
			}
		}

		/**
		 * Different possible actions for events. Use this one when sending events to Google Analytics using the {@link Tracker}.<br>
		 * Each action holds a separate report name, a more readable name. It's this name that get's sent to Google Analytics.
		 * 
		 * @author Robert Nyholm <robert.nyholm@aland.net>
		 * @version 2.3.1
		 * @since 2.3.1
		 */
		public static enum EventAction {
			// @formatter:off
			ALARM_TRIGGERED("Alarm of any type triggered"), ACKNOWLEDGE_DONE("Acknowledgement done"), DEBUG_MENU_TOGGLE("Debug menu toggle"), SECONDARY_ALARM_TRIGGERED("Secondary alarm triggered"), SETTINGS_CHANGED("Settings changed"), PRIMARY_ALARM_TRIGGERED(
					"Primary alarm triggered"), WIDGET_INTERACTION("Widget interaction");
			// @formatter:on

			// Report text of this action
			private final String reportText;

			/**
			 * Creates a new {@link EventAction} with given report text.
			 * 
			 * @param reportText
			 *            Report text of <code>EventAction</code>.
			 */
			private EventAction(String reportText) {
				this.reportText = reportText;
			}

			/**
			 * Get the report text of this {@link EventAction}.
			 * 
			 * @return The report text.
			 */
			public String getReportText() {
				return reportText;
			}
		}

		// The Google Analytics tracking ID
		private static final String TRACKING_ID_DEVELOPMENT = "UA-66577026-1";
		private static final String TRACKING_ID_PRODUCTION = "UA-66577026-2";

		// Need objects for Google Analytics and tracker
		private static GoogleAnalytics analytics;
		private static Tracker tracker;

		/**
		 * To initialize {@link GoogleAnalytics} and {@link Tracker} objects within this {@link GoogleAnalyticsHandler}. <br>
		 * <b><i>Note.</i></b> It's mandatory to run this method before any usage of the handler can be made.
		 * 
		 * @param application
		 *            {@link Application} from where to get a instance of <code>GoogleAnalytics</code>.
		 */
		private static void initialize(Application application) {
			// Setup Google Analytics...
			analytics = GoogleAnalytics.getInstance(application);
			analytics.setLocalDispatchPeriod(1800);
			analytics.setDryRun(false);

			// ...and the tracker, remember to use correct tracking id
			tracker = analytics.newTracker(TRACKING_ID_PRODUCTION);
			tracker.enableExceptionReporting(true);
			tracker.enableAutoActivityTracking(true);
			tracker.setSampleRate(100.0);
		}

		/**
		 * To access the global Google Analytics singleton.
		 * 
		 * @return Global singleton instance of Google Analytics as a {@link GoogleAnalytics} object.
		 * @throws IllegalStateException
		 *             in case <code>GoogleAnalytics</code> is'nt correctly instantiated.
		 */
		public static GoogleAnalytics analytics() throws IllegalStateException {
			if (analytics == null) {
				throw new IllegalStateException("GoogleAnalytics not instantiated");
			}

			return analytics;
		}

		/**
		 * To access the application tracker.
		 * 
		 * @return Application tracker instance as a {@link Tracker} object.
		 * @throws IllegalStateException
		 *             in case <code>Tracker</code> is'nt correctly instantiated.
		 */
		public static Tracker tracker() throws IllegalStateException {
			if (tracker == null) {
				throw new IllegalStateException("Tracker not instantiated");
			}

			return tracker;
		}

		/**
		 * Convenience method to report given {@link Activity} as started through {@link GoogleAnalytics}.
		 * 
		 * @param activity
		 *            <code>Activity</code> which will be reported as started.
		 */
		public static void reportActivityStart(Activity activity) {
			if (activity != null) {
				analytics().reportActivityStart(activity);
			}
		}

		/**
		 * Convenience method to report given {@link Activity} as stopped through {@link GoogleAnalytics}.
		 * 
		 * @param activity
		 *            <code>Activity</code> which will be reported as stopped.
		 */
		public static void reportActivityStop(Activity activity) {
			if (activity != null) {
				analytics().reportActivityStop(activity);
			}
		}

		/**
		 * Convenience method to set screen name of {@link Tracker} and send a screen view hit to Google Analytics.
		 * 
		 * @param activity
		 *            {@link Activity} which name will be set as screen name.
		 */
		public static void setScreenNameAndSendScreenViewHit(Activity activity) {
			if (activity != null) {
				tracker().setScreenName(activity.getClass().getName());
				tracker().send(new HitBuilders.ScreenViewBuilder().build());
			}
		}

		/**
		 * Convenience method to set screen name of {@link Tracker} and send a screen view hit to Google Analytics.
		 * 
		 * @param fragment
		 *            {@link Fragment} which name will be set as screen name.
		 */
		public static void setScreenNameAndSendScreenViewHit(Fragment fragment) {
			if (fragment != null) {
				tracker().setScreenName(fragment.getClass().getName());
				tracker().send(new HitBuilders.ScreenViewBuilder().build());
			}
		}

		/**
		 * Convenience method to send an event to Google Analytics through the {@link Tracker}.
		 * 
		 * @param eventCategory
		 *            {@link EventCategory} of the event.
		 * @param eventAction
		 *            {@link EventAction} of the event.
		 * @param label
		 *            Label of the event.
		 */
		public static void sendEvent(EventCategory eventCategory, EventAction eventAction, String label) {
			if (eventCategory != null && eventAction != null && label != null) {
				tracker().send(new HitBuilders.EventBuilder(eventCategory.getReportText(), eventAction.getReportText()).setLabel(label).build());
			}
		}

		/**
		 * Convenience method to send an event to Google Analytics telling some {@link SharedPreferences} has changed, and in case the data should be reported to Google Analytics that data will be
		 * sent as well, see {@link ReportRule} for more info about that.
		 * 
		 * @param prefs
		 *            <code>SharedPreferences</code> from which object changed will be fetched from.
		 * @param key
		 *            Key in <code>SharedPreferences</code> which object has changed.
		 */
		public static void sendSettingsChangedEvent(SharedPreferences prefs, String key) {
			if (prefs != null && key != null) {
				// Resolve the key
				PrefKey changedPrefKey = PrefKey.of(key);

				// Figure out if the changed preferences on this key should be reported to Google Analytics
				if (!ReportRule.NO_REPORT.equals(changedPrefKey.getReportRule())) {

					// Sanity control in case wrong PrefKey was resolved
					if (prefs.contains(changedPrefKey.getKey())) {
						// Fetch the object
						Object object = prefs.getAll().get(changedPrefKey.getKey());

						// When integers or booleans are reported, we don't take the ReportRule.REPORT_ANONYMIZE in consideration. We also don't
						// need to cast anything as String.valueOf() will work fine for bare objects of these classes
						if (object instanceof Integer || object instanceof Boolean) {
							// Special handling for this shared preference, we want to send a resolved name for the acknowledge method, not the
							// integer which is stored
							if (PrefKey.ACK_METHOD_KEY.equals(changedPrefKey) && object instanceof Integer) {
								object = AcknowledgeMethod.of((Integer) object).getReportText();
							}
							tracker().send(new HitBuilders.EventBuilder(EventCategory.SETTINGS.getReportText(), EventAction.SETTINGS_CHANGED.getReportText()).setLabel(changedPrefKey.getReportText() + ": " + String.valueOf(object)).build());
						} else if (object instanceof String) { // Lists will be treated as strings as well
							// Figure out if the data should be reported straight of or not
							if (ReportRule.REPORT_RAW.equals(changedPrefKey.getReportRule())) {
								tracker().send(new HitBuilders.EventBuilder(EventCategory.SETTINGS.getReportText(), EventAction.SETTINGS_CHANGED.getReportText()).setLabel(changedPrefKey.getReportText() + ": " + String.valueOf(object)).build());
							} else {
								// Translate string to true or false, if string contains any real data then send true else false
								boolean used = ((String) object).length() > 0 ? true : false;
								tracker().send(new HitBuilders.EventBuilder(EventCategory.SETTINGS.getReportText(), EventAction.SETTINGS_CHANGED.getReportText()).setLabel(changedPrefKey.getReportText() + ": " + String.valueOf(used)).build());
							}
						}
					}
				}
			}
		}
	}
}
