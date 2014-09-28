/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.activity;

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
import ax.ha.it.smsalarm.Alarm;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.WidgetProvider;
import ax.ha.it.smsalarm.handler.DatabaseHandler;
import ax.ha.it.smsalarm.handler.LogHandler;
import ax.ha.it.smsalarm.handler.LogHandler.LogPriorities;
import ax.ha.it.smsalarm.handler.PreferencesHandler;
import ax.ha.it.smsalarm.handler.PreferencesHandler.DataType;
import ax.ha.it.smsalarm.handler.PreferencesHandler.PrefKey;

/**
 * Responsible for the application <code>ax.ha.it.smsalarm</code> acknowledge activity. This class allows users to acknowledge an received alarm by
 * calling to specific phone number.<br>
 * Also holds the acknowledge UI.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.2.1
 * @since 1.1-SE
 * @see ListenToPhoneState
 */
public class Acknowledge extends Activity {
	// Log tag string
	private final String LOG_TAG = getClass().getSimpleName();

	// Objects needed for logging and shared preferences handling
	private final LogHandler logger = LogHandler.getInstance();
	private final PreferencesHandler prefHandler = PreferencesHandler.getInstance();

	// Object needed to listen for phones different states
	private ListenToPhoneState customPhoneStateListener;

	// Object to handle database access and methods
	private DatabaseHandler db;

	// The TextViews...
	private TextView titleTextView;
	private TextView fullMessageTextView;
	private TextView lineBusyTextView;
	private TextView countDownTextView;
	private TextView secondsTextView;

	// ...Buttons...
	private Button acknowledgeButton;
	private Button abortButton;

	// ...ProgressBar...
	private ProgressBar redialProgressBar;

	// ...ImageViews...
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

	// Date variable to keep track of call times NB. Must be initialized to avoid NPE,
	// initialized with release date of the first Sms Alarm version (26.11-2011) in milliseconds
	private Date startCall = new Date(1319576400000L);

	// To count down a new call
	@SuppressWarnings("unused")
	private CountDownTimer redialCountDown;

	// Integers to keep track of the different phone states
	private int prePhoneState = -1;
	private int phoneState = -1;

	// Constants for the re-dial parameters
	private final int MIN_CALL_TIME = 7000; // Minimum call time(in milliseconds), if below this the application redials
	private final int REDIAL_COUNTDOWN_TIME = 6000; // Count down time(in milliseconds) before redial should occur
	private final int REDIAL_COUNTDOWN_INTERVAL = 100;

	/**
	 * When activity starts, this method is the entry point. The User Interface is built up and different <code>Listeners</code> are set within this
	 * method.
	 * 
	 * @param savedInstanceState
	 *            Default Bundle
	 * @see #findViews()
	 * @see #getAckHandlerPrefs()
	 * @see #setTextViews()
	 * @see #onResume()
	 * @see #onPause()
	 * @see LogHandler#logCat(LogPriorities, String , String)
	 * @see LogHandler#logAlarm(List, Context) logAlarm(List, Context)
	 * @see DatabaseHandler
	 * @see DatabaseHandler#updateLatestAlarmAcknowledged()
	 * @see DatabaseHandler#getAllAlarm()
	 * @see Alarm
	 * @see WidgetProvider#updateWidgets(Context)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ack);

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate()", "Creation of the Acknowledge Handler started");

		// Initialize database handler object from context
		db = new DatabaseHandler(this);

		// Declare a telephonymanager with propersystemservice and attach listener to it
		TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		customPhoneStateListener = new ListenToPhoneState();
		tManager.listen(customPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate()", "Got TELEPHONY_SERVICE and attached phone state listener to it");

		// FindViews
		findViews();
		// Get Shared Preferences
		getAckHandlerPrefs();
		// Set TextViews
		setTextViews();

		// Create objects that acts as listeners to the buttons
		abortButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().abortButton.OnClickListener().onClick()", "Abort button has been pressed, finishing activity");
				finish();
			}
		});

		// Create objects that acts as listeners to the buttons
		acknowledgeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Check to see if we have any phone number to acknowledge to
				if (!acknowledgeNumber.equals("")) {
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().acknowledgeButton.OnClickListener().onClick()", "Acknowledge button has been pressed and phone number to acknowledge to exist. Continue acknowledge");

					// Update acknowledge time of latest received primary alarm in database
					db.updateLatestPrimaryAlarmAcknowledged();

					// Get all alarms from database and log them to to html file
					logger.logAlarm(db.getAllAlarm(), Acknowledge.this);

					// Update all widgets associated with this application
					WidgetProvider.updateWidgets(Acknowledge.this);

					// Place the acknowledge call
					placeAcknowledgeCall();
				} else {
					Toast.makeText(Acknowledge.this, R.string.ACK_CANNOT, Toast.LENGTH_LONG).show();
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().acknowledgeButton.OnClickListener().onClick()", "Acknowledge button has been pressed but no phone number to acknowledge to has been given");
				}
			}
		});

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate()", "Creation of the Acknowledge Handler completed");
	}

	/**
	 * To trigger event when activity resumes.
	 * 
	 * @see #onCreate(Bundle)
	 * @see #onPause()
	 * @see LogHandler#logCat(LogPriorities, String , String)
	 */
	@Override
	public void onResume() {
		super.onResume();
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onResume()", "Activity resumed");

		// If we already have placed a call
		if (hasCalled) {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onResume()", "An acknowledge call has already been placed, building up progressbar and countdown for a new acknowledge call");

			// Initialize progress bar and textviews needed for countdown
			redialProgressBar.setProgress(0);
			countDownTextView.setText(Integer.toString(REDIAL_COUNTDOWN_TIME / 1000));

			redialCountDown = new CountDownTimer(REDIAL_COUNTDOWN_TIME, REDIAL_COUNTDOWN_INTERVAL) {
				@Override
				public void onTick(long millisUntilFinished) {
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onResume().CountDownTimer().onTick()", "Calculate redial countdown times and update UI widgets");

					// Calculate new value of ProgressBar
					float fraction = millisUntilFinished / (float) REDIAL_COUNTDOWN_TIME;
					// Update ProgressBar and TextView with new values
					countDownTextView.setText((millisUntilFinished / 1000) + "");
					redialProgressBar.setProgress((int) (fraction * REDIAL_COUNTDOWN_INTERVAL));
				}

				@Override
				public void onFinish() {
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onResume().CountDownTimer().onFinish()", "Redial countdown finished, continue to place new acknowledge call");

					// Place the acknowledge call
					placeAcknowledgeCall();
				}
			}.start();
		}
	}

	/**
	 * To handle events to trigger when activity pauses. <b><i>Not yet implemented.</i></b>
	 * 
	 * @see #onCreate(Bundle)
	 * @see #onResume()
	 */
	@Override
	public void onPause() {
		super.onPause();
	}

	/**
	 * To place a acknowledge call to a pre0configured phone number.
	 * 
	 * @see #onCreate(Bundle)
	 * @see #onResume()
	 * @see LogHandler#logCat(LogPriorities, String , String)
	 * @see LogHandler#logCatTxt(LogPriorities, String , String, Throwable)
	 * @see PreferencesHandler#storePrefs(PrefKeys, PrefKeys, Object, Context)
	 */
	private void placeAcknowledgeCall() {
		try {
			// Make a call intent
			Intent callIntent = new Intent(Intent.ACTION_CALL);
			callIntent.setData(Uri.parse("tel:" + acknowledgeNumber));

			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":placeAcknowledgeCall()", "A call intent has been initialized");

			// Store variable to shared preferences indicating that a call has been placed
			prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.HAS_CALLED_KEY, true, Acknowledge.this);

			// Set time when the call has been placed
			startCall = Calendar.getInstance().getTime();
			// Kick off call intent
			startActivity(callIntent);

			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":placeAcknowledgeCall()", "A acknowledge call has been placed to phone number:\"" + acknowledgeNumber + "\" at the time:\"" + startCall.getTime() + "\"");
		} catch (ActivityNotFoundException e) {
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":placeAcknowledgeCall()", "Failed to place acknowledge call", e);
		}
	}

	/**
	 * To find UI widgets and get their reference by ID stored in class variables.
	 * 
	 * @see #onCreate(Bundle)
	 * @see LogHandler#logCat(LogPriorities, String , String)
	 */
	private void findViews() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "Start finding views by their ID");

		// Finding EditText views
		titleTextView = (TextView) findViewById(R.id.ackTitle_tv);
		fullMessageTextView = (TextView) findViewById(R.id.ackFullAlarm_tv);
		lineBusyTextView = (TextView) findViewById(R.id.ackLineBusy_tv);
		countDownTextView = (TextView) findViewById(R.id.ackCountdown_tv);
		secondsTextView = (TextView) findViewById(R.id.ackSeconds_tv);

		// Finding Button views
		acknowledgeButton = (Button) findViewById(R.id.ackAcknowledgeAlarm_btn);
		abortButton = (Button) findViewById(R.id.ackAbortAlarm_btn);

		// Finding Progressbar views
		redialProgressBar = (ProgressBar) findViewById(R.id.ackRedial_pb);

		// Finding ImageView views
		divider1ImageView = (ImageView) findViewById(R.id.ackDivider1_iv);
		divider2ImageView = (ImageView) findViewById(R.id.ackDivider2_iv);

		// If Android API level less then 11 set bright gradient else set dark gradient
		if (Build.VERSION.SDK_INT < 11) {
			divider1ImageView.setImageResource(R.drawable.gradient_divider_10_and_down);
			divider2ImageView.setImageResource(R.drawable.gradient_divider_10_and_down);
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "API level < 11, set bright gradients");
		} else {
			divider1ImageView.setImageResource(R.drawable.gradient_divider_11_and_up);
			divider2ImageView.setImageResource(R.drawable.gradient_divider_11_and_up);
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "API level > 10, set dark gradients");
		}

		// Log and hide UI widgets that user don't need to see right now
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "Hiding elements not needed to show right now");
		lineBusyTextView.setVisibility(View.GONE);
		countDownTextView.setVisibility(View.GONE);
		secondsTextView.setVisibility(View.GONE);
		redialProgressBar.setVisibility(View.GONE);

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "All views found");
	}

	/**
	 * To get <code>Shared Preferences</code> used by class <code>AcknowledgeHandler</code>.
	 * 
	 * @see LogHandler#logCat(LogPriorities, String , String) logCat(LogPriorities, String , String)
	 * @see LogHandler#logCatTxt(LogPriorities, String, String, Throwable)
	 * @see PreferencesHandler#fetchPrefs(PrefKeys, PrefKeys, DataTypes, Context)
	 */
	private void getAckHandlerPrefs() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getAckHandlerPrefs()", "Start retrieving shared preferences needed by class AcknowledgeHandler");

		// Get shared preferences needed by class Acknowledge Handler
		rescueService = (String) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.RESCUE_SERVICE_KEY, DataType.STRING, this);
		fullMessage = (String) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.FULL_MESSAGE_KEY, DataType.STRING, this);
		acknowledgeNumber = (String) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.ACK_NUMBER_KEY, DataType.STRING, this);
		hasCalled = (Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.HAS_CALLED_KEY, DataType.BOOLEAN, this);

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getAckHandlerPrefs()", "Shared preferences retrieved");
	}

	/**
	 * To set <code>TextViews</code> with data for a proper presentation of the UI.
	 * 
	 * @see LogHandler#logCat(LogPriorities, String , String)
	 */
	@SuppressLint("DefaultLocale")
	private void setTextViews() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setTextViews()", "Setting textviews with proper data");

		// Set TextViews from variables and resources
		if (!"".equals(rescueService)) {
			titleTextView.setText(rescueService.toUpperCase() + " " + getResources().getString(R.string.ALARM));
		} else {
			titleTextView.setText(getString(R.string.ALARM));
		}

		fullMessageTextView.setText(fullMessage);

		// Check if the activity already has placed a call, in that case show TextViews for redial
		if (hasCalled) {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setTextViews()", "We have already placed a call, showing redial widgets");

			lineBusyTextView.setVisibility(View.VISIBLE);
			countDownTextView.setVisibility(View.VISIBLE);
			secondsTextView.setVisibility(View.VISIBLE);
			redialProgressBar.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Checks the different phone states while placing a call. With that information given, we can arrange automatic redial functionality in this
	 * parent class <code>AcknowledgeHandler</code>.
	 * 
	 * @author Robert Nyholm <robert.nyholm@aland.net>
	 * @version 2.2
	 * @since 2.1
	 */
	private class ListenToPhoneState extends PhoneStateListener {

		/**
		 * An inherited method to check when call state has changed. This implementation of that method helps to make an automatic redial.
		 * 
		 * @see #swapStates(int)
		 * @see #stateName(int)
		 * @see LogHandler#logCat(LogPriorities, String , String)
		 * @see LogHandler#logCat(LogPriorities, String, String, Throwable)
		 * @see PreferencesHandler#storePrefs(PrefKeys, PrefKeys, Object, Context)
		 */
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":ListenToPhoneState().onCallStateChanged()", "Call state has changed");

			// Swap the phone states
			swapStates(state);
			// Log phone states name(purely in debugging purpose)
			stateName(state);

			// Only do this if phone call go from OFFHOOK to IDLE state
			if (prePhoneState == 2 && phoneState == 0 && NOT_EVALUATED) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":ListenToPhoneState().onCallStateChanged()", "Call state went from \"Off hook\" to \"Idle\"");
				// Set to false because we evaluate this right now
				NOT_EVALUATED = false;
				// Get date for end call
				Date endCall = Calendar.getInstance().getTime();
				// Calculate the call time
				long time = endCall.getTime() - startCall.getTime();

				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":ListenToPhoneState().onCallStateChanged()", "Start time of call is:\"" + startCall.getTime() + "\", end time is:\"" + endCall.getTime() + "\" and the call time was:\"" + time + "\"");

				// If call time is less than preconfigured value the line was busy and call did not go through. In this case we need to start the
				// AcknowledgeHandler activity once more.
				if (endCall.getTime() - startCall.getTime() < MIN_CALL_TIME) {
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":ListenToPhoneState().onCallStateChanged()", "Call time was less than:\"" + MIN_CALL_TIME + "\", assumes the line was busy. Initializing and starting a new intent to place a new call");
					Intent i = new Intent(Acknowledge.this, Acknowledge.class);
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);
				} else {
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":ListenToPhoneState().onCallStateChanged()", "Call time was more than:\"" + MIN_CALL_TIME + "\", assumes the call went through");

					// Store variable to shared preferences indicating that a call has been placed with success
					prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.HAS_CALLED_KEY, false, Acknowledge.this);
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
		 * @return Phone state as String resolved from Integer
		 * @see #onCallStateChanged(int, String)
		 * @see LogHandler#logCat(LogPriorities, String , String)
		 * @see LogHandler#logCatTxt(LogPriorities, String , String)
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
		 * To swap phone states in a way that we have both the previous phone state and current phone state stored.
		 * 
		 * @param currentState
		 *            Phone state to be stored as the current phone state
		 * @see #onCallStateChanged(int, String)
		 * @see LogHandler#logCat(LogPriorities, String , String)
		 */
		void swapStates(int currentState) {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":ListenToPhoneState().swapStates()", "Swapping phone states");

			// Swap phone states
			prePhoneState = phoneState;
			phoneState = currentState;
		}
	}
}