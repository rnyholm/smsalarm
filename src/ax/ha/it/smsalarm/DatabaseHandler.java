/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import ax.ha.it.smsalarm.LogHandler.LogPriorities;

/**
 * Class responsible for all <code>Database</code> access and handling.
 * <code>Database</code> access and handling are done via the <code>SQLiteOpenHelper</code> class.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.1
 * @since 2.1beta
 * @date 2013-08-09
 */
public class DatabaseHandler extends SQLiteOpenHelper {
	// Log tag string
	private static final String LOG_TAG = "DatabaseHandler";

	// Object for logging
	private static final LogHandler logger = LogHandler.getInstance();
	
	// Database Version
	private static final int DB_VERSION = 1;
	
	// Database Name
	private static final String DB_NAME = "alarmsManager";
	
	// Alarms Table name
	private static final String TABLE_ALARMS = "alarms";
	
	// Alarms Table Column names
	private static final String KEY_ID = "id";
	private static final String KEY_RECEIVED = "received";
	private static final String KEY_SENDER = "sender";
	private static final String KEY_MESSAGE = "message";
	private static final String KEY_ACKNOWLEDGED = "acknowledged";
	
	/**
	 * Constructor for the <code>DatabaseHandler</code>, takes just context as
	 * argument. Calling super classes constructor with <code>context, DATABASE_NAME, DATABASE_VERSION</code>.
	 * 
	 * @param context Context in which to create the <code>DatabaseHandler</code> object
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	public DatabaseHandler(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":DatabaseHandler()", "DatabaseHandler object has been created with following data:[DB_NAME:\"" + DB_NAME + "\", DB_VERSION:\"" +  DB_VERSION + "\" and context:\"" + context.toString() + "\"]");
	}

	/**
	 * To create table table with columns representing an <code>Alarm</code>.
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.Alarm
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		// Build up the query for creating the table
        String CREATE_ALARMS_TABLE = "CREATE TABLE " + TABLE_ALARMS + "("+ KEY_ID + " INTEGER PRIMARY KEY," + KEY_RECEIVED + " TEXT,"
            							+ KEY_SENDER + " TEXT," + KEY_MESSAGE + " TEXT," + KEY_ACKNOWLEDGED + " TEXT)";
        // Run query
        db.execSQL(CREATE_ALARMS_TABLE);
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate()", "Table has been created from following query:\"" + CREATE_ALARMS_TABLE + "\"");        
	}

	/**
	 * To upgrade the database.
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALARMS);

		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onUpgrade()", "Table has been dropped if it existed");     
 
        // Create tables again
        onCreate(db);
	}

	/**
	 * To insert a new <code>Alarm</code> in the database.
	 * 
	 * @param alarm Alarm to be inserted in database
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.Alarm
	 */
	public void addAlarm(Alarm alarm) {
		// Get a writable database handle
		SQLiteDatabase db = this.getWritableDatabase();
		
		// Fetch values from alarm and put the into a ContentValues variable
	    ContentValues values = new ContentValues();    
	    values.put(KEY_RECEIVED, alarm.getReceived());			// Date and time when alarm was received
	    values.put(KEY_SENDER, alarm.getSender());				// Sender of the alarm
	    values.put(KEY_MESSAGE, alarm.getMessage());			// Alarm message
	    values.put(KEY_ACKNOWLEDGED, alarm.getAcknowledged());	// Date and time the alarm was acknowledged
	 
	    // Inserting Row
	    db.insert(TABLE_ALARMS, null, values);
	    db.close(); // Closing database connection	
	    
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":addAlarm()", "An alarm with following data has been inserted into the database: [" + alarm.toString() + "]");  
	}
	
	/**
	 * To get a row from the database with the given <code>id</code> as an <code>Alarm</code> object.
	 *  
	 * @param id Find and return the <code>Alarm</code> object with this id given as <code>int</code>
	 * 
	 * @return <code>Alarm</code> object
	 * 
	 * @throws android.database.CursorIndexOutOfBoundsException If no row with the given id is found in the database
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.Alarm
	 */
	public Alarm getAlarm(int id) throws android.database.CursorIndexOutOfBoundsException {
		// Get a readable database handle
	    SQLiteDatabase db = this.getReadableDatabase();
	    
	    // Create query and execute it, store result in cursor
	    Cursor cursor = db.query(TABLE_ALARMS, new String[] { KEY_ID, KEY_RECEIVED, KEY_SENDER, KEY_MESSAGE, KEY_ACKNOWLEDGED }, 
	    						 KEY_ID + "=?", new String[] { String.valueOf(id) }, null, null, null, null);
	    
	    // CHeck if we got any results from the query
	    if (cursor != null) {
			// Log in debug purpose
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getAlarm()", "Got results from query");
			// Move to first element so we can fetch data from the cursor later
	    	cursor.moveToFirst();    	
	    }
	    // Create a new alarm object with data resolved from cursor
	    Alarm alarm = new Alarm(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
	    // Return alarm
	    return alarm;
	}
	
	/**
	 * To get all <code>Alarm</code> entries in database as a <code>List</code> of <code>Alarm</code> objects.
	 * An empty list will be returned if no entries are found in database.
	 * 
	 * @return All entries in database as a <code>List</code> of <code>Alarm</code> objects
	 * 
 	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.Alarm
	 */
	public List<Alarm> getAllAlarm() {
		// List to store all alarms in
		List<Alarm> alarmList = new ArrayList<Alarm>();
		
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_ALARMS;

		// Get a writable database handle
		SQLiteDatabase db = this.getWritableDatabase();
		
		// Execute query, store result in cursor
		Cursor cursor = db.rawQuery(selectQuery, null);
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getAllAlarm()", "All alarms have been fetched from the database with the following query:\"" + selectQuery + "\"");
 
		// Iterate through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				// Create a new alarm object and fill it with data from cursor
				Alarm alarm = new Alarm();
				alarm.setId(Integer.parseInt(cursor.getString(0)));
				alarm.setReceived(cursor.getString(1));
				alarm.setSender(cursor.getString(2));
				alarm.setMessage(cursor.getString(3));
				alarm.setAcknowledged(cursor.getString(4));
				// Adding contact to list
				alarmList.add(alarm);
			} while (cursor.moveToNext());
		}
 
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getAllAlarm()", "Returning following list of Alarms:\"" + alarmList.toString() + "\"");		
		// return contact list
		return alarmList;
	}
	
	/**
	 * To count number of <code>Alarm</code>'s (entries) in database.
	 * 
	 * @return Number of <code>Alarm</code>'s (entries) in database as <code>int</code>
	 * 
 	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.Alarm
	 */
	public int getAlarmsCount() {
		// Select all query
        String countQuery = "SELECT  * FROM " + TABLE_ALARMS;
        
        // To store number of alarms in database in
        int alarmsCount = 0;
        
        // Get a readable database handle
        SQLiteDatabase db = this.getReadableDatabase();
        // Execute query, store result in cursor
        Cursor cursor = db.rawQuery(countQuery, null);
        
        // Store number of elements in cursor before closing it
        alarmsCount = cursor.getCount();
        cursor.close();
 
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getAlarmsCount()", "Number of alarms(entries) in database are:\"" + Integer.toString(alarmsCount) + "\"");
		
        // Return number of entries in database
        return alarmsCount;			
	}
	
	/**
	 * To update an existing <code>Alarm</code> entry in database. 
	 * It finds correct entry to update on alarms <code>id</code>.
	 * 
	 * @param alarm <code>Alarm</code> to be inserted in database.
	 * 
	 * @return <code>int</code> DON'T REALLY KNOW!?!?
	 * 
 	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.Alarm
	 */
	public int updateAlarm(Alarm alarm) {
		// Get a writable database handle
	    SQLiteDatabase db = this.getWritableDatabase();
	    
		// Fetch values from alarm and put the into a ContentValues variable
	    ContentValues values = new ContentValues();    
	    values.put(KEY_RECEIVED, alarm.getReceived());			// Date and time when alarm was received
	    values.put(KEY_SENDER, alarm.getSender());				// Sender of the alarm
	    values.put(KEY_MESSAGE, alarm.getMessage());			// Alarm message
	    values.put(KEY_ACKNOWLEDGED, alarm.getAcknowledged());	// Date and time the alarm was acknowledged
	    
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateAlarm()", "Values have been retrieved from Alarm object and database entry is going to be updated");
	 
	    // Updating row
	    return db.update(TABLE_ALARMS, values, KEY_ID + " = ?", new String[] { String.valueOf(alarm.getId()) });		
	}
	
	/**
	 * To update latest <code>Alarm</code> entry's acknowledge time in database.
	 * 
	 * @see #getAlarmsCount()
	 * @see #getAlarm(int)
	 * @see #updateAlarm(Alarm)
 	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
 	 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String, String, Throwable) logCat(LogPriorities, String, String, Throwable)
	 * @see ax.ha.it.smsalarm.Alarm
	 */
	public void updateLatestAlarmAcknowledged() {				
		try {
			// Log in debug purpose
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateLatestAlarmAcknowledged()", "Trying to update latest alarms acknowledge time in database");
			
			// Get and store number of entries(alarms) in database
			int alarmsCount = this.getAlarmsCount();
			
			// Get latest entry(alarm) in database
			Alarm alarm = this.getAlarm(alarmsCount);
			// Update alarms acknowledge time
			alarm.updateAcknowledged();
			
			// Update alarm entry
			this.updateAlarm(alarm);
			
			// Log in debug purpose
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateLatestAlarmAcknowledged()", "Latest alarms acknowledge time has been updated");			
		} catch (android.database.CursorIndexOutOfBoundsException e) {
			// Log error
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":updateLatestAlarmAcknowledged()", "android.database.CursorIndexOutOfBoundsException occured while getting alarm from database", e);
		}
	}
	
	/**
	 * To delete an existing <code>Alarm</code> entry in database. 
	 * It finds correct entry to delete on alarms <code>id</code>.
	 * 
	 * @param alarm <code>Alarm</code> to be deleted from database.
	 * 
 	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.Alarm
	 */
	public void deleteAlarm(Alarm alarm) {
		// Get a writable database handle
		SQLiteDatabase db = this.getWritableDatabase();
    	db.delete(TABLE_ALARMS, KEY_ID + " = ?", new String[] { String.valueOf(alarm.getId()) });
	    db.close();	
	    
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":deleteAlarm()", "Alarm:\"" + alarm.toString() + "\" has been deleted from database");
	}
}
