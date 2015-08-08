/**
 * Copyright (c) 2015 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.test.receiver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.test.AndroidTestCase;
import ax.ha.it.smsalarm.alarm.Alarm;
import ax.ha.it.smsalarm.alarm.Alarm.AlarmType;
import ax.ha.it.smsalarm.handler.DatabaseHandler;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.DataType;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.PrefKey;
import ax.ha.it.smsalarm.receiver.SmsReceiver;
import ax.ha.it.smsalarm.util.DebugUtils;

/**
 * Test class for {@link SmsReceiver}, but some functionality and methods will also be tested for the {@link DatabaseHandler},
 * {@link SharedPreferencesHandler} and the {@link Alarm} classes. However those classes are <b><i>implicitly</i></b> being tested and should
 * <b><i>not</i></b> be seen as full test cases.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class SmsReceiverTest extends AndroidTestCase {
	// Handlers for shared preferences and database
	private SharedPreferencesHandler prefHandler;
	private DatabaseHandler databaseHandler;

	// Receiver needed for test and alarm for evaluation
	private SmsReceiver receiver;
	private Alarm alarm;

	// Context is also needed
	private Context context;

	// Need to store copies of the shared preferences used by the SmsReceiver for later restoration
	private List<String> originalPrimarySmsNumbers = new ArrayList<String>();
	private List<String> originalSecondarySmsNumbers = new ArrayList<String>();
	private List<String> originalPrimaryFreeTexts = new ArrayList<String>();
	private List<String> originalSecondaryFreeTexts = new ArrayList<String>();
	private List<String> originalPrimaryRegexs = new ArrayList<String>();
	private List<String> originalSecondaryRegexs = new ArrayList<String>();
	private boolean originalEnableSmsAlarm = false;

	// Must store number of received alarms from the database in order to be able to delete the mocked alarms, also the current alarms count for
	// comparison
	private int originalAlarmsCount = 0;
	private int currentAlarmsCount = 0;

	@SuppressWarnings("unchecked")
	@Override
	protected void setUp() throws Exception {
		// Get handlers instance
		prefHandler = SharedPreferencesHandler.getInstance();

		// Get the context
		context = getContext();

		databaseHandler = new DatabaseHandler(context);
		receiver = new SmsReceiver();

		// Fetch the original shared preferences used by the SmsReceiver
		originalPrimarySmsNumbers = (List<String>) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_NUMBERS_KEY, DataType.LIST, context);
		originalSecondarySmsNumbers = (List<String>) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_NUMBERS_KEY, DataType.LIST, context);
		originalPrimaryFreeTexts = (List<String>) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_FREE_TEXTS_KEY, DataType.LIST, context);
		originalSecondaryFreeTexts = (List<String>) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_FREE_TEXTS_KEY, DataType.LIST, context);
		originalPrimaryRegexs = (List<String>) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_REGULAR_EXPRESSIONS_KEY, DataType.LIST, context);
		originalSecondaryRegexs = (List<String>) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_REGULAR_EXPRESSIONS_KEY, DataType.LIST, context);
		originalEnableSmsAlarm = (Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.ENABLE_SMS_ALARM_KEY, DataType.BOOLEAN, context, true);

		// Get the number of alarms in the database
		originalAlarmsCount = databaseHandler.getAlarmsCount();

		// Store the test data to the shared preferences
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_NUMBERS_KEY, Arrays.asList("11111", "22222", "33333"), context);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_NUMBERS_KEY, Arrays.asList("44444", "55555", "66666"), context);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_FREE_TEXTS_KEY, Arrays.asList("Large", "Fire", "test"), context);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_FREE_TEXTS_KEY, Arrays.asList("Small", "Alarm", "quickly"), context);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_REGULAR_EXPRESSIONS_KEY, Arrays.asList("\\d+", "\\w+[@]\\w+[.]\\w+", "\\d{2}[:]\\d{2}"), context);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_REGULAR_EXPRESSIONS_KEY, Arrays.asList("w{3}[.]\\w+[.]\\w+", "small_firealert"), context);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.ENABLE_SMS_ALARM_KEY, true, context);
	}

	@Override
	protected void tearDown() throws Exception {
		// Restore the shared preferences
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_NUMBERS_KEY, originalPrimarySmsNumbers, context);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_NUMBERS_KEY, originalSecondarySmsNumbers, context);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_FREE_TEXTS_KEY, originalPrimaryFreeTexts, context);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_FREE_TEXTS_KEY, originalSecondaryFreeTexts, context);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_REGULAR_EXPRESSIONS_KEY, originalPrimaryRegexs, context);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_REGULAR_EXPRESSIONS_KEY, originalSecondaryRegexs, context);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.ENABLE_SMS_ALARM_KEY, originalEnableSmsAlarm, context);

		// Remove the test alarms from database
		while (originalAlarmsCount < databaseHandler.getAlarmsCount()) {
			databaseHandler.deleteAlarm(databaseHandler.fetchAlarm(databaseHandler.getAlarmsCount()));
		}

		// Nullify the handlers and receiver
		prefHandler = null;
		databaseHandler = null;
		receiver = null;
	}

	/**
	 * To test alarm triggering of an {@link AlarmType#PRIMARY} alarm. These alarms can be triggered both on phone number, free text(s) and regular
	 * expression matching in message. All cases will be tested within this case.
	 */
	public void testOnReceiveTriggerPrimary() {
		// Case 1 - Trigger primary on number
		// Get current alarms count for later evaluation
		currentAlarmsCount = databaseHandler.getAlarmsCount();

		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("11111", "lorem ipsum"));

		// Fetch the latest alarm for further comparison
		alarm = databaseHandler.fetchLatestAlarm();

		// Check the relevant stuff
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("11111", alarm.getSender());
		assertEquals("-", alarm.getTriggerText());
		assertEquals("lorem ipsum", alarm.getMessage());

		// Case 2 - Trigger primary on number and free text
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("22222", "Do some tests of lArge alarm now"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("22222", alarm.getSender());
		assertEquals("lArge", alarm.getTriggerText());
		assertEquals("Do some tests of lArge alarm now", alarm.getMessage());

		// Case 3 - Trigger primary on number and regular expression
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("33333", "Do some tests with mail(testing@test.com)"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("33333", alarm.getSender());
		assertEquals("testing@test.com", alarm.getTriggerText());
		assertEquals("Do some tests with mail(testing@test.com)", alarm.getMessage());

		// Case 3 - Trigger primary on number, free text and regular expression
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("11111", "Do some tests with mail(testing@test.com) and of large alarm"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("11111", alarm.getSender());
		assertEquals("large, testing@test.com", alarm.getTriggerText());
		assertEquals("Do some tests with mail(testing@test.com) and of large alarm", alarm.getMessage());

		// Case 4 - Trigger primary on free text
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("00000", "Fire in apartment"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("00000", alarm.getSender());
		assertEquals("Fire", alarm.getTriggerText());
		assertEquals("Fire in apartment", alarm.getMessage());

		// Case 5- Trigger primary on free texts
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("28463800", "Testing large fire test"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("28463800", alarm.getSender());
		assertEquals("large, fire, test", alarm.getTriggerText());
		assertEquals("Testing large fire test", alarm.getMessage());

		// Case 6 - Trigger primary on number and free texts
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("11111", "Testing test large fire"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("11111", alarm.getSender());
		assertEquals("large, fire, test", alarm.getTriggerText());
		assertEquals("Testing test large fire", alarm.getMessage());

		// Case 7 - Trigger primary on regular expression
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("00000", "Bigfire in apartment 09374"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("00000", alarm.getSender());
		assertEquals("09374", alarm.getTriggerText());
		assertEquals("Bigfire in apartment 09374", alarm.getMessage());

		// Case 8 - Trigger primary on regular expressions
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("00000", "BigFire in apartment 09374 mail: foo@bar.com"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("00000", alarm.getSender());
		assertEquals("09374, foo@bar.com", alarm.getTriggerText());
		assertEquals("BigFire in apartment 09374 mail: foo@bar.com", alarm.getMessage());

		// Case 9 - Trigger primary on number and regular expressions
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("22222", "Testing foo@bar.com 00:67"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("22222", alarm.getSender());
		assertEquals("00, 67, foo@bar.com, 00:67", alarm.getTriggerText());
		assertEquals("Testing foo@bar.com 00:67", alarm.getMessage());

		// Case 10 - Trigger primary on number, free texts and regular expressions
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("11111", "Testing test large foo@bar.com 00:67"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("11111", alarm.getSender());
		assertEquals("large, test, 00, 67, foo@bar.com, 00:67", alarm.getTriggerText());
		assertEquals("Testing test large foo@bar.com 00:67", alarm.getMessage());
	}

	/**
	 * To test alarm triggering of an {@link AlarmType#SECONDARY} alarm. These alarms can be triggered both on phone number, free text(s) and regular
	 * expression matching in message. All cases will be tested within this case.
	 */
	public void testOnReceiveTriggerSecondary() {
		// Case 1 - Trigger secondary on number
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("44444", "lorem ipsum"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.SECONDARY, alarm.getAlarmType());
		assertEquals("44444", alarm.getSender());
		assertEquals("-", alarm.getTriggerText());
		assertEquals("lorem ipsum", alarm.getMessage());

		// Case 2 - Trigger secondary on number and free text
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("666666", "Do some tests of alArm now"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.SECONDARY, alarm.getAlarmType());
		assertEquals("666666", alarm.getSender());
		assertEquals("alArm", alarm.getTriggerText());
		assertEquals("Do some tests of alArm now", alarm.getMessage());

		// Case 3 - Trigger secondary on number and regular expression
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("44444", "Testing www.test.com"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.SECONDARY, alarm.getAlarmType());
		assertEquals("44444", alarm.getSender());
		assertEquals("www.test.com", alarm.getTriggerText());
		assertEquals("Testing www.test.com", alarm.getMessage());

		// Case 4 - Trigger secondary on number, free text and regular expression
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("66666", "Do some tests of alArm now small_firealert"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.SECONDARY, alarm.getAlarmType());
		assertEquals("66666", alarm.getSender());
		assertEquals("alArm, small_firealert", alarm.getTriggerText());
		assertEquals("Do some tests of alArm now small_firealert", alarm.getMessage());

		// Case 5 - Trigger secondary on free text
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("99999", "The word for testing is smaLL"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.SECONDARY, alarm.getAlarmType());
		assertEquals("99999", alarm.getSender());
		assertEquals("smaLL", alarm.getTriggerText());
		assertEquals("The word for testing is smaLL", alarm.getMessage());

		// Case 6 - Trigger secondary on free texts
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("512752", "Testing small alarm quickly"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.SECONDARY, alarm.getAlarmType());
		assertEquals("512752", alarm.getSender());
		assertEquals("small, alarm, quickly", alarm.getTriggerText());
		assertEquals("Testing small alarm quickly", alarm.getMessage());

		// Case 7 - Trigger secondary on number and free texts
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("55555", "Testing quickly small alarm and some other stuff"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.SECONDARY, alarm.getAlarmType());
		assertEquals("55555", alarm.getSender());
		assertEquals("small, alarm, quickly", alarm.getTriggerText());
		assertEquals("Testing quickly small alarm and some other stuff", alarm.getMessage());

		// Case 8 - Trigger secondary on regular expression
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("73548", "Testing (www.foobar.com)"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.SECONDARY, alarm.getAlarmType());
		assertEquals("73548", alarm.getSender());
		assertEquals("www.foobar.com", alarm.getTriggerText());
		assertEquals("Testing (www.foobar.com)", alarm.getMessage());

		// Case 9 - Trigger secondary on regular expressions
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("396244", "Testing (www.foobar.com), and small_firealert"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.SECONDARY, alarm.getAlarmType());
		assertEquals("396244", alarm.getSender());
		assertEquals("www.foobar.com, small_firealert", alarm.getTriggerText());
		assertEquals("Testing (www.foobar.com), and small_firealert", alarm.getMessage());

		// Case 10 - Trigger secondary on number and regular expressions
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("55555", "Testing (www.foobar.com), bla bla and small_firealert, blaha"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.SECONDARY, alarm.getAlarmType());
		assertEquals("55555", alarm.getSender());
		assertEquals("www.foobar.com, small_firealert", alarm.getTriggerText());
		assertEquals("Testing (www.foobar.com), bla bla and small_firealert, blaha", alarm.getMessage());

		// Case 11 - Trigger secondary on number, free texts and regular expressions
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("44444", "Testing (www.foobar.com), ALARM and bla bla and small_firealert, quIckly blaha SMall"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.SECONDARY, alarm.getAlarmType());
		assertEquals("44444", alarm.getSender());
		assertEquals("SMall, ALARM, quIckly, www.foobar.com, small_firealert", alarm.getTriggerText());
		assertEquals("Testing (www.foobar.com), ALARM and bla bla and small_firealert, quIckly blaha SMall", alarm.getMessage());
	}

	/**
	 * To test alarm triggering of an {@link AlarmType#PRIMARY} alarm, and that the alarm isn't down graded to a {@link AlarmType#SECONDARY} if it
	 * contains a message which trigger on secondary free text or regular expression.
	 */
	public void testOnReceiveTriggerPrimaryDontDowngrade() {
		// Case 1 - Trigger primary on number, free text trigger on secondary text, alarm should not be down graded to secondary alarm
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("22222", "Testing small alarm"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("22222", alarm.getSender());
		assertEquals("-", alarm.getTriggerText());
		assertEquals("Testing small alarm", alarm.getMessage());

		// Case 2 - Trigger primary on free text, trigger on secondary text, alarm should not be down graded to secondary alarm
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("00000", "Testing large small alarm"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("00000", alarm.getSender());
		assertEquals("large", alarm.getTriggerText());
		assertEquals("Testing large small alarm", alarm.getMessage());

		// Case 3 - Trigger primary on regular expression, trigger on secondary text, alarm should not be down graded to secondary alarm
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("00000", "Testing test@testing.com small alarm"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("00000", alarm.getSender());
		assertEquals("test@testing.com", alarm.getTriggerText());
		assertEquals("Testing test@testing.com small alarm", alarm.getMessage());

		// Case 4 - Trigger primary on number, free text trigger on both primary and secondary text, alarm should not be down graded to secondary
		// alarm
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("33333", "Testing large small alarm"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("33333", alarm.getSender());
		assertEquals("large", alarm.getTriggerText());
		assertEquals("Testing large small alarm", alarm.getMessage());

		// Case 5 - Trigger primary on number, free text trigger on both primary and secondary text, trigger on regular expression on both primary and
		// secondary regular expressions, alarm should not be down graded to secondary alarm
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("33333", "Testing large small 56 alarm www.test.com"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("33333", alarm.getSender());
		assertEquals("large, 56", alarm.getTriggerText());
		assertEquals("Testing large small 56 alarm www.test.com", alarm.getMessage());

		// Case 6 - Trigger primary on number, trigger secondary on regular expression, alarm should not be down graded to secondary alarm
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("22222", "Testing small_firealert www.foo.bar"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("22222", alarm.getSender());
		assertEquals("-", alarm.getTriggerText());
		assertEquals("Testing small_firealert www.foo.bar", alarm.getMessage());

		// Case 7 - Trigger primary on regular expression, trigger on secondary text, alarm should not be down graded to secondary alarm
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("00000", "Testing 00:86 test@foo.bar"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("00000", alarm.getSender());
		assertEquals("00, 86, test@foo.bar, 00:86", alarm.getTriggerText());
		assertEquals("Testing 00:86 test@foo.bar", alarm.getMessage());

		// Case 8 - Trigger primary on number, trigger on regular expression on both primary and secondary regular expressions, alarm should not be
		// down graded to secondary alarm
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("22222", "Testing test@foo.bar"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("22222", alarm.getSender());
		assertEquals("test@foo.bar", alarm.getTriggerText());
		assertEquals("Testing test@foo.bar", alarm.getMessage());
	}

	/**
	 * To test alarm triggering of an {@link AlarmType#SECONDARY} alarm, and that the alarm is upgraded to a {@link AlarmType#PRIMARY} if it contains
	 * a message which trigger on primary free text or regular expressions.
	 */
	public void testOnReceiveTriggerSecondaryDoUpgrade() {
		// Case 1 - Trigger secondary on number, free text trigger on primary text, alarm should be upgraded to primary alarm
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("55555", "Do some tests of lArge alarm now"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("55555", alarm.getSender());
		assertEquals("lArge", alarm.getTriggerText());
		assertEquals("Do some tests of lArge alarm now", alarm.getMessage());

		// Case 2 - Trigger secondary on number, free text trigger on primary texts, alarm should be upgraded to primary alarm
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("66666", "Testing test large fire"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("66666", alarm.getSender());
		assertEquals("large, fire, test", alarm.getTriggerText());
		assertEquals("Testing test large fire", alarm.getMessage());

		// Case 3 - Trigger secondary on number, regular expression trigger on primary regular expression, alarm should be upgraded to primary alarm
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("66666", "Testing 26"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("66666", alarm.getSender());
		assertEquals("26", alarm.getTriggerText());
		assertEquals("Testing 26", alarm.getMessage());

		// Case 4 - Trigger secondary on number, regular expression trigger on primary regular expressions, alarm should be upgraded to primary alarm
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("55555", "Testing 26:98 (test@test.nu)"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("55555", alarm.getSender());
		assertEquals("26, 98, test@test.nu, 26:98", alarm.getTriggerText());
		assertEquals("Testing 26:98 (test@test.nu)", alarm.getMessage());

		// Case 5 - Trigger secondary on free text, regular expression trigger on primary regular expression, alarm should be upgraded to primary
		// alarm
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("00000", "Quickly testing (testing@test.com)"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("00000", alarm.getSender());
		assertEquals("testing@test.com", alarm.getTriggerText());
		assertEquals("Quickly testing (testing@test.com)", alarm.getMessage());

		// Case 6 - Trigger secondary on free text, regular expression trigger on primary regular expressions, alarm should be upgraded to primary
		// alarm
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("00000", "Quickly small testing (testing@test.com) 67"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("00000", alarm.getSender());
		assertEquals("67, testing@test.com", alarm.getTriggerText());
		assertEquals("Quickly small testing (testing@test.com) 67", alarm.getMessage());

		// Case 6 - Trigger secondary on number, free text and regular expression trigger on primary free text/regular expression, alarm should be
		// upgraded to primary alarm
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("55555", "Testing large test@foo.com"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("55555", alarm.getSender());
		assertEquals("large, test@foo.com", alarm.getTriggerText());
		assertEquals("Testing large test@foo.com", alarm.getMessage());

		// Case 7 - Trigger secondary on number, free text and regular expression trigger on primary free texts/regular expressions, alarm should be
		// upgraded to primary alarm
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("44444", "Testing large test@foo.com, 67:87 fire"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("44444", alarm.getSender());
		assertEquals("large, fire, 67, 87, test@foo.com, 67:87", alarm.getTriggerText());
		assertEquals("Testing large test@foo.com, 67:87 fire", alarm.getMessage());
	}

	/**
	 * To test that no alarms are triggered if Sms Alarm is disabled.
	 */
	public void testOnReceiveNoTriggerDisabled() {
		// Disable Sms Alarm
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.ENABLE_SMS_ALARM_KEY, false, context);

		// Get current alarms count
		currentAlarmsCount = databaseHandler.getAlarmsCount();

		// Invoke onReceive() a few times with intents we know it should trigger alarm on, set receiver to new before each intent
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("66666", "Testing test large fire test@test.com 98:87"));

		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("00000", "Testing large small alarm www.test.com"));

		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("22222", "Do some tests of lArge alarm now 657, www.bg.com test@test.com"));

		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("666666", "Do some tests of alArm now small_firealert"));

		// No alarms should have been added since Sms Alarm is disabled
		assertEquals(currentAlarmsCount, databaseHandler.getAlarmsCount());

		// Enable Sms Alarm again
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.ENABLE_SMS_ALARM_KEY, true, context);
	}

	/**
	 * To test that no alarms are triggered if SMS's are being received from numbers or doesn't contain any free texts or regular expressions that Sms
	 * Alarm is set to trigger on.
	 */
	public void testOnReceiveNoTrigger() {
		// Get current alarms count
		currentAlarmsCount = databaseHandler.getAlarmsCount();

		// Invoke onReceive() a few times with intents we know shouldn't trigger any alarms, set receiver to new before each intent
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("123456", "Some random text"));

		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("098765", "Further tests of no triggering"));

		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("749825", "Some more tests"));

		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("635438", "And a last one"));

		// No alarms should have been added
		assertEquals(currentAlarmsCount, databaseHandler.getAlarmsCount());
	}

	/**
	 * Convenience method to create a "send SMS {@link Intent}" with given sender and body as SMS's sender and message.
	 * 
	 * @param sender
	 *            Sender of the SMS.
	 * @param body
	 *            Message of the SMS.
	 * @return A fully valid SMS <code>Intent</code>.
	 */
	private Intent createMockSMSIntent(String sender, String body) {
		Intent intent = new Intent(SmsReceiver.ACTION_SKIP_ABORT_BROADCAST);
		intent.putExtra("pdus", new Object[] { DebugUtils.createMockSMS(sender, body) });
		intent.putExtra("format", "3gpp");

		return intent;
	}
}
