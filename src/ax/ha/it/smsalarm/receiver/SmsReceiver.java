/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.receiver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.telephony.SmsMessage;
import ax.ha.it.smsalarm.handler.DatabaseHandler;
import ax.ha.it.smsalarm.handler.KitKatHandler;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.DataType;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.PrefKey;
import ax.ha.it.smsalarm.handler.SoundHandler;
import ax.ha.it.smsalarm.pojo.Alarm;
import ax.ha.it.smsalarm.pojo.Alarm.AlarmType;
import ax.ha.it.smsalarm.provider.WidgetProvider;
import ax.ha.it.smsalarm.service.AcknowledgeNotificationService;
import ax.ha.it.smsalarm.service.NotificationService;
import ax.ha.it.smsalarm.util.AlarmLogger;
import ax.ha.it.smsalarm.util.WakeLocker;

/**
 * Class responsible for receiving SMS and handle them accordingly to the application settings.
 * <p>
 * <b><i>Note.</i></b><br>
 * After the introduction of KitKat(API Level 19) the call to {@link BroadcastReceiver#abortBroadcast()} is no longer of use. This means some special
 * handling is needed to be done if application runs on any device using KitKat. See {@link KitKatHandler} for more information.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 0.9beta
 */
public class SmsReceiver extends BroadcastReceiver {
	private static final String LOG_TAG = SmsReceiver.class.getSimpleName();

	// Debug actions to skip abort broadcast, by not disabling this while dispatching a test SMS will cause an exception
	public static final String ACTION_SKIP_ABORT_BROADCAST = "ax.ha.it.smsalarm.SKIP_ABORT_BROADCAST";

	// URI to sms inbox
	private static final String SMS_INBOX_URI = "content://sms/inbox";

	// How long we should acquire a wake lock
	private static final int WAKE_LOCKER_ACQUIRE_TIME = 20000;

	// Objects needed shared preferences, noise and KitKat handling
	private final SharedPreferencesHandler prefHandler = SharedPreferencesHandler.getInstance();
	private final SoundHandler soundHandler = SoundHandler.getInstance();
	private final KitKatHandler kitKatHandler = KitKatHandler.getInstance();

	// Lists of Strings containing primary- and secondary SMS numbers
	private List<String> primarySmsNumbers = new ArrayList<String>();
	private List<String> secondarySmsNumbers = new ArrayList<String>();

	// List of Strings containing free texts triggering an alarm
	private List<String> primaryFreeTexts = new ArrayList<String>();
	private List<String> secondaryFreeTexts = new ArrayList<String>();

	// To handle an incoming alarm properly
	private boolean enableAlarmAck = false;
	private boolean enableSmsAlarm = false;

	private AlarmType alarmType = AlarmType.UNDEFINED;

	// To store incoming SMS phone number and body(message)
	private String msgHeader = "";
	private String msgBody = "";

	// Text which triggered an alarm if free text triggering is used
	private String triggerText = "";

	/**
	 * To take proper actions depending on application settings and SMS senders phone number and/or the text contained within that SMS.
	 * 
	 * @param context
	 *            The Context in which the receiver is running.
	 * @param intent
	 *            The Intent being received.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		fetchSharedPrefs(context);

		// Only if SmsAlarm is enabled
		if (enableSmsAlarm) {
			// Catch the SMS passed in
			Bundle bundle = intent.getExtras();
			SmsMessage[] msgs = null;

			if (bundle != null) {
				// Get some data from the SMS
				Object[] pdus = (Object[]) bundle.get("pdus");
				msgs = new SmsMessage[pdus.length];

				for (int i = 0; i < msgs.length; i++) {
					msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
					msgHeader = msgs[i].getOriginatingAddress();
					msgBody += msgs[i].getMessageBody().toString();
				}

				// Check if income SMS was an alarm
				if (checkAlarm(context)) {
					// If Android API level is greater or equals to KitKat necessary that we do check this as soon as possible
					if (isKitKatOrHigher()) {
						kitKatHandler.handleKitKat(context);
					}
				}

				// Check if the income SMS was any alarm
				if (!alarmType.equals(AlarmType.UNDEFINED)) {
					// Continue handling of received SMS
					handleSMS(context, intent);
				}
			}
		}
	}

	/**
	 * To handle the income SMS. Aborts the system broadcast(will not have any function on KitKat, API Level 19 see {@link KitKatHandler} for more
	 * information) and thereby ignoring the operating systems SMS received settings. Method also handles the income SMS event according to the
	 * settings of the application, playing appropriate alarm signal, handles wake lock, dispatching notifications makes the device vibrate, handles
	 * widgets and so on.
	 * 
	 * @param context
	 *            The Context in which the receiver is running.
	 * @param intent
	 *            Intent from which data are fetched.
	 */
	private void handleSMS(Context context, Intent intent) {
		// Only abort broadcast if not intent action skip abort broadcast are set, this action is used for debug/testing purpose
		if (!ACTION_SKIP_ABORT_BROADCAST.equals(intent.getAction())) {
			// Abort broadcast, SmsAlarm will handle income SMS on it's own
			abortBroadcast();
		}

		// Get database access, add this income SMS(alarm) and log it to file
		DatabaseHandler db = new DatabaseHandler(context);
		db.insertAlarm(new Alarm(msgHeader, msgBody, triggerText, alarmType));
		AlarmLogger.getInstance().logAlarms(db.fetchAllAlarm(), context);

		// Update all widgets associated with this application
		WidgetProvider.updateWidgets(context);

		// Detect whether screen is on or off, if it's off we need to wake it
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		if (!pm.isScreenOn()) {
			// Wake up device by acquire a wake lock and then release it after given time,
			// the time depends on if device runs on KitKat(or higher) or not
			if (isKitKatOrHigher()) {
				WakeLocker.acquireAndRelease(context, (WAKE_LOCKER_ACQUIRE_TIME + KitKatHandler.RINGER_MODE_DELAY));
			} else {
				WakeLocker.acquireAndRelease(context, WAKE_LOCKER_ACQUIRE_TIME);
			}
		}

		// Pattern for regular expression like this; dd.dd.dddd dd:dd:dd: d.d, alarm from http://www.alarmcentralen.ax has this pattern
		Pattern p = Pattern.compile("(\\d{2}).(\\d{2}).(\\d{4})(\\s)(\\d{2}):(\\d{2}):(\\d{2})(\\s)(\\d{1}).(\\d{1})");
		Matcher m = p.matcher(msgBody);

		// Due to previous abort we have to store the SMS manually in phones inbox
		// for some reason this must also be done even if application runs on KitKat, this is strange because abortBroadcast() should be totally
		// ignored on that version, therefore the SMS should be placed in inbox without this snippet. Almost seems like a bug in Android....
		ContentValues values = new ContentValues();
		values.put("address", msgHeader);
		values.put("body", msgBody);
		context.getContentResolver().insert(Uri.parse(SMS_INBOX_URI), values);

		// Play alarm signal
		soundHandler.alarm(context, alarmType);

		// If alarm acknowledge is enabled and alarm type equals primary, store full alarm message
		if (enableAlarmAck && alarmType.equals(AlarmType.PRIMARY)) {
			// Full message in income SMS needs to be stored in shared preferences in this case
			prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.FULL_MESSAGE_KEY, msgBody, context);
		}

		// If message contain a string with correct pattern(alarm from http://www.alarmcentralen.ax), remove the date and time stamp in message
		if (m.find()) {
			msgBody = msgBody.replace(m.group(1) + "." + m.group(2) + "." + m.group(3) + m.group(4) + m.group(5) + ":" + m.group(6) + ":" + m.group(7) + m.group(8) + m.group(9) + "." + m.group(10), "");
		}

		// Store income SMS message in shared preferences so it can be shown in notification
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.MESSAGE_KEY, msgBody, context);

		// Acknowledge is enabled and it is a primary alarm, show acknowledge notification, else show "ordinary" notification
		if (enableAlarmAck && alarmType.equals(AlarmType.PRIMARY)) {
			// Start intent, AcknowledgeNotificationHelper - a helper to show acknowledge notification
			Intent ackNotIntent = new Intent(context, AcknowledgeNotificationService.class);
			context.startService(ackNotIntent);
		} else {
			// Start intent, NotificationHelper - a helper to show notification
			Intent notIntent = new Intent(context, NotificationService.class);
			context.startService(notIntent);
		}
	}

	/**
	 * To fetch all {@link SharedPreferences} used by {@link SmsReceiver} class.
	 * 
	 * @param context
	 *            The Context in which the receiver is running.
	 */
	@SuppressWarnings("unchecked")
	private void fetchSharedPrefs(Context context) {
		primarySmsNumbers = (List<String>) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_NUMBERS_KEY, DataType.LIST, context);
		secondarySmsNumbers = (List<String>) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_NUMBERS_KEY, DataType.LIST, context);
		primaryFreeTexts = (List<String>) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_FREE_TEXTS_KEY, DataType.LIST, context);
		secondaryFreeTexts = (List<String>) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_FREE_TEXTS_KEY, DataType.LIST, context);
		enableAlarmAck = (Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.ENABLE_ACK_KEY, DataType.BOOLEAN, context);
		enableSmsAlarm = (Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.ENABLE_SMS_ALARM_KEY, DataType.BOOLEAN, context, true);
	}

	/**
	 * <b><i>Alarm</i></b>. For this to happen the income SMS must fulfill criteria for either a {@link AlarmType#PRIMARY} or
	 * {@link AlarmType#SECONDARY}.
	 * 
	 * @param context
	 *            The Context in which the receiver is running.
	 * @return <code>true</code> if income SMS was an alarm else <code>false</code>.
	 * @see #checkSmsAlarm(Context)
	 * @see #checkFreeTextAlarm(Context)
	 */
	private boolean checkAlarm(Context context) {
		// Figure out if we got an alarm and return appropriate value
		boolean isSmsAlarm = checkSmsAlarm(context);
		boolean isFreeTextAlarm = checkFreeTextAlarm(context);

		return isSmsAlarm || isFreeTextAlarm;
	}

	/**
	 * To check if received SMS is an <b><i>Alarm</i></b>. The check is done by a <b><i>equality control</i></b> of the senders phone number and the
	 * phone numbers read from {@link SharedPreferences}.
	 * 
	 * @param context
	 *            The Context in which the receiver is running.
	 * @return <code>true</code> if income SMS was an <b><i>Number Triggered</i></b> alarm else <code>false</code>.
	 */
	private boolean checkSmsAlarm(Context context) {
		// First check for primary alarm...
		for (String primarySmsNumber : primarySmsNumbers) {
			// If msgHeader(senders phone number) exists in the list of primary SMS numbers, store alarm and return
			if (msgHeader.equals(primarySmsNumber)) {
				setAlarmType(AlarmType.PRIMARY, context);
				return true;
			}
		}

		// ...then secondary alarm
		for (String secondarySmsNumber : secondarySmsNumbers) {
			if (msgHeader.equals(secondarySmsNumber)) {
				setAlarmType(AlarmType.SECONDARY, context);
				return true;
			}
		}

		return false;
	}

	/**
	 * To check if received SMS is a <b><i>Alarm</i></b>.The check is done by a <b><i>equality control</i></b> of the <b><i>words</i></b> within the
	 * income SMS and the free texts read from {@link SharedPreferences}.
	 * 
	 * @param context
	 *            The Context in which the receiver is running.
	 * @return <code>true</code> if income SMS was an <b><i>Free Text Triggered</i></b> alarm else <code>false</code>.
	 */
	private boolean checkFreeTextAlarm(Context context) {

		// First check for primary alarm...
		for (String primaryFreeText : primaryFreeTexts) {
			// If any of the words within the msgBody exists in the list of primary free texts, store alarm, set the trigger texts and return
			if (findWordEqualsIgnore(primaryFreeText, msgBody)) {
				setAlarmType(AlarmType.PRIMARY, context);

				// Need to know the trigger text
				setTriggerText(primaryFreeText);
				return true;
			}
		}

		// ...then secondary alarm
		for (String secondaryFreeText : secondaryFreeTexts) {
			if (findWordEqualsIgnore(secondaryFreeText, msgBody)) {
				setAlarmType(AlarmType.SECONDARY, context);
				setTriggerText(secondaryFreeText);
				return true;
			}
		}

		return false;
	}

	/**
	 * Convenience method to flag {@link AlarmType} of income SMS.
	 * 
	 * @param alarmType
	 *            Alarm type to be set.
	 * @param context
	 *            The Context in which the receiver is running.
	 */
	private void setAlarmType(AlarmType alarmType, Context context) {
		// Set the given alarm type
		this.alarmType = alarmType;

		// The "ordinary" notification must know what type of alarm was received, therefore it's stored to shared preferences
		prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.LARM_TYPE_KEY, alarmType.ordinal(), context);
	}

	/**
	 * Convenience method to set the <b><i>Trigger Text</i></b> of income SMS.
	 * 
	 * @param triggerText
	 *            Text to be set as trigger text.
	 */
	private void setTriggerText(String triggerText) {
		// If existing text is empty just add trigger text else concatenate and add trigger text
		if (this.triggerText.length() == 0) {
			this.triggerText = triggerText;
		} else {
			this.triggerText = this.triggerText + ", " + triggerText;
		}
	}

	/**
	 * To check if <code>String</code>(textToParse) passed in as argument contains another <code>String</code>(wordToFind) passed in as argument. This
	 * method only checks whole words and not a <code>CharSequence</code>.<br>
	 * <b><i>Note. Method is not case sensitive.</i></b>
	 * 
	 * @param wordToFind
	 *            Word to find.
	 * @param textToParse
	 *            Text to look for word in.
	 * @return <code>true</code> if word is found else <code>false</code>.
	 */
	private boolean findWordEqualsIgnore(String wordToFind, String textToParse) {
		if (wordToFind != null && textToParse != null) {
			if ((wordToFind.length() != 0) && (textToParse.length() != 0)) {
				List<String> words = Arrays.asList(textToParse.split(" "));

				for (String word : words) {
					if (wordToFind.equalsIgnoreCase(word)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Convenience method to figure out if <code>Build.VERSION.SDK_INT</code> equals to <code>Build.VERSION_CODES.KITKAT</code> or higher.
	 * 
	 * @return <code>true</code> if <code>Build.VERSION.SDK_INT</code> equals to <code>Build.VERSION_CODES.KITKAT</code> else <code>false</code>.
	 */
	private boolean isKitKatOrHigher() {
		// Android API level is greater or equals to KitKat
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			return true;
		}

		return false;
	}
}