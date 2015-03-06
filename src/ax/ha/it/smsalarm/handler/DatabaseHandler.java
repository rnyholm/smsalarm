/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.handler;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import ax.ha.it.smsalarm.alarm.Alarm;
import ax.ha.it.smsalarm.alarm.Alarm.AlarmType;

/**
 * Class responsible for all <code>Database</code> access and handling. <code>Database</code> access and handling are done via the
 * {@link SQLiteOpenHelper} class.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.1beta
 */
public class DatabaseHandler extends SQLiteOpenHelper {
	private static final String LOG_TAG = DatabaseHandler.class.getSimpleName();

	// Database Version and the upgrade versions
	private static final int DB_VERSION = 3;
	private static final int DB_VERSION_ADD_TRIGGER_TEXT_COL = 2;
	private static final int DB_VERSION_CHANGE_DATE_FORMAT = DB_VERSION;

	// Database Name
	private static final String DB_NAME = "alarmsManager";

	// Alarms Table name
	private static final String TABLE_ALARMS = "alarms";

	// Temporary tag used for data migration script
	private static final String TMP = "tmp_";

	// Alarms Table Column names
	private static final String KEY_ID = "id";
	private static final String KEY_RECEIVED = "received";
	private static final String KEY_SENDER = "sender";
	private static final String KEY_MESSAGE = "message";
	private static final String KEY_TRIGGER_TEXT = "triggerText";
	private static final String KEY_ACKNOWLEDGED = "acknowledged";
	private static final String KEY_ALARM_TYPE = "alarmType";

	/**
	 * Creates a new instance of {@link DatabaseHandler} with given {@link Context}.
	 * 
	 * @param context
	 *            The Context in which <code>DatabaseHandler</code> will run.
	 */
	public DatabaseHandler(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	/**
	 * To create a table containing all <b><i>Columns</i></b> and <b><i>Attributes</i></b> needed for properly persist a {@link Alarm} object.
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		// Build up the query for creating the table
		String CREATE_ALARMS_TABLE = "CREATE TABLE " + TABLE_ALARMS + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_RECEIVED + " TEXT DEFAULT ''," + KEY_SENDER + " TEXT DEFAULT ''," + KEY_MESSAGE + " TEXT DEFAULT ''," + KEY_TRIGGER_TEXT + " TEXT DEFAULT '-'," + KEY_ACKNOWLEDGED + " TEXT DEFAULT ''," + KEY_ALARM_TYPE + " INTEGER)";
		// Run query
		db.execSQL(CREATE_ALARMS_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Upgrade handling for adding free text trigger columns
		if (oldVersion < DB_VERSION_ADD_TRIGGER_TEXT_COL) {
			// The queries needed for the upgrade and data migration
			String ALTER_QUERY = "ALTER TABLE " + TABLE_ALARMS + " RENAME TO " + TMP + TABLE_ALARMS;
			String DROP_QUERY = "DROP TABLE " + TMP + TABLE_ALARMS;
			String INSERT_QUERY = "INSERT INTO " + TABLE_ALARMS + " (" + KEY_ID + "," + KEY_RECEIVED + "," + KEY_SENDER + "," + KEY_MESSAGE + "," + KEY_ACKNOWLEDGED + "," + KEY_ALARM_TYPE + ")" + "SELECT " + KEY_ID + "," + KEY_RECEIVED + "," + KEY_SENDER + "," + KEY_MESSAGE + "," + KEY_ACKNOWLEDGED + "," + KEY_ALARM_TYPE + " FROM " + TMP + TABLE_ALARMS;

			// Begin data migration and reconstruction of existing database, beginning with renaming existing table
			db.execSQL(ALTER_QUERY);

			// Create the new and correct table
			onCreate(db);

			// Populate new table with existing data from old table(now seen as a temporary table)
			db.execSQL(INSERT_QUERY);

			// Now drop the temporary table
			db.execSQL(DROP_QUERY);
		}

		// Upgrade handling for changing date format of received alarms
		if (oldVersion < DB_VERSION_CHANGE_DATE_FORMAT) {
			// The queries needed for the upgrade and data migration
			String ALTER_QUERY = "ALTER TABLE " + TABLE_ALARMS + " RENAME TO " + TMP + TABLE_ALARMS;
			String DROP_QUERY = "DROP TABLE " + TMP + TABLE_ALARMS;
			String SELECT_ALL_QUERY = "SELECT * FROM " + TMP + TABLE_ALARMS;

			// Rename existing alarm table
			db.execSQL(ALTER_QUERY);

			// Create the new and correct table
			onCreate(db);

			// Fetch all received alarms into a cursor
			Cursor cursor = db.rawQuery(SELECT_ALL_QUERY, null);

			// Iterate through all rows and adding to list
			if (cursor.moveToFirst()) {
				do {
					ContentValues values = new ContentValues();
					values.put(KEY_SENDER, cursor.getString(2));
					values.put(KEY_MESSAGE, cursor.getString(3));
					values.put(KEY_TRIGGER_TEXT, cursor.getString(4));
					values.put(KEY_ALARM_TYPE, cursor.getInt(6));

					// Get the localized time stamps from the cursor
					String received = cursor.getString(1);
					String acknowledged = cursor.getString(5);

					// To store the time stamps in milliseconds
					long receivedMillisecs = -1;
					long acknowledgedMillisecs = -1;

					// Iterate through each possible locale to try to find out the correct one for parsing
					for (Locale locale : Locale.getAvailableLocales()) {
						try {
							if (receivedMillisecs < 0) {
								receivedMillisecs = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, locale).parse(received).getTime();
							}
						} catch (ParseException e) {
							// Swallow this, as this functionality relies on an exception
						}

						try {
							if (acknowledgedMillisecs < 0) {
								acknowledgedMillisecs = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, locale).parse(acknowledged).getTime();
							}
						} catch (ParseException e) {
							// Swallow this, as this functionality relies on an exception
						}
					}

					// Put the rest of the values and persist to database, if and only if we could parse out time stamp for Alarm received
					if (receivedMillisecs > -1) {
						values.put(KEY_RECEIVED, String.valueOf(receivedMillisecs));
						values.put(KEY_ACKNOWLEDGED, acknowledgedMillisecs == -1 ? "-" : String.valueOf(acknowledgedMillisecs));

						// Inserting Row
						db.insert(TABLE_ALARMS, null, values);
					}
				} while (cursor.moveToNext());
			}

			// Close the cursor
			cursor.close();

			// Now drop the temporary table
			db.execSQL(DROP_QUERY);
		}
	}

	/**
	 * To insert a new {@link Alarm} to the database.
	 * 
	 * @param alarm
	 *            Alarm to be inserted in database.
	 */
	public void insertAlarm(Alarm alarm) {
		// Get a writable database handle
		SQLiteDatabase db = getWritableDatabase();

		// @formatter:off
		// Fetch values from alarm and put the into a ContentValues variable
		ContentValues values = new ContentValues();
		values.put(KEY_RECEIVED, alarm.getReceivedMillisecs()); 		// Date and time when alarm was received
		values.put(KEY_SENDER, alarm.getSender()); 						// Sender of the alarm
		values.put(KEY_MESSAGE, alarm.getMessage()); 					// Alarm message
		values.put(KEY_TRIGGER_TEXT, alarm.getTriggerText()); 			// Triggering text of a free text alarm
		values.put(KEY_ACKNOWLEDGED, alarm.getAcknowledgedMillisecs());	// Date and time the alarm was acknowledged
		values.put(KEY_ALARM_TYPE, alarm.getAlarmType().ordinal()); 	// Type of alarm
		// @formatter:on

		// Inserting Row
		db.insert(TABLE_ALARMS, null, values);
		db.close(); // Closing database connection
	}

	/**
	 * To fetch an {@link Alarm} from the database with the given <code>id</code>.
	 * 
	 * @param id
	 *            The id of the Alarm to fetch.
	 * @return Fetched Alarm.
	 */
	public Alarm fetchAlarm(int id) {
		// Get a readable database handle
		SQLiteDatabase db = getReadableDatabase();

		// Create query and execute it, store result in cursor
		Cursor cursor = db.query(TABLE_ALARMS, new String[] { KEY_ID, KEY_RECEIVED, KEY_SENDER, KEY_MESSAGE, KEY_TRIGGER_TEXT, KEY_ACKNOWLEDGED, KEY_ALARM_TYPE }, KEY_ID + "=?", new String[] { String.valueOf(id) }, null, null, null, null);

		// Check if we got any results from the query
		if (cursor != null) {
			// Move to first element so we can fetch data from the cursor later
			cursor.moveToFirst();
		}

		// Create a new alarm object with data resolved from cursor
		Alarm alarm = new Alarm(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), AlarmType.of(cursor.getInt(6)));

		// Close cursor and database
		cursor.close();
		db.close();

		// Return alarm
		return alarm;
	}

	/**
	 * To fetch all {@link Alarm}'s stored in database as a {@link List} of <code>Alarm</code> objects. An empty list will be returned if no entries
	 * are found in database.
	 * 
	 * @return All Alarms stored in database.
	 */
	public List<Alarm> fetchAllAlarm() {
		// List to store all alarms in
		List<Alarm> alarmList = new ArrayList<Alarm>();

		// Select All Query
		String selectQuery = "SELECT * FROM " + TABLE_ALARMS;

		// Get a writable database handle
		SQLiteDatabase db = getWritableDatabase();

		// Execute query, store result in cursor
		Cursor cursor = db.rawQuery(selectQuery, null);

		// Iterate through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				// Create a new alarm object and fill it with data from cursor and add it to the list
				alarmList.add(new Alarm(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), AlarmType.of(cursor.getInt(6))));
			} while (cursor.moveToNext());
		}

		// Close cursor and database
		cursor.close();
		db.close();

		// return contact list
		return alarmList;
	}

	public TreeMap<String, HashMap<String, List<Alarm>>> fetchAllAlarmsOrganized() {
		TreeMap<String, HashMap<String, List<Alarm>>> organizedAlarms = new TreeMap<String, HashMap<String, List<Alarm>>>(new Comparator<String>() {
			@Override
			public int compare(String lhs, String rhs) {
				int lhsYear = Integer.parseInt(lhs.substring(lhs.length() - 4));
				int rhsYear = Integer.parseInt(rhs.substring(rhs.length() - 4));

				if (lhsYear < rhsYear) {
					return 1;
				} else if (lhsYear > rhsYear) {
					return -1;
				}

				return 0;
			}
		});

		HashMap<String, List<Alarm>> alarmsPerMonth = new HashMap<String, List<Alarm>>();
		List<Alarm> alarms = new ArrayList<Alarm>();

		for (Alarm alarm : fetchAllAlarm()) {
			// Get a calendar instance and set time from time and date when the alarm was received
			Calendar calendar = Calendar.getInstance();

			calendar.setTime(alarm.getReceived());
			// Get year and month when the alarm was received
			String yearReceived = String.valueOf(calendar.get(Calendar.YEAR));
			String monthReceived = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());

			if (organizedAlarms.containsKey(yearReceived)) {
				alarmsPerMonth = organizedAlarms.get(yearReceived);

				if (alarmsPerMonth.containsKey(monthReceived)) {
					alarms = alarmsPerMonth.get(monthReceived);
					alarms.add(alarm);
					alarmsPerMonth.put(monthReceived, alarms);
				} else {
					alarms = new ArrayList<Alarm>();
					alarms.add(alarm);
					alarmsPerMonth = new HashMap<String, List<Alarm>>();
					alarmsPerMonth.put(monthReceived, alarms);
				}
			} else {
				alarms = new ArrayList<Alarm>();
				alarms.add(alarm);
				alarmsPerMonth = new HashMap<String, List<Alarm>>();
				alarmsPerMonth.put(monthReceived, alarms);
			}

			organizedAlarms.put(yearReceived, alarmsPerMonth);
		}

		return organizedAlarms;
	}

	/**
	 * To get the number of {@link Alarm}'s in database.
	 * 
	 * @return Number of alarms.
	 */
	public int getAlarmsCount() {
		// Select all query
		String countQuery = "SELECT  * FROM " + TABLE_ALARMS;

		// To store number of alarms in database in
		int alarmsCount = 0;

		// Get a readable database handle
		SQLiteDatabase db = getReadableDatabase();

		// Execute query, store result in cursor
		Cursor cursor = db.rawQuery(countQuery, null);

		// Store number of elements in cursor before closing it
		alarmsCount = cursor.getCount();

		// Close cursor and database
		cursor.close();
		db.close();

		// Return number of entries in database
		return alarmsCount;
	}

	/**
	 * To fetch the latest {@link Alarm} entry in the database.
	 */
	public Alarm fetchLatestAlarm() {
		// Returning latest alarm in database
		return fetchAlarm(getAlarmsCount());
	}

	/**
	 * To update an existing {@link Alarm} in database.
	 * 
	 * @param alarm
	 *            Alarm to be inserted/updated in database.
	 * @return Id of updated Alarm DON'T REALLY KNOW HOW AND WHY!?!?
	 */
	public int updateAlarm(Alarm alarm) {
		// Get a writable database handle
		SQLiteDatabase db = getWritableDatabase();

		// @formatter:off
		// Fetch values from alarm and put the into a ContentValues variable
		ContentValues values = new ContentValues();
		values.put(KEY_RECEIVED, alarm.getReceivedMillisecs()); 		// Date and time when alarm was received
		values.put(KEY_SENDER, alarm.getSender()); 						// Sender of the alarm
		values.put(KEY_MESSAGE, alarm.getMessage()); 					// Alarm message
		values.put(KEY_TRIGGER_TEXT, alarm.getTriggerText()); 			// Triggering text of a free text alarm
		values.put(KEY_ACKNOWLEDGED, alarm.getAcknowledgedMillisecs());	// Date and time the alarm was acknowledged
		values.put(KEY_ALARM_TYPE, alarm.getAlarmType().ordinal()); 	// Type of alarm
		// @formatter:on

		// Updating row
		return db.update(TABLE_ALARMS, values, KEY_ID + " = ?", new String[] { String.valueOf(alarm.getId()) });
	}

	/**
	 * To update latest {@link Alarm}'s acknowledge time in database.
	 */
	public void updateLatestAlarmAcknowledged() {
		// Get and store number of entries(alarms) in database
		int alarmsCount = getAlarmsCount();

		// Get latest entry(alarm) in database
		Alarm alarm = fetchAlarm(alarmsCount);
		// Update alarms acknowledge time
		alarm.updateAcknowledged();

		// Update alarm entry
		updateAlarm(alarm);
	}

	/**
	 * To update the latest {@link Alarm} of {@link AlarmType#PRIMARY} with a new acknowledge time in database.
	 */
	public void updateLatestPrimaryAlarmAcknowledged() {
		// Iterate through all alarms in database from the last one and down
		for (int i = getAlarmsCount(); i > 0; i--) {
			// Get entry(alarm) in database
			Alarm alarm = fetchAlarm(i);

			// If alarm type is primary we want to update it's acknowledge time
			if (AlarmType.PRIMARY.equals(alarm.getAlarmType())) {
				// Update alarms acknowledge time
				alarm.updateAcknowledged();
				// Update alarm entry
				updateAlarm(alarm);

				// Get out of the loop, as rest of the alarm not is are of interest
				break;
			}
		}
	}

	/**
	 * To delete an {@link Alarm} from the database.
	 * 
	 * @param alarm
	 *            Alarm to be deleted from database.
	 */
	public void deleteAlarm(Alarm alarm) {
		// Get a writable database handle
		SQLiteDatabase db = getWritableDatabase();
		db.delete(TABLE_ALARMS, KEY_ID + " = ?", new String[] { String.valueOf(alarm.getId()) });
		db.close();
	}
}
