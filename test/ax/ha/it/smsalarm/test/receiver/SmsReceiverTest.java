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
		originalEnableSmsAlarm = (Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.ENABLE_SMS_ALARM_KEY, DataType.BOOLEAN, context, true);

		// Get the number of alarms in the database
		originalAlarmsCount = databaseHandler.getAlarmsCount();

		// Store the test data to the shared preferences
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_NUMBERS_KEY, Arrays.asList("11111", "22222", "33333"), context);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_NUMBERS_KEY, Arrays.asList("44444", "55555", "66666"), context);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_FREE_TEXTS_KEY, Arrays.asList("Large", "Fire", "test"), context);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_FREE_TEXTS_KEY, Arrays.asList("Small", "Alarm", "quickly"), context);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.ENABLE_SMS_ALARM_KEY, true, context);
	}

	@Override
	protected void tearDown() throws Exception {
		// Restore the shared preferences
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_NUMBERS_KEY, originalPrimarySmsNumbers, context);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_NUMBERS_KEY, originalSecondarySmsNumbers, context);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_FREE_TEXTS_KEY, originalPrimaryFreeTexts, context);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_FREE_TEXTS_KEY, originalSecondaryFreeTexts, context);
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
	 * To test alarm triggering of an {@link AlarmType#PRIMARY} alarm. These alarms can be triggered both on phone number and free text(s) in message.
	 * All cases will be tested within this case.
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
		assertEquals("Large", alarm.getTriggerText());
		assertEquals("Do some tests of lArge alarm now", alarm.getMessage());

		// Case 3 - Trigger primary on free text
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("00000", "Fire in apartment"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("00000", alarm.getSender());
		assertEquals("Fire", alarm.getTriggerText());
		assertEquals("Fire in apartment", alarm.getMessage());

		// Case 4 - Trigger primary on free texts
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("28463800", "Testing large fire test"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("28463800", alarm.getSender());
		assertEquals("Large, Fire, test", alarm.getTriggerText());
		assertEquals("Testing large fire test", alarm.getMessage());

		// Case 5 - Trigger primary on number and free texts
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("11111", "Testing test large fire"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("11111", alarm.getSender());
		assertEquals("Large, Fire, test", alarm.getTriggerText());
		assertEquals("Testing test large fire", alarm.getMessage());
	}

	/**
	 * To test alarm triggering of an {@link AlarmType#SECONDARY} alarm. These alarms can be triggered both on phone number and free text(s) in
	 * message. All cases will be tested within this case.
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
		assertEquals("Alarm", alarm.getTriggerText());
		assertEquals("Do some tests of alArm now", alarm.getMessage());

		// Case 3 - Trigger secondary on free text
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("99999", "The word for testing is smaLL"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.SECONDARY, alarm.getAlarmType());
		assertEquals("99999", alarm.getSender());
		assertEquals("Small", alarm.getTriggerText());
		assertEquals("The word for testing is smaLL", alarm.getMessage());

		// Case 4 - Trigger secondary on free texts
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("512752", "Testing small alarm quickly"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.SECONDARY, alarm.getAlarmType());
		assertEquals("512752", alarm.getSender());
		assertEquals("Small, Alarm, quickly", alarm.getTriggerText());
		assertEquals("Testing small alarm quickly", alarm.getMessage());

		// Case 5 - Trigger secondary on number and free texts
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("55555", "Testing quickly small alarm and some other stuff"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.SECONDARY, alarm.getAlarmType());
		assertEquals("55555", alarm.getSender());
		assertEquals("Small, Alarm, quickly", alarm.getTriggerText());
		assertEquals("Testing quickly small alarm and some other stuff", alarm.getMessage());
	}

	/**
	 * To test alarm triggering of an {@link AlarmType#PRIMARY} alarm, and that the alarm isn't down graded to a {@link AlarmType#SECONDARY} if it
	 * contains a message which trigger on secondary free text.
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
		assertEquals("Large", alarm.getTriggerText());
		assertEquals("Testing large small alarm", alarm.getMessage());

		// Case 3 - Trigger primary on number, free text trigger on both primary and secondary text, alarm should not be down graded to secondary
		// alarm
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("33333", "Testing large small alarm"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("33333", alarm.getSender());
		assertEquals("Large", alarm.getTriggerText());
		assertEquals("Testing large small alarm", alarm.getMessage());
	}

	/**
	 * To test alarm triggering of an {@link AlarmType#SECONDARY} alarm, and that the alarm is upgraded to a {@link AlarmType#PRIMARY} if it contains
	 * a message which trigger on primary free text.
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
		assertEquals("Large", alarm.getTriggerText());
		assertEquals("Do some tests of lArge alarm now", alarm.getMessage());

		// Case 2 - Trigger secondary on number, free text trigger on primary texts, alarm should be upgraded to primary alarm
		currentAlarmsCount = databaseHandler.getAlarmsCount();
		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("66666", "Testing test large fire"));
		alarm = databaseHandler.fetchLatestAlarm();
		assertEquals(++currentAlarmsCount, databaseHandler.getAlarmsCount());
		assertEquals(AlarmType.PRIMARY, alarm.getAlarmType());
		assertEquals("66666", alarm.getSender());
		assertEquals("Large, Fire, test", alarm.getTriggerText());
		assertEquals("Testing test large fire", alarm.getMessage());
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
		receiver.onReceive(context, createMockSMSIntent("66666", "Testing test large fire"));

		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("00000", "Testing large small alarm"));

		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("22222", "Do some tests of lArge alarm now"));

		receiver = new SmsReceiver();
		receiver.onReceive(context, createMockSMSIntent("666666", "Do some tests of alArm now"));

		// No alarms should have been added since Sms Alarm is disabled
		assertEquals(currentAlarmsCount, databaseHandler.getAlarmsCount());

		// Enable Sms Alarm again
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.ENABLE_SMS_ALARM_KEY, true, context);
	}

	/**
	 * To test that no alarms are triggered if SMS's are being received from numbers or doesn't contain any free texts that Sms Alarm is set to
	 * trigger on.
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
