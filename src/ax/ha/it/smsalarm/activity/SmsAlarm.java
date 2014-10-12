/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.fragment.SlidingMenuFragment;
import ax.ha.it.smsalarm.fragment.SmsSettingsFragment;
import ax.ha.it.smsalarm.handler.DatabaseHandler;
import ax.ha.it.smsalarm.pojo.Alarm;
import ax.ha.it.smsalarm.provider.WidgetProvider;
import ax.ha.it.smsalarm.util.AlarmLogger;

import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

/**
 * Main activity for <b><i>Sms Alarm</i></b>. Holds a user interface consisting of a <code>ContenFrame</code>, <code>SlidingMenu</code> and a
 * <code>ActionBar</code>, which in turn will be populated with other {@link Fragment}'s, which holds there own user interface and logics.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 0.9beta
 */
public class SmsAlarm extends SlidingFragmentActivity {
	public static final boolean DEBUG = true;

	// To get database access
	private DatabaseHandler db;

	/**
	 * Perform initialization of <code>Layout</code>'s, {@link Fragment}'s, the {@link SlidingMenu} and {@link ActionBar}. Configuration of these
	 * objects is also done within this method as well as initialization of the {@link DatabaseHandler}.
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

		// Initialize database handler object from context
		db = new DatabaseHandler(this);
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

	/**
	 * When application is destroyed all {@link Alarm}'s stored in database will be written a <code>*.html</code> file.
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();

		// Get all alarms from database and log them into a *.html file
		AlarmLogger.getInstance().logAlarms(db.fetchAllAlarm(), this);
	}

	/**
	 * Overridden in such way that if the {@link SlidingMenu} is showing when <b><i>back button</i></b> is pressed, the sliding menu will be closed.
	 * If it's not showing when back button is pressed the application will follow the usual back pressed behavior.
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
	 * Listen on {@link KeyEvent}, this override is only interested in <code>KeyEvent</code>'s of type {@link KeyEvent#KEYCODE_MENU} (menu button).
	 * When that button is pressed the {@link SlidingMenu} will be toggled, id it's not showing it will be shown and vice versa.
	 */
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

	/**
	 * To configure the {@link ActionBar}.
	 * 
	 * @see #configureSlidingMenu()
	 */
	private void configureActionBar() {
		// @formatter:off
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);	// By pressing action bar icon the menu will opens
		getSupportActionBar().setIcon(R.drawable.ic_launcher);	// Icon in action bar
		setSlidingActionBarEnabled(true);						// Action bar slides along with the menu
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
		sm.setMode(SlidingMenu.LEFT);							// Menu slide in from left
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);	// Menu can be opened with a sliding move anywhere on screen
		sm.setShadowWidthRes(R.dimen.shadow_width);				// Width of menus shadow
		sm.setShadowDrawable(R.drawable.shadow);				// Shadows drawable
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);		// Margin on the right side of screen when menu is open
		sm.setFadeEnabled(true);								// Want menu to fade in and out
		sm.setFadeDegree(0.35f);								// Fading value
		// @formatter:on
	}

	/**
	 * To set correct {@link Fragment} to the <code>MenuFrame</code>.<br>
	 * If no <code>savedInstanceState</code> exists a new instance of {@link SlidingMenuFragment} will be placed in the <code>MenuFrame</code>.
	 * 
	 * @param savedInstanceState
	 *            If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently
	 *            supplied in {@link #onSaveInstanceState}. <b><i>Note: Otherwise it is null.</i></b>
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
	 * If no <code>savedInstanceState</code> exists a new instance of the default <code>Fragment</code> {@link SmsSettingsFragment} will be placed in
	 * the <code>ContentFrame</code>.
	 * 
	 * @param savedInstanceState
	 *            If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently
	 *            supplied in {@link #onSaveInstanceState}. <b><i>Note: Otherwise it is null.</i></b>
	 */
	private void setContentFragment(Bundle savedInstanceState) {
		// Activity is not being re-created, set default fragment to content frame
		if (savedInstanceState == null) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.contentFrame_fl, new SmsSettingsFragment());
			ft.commit();
		}
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
}