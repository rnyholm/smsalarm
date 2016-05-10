/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.activity;

import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.application.SmsAlarmApplication.GoogleAnalyticsHandler;
import ax.ha.it.smsalarm.application.SmsAlarmApplication.GoogleAnalyticsHandler.EventAction;
import ax.ha.it.smsalarm.application.SmsAlarmApplication.GoogleAnalyticsHandler.EventCategory;
import ax.ha.it.smsalarm.fragment.AlarmLogFragment;
import ax.ha.it.smsalarm.fragment.SlidingMenuFragment;
import ax.ha.it.smsalarm.fragment.SmsSettingsFragment;
import ax.ha.it.smsalarm.fragment.SoundSettingsFragment;
import ax.ha.it.smsalarm.fragment.dialog.AlarmSignalDialog;
import ax.ha.it.smsalarm.handler.DatabaseHandler;
import ax.ha.it.smsalarm.provider.WidgetProvider;

/**
 * Main activity for <b><i>Sms Alarm</i></b>. Holds a user interface consisting of a <code>ContenFrame</code>, <code>SlidingMenu</code> and a <code>ActionBar</code>, which in turn will be populated
 * with other {@link Fragment}'s, which holds there own user interface and logics.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.4.1
 * @since 0.9beta
 */
public class SmsAlarm extends SlidingFragmentActivity {
	// Important flag, if set all eventual DEBUG logging and the Debug/Test menu will be shown, set false for production!
	public static final boolean DEBUG = false;

	// Action that can be set to an intent if fragment should be switched to AlarmLogFragment upon creation/new intent of this activity
	public static final String ACTION_SWITCH_TO_ALARM_LOG_FRAGMENT = "ax.ha.it.smsalarm.SWITCH_TO_ALARM_LOG_FRAGMENT";

	// Some static limiting constants to be used to decide whether or not the debug/testing menu items should be shown
	private static final long SHOW_DEBUG_OPTIONS_TIME_LIMIT = 5000;
	private static final int SHOW_DEBUG_OPTIONS_TAP_LIMIT = 10;

	// Helper variables to decide whether or not the debug/testing menu should be shown
	private long menuKeyFirstReleased = 0;
	private int tapCount = 0;

	/**
	 * Perform initialization of <code>Layout</code>'s, {@link Fragment}'s, the {@link SlidingMenu} and {@link ActionBar}. Configuration of these objects is also done within this method as well as
	 * initialization of the {@link DatabaseHandler}.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.content_frame);
		setBehindContentView(R.layout.menu_frame);

		setMenuFragment(savedInstanceState);
		setContentFragment(savedInstanceState);

		configureSlidingMenu();
		configureActionBar();
	}

	@Override
	protected void onStart() {
		super.onStart();

		GoogleAnalyticsHandler.reportActivityStart(this);
	};

	@Override
	protected void onResume() {
		super.onResume();

		GoogleAnalyticsHandler.setScreenNameAndSendScreenViewHit(this);
	}

	/**
	 * When application pauses the widget({@link WidgetProvider}) will be updated.
	 */
	@Override
	protected void onPause() {
		super.onPause();

		// Update all widgets associated to this application
		WidgetProvider.updateWidgets(this);
	}

	@Override
	protected void onStop() {
		super.onStop();

		GoogleAnalyticsHandler.reportActivityStop(this);
	};

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		// To ensure that onResumeFragments() gets called, if we would do this a bunch of IllegalStateExceptions would be thrown
		// See:
		// http://stackoverflow.com/questions/7469082/getting-exception-illegalstateexception-can-not-perform-this-action-after-onsa/18824459#comment44854010_18824459
		super.onPostResume();
		setContentFragment(intent);
	}

	/**
	 * Overridden in such way that if the {@link SlidingMenu} is showing when <b><i>back button</i></b> is pressed, the sliding menu will be closed. If it's not showing when back button is pressed the
	 * application will follow the usual back pressed behavior.
	 */
	@Override
	public void onBackPressed() {
		if (getSlidingMenu().isMenuShowing()) {
			toggle();
		} else {
			super.onBackPressed();
		}
	}

	/**
	 * Listen on {@link KeyEvent}, this override is only interested in <code>KeyEvent</code>'s of type {@link KeyEvent#KEYCODE_MENU} (menu button). When that button is pressed the {@link SlidingMenu}
	 * will be toggled, id it's not showing it will be shown and vice versa.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			toggle();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * Listen on {@link KeyEvent}, this override is only interested in <code>KeyEvent</code>'s of type {@link KeyEvent#KEYCODE_MENU} (menu button). When that button is released in a high enough
	 * frequency the {@link SlidingMenu} will be rebuilt with the debug/testing menu items, if the menu doesn't contain the items already. In those cases the items will be removed.
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// Only if not in debug mode, else these menu items are showing anyway
		if (!SmsAlarm.DEBUG) {
			if (keyCode == KeyEvent.KEYCODE_MENU) {
				// Get current system time
				long currentTime = System.currentTimeMillis();

				// If it's the first time user releases the menu key, or it has elapsed to long time since menu key was first released, reset
				// everything
				if (menuKeyFirstReleased == 0 || ((currentTime - menuKeyFirstReleased) > SHOW_DEBUG_OPTIONS_TIME_LIMIT)) {
					menuKeyFirstReleased = currentTime;
					tapCount = 1;
				} else { // Just increase the tap counter
					tapCount++;
				}

				// The user has tapped the menu key enough times in order to display the debug/testing menu options
				if (tapCount == SHOW_DEBUG_OPTIONS_TAP_LIMIT) {
					// Get the support fragment manager and begin transaction
					FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
					// Get the fragment within the menu frame container and toggle the impose debug flag
					SlidingMenuFragment slidingMenuFragment = (SlidingMenuFragment) getSupportFragmentManager().findFragmentById(R.id.menuFrame_fl);
					slidingMenuFragment.toggleImposeDebugMenu();

					// At last detach and attach the fragment once more and commit the transaction
					ft.detach(slidingMenuFragment);
					ft.attach(slidingMenuFragment);
					ft.commit();
				}
			}
		}

		return super.onKeyUp(keyCode, event);
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

	/**
	 * To configure the {@link ActionBar}.
	 * 
	 * @see #configureSlidingMenu()
	 */
	private void configureActionBar() {
		// @formatter:off
		getSupportActionBar().setDisplayHomeAsUpEnabled(true); // By pressing action bar icon the menu will opens
		getSupportActionBar().setIcon(R.drawable.ic_launcher); // Icon in action bar
		setSlidingActionBarEnabled(true); // Action bar slides along with the menu
		// @formatter:on
	}

	/**
	 * To configure the {@link SlidingMenu}.
	 * 
	 * @see #configureActionBar()
	 */
	private void configureSlidingMenu() {
		// @formatter:off
		SlidingMenu sm = getSlidingMenu();
		sm.setMode(SlidingMenu.LEFT); // Menu slide in from left
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN); // Menu can be opened with a sliding move anywhere on screen
		sm.setShadowWidthRes(R.dimen.shadow_width); // Width of menus shadow
		sm.setShadowDrawable(R.drawable.shadow); // Shadows drawable
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset); // Margin on the right side of screen when menu is open
		sm.setFadeEnabled(true); // Want menu to fade in and out
		sm.setFadeDegree(0.35f); // Fading value
		// @formatter:on
	}

	/**
	 * To set correct {@link Fragment} to the <code>MenuFrame</code>.<br>
	 * If no <code>savedInstanceState</code> exists a new instance of {@link SlidingMenuFragment} will be placed in the <code>MenuFrame</code>.
	 * 
	 * @param savedInstanceState
	 *            If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in {@link #onSaveInstanceState}. <b><i>Note:
	 *            Otherwise it is null.</i></b>
	 */
	private void setMenuFragment(Bundle savedInstanceState) {
		// Activity is not being re-created, set new instance of SlidingMenuFragment to menu frame
		if (savedInstanceState == null) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.menuFrame_fl, new SlidingMenuFragment());
			ft.commit();
		}
	}

	/**
	 * To set correct {@link Fragment} to the <code>ContentFrame</code>.<br>
	 * If no <code>savedInstanceState</code> exists and if {@link SmsAlarm#ACTION_SWITCH_TO_ALARM_LOG_FRAGMENT} hasn't been set as action to this activities {@link Intent} a new instance of the
	 * default <code>Fragment</code> {@link SmsSettingsFragment} will be placed in the <code>ContentFrame</code>.<br>
	 * If {@link SmsAlarm#ACTION_SWITCH_TO_ALARM_LOG_FRAGMENT} has been set as action to this activities <code>Intent</code> then a new instance of <code>Fragment</code> {@link AlarmLogFragment} will
	 * be placed in the <code>ContentFrame</code>.
	 * 
	 * @param savedInstanceState
	 *            If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in {@link #onSaveInstanceState}. <b><i>Note:
	 *            Otherwise it is null.</i></b>
	 * @see #setContentFragment(Intent)
	 */
	private void setContentFragment(Bundle savedInstanceState) {
		// Activity is not being re-created, set default fragment to content frame
		if (savedInstanceState == null) {
			// Set content fragment depending on intent
			setContentFragment(getIntent());
		}
	}

	/**
	 * To set correct {@link Fragment} to the <code>ContentFrame</code>.<br>
	 * What <code>Fragment</code> is to be placed in the <code>ContentFrame</code> depends on if given {@link Intent} has {@link SmsAlarm#ACTION_SWITCH_TO_ALARM_LOG_FRAGMENT} set as action.<br>
	 * If the <code>Intent</code> is missing that action then the default {@link SmsSettingsFragment} will be placed in the <code>ContentFrame</code>. <br>
	 * On the other hand if the action exists then a {@link AlarmLogFragment} will be placed in the <code>ContentFrame</code>.
	 * <p>
	 * Note. If the <code>SlidingMenu</code> is showing when an <code>AlarmLogFragment</code> is placed in the <code>ContentView</code> it will be placed in background, this is to ensure the
	 * <code>AlarmLogFragment</code> is on top.
	 * 
	 * @param intent
	 *            <code>Intent</code> which action are checked and from it a decision what <code>Fragment</code> that should be placed in <code>ContentFrame</code> are taken.
	 */
	private void setContentFragment(Intent intent) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		// Check if switch to alarm log fragment action exists
		if (ACTION_SWITCH_TO_ALARM_LOG_FRAGMENT.equals(intent.getAction())) {
			ft.replace(R.id.contentFrame_fl, new AlarmLogFragment());

			// If menu is showing, toggle it to background
			if (getSlidingMenu().isMenuShowing()) {
				toggle();
			}

			// Report to Google Analytics that the alarm log fragment was opened through widget interaction
			GoogleAnalyticsHandler.sendEvent(EventCategory.USER_INTERFACE, EventAction.WIDGET_INTERACTION, WidgetProvider.OPEN_ALARM_LOG_LABEL);
		} else {
			ft.replace(R.id.contentFrame_fl, new SmsSettingsFragment());
		}

		ft.commit();
	}

	/**
	 * To switch {@link Fragment} in <code>ContentFrame</code>.<br>
	 * The current <code>Fragment</code> in <code>ContentFrame</code> will be replaced by given fragment.
	 * 
	 * @param fragment
	 *            <code>Fragment</code> to be placed in content frame.
	 */
	public void switchContent(Fragment fragment) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.contentFrame_fl, fragment);
		ft.commit();

		// Close menu and show above view(content view)
		getSlidingMenu().showContent();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Only interested in requestCodes indicating adding a new alarm signal, it doesn't matter what the resultCodes are, must take care of this
		// event anyways
		if (requestCode == AlarmSignalDialog.ADD_ALARM_SIGNAL_FROM_PRIMARY_DIALOG_REQUEST_CODE || requestCode == AlarmSignalDialog.ADD_ALARM_SIGNAL_FROM_SECONDARY_DIALOG_REQUEST_CODE) {
			// To ensure that onResumeFragments() gets called, if we would do this a bunch of IllegalStateExceptions would be thrown
			// See:
			// http://stackoverflow.com/questions/7469082/getting-exception-illegalstateexception-can-not-perform-this-action-after-onsa/18824459#comment44854010_18824459
			super.onPostResume();

			// Find SoundSettingsFragment by id of content frame, we know that it's on top of the FragmentManagers BackStack so this is safe.
			// Do further handling in fragments onActivityResult()
			SoundSettingsFragment soundSettingsFragment = (SoundSettingsFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame_fl);
			soundSettingsFragment.onActivityResult(requestCode, resultCode, data);
		}
	}
}