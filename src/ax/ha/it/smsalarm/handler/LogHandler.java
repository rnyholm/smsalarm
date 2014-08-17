/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.handler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import ax.ha.it.smsalarm.Alarm;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.R.string;

/**
 * This class is responsible for all logging. This means logging to LogCat, errlog.txt and
 * alarms.html.<br>
 * <b><i>LogHandler is a singleton.</i></b>
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.2.1
 * @since 2.0
 */
@SuppressLint("SimpleDateFormat")
public class LogHandler {
	/**
	 * Enumeration for different LogPriorities needed to decide which kind of logging we're suppose
	 * to do.
	 * 
	 * @author Robert Nyholm <robert.nyholm@aland.net>
	 * @version 2.1.4
	 * @since 2.1
	 * 
	 * @see <a href="http://developer.android.com/reference/android/util/Log.html">Log reference at
	 *      Android Developers</a>
	 */
	public enum LogPriorities {
		ASSERT, DEBUG, ERROR, INFO, VERBOSE, WARN;
	}

	// Singleton instance of this class
	private static LogHandler INSTANCE;

	// Log tag
	private final String LOG_TAG = getClass().getSimpleName();
	// Names for the txt and html file
	private final String TXT_LOG_FILE = "errorlog.txt";
	private final String HTML_ALARM_FILE = "alarms.html";
	// Name of directory for application
	private final String DIRECTORY = "SmsAlarm";

	// String representing a Windows newline CRLN
	private final String EOL = "\r\n";

	// String to store ExternalStorageState in
	private final String STATE = Environment.getExternalStorageState();

	// Date and SimpleDateFormatter, to get current time in a certain
	// format(LogCat like)
	private Date now = Calendar.getInstance().getTime();
	private final SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd HH:mm:ss.SSS");

	// Variables to store external storage availability, both readable and writeable
	private boolean mExternalStorageAvailable = false; // Practically unnecessary for this
														// application, we just need to write
	private boolean mExternalStorageWriteable = false;

	/**
	 * Private constructor, is private due to it's singleton pattern.
	 */
	private LogHandler() {
		// Log information
		logCat(LogPriorities.DEBUG, LOG_TAG + ":LogHandler()", "New instance of LogHandler created");
	}

	/**
	 * Method to get the singleton instance of this class.
	 * 
	 * @return Singleton instance of LogHandler
	 */
	public static LogHandler getInstance() {
		// If instance of this object is null create a new one
		if (INSTANCE == null) {
			INSTANCE = new LogHandler();
		}

		return INSTANCE;
	}

	/**
	 * Method to create a LogCat log message. If no valid priority value are given it will log a
	 * message with <b><i>ERROR</i></b> priority with the origin log message and current error.
	 * 
	 * @param priority
	 *            Logpriority as LogPriorities
	 * @param logTag
	 *            Log tag as string
	 * @param message
	 *            Log message
	 * 
	 * @see #logCat(LogPriorities, String, String, Throwable)
	 * @see #logTxt(LogPriorities, String, String)
	 * @see #logCatTxt(LogPriorities, String, String)
	 * @see #logCatTxt(LogPriorities, String, String, Throwable)
	 */
	public void logCat(LogPriorities priority, String logTag, String message) {
		// Switch through the different log priorities and log the correct
		// priority and message
		switch (priority) {
			case ASSERT:
				// HAS NO Log METHOD
				break;
			case DEBUG:
				Log.d(logTag, message);
				break;
			case ERROR:
				Log.e(logTag, message);
				break;
			case INFO:
				Log.i(logTag, message);
				break;
			case VERBOSE:
				Log.v(logTag, message);
				break;
			case WARN:
				Log.w(logTag, message);
				break;
			default:
				// Invalid log priority detected log another log message with this
				// error and the origin log message
				logCat(LogPriorities.ERROR, LOG_TAG + ":logCat()", "Log priority wasn't found, check that correct priority enumeration is used." + " Original logmessage came from: \"" + logTag + "\", with message: \"" + message + "\"");
		}
	}

	/**
	 * Method to create a LogCat log message with throwable object If no valid priority value are
	 * given it will log a message with <b><i>ERROR</i></b> priority with the origin log message and
	 * current error.
	 * 
	 * @param priority
	 *            Logriority as LogPriorities
	 * @param logTag
	 *            Log tag as string
	 * @param message
	 *            Log message
	 * @param thr
	 *            Throwable object
	 * 
	 * @see #logCat(LogPriorities, String, String)
	 * @see #logTxt(LogPriorities, String, String)
	 * @see #logCatTxt(LogPriorities, String, String)
	 * @see #logCatTxt(LogPriorities, String, String, Throwable)
	 */
	public void logCat(LogPriorities priority, String logTag, String message, Throwable thr) {
		// Switch through the different log priorities and log the correct
		// priority, message and throwable
		switch (priority) {
			case ASSERT:
				// HAS NO Log METHOD
				break;
			case DEBUG:
				Log.d(logTag, message, thr);
				break;
			case ERROR:
				Log.e(logTag, message, thr);
				break;
			case INFO:
				Log.i(logTag, message, thr);
				break;
			case VERBOSE:
				Log.v(logTag, message, thr);
				break;
			case WARN:
				Log.w(logTag, message, thr);
				break;
			default:
				// Invalid log priority detected log another log message with this error and the
				// origin log message
				logCat(LogPriorities.ERROR, LOG_TAG + ":logCat()", "Log priority wasn't found, check that correct priority enumeration is used." + " Original logmessage came from: \"" + logTag + "\", with message: \"" + message + "\" and throwable: \"" + thr + "\"");
		}
	}

	/**
	 * Method to store a log message to file. If no valid priority value are given it will log a
	 * message with <b><i>ERROR</i></b> priority with the origin log message and current error.<br>
	 * Throws different exceptions depending on error.
	 * 
	 * @param priority
	 *            Log priority as LogPriorities
	 * @param logTag
	 *            Log tag as string
	 * @param message
	 *            Log message
	 * 
	 * @see #logCat(LogPriorities, String, String)
	 * @see #logCat(LogPriorities, String, String, Throwable)
	 * @see #logCatTxt(LogPriorities, String, String)
	 * @see #logCatTxt(LogPriorities, String, String, Throwable)
	 * @see #checkDirAndFile(File, File, File, String)
	 * @see #checkExternalStorageState()
	 */
	public void logTxt(LogPriorities priority, String logTag, String message) {
		// Get current time
		now = Calendar.getInstance().getTime();

		// Check external storage status
		checkExternalStorageState();

		// Only if external storage is fully available can directory and file creating continue
		if (mExternalStorageAvailable == true && mExternalStorageWriteable == true) {

			// Variable for file
			File file = null;

			// Get path to root
			String root = Environment.getExternalStorageDirectory().toString();

			/*
			 * Create file objects, one representing the folder and one the actual log file These
			 * are used to check if they already exists or not
			 */
			File saDir = new File(root + "/" + DIRECTORY);
			File saFile = new File(root + "/" + DIRECTORY + "/" + TXT_LOG_FILE);

			// Check if directory and log file already exists
			checkDirAndFile(saDir, saFile, file, TXT_LOG_FILE);

			// Create log file object
			file = new File(saDir, TXT_LOG_FILE);

			try {
				// Create BufferedWriter with FileWriter of file, used to write to file
				BufferedWriter bW = new BufferedWriter(new FileWriter(file, true));

				// Switch through the different log priorities and log the correct priority and
				// message
				switch (priority) {
					case ASSERT:
						// HAS NO Log METHOD
						break;
					case DEBUG:
						bW.write("DEBUG\t" + formatter.format(now) + "\t" + logTag + " - " + message + EOL);
						break;
					case ERROR:
						bW.write("ERROR\t" + formatter.format(now) + "\t" + logTag + " - " + message + EOL);
						break;
					case INFO:
						bW.write("INFO\t" + formatter.format(now) + "\t" + logTag + " - " + message + EOL);
						break;
					case VERBOSE:
						bW.write("VERBOSE\t" + formatter.format(now) + "\t" + logTag + " - " + message + EOL);
						break;
					case WARN:
						bW.write("WARN\t" + formatter.format(now) + "\t" + logTag + " - " + message + EOL);
						break;
					default:
						// Invalid log priority detected log another log message
						// with this error and the origin log message
						logTxt(LogPriorities.ERROR, LOG_TAG + ":logTxt()", "Can't write to file because log priority wasn't found, check that correct priority enumeration is used");
				}

				// Ensure that everything has been written to the file and close
				bW.flush();
				bW.close();
			} catch (Exception e) {
				logCat(LogPriorities.ERROR, LOG_TAG + ":logTxt()", "An Exception occurred during writing to file: \"" + TXT_LOG_FILE + "\"", e);
			}
		} else {
			// External storage is not available, log it
			logCat(LogPriorities.WARN, LOG_TAG + ":logTxt()", "Cannot log to file because external storage isn't available, check earlier logs fore more details");
		}
	}

	/**
	 * Method to store a log message and throwable to file. If no valid priority value are given it
	 * will log a message with <b><i>ERROR</i></b> priority with the origin log message and current
	 * error.<br>
	 * Throws different exceptions depending on error.
	 * 
	 * @param priority
	 *            Logpriority as LogPriorities
	 * @param logTag
	 *            Log tag as string
	 * @param message
	 *            Log message
	 * @param thr
	 *            Throwable object
	 * 
	 * @see #logCat(LogPriorities, String, String)
	 * @see #logCat(LogPriorities, String, String, Throwable)
	 * @see #logCatTxt(LogPriorities, String, String)
	 * @see #logCatTxt(LogPriorities, String, String, Throwable)
	 * @see #checkDirAndFile(File, File, File, STring)
	 * @see #checkExternalStorageState()
	 */
	public void logTxt(LogPriorities priority, String logTag, String message, Throwable thr) {
		// Get current time
		now = Calendar.getInstance().getTime();

		// Check external storage status
		checkExternalStorageState();

		// Only if external storage is fully available can directory and file
		// creating continue
		if (mExternalStorageAvailable == true && mExternalStorageWriteable == true) {

			// Variable for file
			File file = null;

			// Get path to root
			String root = Environment.getExternalStorageDirectory().toString();

			/*
			 * Create file objects, one representing the folder and one the actual log file These
			 * are used to check if they already exists or not
			 */
			File saDir = new File(root + "/" + DIRECTORY);
			File saFile = new File(root + "/" + DIRECTORY + "/" + TXT_LOG_FILE);

			// Check if directory and log file already exists
			checkDirAndFile(saDir, saFile, file, TXT_LOG_FILE);

			// Create log file object
			file = new File(saDir, TXT_LOG_FILE);

			try {
				// Create BufferedWriter with FileWriter of file, used to write
				// to file
				BufferedWriter bW = new BufferedWriter(new FileWriter(file, true));

				// Switch through the different log priorities and log the
				// correct priority and message
				switch (priority) {
					case ASSERT:
						// HAS NO Log METHOD
						break;
					case DEBUG:
						bW.write("DEBUG\t" + formatter.format(now) + "\t" + logTag + " - " + message + " - " + thr + EOL);
						break;
					case ERROR:
						bW.write("ERROR\t" + formatter.format(now) + "\t" + logTag + " - " + message + " - " + thr + EOL);
						break;
					case INFO:
						bW.write("INFO\t" + formatter.format(now) + "\t" + logTag + " - " + message + " - " + thr + EOL);
						break;
					case VERBOSE:
						bW.write("VERBOSE\t" + formatter.format(now) + "\t" + logTag + " - " + message + " - " + thr + EOL);
						break;
					case WARN:
						bW.write("WARN\t" + formatter.format(now) + "\t" + logTag + " - " + message + " - " + thr + EOL);
						break;
					default:
						// Invalid log priority detected log another log message
						// with this error and the origin log message
						logTxt(LogPriorities.ERROR, LOG_TAG + ":logTxt()", "Can't write to file because log priority wasn't found, check that correct priority enumeration is used");
				}

				// Ensure that everything has been written to the file and close
				bW.flush();
				bW.close();
			} catch (Exception e) {
				logCat(LogPriorities.ERROR, LOG_TAG + ":logTxt()", "An Exception occurred during writing to file: \"" + TXT_LOG_FILE + "\"", e);
			}
		} else {
			// External storage is not available, log it
			logCat(LogPriorities.WARN, LOG_TAG + ":logTxt()", "Cannot log to file because external storage isn't available, check earlier logs fore more details");
		}
	}

	/**
	 * To return complete path to the alarm log file. Throws exception if alarm log file doesn't
	 * exist or if external storage isn't available for reading.
	 * 
	 * @return Full path to alarm log file as string
	 * 
	 * @throws IOException
	 *             If alarm log file doesn't exist or if external storage isn't available for
	 *             reading
	 */
	public String getAlarmLogPath() throws IOException {
		// Check external storage status
		checkExternalStorageState();

		// Check if external storage is available for reading
		if (mExternalStorageAvailable) {
			// Get path to root
			String root = Environment.getExternalStorageDirectory().toString();

			// New file object from root, sms alarm directory and alarm log file name
			File file = new File(root + "/" + DIRECTORY + "/" + HTML_ALARM_FILE);

			// Check if alarm log file exists
			if (file.exists()) {
				// Return path of alarm log file
				return file.getPath();
			} else {
				// Alarm log file doesn't exist
				logCat(LogPriorities.WARN, LOG_TAG + ":getAlarmLogPath()", "Cannot return path to alarm log file because it doesn't exist");

				// Throw new exception
				throw new IOException("Alarm log file:\"" + file.getPath() + "\" doesn't exist");
			}
		} else {
			// External storage is not available, log it
			logCat(LogPriorities.WARN, LOG_TAG + ":getAlarmLogPath()", "Cannot log to file because external storage isn't available, check earlier logs fore more details");

			// Throw new exception
			throw new IOException("External storage isn't available for reading");
		}
	}

	/**
	 * Method to write alarm log file as an <code>.html</code> file.
	 * 
	 * @param alarms
	 *            List of <Alarm> objects to be written to file
	 * @param context
	 *            Context for retrieving <code>Android resources</code>
	 * 
	 * @see #checkExternalStorageState()
	 * @see #checkDirAndFile(File, File, File, String)
	 * @see ax.ha.it.smsalarm.Alarm ax.ha.it.smsalarm.Alarm
	 * @see #logCatTxt(LogPriorities, String, String, Throwable)
	 */
	public void logAlarm(List<Alarm> alarms, Context context) {
		// Check external storage status
		checkExternalStorageState();

		// Only if external storage is fully available can directory and file
		// creating continue
		if (mExternalStorageAvailable && mExternalStorageWriteable) {

			// To calculate when table row should have background color
			int alt = 0;

			// Variable for file
			File file = null;

			// Get path to root
			String root = Environment.getExternalStorageDirectory().toString();

			/*
			 * Create file objects, one representing the folder and one the actual html file These
			 * are used to check if they already exists or not
			 */
			File saDir = new File(root + "/" + DIRECTORY);
			File saFile = new File(root + "/" + DIRECTORY + "/" + HTML_ALARM_FILE);

			// Check if directory and html file already exists
			checkDirAndFile(saDir, saFile, file, HTML_ALARM_FILE);

			// Create html file object
			file = new File(saDir, HTML_ALARM_FILE);

			try {
				// Create BufferedWriter with FileWriter of file, used to write
				// to file
				BufferedWriter bW = new BufferedWriter(new FileWriter(file, false));

				// Write the html header
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

					// Write the rest html for the table row
					bW.write("\t\t\t\t<td>" + alarm.getReceived() + "</td>" + EOL);
					bW.write("\t\t\t\t<td>" + alarm.getSender() + "</td>" + EOL);
					bW.write("\t\t\t\t<td>" + alarm.getTriggerText() + "</td>" + EOL);
					bW.write("\t\t\t\t<td>" + alarm.getMessage() + "</td>" + EOL);
					bW.write("\t\t\t\t<td>" + alarm.getAcknowledged() + "</td>" + EOL);
					bW.write("\t\t\t</tr>" + EOL);

					// Increase to get background color on correct rows
					alt++;
				}

				// Write html footer
				bW.write("\t\t</table>" + EOL);
				bW.write("\t\t<p class=\"center\">" + context.getString(R.string.APP_NAME) + " " + context.getString(R.string.DASH) + " " + context.getString(R.string.APP_DESCR) + "<br />&#169;Robert Nyholm " + Integer.toString(Calendar.getInstance().get(Calendar.YEAR)) + "<br /><a href=\"http://www.smsalarm-app.net\" target=\"_blank\">www.smsalarm-app.net</a><br />" + String.format(context.getString(R.string.SPLASH_VERSION), context.getString(R.string.APP_VERSION)) + "</p>" + EOL);
				bW.write("\t</body>" + EOL);
				bW.write("</html>" + EOL);

				// Ensure that everything has been written to the file and close
				bW.flush();
				bW.close();
			} catch (Exception e) {
				logCat(LogPriorities.ERROR, LOG_TAG + ":logAlarm()", "An Exception occurred during writing to file: \"" + HTML_ALARM_FILE + "\"", e);
			}
		} else {
			// External storage is not available, log it
			logCat(LogPriorities.WARN, LOG_TAG + ":logAlarm()", "Cannot log to file because external storage isn't available, check earlier logs fore more details");
		}
	}

	/**
	 * Method to check if needed directory or file for application exist, if not theyr'e created.
	 * 
	 * @param saDir
	 *            Path and name of directory as File
	 * @param saFile
	 *            Path and name of file as File
	 * @param file
	 *            File object used to create a new file or directory
	 * @param fileName
	 *            Name of file that will be created
	 */
	private void checkDirAndFile(File saDir, File saFile, File file, String fileName) {
		// If SmsAlarm directory doesn't exist create it and create file
		if (!saDir.exists()) {
			// Make dir
			saDir.mkdirs();

			// Create the new file
			createNewFile(saDir, saFile, file, fileName);
		}

		// If log file don't exists, create it
		else if (!saFile.exists()) {
			// Create the new file
			createNewFile(saDir, saFile, file, fileName);
		}
	}

	/**
	 * To create a new file on the filesystem.
	 * 
	 * @param saDir
	 *            Path and name of directory as File
	 * @param saFile
	 *            Path and name of file as File
	 * @param file
	 *            File object used to create a new file or directory
	 * @param fileName
	 *            Name of file that will be created
	 * 
	 * @see #logCatTxt(LogPriorities, String, String, Throwable)
	 */
	private void createNewFile(File saDir, File saFile, File file, String fileName) {
		// Create log file object
		file = new File(saDir, fileName);

		try {
			// Try create new file
			file.createNewFile();
		} catch (IOException e) {
			// Log exception
			logCat(LogPriorities.ERROR, LOG_TAG + ":createNewFile()", "An IOException occurred during creation of file: \"" + fileName + "\"", e);
		}
	}

	/**
	 * Method to create a LogCat message, it also stores the same information to file.
	 * 
	 * @param priority
	 *            Logpriority as LogPriorities
	 * @param logTag
	 *            Log tag as string
	 * @param message
	 *            Log message
	 * 
	 * @see #logCat(LogPriorities, String, String)
	 * @see #logCat(LogPriorities, String, String, Throwable)
	 * @see #logCatTxt(LogPriorities, String, String, Throwable)
	 * @see #checkExternalStorageState()
	 */
	public void logCatTxt(LogPriorities priority, String logTag, String message) {
		logCat(priority, logTag, message);
		logTxt(priority, logTag, message);
	}

	/**
	 * Method to create a LogCat message with throwable, it also stores the same information to
	 * file.
	 * 
	 * @param priority
	 *            Logpriority as LogPriorities
	 * @param logTag
	 *            Log tag as string
	 * @param message
	 *            Log message
	 * @param thr
	 *            Throwable object
	 * 
	 * @see #logCat(LogPriorities, String, String)
	 * @see #logCat(LogPriorities, String, String, Throwable)
	 * @see #logCatTxt(LogPriorities, String, String)
	 * @see #checkExternalStorageState()
	 */
	public void logCatTxt(LogPriorities priority, String logTag, String message, Throwable thr) {
		logCat(priority, logTag, message, thr);
		logTxt(priority, logTag, message, thr);
	}

	/**
	 * This method checks the state of external storage media and decides whether its available for
	 * reading and writing, only reading, only writing or not available at all.
	 * 
	 * @see #logCatTxt(LogPriorities, String, String)
	 * @see #logCatTxt(LogPriorities, String, String, Throwable)
	 * @see #logTxt(LogPriorities, String, String)
	 */
	private void checkExternalStorageState() {
		// If statements deciding if we can read/write, both or nothing from the
		// external storage
		if (Environment.MEDIA_MOUNTED.equals(STATE)) {
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(STATE)) {
			// We can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
			logCat(LogPriorities.WARN, LOG_TAG + ":checkExternalStorageState()", "Cannot write log, cause: External storage is not available for writing");
		} else {
			/*
			 * Something else is wrong. It may be one of many other states, but all we need to know
			 * is we can neither read nor write
			 */
			mExternalStorageAvailable = mExternalStorageWriteable = false;
			logCat(LogPriorities.ERROR, LOG_TAG + ":checkExternalStorageState()", "Cannot write log, cause: External storage is not available for writing or reading");
		}
	}
}
