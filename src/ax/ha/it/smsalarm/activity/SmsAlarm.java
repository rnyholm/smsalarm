/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.WidgetProvider;
import ax.ha.it.smsalarm.enumeration.DialogTypes;
import ax.ha.it.smsalarm.fragment.SlidingMenuFragment;
import ax.ha.it.smsalarm.fragment.SmsSettingsFragment;
import ax.ha.it.smsalarm.handler.DatabaseHandler;
import ax.ha.it.smsalarm.handler.LogHandler;
import ax.ha.it.smsalarm.handler.LogHandler.LogPriorities;
import ax.ha.it.smsalarm.handler.PreferencesHandler;
import ax.ha.it.smsalarm.handler.PreferencesHandler.DataTypes;
import ax.ha.it.smsalarm.handler.PreferencesHandler.PrefKeys;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

/**
 * Main activity to configure application. Also holds the main User Interface.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.2.1
 * @since 0.9beta
 * @see #onCreate(Bundle)
 * @see #onPause()
 * @see #onDestroy()
 */
public class SmsAlarm extends SlidingFragmentActivity {

	// Log tag string
	private final String LOG_TAG = getClass().getSimpleName();

	// Objects needed for logging, shared preferences and noise handling
	private final LogHandler logger = LogHandler.getInstance();
	private static final PreferencesHandler prefHandler = PreferencesHandler.getInstance();

	// Object to handle database access and methods
	private DatabaseHandler db;

	// Content and fragment for the slidingmenu
	private Fragment content;
	private ListFragment fragment;

	// Variables of different UI elements and types
	// The EdittextObjects
	private EditText rescueServiceEditText;

	// The Button objects

	private Button editRescueServiceButton;

	// The CheckBox objects
	private CheckBox enableSmsAlarmCheckBox;

	// The ImageView objects
	private ImageView divider1ImageView;
	private ImageView divider2ImageView;
	private ImageView divider3ImageView;
	private ImageView divider4ImageView;

	// The textView objects
	private TextView enableSmsAlarmInfoTextView;

	// String to store firedepartments name in
	private String rescueService = "";

	// Boolean variables to store whether to use OS soundsettings or not, and if acknowledge is
	// enabled
	private boolean enableSmsAlarm = true;

	/**
	 * When activity starts, this method is the entry point. The User Interface is built up and different <code>Listeners</code> are set within this
	 * method.
	 * 
	 * @param savedInstanceState
	 *            Default Bundle
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
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCatTxt(LogPriorities, String, String) logCatTxt(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCatTxt(LogPriorities, String, String, Throwable) logCatTxt(LogPriorities, String, String,
	 *      Throwable)
	 * @see ax.ha.it.smsalarm.handler.NoiseHandler#makeNoise(Context, int, boolean, boolean) makeNoise(Context, int, boolean, boolean)
	 * @see ax.ha.it.smsalarm.handler.PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context) setPrefs(PrefKeys, PrefKeys, Object, Context)
	 * @see ax.ha.it.smsalarm.handler.DatabaseHandler ax.ha.it.smsalarm.DatabaseHandler
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Log in debugging and information purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate()", "Creation of Sms Alarm started");

		// Set behind and content view
		setBehindContentView(R.layout.menu_frame);
		setContentView(R.layout.content_frame);

		// Set correct fragment to menu and correct content to this object
		setFragmentToMenu(savedInstanceState);
		setContentFragment(savedInstanceState);

		// Set correct content to content frame
		getSupportFragmentManager().beginTransaction().replace(R.id.contentFrame_fl, content).commit();

		// Configure the sliding menu
		configureSlidingMenu();

		// Configure action bar
		configureActionBar();

		// Get sharedPreferences
		getSmsAlarmPrefs();

//		// FindViews
//		findViews();
//
//		// Initialize database handler object from context
//		db = new DatabaseHandler(this);
//

//
//

//
//		// Set listener to editRescueServiceButton
//		editRescueServiceButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				// Logging
//				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().editRescueServiceButton.OnClickListener().onClick()", "Edit rescue service button pressed");
//				// Build up and show input dialog of type primary number
//				buildAndShowInputDialog(DialogTypes.RESCUESERVICE);
//			}
//		});
//

//

//

//

//

//
//		// Set listener to enableSmsAlarmCheckBox
//		enableSmsAlarmCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//				// Log that CheckBox been pressed
//				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().enableSmsAlarmCheckBox.onCheckedChange()", "Enable Sms Alarm checkbox pressed(or checkbox initialized)");
//
//				// Set checkbox depending on it's checked status and store variable
//				if (enableSmsAlarmCheckBox.isChecked()) {
//					// Store value to variable
//					enableSmsAlarm = true;
//					// Logging
//					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().enableSmsAlarmCheckBox.onCheckedChange()", "Enable SmsAlarm checkbox \"Checked\"(" + enableSmsAlarm + ")");
//				} else {
//					enableSmsAlarm = false;
//					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate().enableSmsAlarmCheckBox.onCheckedChange()", "Enable SmsAlarm checkbox \"Unchecked\"(" + enableSmsAlarm + ")");
//				}
//
//				try {
//					// Store value to shared preferences
//					prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.ENABLE_SMS_ALARM_KEY, enableSmsAlarm, SmsAlarm.this);
//				} catch (IllegalArgumentException e) {
//					logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":onCreate().enableSmsAlarmCheckBox.onCheckedChange()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
//				}
//			}
//		});
//

//
//		// Update all UI widgets
//		updateWholeUI();

		// Log in debugging and information purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate()", "Creation of Sms Alarm completed");
	}

	/**
	 * To handle events to trigger when activity pauses. <b><i>Not yet implemented.</i></b>
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
	 * To handle events to trigger when activity destroys. Writes all alarms in database into a <code>.html</code> log file.
	 * 
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logAlarm(List, Context) logAlarm(List, Context)
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.handler.DatabaseHandler#getAllAlarm() getAllAlarm()
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

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		getSupportFragmentManager().putFragment(outState, "content", content);
	}

	@Override
	public void onBackPressed() {
		if (getSlidingMenu().isMenuShowing()) {
			toggle();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			toggle();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				toggle();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private void configureActionBar() {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(R.drawable.ic_launcher);
		setSlidingActionBarEnabled(true);
	}

	private void configureSlidingMenu() {
		// configure the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setMode(SlidingMenu.LEFT);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
	}

	private void setFragmentToMenu(Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			fragment = new SlidingMenuFragment();// new RandomList();
			ft.replace(R.id.menuFrame_fl, fragment);
			ft.commit();
		} else {
			fragment = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.menuFrame_fl);
		}
	}

	private void setContentFragment(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			content = getSupportFragmentManager().getFragment(savedInstanceState, "content");
		}
		if (content == null) {
			content = new SmsSettingsFragment(this);
		}
	}

	public void switchContent(Fragment fragment) {
		content = fragment;
		getSupportFragmentManager().beginTransaction().replace(R.id.contentFrame_fl, fragment).commit();
		getSlidingMenu().showContent();
	}

	/**
	 * To build up the menu, called one time only and that's the first time the menu is inflated.
	 * 
	 * @see #onOptionsItemSelected(MenuItem)
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Logging
//		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreateOptionsMenu()", "Menu created");
//		MenuInflater inflater = getMenuInflater();
//		inflater.inflate(R.menu.menu, menu);
//		return true;
//	}

	/**
	 * Method to inflate menu with it's items.
	 * 
	 * @see #buildAndShowAboutDialog()
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String, String, Throwable) logCatTxt(LogPriorities, String, String, Throwable)
	 * @see ax.ha.it.smsalarm.PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context) setPrefs(PrefKeys, PrefKeys, Object, Context)
	 */
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//			case R.id.ABOUT:
//				// Logging
//				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onOptionsItemSelected()", "Menu item ABOUT selected");
//				// Build up and show the about dialog
//				buildAndShowAboutDialog();
//				return true;
//			default:
//				return super.onOptionsItemSelected(item);
//		}
//	}

	/**
	 * To find UI widgets and get their reference by ID stored in class variables.
	 * 
	 * @see #onCreate(Bundle)
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	private void findViews() {
		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "Start finding Views by their ID");

		// Declare and initialize variables of type EditText
		rescueServiceEditText = (EditText) findViewById(R.id.rescueServiceName_et);

		// Declare and initialize variables of type button
		editRescueServiceButton = (Button) findViewById(R.id.editRescueServiceName_btn);

		// Declare and initialize variables of type CheckBox
		enableSmsAlarmCheckBox = (CheckBox) findViewById(R.id.enableSmsAlarm_chk);

		// Declare and initialize variables of type TextView
		enableSmsAlarmInfoTextView = (TextView) findViewById(R.id.enableSmsAlarmHint_tv);

		// If Android API level is greater than Jelly Bean
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
			// We need to get some Android resources in order to calculate proper pixel dimensions
			// from dp
			Resources resources = getResources();

			// Calculate pixel dimensions for the different margins
			// 32dp calculated to pixels
			int pixelsLeft = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, resources.getDisplayMetrics());
			// 5dp calculated to pixels
			int pixelsRight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, resources.getDisplayMetrics());
			int pixelsTop = 0;
			// If the locale on device is german(de) set pixelstop to -6dp else -9dp
			if ("de".equals(Locale.getDefault().getLanguage())) {
				// Logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "The device has german(de) locale, set different margin-top on information TextViews for the checkboxes than other locales");
				// -6dp calculated to pixels
				pixelsTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -6, resources.getDisplayMetrics());
			} else {
				// -9dp calculated to pixels
				pixelsTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -9, resources.getDisplayMetrics());
			}

			// Set layout parameters for the sound settings info textview
			// Wrap content, both on height and width

			// Set layout parameters for the play tone twice textview
			RelativeLayout.LayoutParams paramsPlayToneTwiceInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			paramsPlayToneTwiceInfoTextView.setMargins(pixelsLeft, pixelsTop, pixelsRight, 0);

			// Set layout parameters for the enable sms alarm info textview
			RelativeLayout.LayoutParams paramsEnableSmsAlarmInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			paramsEnableSmsAlarmInfoTextView.setMargins(pixelsLeft, pixelsTop, pixelsRight, 0);
			paramsEnableSmsAlarmInfoTextView.addRule(RelativeLayout.BELOW, enableSmsAlarmCheckBox.getId());
			paramsEnableSmsAlarmInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, enableSmsAlarmCheckBox.getId());

			// Apply the previously configured layout parameters to the correct
			// textviews
			enableSmsAlarmInfoTextView.setLayoutParams(paramsEnableSmsAlarmInfoTextView);

			// Logging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "API level > 16, edit margins on information TextViews for the checkboxes");
		} else { // The device has API level < 17, we just need to check if the locale is german
			// If the locale on device is german(de) we need to adjust the margin top for the
			// information textviews for the checkboxes to -6dp
			if ("de".equals(Locale.getDefault().getLanguage())) {
				// We need to get some Android resources in order to calculate proper pixel
				// dimensions from dp
				Resources resources = getResources();

				// Calculate pixel dimensions for the different margins
				// 38dp calculated to pixels
				int pixelsLeft = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 38, resources.getDisplayMetrics());
				// 5dp calculated to pixels
				int pixelsRight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, resources.getDisplayMetrics());
				// -6dp calculated to pixels
				int pixelsTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -6, resources.getDisplayMetrics());

				// Set layout parameters for the sound settings info textview
				// Wrap content, both on height and width
				// Set layout parameters for the enable sms alarm info textview
				RelativeLayout.LayoutParams paramsEnableSmsAlarmInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				paramsEnableSmsAlarmInfoTextView.setMargins(pixelsLeft, pixelsTop, pixelsRight, 0);
				paramsEnableSmsAlarmInfoTextView.addRule(RelativeLayout.BELOW, enableSmsAlarmCheckBox.getId());
				paramsEnableSmsAlarmInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, enableSmsAlarmCheckBox.getId());

				// Apply the previously configured layout parameters to the correct
				// textviews
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

		// If Android API level is pre Honeycomb set bright gradient else set dark gradient
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
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

		// Set some attributes to the fireDepartmentEditText
		rescueServiceEditText.setEnabled(false);
		rescueServiceEditText.setClickable(false);
		rescueServiceEditText.setFocusable(false);
		rescueServiceEditText.setBackgroundColor(Color.WHITE);
		rescueServiceEditText.setTextColor(Color.BLACK);

		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "All Views found");
	}

	/**
	 * To get <code>Shared Preferences</code> used by class <code>SmsAlarm</code>.
	 * 
	 * @see #setSmsAlarmPrefs()
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCatTxt(LogPriorities, String, String, Throwable) logCatTxt(LogPriorities, String, String,
	 *      Throwable)
	 * @see ax.ha.it.smsalarm.handler.PreferencesHandler#getPrefs(PrefKeys, PrefKeys, DataTypes, Context) getPrefs(PrefKeys, PrefKeys, DataTypes,
	 *      Context)
	 * @see ax.ha.it.smsalarm.handler.PreferencesHandler#getPrefs(PrefKeys, PrefKeys, DataTypes, Context, Object) getPrefs(PrefKeys, PrefKeys,
	 *      DataTypes, Context, Object)
	 */
	@SuppressWarnings("unchecked")
	private void getSmsAlarmPrefs() {
		// Some logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getSmsAlarmPrefs()", "Start retrieving shared preferences needed by class SmsAlarm");

		try {
			// Get shared preferences needed by class Sms Alarm
			enableSmsAlarm = (Boolean) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.ENABLE_SMS_ALARM_KEY, DataTypes.BOOLEAN, this, true);
			rescueService = (String) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.RESCUE_SERVICE_KEY, DataTypes.STRING, this);
		} catch (IllegalArgumentException e) {
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":getSmsAlarmPrefs()", "An unsupported datatype was given as argument to PreferencesHandler.getPrefs()", e);
		}
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getSmsAlarmPrefs()", "Shared preferences retrieved");
	}

	/**
	 * Universal method to build up one of four different types of input dialogs. The supported types are: <b><i>SMS_PRIMARY</b></i>,
	 * <b><i>SMS_SECONDARY</b></i>, <b><i>ACKNOWLEDGE</b></i> and <b><i>RESCUESERVICE</b></i>. If a dialog type are given as parameter thats not
	 * supported a dummy dialog will be built and shown.
	 * 
	 * @param type
	 *            Type of dialog to build up and show
	 * @see #buildAndShowAboutDialog()
	 * @see #buildAndShowToneDialog()
	 * @see #buildAndShowDeleteDialog()
	 * @see #updatePrimaryListenSmsNumberEditText()
	 * @see #updateSecondarySmsNumberSpinner()
	 * @see #updateAcknowledgeNumberEditText()
	 * @see #updateRescueServiceEditText()
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCatTxt(LogPriorities, String, String) logCatTxt(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCatTxt(LogPriorities, String, String, Throwable) logCatTxt(LogPriorities, String, String,
	 *      Throwable)
	 * @see ax.ha.it.smsalarm.handler.PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context) setPrefs(PrefKeys, PrefKeys, Object, Context)
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

		// Set a textwatcher to the edittext removing any whitespace characters
		noBlanksInputEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// Do nothing here!
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// Do nothing here!
			}

			@Override
			public void afterTextChanged(Editable editable) {
				// Store input from edittext as a string stripped from whitespace
				String result = editable.toString().replaceAll(" ", "");

				// If input from edittext and result don't have the same length whitespace has been
				// stripped, we need to set text to edittext and move cursor to correct position
				if (!editable.toString().equals(result)) {
					noBlanksInputEditText.setText(result);
					noBlanksInputEditText.setSelection(result.length());
				}
			}
		});

		/*
		 * Switch through the different dialog types and set correct strings and edittext to the dialog. If dialog type is non supported a default
		 * dialog DUMMY is built up.
		 */
		switch (type) {
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
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// Log information
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "Positive Button pressed");

				/*
				 * Switch through the different dialog types and set proper input handling to each of them. If dialog type is non supported no input
				 * is taken.
				 */
				switch (type) {
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
		if (type.ordinal() >= DialogTypes.FREE_TEXT_PRIMARY.ordinal() && type.ordinal() <= DialogTypes.RESCUESERVICE.ordinal()) {
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
	 * To build up and show an about dialog.
	 * 
	 * @see #buildAndShowDeleteDialog()
	 * @see #buildAndShowInputDialog(DialogTypes)
	 * @see #buildAndShowToneDialog()
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
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
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
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
	 * To check if given <code>String</code> exists in given <code>List</code> of <code>Strings</code>. Method is not case sensitive.
	 * 
	 * @param string
	 *            String to check if exists in list.
	 * @param list
	 *            List to check if string exists in.
	 * @return <code>true</code> if given String exists in given List else <code>false</code>.<br>
	 *         <code>false</code> is also returned if either given argument is <code>null</code>.
	 */
	@SuppressLint("DefaultLocale")
	private boolean existsIn(String string, List<String> list) {
		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":existsIn()", "String=\"" + string + "\" is about to be checked if exists in list=\"" + list + "\"");

		if (string != null && list != null) {
			List<String> caseUpperList = new ArrayList<String>();

			for (String str : list) {
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
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	private void updateWholeUI() {
		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateWholeUI()", "Whole user interface is about to be updated");

		// Update rescue service EditText
		updateRescueServiceEditText();

		// Update enable Sms Alarm CheckBox widget
		updateEnableSmsAlarmCheckBox();

		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateWholeUI()", "User interface updated");
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
	 * To update enable Sms Alarm <code>CheckBox</code> widget.
	 * 
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
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

}