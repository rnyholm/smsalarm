/*
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */

package ax.ha.it.smsalarm;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
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
 * @date 2013-07-05
 * 
 * @see #onCreate(Bundle)
 */
public class Splash extends Activity {
	// Log tag string
	private String LOG_TAG = "Splash";

	// Objects needed for logging, shared preferences and noise handling
	private LogHandler logger = LogHandler.getInstance();	
	private PreferencesHandler prefHandler = PreferencesHandler.getInstance();

	// Time before the activities switch
	private int delay = 5000;

	// Handler for delayed thread
	private Handler handler;
	
	// Variable indicating whether user license is agreed or not
	private boolean userLicenseAgreed = false;

	/**
	 * When activity starts, this method is the entry point. The splash screen
	 * is built up within this method.
	 * 
	 * @param savedInstanceState
	 *            Bundle
	 * 
	 * @see #switchActivity()
	 * @see #onPause()
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Remove title on activity
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.splash);

		// Some logging for information and debugging
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onCreate()", "Layout has been set with correct settings");
		
		// Retrieve value from shared preferences, this is to decide if user has agreed user the user license before or not
		this.userLicenseAgreed = (Boolean) this.prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.USER_LICENSE_AGREED, DataTypes.BOOLEAN, this, false);

		// Get a handle to the layout by finding it's id
		RelativeLayout splashRelativeLayout = (RelativeLayout) findViewById(R.id.splash_rl);

		// Create object that acts as listener to the sbStartView
		splashRelativeLayout.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// User is only able to switch activity if he/her has agreed the user license
				if (userLicenseAgreed) {
					// Some logging for information and debugging
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().onClickListener.onClick()", "User has tapped screen");
					// Switch activity
					switchActivity();
				}
			}
		});

		// Initialize a handler object, used to put a thread to sleep
		this.handler = new Handler();
		this.handler.postDelayed(new Runnable() {
			public void run() {
				// Only switch activity if user has agreed the user license
				if (userLicenseAgreed) {
					// Some logging for information and debugging
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().Handler.run()", "Time has elapsed");
					// Start activity after thread has been a sleep for a given
					// delay time
					switchActivity();
				}
			}
		}, delay);

		// If user hasn't agreed the user license, show ULA and let user agree or disagree
		if (!this.userLicenseAgreed) {
			this.buildAndShowULADialog();
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
	
	private void buildAndShowULADialog() {
    	// Logging
    	this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":buildAndShowAboutDialog()", "Start building about dialog");   	
 
       	//Build up the alert dialog
    	AlertDialog.Builder dialog = new AlertDialog.Builder(this);
    	
    	LayoutInflater factory = LayoutInflater.from(this);
    	
    	final View view = factory.inflate(R.layout.about, null);

    	//Set attributes
    	dialog.setIcon(android.R.drawable.ic_dialog_info);
    	dialog.setTitle(R.string.ABOUT);
    	dialog.setView(view);
    	dialog.setCancelable(false);
    	
    	// Logging
    	this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":buildAndShowAboutDialog()", "Dialog attributes set");    	
    	
    	//Set a neutral button
    	dialog.setNeutralButton(R.string.OK, new DialogInterface.OnClickListener() {
    		@Override
    		public void onClick(DialogInterface dialog, int whichButton) {
    			// DO NOTHING, except logging
    			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowAboutDialog().NeutralButton.OnClickListener().onClick()", "Neutral Button pressed in dialog, nothing done");	
    		}
    	});
    	
    	// Logging
    	this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":buildAndShowAboutDialog()", "Showing dialog");    	

    	// Show dialog
    	dialog.show();        		
	}

	/**
	 * Method to switch activity. Called either when user tap screen or delay
	 * has passed.
	 * 
	 * @see #onCreate(Bundle)
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
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
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	private void removeMsgFrHandler() {
		this.handler.removeMessages(0);

		// Some logging for information and debugging
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":removeMsgFrHandler", "Messages have been removed from Handler");
	}
}
