/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import ax.ha.it.smsalarm.LogHandler.LogPriorities;
import ax.ha.it.smsalarm.PreferencesHandler.DataTypes;
import ax.ha.it.smsalarm.PreferencesHandler.PrefKeys;

/**
 * Responsible for the application <code>ax.ha.it.smsalarm</code> acknowledge
 * activity. This class allows users to acknowledge an received alarm by calling
 * to specific phone number.<br>
 * Also holds the acknowledge UI.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.1
 * @since 1.1-SE
 */
public class AcknowledgeHandler extends Activity {
	// Log tag string
	private final String LOG_TAG = this.getClass().getSimpleName();

	// Objects needed for logging and shared preferences handling
	private LogHandler logger = LogHandler.getInstance();
	private PreferencesHandler prefHandler = PreferencesHandler.getInstance();

	// Object needed to listen for phones different states
	private ListenToPhoneState listener;
	
	// Objact to handle database access and methods
	private DatabaseHandler db;

	// Variables of different UI elements and types
	// The TextView Objects
	private TextView titleTextView;
	private TextView fullMessageTextView;
	private TextView lineBusyTextView;
	private TextView countDownTextView;
	private TextView secondsTextView;

	// The Button objects
	private Button acknowledgeButton;
	private Button abortButton;

	// The ProgressBar Object
	private ProgressBar redialProgressBar;

	// The ImageView Objects
	private ImageView divider1ImageView;
	private ImageView divider2ImageView;

	// Strings for the data presentation in UI
	private String rescueService = "";
	private String fullMessage = "";

	// String representing phone number to which we acknowledge to
	private String acknowledgeNumber = "";

	// Boolean to indicate if a called has been placed already
	private boolean hasCalled = false;

	// Boolean to keep track if we have evaluated a certain phone state
	private boolean NOT_EVALUATED = true;

	// Date variable to keep track of call times
	private Date startCall;

	// To countdown a new call
	private CountDownTimer redialCountDown;

	// Integers to keep track of the different phone states
	private int prePhoneState = -1;
	private int phoneState = -1;

	// Constants for the redial parameters
	private int MIN_CALL_TIME = 7000; // Minimum call time(in milliseconds), if below this the application redials
	private int REDIAL_COUNTDOWN_TIME = 6000; // Count down time(in milliseconds) before redial should occur
	private int REDIAL_COUNTDOWN_INTERVAL = 100;

	/**
	 * When activity starts, this method is the entry point. The User Interface
	 * is built up and different <code>Listeners</code> are set within this
	 * method.
	 * 
	 * @param savedInstanceState
	 *            Default Bundle
	 * 
	 * @see #findViews()
	 * @see #getAckHandlerPrefs()
	 * @see #setTextViews()
	 * @see #onResume()
	 * @see #onPause()
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String , String) logCat(LogPriorities, String , String)
	 * @see ax.ha.it.smsalarm.LogHandler#logAlarm(List, Context) logAlarm(List, Context)
	 * @see ax.ha.it.smsalarm.DatabaseHandler ax.ha.it.smsalarm.DatabaseHandler
	 * @see ax.ha.it.smsalarm.DatabaseHandler#updateLatestAlarmAcknowledged() updateLatestAlarmAcknowledged()
	 * @see ax.ha.it.smsalarm.DatabaseHandler#getAllAlarm() getAllAlarm()
	 * @see ax.ha.it.smsalarm.Alarm ax.ha.it.smsalarm.Alarm
	 * @see ax.ha.it.smsalarm.WidgetProvider#updateWidgets(Context) @see ax.ha.it.smsalarm.WidgetProvider#updateWidgets(Context)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ack);

		// Log in debugging and information purpose
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onCreate()", "Creation of the Acknowledge Handler started");
		
		// Initialize database handler object from context
		this.db = new DatabaseHandler(this);

		// Declare a telephonymanager with propersystemservice and attach
		// listener to it
		TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		listener = new ListenToPhoneState();
		tManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);

		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onCreate()", "Got TELEPHONY_SERVICE and attached PhoneStateListener to it");

		// FindViews
		this.findViews();

		// Get Shared Preferences
		this.getAckHandlerPrefs();

		// Set TextViews
		this.setTextViews();

		// Create objects that acts as listeners to the buttons
		abortButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().abortButton.OnClickListener().onClick()", "Abort button has been pressed, finishing activity");
				finish();
			}
		});

		// Create objects that acts as listeners to the buttons
		acknowledgeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Check to see if we have any phone number to acknowledge to
				if (!acknowledgeNumber.equals("")) {
					// Logging
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().acknowledgeButton.OnClickListener().onClick()", "Acknowledge button has been pressed and phone number to acknowledge to exist. Continue acknowledge");
					// Update acknowledgetime in database
					db.updateLatestAlarmAcknowledged();
					// Get all alarms from database and log them to to html file
					logger.logAlarm(db.getAllAlarm(), AcknowledgeHandler.this);
					// Update all widgets associated with this application
					WidgetProvider.updateWidgets(AcknowledgeHandler.this);
					// Place the acknowledge call
					placeAcknowledgeCall();
				} else {
					Toast.makeText(AcknowledgeHandler.this, R.string.ACK_CANNOT, Toast.LENGTH_LONG).show();
					// Logging
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().acknowledgeButton.OnClickListener().onClick()", "Acknowledge button has been pressed but no phone number to acknowledge to has been given");
				}
			}
		});
		// Log in debugging and information purpose
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onCreate()", "Creation of the Acknowledge Handler completed");
	}

	/**
	 * To handler events to trigger when activity resumes.
	 * 
	 * @see #onCreate(Bundle)
	 * @see #onPause()
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String , String) logCat(LogPriorities, String , String)
	 */
	@Override
	public void onResume() {
		super.onResume();
		// Log in debugging and information purpose
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onResume()", "Activity resumed");
		// If we already have placed a call
		if (this.hasCalled) {
			// Logging
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onResume()", "An acknowledge call has already been placed, building up progressbar and countdown for a new acknowledge call");
			// Initialize progress bar and textviews needed for countdown
			this.redialProgressBar.setProgress(0);
			this.countDownTextView.setText(Integer.toString(REDIAL_COUNTDOWN_TIME / 1000));
			this.redialCountDown = new CountDownTimer(REDIAL_COUNTDOWN_TIME, REDIAL_COUNTDOWN_INTERVAL) {
				@Override
				public void onTick(long millisUntilFinished) {
					// Logging
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onResume().CountDownTimer().onTick()", "Calculate redial countdown times and update UI widgets");
					// Calculate new value of ProgressBar
					float fraction = millisUntilFinished / (float) REDIAL_COUNTDOWN_TIME;
					// Update ProgressBar and TextView with new values
					countDownTextView.setText((millisUntilFinished / 1000) + "");
					redialProgressBar.setProgress((int) (fraction * REDIAL_COUNTDOWN_INTERVAL));
				}

				@Override
				public void onFinish() {
					// Logging
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onResume().CountDownTimer().onFinish()", "Redial countdown finished, continue to place new acknowledge call");
					// Place the acknowledge call
					placeAcknowledgeCall();
				}
			}.start();
		}
	}

	/**
	 * To handle events to trigger when activity pauses. <b><i>Not yet
	 * implemented.</i></b>
	 * 
	 * @see #onCreate(Bundle)
	 * @see #onResume()
	 */
	@Override
	public void onPause() {
		super.onPause();
	}

	/**
	 * To place a acknowledge call to a preconfigured phone number.
	 * 
	 * @see #onCreate(Bundle)
	 * @see #onResume()
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String , String) logCat(LogPriorities, String , String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String , String, Throwable) logCatTxt(LogPriorities, String , String, Throwable)
	 * @see ax.ha.it.smsalarm.PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context) setPrefs(PrefKeys, PrefKeys, Object, Context)
	 */
	private void placeAcknowledgeCall() {
		try {
			// Make a call intent
			Intent callIntent = new Intent(Intent.ACTION_CALL);
			callIntent.setData(Uri.parse("tel:" + acknowledgeNumber));
			// Logging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":placeAcknowledgeCall()", "A call intent has been initialized");
			try {
				// Store variable to shared preferences indicating that a call has been placed
				prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.HAS_CALLED_KEY, true, AcknowledgeHandler.this);
			} catch(IllegalArgumentException e) {
				logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":placeAcknowledgeCall()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
			}
			// Set time when the call has been placed
			startCall = Calendar.getInstance().getTime();
			// Kick off call intent
			startActivity(callIntent);
			// Logging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":placeAcknowledgeCall()", "A acknowledge call has been placed to phone number:\"" + acknowledgeNumber + "\" at the time:\"" + startCall.getTime() + "\"");
		} catch (ActivityNotFoundException e) {
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":placeAcknowledgeCall()", "Failed to place acknowledge call", e);
		}
	}

	/**
	 * To find UI widgets and get their reference by ID stored in class
	 * variables.
	 * 
	 * @see #onCreate(Bundle)
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String , String) logCat(LogPriorities, String , String)
	 */
	private void findViews() {
		// Logging
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":findViews()", "Start finding Views by their ID");

		// Declare and initialize variables of type TextView
		this.titleTextView = (TextView) findViewById(R.id.ackTitle_tv);
		this.fullMessageTextView = (TextView) findViewById(R.id.ackFullAlarm_tv);
		this.lineBusyTextView = (TextView) findViewById(R.id.ackLineBusy_tv);
		this.countDownTextView = (TextView) findViewById(R.id.ackCountdown_tv);
		this.secondsTextView = (TextView) findViewById(R.id.ackSeconds_tv);

		// Declare and initialize variables of type Button
		this.acknowledgeButton = (Button) findViewById(R.id.ackAcknowledgeAlarm_btn);
		this.abortButton = (Button) findViewById(R.id.ackAbortAlarm_btn);

		// Declare and initialize variable of type ProgressBar
		this.redialProgressBar = (ProgressBar) findViewById(R.id.ackRedial_pb);

		// Declare and initialize variables of type ImageView
		this.divider1ImageView = (ImageView) findViewById(R.id.ackDivider1_iv);
		this.divider2ImageView = (ImageView) findViewById(R.id.ackDivider2_iv);

		// If Android API level less then 11 set bright gradient else set dark
		// gradient
		if (Build.VERSION.SDK_INT < 11) {
			this.divider1ImageView.setImageResource(R.drawable.gradient_divider_10_and_down);
			this.divider2ImageView.setImageResource(R.drawable.gradient_divider_10_and_down);
			// Logging
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":findViews()", "API level < 11, set bright gradients");
		} else {
			this.divider1ImageView.setImageResource(R.drawable.gradient_divider_11_and_up);
			this.divider2ImageView.setImageResource(R.drawable.gradient_divider_11_and_up);
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":findViews()", "API level > 10, set dark gradients");
		}

		// Log and hide UI widgets that user don't need to see right now
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":findViews()", "Hiding elements not needed to show right now");
		this.lineBusyTextView.setVisibility(View.GONE);
		this.countDownTextView.setVisibility(View.GONE);
		this.secondsTextView.setVisibility(View.GONE);
		this.redialProgressBar.setVisibility(View.GONE);

		// Logging
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":findViews()", "All Views found");
	}

	/**
	 * To get <code>Shared Preferences</code> used by class
	 * <code>AcknowledgeHandler</code>.
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String , String) logCat(LogPriorities, String , String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String, String, Throwable) logCatTxt(LogPriorities, String, String, Throwable)
	 * @see ax.ha.it.smsalarm.PreferencesHandler#getPrefs(PrefKeys, PrefKeys, DataTypes, Context) getPrefs(PrefKeys, PrefKeys, DataTypes, Context)
	 */
	private void getAckHandlerPrefs() {
		// Some logging
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":getAckHandlerPrefs()", "Start retrieving shared preferences needed by class AcknowledgeHandler");

		try {
			// Get shared preferences needed by class Acknowledge Handler
			this.rescueService = (String) this.prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.RESCUE_SERVICE_KEY, DataTypes.STRING, this);
			this.fullMessage = (String) this.prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.FULL_MESSAGE_KEY, DataTypes.STRING, this);
			this.acknowledgeNumber = (String) this.prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.ACK_NUMBER_KEY, DataTypes.STRING, this);
			this.hasCalled = (Boolean) this.prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.HAS_CALLED_KEY, DataTypes.BOOLEAN, this);
		} catch(IllegalArgumentException e) {
			logger.logCatTxt(LogPriorities.ERROR, this.LOG_TAG + ":getAckHandlerPrefs()", "An unsupported datatype was given as argument to PreferencesHandler.getPrefs()", e);
		} 

		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":getAckHandlerPrefs()", "Shared preferences retrieved");
	}

	/**
	 * To set <code>TextViews</code> with data for a proper presentation of the
	 * UI.
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String , String) logCat(LogPriorities, String , String)
	 */
	@SuppressLint("DefaultLocale")
	private void setTextViews() {
		// Some logging
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":setTextViews()", "Setting TextViews with proper data");
		// Set TextViews from variables and resources
		if (!this.rescueService.isEmpty()) {
			this.titleTextView.setText(this.rescueService.toUpperCase() + " " + getResources().getString(R.string.ALARM));
		} else {
			this.titleTextView.setText(getString(R.string.ALARM));
		}
		this.fullMessageTextView.setText(this.fullMessage);
		// Check if the activity already has placed a call, in that case show TextViews for redial
		if (this.hasCalled) {
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":setTextViews()", "We have already placed a call, showing redial widgets");
			this.lineBusyTextView.setVisibility(View.VISIBLE);
			this.countDownTextView.setVisibility(View.VISIBLE);
			this.secondsTextView.setVisibility(View.VISIBLE);
			this.redialProgressBar.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Checks the different phone states while placing a call. With that
	 * information given, we can arrange automatic redial functionality in this
	 * parent class <code>AcknowledgeHandler</code>.
	 * 
	 * @author Robert Nyholm <robert.nyholm@aland.net>
	 * @version 2.1
	 * @since 2.1
	 */
	private class ListenToPhoneState extends PhoneStateListener {

		/**
		 * An inherited method to check when call state has changed. This
		 * implementation of that method helps to make an automatic redial.
		 * 
		 * @see #swapStates(int)
		 * @see #stateName(int)
		 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String , String) logCat(LogPriorities, String , String)
		 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String, Throwable) logCat(LogPriorities, String, String, Throwable)
		 * @see ax.ha.it.smsalarm.PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context) setPrefs(PrefKeys, PrefKeys, Object, Context)
		 */
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			// Logging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":ListenToPhoneState().onCallStateChanged()", "Call state has changed");
			// Swap the phone states
			swapStates(state);
			// Log phone states name(purely in debugging purpose)
			stateName(state);

			// Only do this if phone call go from OFFHOOK to IDLE state
			if (prePhoneState == 2 && phoneState == 0 && NOT_EVALUATED) {
				// Logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":ListenToPhoneState().onCallStateChanged()", "Call state went from \"Off hook\" to \"Idle\"");
				// Set to false because we evaluate this right now
				NOT_EVALUATED = false;
				// Get date for end call
				Date endCall = Calendar.getInstance().getTime();
				// Calculate the call time
				long time = endCall.getTime() - startCall.getTime();
				// Logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":ListenToPhoneState().onCallStateChanged()", "Start time of call is:\"" + startCall.getTime() + "\", end time is:\"" + endCall.getTime() + "\" and the call time was:\"" + time + "\"");

				/*
				 * If call time is less than preconfigured value the line was
				 * busy and call did not go through. In this case we need to
				 * start the AcknowledgeHandler activity once more.
				 */
				if (endCall.getTime() - startCall.getTime() < MIN_CALL_TIME) {
					// Logging
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":ListenToPhoneState().onCallStateChanged()", "Call time was less than:\"" + MIN_CALL_TIME + "\", assumes the line was busy. Initializing and starting a new intent to place a new call");
					Intent i = new Intent(AcknowledgeHandler.this, AcknowledgeHandler.class);
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);
				} else {
					// Logging
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":ListenToPhoneState().onCallStateChanged()", "Call time was more than:\"" + MIN_CALL_TIME + "\", assumes the call went through");
					try {
						// Store variable to shared preferences indicating that a call has been placed with success
						prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.HAS_CALLED_KEY, false, AcknowledgeHandler.this);
					} catch(IllegalArgumentException e) {
						logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":ListenToPhoneState().onCallStateChanged()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
					}					
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":ListenToPhoneState().onCallStateChanged()", "Finishing activity");
					// Finish this activity
					finish();
				}
			}
			super.onCallStateChanged(state, incomingNumber);
		}

		/**
		 * To get phone state as String.
		 * 
		 * @param state
		 *            Phone state as Integer
		 * 
		 * @return Phone state as String resolved from Integer
		 * 
		 * @see #onCallStateChanged(int, String)
		 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String , String) logCat(LogPriorities, String , String)
		 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String , String) logCatTxt(LogPriorities, String , String)
		 */
		String stateName(int state) {
			// Switch through the different phone states
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":ListenToPhoneState().stateName()", "Current call state is:\"Idle\"");
				return "Idle";
			case TelephonyManager.CALL_STATE_OFFHOOK:
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":ListenToPhoneState().stateName()", "Current call state is:\"Off hook\"");
				return "Off hook";
			case TelephonyManager.CALL_STATE_RINGING:
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":ListenToPhoneState().stateName()", "Current call state is:\"Ringing\"");
				return "Ringing";
			default:
				logger.logCatTxt(LogPriorities.WARN, LOG_TAG + ":ListenToPhoneState().stateName()", "Unsupported call state occured, current state is:\"" + Integer.toString(state) + "\"");
				return Integer.toString(state);
			}
		}

		/**
		 * To swap phone states in a way that we have both the previous phone
		 * state and current phone state stored.
		 * 
		 * @param currentState
		 *            Phone state to be stored as the current phone state
		 * 
		 * @see #onCallStateChanged(int, String)
		 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String , String) logCat(LogPriorities, String , String)
		 */
		void swapStates(int currentState) {
			// Logging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":ListenToPhoneState().swapStates()", "Swapping phone states");
			// Swap phone states
			prePhoneState = phoneState;
			phoneState = currentState;
		}
	}
}