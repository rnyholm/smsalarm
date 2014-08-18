/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import ax.ha.it.smsalarm.handler.LogHandler;
import ax.ha.it.smsalarm.handler.LogHandler.LogPriorities;

/**
 * Utility class containing different utilities and helper method.<br>
 * All of this functionality should be accessed in a static manner.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class Util {
	// Log tag string
	private final static String LOG_TAG = "Util";

	/**
	 * To check if given <code>String</code> exists in given <code>List</code> of <code>Strings</code>. Method is not case sensitive.
	 * 
	 * @param string
	 *            String to check if exists in list.
	 * @param list
	 *            List to check if string exists in.
	 * @return <code>true</code> if given String exists in given List else <code>false</code>.<br>
	 *         <code>false</code> is also returned if either given argument is <code>null</code>.
	 */
	@SuppressLint("DefaultLocale")
	public static boolean existsIn(String string, List<String> list) {
		// Get LogHandler instance
		LogHandler logger = LogHandler.getInstance();

		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":existsIn()", "String=\"" + string + "\" is about to be checked if exists in list=\"" + list + "\"");

		if (string != null && list != null) {
			List<String> caseUpperList = new ArrayList<String>();

			for (String str : list) {
				caseUpperList.add(str.toUpperCase());
			}

			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":existsIn()", "Returning " + caseUpperList.contains(string.toUpperCase()));
			return caseUpperList.contains(string.toUpperCase());
		} else {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":existsIn()", "Given string and/or list is null, returning FALSE");
			return false;
		}
	}
}
