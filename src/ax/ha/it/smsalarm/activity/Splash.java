/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.fragment.dialog.EulaDialog;
import ax.ha.it.smsalarm.handler.LogHandler;
import ax.ha.it.smsalarm.handler.LogHandler.LogPriorities;
import ax.ha.it.smsalarm.handler.PreferencesHandler;
import ax.ha.it.smsalarm.handler.PreferencesHandler.DataType;
import ax.ha.it.smsalarm.handler.PreferencesHandler.PrefKey;

/**
 * Splash activity, just shows splash screen and after a certain time or a tap on screen activity switch to SmsAlarm.<br>
 * If user hasn't agreed the EULA, a dialog showing that will be visible. If user doesn't accept it then he/she will not be able to start or enable
 * <b><i>Sms Alarm</i></b>.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.1
 */
public class Splash extends FragmentActivity {
	private final static String LOG_TAG = Splash.class.getSimpleName();

	// Objects needed for logging, shared preferences and noise handling
	private final LogHandler logger = LogHandler.getInstance();
	private final PreferencesHandler prefHandler = PreferencesHandler.getInstance();

	// Time before the activities switch
	private final int delay = 5000;

	// Handler for delayed thread
	private Handler handler;

	// Variable indicating whether user license is agreed or not
	private boolean endUserLicenseAgreed = false;

	// For the TextView displaying version, needed in order to edit it
	private TextView versionTextView;

	/**
	 * Performs initialization of user interface components and loads needed data.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Remove title on activity
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.splash);

		// Text with correct version number
		versionTextView = (TextView) findViewById(R.id.splashVersion_tv);
		versionTextView.setText(String.format(getString(R.string.SPLASH_VERSION), getString(R.string.APP_VERSION)));

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate()", "Layout has been set with correct settings");

		// Fetch value from shared preferences, this is to decide if user has agreed user the user license before or not
		endUserLicenseAgreed = (Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.END_USER_LICENSE_AGREED, DataType.BOOLEAN, this, false);

		// Only set up onClickListener and start Runnable if user has agreed the end user license agreement
		if (endUserLicenseAgreed) {
			// Get a handle to the layout by finding it's id
			RelativeLayout splashRelativeLayout = (RelativeLayout) findViewById(R.id.splash_rl);
			// Create object that acts as listener to the sbStartView
			splashRelativeLayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().onClickListener.onClick()", "User has tapped screen");
					switchActivity();
				}
			});

			// Initialize a handler object, used to put a thread to sleep, activity will be switched after thread has been a sleep for a given time
			handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().Handler.run()", "Time has elapsed");
					switchActivity();
				}
			}, delay);
		} else { // Else show dialog requesting for user to agree the license
			EulaDialog dialog = new EulaDialog();
			dialog.show(getSupportFragmentManager(), EulaDialog.EULA_DIALOG_TAG);
		}

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate()", "Listener and Handler have been set");
	}

	/**
	 * To handle an <b><i>positive on click action</i></b> from the {@link EulaDialog}.
	 * 
	 * @see EulaDialog
	 */
	public void doPositiveClick() {
		// Put end user license agreed in shared preferences so we don't show this dialog again
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.END_USER_LICENSE_AGREED, true, Splash.this);

		switchActivity();
	}

	/**
	 * To handle an <b><i>negative on click action</i></b> from the {@link EulaDialog}.
	 * 
	 * @see EulaDialog
	 */
	public void doNegativeClick() {
		// Just finish activity
		finish();
	}

	/**
	 * Removes messages from handler when application pauses.
	 * 
	 * @see #removeMessagesFromHandler()
	 */
	@Override
	public void onPause() {
		super.onPause();
		removeMessagesFromHandler();
	}

	/**
	 * Method to switch activity. Called either when user tap screen or delay has passed.
	 */
	private void switchActivity() {
		// Create intent and start next activity from it
		Intent saIntent = new Intent(this, SmsAlarm.class);
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":switchActivity()", "Intent has been set and application is about to switch activity to SmsAlarm");
		startActivity(saIntent);
	}

	/**
	 * Method to remove messages from handler.
	 */
	private void removeMessagesFromHandler() {
		// Null check in case user didn't agree the EULA
		if (handler != null) {
			handler.removeMessages(0);
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":removeMessagesFromHandler()", "Messages have been removed from Handler");
		}
	}
}
