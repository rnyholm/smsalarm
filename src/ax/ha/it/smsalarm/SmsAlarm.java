/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import ax.ha.it.smsalarm.LogHandler.LogPriorities;
import ax.ha.it.smsalarm.PreferencesHandler.DataTypes;
import ax.ha.it.smsalarm.PreferencesHandler.PrefKeys;

/**
 * Main activity to configure application. Also holds the main User Interface.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.2
 * @since 0.9beta
 * 
 * @see #onCreate(Bundle)
 * @see #onPause()
 * @see #onDestroy()
 */
public class SmsAlarm extends Activity {
	/**
	 * Enumeration for different types of input dialogs.
	 * 
	 * @author Robert Nyholm <robert.nyholm@aland.net>
	 * @version 2.2
	 * @since 2.1
	 */
	private enum DialogTypes {
		SMS_PRIMARY, SMS_SECONDARY, FREE_TEXT_PRIMARY, FREE_TEXT_SECONDARY, ACKNOWLEDGE, RESCUESERVICE;
	}

	// Log tag string
	private final String LOG_TAG = getClass().getSimpleName();

	// Objects needed for logging, shared preferences and noise handling
	private LogHandler logger = LogHandler.getInstance();
	private static final PreferencesHandler prefHandler = PreferencesHandler.getInstance();
	private NoiseHandler noiseHandler = NoiseHandler.getInstance();

	// Object to handle database access and methods
	private DatabaseHandler db;

	// Variables of different UI elements and types
	// The EdittextObjects
	private EditText selectedToneEditText;
	private EditText ackNumberEditText;
	private EditText rescueServiceEditText;

	// The Button objects
	private Button addPrimarySmsNumberButton;
	private Button removePrimarySmsNumberButton;
	private Button addSecondarySmsNumberButton;
	private Button removeSecondarySmsNumberButton;
	private Button addPrimaryFreeTextButton;
	private Button removePrimaryFreeTextButton;
	private Button addSecondaryFreeTextButton;
	private Button removeSecondaryFreeTextButton;
	private Button editMsgToneButton;
	private Button listenMsgToneButton;
	private Button ackNumberButton;
	private Button editRescueServiceButton;

	// The CheckBox objects
	private CheckBox soundSettingCheckBox;
	private CheckBox enableAckCheckBox;
	private CheckBox playToneTwiceSettingCheckBox;
	private CheckBox enableSmsAlarmCheckBox;

	// The ImageView objects
	private ImageView divider1ImageView;
	private ImageView divider2ImageView;
	private ImageView divider3ImageView;
	private ImageView divider4ImageView;

	// The Spinner objects
	private Spinner toneSpinner;
	private Spinner primarySmsNumberSpinner;
	private Spinner secondarySmsNumberSpinner;
	private Spinner primaryFreeTextSpinner;
	private Spinner secondaryFreeTextSpinner;

	// The textView objects
	private TextView soundSettingInfoTextView;
	private TextView playToneTwiceInfoTextView;
	private TextView enableSmsAlarmInfoTextView;
	private TextView enableAckInfoTextView;

	// Strings to store different important numbers
	private String acknowledgeNumber = "";

	// List of strings containing primary- and secondary sms numbers, also primary- and secondary free texts 
	private List<String> primarySmsNumbers = new ArrayList<String>();
	private List<String> secondarySmsNumbers = new ArrayList<String>();
	private List<String> primaryFreeTexts = new ArrayList<String>();
	private List<String> secondaryFreeTexts = new ArrayList<String>();
	private List<String> emptySmsNumbers = new ArrayList<String>(); // <-- A "dummy" list just containing one element, one string
	private List<String> emptyFreeTexts = new ArrayList<String>(); // <-- A "dummy" list just containing one element, on string

	// String to store firedepartments name in
	private String rescueService = "";

	// Integer to store which tone id to be used
	private int primaryMessageToneId = 0;
	private int secondaryMessageToneId = 1;

	// Boolean variables to store whether to use OS soundsettings or not, and if acknowledge is enabled
	private boolean useOsSoundSettings = false;
	private boolean useAlarmAcknowledge = false;
	private boolean playToneTwice = false;
	private boolean enableSmsAlarm = true;

	// Integer holding spinners positions
	private int toneSpinnerPos = 0;

	/**
	 * When activity starts, this method is the entry point. The User Interface
	 * is built up and different <code>Listeners</code> are set within this
	 * method.
	 * 
	 * @param savedInstanceState
	 *            Default Bundle
	 * 
	 * @see #findViews()
	 * @see #updateSelectedToneEditText()
	 * @see #updateAcknowledgeWidgets()
	 * @see #updateWholeUI()
	 * @see #buildAndShowInputDialog(DialogTypes)
	 * @see #buildAndShowDeleteDialog(DialogTypes)
	 * @see #getSmsAlarmPrefs()
	 * @see #buildAndShowDeleteDialog()
	 * @see #buildAndShowToneDialog()
	 * @see #onPause()
	 * @see #onDestroy()
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 *      logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String,
	 *      String) logCatTxt(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String,
	 *      String, Throwable) logCatTxt(LogPriorities, String, String,
	 *      Throwable)
	 * @see ax.ha.it.smsalarm.NoiseHandler#makeNoise(Context, int, boolean,
	 *      boolean) makeNoise(Context, int, boolean, boolean)
	 * @see ax.ha.it.smsalarm.PreferencesHandler#setPrefs(PrefKeys, PrefKeys,
	 *      Object, Context) setPrefs(PrefKeys, PrefKeys, Object, Context)
	 * @see ax.ha.it.smsalarm.DatabaseHandler ax.ha.it.smsalarm.DatabaseHandler
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Log in debugging and information purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate()", "Creation of Sms Alarm started");

		// Get sharedPreferences
		getSmsAlarmPrefs();

		// FindViews
		findViews();

		// Initialize database handler object from context
		db = new DatabaseHandler(this);

		// Fill tone spinner with values
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.alarms, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// Set adapter to tone spinner
		toneSpinner.setAdapter(adapter);
		
		// Set listener to addPrimarySmsNumberButton
		addPrimarySmsNumberButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().addPrimarySmsNumberButton.OnClickListener().onClick()", "Add PRIMARY sms number button pressed");
				// Build up and show input dialog of type primary number
				buildAndShowInputDialog(DialogTypes.SMS_PRIMARY);
			}
		});
		
		// Set listener to removePrimarySmsNumberButton
		removePrimarySmsNumberButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().removePrimarySmsNumberButton.OnClickListener().onClick()", "Remove PRIMARY sms number button pressed");

				// Only show delete dialog if primary sms numbers exists, else show toast
				if (!primarySmsNumbers.isEmpty()) {
					// Show alert dialog(prompt user for deleting number)
					buildAndShowDeleteDialog(DialogTypes.SMS_PRIMARY);
				} else {
					Toast.makeText(SmsAlarm.this, R.string.NO_PRIMARY_NUMBER_EXISTS, Toast.LENGTH_LONG).show();
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().removePrimarySmsNumberButton.OnClickListener().onClick()", "Cannot build and show dialog because the list of PRIMARY sms numbers are empty");
				}
			}
		});

		// Set listener to addSecondarySmsNumberButton
		addSecondarySmsNumberButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().addSecondarySmsNumberButton.OnClickListener().onClick()", "Add SECONDARY sms number button pressed");
				// Build up and show input dialog of type secondary number
				buildAndShowInputDialog(DialogTypes.SMS_SECONDARY);
			}
		});

		// Set listener to removeSecondarySmsNumberButton
		removeSecondarySmsNumberButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().removeSecondarySmsNumberButton.OnClickListener().onClick()", "Remove SECONDARY sms number button pressed");

				// Only show delete dialog if secondary sms numbers exists, else show toast
				if (!secondarySmsNumbers.isEmpty()) {
					// Show alert dialog(prompt user for deleting number)
					buildAndShowDeleteDialog(DialogTypes.SMS_SECONDARY);
				} else {
					Toast.makeText(SmsAlarm.this, R.string.NO_SECONDARY_NUMBER_EXISTS, Toast.LENGTH_LONG).show();
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().removeSecondarySmsNumberButton.OnClickListener().onClick()", "Cannot build and show dialog because the list of SECONDARY sms numbers are");
				}
			}
		});
		
		// Set listener to addPrimaryFreeTextButton
		addPrimaryFreeTextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().addPrimaryFreeTextButton.OnClickListener().onClick()", "Add PRIMARY free text button pressed");
				// Build up and show input dialog of type free text primary
				buildAndShowInputDialog(DialogTypes.FREE_TEXT_PRIMARY);
			}
		});
		
		// Set listener to removePrimaryFreeTextButton
		removePrimaryFreeTextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().removePrimaryFreeTextButton.OnClickListener().onClick()", "Remove PRIMARY free text button pressed");
				
				// Only show delete dialog if primary free texts exists
				if (!primaryFreeTexts.isEmpty()) {
					// Show alert dialog(prompt user for deleting text)
					buildAndShowDeleteDialog(DialogTypes.FREE_TEXT_PRIMARY);
				} else {
					Toast.makeText(SmsAlarm.this, R.string.NO_PRIMARY_FREE_TEXT_EXISTS, Toast.LENGTH_LONG).show();
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().removePrimaryFreeTextButton.OnClickListener().onClick()", "Cannot build and show dialog because the list of PRIMARY free texts are empty");
				}
			}
		});
		
		// Set listener to addSecondaryFreeTextButton
		addSecondaryFreeTextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().addSecondaryFreeTextButton.OnClickListener().onClick()", "Add SECONDARY free text button pressed");
				// Build up and show input dialog of type secondary number
				buildAndShowInputDialog(DialogTypes.FREE_TEXT_SECONDARY);
			}
		});
		
		// Set listener to removeSecondaryFreeTextButton
		removeSecondaryFreeTextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().removeSecondaryFreeTextButton.OnClickListener().onClick()", "Remove SECONDARY free text button pressed");
				// Only show delete dialog if secondary free texts exists
				if (!secondaryFreeTexts.isEmpty()) {
					// Show alert dialog(prompt user for deleting text)
					buildAndShowDeleteDialog(DialogTypes.FREE_TEXT_SECONDARY);
				} else {
					Toast.makeText(SmsAlarm.this, R.string.NO_SECONDARY_FREE_TEXT_EXISTS, Toast.LENGTH_LONG).show();
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().removePrimaryFreeTextButton.OnClickListener().onClick()", "Cannot build and show dialog because the list of SECONDARY free texts are");
				}
			}
		});

		// Set listener to ackNumberButton
		ackNumberButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().ackNumberButton.OnClickListener().onClick()", "Edit acknowledge number button pressed");
				// Build up and show input dialog of type acknowledge number
				buildAndShowInputDialog(DialogTypes.ACKNOWLEDGE);
			}
		});

		// Set listener to editRescueServiceButton
		editRescueServiceButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().editRescueServiceButton.OnClickListener().onClick()", "Edit rescue service button pressed");
				// Build up and show input dialog of type primary number
				buildAndShowInputDialog(DialogTypes.RESCUESERVICE);
			}
		});

		// Set listener to editMsgToneButton
		editMsgToneButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().editMsgToneButton.OnClickListener().onClick()", "Edit message tone button pressed");
				// Build up and Show alert dialog(prompt for message tone)
				buildAndShowToneDialog();
			}
		});

		// Set listener to listenMsgToneButton
		listenMsgToneButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Play the correct tone and vibrate, depending on spinner value
				if (toneSpinnerPos == 0) {
					// Logging
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().listenMsgToneButton.OnClickListener().onClick()", "Listen message tone button pressed. Message tone for PRIMARY alarm will be played");
					// Play message tone and vibrate
					noiseHandler.makeNoise(SmsAlarm.this, primaryMessageToneId, useOsSoundSettings, false);
				} else if (toneSpinnerPos == 1) {
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().listenMsgToneButton.OnClickListener().onClick()", "Listen message tone button pressed. Message tone for SECONDARY alarm will be played");
					noiseHandler.makeNoise(SmsAlarm.this, secondaryMessageToneId, useOsSoundSettings, false);
				} else {
					// DO NOTHING EXCEPT LOG ERROR MESSAGE
					logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":onCreate().listenMsgToneButton.OnClickListener().onClick()", "Invalid spinner position occurred. Current tone spinner position is: \"" + Integer.toString(toneSpinnerPos) + "\"");
				}
			}
		});

		// Set listener to soundSettingCheckBox
		soundSettingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// Log that CheckBox been pressed
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().soundSettingCheckBox.onCheckedChange()", "Use OS sound settings checkbox pressed(or checkbox initialized)");

				// Set checkbox depending on it's checked status and store variable
				if (soundSettingCheckBox.isChecked()) {
					// Store value to variable
					useOsSoundSettings = true;
					// logging
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().soundSettingCheckBox.onCheckedChange()", "Use OS sound settings checkbox \"Checked\"(" + useOsSoundSettings + ")");
				} else {
					useOsSoundSettings = false;
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().soundSettingCheckBox.onCheckedChange()", "Use OS sound settings checkbox \"Unchecked\"(" + useOsSoundSettings + ")");
				}

				try {
					// Store value to shared preferences
					prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.USE_OS_SOUND_SETTINGS_KEY, useOsSoundSettings, SmsAlarm.this);
				} catch (IllegalArgumentException e) {
					logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":onCreate().soundSettingCheckBox.onCheckedChange()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
				}
			}
		});

		// Set listener to enableAckCheckBox
		enableAckCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// Log that CheckBox been pressed
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().enableAckCheckBox.onCheckedChange()", "Enable acknowledge checkbox pressed(or checkbox initialized)");

				// Set checkbox depending on it's checked status and store variable
				if (enableAckCheckBox.isChecked()) {
					// Store value to variable
					useAlarmAcknowledge = true;
					// logging
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().enableAckCheckBox.onCheckedChange()", "Use OS sound settings checkbox \"Checked\"(" + useAlarmAcknowledge + ")");
				} else {
					useAlarmAcknowledge = false;
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().enableAckCheckBox.onCheckedChange()", "Use OS sound settings checkbox \"Unchecked\"(" + useAlarmAcknowledge + ")");
				}

				try {
					// Store value to shared preferences
					prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.ENABLE_ACK_KEY, useAlarmAcknowledge, SmsAlarm.this);
				} catch (IllegalArgumentException e) {
					logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":onCreate().enableAckCheckBox.onCheckedChange()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
				}
				
				// Update UI widgets affected by enable acknowledge
				updateAcknowledgeWidgets();
			}
		});

		// Set listener to playToneTwiceSettingCheckBox
		playToneTwiceSettingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// Log that CheckBox been pressed
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().playToneTwiceSettingCheckBox.onCheckedChange()", "Play tone twice checkbox pressed(or checkbox initialized)");

				// Set checkbox depending on it's checked status and store variable
				if (playToneTwiceSettingCheckBox.isChecked()) {
					// Store value to variable
					playToneTwice = true;
					// Logging
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().playToneTwiceSettingCheckBox.onCheckedChange()", "Play tone twice checkbox \"Checked\"(" + playToneTwice + ")");
				} else {
					playToneTwice = false;
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().playToneTwiceSettingCheckBox.onCheckedChange()", "Play tone twice checkbox \"Unhecked\"(" + playToneTwice + ")");
				}

				try {
					// Store value to shared preferences
					prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.PLAY_TONE_TWICE_KEY, playToneTwice, SmsAlarm.this);
				} catch (IllegalArgumentException e) {
					logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":onCreate().playToneTwiceSettingCheckBox.onCheckedChange()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
				}
			}
		});

		// Set listener to enableSmsAlarmCheckBox
		enableSmsAlarmCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// Log that CheckBox been pressed
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().enableSmsAlarmCheckBox.onCheckedChange()", "Enable Sms Alarm checkbox pressed(or checkbox initialized)");

				// Set checkbox depending on it's checked status and store variable
				if (enableSmsAlarmCheckBox.isChecked()) {
					// Store value to variable
					enableSmsAlarm = true;
					// Logging
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().enableSmsAlarmCheckBox.onCheckedChange()", "Enable SmsAlarm checkbox \"Checked\"(" + enableSmsAlarm + ")");
				} else {
					enableSmsAlarm = false;
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().enableSmsAlarmCheckBox.onCheckedChange()", "Enable SmsAlarm checkbox \"Unchecked\"(" + enableSmsAlarm + ")");
				}

				try {
					// Store value to shared preferences
					prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.ENABLE_SMS_ALARM_KEY, enableSmsAlarm, SmsAlarm.this);
				} catch (IllegalArgumentException e) {
					logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":onCreate().enableSmsAlarmCheckBox.onCheckedChange()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
				}
			}
		});

		// Set listener to tone spinner
		toneSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				// Store tone spinners position
				toneSpinnerPos = toneSpinner.getSelectedItemPosition();
				// Logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().toneSpinner.OnItemSelectedListener().onItemSelected()", "Item in tone spinner pressed(or spinner initialized)");
				// Update selected tone EditText widget
				updateSelectedToneEditText();
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// DO NOTHING!
			}
		});

		// Update all UI widgets
		updateWholeUI();

		// Log in debugging and information purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate()", "Creation of Sms Alarm completed");
	}

	/**
	 * To handle events to trigger when activity pauses. <b><i>Not yet
	 * implemented.</i></b>
	 * 
	 * @see #onCreate(Bundle)
	 * @see #onDestroy()
	 */
	@Override
	public void onPause() {
		super.onPause();
		// DO NOTHING!
	}

	/**
	 * To handle events to trigger when activity destroys. Writes all alarms in
	 * database into a <code>.html</code> log file.
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logAlarm(List, Context) logAlarm(List,
	 *      Context)
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 *      logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.DatabaseHandler#getAllAlarm() getAllAlarm()
	 * @see ax.ha.it.smsalarm.Alarm ax.ha.it.smsalarm.Alarm
	 * @see ax.ha.it.smsalarm.WidgetProvider#updateWidgets(Context)
	 * @see #onCreate(Bundle)
	 * @see #onPause()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onDestroy()", LOG_TAG + " is about to be destroyed");
		// Get all alarms from database and log them to to html file
		logger.logAlarm(db.getAllAlarm(), this);
		// Update all widgets associated to this application
		WidgetProvider.updateWidgets(this);
	}

	/**
	 * To build up the menu, called one time only and that's the first time the
	 * menu is inflated.
	 * 
	 * @see #onOptionsItemSelected(MenuItem)
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 *      logCat(LogPriorities, String, String)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreateOptionsMenu()", "Menu created");
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	/**
	 * Method to inflate menu with it's items.
	 * 
	 * @see #buildAndShowAboutDialog()
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 *      logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String,
	 *      String, Throwable) logCatTxt(LogPriorities, String, String,
	 *      Throwable)
	 * @see ax.ha.it.smsalarm.PreferencesHandler#setPrefs(PrefKeys, PrefKeys,
	 *      Object, Context) setPrefs(PrefKeys, PrefKeys, Object, Context)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.ABOUT:
			// Logging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onOptionsItemSelected()", "Menu item ABOUT selected");
			// Build up and show the about dialog
			buildAndShowAboutDialog();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * To find UI widgets and get their reference by ID stored in class
	 * variables.
	 * 
	 * @see #onCreate(Bundle)
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 *      logCat(LogPriorities, String, String)
	 */
	private void findViews() {
		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "Start finding Views by their ID");

		// Declare and initialize variables of type EditText
		selectedToneEditText = (EditText) findViewById(R.id.msgTone_et);
		ackNumberEditText = (EditText) findViewById(R.id.ackNumber_et);
		rescueServiceEditText = (EditText) findViewById(R.id.rescueServiceName_et);

		// Declare and initialize variables of type button
		addPrimarySmsNumberButton = (Button) findViewById(R.id.addPrimarySmsNumber_btn);
		removePrimarySmsNumberButton = (Button) findViewById(R.id.deletePrimarySmsNumber_btn);
		addSecondarySmsNumberButton = (Button) findViewById(R.id.addSecondarySmsNumber_btn);
		addPrimaryFreeTextButton = (Button) findViewById(R.id.addPrimaryFreeText_btn);
		removePrimaryFreeTextButton = (Button) findViewById(R.id.deletePrimaryFreeText_btn);
		addSecondaryFreeTextButton = (Button) findViewById(R.id.addSecondaryFreeText_btn);
		removeSecondaryFreeTextButton = (Button) findViewById(R.id.deleteSecondaryFreeText_btn);
		removeSecondarySmsNumberButton = (Button) findViewById(R.id.deleteSecondarySmsNumber_btn);
		editMsgToneButton = (Button) findViewById(R.id.editMsgTone_btn);
		listenMsgToneButton = (Button) findViewById(R.id.listenMsgTone_btn);
		ackNumberButton = (Button) findViewById(R.id.editAckNumber_btn);
		editRescueServiceButton = (Button) findViewById(R.id.editRescueServiceName_btn);

		// Declare and initialize variables of type CheckBox
		soundSettingCheckBox = (CheckBox) findViewById(R.id.useSysSoundSettings_chk);
		enableAckCheckBox = (CheckBox) findViewById(R.id.enableAcknowledge_chk);
		playToneTwiceSettingCheckBox = (CheckBox) findViewById(R.id.playToneTwiceSetting_chk);
		enableSmsAlarmCheckBox = (CheckBox) findViewById(R.id.enableSmsAlarm_chk);

		// Declare and initialize variables of type Spinner
		toneSpinner = (Spinner) findViewById(R.id.toneSpinner_sp);
		primarySmsNumberSpinner = (Spinner) findViewById(R.id.primarySmsNumberSpinner_sp);
		secondarySmsNumberSpinner = (Spinner) findViewById(R.id.secondarySmsNumberSpinner_sp);
		primaryFreeTextSpinner = (Spinner) findViewById(R.id.primaryFreeTextSpinner_sp);
		secondaryFreeTextSpinner = (Spinner) findViewById(R.id.secondaryFreeTextSpinner_sp);

		// Declare and initialize variables of type TextView
		soundSettingInfoTextView = (TextView) findViewById(R.id.useSysSoundSettingsHint_tv);
		playToneTwiceInfoTextView = (TextView) findViewById(R.id.playToneTwiceSettingHint_tv);
		enableSmsAlarmInfoTextView = (TextView) findViewById(R.id.enableSmsAlarmHint_tv);
		enableAckInfoTextView = (TextView) findViewById(R.id.enableAcknowledgeHint_tv);

		// If Android API level is greater than 16 we need to adjust some margins
		if (Build.VERSION.SDK_INT > 16) {
			// We need to get some Android resources in order to calculate proper pixel dimensions from dp
			Resources resources = getResources();
			// Calculate pixel dimensions for the different margins
			int pixelsLeft = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, resources.getDisplayMetrics()); // 32dp calculated to pixels
			int pixelsRight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, resources.getDisplayMetrics()); // 5dp calculated to pixels
			int pixelsTop = 0;
			// If the locale on device is german(de) set pixelstop to -6dp else -9dp
			if ("de".equals(Locale.getDefault().getLanguage())) {
				// Logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "The device has german(de) locale, set different margin-top on information TextViews for the checkboxes than other locales");
				pixelsTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -6, resources.getDisplayMetrics()); // -6dp calculated to pixels
			} else {
				pixelsTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -9, resources.getDisplayMetrics()); // -9dp calculated to pixels
			}

			// Set layout parameters for the sound settings info textview
			RelativeLayout.LayoutParams paramsSoundSettingInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); // Wrap content, both on height and width
			paramsSoundSettingInfoTextView.setMargins(pixelsLeft, pixelsTop, pixelsRight, 0); // Margins left, top, right, bottom
			paramsSoundSettingInfoTextView.addRule(RelativeLayout.BELOW, soundSettingCheckBox.getId()); // Add rule, below UI widget
			paramsSoundSettingInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, soundSettingCheckBox.getId()); // Add rule, align left of UI widget

			// Set layout parameters for the play tone twice textview
			RelativeLayout.LayoutParams paramsPlayToneTwiceInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			paramsPlayToneTwiceInfoTextView.setMargins(pixelsLeft, pixelsTop, pixelsRight, 0);
			paramsPlayToneTwiceInfoTextView.addRule(RelativeLayout.BELOW, playToneTwiceSettingCheckBox.getId());
			paramsPlayToneTwiceInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, playToneTwiceSettingCheckBox.getId());

			// Set layout parameters for the enable ack info textview
			RelativeLayout.LayoutParams paramsEnableAckInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			paramsEnableAckInfoTextView.setMargins(pixelsLeft, pixelsTop, pixelsRight, 0);
			paramsEnableAckInfoTextView.addRule(RelativeLayout.BELOW, enableAckCheckBox.getId());
			paramsEnableAckInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, enableAckCheckBox.getId());

			// Set layout parameters for the enable sms alarm info textview
			RelativeLayout.LayoutParams paramsEnableSmsAlarmInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			paramsEnableSmsAlarmInfoTextView.setMargins(pixelsLeft, pixelsTop, pixelsRight, 0);
			paramsEnableSmsAlarmInfoTextView.addRule(RelativeLayout.BELOW, enableSmsAlarmCheckBox.getId());
			paramsEnableSmsAlarmInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, enableSmsAlarmCheckBox.getId());

			// Apply the previously configured layout parameters to the correct
			// textviews
			soundSettingInfoTextView.setLayoutParams(paramsSoundSettingInfoTextView);
			playToneTwiceInfoTextView.setLayoutParams(paramsPlayToneTwiceInfoTextView);
			enableAckInfoTextView.setLayoutParams(paramsEnableAckInfoTextView);
			enableSmsAlarmInfoTextView.setLayoutParams(paramsEnableSmsAlarmInfoTextView);

			// Logging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "API level > 16, edit margins on information TextViews for the checkboxes");
		} else { // The device has API level < 17, we just need to check if the locale is german
			// If the locale on device is german(de) we need to adjust the margin top for the information textviews for the checkboxes to -6dp
			if ("de".equals(Locale.getDefault().getLanguage())) {
				// We need to get some Android resources in order to calculate proper pixel dimensions from dp
				Resources resources = getResources();
				
				// Calculate pixel dimensions for the different margins
				int pixelsLeft = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 38, resources.getDisplayMetrics()); // 38dp calculated to pixels
				int pixelsRight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, resources.getDisplayMetrics()); // 5dp calculated to pixels
				int pixelsTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -6, resources.getDisplayMetrics()); // -6dp calculated to pixels
				
				// Set layout parameters for the sound settings info textview
				RelativeLayout.LayoutParams paramsSoundSettingInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); // Wrap content, both on height and width
				paramsSoundSettingInfoTextView.setMargins(pixelsLeft, pixelsTop, pixelsRight, 0); // Margins left, top, right, bottom
				paramsSoundSettingInfoTextView.addRule(RelativeLayout.BELOW, soundSettingCheckBox.getId()); // Add rule, below UI widget
				paramsSoundSettingInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, soundSettingCheckBox.getId()); // Add rule, align left of UI widget

				// Set layout parameters for the play tone twice textview
				RelativeLayout.LayoutParams paramsPlayToneTwiceInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				paramsPlayToneTwiceInfoTextView.setMargins(pixelsLeft, pixelsTop, pixelsRight, 0);
				paramsPlayToneTwiceInfoTextView.addRule(RelativeLayout.BELOW, playToneTwiceSettingCheckBox.getId());
				paramsPlayToneTwiceInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, playToneTwiceSettingCheckBox.getId());

				// Set layout parameters for the enable ack info textview
				RelativeLayout.LayoutParams paramsEnableAckInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				paramsEnableAckInfoTextView.setMargins(pixelsLeft, pixelsTop, pixelsRight, 0);
				paramsEnableAckInfoTextView.addRule(RelativeLayout.BELOW, enableAckCheckBox.getId());
				paramsEnableAckInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, enableAckCheckBox.getId());

				// Set layout parameters for the enable sms alarm info textview
				RelativeLayout.LayoutParams paramsEnableSmsAlarmInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				paramsEnableSmsAlarmInfoTextView.setMargins(pixelsLeft, pixelsTop, pixelsRight, 0);
				paramsEnableSmsAlarmInfoTextView.addRule(RelativeLayout.BELOW, enableSmsAlarmCheckBox.getId());
				paramsEnableSmsAlarmInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, enableSmsAlarmCheckBox.getId());

				// Apply the previously configured layout parameters to the correct
				// textviews
				soundSettingInfoTextView.setLayoutParams(paramsSoundSettingInfoTextView);
				playToneTwiceInfoTextView.setLayoutParams(paramsPlayToneTwiceInfoTextView);
				enableAckInfoTextView.setLayoutParams(paramsEnableAckInfoTextView);
				enableSmsAlarmInfoTextView.setLayoutParams(paramsEnableSmsAlarmInfoTextView);

				// Logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "API level < 17 but the device has german(de) locale, set different margin-top on information TextViews for the checkboxes to fit the language");	
			} 		
		}

		// Declare and initialize variables of type ImageView
		divider1ImageView = (ImageView) findViewById(R.id.mainDivider1_iv);
		divider2ImageView = (ImageView) findViewById(R.id.mainDivider2_iv);
		divider3ImageView = (ImageView) findViewById(R.id.mainDivider3_iv);
		divider4ImageView = (ImageView) findViewById(R.id.mainDivider4_iv);

		// If Android API level less then 11 set bright gradient else set dark gradient
		if (Build.VERSION.SDK_INT < 11) {
			divider1ImageView.setImageResource(R.drawable.gradient_divider_10_and_down);
			divider2ImageView.setImageResource(R.drawable.gradient_divider_10_and_down);
			divider3ImageView.setImageResource(R.drawable.gradient_divider_10_and_down);
			divider4ImageView.setImageResource(R.drawable.gradient_divider_10_and_down);
			// Logging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "API level < 11, set bright gradients");
		} else {
			divider1ImageView.setImageResource(R.drawable.gradient_divider_11_and_up);
			divider2ImageView.setImageResource(R.drawable.gradient_divider_11_and_up);
			divider3ImageView.setImageResource(R.drawable.gradient_divider_11_and_up);
			divider4ImageView.setImageResource(R.drawable.gradient_divider_11_and_up);
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "API level > 10, set dark gradients");
		}

		// Set some attributes to the ackNumberEditText
		ackNumberEditText.setEnabled(false);
		ackNumberEditText.setClickable(false);
		ackNumberEditText.setFocusable(false);
		ackNumberEditText.setBackgroundColor(Color.WHITE);

		// Set some attributes to the fireDepartmentEditText
		rescueServiceEditText.setEnabled(false);
		rescueServiceEditText.setClickable(false);
		rescueServiceEditText.setFocusable(false);
		rescueServiceEditText.setBackgroundColor(Color.WHITE);
		rescueServiceEditText.setTextColor(Color.BLACK);

		// Set some attributes to the selectedToneEditText
		selectedToneEditText.setEnabled(false);
		selectedToneEditText.setClickable(false);
		selectedToneEditText.setFocusable(false);
		selectedToneEditText.setBackgroundColor(Color.WHITE);
		selectedToneEditText.setTextColor(Color.BLACK);

		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "All Views found");
	}
	
	/**
	 * To get <code>Shared Preferences</code> used by class
	 * <code>SmsAlarm</code>.
	 * 
	 * @see #setSmsAlarmPrefs()
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 *      logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String,
	 *      String, Throwable) logCatTxt(LogPriorities, String, String,
	 *      Throwable)
	 * @see ax.ha.it.smsalarm.PreferencesHandler#getPrefs(PrefKeys, PrefKeys,
	 *      DataTypes, Context) getPrefs(PrefKeys, PrefKeys, DataTypes, Context)
	 * @see ax.ha.it.smsalarm.PreferencesHandler#getPrefs(PrefKeys, PrefKeys,
	 *      DataTypes, Context, Object) getPrefs(PrefKeys, PrefKeys, DataTypes,
	 *      Context, Object)
	 */
	@SuppressWarnings("unchecked")
	private void getSmsAlarmPrefs() {
		// Some logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getSmsAlarmPrefs()", "Start retrieving shared preferences needed by class SmsAlarm");

		try {
			// Get shared preferences needed by class Sms Alarm
			primarySmsNumbers = (List<String>) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_LISTEN_NUMBERS_KEY, DataTypes.LIST, this);
			secondarySmsNumbers = (List<String>) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.SECONDARY_LISTEN_NUMBERS_KEY, DataTypes.LIST, this);
			primaryFreeTexts = (List<String>) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_LISTEN_FREE_TEXTS_KEY, DataTypes.LIST, this);
			secondaryFreeTexts = (List<String>) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.SECONDARY_LISTEN_FREE_TEXTS_KEY, DataTypes.LIST, this);
			primaryMessageToneId = (Integer) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_MESSAGE_TONE_KEY, DataTypes.INTEGER, this);
			secondaryMessageToneId = (Integer) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.SECONDARY_MESSAGE_TONE_KEY, DataTypes.INTEGER, this, 1);
			useOsSoundSettings = (Boolean) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.USE_OS_SOUND_SETTINGS_KEY, DataTypes.BOOLEAN, this);
			useAlarmAcknowledge = (Boolean) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.ENABLE_ACK_KEY, DataTypes.BOOLEAN, this);
			acknowledgeNumber = (String) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.ACK_NUMBER_KEY, DataTypes.STRING, this);
			playToneTwice = (Boolean) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.PLAY_TONE_TWICE_KEY, DataTypes.BOOLEAN, this);
			enableSmsAlarm = (Boolean) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.ENABLE_SMS_ALARM_KEY, DataTypes.BOOLEAN, this, true);
			rescueService = (String) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.RESCUE_SERVICE_KEY, DataTypes.STRING, this);
		} catch (IllegalArgumentException e) {
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":getSmsAlarmPrefs()", "An unsupported datatype was given as argument to PreferencesHandler.getPrefs()", e);
		}
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getSmsAlarmPrefs()", "Shared preferences retrieved");
	}

	/**
	 * Universal method to build up one of four different types of delete dialogs. The
	 * supported types are: <b><i>SMS_PRIMARY</b></i>, <b><i>SMS_SECONDARY</b></i>,
	 * <b><i>FREE_TEXT_PRIMARY</b></i> and <b><i>FREE_TEXT_SECONDARY</b></i>. If a dialog
	 * type are given as parameter thats not supported a dummy dialog will be
	 * built and shown.
	 * 
	 * @param type
	 *            Type of delete dialog to build up and shown
	 *            
	 * @see #buildAndShowAboutDialog()
	 * @see #buildAndShowInputDialog(DialogTypes)
	 * @see #buildAndShowToneDialog()
	 * @see #updateSecondarySmsNumberSpinner()
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 *      logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String,
	 *      String, Throwable) logCatTxt(LogPriorities, String, String,
	 *      Throwable)
	 * @see ax.ha.it.smsalarm.PreferencesHandler#setPrefs(PrefKeys, PrefKeys,
	 *      Object, Context) setPrefs(PrefKeys, PrefKeys, Object, Context)
	 */
	private void buildAndShowDeleteDialog(final DialogTypes type) {
		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowDeleteDialog()", "Start building delete dialog");
		
		// Build up the alert dialog
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		
		// Set some attributes, title and message containing actual number
		dialog.setIcon(android.R.drawable.ic_dialog_alert);
		
		/*
		 * Switch through the different dialog types and set correct strings and
		 * edittext to the dialog. If dialog type is non supported a default
		 * dialog DUMMY is built up.
		 */
		switch (type) {
		case SMS_PRIMARY:
			// Set title
			dialog.setTitle(R.string.DELETE_NUMBER_PROMPT_TITLE);
			// Build and set message
			dialog.setMessage(getString(R.string.DELETE_PRIMARY_NUMBER_PROMPT_MESSAGE) + " " + primarySmsNumbers.get(primarySmsNumberSpinner.getSelectedItemPosition()) + "?");
			// Set dialog to non cancelable
			dialog.setCancelable(false);
			// Logging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowDeleteDialog()", "Dialog attributes is set for dialog type SMS_PRIMARY");
			break;
		case SMS_SECONDARY:		
			dialog.setTitle(R.string.DELETE_NUMBER_PROMPT_TITLE);
			dialog.setMessage(getString(R.string.DELETE_SECONDARY_NUMBER_PROMPT_MESSAGE) + " " + secondarySmsNumbers.get(secondarySmsNumberSpinner.getSelectedItemPosition()) + "?");
			dialog.setCancelable(false);
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowDeleteDialog()", "Dialog attributes is set for dialog type SMS_SECONDARY");
			break;
		case FREE_TEXT_PRIMARY:
			dialog.setTitle(R.string.DELETE_FREE_TEXT_PROMPT_TITLE);
			dialog.setMessage(getString(R.string.DELETE_PRIMARY_FREE_TEXT_PROMPT_MESSAGE) + " " + primaryFreeTexts.get(primaryFreeTextSpinner.getSelectedItemPosition()) + "?");
			dialog.setCancelable(false);
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowDeleteDialog()", "Dialog attributes is set for dialog type FREE_TEXT_PRIMARY");
			break;
		case FREE_TEXT_SECONDARY:
			dialog.setTitle(R.string.DELETE_FREE_TEXT_PROMPT_TITLE);
			dialog.setMessage(getString(R.string.DELETE_SECONDARY_FREE_TEXT_PROMPT_MESSAGE) + " " + secondaryFreeTexts.get(secondaryFreeTextSpinner.getSelectedItemPosition()) + "?");
			dialog.setCancelable(false);
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowDeleteDialog()", "Dialog attributes is set for dialog type FREE_TEXT_SECONDARY");
			break;
		default: // <--Unsupported dialog type. Displaying a dummy dialog!
			dialog.setTitle("Congratulations!");
			dialog.setMessage("Somehow you got this dialog to show up! I bet a monkey must have been messing around with the code;-)");
			dialog.setCancelable(false);
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":buildAndShowDeleteDialog()", "A UNSUPPORTED dialog type has been given as parameter, a DUMMY dialog will be built and shown");
		}		

		// Set a positive button and listen on it
		dialog.setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// Log information
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowDeleteDialog().PosButton.OnClickListener().onClick()", "Positive Button pressed");
				
				/*
				 * Switch through the different dialog types and set proper
				 * input handling to each of them. If dialog type is non
				 * supported no input is taken.
				 */
				switch (type) {
				case SMS_PRIMARY:
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowDeleteDialog().PosButton.OnClickListener().onClick()", "PRIMARY sms number: \"" + primarySmsNumbers.get(primarySmsNumberSpinner.getSelectedItemPosition()) + "\" is about to be removed from the list of PRIMARY sms numbers");
					// Delete number from list
					primarySmsNumbers.remove(primarySmsNumberSpinner.getSelectedItemPosition());
					try {
						// Store to shared preferences
						prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_LISTEN_NUMBERS_KEY, primarySmsNumbers, SmsAlarm.this);
					} catch (IllegalArgumentException e) {
						logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":buildAndShowDeleteDialog().PosButton.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
					}
					// Update affected UI widgets
					updatePrimarySmsNumberSpinner();
					break;
				case SMS_SECONDARY:
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowDeleteDialog().PosButton.OnClickListener().onClick()", "SECONDARY sms number: \"" + secondarySmsNumbers.get(secondarySmsNumberSpinner.getSelectedItemPosition()) + "\" is about to be removed from the list of SECONDARY sms numbers");
					secondarySmsNumbers.remove(secondarySmsNumberSpinner.getSelectedItemPosition());
					try {
						prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.SECONDARY_LISTEN_NUMBERS_KEY, secondarySmsNumbers, SmsAlarm.this);
					} catch (IllegalArgumentException e) {
						logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":buildAndShowDeleteDialog().PosButton.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
					}
					updateSecondarySmsNumberSpinner();
					break;
				case FREE_TEXT_PRIMARY:
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowDeleteDialog().PosButton.OnClickListener().onClick()", "PRIMARY free text: \"" + primaryFreeTexts.get(primaryFreeTextSpinner.getSelectedItemPosition()) + "\" is about to be removed from the list of PRIMARY free texts");
					// Delete free text from list
					primaryFreeTexts.remove(primaryFreeTextSpinner.getSelectedItemPosition());
					try {
						// Store to shared preferences
						prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_LISTEN_FREE_TEXTS_KEY, primaryFreeTexts, SmsAlarm.this);
					} catch (IllegalArgumentException e) {
						logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":buildAndShowDeleteDialog().PosButton.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
					}
					// Update affected UI widgets
					updatePrimaryFreeTextSpinner();
					break;
				case FREE_TEXT_SECONDARY:
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowDeleteDialog().PosButton.OnClickListener().onClick()", "SECONDARY free text: \"" + secondaryFreeTexts.get(secondaryFreeTextSpinner.getSelectedItemPosition()) + "\" is about to be removed from the list of SECONDARY free texts");
					secondaryFreeTexts.remove(secondaryFreeTextSpinner.getSelectedItemPosition());
					try {
						prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.SECONDARY_LISTEN_FREE_TEXTS_KEY, secondaryFreeTexts, SmsAlarm.this);
					} catch (IllegalArgumentException e) {
						logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":buildAndShowDeleteDialog().PosButton.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
					}
					updateSecondaryFreeTextSpinner();
					break;
				default: // <--Unsupported dialog type
					logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":buildAndShowDeleteDialog().PositiveButton.OnClickListener().onClick()", "Nothing is stored beacause given dialog type is UNSUPPORTED, given dialog is of type number: \"" + type.name() + "\"");
				}
			}
		});
		
		// Only set neutral button if dialog type is supported
		if (type.ordinal() >= DialogTypes.SMS_PRIMARY.ordinal() && type.ordinal() <= DialogTypes.FREE_TEXT_SECONDARY.ordinal()) {
			// Set a neutral button, due to documentation it has same functionality as "back" button
			dialog.setNeutralButton(R.string.NO, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					// DO NOTHING, except logging
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowDeleteDialog().NeutralButton.OnClickListener().onClick()", "Neutral Button pressed in dialog, nothing done");
				}
			});
		}

		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowDeleteDialog()", "Showing dialog");

		// Show it
		dialog.show();
	}

	/**
	 * Universal method to build up one of four different types of input dialogs. The
	 * supported types are: <b><i>SMS_PRIMARY</b></i>, <b><i>SMS_SECONDARY</b></i>,
	 * <b><i>ACKNOWLEDGE</b></i> and <b><i>RESCUESERVICE</b></i>. If a dialog
	 * type are given as parameter thats not supported a dummy dialog will be
	 * built and shown.
	 * 
	 * @param type
	 *            Type of dialog to build up and show
	 * 
	 * @see #buildAndShowAboutDialog()
	 * @see #buildAndShowToneDialog()
	 * @see #buildAndShowDeleteDialog()
	 * @see #updatePrimaryListenSmsNumberEditText()
	 * @see #updateSecondarySmsNumberSpinner()
	 * @see #updateAcknowledgeNumberEditText()
	 * @see #updateRescueServiceEditText()
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 *      logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String,
	 *      String) logCatTxt(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String,
	 *      String, Throwable) logCatTxt(LogPriorities, String, String,
	 *      Throwable)
	 * @see ax.ha.it.smsalarm.PreferencesHandler#setPrefs(PrefKeys, PrefKeys,
	 *      Object, Context) setPrefs(PrefKeys, PrefKeys, Object, Context)
	 */
	private void buildAndShowInputDialog(final DialogTypes type) {
		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog()", "Start building dialog");
		
		// Build up the alert dialog
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);

		// Set some attributes
		dialog.setIcon(android.R.drawable.ic_dialog_info);

		// Set an EditText view to get user input
		final EditText inputEditText = new EditText(this);
		final EditText noBlanksInputEditText = new EditText(this);
		
		// Filter that trim away any blank space character, in practice this means that only a single word is allowed to be written
		InputFilter filter = new InputFilter() {			
			@Override
			public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
				return source.toString().trim();
			}
		};

		// Set filter to free text input
		noBlanksInputEditText.setFilters(new InputFilter[]{ filter });
		
		/*
		 * Switch through the different dialog types and set correct strings and
		 * edittext to the dialog. If dialog type is non supported a default
		 * dialog DUMMY is built up.
		 */
		switch (type) {
		case SMS_PRIMARY:
			// Set title
			dialog.setTitle(R.string.NUMBER_PROMPT_TITLE);
			// Set message
			dialog.setMessage(R.string.PRIMARY_NUMBER_PROMPT_MESSAGE);
			// Set hint to edittext
			noBlanksInputEditText.setHint(R.string.NUMBER_PROMPT_HINT);
			// Set Input type to edittext
			noBlanksInputEditText.setInputType(InputType.TYPE_CLASS_PHONE);
			// Set dialog to non cancelable
			dialog.setCancelable(false);
			// Bind dialog to input
			dialog.setView(noBlanksInputEditText);
			// Logging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog()", "Dialog attributes is set for dialog type SMS_PRIMARY");
			break;
		case SMS_SECONDARY:
			dialog.setTitle(R.string.NUMBER_PROMPT_TITLE);
			dialog.setMessage(R.string.SECONDARY_NUMBER_PROMPT_MESSAGE);
			noBlanksInputEditText.setHint(R.string.NUMBER_PROMPT_HINT);
			noBlanksInputEditText.setInputType(InputType.TYPE_CLASS_PHONE);
			dialog.setCancelable(false);
			dialog.setView(noBlanksInputEditText);
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog()", "Dialog attributes is set for dialog type SMS_SECONDARY");
			break;
		case FREE_TEXT_PRIMARY:
			dialog.setTitle(R.string.FREE_TEXT_PROMPT_TITLE);
			dialog.setMessage(R.string.PRIMARY_FREE_TEXT_PROMPT_MESSAGE);
			noBlanksInputEditText.setHint(R.string.FREE_TEXT_PROMPT_HINT);
			noBlanksInputEditText.setInputType(InputType.TYPE_CLASS_TEXT);
			dialog.setCancelable(false);
			dialog.setView(noBlanksInputEditText);
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog()", "Dialog attributes is set for dialog type FREE_TEXT_PRIMARY");
			break;
		case FREE_TEXT_SECONDARY:
			dialog.setTitle(R.string.FREE_TEXT_PROMPT_TITLE);
			dialog.setMessage(R.string.SECONDARY_FREE_TEXT_PROMPT_MESSAGE);
			noBlanksInputEditText.setHint(R.string.FREE_TEXT_PROMPT_HINT);
			noBlanksInputEditText.setInputType(InputType.TYPE_CLASS_TEXT);
			dialog.setCancelable(false);
			dialog.setView(noBlanksInputEditText);
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog()", "Dialog attributes is set for dialog type FREE_TEXT_SECONDARY");
			break;			
		case ACKNOWLEDGE:
			dialog.setTitle(R.string.NUMBER_PROMPT_TITLE);
			dialog.setMessage(R.string.ACK_NUMBER_PROMPT_MESSAGE);
			noBlanksInputEditText.setHint(R.string.NUMBER_PROMPT_HINT);
			noBlanksInputEditText.setInputType(InputType.TYPE_CLASS_PHONE);
			dialog.setCancelable(false);
			dialog.setView(noBlanksInputEditText);
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog()", "Dialog attributes is set for dialog type ACKNOWLEDGE");
			break;
		case RESCUESERVICE:
			dialog.setTitle(R.string.RESCUE_SERVICE_PROMPT_TITLE);
			dialog.setMessage(R.string.RESCUE_SERVICE_PROMPT_MESSAGE);
			inputEditText.setHint(R.string.RESCUE_SERVICE_NAME_HINT);
			inputEditText.setInputType(InputType.TYPE_CLASS_TEXT);
			dialog.setCancelable(false);
			dialog.setView(inputEditText);
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog()", "Dialog attributes is set for dialog type RESCUESERVICE");
			break;
		default: // <--Unsupported dialog type. Displaying a dummy dialog!
			dialog.setTitle("Congratulations!");
			dialog.setMessage("Somehow you got this dialog to show up! I bet a monkey must have been messing around with the code;-)");
			dialog.setCancelable(false);
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":buildAndShowInputDialog()", "A UNSUPPORTED dialog type has been given as parameter, a DUMMY dialog will be built and shown");
		}

		// Set a positive button and listen on it
		dialog.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
			// To store input from dialogs edittext field
			String input = "";
			
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// Log information
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "Positive Button pressed");

				// Boolean indicating if there are duplicates of primary and secondary sms numbers and also of the primary and secondary free texts
				boolean duplicatedNumbers = false;
				boolean duplicatedFreeTexts = false;

				/*
				 * Switch through the different dialog types and set proper
				 * input handling to each of them. If dialog type is non
				 * supported no input is taken.
				 */
				switch (type) {
				case SMS_PRIMARY:					
					//Store input
					input = noBlanksInputEditText.getText().toString();
					// If input doesn't exist in the list of secondarySmsNumbers and input isn't empty
					if (!existsIn(input, secondarySmsNumbers) && !input.equals("")) {
						// Iterate through all strings in the list of primarySmsNumbers to check if number already exists
						for (String number : primarySmsNumbers) {
							// If a string in the list is equal with the input then it's duplicated
							if (number.equalsIgnoreCase(input)) {
								duplicatedNumbers = true;
							}
						}
							
						// Store input if duplicated numbers is false
						if (!duplicatedNumbers) {
							// Add given input to list
							primarySmsNumbers.add(input);
							try {
								// Store to shared preferences
								prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_LISTEN_NUMBERS_KEY, primarySmsNumbers, SmsAlarm.this);
							} catch (IllegalArgumentException e) {
								logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
							}	
							// Update affected UI widgets
							updatePrimarySmsNumberSpinner();
							// Log
							logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "New PRIMARY sms number has been stored from user input to the list of PRIMARY sms numbers. New PRIMARY sms number is: \"" + input + "\"");
						} else {
							Toast.makeText(SmsAlarm.this, R.string.NUMBER_ALREADY_IN_PRIMARY_LIST, Toast.LENGTH_LONG).show();
							logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "Given PRIMARY sms number(" + input + ") already exists in the list of PRIMARY sms numbers and therefore cannot be stored. Showing dialog of type SMS_PRIMARY again");
							buildAndShowInputDialog(type);							
						}
					} else {
						// Empty input was given
						if (input.equals("")) {
							Toast.makeText(SmsAlarm.this, R.string.NUMBER_IS_NEEDED, Toast.LENGTH_LONG).show();
							logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "Given PRIMARY sms number is empty and therefore cannot be stored. Showing dialog of type SMS_PRIMARY again");						
						} else { // Given primary number exists in the list of secondary numbers
							Toast.makeText(SmsAlarm.this, R.string.DUPLICATED_NUMBERS, Toast.LENGTH_LONG).show();
							logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "Given PRIMARY sms number(" + input + ") already exists in the list of SECONDARY sms numbers and therefore cannot be stored. Showing dialog of type SMS_PRIMARY again");				
						}
						buildAndShowInputDialog(type);
					}
					break;
				case SMS_SECONDARY:
					//Store input
					input = noBlanksInputEditText.getText().toString();
					// If input doesn't exist in the list of primarySmsNumbers and input isn't empty
					if (!existsIn(input, primarySmsNumbers) && !input.equals("")) {
						// Iterate through all strings in the list of secondarySmsNumbers to check if number already exists
						for (String number : secondarySmsNumbers) {
							// If a string in the list is equal with the input then it's duplicated
							if (number.equalsIgnoreCase(input)) {
								duplicatedNumbers = true;
							}
						}
							
						// Store input if duplicated numbers is false
						if (!duplicatedNumbers) {
							// Add given input to list
							secondarySmsNumbers.add(input);
							try {
								// Store to shared preferences
								prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.SECONDARY_LISTEN_NUMBERS_KEY, secondarySmsNumbers, SmsAlarm.this);
							} catch (IllegalArgumentException e) {
								logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
							}	
							// Update affected UI widgets
							updateSecondarySmsNumberSpinner();
							// Log
							logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "New SECONDARY sms number has been stored from user input to the list of SECONDARY sms numbers. New SECONDARY sms number is: \"" + input + "\"");
						} else {
							Toast.makeText(SmsAlarm.this, R.string.NUMBER_ALREADY_IN_SECONDARY_LIST, Toast.LENGTH_LONG).show();
							logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "Given SECONDARY sms number(" + input + ") already exists in the list of SECONDARY sms numbers and therefore cannot be stored. Showing dialog of type SMS_SECONDARY again");
							buildAndShowInputDialog(type);							
						}
					} else {
						// Empty input was given
						if (input.equals("")) {
							Toast.makeText(SmsAlarm.this, R.string.NUMBER_IS_NEEDED, Toast.LENGTH_LONG).show();
							logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "Given SECONDARY sms number is empty and therefore cannot be stored. Showing dialog of type SMS_SECONDARY again");						
						} else { // Given primary number exists in the list of secondary numbers
							Toast.makeText(SmsAlarm.this, R.string.DUPLICATED_NUMBERS, Toast.LENGTH_LONG).show();
							logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "Given SECONDARY sms number(" + input + ") already exists in the list of PRIMARY sms numbers and therefore cannot be stored. Showing dialog of type SMS_SECONDARY again");				
						}
						buildAndShowInputDialog(type);
					}
					break;
				case FREE_TEXT_PRIMARY:
					//Store input
					input = noBlanksInputEditText.getText().toString();
					// If input doesn't exist in the list of secondaryFreeTexts and input isn't empty
					if (!existsIn(input, secondaryFreeTexts) && !input.equals("")) {
						// Iterate through all strings in the list of primaryFreeTexts to check if text already exists
						for (String text : primaryFreeTexts) {
							// If a string in the list is equal with the input then it's duplicated
							if (text.equalsIgnoreCase(input)) {
								duplicatedFreeTexts = true;
							}
						}
							
						// Store input if duplicated free texts is false
						if (!duplicatedFreeTexts) {
							// Add given input to list
							primaryFreeTexts.add(input);
							try {
								// Store to shared preferences
								prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_LISTEN_FREE_TEXTS_KEY, primaryFreeTexts, SmsAlarm.this);
							} catch (IllegalArgumentException e) {
								logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
							}	
							// Update affected UI widgets
							updatePrimaryFreeTextSpinner();
							// Log
							logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "New PRIMARY free text has been stored from user input to the list of PRIMARY free texts. New PRIMARY free text is: \"" + input + "\"");
						} else {
							Toast.makeText(SmsAlarm.this, R.string.FREE_TEXT_ALREADY_IN_PRIMARY_LIST, Toast.LENGTH_LONG).show();
							logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "Given PRIMARY free text(" + input + ") already exists in the list of PRIMARY free texts and therefore cannot be stored. Showing dialog of type FREE_TEXT_PRIMARY again");
							buildAndShowInputDialog(type);							
						}
					} else {
						// Empty primary free text was given
						if (input.equals("")) {
							Toast.makeText(SmsAlarm.this, R.string.TEXT_IS_NEEDED, Toast.LENGTH_LONG).show();
							logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "Given PRIMARY free text is empty and therefore cannot be stored. Showing dialog of type FREE_TEXT_PRIMARY again");							
						} else { // Given primary free text exists in the list of secondary free texts
							Toast.makeText(SmsAlarm.this, R.string.DUPLICATED_FREE_TEXTS, Toast.LENGTH_LONG).show();
							logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "Given PRIMARY free text(" + input + ") already exists in the list of SECONDARY free texts and therefore cannot be stored. Showing dialog of type FREE_TEXT_PRIMARY again");						
						}
						buildAndShowInputDialog(type);
					}
					break;
				case FREE_TEXT_SECONDARY:
					//Store input
					input = noBlanksInputEditText.getText().toString();
					// If input doesn't exist in the list of primaryFreeTexts and input isn't empty
					if (!existsIn(input, primaryFreeTexts) && !input.equals("")) {
						// Iterate through all strings in the list of secondaryFreeTexts to check if text already exists
						for (String text : secondaryFreeTexts) {
							// If a string in the list is equal with the input then it's duplicated
							if (text.equalsIgnoreCase(input)) {
								duplicatedFreeTexts = true;
							}
						}
							
						// Store input if duplicated free texts is false
						if (!duplicatedFreeTexts) {
							// Add given input to list
							secondaryFreeTexts.add(input);
							try {
								// Store to shared preferences
								prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.SECONDARY_LISTEN_FREE_TEXTS_KEY, secondaryFreeTexts, SmsAlarm.this);
							} catch (IllegalArgumentException e) {
								logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
							}	
							// Update affected UI widgets
							updateSecondaryFreeTextSpinner();
							// Log
							logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "New SECONDARY free text has been stored from user input to the list of SECONDARY free texts. New SECONDARY free text is: \"" + input + "\"");
						} else {
							Toast.makeText(SmsAlarm.this, R.string.FREE_TEXT_ALREADY_IN_SECONDARY_LIST, Toast.LENGTH_LONG).show();
							logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "Given SECONDARY free text(" + input + ") already exists in the list of SECONDARY free texts and therefore cannot be stored. Showing dialog of type FREE_TEXT_SECONDARY again");
							buildAndShowInputDialog(type);							
						}
					} else {
						// Empty primary free text was given
						if (input.equals("")) {
							Toast.makeText(SmsAlarm.this, R.string.TEXT_IS_NEEDED, Toast.LENGTH_LONG).show();
							logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "Given SECONDARY free text is empty and therefore cannot be stored. Showing dialog of type FREE_TEXT_SECONDARY again");							
						} else { // Given secondary free text exists in the list of primary free texts
							Toast.makeText(SmsAlarm.this, R.string.DUPLICATED_FREE_TEXTS, Toast.LENGTH_LONG).show();
							logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "Given SECONDARY free text(" + input + ") already exists in the list of PRIMARY free texts and therefore cannot be stored. Showing dialog of type FREE_TEXT_SECONDARY again");						
						}
						buildAndShowInputDialog(type);
					}
					break;
				case ACKNOWLEDGE:
					// Store input
					acknowledgeNumber = noBlanksInputEditText.getText().toString();
					try {
						// Store to shared preferences
						prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.ACK_NUMBER_KEY, acknowledgeNumber, SmsAlarm.this);
					} catch (IllegalArgumentException e) {
						logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
					}
					// Update affected UI widgets
					updateAcknowledgeNumberEditText();
					// Log
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "New ACKNOWLEDGE phone number has been stored from user input . New ACKNOWLEDGE phone number is: \"" + acknowledgeNumber + "\"");
					break;
				case RESCUESERVICE:
					// Store input to class variable
					rescueService = inputEditText.getText().toString();
					try {
						// Store to shared preferences
						prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.RESCUE_SERVICE_KEY, rescueService, SmsAlarm.this);
					} catch (IllegalArgumentException e) {
						logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
					}
					// Update affected UI widgets
					updateRescueServiceEditText();
					// Log
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "New RESCUESERVICE name has been stored from user input . New RESCUESERVICE name is: \"" + rescueService + "\"");
					break;
				default: // <--Unsupported dialog type
					logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "Nothing is stored beacause given dialog type is UNSUPPORTED, given dialog is of type number: \"" + Integer.toString(type.ordinal()) + "\"");
				}
			}
		});

		// Only set neutral button if dialog type is supported
		if (type.ordinal() >= DialogTypes.SMS_PRIMARY.ordinal() && type.ordinal() <= DialogTypes.RESCUESERVICE.ordinal()) {
			// Set a neutral button, due to documentation it has same functionality as "back" button
			dialog.setNeutralButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					// DO NOTHING, except logging
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog().NeutralButton.OnClickListener().onClick()", "Neutral Button pressed in dialog, nothing done");
				}
			});
		}

		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog()", "Showing dialog");

		// Show it
		dialog.show();
	}

	/**
	 * To build up and show a dialog with a list populated with message tones.
	 * User chooses applications message tones from that list.
	 * 
	 * @see #buildAndShowInputDialog(DialogTypes)
	 * @see #buildAndShowAboutDialog()
	 * @see #updateSelectedToneEditText()
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 *      logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String,
	 *      String) logCatTxt(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String,
	 *      String, Throwable) logCatTxt(LogPriorities, String, String,
	 *      Throwable)
	 * @see ax.ha.it.smsalarm.PreferencesHandler#setPrefs(PrefKeys, PrefKeys,
	 *      Object, Context) setPrefs(PrefKeys, PrefKeys, Object, Context)
	 */
	private void buildAndShowToneDialog() {
		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowToneDialog()", "Start building tone dialog");

		// Build up the alert dialog
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);

		// Set attributes
		dialog.setIcon(android.R.drawable.ic_dialog_info);
		dialog.setTitle(R.string.TONE_PROMPT_TITLE);
		dialog.setCancelable(false);

		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowToneDialog()", "Dialog attributes set");

		// Set items to list view from resource array tones
		dialog.setItems(R.array.tones, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int listPosition) {
				// Log information
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowToneDialog().Item.OnClickListener().onClick()", "Item in message tones list pressed");

				// Store position(toneId) in correct variable, depending on spinner value
				if (toneSpinnerPos == 0) { // <--PRIMARY MESSAGE TONE
					// Store primary message tone id from position of list
					primaryMessageToneId = listPosition;
					// Log information
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowToneDialog().Item.OnClickListener().onClick()", "New PRIMARY message tone selected. Tone: \"" + noiseHandler.msgToneLookup(SmsAlarm.this, primaryMessageToneId) + "\", id: \"" + primaryMessageToneId + "\" and tone spinner position: \"" + Integer.toString(toneSpinnerPos) + "\"");
					try {
						// Store primary message tone id to preferences to preferences
						prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_MESSAGE_TONE_KEY, primaryMessageToneId, SmsAlarm.this);
					} catch (IllegalArgumentException e) {
						logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":buildAndShowToneDialog().Item.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
					}
					// Update selected tone EditText
					updateSelectedToneEditText();
				} else if (toneSpinnerPos == 1) { // <--SECONDARY MESSAGE TONE
					secondaryMessageToneId = listPosition;
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowToneDialog().Item.OnClickListener().onClick()", "New SECONDARY message tone selected. Tone: \"" + noiseHandler.msgToneLookup(SmsAlarm.this, secondaryMessageToneId) + "\", id: \"" + secondaryMessageToneId + "\" and tone Spinner position: \"" + Integer.toString(toneSpinnerPos) + "\"");
					try {
						prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.SECONDARY_MESSAGE_TONE_KEY, secondaryMessageToneId, SmsAlarm.this);
					} catch (IllegalArgumentException e) {
						logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":buildAndShowToneDialog().Item.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
					}
					updateSelectedToneEditText();
				} else { // <--UNSUPPORTED SPINNER POSITION
					// DO NOTHING EXCEPT LOG ERROR MESSAGE
					logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":buildAndShowToneDialog().Item.OnClickListener().onClick()", "Invalid spinner position occurred. Current tone spinner position is: \"" + Integer.toString(toneSpinnerPos) + "\"");
				}
			}
		});

		// Set a neutral button and listener
		dialog.setNeutralButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// DO NOTHING, except logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowToneDialog().NeutralButton.OnClickListener().onClick()", "Neutral Button pressed in dialog, nothing done");
			}
		});

		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowToneDialog()", "Showing dialog");

		// Show dialog
		dialog.show();
	}

	/**
	 * To build up and show an about dialog.
	 * 
	 * @see #buildAndShowDeleteDialog()
	 * @see #buildAndShowInputDialog(DialogTypes)
	 * @see #buildAndShowToneDialog()
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 *      logCat(LogPriorities, String, String)
	 */
	private void buildAndShowAboutDialog() {
		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowAboutDialog()", "Start building about dialog");

		// Build up the alert dialog
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);

		LayoutInflater factory = LayoutInflater.from(this);

		final View view = factory.inflate(R.layout.about, null);

		// Get TextViews from its view
		TextView buildTextView = (TextView) view.findViewById(R.id.aboutBuild_tv);
		TextView versionTextView = (TextView) view.findViewById(R.id.aboutVersion_tv);
		
		// Set correct text, build and version number, to the TextViews
		buildTextView.setText(String.format(getString(R.string.ABOUT_BUILD), getString(R.string.APP_BUILD)));
		versionTextView.setText(String.format(getString(R.string.ABOUT_VERSION), getString(R.string.APP_VERSION)));
		
		// Set correct icon depending on api level
		if (Build.VERSION.SDK_INT < 11) {
			dialog.setIcon(R.drawable.ic_launcher_trans_10_and_down);
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowAboutDialog()", "API level < 11, set icon adapted to black background color");
		} else {
			dialog.setIcon(R.drawable.ic_launcher_trans);
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowAboutDialog()", "API level > 10, set icon adapted to white background color");
		}
		
		// Set rest of the attributes
		dialog.setTitle(R.string.ABOUT);
		dialog.setView(view);
		dialog.setCancelable(false);

		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowAboutDialog()", "Dialog attributes set");

		// Set a neutral button
		dialog.setNeutralButton(R.string.OK, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// DO NOTHING, except logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowAboutDialog().NeutralButton.OnClickListener().onClick()", "Neutral Button pressed in dialog, nothing done");
			}
		});

		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowAboutDialog()", "Showing dialog");

		// Show dialog
		dialog.show();
	}
	
	/**
	 * To check if given <code>String</code> exists in given <code>List</code> 
	 * of <code>Strings</code>. Method is not case sensitive.
	 * 
	 * @param string String to check if exists in list.
	 * @param list List to check if string exists in.
	 * 
	 * @return <code>true</code> if given String exists in given List else <code>false</code>.<br>
	 * 		   <code>false</code> is also returned if either given argument is <code>null</code>.
	 */
	@SuppressLint("DefaultLocale")
	public boolean existsIn(String string, List<String> list) {
		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":existsIn()", "String=\"" + string + "\" is about to be checked if exists in list=\"" + list + "\"");
		
		if (string != null && list != null) {
			List<String> caseUpperList = new ArrayList<String>();
			
			for (String str: list) {
				caseUpperList.add(str.toUpperCase());
			}
			
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":existsIn()", "Returning " + caseUpperList.contains(string.toUpperCase()));
			return caseUpperList.contains(string.toUpperCase());
		} else {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":existsIn()", "Given string and/or list is null, returning FALSE");
			return false;
		}
	}

	/**
	 * To update Sms Alarms whole User Interface.
	 * 
	 * @see #updatePrimaryListenSmsNumberEditText()
	 * @see #updateSecondarySmsNumberSpinner()
	 * @see #updatePrimaryFreeTextSpinner()
	 * @see #updateSecondaryFreeTextSpinner()
	 * @see #updateAcknowledgeNumberEditText()
	 * @see #updateRescueServiceEditText()
	 * @see #updateSelectedToneEditText()
	 * @see #updateUseOsSoundSettingsCheckBox()
	 * @see #updatePlayToneTwiceCheckBox()
	 * @see #updateEnableSmsAlarmCheckBox()
	 * @see #updateAcknowledgeWidgets()
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 *      logCat(LogPriorities, String, String)
	 */
	private void updateWholeUI() {
		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateWholeUI()", "Whole user interface is about to be updated");

		// Update primary sms numbers Spinner
		updatePrimarySmsNumberSpinner();
		
		// Update secondary sms numbers Spinner
		updateSecondarySmsNumberSpinner();
		
		// Update primary free text Spinner
		updatePrimaryFreeTextSpinner();
		
		// Update secondary free text spinner
		updateSecondaryFreeTextSpinner();

		// Update acknowledge number EditText
		updateAcknowledgeNumberEditText();

		// Update rescue service EditText
		updateRescueServiceEditText();

		// Update selected EditText widget
		updateSelectedToneEditText();

		// Update use OS sound settings CheckBox widget
		updateUseOsSoundSettingsCheckBox();

		// Update play tone twice CheckBox widget
		updatePlayToneTwiceCheckBox();

		// Update enable Sms Alarm CheckBox widget
		updateEnableSmsAlarmCheckBox();

		// Update widgets in relation to alarm acknowledgment
		updateAcknowledgeWidgets();

		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateWholeUI()", "User interface updated");
	}
	
	/**
	 * To update primary sms number <code>Spinner</code> with correct
	 * values.
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 *      logCat(LogPriorities, String, String)
	 */
	private void updatePrimarySmsNumberSpinner() {
		// Check if there are primary sms numbers and build up a proper spinner according to that information
		if (!primarySmsNumbers.isEmpty()) {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, primarySmsNumbers);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			primarySmsNumberSpinner.setAdapter(adapter);
			// Logging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updatePrimarySmsNumberSpinner()", "Populate PRIMARY sms number spinner with values: " + primarySmsNumbers);
		} else {
			// Only add item to list if it's empty
			if (emptySmsNumbers.isEmpty()) {
				emptySmsNumbers.add(getString(R.string.ENTER_PHONE_NUMBER_HINT));
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, emptySmsNumbers);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			primarySmsNumberSpinner.setAdapter(adapter);
			// Logging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updatePrimarySmsNumberSpinner()", "List with PRIMARY sms numbers is empty, populating spinner with an empty list");
		}

		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updatePrimarySmsNumberSpinner()", "PRIMARY sms numbers spinner updated");
	}

	/**
	 * To update secondary sms number <code>Spinner</code> with correct
	 * values.
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 *      logCat(LogPriorities, String, String)
	 */
	private void updateSecondarySmsNumberSpinner() {
		// Check if there are secondary sms numbers and build up a proper spinner according to that information
		if (!secondarySmsNumbers.isEmpty()) {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, secondarySmsNumbers);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			secondarySmsNumberSpinner.setAdapter(adapter);
			// Logging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateSecondarySmsNumberSpinner()", "Populate SECONDARY sms number spinner with values: " + secondarySmsNumbers);
		} else {
			// Only add item to list if it's empty
			if (emptySmsNumbers.isEmpty()) {
				emptySmsNumbers.add(getString(R.string.ENTER_PHONE_NUMBER_HINT));
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, emptySmsNumbers);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			secondarySmsNumberSpinner.setAdapter(adapter);
			// Logging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateSecondarySmsNumberSpinner()", "List with SECONDARY sms numbers is empty, populating spinner with an empty list");
		}

		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateSecondarySmsNumberSpinner()", "SECONDARY sms numbers spinner updated");
	}
	
	/**
	 * To update primary free texts <code>Spinner</code> with correct
	 * values.
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 *      logCat(LogPriorities, String, String)
	 */
	private void updatePrimaryFreeTextSpinner() {
		// Check if there are primary free test and build up a proper spinner according to that information
		if (!primaryFreeTexts.isEmpty()) {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, primaryFreeTexts);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			primaryFreeTextSpinner.setAdapter(adapter);
			// Logging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updatePrimaryFreeTextSpinner()", "Populate PRIMARY free text spinner with values: " + primaryFreeTexts);
		} else {
			// Only add item to list if it's empty
			if (emptyFreeTexts.isEmpty()) {
				emptyFreeTexts.add(getString(R.string.ENTER_FREE_TEXT_HINT));
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, emptyFreeTexts);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			primaryFreeTextSpinner.setAdapter(adapter);
			// Logging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updatePrimaryFreeTextSpinner()", "List with PRIMARY free texts is empty, populating spinner with an empty list");
		}
		
		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updatePrimaryFreeTextSpinner()", "PRIMARY free text spinner updated");
	}
	
	/**
	 * To update secondary free texts <code>Spinner</code> with correct
	 * values.
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 *      logCat(LogPriorities, String, String)
	 */
	private void updateSecondaryFreeTextSpinner() {
		// Check if there are primary listen free test and build up a proper spinner according to that information
		if (!secondaryFreeTexts.isEmpty()) {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, secondaryFreeTexts);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			secondaryFreeTextSpinner.setAdapter(adapter);
			// Logging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateSecondaryFreeTextSpinner()", "Populate SECONDARY free text spinner with values: " + secondaryFreeTexts);
		} else {
			// Only add item to list if it's empty
			if (emptyFreeTexts.isEmpty()) {
				emptyFreeTexts.add(getString(R.string.ENTER_FREE_TEXT_HINT));
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, emptyFreeTexts);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			secondaryFreeTextSpinner.setAdapter(adapter);
			// Logging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateSecondaryFreeTextSpinner()", "List with SECONDARY free texts is empty, populating spinner with an empty list");
		}
		
		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateSecondaryFreeTextSpinner()", "SECONDARY free text spinner updated");
	}

	/**
	 * To update acknowledge number <code>EditText</code> widget.
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 *      logCat(LogPriorities, String, String)
	 */
	private void updateAcknowledgeNumberEditText() {
		// Update acknowledge number EditText with value
		ackNumberEditText.setText(acknowledgeNumber);

		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateAcknowledgeNumberEditText()", "Acknowledge number edittext set to: " + acknowledgeNumber);
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateAcknowledgeNumberEditText()", "Acknowledge number edittext updated");
	}

	/**
	 * To update selected tone <code>EditText</code> widget with value of
	 * <code>toneSpinner</code> position.
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 *      logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String,
	 *      String) logCatTxt(LogPriorities, String, String)
	 */
	private void updateSelectedToneEditText() {
		// Log tone spinner position
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateSelectedToneEditText()", "Tone spinner position is: " + Integer.toString(toneSpinnerPos));

		// Set message tone to the selectedToneEditText, depending on which value spinner has. Also log this event
		if (toneSpinnerPos == 0) {
			selectedToneEditText.setText(noiseHandler.msgToneLookup(this, primaryMessageToneId));
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateSelectedToneEditText()", "Selected tone edittext updated");
		} else if (toneSpinnerPos == 1) {
			selectedToneEditText.setText(noiseHandler.msgToneLookup(this, secondaryMessageToneId));
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateSelectedToneEditText()", "Selected tone edittext updated");
		} else {
			// DO NOTHING EXCEPT LOG ERROR MESSAGE
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":updateSelectedToneEditText()", "Invalid spinner position occurred. Current tone spinner position is: \"" + Integer.toString(toneSpinnerPos) + "\"");
		}
	}

	/**
	 * To update rescue service <code>EditText</code> widget.
	 * 
	 * @see ax.ha.it.smsalarm#LogHandler.logCatTxt(int, String , String)
	 */
	private void updateRescueServiceEditText() {
		// Update rescue service EditText
		rescueServiceEditText.setText(rescueService);

		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateRescueServiceEditText()", "Rescue service edittext set to: " + rescueService);
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateRescueServiceEditText()", "Rescue service edittext updated");
	}

	/**
	 * To update use OS sound settings <code>CheckBox</code> widget.
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 *      logCat(LogPriorities, String, String)
	 */
	private void updateUseOsSoundSettingsCheckBox() {
		// Update use OS sound settings CheckBox
		if (useOsSoundSettings) {
			soundSettingCheckBox.setChecked(true);
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateUseOsSoundSettingsCheckBox()", "Use OS sound settings checkbox \"Checked\"(" + useOsSoundSettings + ")");
		} else {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateUseOsSoundSettingsCheckBox()", "Use OS sound settings checkbox \"Unchecked\"(" + useOsSoundSettings + ")");
		}

		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateUseOsSoundSettingsCheckBox()", "Use OS sound settings checkbox updated");
	}

	/**
	 * To update play tone twice <code>CheckBox</code> widget.
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 *      logCat(LogPriorities, String, String)
	 */
	private void updatePlayToneTwiceCheckBox() {
		// Update play tone twice CheckBox
		if (playToneTwice) {
			playToneTwiceSettingCheckBox.setChecked(true);
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updatePlayToneTwiceCheckBox()", "Play tone twice checkbox \"Checked\"(" + playToneTwice + ")");
		} else {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updatePlayToneTwiceCheckBox()", "Play tone twice checkbox \"Unchecked\"(" + playToneTwice + ")");
		}

		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updatePlayToneTwiceCheckBox()", "Play tone twice checkbox updated");
	}

	/**
	 * To update enable Sms Alarm <code>CheckBox</code> widget.
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 *      logCat(LogPriorities, String, String)
	 */
	private void updateEnableSmsAlarmCheckBox() {
		// Update enable Sms Alarm CheckBox(default checked=true)
		if (!enableSmsAlarm) {
			enableSmsAlarmCheckBox.setChecked(false);
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateEnableSmsAlarmCheckBox()", "Enable SmsAlarm checkbox \"Unchecked\"(" + enableSmsAlarm + ")");
		} else {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateEnableSmsAlarmCheckBox()", "Enable SmsAlarm checkbox \"Checked\"(" + enableSmsAlarm + ")");
		}

		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateEnableSmsAlarmCheckBox()", "Enable SmsAlarm checkbox updated");
	}

	/**
	 * To update widgets with relations to alarm acknowledgement. These are
	 * widgets of type <code>CheckBox</code>, <code>Button</code> and
	 * <code>EditText</code>, they are enableAckCheckBox, ackNumberButton and
	 * ackNumberEditText.
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String)
	 *      logCat(LogPriorities, String, String)
	 */
	private void updateAcknowledgeWidgets() {
		/*
		 * Set checkbox for the enableAckCheckBox to true or false, also set
		 * some attributes to the ackNumberButton and the ackNumberField
		 */
		if (useAlarmAcknowledge) {
			enableAckCheckBox.setChecked(true);
			ackNumberButton.setEnabled(true);
			ackNumberEditText.setTextColor(Color.BLACK);
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateAcknowledgeWidgets()", "Enable acknowledge checkbox \"Checked\"(" + useAlarmAcknowledge + "), acknowledge number button is \"Enabled\" and acknowledge number edittext is \"Enabled\"");
		} else {
			ackNumberButton.setEnabled(false);
			ackNumberEditText.setTextColor(Color.GRAY);
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateAcknowledgeWidgets()", "Enable acknowledge checkbox \"Unchecked\"(" + useAlarmAcknowledge + "), acknowledge number button is \"Disabled\" and acknowledge number edittext is \"Disabled\"");
		}

		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateAcknowledgeWidgets()", "Acknowledge alarm UI widgets updated");
	}
}