/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.activity.SmsAlarm;
import ax.ha.it.smsalarm.handler.LogHandler;
import ax.ha.it.smsalarm.handler.LogHandler.LogPriorities;
import ax.ha.it.smsalarm.slidingmenu.adapter.SlidingMenuAdapter;
import ax.ha.it.smsalarm.slidingmenu.model.SlidingMenuItem;

import com.actionbarsherlock.app.SherlockListFragment;

/**
 * Class representing a <code>SherlockListFragment</code>, or simply said, a fragment. This fragment is used in this applications
 * <code>SlidingMenu</code>.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 * @see SlidingMenuItem
 * @see SlidingMenuAdapter
 */
public class SlidingMenuFragment extends SherlockListFragment {
	// For logging
	private String LOG_TAG = getClass().getSimpleName();
	private LogHandler logger = LogHandler.getInstance();

	/**
	 * To get the correct <code>View</code> upon creation of this fragment. The layout is inflated using given <code>LayoutInflater</code>.
	 * 
	 * @param inflater
	 *            Layout inflater to be used when this layout is to be inflated.
	 * @param container
	 * @param savedInstanceState
	 */
	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreateView()", "Layout for this fragment is about to be inflated and returned");
		return inflater.inflate(R.layout.list, null);
	}

	/**
	 * To populate this fragment with data and set correct adapter to it.
	 * 
	 * @param savedInstanceState
	 * @see SlidingMenuFragment#createMenuItems(SlidingMenuAdapter)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onActivityCreated()", "Activity has been created, creating menu items and setting the correct adapter to this fragment");

		// Create adapter, menu items and at last set correct adapter to this fragment
		SlidingMenuAdapter adapter = new SlidingMenuAdapter(getActivity());
		createMenuItems(adapter);
		setListAdapter(adapter);
	}

	/**
	 * To build up the <code>SlidingMenuItem</code>'s in that goes into this <code>Fragment</code>. The menu items will be added to the given adapter
	 * for correct data representation.
	 * 
	 * @param adapter
	 *            Adapter to which the created menu item's are added.
	 */
	private void createMenuItems(SlidingMenuAdapter adapter) {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createMenuItems()", "Creating menu items and adding them to adapter");

		// Create menu items and add them to given adapter
		adapter.add(new SlidingMenuItem("Inställningar*"));
		adapter.add(new SlidingMenuItem(101, "Sms*", R.drawable.ic_menu_sms));
		adapter.add(new SlidingMenuItem(102, "Ord*", R.drawable.ic_menu_word));
		adapter.add(new SlidingMenuItem(103, "Ljud*", R.drawable.ic_menu_sound));
		adapter.add(new SlidingMenuItem(104, "Kvittering*", R.drawable.ic_menu_ack));
		adapter.add(new SlidingMenuItem(105, "Övrigt*", R.drawable.ic_menu_other));
		adapter.add(new SlidingMenuItem("Om*"));
		adapter.add(new SlidingMenuItem(201, "Öppen källkod*", R.drawable.ic_menu_os));
		adapter.add(new SlidingMenuItem(202, "Om Sms Alarm*", R.drawable.ic_menu_about));
	}

	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {
		Fragment fragment = null;
		SlidingMenuItem menuItem = (SlidingMenuItem) getListView().getItemAtPosition(position);

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onListItemClick()", menuItem.toString() + " clicked");

		// Resolve correct fragment by going through their unique ID's
		switch (menuItem.getId()) {
			case (101):
				fragment = new SmsSettingsFragment(getActivity());
				break;
			case (102):
				fragment = new FreeTextSettingsFragment(getActivity());
				break;
			case (103):
				fragment = new SoundSettingsFragment(getActivity());
				break;
			case (104):
				fragment = new AcknowledgeSettingsFragment(getActivity());
				break;
			case (105):
				fragment = new OtherSettingsFragment(getActivity());
				break;
			case (201):
				break;
			case (202):
				break;
			default:
				logger.logCatTxt(LogPriorities.WARN, LOG_TAG + ":onListItemClick()", "Unable to resolve a Fragment for given menu item id: \"" + Integer.toString(menuItem.getId()) + "\", check if implementation exist for menu item");
				break;
		}

		// Switch fragment, if it was possible to resolve a fragment
		if (fragment != null) {
			switchFragment(fragment);
		}
	}

	/**
	 * To switch <code>Fragment</code> of <code>Activity SmsAlarm</code>.<br>
	 * 
	 * @param fragment
	 *            Fragment to be placed "in front" by {@link SmsAlarm#switchContent(Fragment)}.
	 * @see SmsAlarm#switchContent(Fragment)
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 * @see LogHandler#logCatTxt(LogPriorities, String, String)
	 */
	private void switchFragment(Fragment fragment) {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":switchFragment()", "Fragment is about to be switched");

		if (getActivity() instanceof SmsAlarm) {
			((SmsAlarm) getActivity()).switchContent(fragment);
		} else {
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":switchFragment()", "Unable to switch fragment. Reason: \"Activity of wrong instance\". Accepted instance is: \"SmsAlarm.class\", found instance is:\"" + getActivity().getLocalClassName() + "\"");
		}
	}
}