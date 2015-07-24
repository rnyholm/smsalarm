/**
 * Copyright (c) 2015 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.test.util;

import android.test.AndroidTestCase;
import ax.ha.it.smsalarm.util.InitializableString;

/**
 * Test class for {@link InitializableString} and it's methods.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class InitializableStringTest extends AndroidTestCase {

	public void testInitiailizableString() {
		InitializableString is = new InitializableString("testing");
		assertEquals("testing", is.getInitialValue());
		assertEquals("testing", is.getValue());

		is = new InitializableString("");
		assertEquals("", is.getInitialValue());
		assertEquals("", is.getValue());
	}

	public void testMethods() {
		InitializableString is = new InitializableString("Test");
		is.setValue("New test");
		assertEquals("Test", is.getInitialValue());
		assertEquals("New test", is.getValue());
	}
}
