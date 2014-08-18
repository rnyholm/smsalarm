/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.fragment;

import android.view.View;

/**
 * Interface for all the UI fragments in application, holding method declaration all fragments must implement.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public interface SmsAlarmFragment {

	/**
	 * To find UI widgets and get their reference by ID contained in view of the <code>Fragment</code>.
	 * 
	 * @param view
	 *            <code>View</code> from witch UI widgets will be found.
	 */
	void findViews(View view);

	/**
	 * To fetch <code>Shared Preferences</code> used by member variables of this <code>Fragment</code>.
	 */
	void fetchSharedPrefs();

	/**
	 * To update all UI widgets contained in this <code>Fragments View</code>.
	 */
	void updateFragmentView();

	/**
	 * To set different listeners to the UI widgets contained within this <code>Fragment</code>.
	 */
	void setListeners();
}
