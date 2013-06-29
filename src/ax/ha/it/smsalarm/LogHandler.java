/*
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */

package ax.ha.it.smsalarm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

/**
 * This class is responsible for all logging. This means logging to LogCat,
 * log.txt and alarms.html.<br>
 * <b><i>LogHandler is a singleton.</i></b>
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.1
 * @since 2.0
 * @date 2013-06-30
 */
public class LogHandler {
	// Singleton instance of this class
	private static LogHandler INSTANCE;

	// Log tag
	private final String LOG_TAG = "LogHandler";
	// Names for the txt and html file
	private final String TXT_LOG_FILE = "log.txt";
	// Name of directory for application
	private final String DIRECTORY = "SmsAlarm";

	// String representing a Windows newline CRLN
	private final String EOL = "\r\n";

	// String to store ExternalStorageState in
	private final String STATE = Environment.getExternalStorageState();

	// Constants representing log priority
	private final int ASSERT = 7;
	private final int DEBUG = 3;
	private final int ERROR = 6;
	private final int INFO = 4;
	private final int VERBOSE = 2;
	private final int WARN = 5;

	// Date and SimpleDateFormatter, to get current time in a certain format(LogCat like)
	private Date today = Calendar.getInstance().getTime();
	private SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd HH:mm:ss.SSS");

	// Variables to store external storage availability, both readable and writeable
	private boolean mExternalStorageAvailable = false; // Practically unnecessary for this application, we just need to write
	private boolean mExternalStorageWriteable = false;

	/**
	 * Private constructor, is private due to it's singleton pattern.
	 */
	private LogHandler() {
		// Log information
		this.logCatTxt(this.INFO, this.LOG_TAG + ":LogHandler()", "New instance of LogHandler created");
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
	 * Getter for the log priority constant ASSERT
	 * 
	 * @return The ASSERT constant, value 7
	 */
	public int getASSERT() {
		return ASSERT;
	}

	/**
	 * Getter for the log priority constant DEBUG
	 * 
	 * @return The DEBUG constant, value 3
	 */
	public int getDEBUG() {
		return DEBUG;
	}

	/**
	 * Getter for the log priority constant ERROR
	 * 
	 * @return The ERROR constant, value 6
	 */
	public int getERROR() {
		return ERROR;
	}

	/**
	 * Getter for the log priority constant INFO
	 * 
	 * @return The INFO constant, value 4
	 */
	public int getINFO() {
		return INFO;
	}

	/**
	 * Getter for the log priority constant VERBOSE
	 * 
	 * @return The VERBOSE constant, value 2
	 */
	public int getVERBOSE() {
		return VERBOSE;
	}

	/**
	 * Getter for the log priority constant WARN
	 * 
	 * @return The WARN constant, value 5
	 */
	public int getWARN() {
		return WARN;
	}

	/**
	 * Method to create a LogCat log message. If no valid priority value are
	 * given it will log a message with <b><i>ERROR</i></b> priority with the
	 * origin log message and current error.
	 * 
	 * @param priority
	 *            Priority as integer see {@link #http
	 *            ://www.http://developer.android
	 *            .com/reference/android/util/Log.html}
	 * @param logTag
	 *            Log tag as string
	 * @param message
	 *            Log message
	 * 
	 * @see #logCat(int, String, String, Throwable)
	 * @see #logTxt(int, String, String)
	 * @see #logCatTxt(int, String, String)
	 * @see #logCatTxt(int, String, String, Throwable)
	 */
	public void logCat(int priority, String logTag, String message) {
		// Switch through the different log priorities and log the correct priority and message
		switch (priority) {
		case (ASSERT):// <-- 7
			// HAS NO Log METHOD
			break;
		case (DEBUG):// <-- 3
			Log.d(logTag, message);
			break;
		case (ERROR):// <-- 6
			Log.e(logTag, message);
			break;
		case (INFO):// <-- 4
			Log.i(logTag, message);
			break;
		case (VERBOSE):// <-- 2
			Log.v(logTag, message);
			break;
		case (WARN):// <-- 5
			Log.w(logTag, message);
			break;
		default:
			// Invalid log priority detected log another log message with this error and the origin log message
			this.logCat(this.ERROR, this.LOG_TAG + ":logCat()", "Log priority wasn't found, check that correct priority constant is used." + " Original logmessage came from: " + logTag + ", with message: " + message);
		}
	}

	/**
	 * Method to create a LogCat log message with throwable object If no valid
	 * priority value are given it will log a message with <b><i>ERROR</i></b>
	 * priority with the origin log message and current error.
	 * 
	 * @param priority
	 *            Priority as integer see {@link #http
	 *            ://www.http://developer.android
	 *            .com/reference/android/util/Log.html}
	 * @param logTag
	 *            Log tag as string
	 * @param message
	 *            Log message
	 * @param thr
	 *            Throwable object
	 * 
	 * @see #logCat(int, String, String)
	 * @see #logTxt(int, String, String)
	 * @see #logCatTxt(int, String, String)
	 * @see #logCatTxt(int, String, String, Throwable)
	 */
	public void logCat(int priority, String logTag, String message, Throwable thr) {
		// Switch through the different log priorities and log the correct priority, message and throwable
		switch (priority) {
		case (ASSERT):// <-- 7
			// HAS NO Log METHOD
			break;
		case (DEBUG):// <-- 3
			Log.d(logTag, message, thr);
			break;
		case (ERROR):// <-- 6
			Log.e(logTag, message, thr);
			break;
		case (INFO):// <-- 4
			Log.i(logTag, message, thr);
			break;
		case (VERBOSE):// <-- 2
			Log.v(logTag, message, thr);
			break;
		case (WARN):// <-- 5
			Log.w(logTag, message, thr);
			break;
		default:
			// Invalid log priority detected log another log message with this error and the origin log message
			this.logCat(this.ERROR, this.LOG_TAG + ":logCat()", "Log priority wasn't found, check that correct priority constant is used." + " Original logmessage came from: " + logTag + ", with message: " + message + " and throwable: " + thr);
		}
	}

	/**
	 * Method to store a log message to file. If no valid priority value are
	 * given it will log a message with <b><i>ERROR</i></b> priority with the
	 * origin log message and current error.<br>
	 * Throws different exceptions depending on error.
	 * 
	 * @param priority
	 *            Priority as integer see {@link #http
	 *            ://www.http://developer.android
	 *            .com/reference/android/util/Log.html}
	 * @param logTag
	 *            Log tag as string
	 * @param message
	 *            Log message
	 * 
	 * @see #logCat(int, String, String)
	 * @see #logCat(int, String, String, Throwable)
	 * @see #logCatTxt(int, String, String)
	 * @see #logCatTxt(int, String, String, Throwable)
	 * @see #checkDirAndFile(File, File, File)
	 * @see #writeLogHeader(File)
	 * @see #checkExternalStorageState()
	 */
	public void logTxt(int priority, String logTag, String message) {
		// Get current time
		this.today = Calendar.getInstance().getTime();

		// Check external storage status
		this.checkExternalStorageState();

		// Only if external storage is fully available can directory and file creating continue
		if (this.mExternalStorageAvailable == true && this.mExternalStorageWriteable == true) {

			// Variable for file
			File file = null;

			// Get path to root
			String root = Environment.getExternalStorageDirectory().toString();

			/* 
			 * Create file objects, one representing the folder and one the actual log file
			 * These are used to check if they already exists or not
			 */
			File saDir = new File(root + "/" + this.DIRECTORY);
			File saFile = new File(root + "/" + this.DIRECTORY + "/" + this.TXT_LOG_FILE);

			// Check if directory and log file already exists
			this.checkDirAndFile(saDir, saFile, file);

			// Create log file object
			file = new File(saDir, this.TXT_LOG_FILE);

			try {
				// Create BufferedWriter with FileWriter of file, used to write to file
				BufferedWriter bW = new BufferedWriter(new FileWriter(file, true));

				// Switch through the different log priorities and log the correct priority and message
				switch (priority) {
				case (ASSERT):// <-- 7
					// HAS NO Log METHOD
					break;
				case (DEBUG):// <-- 3
					bW.write("DEBUG\t\t" + this.formatter.format(this.today) + "\t" + logTag + "#" + message + this.EOL);
					bW.newLine();
					break;
				case (ERROR):// <-- 6
					bW.write("ERROR\t\t" + this.formatter.format(this.today) + "\t" + logTag + "#" + message + this.EOL);
					bW.newLine();
					break;
				case (INFO):// <-- 4
					bW.write("INFO\t\t" + this.formatter.format(this.today) + "\t" + logTag + "#" + message + this.EOL);
					bW.newLine();
					break;
				case (VERBOSE):// <-- 2
					bW.write("VERBOSE\t\t" + this.formatter.format(this.today) + "\t" + logTag + "#" + message + this.EOL);
					bW.newLine();
					break;
				case (WARN):// <-- 5
					bW.write("WARN\t\t" + this.formatter.format(this.today) + "\t" + logTag + "#" + message + this.EOL);
					bW.newLine();
					break;
				default:
					// Invalid log priority detected log another log message with this error and the origin log message
					this.logCat(this.ERROR, this.LOG_TAG + ":logTxt()", "Can't write to file because log priority wasn't found, check that correct priority constant is used");
				}

				// Ensure that everything has been written to the file and close
				bW.flush();
				bW.close();
			} catch (Exception e) {
				e.printStackTrace();
				this.logCat(this.ERROR, this.LOG_TAG + ":logTxt()", "An Exception occurred during writing to file: " + this.TXT_LOG_FILE, e);
			}
		} else {
			// External storage is not available, log it
			this.logCat(this.WARN, this.LOG_TAG + ":logTxt()", "Cannot log to file because external storage isn't available, check earlier logs fore more details");
		}
	}

	/**
	 * Method to store a log message and throwable to file. If no valid priority
	 * value are given it will log a message with <b><i>ERROR</i></b> priority
	 * with the origin log message and current error.<br>
	 * Throws different exceptions depending on error.
	 * 
	 * @param priority
	 *            Priority as integer see {@link #http
	 *            ://www.http://developer.android
	 *            .com/reference/android/util/Log.html}
	 * @param logTag
	 *            Log tag as string
	 * @param message
	 *            Log message
	 * @param thr
	 *            Throwable object
	 * 
	 * @see #logCat(int, String, String)
	 * @see #logCat(int, String, String, Throwable)
	 * @see #logCatTxt(int, String, String)
	 * @see #logCatTxt(int, String, String, Throwable)
	 * @see #checkDirAndFile(File, File, File)
	 * @see #writeLogHeader(File)
	 * @see #checkExternalStorageState()
	 */
	public void logTxt(int priority, String logTag, String message, Throwable thr) {
		// Get current time
		this.today = Calendar.getInstance().getTime();

		// Check external storage status
		this.checkExternalStorageState();

		// Only if external storage is fully available can directory and file creating continue
		if (this.mExternalStorageAvailable == true && this.mExternalStorageWriteable == true) {

			// Variable for file
			File file = null;

			// Get path to root
			String root = Environment.getExternalStorageDirectory().toString();

			/* 
			 * Create file objects, one representing the folder and one the actual log file
			 * These are used to check if they already exists or not
			 */
			File saDir = new File(root + "/" + this.DIRECTORY);
			File saFile = new File(root + "/" + this.DIRECTORY + "/" + this.TXT_LOG_FILE);

			// Check if directory and log file already exists
			this.checkDirAndFile(saDir, saFile, file);

			// Create log file object
			file = new File(saDir, this.TXT_LOG_FILE);

			try {
				// Create BufferedWriter with FileWriter of file, used to write to file
				BufferedWriter bW = new BufferedWriter(new FileWriter(file, true));

				// Switch through the different log priorities and log the correct priority and message
				switch (priority) {
				case (ASSERT):// <-- 7
					// HAS NO Log METHOD
					break;
				case (DEBUG):// <-- 3
					bW.write("DEBUG\t\t" + this.formatter.format(this.today) + "\t" + logTag + "#" + message + "#" + thr + this.EOL);
					bW.newLine();
					break;
				case (ERROR):// <-- 6
					bW.write("ERROR\t\t" + this.formatter.format(this.today) + "\t" + logTag + "#" + message + "#" + thr + this.EOL);
					bW.newLine();
					break;
				case (INFO):// <-- 4
					bW.write("INFO\t\t" + this.formatter.format(this.today) + "\t" + logTag + "#" + message + "#" + thr + this.EOL);
					bW.newLine();
					break;
				case (VERBOSE):// <-- 2
					bW.write("VERBOSE\t\t" + this.formatter.format(this.today) + "\t" + logTag + "#" + message + "#" + thr + this.EOL);
					bW.newLine();
					break;
				case (WARN):// <-- 5
					bW.write("WARN\t\t" + this.formatter.format(this.today) + "\t" + logTag + "#" + message + "#" + thr + this.EOL);
					bW.newLine();
					break;
				default:
					// Invalid log priority detected log another log message with this error and the origin log message
					this.logCat(this.ERROR, this.LOG_TAG + ":logTxt()", "Can't write to file because log priority wasn't found, check that correct priority constant is used");
				}

				// Ensure that everything has been written to the file and close
				bW.flush();
				bW.close();
			} catch (Exception e) {
				e.printStackTrace();
				this.logCat(this.ERROR, this.LOG_TAG + ":logTxt()", "An Exception occurred during writing to file: " + this.TXT_LOG_FILE, e);
			}
		} else {
			// External storage is not available, log it
			this.logCat(this.WARN, this.LOG_TAG + ":logTxt()", "Cannot log to file because external storage isn't available, check earlier logs fore more details");
		}
	}

	/**
	 * Method to check if directory needed directory or file for application
	 * exist, if not theyr'e created.
	 * 
	 * @param saDir
	 *            Path and name of directory as File
	 * @param saFile
	 *            Path and name of file as File
	 * @param file
	 *            File object used to create a new file or directory
	 * 
	 * @exception IOException
	 *                if an error occur during file creation
	 */
	private void checkDirAndFile(File saDir, File saFile, File file) {
		// If SmsAlarm directory doesn't exist create it and create file
		if (!saDir.exists()) {
			// Make dir
			saDir.mkdirs();

			// Create log file object
			file = new File(saDir, this.TXT_LOG_FILE);

			try {
				// Try create new file
				file.createNewFile();
			} catch (IOException e) {
				// Print stack trace and log it
				e.printStackTrace();
				this.logCat(this.ERROR, this.LOG_TAG + ":logTxt()", "An IOException occurred during creation of file: " + this.TXT_LOG_FILE, e);
			}

			// To write log header
			this.writeLogHeader(file);
		}

		// If log file don't exists, create it
		else if (!saFile.exists()) {
			// Create log file object
			file = new File(saDir, this.TXT_LOG_FILE);

			try {
				// Try create new file
				file.createNewFile();
			} catch (IOException e) {
				// Print stack trace and log it
				e.printStackTrace();
				this.logCat(this.ERROR, this.LOG_TAG + ":logTxt()", "An IOException occurred during creation of file: " + this.TXT_LOG_FILE, e);
			}

			// To write log header
			this.writeLogHeader(file);
		}

	}

	/**
	 * Method to write header to log file.
	 * 
	 * @param file
	 *            File to write header to
	 * 
	 * @exception Exception
	 *                if an error occur during writing to file
	 */
	private void writeLogHeader(File file) {
		try {
			// Create BufferedWriter with FileWriter of file, used to write to file
			BufferedWriter bW = new BufferedWriter(new FileWriter(file, false));

			// Write header to file
			bW.write("SmsAlarm - Application Log" + this.EOL + this.EOL);
			bW.write("Priority\tTime\t\t\tTag#Message#Throwable" + this.EOL);
			bW.write("********\t****\t\t\t*********************" + this.EOL);

			// Ensure that everything has been written to the file and close
			bW.flush();
			bW.close();
		} catch (Exception e) {
			// Print stack trace and log it
			e.printStackTrace();
			this.logCat(this.ERROR, this.LOG_TAG + ":logTxt()", "An Exception occurred during writing to file: " + this.TXT_LOG_FILE, e);
		}
	}

	/**
	 * Method to create a LogCat message, it also stores the same information to
	 * file.
	 * 
	 * @param priority
	 *            Priority as integer see {@link #http
	 *            ://www.http://developer.android
	 *            .com/reference/android/util/Log.html}
	 * @param logTag
	 *            Log tag as string
	 * @param message
	 *            Log message
	 * 
	 * @see #logCat(int, String, String)
	 * @see #logCat(int, String, String, Throwable)
	 * @see #logCatTxt(int, String, String, Throwable)
	 * @see #checkExternalStorageState()
	 */
	public void logCatTxt(int priority, String logTag, String message) {
		this.logCat(priority, logTag, message);
		this.logTxt(priority, logTag, message);
	}

	/**
	 * Method to create a LogCat message with throwable, it also stores the same
	 * information to file.
	 * 
	 * @param priority
	 *            Priority as integer see {@link #http
	 *            ://www.http://developer.android
	 *            .com/reference/android/util/Log.html}
	 * @param logTag
	 *            Log tag as string
	 * @param message
	 *            Log message
	 * @param thr
	 *            Throwable object
	 * 
	 * @see #logCat(int, String, String)
	 * @see #logCat(int, String, String, Throwable)
	 * @see #logCatTxt(int, String, String)
	 * @see #checkExternalStorageState()
	 */
	public void logCatTxt(int priority, String logTag, String message, Throwable thr) {
		this.logCat(priority, logTag, message, thr);
		this.logTxt(priority, logTag, message, thr);
	}

	/**
	 * This method checks the state of external storage media and decides
	 * whether its available for reading and writing, only reading, only writing
	 * or not available at all.
	 * 
	 * @see #logCatTxt(int, String, String)
	 * @see #logCatTxt(int, String, String, Throwable)
	 * @see #logTxt(int, String, String)
	 */
	private void checkExternalStorageState() {
		// If statements deciding if we can read/write, both or nothing from the external storage
		if (Environment.MEDIA_MOUNTED.equals(this.STATE)) {
			// We can read and write the media
			this.mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(this.STATE)) {
			// We can only read the media
			this.mExternalStorageAvailable = true;
			this.mExternalStorageWriteable = false;
			this.logCatTxt(this.WARN, this.LOG_TAG + ":checkExternalStorageState()", "Cannot write log, cause: External storage is not available for writing");
		} else {
			/* 
			 * Something else is wrong. It may be one of many other states, but all we need
			 * to know is we can neither read nor write 
			 */
			this.mExternalStorageAvailable = this.mExternalStorageWriteable = false;
			this.logCatTxt(this.ERROR, this.LOG_TAG + ":checkExternalStorageState()", "Cannot write log, cause: External storage is not available for writing or reading");
		}
	}
}
