/**
 * Copyright (c) 2015 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.alarm.log.model;

import ax.ha.it.smsalarm.alarm.Alarm;
import ax.ha.it.smsalarm.alarm.log.adapter.AlarmLogItemAdapter;
import ax.ha.it.smsalarm.fragment.AlarmLogFragment;

/**
 * Class representing a alarm log item that's used in <b><i>Sms Alarm</i></b>'s <b><i>Alarm log</i></b>.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 * @see AlarmLogItemAdapter
 * @see AlarmLogFragment
 */
public class AlarmLogItem {
	// Alarm, year- and month received
	private Alarm alarm;
	private String yearReceived;
	private String monthReceived;

	/**
	 * Creates a new instance of {@link AlarmLogItem} with <b><i>year</i></b> and <b><i>month</i></b> when the alarms was received.
	 * <p>
	 * <b><i>Note. If the <code>AlarmLogItem</code> is created with this constructor it will be seen as a "section title" in the
	 * {@link AlarmLogFragment}</i></b>.
	 * 
	 * @param yearReceived
	 *            {@link String} representing the year when alarms was received.
	 * @param monthReceived
	 *            <code>String</code> representing the year when the alarms was received.
	 */
	public AlarmLogItem(String yearReceived, String monthReceived) {
		this.yearReceived = yearReceived;
		this.monthReceived = monthReceived;
	}

	/**
	 * Creates a new instance of {@link AlarmLogItem} with an {@link Alarm}.
	 * 
	 * @param alarm
	 *            <code>Alarm</code> within this <code>AlarmLogItem</code>.
	 */
	public AlarmLogItem(Alarm alarm) {
		this.alarm = alarm;
	}

	/**
	 * To get the alarm within this {@link AlarmLogItem}.
	 * 
	 * @return This <code>AlarmLogItem</code>'s <code>Alarm</code>.
	 */
	public Alarm getAlarm() {
		return alarm;
	}

	/**
	 * To get the year when the alarms was received.
	 * 
	 * @return Year when the alarms was received.
	 */
	public String getYearReceived() {
		return yearReceived;
	}

	/**
	 * To get the month when the alarms was received.
	 * 
	 * @return Month when the alarms was received.
	 */
	public String getMonthReceived() {
		return monthReceived;
	}

	/**
	 * To find out if this {@link AlarmLogItem} should be seen as a <b><i>Section Title</i></b>. It is seen as a section title if it doesn't contain
	 * any {@link Alarm} but holds <b><i>both</i></b> year and month received.
	 * 
	 * @return <code>true</code> if this <code>AlarmLogItem</code> should be seen as a section title, else <code>false</code>.
	 */
	public boolean isSectionTitle() {
		return alarm == null && yearReceived != null && monthReceived != null;
	}
}
