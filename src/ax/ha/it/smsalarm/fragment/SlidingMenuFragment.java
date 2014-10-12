/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.activity.SmsAlarm;
import ax.ha.it.smsalarm.service.NotificationService;
import ax.ha.it.smsalarm.slidingmenu.adapter.SlidingMenuAdapter;
import ax.ha.it.smsalarm.slidingmenu.model.SlidingMenuItem;

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

	/**
	 * Creates a new instance of {@link SlidingMenuFragment}.
	 */
	public SlidingMenuFragment() {
		// Just empty...
	}

	/**
	 * To get the correct {@link View} upon creation of a {@link SlidingMenuFragment} object.
	 */
	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list, null);
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
		adapter.add(new SlidingMenuItem(103, getString(R.string.MENU_TITLE_SOUND), R.drawable.ic_menu_sound));
		adapter.add(new SlidingMenuItem(104, getString(R.string.MENU_TITLE_ACKNOWLEDGE), R.drawable.ic_menu_ack));
		adapter.add(new SlidingMenuItem(105, getString(R.string.MENU_TITLE_OTHER), R.drawable.ic_menu_other));
		adapter.add(new SlidingMenuItem(getString(R.string.MENU_TITLE_ABOUT)));
		adapter.add(new SlidingMenuItem(201, getString(R.string.MENU_TITLE_OPEN_SOURCE), R.drawable.ic_menu_os));
		adapter.add(new SlidingMenuItem(202, getString(R.string.ABOUT), R.drawable.ic_menu_about));

		// Build up the testing/debug menu
		if (SmsAlarm.DEBUG) {
			adapter.add(new SlidingMenuItem(getString(R.string.DEBUG_MENU_TITLE_DEVELOP)));
			adapter.add(new SlidingMenuItem(301, getString(R.string.DEBUG_MENU_TITLE_NOTIFICATION)));
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
				fragment = new SoundSettingsFragment();
				break;
			case (104):
				fragment = new AcknowledgeSettingsFragment();
				break;
			case (105):
				fragment = new OtherSettingsFragment();
				break;
			case (201):
				break;
			case (202):
				fragment = new AboutFragment();
				break;
			case (301):
				Intent notIntent = new Intent(getActivity(), NotificationService.class);
				getActivity().startService(notIntent);
				break;
			default:
				if (SmsAlarm.DEBUG) {
					Log.e(LOG_TAG + ":onListItemClick()", "Unable to resolve a Fragment for given menu item id: \"" + menuItem.getId() + "\", check if implementation exist for menu item");
				}
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
}