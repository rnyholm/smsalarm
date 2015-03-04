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
	//
	private Alarm alarm;
	private String yearReceived;
	private String monthReceived;

	public AlarmLogItem(String yearReceived, String monthReceived) {
		this.yearReceived = yearReceived;
		this.monthReceived = monthReceived;
	}

	public AlarmLogItem(Alarm alarm) {
		this.alarm = alarm;
	}

	public Alarm getAlarm() {
		return alarm;
	}

	public String getYearReceived() {
		return yearReceived;
	}

	public String getMonthReceived() {
		return monthReceived;
	}

	public boolean isSectionTitle() {
		return alarm == null && yearReceived != null && monthReceived != null;
	}
}
