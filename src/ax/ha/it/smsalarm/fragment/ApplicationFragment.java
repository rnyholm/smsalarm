/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.fragment;

import android.view.View;
import ax.ha.it.smsalarm.handler.LogHandler;
import ax.ha.it.smsalarm.handler.LogHandler.LogPriorities;

/**
 * Interface for all the UI fragments in application, holding method declaration all fragments must implement.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public interface ApplicationFragment {

	/**
	 * To find UI widgets and get their reference by ID contained in view of the <code>Fragment</code>.<br>
	 * If some other attributes or special handling has to be done to some UI widgets it's also done within this method.
	 * 
	 * @param view
	 *            <code>View</code> from witch UI widgets will be found.
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 */
	void findViews(View view);

	/**
	 * To fetch <code>Shared Preferences</code> used by member variables of this <code>Fragment</code>.
	 * 
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 * @see LogHandler#logCatTxt(LogPriorities, String, String, Throwable)
	 */
	void fetchSharedPrefs();

	/**
	 * To update all UI widgets contained in this <code>Fragments View</code>.
	 * 
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 */
	void updateFragmentView();

	/**
	 * To set different listeners to the UI widgets contained within this <code>Fragment</code>.
	 */
	void setListeners();
}
