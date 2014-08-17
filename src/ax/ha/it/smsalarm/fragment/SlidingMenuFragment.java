/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.R.drawable;
import ax.ha.it.smsalarm.R.layout;
import ax.ha.it.smsalarm.handler.LogHandler;
import ax.ha.it.smsalarm.handler.LogHandler.LogPriorities;
import ax.ha.it.smsalarm.slidingmenu.adapter.SlidingMenuAdapter;
import ax.ha.it.smsalarm.slidingmenu.model.SlidingMenuItem;

import com.actionbarsherlock.app.SherlockListFragment;

/**
 * Class representing a <code>SherlockListFragment</code>, or simply said, a fragmet. This fragment
 * is used in this applications <code>SlidingMenu</code>.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 * @see SlidingMenuItem
 * @see SlidingMenuAdapter
 */
public class SlidingMenuFragment extends SherlockListFragment {
	// Logging information
	private String LOG_TAG = getClass().getSimpleName();
	private LogHandler logger = LogHandler.getInstance();

	/**
	 * To get the correct <code>View</code> upon creation of this fragment. The layout is inflated
	 * using given <code>LayoutInflater</code>.
	 * 
	 * @param inflater
	 *            Layout inflater to be used when this layout is to be inflated.
	 * @param container
	 * @param savedInstanceState
	 */
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
	 * To build up the <code>SlidingMenuItem</code>'s in that goes into this <code>Fragment</code>.
	 * The menu items will be added to the given adapter for correct data representation.
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
		Fragment newContent = null;

		SlidingMenuItem menuItem = (SlidingMenuItem) getListView().getItemAtPosition(position);

		switch (menuItem.getId()) {
			case (101):

				break;
			case (102):

				break;
			case (103):

				break;
			case (104):

				break;
			case (105):

				break;
			case (201):

				break;
			case (202):

				break;
			default:
				break;
		}

		if (newContent != null) {
//			switchFragment(newContent);
		}

//		switch (position) {
//			case 0:
//				newContent = new ColorFragment(R.color.red);
//				break;
//			case 1:
//				newContent = new ColorFragment(R.color.green);
//				break;
//			case 2:
//				newContent = new ColorFragment(R.color.blue);
//				break;
//			case 3:
//				newContent = new ColorFragment(android.R.color.white);
//				break;
//			case 4:
//				newContent = new ColorFragment(android.R.color.black);
//				break;
//		}
//		if (newContent != null)
//			switchFragment(newContent);
	}

	// the meat of switching the above fragment
//	private void switchFragment(Fragment fragment) {
//		if (getActivity() == null)
//			return;
//
//		if (getActivity() instanceof FragmentChangeActivity) {
//			FragmentChangeActivity fca = (FragmentChangeActivity) getActivity();
//			fca.switchContent(fragment);
//		} else if (getActivity() instanceof ResponsiveUIActivity) {
//			ResponsiveUIActivity ra = (ResponsiveUIActivity) getActivity();
//			ra.switchContent(fragment);
//		}
//	}
}