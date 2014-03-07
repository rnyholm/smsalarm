/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import ax.ha.it.smsalarm.LogHandler.LogPriorities;

/**
 * Class representing an Alarm. Mainly holds data for an Alarm but also
 * some help methods for setting and getting data.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.2
 * @since 2.1beta
 */
public class Alarm {
	// Log tag string
	private final String LOG_TAG = getClass().getSimpleName();

	// Object for logging
	private LogHandler logger = LogHandler.getInstance();
	
	// Variables holding data for an alarm
	private int id;					// Unique id for this alarm
	private String received;		// Localized datetime when the alarm was received
	private String sender;			// Sender of alarm(e-mail or phone number)
	private String message;			// Alarm message
	private String triggerText;		// Text found in message triggering an alarm
	private String acknowledged;	// Localized datetime when the alarm was acknowledged
	private AlarmTypes alarmType = AlarmTypes.UNDEFINED; // Indicating which kind of alarm this object is
	
	/**
	 * To create a new empty Alarm object.
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	public Alarm() {
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":Alarm()", "A new empty Alarm object was created");
	}
	
	/**
	 * To create a new Alarm object.
	 * 
	 * @param sender Alarm sender as <code>String</code>
	 * @param message Alarm message as <code>String</code>
	 * @param triggerText Alarm triggerText as <code>String</code>
	 * @param alarmType Alarm type as <code>AlarmTypes</code>
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	public Alarm(String sender, String message, String triggerText, AlarmTypes alarmType) {
		// Create and store a localized timestamp, this depends on users locale and/or settings
		received = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
		this.sender = sender;
		this.message = message;
		if (!triggerText.isEmpty()) {
			this.triggerText = triggerText;
		} else {
			this.triggerText = "-";
		}
		acknowledged  = "-";
		this.alarmType = alarmType;
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":Alarm()", "A new Alarm object was created with following data: [" + toString() + "]");
	}
	
	/**
	 * To create a new Alarm object.
	 * 
	 * @param id Alarm id as <code>int</code>
	 * @param received Alarm received date and time as <code>String</code>
	 * @param sender Alarm sender as <code>String</code>
	 * @param message Alarm message as <code>String</code>
	 * @param triggerText Alarm triggerText as <code>String</code>
	 * @param acknowledged Alarm acknowledge date and time as <code>String</code>
	 * @param alarmType Alarm type as <code>AlarmTypes</code>
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	public Alarm(int id, String received, String sender, String message, String triggerText, String acknowledged, AlarmTypes alarmType) {
		this.id = id;
		this.received = received;
		this.sender = sender;
		this.message = message;
		this.triggerText = triggerText;
		this.acknowledged  = acknowledged;
		this.alarmType = alarmType;
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":Alarm()", "A new Alarm object was created with following data: [" + toString() + "]");
	}
	
	/**
	 * To indicate whether this Alarm object is empty or not. An Alarm object is defined empty if all member variables are empty.
	 * 
	 * @return <code>true</code> if this object is empty, else <code>false</code>
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	public boolean isEmpty() {
		// Check to see if the member variables are empty
		if(getId() == 0 && received.isEmpty() && sender.isEmpty() && message.isEmpty() && triggerText.isEmpty() && acknowledged.isEmpty() && alarmType.equals(AlarmTypes.UNDEFINED)) {
			// Log in debug purpose
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":isEmpty()", "This Alarm object is empty, returning true");
			return true;
		} else {
			// Log in debug purpose
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":isEmpty()", "This Alarm object is not empty, returning false");
			return false;
		}
	}
	
	/**
	 * To get Alarm's id.
	 * 
	 * @return Alarm id as <code>int</code>
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	public int getId() {
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getId()", "Returning alarm id:\"" + id + "\"");
		return id;
	}

	/**
	 * To set Alarm's id.
	 * 
	 * @param id Alarm id as <code>int</code>
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	public void setId(int id) {
		this.id = id;
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setId()", "Alarm id has been set, id is:\"" + id + "\"");
	}

	/**
	 * To get date and time when an Alarm was received.
	 * 
	 * @return Datetime when an Alarm was received as <code>String</code>
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	public String getReceived() {
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getReceived()", "Returning alarm received date and time:\"" + received + "\"");
		return received;
	}

	/**
	 * To set date and time when an Alarm was received.
	 * 
	 * @param date Date when an Alarm was received as <code>Date</code>
	 * 
	 * @see #updateReceived()
	 * @see #setReceived(String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	public void setReceived(Date date) {
		// Create and store a localized timestamp, this depends on users locale and/or settings
		received = DateFormat.getDateTimeInstance().format(date);
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setReceived()", "Alarm received date and time has been set to:\"" + received + "\"");
	}
	
	/**
	 * To set date and time when an Alarm was received.
	 * 
	 * @param received Date and time when an Alarm was received as <code>String</code>
	 * 
	 * @see #updateReceived()
	 * @see #setReceived(Date)
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	public void setReceived(String received) {
		// Create and store a localized timestamp, this depends on users locale and/or settings
		this.received = received;
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setReceived()", "Alarm received date and time has been set to:\"" + received + "\"");
	}

	/**
	 * To get Alarm's sender.
	 * 
	 * @return Sender as <code>String</code>
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	public String getSender() {
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getSender()", "Returning alarm sender:\"" + sender + "\"");
		return sender;
	}

	/**
	 * To set Alarm's sender.
	 * 
	 * @param sender Sender as <code>String</code>
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	public void setSender(String sender) {
		this.sender = sender;
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setSender()", "Alarm sender has been set to:\"" + sender + "\"");
	}

	/**
	 * To get Alarm's alarm message.
	 * 
	 * @return Alarm message as <code>String</code>
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	public String getMessage() {
		// Log in debug purpose
		this.logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getMessage()", "Returning alarm message:\"" + message + "\"");
		return message;
	}

	/**
	 * To set Alarm's alarm message.
	 * 
	 * @param message Alarm message as <code>String</code>
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	public void setMessage(String message) {
		this.message = message;
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setMessage()", "Alarm message has been set to:\"" + message + "\"");
	}

	/**
	 * To get Alarm's trigger text.
	 * 
	 * @return Alarm trigger text as <code>String</code>
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	public String getTriggerText() {
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getTriggerText()", "Returning alarm trigger text:\"" + triggerText + "\"");
		return triggerText;
	}

	/**
	 * To set Alarm's trigger text.
	 * 
	 * @param message Alarm trigger text as <code>String</code>
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	public void setTriggerText(String triggerText) {
		this.triggerText = triggerText;
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setTriggerText()", "Alarm trigger text has been set to:\"" + triggerText + "\"");
	}

	/**
	 * To get Alarm's acknowledge time.
	 * 
	 * @return Acknowledge time as <code>String</code>
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	public String getAcknowledged() {
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getAcknowledged()", "Returning alarm acknowledge date and time:\"" + acknowledged + "\"");
		return acknowledged;
	}
	
	/**
	 * To set date and time when an Alarm was acknowledged.
	 * 
	 * @param context Context
	 * @param date Date when Alarm was acknowledged as <code>Date</code>
	 * 
	 * @see #updateAcknowledged()
	 * @see #setAcknowledged(String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	public void setAcknowledged(Context context, Date date) {
		// Create and store a localized timestamp, this depends on users locale and/or settings
		acknowledged = getLocalizedHHMMStamp(context, date);
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setAcknowledged()", "Alarm acknowledge date and time has been set to:\"" + acknowledged + "\"");
	}
	
	/**
	 * To set date and time when an Alarm was acknowledged.
	 * 
	 * @param acknowledged Date and time when Alarm was acknowledged as <code>String</code>
	 * 
	 * @see #updateAcknowledged()
	 * @see #setAcknowledged(Date)
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	public void setAcknowledged(String acknowledged) {
		// Create and store a localized timestamp, this depends on users locale and/or settings
		this.acknowledged = acknowledged;
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setAcknowledged()", "Alarm acknowledge date and time has been set to:\"" + acknowledged + "\"");
	}
	
	/**
	 * To update/set date and time when an Alarm was received. 
	 * Date and time will be set to now.
	 * 
	 * @see #setReceived(Date)
	 * @see #setReceived(String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	public void updateReceived() {
		// Create and store a localized timestamp, this depends on users locale and/or settings
		received = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());	
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateReceived()", "Alarm received date and time has been updated to:\"" + received + "\"");
	}

	/**
	 * To get Alarm's type.
	 * 
	 * @return Alarm type as <code>AlarmTypes</code>
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	public AlarmTypes getAlarmType() {
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getAlarmType()", "Returning alarmType:\"" + alarmType.toString() + "\"");
		return alarmType;
	}

	/**
	 * To set Alarm's type.
	 * 
	 * @param alarmType Alarm type as <code>AlarmTypes</code>
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	public void setAlarmType(AlarmTypes alarmType) {
		// Set alarm type
		this.alarmType = alarmType;
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getId()", "Alarm type has been set to:\"" + alarmType.toString() + "\"");
	}

	/**
	 * To update/set date and time when an Alarm was acknowledged. 
	 * Date and time will be set to now.
	 * 
	 * @param context Context
	 * 
	 * @see #setAcknowledged(Date)
	 * @see #setAcknowledged(String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */	
	public void updateAcknowledged(Context context) {
		// Create and store a localized timestamp, this depends on users locale and/or settings
		acknowledged = getLocilizedHHMMStamp(context);	
		// Log in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateAcknowledged()", "Alarm acknowledge date and time has been updated to:\"" + acknowledged + "\"");
	}
	
	/**
	 * To figure out if <b><i>24h</i></b> or <b><i>12h</i></b> clock is beeing used on device.
	 * A timestamp in format:<br>
	 * <b><i>hh:mm</i></b><br>
	 * will be created and returned according to <b><i>current Date</i></b> and clock format
	 * beeing used on device.
	 * 
	 * @param context Context is needed to figure out clock format.
	 * 
	 * @return Timestamp in one of following format; <b><i>hh:mm</i></b> or <b><i>hh:mm am/pm</i></b>.
	 * 
	 * @see #getLocalizedHHMMStamp(Context, Date)
	 */
	private String getLocilizedHHMMStamp(Context context) {
		return getLocalizedHHMMStamp(context, Calendar.getInstance().getTime());
	}
	
	/**
	 * To figure out if <b><i>24h</i></b> or <b><i>12h</i></b> clock is being used on device.
	 * A timestamp in format:<br>
	 * <b><i>hh:mm</i></b><br>
	 * will be created and returned according to <b><i>given Date</i></b> and clock format
	 * being used on device.
	 * 
	 * @param context Context is needed to figure out clock format.
	 * @param now <code>Date</code> to pick create timestamp from.
	 * 
	 * @return Timestamp in one of following format; <b><i>hh:mm</i></b> or <b><i>hh:mm am/pm</i></b>.
	 *
	 * @see #getLocilizedHHMMStamp(Context)
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	@SuppressLint("SimpleDateFormat")
	private String getLocalizedHHMMStamp(Context context, Date now) {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getLocalizedHHMMStamp()", "Resolving clock format beeing used on device and getting timestamp(hh:mm) in correct format");
		
		// Different formatters for 12 and 24 hour timestamps
		SimpleDateFormat formatter24 = new SimpleDateFormat("HH:mm");
		SimpleDateFormat formatter12 = new SimpleDateFormat("K:mm a");
				
		// According to users preferences the OS clock is displayed in 24 hour format
		if (android.text.format.DateFormat.is24HourFormat(context)) {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getLocalizedHHMMStamp()", "24 hour clock is used on device, formatting timestamp according to that");
			return formatter24.format(now);
		} else {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getLocalizedHHMMStamp()", "12 hour clock is used on device, formatting timestamp according to that");
			return formatter12.format(now);
		}
	}

	/**
	 * Alarm class overridden implementation of toString()
	 */
	@Override
	public String toString() {
		return "id:\"" + Integer.toString(id) + "\", received:\"" + received + "\", sender:\"" + sender + "\", message:\"" + message + "\", trigger text:\"" + triggerText + "\", acknowledged:\"" + acknowledged + "\" and alarmType:\"" + alarmType.toString() + "\"";
	}
}
