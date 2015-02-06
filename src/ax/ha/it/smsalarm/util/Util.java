/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.util;

import java.io.File;
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

	// Different valid URI http's
	public static final String URI_HTTP = "http://";
	public static final String URI_HTTPS = "https://";

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
	public static boolean existsInIgnoreCases(String string, List<String> list) {
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
	 * To check if given <code>String</code> exists in given <code>List</code> of <code>Strings</code>.<br>
	 * <b><i>Note. Method is case sensitive.</i></b>
	 * 
	 * @param string
	 *            String to check if exists in list
	 * @param list
	 *            List to check if string exists in
	 * @return <code>true</code> if given String exists in given List else <code>false</code>.<br>
	 *         <code>false</code> is also returned if either given argument is <code>null</code>.
	 */
	@SuppressLint("DefaultLocale")
	public static boolean existsInConsiderCases(String string, List<String> list) {
		if (string != null && list != null) {
			return list.contains(string);
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
		} else if (!targetURI.startsWith(URI_HTTP) && !targetURI.startsWith(URI_HTTPS)) {
			if (SmsAlarm.DEBUG) {
				Log.e(LOG_TAG + ":browseURI()", "Failed to browse URI, given targetURI isn't a valid URI - doesn't start with either http:// or https://");
			}
		} else {
			Uri URI = Uri.parse(targetURI);
			Intent launchBrowser = new Intent(Intent.ACTION_VIEW, URI);
			context.startActivity(launchBrowser);
		}
	}

	/**
	 * To get just the file name of the given <code>filePath</code> <b><i>including</i></b> it's file extension.<br>
	 * Ex. The given path of: <b><i>/storage/sdcard0/download/ledarlarm.wma</b></i> <br>
	 * would result in returned file name: <b><i>ledarlarm.wma</b></i>
	 * <p>
	 * If a given filePath is <code>null</code> or <code>empty</code> an empty {@link String} is returned.
	 * 
	 * @param filePath
	 *            Path to file that file name should be resolved from.
	 * @return The stripped out file name from given path including it's file extension.
	 * @see #getBaseFileName(String)
	 */
	public static String getFileName(String filePath) {
		if (filePath != null && filePath.length() > 0) {
			return new File(filePath).getName();
		}

		return "";
	}

	/**
	 * To get just the file name of the given <code>filePath</code> <b><i>excluding</i></b> it's file extension.<br>
	 * Ex. The given path of: <b><i>/storage/sdcard0/download/ledarlarm.wma</b></i> <br>
	 * or <b><i>ledarlarm.wma</i></b><br>
	 * or <b><i>ledarlarm</i></b><br>
	 * would result in returned file name: <b><i>ledarlarm</b></i><br>
	 * <p>
	 * If a given filePath is <code>null</code> or <code>empty</code> an empty {@link String} is returned.
	 * 
	 * @param filePath
	 *            Path to file that file name should be resolved from.
	 * @return The stripped out file name from given path excluding it's file extension.
	 * @see #getFileName(String)
	 */
	public static String getBaseFileName(String filePath) {
		String fileName = getFileName(filePath);

		// First clean out just the name from the file extension
		int pos = fileName.lastIndexOf(".");

		// If there was any dot found
		if (pos != -1) {
			fileName = fileName.substring(0, pos);
		}

		return fileName;
	}

	/**
	 * To figure out if file on given <code>filePath</code> exists or not.
	 * <p>
	 * If a given filePath is <code>null</code> or <code>empty</code> then <code>false</code> is returned.
	 * 
	 * @param filePath
	 *            Path of file to check if exists or not.
	 * @return <code>true</code> if given file path exists, else <code>false</code>.
	 */
	public static boolean fileExists(String filePath) {
		if (filePath != null && filePath.length() > 0) {
			return new File(filePath).exists();
		}

		return false;
	}
}
