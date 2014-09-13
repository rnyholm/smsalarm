/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.activity;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.WidgetProvider;
import ax.ha.it.smsalarm.enumeration.DialogTypes;
import ax.ha.it.smsalarm.fragment.SlidingMenuFragment;
import ax.ha.it.smsalarm.fragment.SmsSettingsFragment;
import ax.ha.it.smsalarm.handler.DatabaseHandler;
import ax.ha.it.smsalarm.handler.LogHandler;
import ax.ha.it.smsalarm.handler.LogHandler.LogPriorities;
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

	// Object to handle database access and methods
	private DatabaseHandler db;

	// Content and fragment for the slidingmenu
	private Fragment content;
	private ListFragment fragment;

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

		// Initialize database handler object from context
		db = new DatabaseHandler(this);

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
}