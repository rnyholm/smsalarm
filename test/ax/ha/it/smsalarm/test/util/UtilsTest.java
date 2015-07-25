/**
 * Copyright (c) 2015 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.test.util;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import ax.ha.it.smsalarm.util.Utils;

/**
 * Test class for {@link Utils} and it's methods.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class UtilsTest extends TestCase {

	private List<String> foxList;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		foxList = Arrays.asList("The", "quick", "Brown", "fOX", "jumpS", "oVEr", "the", "lazy", "dog");
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		foxList = null;
	}

	public void testExistsInIgnoreCases() {
		assertTrue(Utils.existsInIgnoreCases("JuMpS", foxList));
		assertTrue(Utils.existsInIgnoreCases("LAzy", foxList));
		assertTrue(Utils.existsInIgnoreCases("doG", foxList));
		assertTrue(Utils.existsInIgnoreCases("QuicK", foxList));
		assertFalse(Utils.existsInIgnoreCases("not", foxList));
		assertFalse(Utils.existsInIgnoreCases("Thee", foxList));
		assertFalse(Utils.existsInIgnoreCases("12k7f", foxList));
		assertFalse(Utils.existsInIgnoreCases("FiSH", foxList));
	}

	public void testExistsInConsiderCases() {
		assertTrue(Utils.existsInConsiderCases("The", foxList));
		assertTrue(Utils.existsInConsiderCases("fOX", foxList));
		assertTrue(Utils.existsInConsiderCases("oVEr", foxList));
		assertTrue(Utils.existsInConsiderCases("Brown", foxList));
		assertFalse(Utils.existsInConsiderCases("brown", foxList));
		assertFalse(Utils.existsInConsiderCases("jumps", foxList));
		assertFalse(Utils.existsInConsiderCases("fOx", foxList));
		assertFalse(Utils.existsInConsiderCases("LAzy", foxList));

	}

	public void testGetFileName() {
		assertEquals("", Utils.getFileName(null));
		assertEquals("", Utils.getFileName(""));
		assertEquals("ledarlarm.wma", Utils.getFileName("/storage/sdcard0/download/ledarlarm.wma"));
		assertEquals("ledar larm.wma", Utils.getFileName("/storage/sdcard0/download/ledar larm.wma"));
		assertEquals("le*[darlarm.wma", Utils.getFileName("/storage/sdcard0/download/le*[darlarm.wma"));
		assertEquals("åöä1234_.wma", Utils.getFileName("/storage/sdcard0/download/åöä1234_.wma"));
		assertEquals("le-dar-la_rm.wma", Utils.getFileName("/storage/sdcard0/download/le-dar-la_rm.wma"));
		assertEquals("ledarlarm", Utils.getFileName("/storage/sdcard0/download/ledarlarm"));
		assertEquals("ledarlarm.wma", Utils.getFileName("/storage/download/ledarlarm.wma"));
		assertEquals("ledarlarm.wma", Utils.getFileName("/storage/ledarlarm.wma"));
		assertEquals("ledarlarm.wma", Utils.getFileName("/ledarlarm.wma"));
		assertEquals("ledarlarm.wma", Utils.getFileName("ledarlarm.wma"));
	}

	public void testGetBaseFileName() {
		assertEquals("", Utils.getBaseFileName(null));
		assertEquals("", Utils.getBaseFileName(""));
		assertEquals("ledarlarm", Utils.getBaseFileName("/storage/sdcard0/download/ledarlarm.wma"));
		assertEquals("ledar larm", Utils.getBaseFileName("/storage/sdcard0/download/ledar larm.wma"));
		assertEquals("le*[darlarm", Utils.getBaseFileName("/storage/sdcard0/download/le*[darlarm.wma"));
		assertEquals("åöä1234_", Utils.getBaseFileName("/storage/sdcard0/download/åöä1234_.wma"));
		assertEquals("le-dar-la_rm", Utils.getBaseFileName("/storage/sdcard0/download/le-dar-la_rm.wma"));
		assertEquals("ledarlarm", Utils.getBaseFileName("/storage/sdcard0/download/ledarlarm"));
		assertEquals("ledarlarm", Utils.getBaseFileName("/storage/download/ledarlarm.wma"));
		assertEquals("ledarlarm", Utils.getBaseFileName("/storage/ledarlarm.wma"));
		assertEquals("ledarlarm", Utils.getBaseFileName("/ledarlarm.wma"));
		assertEquals("ledarlarm", Utils.getBaseFileName("ledarlarm.wma"));
	}

	public void testRemoveSpaces() {
		assertEquals("", Utils.removeSpaces(null));
		assertEquals("", Utils.removeSpaces(""));
		assertEquals("+3584571234567", Utils.removeSpaces("+3584571234567"));
		assertEquals("+3584571234567", Utils.removeSpaces(" +358 4571234567"));
		assertEquals("+3584571234567", Utils.removeSpaces("+   35 845 712 3 4       5 6   7"));
		assertEquals("Justalittleteststringnow!", Utils.removeSpaces("Just a little test string now!"));
		assertEquals("Testingabitmooore,withsomeotherch4aract3rsalso!", Utils.removeSpaces(" Te sting a   bit m o o o re, with some other ch4aract3rs also!    "));
	}

	public void testCleanAlarmCentralAXMessage() {
		assertEquals("", Utils.cleanAlarmCentralAXMessage(null));
		assertEquals("", Utils.cleanAlarmCentralAXMessage(""));
		assertEquals("test", Utils.cleanAlarmCentralAXMessage("test"));
		assertEquals("Litet larm - Automatlarm vikingline lager(1682) Länsmanshägnan 7 jomala", Utils.cleanAlarmCentralAXMessage("Litet larm - Automatlarm vikingline lager(1682) Länsmanshägnan 7 jomala"));
		assertEquals("Litet larm - Automatlarm vikingline lager(1682) Länsmanshägnan 7 jomala", Utils.cleanAlarmCentralAXMessage("02.02.2012 23:55:40 2.5 Litet larm - Automatlarm vikingline lager(1682) Länsmanshägnan 7 jomala"));
	}

	public void testAdjustStringLength() {
		assertEquals("", Utils.adjustStringLength(null, 12));
		assertEquals("", Utils.adjustStringLength("testar", -1));
		assertEquals("", Utils.adjustStringLength(null, -5));
		assertEquals("testar", Utils.adjustStringLength("testar", 10));
		assertEquals("testar", Utils.adjustStringLength("testar", 6));
		assertEquals("tes", Utils.adjustStringLength("testar", 3));
		assertEquals("", Utils.adjustStringLength("testar", 0));
		assertEquals("testar ett lite ", Utils.adjustStringLength("testar ett lite längre nu", 16));
	}
}
