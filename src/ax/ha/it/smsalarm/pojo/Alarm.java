/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.pojo;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;

/**
 * Class representing an Alarm. Mainly holds data for an Alarm but also some help methods for setting and getting data.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.1beta
 */
public class Alarm {
	/**
	 * Enumeration for then different types of <b><i>Alarms</i></b>.
	 * 
	 * @author Robert Nyholm <robert.nyholm@aland.net>
	 * @version 2.3.1
	 * @since 2.1
	 */
	public enum AlarmType {
		// @formatter:off
		PRIMARY, 
		SECONDARY, 
		UNDEFINED;
		// @formatter:on

		/**
		 * To resolve the correct {@link AlarmType} from given numerical value. If an unsupported value is given as parameter a enumeration of type
		 * <code>AlarmTypes.UNDEFINED</code> is returned.
		 * 
		 * @param value
		 *            Numerical value that corresponds to a enumeration of AlarmType.
		 * @return Corresponding AlarmType enumeration if it exists else <code>AlarmTypes.UNDEFINED</code>.
		 */
		public static AlarmType of(int value) {
			switch (value) {
				case (0):
					return PRIMARY;
				case (1):
					return SECONDARY;
				default:
					return UNDEFINED;
			}
		}
	}

	// @formatter:off
	// Variables holding data for an alarm
	private int id; 									// Unique id for this alarm
	private String received; 							// Localized DateTime when the alarm was received
	private String sender; 								// Sender of alarm(phone number)
	private String message; 							// Alarm message
	private String triggerText; 						// Text found in message triggering an alarm
	private String acknowledged; 						// Localized DateTime when the alarm was acknowledged
	private AlarmType alarmType = AlarmType.UNDEFINED; 	// Indicating which kind of alarm this object is
	// @formatter:on

	/**
	 * Creates a new instance of {@link Alarm}.
	 */
	public Alarm() {
		// Just empty...
	}

	/**
	 * Creates a new instance of {@link Alarm}.
	 * 
	 * @param sender
	 *            Sender of alarm.
	 * @param message
	 *            Alarm message.
	 * @param triggerText
	 *            Text in income message that triggered an alarm.
	 * @param alarmType
	 *            Type of alarm.
	 */
	public Alarm(String sender, String message, String triggerText, AlarmType alarmType) {
		// Create and store a localized TimeStamp, this depends on users locale and/or settings
		received = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
		this.sender = sender;
		this.message = message;

		if (triggerText.length() != 0) {
			this.triggerText = triggerText;
		} else {
			this.triggerText = "-";
		}

		acknowledged = "-";
		this.alarmType = alarmType;
	}

	/**
	 * Creates a new instance of {@link Alarm}.
	 * 
	 * @param id
	 *            Id of alarm.
	 * @param received
	 *            Alarm received date and time.
	 * @param sender
	 *            Sender of alarm.
	 * @param message
	 *            Alarm message.
	 * @param triggerText
	 *            Text in income message that triggered an alarm.
	 * @param acknowledged
	 *            Alarm acknowledge date and time.
	 * @param alarmType
	 *            Type of alarm.
	 */
	public Alarm(int id, String received, String sender, String message, String triggerText, String acknowledged, AlarmType alarmType) {
		this.id = id;
		this.received = received;
		this.sender = sender;
		this.message = message;
		this.triggerText = triggerText;
		this.acknowledged = acknowledged;
		this.alarmType = alarmType;
	}

	/**
	 * To indicate whether this Alarm is empty or not. An alarm is defined empty if <b><i>all</i></b> member variables are empty.
	 * 
	 * @return <code>true</code> if this object is empty, else <code>false</code>.
	 */
	public boolean isEmpty() {
		// Check to see if the member variables are empty
		if (getId() == 0 && (received.length() == 0) && (sender.length() == 0) && (message.length() == 0) && (triggerText.length() == 0) && (acknowledged.length() == 0) && (alarmType == AlarmType.UNDEFINED)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * To get Alarm's id.
	 * 
	 * @return Id of alarm.
	 */
	public int getId() {
		return id;
	}

	/**
	 * To set Alarm's id.
	 * 
	 * @param id
	 *            Id of alarm.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * To get date and time when this Alarm was received.
	 * 
	 * @return Date and time when this alarm was received.
	 */
	public String getReceived() {
		return received;
	}

	/**
	 * To set date and time when this Alarm was received.
	 * 
	 * @param date
	 *            Date when this Alarm was received.
	 * @see #updateReceived()
	 * @see #setReceived(String)
	 */
	public void setReceived(Date date) {
		// Create and store a localized TimeStamp, this depends on users locale and/or settings
		received = DateFormat.getDateTimeInstance().format(date);
	}

	/**
	 * To set date and time when this Alarm was received.
	 * 
	 * @param received
	 *            Date and time when this Alarm was received.
	 * @see #updateReceived()
	 * @see #setReceived(Date)
	 */
	public void setReceived(String received) {
		// Create and store a localized TimeStamp, this depends on users locale and/or settings
		this.received = received;
	}

	/**
	 * To get Alarm's sender.
	 * 
	 * @return Sender of alarm.
	 */
	public String getSender() {
		return sender;
	}

	/**
	 * To set Alarm's sender.
	 * 
	 * @param sender
	 *            Sender of alarm.
	 */
	public void setSender(String sender) {
		this.sender = sender;
	}

	/**
	 * To get Alarm's alarm message.
	 * 
	 * @return Message of alarm.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * To set Alarm's message.
	 * 
	 * @param message
	 *            Alarms message.
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * To get Alarm's trigger text.
	 * 
	 * @return Alarms trigger text.
	 */
	public String getTriggerText() {
		return triggerText;
	}

	/**
	 * To set Alarm's trigger text.
	 * 
	 * @param triggerText
	 *            Trigger text of alarm.
	 */
	public void setTriggerText(String triggerText) {
		this.triggerText = triggerText;
	}

	/**
	 * To get Alarm's acknowledge time.
	 * 
	 * @return Alarm's acknowledge time.
	 */
	public String getAcknowledged() {
		return acknowledged;
	}

	/**
	 * To set date and time when an Alarm was acknowledged.
	 * 
	 * @param context
	 *            Context.
	 * @param date
	 *            Date when Alarm was acknowledged.
	 * @see #updateAcknowledged()
	 * @see #setAcknowledged(String)
	 */
	public void setAcknowledged(Context context, Date date) {
		// Create and store a localized TimeStamp, this depends on users locale and/or settings
		acknowledged = DateFormat.getDateTimeInstance().format(date);
	}

	/**
	 * To set date and time when an Alarm was acknowledged.
	 * 
	 * @param acknowledged
	 *            Date when Alarm was acknowledged.
	 * @see #updateAcknowledged()
	 * @see #setAcknowledged(Date)
	 */
	public void setAcknowledged(String acknowledged) {
		// Create and store a localized TimeStamp, this depends on users locale and/or settings
		this.acknowledged = acknowledged;
	}

	/**
	 * To update/set date and time when an Alarm was received. Date and time will be set to now.
	 * 
	 * @see #setReceived(Date)
	 * @see #setReceived(String)
	 */
	public void updateReceived() {
		// Create and store a localized TimeStamp, this depends on users locale and/or settings
		received = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
	}

	/**
	 * To get Alarm's type.
	 * 
	 * @return Type of alarm.
	 */
	public AlarmType getAlarmType() {
		return alarmType;
	}

	/**
	 * To set Alarm's type.
	 * 
	 * @param alarmType
	 *            Type of alarm.
	 */
	public void setAlarmType(AlarmType alarmType) {
		// Set alarm type
		this.alarmType = alarmType;
	}

	/**
	 * To update/set date and time when an Alarm was acknowledged. Date and time will be set to now.
	 * 
	 * @see #setAcknowledged(Date)
	 * @see #setAcknowledged(String)
	 */
	public void updateAcknowledged() {
		// Create and store a localized TimeStamp, this depends on users locale and/or settings
		acknowledged = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
	}
}
