/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import ax.ha.it.smsalarm.Alarm;
import ax.ha.it.smsalarm.BuildConfig;
import ax.ha.it.smsalarm.R;

/**
 * Utility class for logging all {@link Alarm}'s within the database to a dedicated <code>*.html</code> file.<br>
 * <b><i>{@link AlarmLogger} is a singleton.</i></b>
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.0
 */
public class AlarmLogger {
	private static final String LOG_TAG = AlarmLogger.class.getSimpleName();

	// Singleton instance of this class
	private static AlarmLogger INSTANCE;

	// Filename of AlarmLog and directory
	private static final String HTML_ALARM_FILE = "alarms.html";
	private static final String DIRECTORY = "SmsAlarm";

	private static final String EOL = "\r\n";

	// String to store ExternalStorageState in
	private final String STATE = Environment.getExternalStorageState();

	// Variables to store external storage availability, both readable and writable
	private boolean mExternalStorageAvailable = false; // Practically unnecessary for this application, we just need to write
	private boolean mExternalStorageWriteable = false;

	/**
	 * Creates a new instance of {@link AlarmLogger}.
	 */
	private AlarmLogger() {
		// Just empty...
	}

	/**
	 * To get the <b><i>singleton</i></b> instance of {@link AlarmLogger}.
	 * 
	 * @return Instance of <code>AlarmLogger</code>.
	 */
	public static AlarmLogger getInstance() {
		// If instance of this object is null create a new one
		if (INSTANCE == null) {
			INSTANCE = new AlarmLogger();
		}

		return INSTANCE;
	}

	/**
	 * To write all Alarms from the database to a <code>*.html</code> file.
	 * 
	 * @param alarms
	 *            Alarms to be written to file.
	 * @param context
	 *            Context.
	 */
	public void logAlarms(List<Alarm> alarms, Context context) {
		// Check external storage status
		checkExternalStorageState();

		// Only if external storage is fully available can directory and file creating continue
		if (mExternalStorageAvailable && mExternalStorageWriteable) {

			// To calculate when table row should have background color
			int alt = 0;

			// Variable for file
			File file = null;

			// Get path to root
			String root = Environment.getExternalStorageDirectory().toString();

			// Create file objects, one representing the folder and one the actual HTML file These are used to check if they already exists or not
			File saDir = new File(root + "/" + DIRECTORY);
			File saFile = new File(root + "/" + DIRECTORY + "/" + HTML_ALARM_FILE);

			// Check if directory and HTML file already exists
			checkDirAndFile(saDir, saFile, file, HTML_ALARM_FILE);

			// Create HTML file object
			file = new File(saDir, HTML_ALARM_FILE);

			try {
				// Create BufferedWriter with FileWriter of file, used to write to file
				BufferedWriter bW = new BufferedWriter(new FileWriter(file, false));

				// Write the HTML header
				bW.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">" + EOL);
				bW.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">" + EOL);
				bW.write("\t<style>" + EOL);
				bW.write("\t\tbody {" + EOL);
				bW.write("\t\t\tfont-family:\"Trebuchet MS\", Arial, Helvetica, sans-serif;" + EOL);
				bW.write("\t\t}" + EOL);
				bW.write("\t\t#smsalarm {" + EOL);
				bW.write("\t\t\twidth:100%;" + EOL);
				bW.write("\t\t\tborder-collapse:collapse;" + EOL);
				bW.write("\t\t}" + EOL);
				bW.write("\t\t#smsalarm td, #smsalarm th {" + EOL);
				bW.write("\t\t\tfont-size:1em;" + EOL);
				bW.write("\t\t\tborder:1px solid #4d90fe;" + EOL);
				bW.write("\t\t\tpadding:3px 7px 2px 7px;" + EOL);
				bW.write("\t\t}" + EOL);
				bW.write("\t\t#smsalarm th {" + EOL);
				bW.write("\t\t\tfont-size:1.1em;" + EOL);
				bW.write("\t\t\ttext-align:left;" + EOL);
				bW.write("\t\t\tpadding-top:5px;" + EOL);
				bW.write("\t\t\tpadding-bottom:4px;" + EOL);
				bW.write("\t\t\tbackground-color:#72a7ff;" + EOL);
				bW.write("\t\t\tcolor:#ffffff;" + EOL);
				bW.write("\t\t}" + EOL);
				bW.write("\t\t#smsalarm tr.alt td {" + EOL);
				bW.write("\t\t\tcolor:#000000;" + EOL);
				bW.write("\t\t\tbackground-color:#C3DAFF;" + EOL);
				bW.write("\t\t}" + EOL);
				bW.write("\t\t.center {" + EOL);
				bW.write("\t\t\tmargin-left:auto;" + EOL);
				bW.write("\t\t\tmargin-right:auto;" + EOL);
				bW.write("\t\t\twidth:95%;" + EOL);
				bW.write("\t\t\ttext-align: center;" + EOL);
				bW.write("\t\t}" + EOL);
				bW.write("\t</style>" + EOL);
				bW.write(EOL);
				bW.write("\t<head>" + EOL);
				bW.write("\t\t<title>" + context.getString(R.string.APP_NAME) + " " + context.getString(R.string.DASH) + " " + context.getString(R.string.APP_DESCR) + "</title>" + EOL);
				bW.write("\t\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />" + EOL);
				bW.write("\t</head>" + EOL);
				bW.write("" + EOL);
				bW.write("\t<body>" + EOL);
				bW.write("\t\t<h1>" + context.getString(R.string.APP_NAME) + " " + context.getString(R.string.DASH) + " " + context.getString(R.string.HTML_WIDGET_RECEIVED_ALARMS) + "</h1>" + EOL);
				bW.write("" + EOL);
				bW.write("\t\t<table id=\"smsalarm\">" + EOL);
				bW.write("\t\t\t<tr>" + EOL);
				bW.write("\t\t\t\t<th>" + context.getString(R.string.HTML_WIDGET_RECEIVED) + "</th>" + EOL);
				bW.write("\t\t\t\t<th>" + context.getString(R.string.HTML_WIDGET_SENDER) + "</th>" + EOL);
				bW.write("\t\t\t\t<th>" + context.getString(R.string.HTML_WIDGET_TRIGGER_TEXT) + "</th>" + EOL);
				bW.write("\t\t\t\t<th>" + context.getString(R.string.HTML_WIDGET_LARM) + "</th>" + EOL);
				bW.write("\t\t\t\t<th>" + context.getString(R.string.HTML_WIDGET_ACK) + "</th>" + EOL);
				bW.write("\t\t\t</tr>" + EOL);

				// Populate table with alarms from list
				for (Alarm alarm : alarms) {
					// To set background color on each other table row
					if (alt % 2 != 0) {
						bW.write("\t\t\t<tr class=\"alt\">" + EOL);
					} else {
						bW.write("\t\t\t<tr>" + EOL);
					}

					// Write the rest HTML for the table row
					bW.write("\t\t\t\t<td>" + alarm.getReceived() + "</td>" + EOL);
					bW.write("\t\t\t\t<td>" + alarm.getSender() + "</td>" + EOL);
					bW.write("\t\t\t\t<td>" + alarm.getTriggerText() + "</td>" + EOL);
					bW.write("\t\t\t\t<td>" + alarm.getMessage() + "</td>" + EOL);
					bW.write("\t\t\t\t<td>" + alarm.getAcknowledged() + "</td>" + EOL);
					bW.write("\t\t\t</tr>" + EOL);

					// Increase to get background color on correct rows
					alt++;
				}

				// Write HTML footer
				bW.write("\t\t</table>" + EOL);
				bW.write("\t\t<p class=\"center\">" + context.getString(R.string.APP_NAME) + " " + context.getString(R.string.DASH) + " " + context.getString(R.string.APP_DESCR) + "<br />&#169;Robert Nyholm " + Integer.toString(Calendar.getInstance().get(Calendar.YEAR)) + "<br /><a href=\"http://www.smsalarm-app.net\" target=\"_blank\">www.smsalarm-app.net</a><br />" + String.format(context.getString(R.string.SPLASH_VERSION), context.getString(R.string.APP_VERSION)) + "</p>" + EOL);
				bW.write("\t</body>" + EOL);
				bW.write("</html>" + EOL);

				// Ensure that everything has been written to the file and close
				bW.flush();
				bW.close();
			} catch (Exception e) {
				if (BuildConfig.DEBUG) {
					Log.e(LOG_TAG + ":logAlarms()", "An Exception occurred during writing to file: \"" + HTML_ALARM_FILE + "\"", e);
				}
			}
		}
	}

	/**
	 * To get the complete path to the alarm log file.
	 * 
	 * @return Full path to alarm log file.
	 */
	public String getAlarmLogPath() {
		// Get path to root
		String root = Environment.getExternalStorageDirectory().toString();
		// Build up rest of the path and return it
		return root + "/" + DIRECTORY + "/" + HTML_ALARM_FILE;
	}

	/**
	 * To check if needed directory or file exist, if not theyr'e created.
	 * 
	 * @param saDir
	 *            Path and name of directory as File.
	 * @param saFile
	 *            Path and name of file as File.
	 * @param file
	 *            File object used to create a new file or directory.
	 * @param fileName
	 *            Name of file that will be created.
	 */
	private void checkDirAndFile(File saDir, File saFile, File file, String fileName) {
		// If SmsAlarm directory doesn't exist create it and create a new file
		if (!saDir.exists()) {
			saDir.mkdirs();
			createFile(saDir, saFile, file, fileName);
		} else if (!saFile.exists()) { // If log file don't exists, create it
			createFile(saDir, saFile, file, fileName);
		}
	}

	/**
	 * To create a new file on the file system.
	 * 
	 * @param saDir
	 *            Path and name of directory as File.
	 * @param saFile
	 *            Path and name of file as File.
	 * @param file
	 *            File object used to create a new file or directory.
	 * @param fileName
	 *            Name of file that will be created.
	 */
	private void createFile(File saDir, File saFile, File file, String fileName) {
		// Create log file object
		file = new File(saDir, fileName);

		try {
			file.createNewFile();
		} catch (IOException e) {
			if (BuildConfig.DEBUG) {
				Log.e(LOG_TAG + ":createFile()", "An error occurred while creating file: \"" + fileName + "\"", e);
			}
		}
	}

	/**
	 * To check state of the external storage media and decides whether it's available for reading and writing, only reading, only writing or not
	 * available at all.
	 */
	private void checkExternalStorageState() {
		// If statements deciding if we can read/write, both or nothing from the external storage
		if (Environment.MEDIA_MOUNTED.equals(STATE)) {
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(STATE)) {
			// We can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			// Something else is wrong. It may be one of many other states, but all we need to know is we can neither read nor write
			mExternalStorageAvailable = mExternalStorageWriteable = false;

			if (BuildConfig.DEBUG) {
				Log.e(LOG_TAG + ":checkExternalStorageState()", "Can't write log, cause: External storage is not available for writing or reading");
			}
		}
	}
}
