/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm;

/**
 * Enumeration for different types of alarms.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.1
 * @since 2.1
 */
public enum AlarmTypes {
	PRIMARY, SECONDARY, UNDEFINED;
	
	/**
	 * To get correct <code>AlarmTypes</code> from given <code>integer</code>.
	 * If any unsupported <code>integer</code> is given as parameter 
	 * <code>AlarmTypes.UNDEFINED</code> is returned.
	 * 
	 * @param alarmType Integer that corresponds to any enum of <code>AlarmTypes</code>
	 * 
	 * @return Corresponding <code>AlarmTypes</code> enum if it exists else <code>AlarmTypes.UNDEFINED</code>
	 */
	public static AlarmTypes of(int alarmType) {
	    switch (alarmType) {
		case (0):
			return PRIMARY;
		case (1):
			return SECONDARY;
		default:
			return UNDEFINED;
		}
	}
}	
