/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.fragment;

import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.view.View;

/**
 * Interface for all the <b><i>user interface</i></b> {@link Fragment}'s in application.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public interface ApplicationFragment {
	/**
	 * To find user interface widgets and get their reference by ID contained in view of the {@link Fragment}.<br>
	 * If some other attributes or special handling has to be done to some user interface widgets it's also done within this method.
	 * 
	 * @param view
	 *            {@link View} from witch UI widgets will be found.
	 */
	void findViews(View view);

	/**
	 * To fetch {@link SharedPreferences} used by member variables of this {@link Fragment}.
	 */
	void fetchSharedPrefs();

	/**
	 * To update all user interface widgets contained in this {@link Fragment}'s {@link View}.
	 */
	void updateFragmentView();

	/**
	 * To set different listeners to the user interface widgets contained within this {@link Fragment}.
	 */
	void setListeners();
}
