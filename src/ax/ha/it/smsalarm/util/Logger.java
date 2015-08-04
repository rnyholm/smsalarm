/**
 * Copyright (c) 2015 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.util.Log;

/**
 * Utility class to be used when loggin to file is needed.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
@SuppressLint("SimpleDateFormat")
public class Logger {
	private static final String LOG_TAG = Logger.class.getSimpleName();

	// Name of directory for application
	private static final String DIRECTORY = "SmsAlarm";

	// End of line is nice to have, this particular one represents a windows end of line
	private static final String EOL = "\r\n";

	// SimpleDateFormatter and Date, to get current time in a certain format(LogCat like)
	private static final SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd HH:mm:ss.SSS");
	private Date now = Calendar.getInstance().getTime();

	// Name of file
	private final String fileName;

	/**
	 * Creates a new instance of {@link Logger}, given file name is the file which this instance always will write it's log records to.<br>
	 * The directory of the log files will always be "SmsAlarm".
	 * 
	 * @param fileName
	 *            Name of log file.
	 */
	public Logger(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * To write a log record to file. Which file the log record will be written to depends on what filename this instance of {@link Logger} was
	 * created with.<br>
	 * If necessary directories and files doesn't exists this method will create them before the record is written.
	 * <p>
	 * The output of the log record to file will look like following:<br>
	 * 
	 * <pre>
	 * 13-05-03 23:57:42.123    message
	 * </pre>
	 * 
	 * @param message
	 *            Message of log record.
	 */
	public void log2File(String message) {
		// Only proceed further with logging if the external storage is writable
		if (isExternalStorageWritable()) {
			// Get path to root
			String root = Environment.getExternalStorageDirectory().toString();

			// Create file objects, one representing the directory and one the actual file. These are used to check if they already exists or not and
			// for logging
			File directory = new File(root + "/" + DIRECTORY);
			File file = new File(root + "/" + DIRECTORY + "/" + fileName);

			// Check if directory and log file already exists
			checkDirAndFile(directory, file);

			try {
				// Create BufferedWriter with FileWriter of file, used to write to file
				BufferedWriter bW = new BufferedWriter(new FileWriter(file, true));

				// Get current time
				now = Calendar.getInstance().getTime();

				// Write the log entry to writer
				bW.write(formatter.format(now) + "\t" + message + EOL);

				// Ensure that everything has been written to the file and close
				bW.flush();
				bW.close();
			} catch (Exception e) {
				// An exception occurred while writing to file, log it
				Log.e(LOG_TAG + ":log2File()", "An Exception occurred during writing to file: \"" + fileName + "\"", e);
			}
		} else {
			// External storage isn't available for writing, log this
			Log.e(LOG_TAG + ":log2File()", "Unable to log to file because the external storage isn't available for writing");
		}
	}

	/**
	 * To check if needed directory or file for the application and this instance of {@link Logger} exists on the file system, if they doesn't they
	 * are created.
	 * 
	 * @param directory
	 *            File which path represents the directory needed.
	 * @param file
	 *            File which path represents the file needed.
	 */
	private void checkDirAndFile(File directory, File file) {
		// If directory doesn't exist then create both the directory and a new file, else if the file doesn't exist just create that one
		if (!directory.exists()) {
			directory.mkdirs();

			createNewFile(file);
		} else if (!file.exists()) {
			createNewFile(file);
		}
	}

	/**
	 * Creates a new file on the file system according to the path of given file.
	 * 
	 * @param file
	 *            File which path the new file will be created according to.
	 */
	private void createNewFile(File file) {
		try {
			file.createNewFile();
		} catch (IOException e) {
			Log.e(LOG_TAG + ":createNewFile()", "An exception occurred during creation of file: \"" + fileName + "\"", e);
		}
	}

	/**
	 * To figure out whether or not the external storage is writable or not.
	 * 
	 * @return <code>true</code> if the external storage is writable, else <code>false</code>.
	 */
	private boolean isExternalStorageWritable() {
		boolean result = false;

		// Get the current storage state
		String externalStorageState = Environment.getExternalStorageState();

		// MEDIA_MOUNTED = Writable, good to go
		if (Environment.MEDIA_MOUNTED.equals(externalStorageState)) {
			result = true;
		}

		return result;
	}
}
