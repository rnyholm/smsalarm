/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.RelativeLayout;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import ax.ha.it.smsalarm.LogHandler.LogPriorities;
import ax.ha.it.smsalarm.PreferencesHandler.DataTypes;
import ax.ha.it.smsalarm.PreferencesHandler.PrefKeys;

/**
 * Splash activity, just shows splash screen and after a certain time or a tap
 * on screen activity switch to SmsAlarm.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.1
 * @since 2.1
 * 
 * @see #onCreate(Bundle)
 */
public class Splash extends Activity {
	// Log tag string
	private String LOG_TAG = this.getClass().getSimpleName();

	// Objects needed for logging, shared preferences and noise handling
	private LogHandler logger = LogHandler.getInstance();
	private PreferencesHandler prefHandler = PreferencesHandler.getInstance();

	// Time before the activities switch
	private int delay = 5000;

	// Handler for delayed thread
	private Handler handler;

	// Variable indicating whether user license is agreed or not
	private boolean endUserLicenseAgreed = false;

	/**
	 * When activity starts, this method is the entry point. The splash screen
	 * is built up within this method.
	 * 
	 * @param savedInstanceState
	 *            Bundle
	 * 
	 * @see #buildAndShowEULADialog()
	 * @see #switchActivity()
	 * @see #onPause()
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 *      logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.PreferencesHandler#getPrefs(PrefKeys,
	 *      PrefKeys,DataTypes, android.content.Context, Object)
	 *      getPrefs(PrefKeys, PrefKeys,DataTypes, android.content.Context,
	 *      Object)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Remove title on activity
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.splash);

		// Some logging for information and debugging
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onCreate()", "Layout has been set with correct settings");

		/*
		 * Retrieve value from shared preferences, this is to decide if user has
		 * agreed user the user license before or not
		 */
		try {
			this.endUserLicenseAgreed = (Boolean) this.prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.END_USER_LICENSE_AGREED, DataTypes.BOOLEAN, this, false);
		} catch(IllegalArgumentException e) {
			logger.logCatTxt(LogPriorities.ERROR, this.LOG_TAG + ":onCreate()", "An unsupported datatype was given as argument to PreferencesHandler.getPrefs()", e);
		} 

		/*
		 * Only set up onClickListener and start Runnable if user has agreed the
		 * end user license agreement
		 */
		if (this.endUserLicenseAgreed) {
			// Get a handle to the layout by finding it's id
			RelativeLayout splashRelativeLayout = (RelativeLayout) findViewById(R.id.splash_rl);
			// Create object that acts as listener to the sbStartView
			splashRelativeLayout.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// Some logging for information and debugging
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().onClickListener.onClick()", "User has tapped screen");
					// Switch activity
					switchActivity();
				}
			});

			// Initialize a handler object, used to put a thread to sleep
			this.handler = new Handler();
			this.handler.postDelayed(new Runnable() {
				public void run() {
					// Some logging for information and debugging
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().Handler.run()", "Time has elapsed");
					// Start activity after thread has been a sleep for a given
					// delay time
					switchActivity();
				}
			}, delay);
		} else { // Else show dialog requesting for user to agree the license
			this.buildAndShowEULADialog();
		}

		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onCreate()", "Listener and Handler have been set");
	}

	/**
	 * Removes messages from handler when application pauses. Also calls it's
	 * superclass.
	 * 
	 * @see #removeMsgFrHandler()
	 */
	@Override
	public void onPause() {
		super.onPause();
		this.removeMsgFrHandler();
	}

	/**
	 * To build up and show an EULA dialog.
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 *      logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.PreferencesHandler#setPrefs(PrefKeys, PrefKeys,
	 *      Object, android.content.Context) setPrefs(PrefKeys, PrefKeys,
	 *      Object, android.content.Context)
	 */
	private void buildAndShowEULADialog() {
		// Logging
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":buildAndShowEULADialog()", "Start building EULA dialog");

		// Build up the alert dialog
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);

		// Set attributes
		dialog.setIcon(android.R.drawable.ic_dialog_info);
		dialog.setTitle(R.string.EULA_TITLE);
		dialog.setMessage(R.string.EULA);
		dialog.setCancelable(false);

		// Logging
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":buildAndShowEULADialog()", "Dialog attributes set");

		// Set a positive button
		dialog.setPositiveButton(R.string.EULA_AGREE, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Debug logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowEULADialog().PositiveButton.OnClickListener().onClick()", "Positive Button pressed in dialog, store shared preferences and switch activity");
				// Put end user license agreed in shared preferences so we don't
				// show this dialog again
				try {
					prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.END_USER_LICENSE_AGREED, true, Splash.this);
				} catch(IllegalArgumentException e) {
					logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":buildAndShowEULADialog().PositiveButton.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
				} 
				// Switch activity
				switchActivity();
			}
		});

		// Set a negative button
		dialog.setNegativeButton(R.string.EULA_DECLINE, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// Debug logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowEULADialog().NegativeButton.OnClickListener().onClick()", "Negative Button pressed in dialog, finishing activity");
				// Finish activity
				finish();
			}
		});

		// Logging
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":buildAndShowEULADialog()", "Showing dialog");

		// Show dialog
		dialog.show();
	}

	/**
	 * Method to switch activity. Called either when user tap screen or delay
	 * has passed.
	 * 
	 * @see #onCreate(Bundle)
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 *      logCat(LogPriorities, String, String)
	 */
	private void switchActivity() {
		// Create intent and start next activity from it
		Intent saIntent = new Intent(this, SmsAlarm.class);

		// Some logging for information and debugging
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":switchActivity()", "Intent has been set and application is about to switch activity to SmsAlarm");

		startActivity(saIntent);
	}

	/**
	 * Method to remove messages from handler.
	 * 
	 * @see #onPause()
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 *      logCat(LogPriorities, String, String)
	 */
	private void removeMsgFrHandler() {
		// Null check in case user didn't agree the EULA
		if (handler != null) {
			this.handler.removeMessages(0);
			// Some logging for information and debugging
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":removeMsgFrHandler", "Messages have been removed from Handler");
		}
	}
}
