/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.util;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;

/**
 * Utility class containing different utilities and helper method.<br>
 * <b><i>Note. All of this functionality should be accessed in a static manner.</i></b>
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class Util {

	/**
	 * To check if given <code>String</code> exists in given <code>List</code> of <code>Strings</code>.<br>
	 * <b><i>Note. Method is not case sensitive.</i></b>
	 * 
	 * @param string
	 *            String to check if exists in list
	 * @param list
	 *            List to check if string exists in
	 * @return <code>true</code> if given String exists in given List else <code>false</code>.<br>
	 *         <code>false</code> is also returned if either given argument is <code>null</code>.
	 */
	@SuppressLint("DefaultLocale")
	public static boolean existsIn(String string, List<String> list) {
		if (string != null && list != null) {
			List<String> caseUpperList = new ArrayList<String>();

			for (String str : list) {
				caseUpperList.add(str.toUpperCase());
			}

			return caseUpperList.contains(string.toUpperCase());
		}

		return false;
	}
}
