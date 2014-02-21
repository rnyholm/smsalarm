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
 * @version 2.2
 * @since 2.1beta
 */
public class DatabaseHandler extends SQLiteOpenHelper {
	// Log tag string
	private final String LOG_TAG = this.getClass().getSimpleName();
	
	// We need to have context in order to set some time stamps
	private Context context;

	// Object for logging
	private static final LogHandler logger = LogHandler.getInstance();
	
	// Database Version
	private static final int DB_VERSION = 2;
	
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
	 * Constructor for the <code>DatabaseHandler</code>, takes just context as
	 * argument. Calling super classes constructor with <code>context, DATABASE_NAME, DATABASE_VERSION</code>.
	 * 
	 * @param context Context in which to create the <code>DatabaseHandler</code> object
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	public DatabaseHandler(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		
		// Store the context
		this.context = context;
		
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":DatabaseHandler()", "DatabaseHandler object has been created with following data:[DB_NAME:\"" + DB_NAME + "\", DB_VERSION:\"" +  DB_VERSION + "\" and context:\"" + context.toString() + "\"]");
	}

	/**
	 * To create table table with columns representing an <code>Alarm</code>.
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.Alarm ax.ha.it.smsalarm.Alarm
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		// Build up the query for creating the table
        String CREATE_ALARMS_TABLE = "CREATE TABLE " + TABLE_ALARMS + "("+ KEY_ID + " INTEGER PRIMARY KEY," + KEY_RECEIVED + " TEXT DEFAULT '',"
            							+ KEY_SENDER + " TEXT DEFAULT ''," + KEY_MESSAGE + " TEXT DEFAULT ''," + KEY_TRIGGER_TEXT + " TEXT DEFAULT '-'," + KEY_ACKNOWLEDGED + " TEXT DEFAULT ''," + KEY_ALARM_TYPE + " INTEGER)";
        // Run query
        db.execSQL(CREATE_ALARMS_TABLE);
        
        // Log in debug purpose
        logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate()", "Executed SQL query:\"" + CREATE_ALARMS_TABLE + "\""); 
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate()", "Table has been created");        
	}

	/**
	 * To upgrade the database.
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onUpgrade()", oldVersion + " -- " + newVersion); 
		
        // If there is a new version of the database, reconstruct existing database and handle data migration
        if (newVersion > oldVersion && newVersion == DB_VERSION) {
     		// Log in debug purpose
    		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onUpgrade()", "Table:\"" + TABLE_ALARMS + "\" already exists, begin upgrade of table structure and data migration");  
    		
    		// The queries needed for the upgrade and data migration
    		String ALTER_QUERY = "ALTER TABLE " + TABLE_ALARMS + " RENAME TO " + TMP + TABLE_ALARMS;
        	String DROP_QUERY = "DROP TABLE " + TMP + TABLE_ALARMS;
        	String INSERT_QUERY = "INSERT INTO " + TABLE_ALARMS + " (" + KEY_ID + "," + KEY_RECEIVED + "," + KEY_SENDER + "," + KEY_MESSAGE + "," + KEY_ACKNOWLEDGED + "," + KEY_ALARM_TYPE + ")" +
        						  "SELECT " + KEY_ID + "," + KEY_RECEIVED + "," + KEY_SENDER + "," + KEY_MESSAGE + "," + KEY_ACKNOWLEDGED + "," + KEY_ALARM_TYPE +  " FROM " + TMP + TABLE_ALARMS;
    		
        	// Begin data migration and reconstruction of existing database, beginning with renaming existing table
        	db.execSQL(ALTER_QUERY);
        	logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onUpgrade()", "Executed SQL query:\"" + ALTER_QUERY + "\""); 
        	
        	// Create the new and correct table
        	onCreate(db);
        	
        	// Populate new table with existing data from old table(now seen as a temporary table)
        	db.execSQL(INSERT_QUERY);
        	logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onUpgrade()", "Executed SQL query:\"" + INSERT_QUERY + "\""); 

        	// Now drop the temporary table
        	db.execSQL(DROP_QUERY);
        	logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onUpgrade()", "Executed SQL query:\"" + DROP_QUERY + "\""); 
    		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onUpgrade()", "Existing table:\"" + TABLE_ALARMS + "\" has been altered by adding column:\"" + KEY_TRIGGER_TEXT + "\" and populating it with existing data");  
        } else {
     		// Log in debug purpose
    		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onUpgrade()", "Table:\"" + TABLE_ALARMS + "\" doesn't exist, create a new one");
    		
    		// Just create the new table
    		onCreate(db);
        }
	}

	/**
	 * To insert a new <code>Alarm</code> in the database.
	 * 
	 * @param alarm Alarm to be inserted in database
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.Alarm ax.ha.it.smsalarm.Alarm
	 * @see ax.ha.it.smsalarm.Alarm#getReceived() getReceived()
	 * @see ax.ha.it.smsalarm.Alarm#getSender() getSender()
	 * @see ax.ha.it.smsalarm.Alarm#getMessage() getMessage()
	 * @see ax.ha.it.smsalarm.Alarm#getAcknowledged() getAcknowledged()
	 * @see ax.ha.it.smsalarm.Alarm#getAlarmType() getAlarmType()
	 */
	public void addAlarm(Alarm alarm) {
		// Get a writable database handle
		SQLiteDatabase db = this.getWritableDatabase();
		
		// Fetch values from alarm and put the into a ContentValues variable
	    ContentValues values = new ContentValues();    
	    values.put(KEY_RECEIVED, alarm.getReceived());				// Date and time when alarm was received
	    values.put(KEY_SENDER, alarm.getSender());					// Sender of the alarm
	    values.put(KEY_MESSAGE, alarm.getMessage());				// Alarm message
	    values.put(KEY_TRIGGER_TEXT, alarm.getTriggerText());		// Triggering text of a free text alarm	    
	    values.put(KEY_ACKNOWLEDGED, alarm.getAcknowledged());		// Date and time the alarm was acknowledged
	    values.put(KEY_ALARM_TYPE, alarm.getAlarmType().ordinal());	// Type of alarm
	    
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
	 * @see ax.ha.it.smsalarm.Alarm ax.ha.it.smsalarm.Alarm
	 */
	public Alarm getAlarm(int id) throws android.database.CursorIndexOutOfBoundsException {
		// Get a readable database handle
	    SQLiteDatabase db = this.getReadableDatabase();
	    
	    // Create query and execute it, store result in cursor
	    Cursor cursor = db.query(TABLE_ALARMS, new String[] { KEY_ID, KEY_RECEIVED, KEY_SENDER, KEY_MESSAGE, KEY_TRIGGER_TEXT, KEY_ACKNOWLEDGED, KEY_ALARM_TYPE }, 
	    						 KEY_ID + "=?", new String[] { String.valueOf(id) }, null, null, null, null);
	    
	    // CHeck if we got any results from the query
	    if (cursor != null) {
			// Log in debug purpose
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getAlarm()", "Got results from query");
			// Move to first element so we can fetch data from the cursor later
	    	cursor.moveToFirst();    	
	    }
	    // Create a new alarm object with data resolved from cursor
	    Alarm alarm = new Alarm(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), AlarmTypes.of(Integer.parseInt(cursor.getString(6))));
        // Close cursor and database
        cursor.close();
        db.close();
	    
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
	 * @see ax.ha.it.smsalarm.Alarm ax.ha.it.smsalarm.Alarm
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
				// Create a new alarm object and fill it with data from cursor and add it to the list
				alarmList.add(new Alarm(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), AlarmTypes.of(Integer.parseInt(cursor.getString(6)))));
			} while (cursor.moveToNext());
		}
		
        // Close cursor and database
        cursor.close();
        db.close();
 
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
        
        // Close cursor and database
        cursor.close();
        db.close();
 
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getAlarmsCount()", "Number of alarms(entries) in database are:\"" + Integer.toString(alarmsCount) + "\"");
		
        // Return number of entries in database
        return alarmsCount;			
	}
	
	/**
	 * To get the latest <code>Alarm</code> entry in the database.
	 * 
	 * @see #getAlarmsCount()
	 * @see #getAlarm(int)
 	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
 	 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String, String, Throwable) logCat(LogPriorities, String, String, Throwable)
	 * @see ax.ha.it.smsalarm.Alarm ax.ha.it.smsalarm.Alarm
	 */
	public Alarm getLatestAlarm() {
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getLatestAlarm()", "Try to return latest alarm(entry) in database");		
		
		try {
			// Returning latest alarm in database
			return this.getAlarm(this.getAlarmsCount());
		} catch (android.database.CursorIndexOutOfBoundsException e) {
			// Log error
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":getLatestAlarm()", "android.database.CursorIndexOutOfBoundsException occurred while getting alarm from database", e);
		}
		
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getLatestAlarm()", "Returning an empty Alarm object");		
		// If an exception occurred return a new Alarm object
		return new Alarm();
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
	 * @see ax.ha.it.smsalarm.Alarm ax.ha.it.smsalarm.Alarm
	 * @see ax.ha.it.smsalarm.Alarm#getReceived() getReceived()
	 * @see ax.ha.it.smsalarm.Alarm#getSender() getSender()
	 * @see ax.ha.it.smsalarm.Alarm#getMessage() getMessage()
	 * @see ax.ha.it.smsalarm.Alarm#getAcknowledged() getAcknowledged()
	 * @see ax.ha.it.smsalarm.Alarm#getAlarmType() getAlarmType()
	 */
	public int updateAlarm(Alarm alarm) {
		// Get a writable database handle
	    SQLiteDatabase db = this.getWritableDatabase();
	    
		// Fetch values from alarm and put the into a ContentValues variable
	    ContentValues values = new ContentValues();    
	    values.put(KEY_RECEIVED, alarm.getReceived());				// Date and time when alarm was received
	    values.put(KEY_SENDER, alarm.getSender());					// Sender of the alarm
	    values.put(KEY_MESSAGE, alarm.getMessage());				// Alarm message
	    values.put(KEY_TRIGGER_TEXT, alarm.getTriggerText());		// Triggering text of a free text alarm
	    values.put(KEY_ACKNOWLEDGED, alarm.getAcknowledged());		// Date and time the alarm was acknowledged
	    values.put(KEY_ALARM_TYPE, alarm.getAlarmType().ordinal());	// Type of alarm
	    
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
	 * @see ax.ha.it.smsalarm.Alarm ax.ha.it.smsalarm.Alarm
	 * @see ax.ha.it.smsalarm.Alarm#updateAcknowledged() updateAcknowledged()
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
			alarm.updateAcknowledged(this.context);
			
			// Update alarm entry
			this.updateAlarm(alarm);
			
			// Log in debug purpose
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateLatestAlarmAcknowledged()", "Latest alarms acknowledge time has been updated");			
		} catch (android.database.CursorIndexOutOfBoundsException e) {
			// Log error
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":updateLatestAlarmAcknowledged()", "android.database.CursorIndexOutOfBoundsException occurred while getting alarm from database", e);
		}
	}
	
	/**
	 * To update latest <b><i>PRIMARY</i></b> <code>Alarm</code> entry's acknowledge time in database.
	 * 
	 * @parm primaryAlarmNumber Phone number of primary alarms sender as String.
	 * 
	 * @see #getAlarmsCount()
	 * @see #getAlarm(int)
	 * @see #updateAlarm(Alarm)
 	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
 	 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String, String, Throwable) logCat(LogPriorities, String, String, Throwable)
	 * @see ax.ha.it.smsalarm.Alarm ax.ha.it.smsalarm.Alarm
	 * @see ax.ha.it.smsalarm.Alarm#updateAcknowledged() updateAcknowledged()
	 * @see ax.ha.it.smsalarm.Alarm#getAlarmType() getAlarmType()
	 */
	public void updateLatestPrimaryAlarmAcknowledged() {				
		try {
			// Log in debug purpose
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateLatestPrimaryAlarmAcknowledged()", "Trying to update latest primary alarms acknowledge time in database");
			
			// Iterate through all alarms in database from the last one and down
			for (int i = this.getAlarmsCount(); i > 0; i--) {
				// Get entry(alarm) in database
				Alarm alarm = this.getAlarm(i);
				
				// If alarm type is primary we want to update it's acknowledge time
				if (alarm.getAlarmType().equals(AlarmTypes.PRIMARY)) {
					// Update alarms acknowledge time
					alarm.updateAcknowledged(this.context);	
					// Update alarm entry
					this.updateAlarm(alarm);
					
					// Log in debug purpose
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateLatestPrimaryAlarmAcknowledged()", "Latest primary alarms acknowledge time has been updated");	
					
					// Get out of the loop
					break;
				}
			}
		} catch (android.database.CursorIndexOutOfBoundsException e) {
			// Log error
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":updateLatestPrimaryAlarmAcknowledged()", "android.database.CursorIndexOutOfBoundsException occurred while getting alarm from database", e);
		}
	}
	
	/**
	 * To delete an existing <code>Alarm</code> entry in database. 
	 * It finds correct entry to delete on alarms <code>id</code>.
	 * 
	 * @param alarm <code>Alarm</code> to be deleted from database.
	 * 
 	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.Alarm ax.ha.it.smsalarm.Alarm
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
