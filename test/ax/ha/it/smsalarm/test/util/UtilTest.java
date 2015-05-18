/**
 * Copyright (c) 2015 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.test.util;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import ax.ha.it.smsalarm.util.Util;

/**
 * Test class for {@link Util} and it's methods.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class UtilTest extends TestCase {

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
		assertTrue(Util.existsInIgnoreCases("JuMpS", foxList));
		assertTrue(Util.existsInIgnoreCases("LAzy", foxList));
		assertTrue(Util.existsInIgnoreCases("doG", foxList));
		assertTrue(Util.existsInIgnoreCases("QuicK", foxList));
		assertFalse(Util.existsInIgnoreCases("not", foxList));
		assertFalse(Util.existsInIgnoreCases("Thee", foxList));
		assertFalse(Util.existsInIgnoreCases("12k7f", foxList));
		assertFalse(Util.existsInIgnoreCases("FiSH", foxList));
	}

	public void testExistsInConsiderCases() {
		assertTrue(Util.existsInConsiderCases("The", foxList));
		assertTrue(Util.existsInConsiderCases("fOX", foxList));
		assertTrue(Util.existsInConsiderCases("oVEr", foxList));
		assertTrue(Util.existsInConsiderCases("Brown", foxList));
		assertFalse(Util.existsInConsiderCases("brown", foxList));
		assertFalse(Util.existsInConsiderCases("jumps", foxList));
		assertFalse(Util.existsInConsiderCases("fOx", foxList));
		assertFalse(Util.existsInConsiderCases("LAzy", foxList));

	}

	public void testGetFileName() {
		assertEquals("", Util.getFileName(null));
		assertEquals("", Util.getFileName(""));
		assertEquals("ledarlarm.wma", Util.getFileName("/storage/sdcard0/download/ledarlarm.wma"));
		assertEquals("ledar larm.wma", Util.getFileName("/storage/sdcard0/download/ledar larm.wma"));
		assertEquals("le*[darlarm.wma", Util.getFileName("/storage/sdcard0/download/le*[darlarm.wma"));
		assertEquals("åöä1234_.wma", Util.getFileName("/storage/sdcard0/download/åöä1234_.wma"));
		assertEquals("le-dar-la_rm.wma", Util.getFileName("/storage/sdcard0/download/le-dar-la_rm.wma"));
		assertEquals("ledarlarm", Util.getFileName("/storage/sdcard0/download/ledarlarm"));
		assertEquals("ledarlarm.wma", Util.getFileName("/storage/download/ledarlarm.wma"));
		assertEquals("ledarlarm.wma", Util.getFileName("/storage/ledarlarm.wma"));
		assertEquals("ledarlarm.wma", Util.getFileName("/ledarlarm.wma"));
		assertEquals("ledarlarm.wma", Util.getFileName("ledarlarm.wma"));
	}

	public void testGetBaseFileName() {
		assertEquals("", Util.getBaseFileName(null));
		assertEquals("", Util.getBaseFileName(""));
		assertEquals("ledarlarm", Util.getBaseFileName("/storage/sdcard0/download/ledarlarm.wma"));
		assertEquals("ledar larm", Util.getBaseFileName("/storage/sdcard0/download/ledar larm.wma"));
		assertEquals("le*[darlarm", Util.getBaseFileName("/storage/sdcard0/download/le*[darlarm.wma"));
		assertEquals("åöä1234_", Util.getBaseFileName("/storage/sdcard0/download/åöä1234_.wma"));
		assertEquals("le-dar-la_rm", Util.getBaseFileName("/storage/sdcard0/download/le-dar-la_rm.wma"));
		assertEquals("ledarlarm", Util.getBaseFileName("/storage/sdcard0/download/ledarlarm"));
		assertEquals("ledarlarm", Util.getBaseFileName("/storage/download/ledarlarm.wma"));
		assertEquals("ledarlarm", Util.getBaseFileName("/storage/ledarlarm.wma"));
		assertEquals("ledarlarm", Util.getBaseFileName("/ledarlarm.wma"));
		assertEquals("ledarlarm", Util.getBaseFileName("ledarlarm.wma"));
	}

	public void testRemoveSpaces() {
		assertEquals("", Util.removeSpaces(null));
		assertEquals("", Util.removeSpaces(""));
		assertEquals("+3584571234567", Util.removeSpaces("+3584571234567"));
		assertEquals("+3584571234567", Util.removeSpaces(" +358 4571234567"));
		assertEquals("+3584571234567", Util.removeSpaces("+   35 845 712 3 4       5 6   7"));
		assertEquals("Justalittleteststringnow!", Util.removeSpaces("Just a little test string now!"));
		assertEquals("Testingabitmooore,withsomeotherch4aract3rsalso!", Util.removeSpaces(" Te sting a   bit m o o o re, with some other ch4aract3rs also!    "));
	}

	public void testCleanAlarmCentralAXMessage() {
		assertEquals("", Util.cleanAlarmCentralAXMessage(null));
		assertEquals("", Util.cleanAlarmCentralAXMessage(""));
		assertEquals("test", Util.cleanAlarmCentralAXMessage("test"));
		assertEquals("Litet larm - Automatlarm vikingline lager(1682) Länsmanshägnan 7 jomala", Util.cleanAlarmCentralAXMessage("Litet larm - Automatlarm vikingline lager(1682) Länsmanshägnan 7 jomala"));
		assertEquals("Litet larm - Automatlarm vikingline lager(1682) Länsmanshägnan 7 jomala", Util.cleanAlarmCentralAXMessage("02.02.2012 23:55:40 2.5 Litet larm - Automatlarm vikingline lager(1682) Länsmanshägnan 7 jomala"));
	}

	public void testAdjustStringLength() {
		assertEquals("", Util.adjustStringLength(null, 12));
		assertEquals("", Util.adjustStringLength("testar", -1));
		assertEquals("", Util.adjustStringLength(null, -5));
		assertEquals("testar", Util.adjustStringLength("testar", 10));
		assertEquals("testar", Util.adjustStringLength("testar", 6));
		assertEquals("tes", Util.adjustStringLength("testar", 3));
		assertEquals("", Util.adjustStringLength("testar", 0));
		assertEquals("testar ett lite ", Util.adjustStringLength("testar ett lite längre nu", 16));
	}
}
