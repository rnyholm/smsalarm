/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;

/**
 * Class containing different convenience methods and 
 * other functionality.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.2
 * @since 2.2
 */
public class Utils {
	/**
	 * To check if given <code>String</code> exists in given <code>List</code> 
	 * of <code>Strings</code>. Method is not case sensitive.
	 * 
	 * @param string String to check if exists in list.
	 * @param list List to check if string exists in.
	 * 
	 * @return <code>true</code> if given String exists in given List else <code>false</code>.<br>
	 * 		   <code>false</code> is also returned if either given argument is <code>null</code>.
	 */
	@SuppressLint("DefaultLocale")
	public static boolean existsIn(String string, List<String> list) {
		if (string != null && list != null) {
			List<String> caseUpperList = new ArrayList<String>();
			
			for (String str: list) {
				caseUpperList.add(str.toUpperCase());
			}
			
			return caseUpperList.contains(string.toUpperCase());
		} else {
			return false;
		}
	}
}