/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import ax.ha.it.smsalarm.LogHandler.LogPriorities;

/**
 * Class representing an Alarm. Mainly holds data for an Alarm but also
 * some help methods for setting and getting data.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.1
 * @since 2.1beta
 * @date 2013-08-09
 */
public class Alarm {
	// Log tag string
	private final String LOG_TAG = "Alarm";

	// Object for logging
	private LogHandler logger = LogHandler.getInstance();
	
	// Variables holding data for an alarm
	int id;					// Unique id for this alarm
	String received;		// Localized datetime when the alarm was received
	String sender;			// Sender of alarm(e-mail or phone number)
	String message;			// Alarm message
	String acknowledged;	// Localized datetime when the alarm was acknowledged
	
	/**
	 * To create a new empty Alarm object.
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	public Alarm() {
		// Log in debug purpose
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":Alarm()", "A new empty Alarm object was created");
	}
	
	/**
	 * To create a new Alarm object.
	 * 
	 * @param sender Alarm sender as <code>String</code>
	 * @param message Alarm message as <code>String</code>
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	public Alarm(String sender, String message) {
		// Create and store a localized timestamp, this depends on users locale and/or settings
		this.received = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
		this.sender = sender;
		this.message = message;
		this.acknowledged  = "-";
		// Log in debug purpose
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":Alarm()", "A new Alarm object was created with following data: [" + this.toString() + "]");
	}
	
	/**
	 * To create a new Alarm object.
	 * 
	 * @param id Alarm id as <code>int</code>
	 * @param rececived Alarm received date and time as <code>String</code>
	 * @param sender Alarm sender as <code>String</code>
	 * @param message Alarm message as <code>String</code>
	 * @param acknowledged Alarm acknowledge date and time as <code>String</code>
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	public Alarm(int id, String received, String sender, String message, String acknowledged) {
		this.id = id;
		this.received = received;
		this.sender = sender;
		this.message = message;
		this.acknowledged  = acknowledged;
		// Log in debug purpose
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":Alarm()", "A new Alarm object was created with following data: [" + this.toString() + "]");
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
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":getId()", "Returning alarm id:\"" + this.id + "\"");
		return this.id;
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
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":setId()", "Alarm id has been set, id is:\"" + this.id + "\"");
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
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":getReceived()", "Returning alarm received date and time:\"" + this.received + "\"");
		return this.received;
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
		this.received = DateFormat.getDateTimeInstance().format(date);
		// Log in debug purpose
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":setReceived()", "Alarm received date and time has been set to:\"" + this.received + "\"");
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
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":setReceived()", "Alarm received date and time has been set to:\"" + this.received + "\"");
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
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":getSender()", "Returning alarm sender:\"" + this.sender + "\"");
		return this.sender;
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
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":setSender()", "Alarm sender has been set to:\"" + this.sender + "\"");
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
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":getMessage()", "Returning alarm message:\"" + this.message + "\"");
		return this.message;
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
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":getId()", "Alarm message has been set to:\"" + this.message + "\"");
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
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":getAcknowledged()", "Returning alarm acknowledge date and time:\"" + this.acknowledged + "\"");
		return this.acknowledged;
	}
	
	/**
	 * To set date and time when an Alarm was acknowledged.
	 * 
	 * @param date Date when Alarm was acknowledged as <code>Date</code>
	 * 
	 * @see #updateAcknowledged()
	 * @see #setAcknowledged(String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	public void setAcknowledged(Date date) {
		// Create and store a localized timestamp, this depends on users locale and/or settings
		this.acknowledged = DateFormat.getDateTimeInstance().format(date);
		// Log in debug purpose
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":setAcknowledged()", "Alarm acknowledge date and time has been set to:\"" + this.acknowledged + "\"");
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
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":setAcknowledged()", "Alarm acknowledge date and time has been set to:\"" + this.acknowledged + "\"");
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
		this.received = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());	
		// Log in debug purpose
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":updateReceived()", "Alarm received date and time has been updated to:\"" + this.received + "\"");
	}
	
	/**
	 * To update/set date and time when an Alarm was acknowledged. 
	 * Date and time will be set to now.
	 * 
	 * @see #setAcknowledged(Date)
	 * @see #setAcknowledged(String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */	
	public void updateAcknowledged() {
		// Create and store a localized timestamp, this depends on users locale and/or settings
		this.acknowledged = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());	
		// Log in debug purpose
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":updateAcknowledged()", "Alarm acknowledge date and time has been updated to:\"" + this.acknowledged + "\"");
	}

	/**
	 * Alarm class overridden implementation of toString()
	 */
	@Override
	public String toString() {
		return "id:\"" + Integer.toString(this.id) + "\", received:\"" + this.received + "\", sender:\"" + this.sender + "\", message:\"" + this.message + "\" and acknowledged:\"" + this.acknowledged + "\"";
	}
}
