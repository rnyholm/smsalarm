/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.alarm;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.base.Optional;

/**
 * Class representing an Alarm. Mainly holds data for an Alarm but also some help methods for setting and getting data.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.1beta
 */
public class Alarm implements Parcelable {
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
	private int id; 														// Unique id for this alarm
	private Date received; 													// Date when the alarm was received
	private String sender; 													// Sender of alarm(phone number)
	private String message; 												// Alarm message
	private String triggerText; 											// Text found in message triggering an alarm
	private Optional<Date> optionalAcknowledged = Optional.<Date> absent();	// Optional date when the alarm was acknowledged
	private AlarmType alarmType = AlarmType.UNDEFINED; 						// Indicating which kind of alarm this object is
	// @formatter:on

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
		// Store a date when this alarm was received
		received = new Date();

		this.sender = sender;
		this.message = message;

		if (triggerText.length() > 0) {
			this.triggerText = triggerText;
		} else {
			this.triggerText = "-";
		}

		// At this point alarm hasn't been acknowledged yet
		optionalAcknowledged = Optional.<Date> absent();
		this.alarmType = alarmType;
	}

	/**
	 * Creates a new instance of {@link Alarm}.
	 * 
	 * @param id
	 *            Id of alarm.
	 * @param received
	 *            Alarm received date and time as a {@link String} in milliseconds.
	 * @param sender
	 *            Sender of alarm.
	 * @param message
	 *            Alarm message.
	 * @param triggerText
	 *            Text in income message that triggered an alarm.
	 * @param acknowledged
	 *            Alarms optional acknowledge date and time as <code>String</code> in milliseconds.
	 * @param alarmType
	 *            Type of alarm.
	 */
	public Alarm(int id, String received, String sender, String message, String triggerText, String acknowledged, AlarmType alarmType) {
		this.id = id;
		this.sender = sender;
		this.message = message;
		this.triggerText = triggerText;
		this.alarmType = alarmType;

		// Should always exist a date received
		this.received = new Date(Long.parseLong(received));

		// Could exist but is not mandatory
		if ("-".equals(acknowledged)) {
			optionalAcknowledged = Optional.<Date> absent();
		} else {
			optionalAcknowledged = Optional.<Date> of(new Date(Long.parseLong(acknowledged)));
		}
	}

	/**
	 * To find out if this Alarm holds enough valid data to be shown in the applications widget.<br>
	 * Following data must be valid:<br>
	 * <ul>
	 * <li><b><i>Date and time received</i></b></li>
	 * <li><b><i>Sender</i></b></li>
	 * <li><b><i>Message</i></b></li>
	 * <li><b><i>Trigger text</i></b></li>
	 * <li><b><i>Alarm type</i></b></li>
	 * </ul>
	 * <p>
	 * To be valid each of the checked item must not be <code>null</code> and hold correct content.
	 * 
	 * @return <code>true</code> if this alarm holds enough data to be shown in the widget, else <code>false</code>.
	 */
	public boolean holdsValidInfo() {
		if (received != null && sender != null && sender.length() > 0 && message != null && message.length() > 0 && triggerText != null && triggerText.length() > 0 && !AlarmType.UNDEFINED.equals(alarmType)) {
			return true;
		}

		return false;
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
	 * To get date and time when this Alarm was received as a {@link String} according to the default {@link Locale}.
	 * 
	 * @return Date and time when this alarm was received as a <code>String</code>.
	 */
	public String getReceivedLocalized() {
		return DateFormat.getDateTimeInstance().format(received);
	}

	public String getReceivedForLog() {
		DateFormat dateTimeInstance = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());
		return dateTimeInstance.format(getReceived());
	}

	/**
	 * To get date and time when this alarm was received as a {@link Date} object.
	 * 
	 * @return Time and date when this Alarm was received as a <code>Date</code> object.
	 */
	public Date getReceived() {
		return received;
	}

	/**
	 * To get date and time when this Alarm was received as a {@link String} in milliseconds.<br>
	 * 
	 * @return Alarm's received time as a <code>String</code> in milliseconds.
	 */
	public String getReceivedMillisecs() {
		return String.valueOf(received.getTime());
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
	 * To get Alarm's alarm message.
	 * 
	 * @return Message of alarm.
	 */
	public String getMessage() {
		return message;
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
	 * To get date and time when this Alarm was acknowledged as a {@link String} according to the default {@link Locale}.
	 * 
	 * @return Alarm's acknowledge time as a <code>String</code>.
	 */
	public String getAcknowledgedLocalized() {
		if (optionalAcknowledged.isPresent()) {
			return DateFormat.getDateTimeInstance().format(optionalAcknowledged.get());
		}

		return "-";
	}

	/**
	 * To get date and time when this Alarm was acknowledged as a {@link String} in milliseconds.<br>
	 * If this Alarm doesn't contain any date and time acknowledged <b><i>-</i></b> is returned.
	 * 
	 * @return Alarm's acknowledge time as a <code>String</code> in milliseconds if present, else "-".
	 */
	public String getAcknowledgedMillisecs() {
		return optionalAcknowledged.isPresent() ? String.valueOf(optionalAcknowledged.get().getTime()) : "-";
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
	 * To update/set date and time when an Alarm was acknowledged. Date and time will be set to now.
	 * 
	 * @see #setAcknowledged(Date)
	 * @see #setAcknowledged(String)
	 */
	public void updateAcknowledged() {
		optionalAcknowledged = Optional.<Date> of(new Date());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeSerializable(received);
		dest.writeString(sender);
		dest.writeString(message);
		dest.writeString(triggerText);
		dest.writeSerializable(optionalAcknowledged);
		dest.writeInt(alarmType.ordinal());
	}

	@SuppressWarnings("unchecked")
	private void readFromParcelable(Parcel source) {
		id = source.readInt();
		received = (Date) source.readSerializable();
		sender = source.readString();
		message = source.readString();
		triggerText = source.readString();
		optionalAcknowledged = (Optional<Date>) source.readSerializable();
		alarmType = AlarmType.of(source.readInt());
	}

	public static final Parcelable.Creator<Alarm> CREATOR = new Parcelable.Creator<Alarm>() {
		@Override
		public Alarm createFromParcel(Parcel source) {
			return new Alarm(source);
		}

		@Override
		public Alarm[] newArray(int size) {
			return new Alarm[size];
		}
	};

	public Alarm(Parcel in) {
		readFromParcelable(in);
	}
}
