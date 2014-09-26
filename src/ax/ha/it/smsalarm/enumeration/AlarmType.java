/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.enumeration;

/**
 * Enumeration for different types of <b><i>Alarms</i></b>.
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
	 *            Numerical value that corresponds to a enumeration of <code>AlarmType</code>
	 * @return Corresponding <code>AlarmType</code> enumeration if it exists else <code>AlarmTypes.UNDEFINED</code>
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
