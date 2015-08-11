/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.activity.SmsAlarm;
import ax.ha.it.smsalarm.fragment.dialog.ConfirmInsertMockAlarmsDialog;
import ax.ha.it.smsalarm.fragment.dialog.ConfirmMockSharedPreferencesDialog;
import ax.ha.it.smsalarm.fragment.dialog.MockSmsDialog;
import ax.ha.it.smsalarm.slidingmenu.adapter.SlidingMenuAdapter;
import ax.ha.it.smsalarm.slidingmenu.model.SlidingMenuItem;
import ax.ha.it.smsalarm.util.DebugUtils;

import com.actionbarsherlock.app.SherlockListFragment;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

/**
 * {@link Fragment} containing all the views and user interface widgets for the {@link SlidingMenu} within the application. Also holds logic for
 * creating {@link SlidingMenuItem}'s and for switching <code>Fragment</code>'s in the applications content view.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 * @see SlidingMenuItem
 * @see SlidingMenuAdapter
 */
public class SlidingMenuFragment extends SherlockListFragment {
	private static final String LOG_TAG = SlidingMenuFragment.class.getSimpleName();

	// A forceful way of displaying the debug/testing menu items
	private boolean imposeDebugMenu = false;

	/**
	 * Creates a new instance of {@link SlidingMenuFragment}.
	 */
	public SlidingMenuFragment() {
		// Just empty...
	}

	/**
	 * Toggles the state of whether or not the sliding menu fragment being created should contain the Debug/Testing menu items or not. This method is
	 * of toggling behavior, in other words if the debug menu is imposed if this method is called it's set to not be imposed and vice versa. <br>
	 * <b><i>Note.</i></b>This method call makes changes that doesn't take the global {@link SmsAlarm#DEBUG} variable in consideration.
	 */
	public void toggleImposeDebugMenu() {
		if (imposeDebugMenu) {
			imposeDebugMenu = false;
			Toast.makeText(getActivity(), getString(R.string.DEBUG_TOAST_SAY_BYE_BYE_TO_THE_DEV_WORLD), Toast.LENGTH_LONG).show();
		} else {
			imposeDebugMenu = true;
			Toast.makeText(getActivity(), getString(R.string.DEBUG_TOAST_WELCOME_TO_THE_DEV_WORLD), Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * To get the correct {@link View} upon creation of a {@link SlidingMenuFragment} object.
	 */
	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.menu_list, null);
	}

	/**
	 * To complete the creation of a {@link SlidingMenuFragment} object by setting correct adapter({@link SlidingMenuAdapter}) and populate the
	 * <code>SlidingMenuFragment</code> with {@link SlidingMenuItem}'s.
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Create adapter, menu items and at last set correct adapter to this fragment
		SlidingMenuAdapter adapter = new SlidingMenuAdapter(getActivity());
		createMenuItems(adapter);
		setListAdapter(adapter);
	}

	/**
	 * To create the {@link SlidingMenuItem}'s that goes into the {@link SlidingMenu} in the application. The created <code>SlidingMenuItems</code>'s
	 * will be added to the given {@link SlidingMenuAdapter}.
	 * 
	 * @param adapter
	 *            SlidingMenuAdapter to which the created SlidingMenuItem's are added.
	 */
	private void createMenuItems(SlidingMenuAdapter adapter) {
		// Create menu items and add them to given adapter
		adapter.add(new SlidingMenuItem(getString(R.string.MENU_TITLE_SETTINGS)));
		adapter.add(new SlidingMenuItem(101, getString(R.string.MENU_TITLE_SMS), R.drawable.ic_menu_sms));
		adapter.add(new SlidingMenuItem(102, getString(R.string.MENU_TITLE_FREE_TEXT), R.drawable.ic_menu_word));
		adapter.add(new SlidingMenuItem(103, getString(R.string.MENU_TITLE_REGEX), R.drawable.ic_menu_regex));
		adapter.add(new SlidingMenuItem(104, getString(R.string.MENU_TITLE_SOUND), R.drawable.ic_menu_sound));
		adapter.add(new SlidingMenuItem(105, getString(R.string.MENU_TITLE_ACKNOWLEDGE), R.drawable.ic_menu_ack));
		adapter.add(new SlidingMenuItem(106, getString(R.string.MENU_TITLE_OTHER), R.drawable.ic_menu_other));
		adapter.add(new SlidingMenuItem(getString(R.string.MENU_TITLE_ALARM_LOG)));
		adapter.add(new SlidingMenuItem(201, getString(R.string.MENU_TITLE_ALL_ALARMS_LOG), R.drawable.ic_menu_alarm_log_all));
		adapter.add(new SlidingMenuItem(202, getString(R.string.MENU_TITLE_ALL_PRIMARY_ALARMS_LOG), R.drawable.ic_menu_alarm_log_primary));
		adapter.add(new SlidingMenuItem(203, getString(R.string.MENU_TITLE_ALL_SECONDARY_ALARMS_LOG), R.drawable.ic_menu_alarm_log_secondary));
		adapter.add(new SlidingMenuItem(getString(R.string.MENU_TITLE_ABOUT)));
		adapter.add(new SlidingMenuItem(301, getString(R.string.MENU_TITLE_APPRECIATION), R.drawable.ic_menu_appreciation));
		adapter.add(new SlidingMenuItem(302, getString(R.string.MENU_TITLE_OPEN_SOURCE), R.drawable.ic_menu_os));
		adapter.add(new SlidingMenuItem(303, getString(R.string.ABOUT), R.drawable.ic_menu_about));

		// Build up the testing/debug menu
		if (imposeDebugMenu || SmsAlarm.DEBUG) {
			adapter.add(new SlidingMenuItem(getString(R.string.DEBUG_MENU_TITLE_DEVELOP)));
			adapter.add(new SlidingMenuItem(401, getString(R.string.DEBUG_MENU_TITLE_DISPATCH_MOCK_SMS)));
			adapter.add(new SlidingMenuItem(402, getString(R.string.DEBUG_MENU_TITLE_NOTIFICATION)));
			adapter.add(new SlidingMenuItem(403, getString(R.string.DEBUG_MENU_TITLE_ACKNOWLEDGE_NOTIFICATION)));
			adapter.add(new SlidingMenuItem(404, getString(R.string.DEBUG_MENU_TITLE_INSERT_MOCK_ALARMS)));
			adapter.add(new SlidingMenuItem(405, getString(R.string.DEBUG_MENU_TITLE_MOCK_SHARED_PREFS)));
		}
	}

	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {
		Fragment fragment = null;
		SlidingMenuItem menuItem = (SlidingMenuItem) getListView().getItemAtPosition(position);

		// Resolve correct fragment by going through their unique ID's
		switch (menuItem.getId()) {
			case (101):
				fragment = new SmsSettingsFragment();
				break;
			case (102):
				fragment = new FreeTextSettingsFragment();
				break;
			case (103):
				fragment = new RegexSettingsFragment();
				break;
			case (104):
				fragment = new SoundSettingsFragment();
				break;
			case (105):
				fragment = new AcknowledgeSettingsFragment();
				break;
			case (106):
				fragment = new OtherSettingsFragment();
				break;
			case (201):
				fragment = new AlarmLogFragment();
				break;
			case (202):
				fragment = new PrimaryAlarmLogFragment();
				break;
			case (203):
				fragment = new SecondaryAlarmLogFragment();
				break;
			case (301):
				fragment = new AppreciationFragment();
				break;
			case (302):
				fragment = new OpenSourceFragment();
				break;
			case (303):
				fragment = new AboutFragment();
				break;
			case (401):
				MockSmsDialog mockSmsDialog = new MockSmsDialog();
				mockSmsDialog.setTargetFragment(SlidingMenuFragment.this, MockSmsDialog.MOCK_SMS_DIALOG_REQUEST_CODE);
				mockSmsDialog.show(getFragmentManager(), MockSmsDialog.MOCK_SMS_DIALOG_TAG);
				break;
			case (402):
				DebugUtils.dispatchNotification(getActivity());
				break;
			case (403):
				DebugUtils.dispatchAcknowledgeNotification(getActivity());
				break;
			case (404):
				ConfirmInsertMockAlarmsDialog confirmInsertMockAlarmsDialog = new ConfirmInsertMockAlarmsDialog();
				confirmInsertMockAlarmsDialog.setTargetFragment(SlidingMenuFragment.this, ConfirmInsertMockAlarmsDialog.CONFIRM_INSERT_MOCK_ALARMS_REQUEST_CODE);
				confirmInsertMockAlarmsDialog.show(getFragmentManager(), ConfirmInsertMockAlarmsDialog.CONFIRM_INSERT_MOCK_ALARMS_TAG);
				break;
			case (405):
				ConfirmMockSharedPreferencesDialog confirmMockSharedPrefsDialog = new ConfirmMockSharedPreferencesDialog();
				confirmMockSharedPrefsDialog.setTargetFragment(SlidingMenuFragment.this, ConfirmMockSharedPreferencesDialog.CONFIRM_MOCK_SHARED_PREFERENCES_REQUEST_CODE);
				confirmMockSharedPrefsDialog.show(getFragmentManager(), ConfirmMockSharedPreferencesDialog.CONFIRM_MOCK_SHARED_PREFERENCES_TAG);
				break;
			default:
				Log.e(LOG_TAG + ":onListItemClick()", "Unable to resolve a Fragment for given menu item id: \"" + menuItem.getId() + "\", check if implementation exist for menu item");
		}

		// Switch fragment, if it was possible to resolve a fragment
		if (fragment != null) {
			switchFragment(fragment);
		}
	}

	/**
	 * To switch {@link Fragment} in applications content view, located in {@link SmsAlarm}.
	 * 
	 * @param fragment
	 *            Fragment to be placed "in front" by {@link SmsAlarm#switchContent(Fragment)}.
	 */
	private void switchFragment(Fragment fragment) {
		if (getActivity() instanceof SmsAlarm) {
			((SmsAlarm) getActivity()).switchContent(fragment);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
		// Only interested in OK results, don't care at all about the others
		if (resultCode == Activity.RESULT_OK) {
			// Only interested in certain request codes...
			switch (requestCode) {
				case (MockSmsDialog.MOCK_SMS_DIALOG_REQUEST_CODE):
					// Get sender and body, at last dispatch SMS
					String smsSender = data.getStringExtra(MockSmsDialog.SMS_SENDER);
					String smsBody = data.getStringExtra(MockSmsDialog.SMS_BODY);

					DebugUtils.dispatchMockSMS(getActivity(), smsSender, smsBody);
					break;
				case (ConfirmMockSharedPreferencesDialog.CONFIRM_MOCK_SHARED_PREFERENCES_REQUEST_CODE):
					// User obviously want to mock shared preferences, mock them
					DebugUtils.mockSharedPreferences(getActivity());
					Toast.makeText(getActivity(), getString(R.string.DEBUG_TOAST_SHARED_PREFERENCES_MOCKED), Toast.LENGTH_LONG).show();
					break;
				case (ConfirmInsertMockAlarmsDialog.CONFIRM_INSERT_MOCK_ALARMS_REQUEST_CODE):
					// User wants to insert mock alarms, insert them
					DebugUtils.insertMockAlarms(getActivity());
					Toast.makeText(getActivity(), getString(R.string.DEBUG_TOAST_MOCK_ALARMS_INSERTED), Toast.LENGTH_LONG).show();
					break;
				default:
					Log.e(LOG_TAG + ":onActivityResult()", "An unsupported result occurred, result code: \"" + resultCode + "\" and request code: \"" + requestCode + "\"");
			}
		}
	};
}