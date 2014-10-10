/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
import ax.ha.it.smsalarm.handler.PreferencesHandler;
import ax.ha.it.smsalarm.handler.PreferencesHandler.DataType;
import ax.ha.it.smsalarm.handler.PreferencesHandler.PrefKey;

/**
 * Activity just to show splash screen and after a certain time or a tap on screen activity switch to activity {@link SmsAlarm}.<br>
 * If user hasn't agreed the EULA, a dialog showing that will be visible. If user doesn't accept it then he/she will not be able to start or enable
 * <b><i>Sms Alarm</i></b>.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.1
 */
public class Splash extends FragmentActivity {
	// To handle the shared preferences
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
	 * Perform initialization of <code>Layout</code>'s, fetching {@link SharedPreferences}, setting up {@link View}'s, setting different
	 * <code>Listener</code>'s and other things that's needed by this {@link Activity} in order for it to work.
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
					switchActivity();
				}
			});

			// Initialize a handler object, used to put a thread to sleep, activity will be switched after thread has been a sleep for a given time
			handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					switchActivity();
				}
			}, delay);
		} else { // Else show dialog requesting for user to agree the license
			EulaDialog dialog = new EulaDialog();
			dialog.show(getSupportFragmentManager(), EulaDialog.EULA_DIALOG_TAG);
		}
	}

	/**
	 * To handle an <b><i>positive on click action</i></b> from the {@link EulaDialog}.
	 * 
	 * @see EulaDialog
	 */
	public void doPositiveClick() {
		// Put end user license agreed in shared preferences so we don't show this dialog again, also enable Sms Alarm and at last switch activity
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.END_USER_LICENSE_AGREED, true, Splash.this);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.ENABLE_SMS_ALARM_KEY, true, Splash.this);
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
	 * So switch activity to {@link SmsAlarm}.
	 */
	private void switchActivity() {
		Intent saIntent = new Intent(this, SmsAlarm.class);
		startActivity(saIntent);
	}

	/**
	 * To remove any messages from {@link Handler}.
	 */
	private void removeMessagesFromHandler() {
		// Null check in case user didn't agree the EULA
		if (handler != null) {
			handler.removeMessages(0);
		}
	}
}
