/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.activity;

import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.handler.DatabaseHandler;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.DataType;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.PrefKey;
import ax.ha.it.smsalarm.provider.WidgetProvider;

/**
 * Handles and are responsible for the <b><i>Sms Alarm's Acknowledge</i></b> by call functionality.<br>
 * Of course this class also holds the <b><i>user interface</i></b> for this functionality.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 1.1-SE
 * @see ListenToPhoneState
 */
public class Acknowledge extends Activity {
	// To handle the shared preferences
	private final SharedPreferencesHandler prefHandler = SharedPreferencesHandler.getInstance();

	// To listen at the phones different states
	private ListenToPhoneState customPhoneStateListener;

	// Database access
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

	// ...and ProgressBar
	private ProgressBar redialProgressBar;

	// Strings for the data presentation in UI
	private String rescueService = "";
	private String fullMessage = "";

	// String representing phone number to which we acknowledge to
	private String acknowledgeNumber = "";

	// Boolean to indicate if a called has been placed already
	private boolean hasCalled = false;

	// Boolean to keep track if we have evaluated a certain phone state
	private boolean NOT_EVALUATED = true;

	// Date variable to keep track of call times. Note - Must be initialized to avoid NPE,
	// initialized with release date of the first Sms Alarm version (26.11-2011) in milliseconds
	private Date startCall = new Date(1319576400000L);

	// @formatter:off
	// Constants for the re-dial parameters
	private static final int MIN_CALL_TIME = 7000; 				// Minimum call time(in milliseconds), if below this the application redials
	private static final int REDIAL_COUNTDOWN_TIME = 6000; 		// Count down time(in milliseconds) before redial should occur
	private static final int REDIAL_COUNTDOWN_INTERVAL = 100;
	// @formatter:on

	/**
	 * Perform initialization of <code>Layout</code>'s, fetching {@link SharedPreferences}, setting up {@link View}'s, setting different
	 * <code>Listener</code>'s, getting database access and other things that's needed by this {@link Activity} in order for it to work.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ack);

		// Initialize database handler object from context
		db = new DatabaseHandler(this);

		// Declare a telephony manager with proper SystemService and attach listener to it
		TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		customPhoneStateListener = new ListenToPhoneState();
		tManager.listen(customPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

		findViews();
		fetchSharedPrefs();
		setTextViews();

		// Set listener to the abort Button
		abortButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Finish of this activity
				finish();
			}
		});

		// Set listener to the acknowledge Button
		acknowledgeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Check to see if any phone number to acknowledge to exists
				if (!"".equals(acknowledgeNumber)) {
					// Update acknowledge time of latest received primary alarm in database
					db.updateLatestPrimaryAlarmAcknowledged();

					// Update all widgets associated with this application
					WidgetProvider.updateWidgets(Acknowledge.this);

					// Place the acknowledge call
					placeAcknowledgeCall();
				} else { // No phone number to acknowledge to exists, show toast
					Toast.makeText(Acknowledge.this, R.string.ACK_CANNOT, Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	/**
	 * The <b><i>Redial</i></b> handling occurs within this method. If this method is entered with this object in a <b><i>Has Called</i></b> state,
	 * it's needed to place yet another call, because it's sure that this object already has placed a call that didn't get through.
	 */
	@Override
	public void onResume() {
		super.onResume();

		// If, and only if, we already have placed a call
		if (hasCalled) {
			// Initialize progress bar and TextViews needed for count down
			redialProgressBar.setProgress(0);
			countDownTextView.setText(Integer.toString(REDIAL_COUNTDOWN_TIME / 1000));

			new CountDownTimer(REDIAL_COUNTDOWN_TIME, REDIAL_COUNTDOWN_INTERVAL) {
				@Override
				public void onTick(long millisUntilFinished) {
					// Calculate new value of ProgressBar
					float fraction = millisUntilFinished / (float) REDIAL_COUNTDOWN_TIME;
					// Update ProgressBar and TextView with new values
					countDownTextView.setText((millisUntilFinished / 1000) + "");
					redialProgressBar.setProgress((int) (fraction * REDIAL_COUNTDOWN_INTERVAL));
				}

				@Override
				public void onFinish() {
					placeAcknowledgeCall();
				}
			}.start();
		}
	}

	/**
	 * To place an <b><i>Acknowledge Call</i></b> to a preconfigured phone number.
	 */
	private void placeAcknowledgeCall() {
		// Make a call intent
		Intent callIntent = new Intent(Intent.ACTION_CALL);
		callIntent.setData(Uri.parse("tel:" + acknowledgeNumber));

		// Store variable to shared preferences indicating that a call has been placed
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.HAS_CALLED_KEY, true, Acknowledge.this);

		// Set time when the call has been placed
		startCall = Calendar.getInstance().getTime();
		// Kick off call intent
		startActivity(callIntent);
	}

	/**
	 * To find user interface widgets and get their reference by ID.<br>
	 * If some other attributes or special handling has to be done to some user interface widgets it's also done within this method.
	 */
	private void findViews() {
		// Finding EditText views
		titleTextView = (TextView) findViewById(R.id.ackTitle_tv);
		fullMessageTextView = (TextView) findViewById(R.id.ackFullAlarm_tv);
		lineBusyTextView = (TextView) findViewById(R.id.ackLineBusy_tv);
		countDownTextView = (TextView) findViewById(R.id.ackCountdown_tv);
		secondsTextView = (TextView) findViewById(R.id.ackSeconds_tv);

		// Finding Button views
		acknowledgeButton = (Button) findViewById(R.id.ackAcknowledgeAlarm_btn);
		abortButton = (Button) findViewById(R.id.ackAbortAlarm_btn);

		// Finding ProgressBar view
		redialProgressBar = (ProgressBar) findViewById(R.id.ackRedial_pb);

		// Hide UI widgets that user don't need to see right now
		lineBusyTextView.setVisibility(View.GONE);
		countDownTextView.setVisibility(View.GONE);
		secondsTextView.setVisibility(View.GONE);
		redialProgressBar.setVisibility(View.GONE);
	}

	/**
	 * To fetch all {@link SharedPreferences} used by {@link Acknowledge} class.
	 */
	private void fetchSharedPrefs() {
		rescueService = (String) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.RESCUE_SERVICE_KEY, DataType.STRING, this);
		fullMessage = (String) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.FULL_MESSAGE_KEY, DataType.STRING, this);
		acknowledgeNumber = (String) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.ACK_NUMBER_KEY, DataType.STRING, this);
		hasCalled = (Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.HAS_CALLED_KEY, DataType.BOOLEAN, this);
	}

	/**
	 * To set {@link TextView}'s with correct texts for a proper presentation of the user interface.
	 */
	@SuppressLint("DefaultLocale")
	private void setTextViews() {
		// Set TextViews from variables and resources
		if (!"".equals(rescueService)) {
			titleTextView.setText(rescueService.toUpperCase() + " " + getResources().getString(R.string.ALARM));
		} else {
			titleTextView.setText(getString(R.string.ALARM));
		}

		// Show full alarm message
		fullMessageTextView.setText(fullMessage);

		// Check if the activity already has placed a call, in that case show TextViews for redial
		if (hasCalled) {
			lineBusyTextView.setVisibility(View.VISIBLE);
			countDownTextView.setVisibility(View.VISIBLE);
			secondsTextView.setVisibility(View.VISIBLE);
			redialProgressBar.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Checks the different phone states while placing a call. With that information given, it's possible to arrange automatic <b><i>Redial</i></b>
	 * functionality in class {@link Acknowledge}.
	 * 
	 * @author Robert Nyholm <robert.nyholm@aland.net>
	 * @version 2.3.1
	 * @since 2.1
	 */
	private class ListenToPhoneState extends PhoneStateListener {
		// To keep track of the different phone states
		private int prePhoneState = -1;
		private int phoneState = -1;

		/**
		 * To figure out different <b><i>Call State Changes</i></b> in such way that it's possible to figure out if a <b><i>Redial</i></b> is needed.
		 */
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			// Swap the phone states
			swapStates(state);

			// Only do this if phone call go from OFFHOOK to IDLE state
			if (prePhoneState == 2 && phoneState == 0 && NOT_EVALUATED) {
				// Set to false because this state change is being evaluated right now
				NOT_EVALUATED = false;

				// Get date for end call and calculate call time
				Date endCall = Calendar.getInstance().getTime();

				// If call time is less than minimum call time needed for it to count as a proper phone call the line was busy. In this case
				// Acknowledge activity needs to be started once more
				if (endCall.getTime() - startCall.getTime() < MIN_CALL_TIME) {
					Intent i = new Intent(Acknowledge.this, Acknowledge.class);
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);
				} else {
					// Store variable to shared preferences indicating that a call has been placed with success
					prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.HAS_CALLED_KEY, false, Acknowledge.this);

					// Finish of this activity
					finish();
				}
			}

			// Don't forget, call super class implementation
			super.onCallStateChanged(state, incomingNumber);
		}

		/**
		 * To swap phone states in a way that both the previous phone state and current phone state stored.
		 * 
		 * @param currentState
		 *            Phone state to be stored as the current phone state.
		 */
		void swapStates(int currentState) {
			// Swap phone states
			prePhoneState = phoneState;
			phoneState = currentState;
		}
	}
}