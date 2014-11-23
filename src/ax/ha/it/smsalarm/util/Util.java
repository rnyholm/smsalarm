/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.util;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import ax.ha.it.smsalarm.activity.SmsAlarm;

/**
 * Utility class containing different utilities and helper method.<br>
 * <b><i>Note. All of this functionality should be accessed in a static manner.</i></b>
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class Util {
	private static final String LOG_TAG = Util.class.getSimpleName();

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

	/**
	 * To open start of an intent with action {@link Intent#ACTION_VIEW} and given <code>targetURI</code> parsed out as a {@link Uri}. By doing this
	 * the web browser is displayed and redirected to given URI. So in short this method opens the web browser with given URI.
	 * <p>
	 * Note. The given targetURI must not be:<br>
	 * - <b><i><code>null</code></i></b><br>
	 * - <b><i>Empty</i></b><br>
	 * <p>
	 * It must also begin with either <b><i>http://</i></b> or <b><i>https://</i></b>.
	 * 
	 * @param context
	 *            Context in which intent is started.
	 * @param targetURI
	 *            URI to open in web browser.
	 */
	public static void browseURI(Context context, String targetURI) {
		// Some sanity checks
		if (targetURI == null || targetURI.length() == 0) {
			if (SmsAlarm.DEBUG) {
				Log.e(LOG_TAG + ":browseURI()", "Failed to browse URI, given targetURI is missing");
			}
		} else if (!targetURI.startsWith("http://") && !targetURI.startsWith("https://")) {
			if (SmsAlarm.DEBUG) {
				Log.e(LOG_TAG + ":browseURI()", "Failed to browse URI, given targetURI isn't a valid URI - doesn't start with either http:// or https://");
			}
		} else {
			Uri URI = Uri.parse(targetURI);
			Intent launchBrowser = new Intent(Intent.ACTION_VIEW, URI);
			context.startActivity(launchBrowser);
		}
	}
}
