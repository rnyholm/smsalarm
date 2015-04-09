/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import ax.ha.it.smsalarm.alarm.Alarm;
import ax.ha.it.smsalarm.alarm.Alarm.AlarmType;
import ax.ha.it.smsalarm.handler.DatabaseHandler;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.PrefKey;
import ax.ha.it.smsalarm.handler.SoundHandler;
import ax.ha.it.smsalarm.receiver.SmsReceiver;
import ax.ha.it.smsalarm.service.AcknowledgeNotificationService;
import ax.ha.it.smsalarm.service.NotificationService;

/**
 * Utility class containing different methods for <b><i>Debug/Test Usage</i></b>.<br>
 * <b><i>Note. All of this functionality should be accessed in a static manner.</i></b>
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class DebugUtils {
	private static final String LOG_TAG = DebugUtils.class.getSimpleName();

	/**
	 * To populate the {@link SharedPreferences} with mock data for test purpose.
	 * 
	 * @param context
	 *            Context in which Shared Preferences are set.
	 */
	public static void mockSharedPreferences(Context context) {
		SharedPreferencesHandler prefHandler = SharedPreferencesHandler.getInstance();
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_NUMBERS_KEY, Arrays.asList("11111", "22222", "33333"), context);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_NUMBERS_KEY, Arrays.asList("44444", "55555"), context);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_FREE_TEXTS_KEY, Arrays.asList("Grundlarm", "Brand", "test"), context);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_FREE_TEXTS_KEY, Arrays.asList("Litet", "Larm"), context);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_ALARM_SIGNAL_KEY, SoundHandler.getInstance().resolveAlarmSignal(context, 2), context);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_ALARM_SIGNAL_KEY, SoundHandler.getInstance().resolveAlarmSignal(context, 7), context);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.ENABLE_ACK_KEY, true, context);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.ACK_NUMBER_KEY, "04579999888", context);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.USE_OS_SOUND_SETTINGS_KEY, true, context);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PLAY_ALARM_SIGNAL_TWICE_KEY, false, context);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.ENABLE_SMS_ALARM_KEY, true, context);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.RESCUE_SERVICE_KEY, "Test Räddningstjänst", context);
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.USE_FLASH_NOTIFICATION, false, context);
	}

	/**
	 * To create mock {@link Alarm}'s and insert them into the <b><i>Database</i></b>. To only be used for debug and testing purpose.
	 * 
	 * @param context
	 *            Context in which <code>Alarm</code>'s are being mocked and inserted into database.
	 * @see DatabaseHandler#insertMockAlarms()
	 */
	@SuppressWarnings("deprecation")
	public static void insertMockAlarms(Context context) {
		DatabaseHandler db = new DatabaseHandler(context);
		db.insertMockAlarms();
	}

	/**
	 * To dispatch a {@link NotificationService} for test purpose.<br>
	 * Latest received alarm will be used to build up the Notification, if no one exists default values will be used from {@link SharedPreferences}.
	 * 
	 * @param context
	 *            Context in which Notification are dispatched.
	 */
	public static void dispatchNotification(Context context) {
		Intent notificationIntent = new Intent(context, NotificationService.class);
		notificationIntent.putExtra(Alarm.TAG, new Alarm("1234567", "02.02.2012 23:55:40 2.5 Litet larm - Automatlarm vikingline lager(1682) Länsmanshägnan 7 jomala", "", AlarmType.SECONDARY));
		context.startService(notificationIntent);
	}

	/**
	 * To dispatch a {@link AcknowledgeNotificationService} for test purpose.<br>
	 * Latest received alarm will be used to build up the Notification, if no one exists default values will be used from {@link SharedPreferences}.
	 * 
	 * @param context
	 *            Context in which Notification are dispatched.
	 */
	public static void dispatchAcknowledgeNotification(Context context) {
		Intent acknowledgeNotificationIntent = new Intent(context, AcknowledgeNotificationService.class);
		acknowledgeNotificationIntent.putExtra(Alarm.TAG, new Alarm("1234567", "02.02.2012 23:55:40 2.5 Litet larm - Automatlarm vikingline lager(1682) Länsmanshägnan 7 jomala", "larm", AlarmType.PRIMARY));
		context.startService(acknowledgeNotificationIntent);
	}

	/**
	 * To dispatch a mock SMS. This is done by building up a fully compatible SMS, building up an {@link Intent} with {@link SmsReceiver} as class and
	 * invoke the broadcast manually. However one special difference exists in this built up intent, the action of it is set to
	 * {@link SmsReceiver#ACTION_SKIP_ABORT_BROADCAST} this is to skip the <b><i>Abort Broadcast</i></b> method invocation. If this isn't skipped an
	 * exception will occur.
	 * 
	 * @param context
	 *            Context within SmsReceiver are triggered.
	 * @param sender
	 *            Sender of the SMS.
	 * @param body
	 *            Message of the SMS.
	 */
	public static void dispatchMockSMS(Context context, String sender, String body) {
		byte[] pdu = null;
		byte[] scBytes = PhoneNumberUtils.networkPortionToCalledPartyBCD("0000000000");
		byte[] senderBytes = PhoneNumberUtils.networkPortionToCalledPartyBCD(sender);
		int lsmcs = scBytes.length;
		byte[] dateBytes = new byte[7];
		Calendar calendar = new GregorianCalendar();

		dateBytes[0] = reverseByte((byte) (calendar.get(Calendar.YEAR)));
		dateBytes[1] = reverseByte((byte) (calendar.get(Calendar.MONTH) + 1));
		dateBytes[2] = reverseByte((byte) (calendar.get(Calendar.DAY_OF_MONTH)));
		dateBytes[3] = reverseByte((byte) (calendar.get(Calendar.HOUR_OF_DAY)));
		dateBytes[4] = reverseByte((byte) (calendar.get(Calendar.MINUTE)));
		dateBytes[5] = reverseByte((byte) (calendar.get(Calendar.SECOND)));
		dateBytes[6] = reverseByte((byte) ((calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / (60 * 1000 * 15)));

		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			bo.write(lsmcs);
			bo.write(scBytes);
			bo.write(0x04);
			bo.write((byte) sender.length());
			bo.write(senderBytes);
			bo.write(0x00);
			bo.write(0x00); // encoding: 0 for default 7bit
			bo.write(dateBytes);
			try {
				String sReflectedClassName = "com.android.internal.telephony.GsmAlphabet";
				Class<?> cReflectedNFCExtras = Class.forName(sReflectedClassName);
				Method stringToGsm7BitPacked = cReflectedNFCExtras.getMethod("stringToGsm7BitPacked", new Class[] { String.class });
				stringToGsm7BitPacked.setAccessible(true);
				byte[] bodybytes = (byte[]) stringToGsm7BitPacked.invoke(null, body);
				bo.write(bodybytes);
			} catch (Exception e) {
				Log.e(LOG_TAG, "Failed convert string: \"" + body + "\" to GSM 7 Bit Packed", e);
			}

			pdu = bo.toByteArray();
		} catch (IOException e) {
		}

		Intent intent = new Intent(context, SmsReceiver.class);
		intent.putExtra("pdus", new Object[] { pdu });
		intent.putExtra("format", "3gpp");
		intent.setAction(SmsReceiver.ACTION_SKIP_ABORT_BROADCAST);
		context.sendBroadcast(intent);
	}

	/**
	 * Convenience method to reverse a <code>byte</code>.
	 * 
	 * @param b
	 *            Byte to be reversed.
	 * @return Given byte, reversed.
	 */
	private static byte reverseByte(byte b) {
		return (byte) ((b & 0xF0) >> 4 | (b & 0x0F) << 4);
	}
}
