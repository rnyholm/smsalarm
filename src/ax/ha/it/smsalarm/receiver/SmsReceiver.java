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
import ax.ha.it.smsalarm.alarm.Alarm;
import ax.ha.it.smsalarm.alarm.Alarm.AlarmType;
import ax.ha.it.smsalarm.handler.DatabaseHandler;
import ax.ha.it.smsalarm.handler.KitKatHandler;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.DataType;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.PrefKey;
import ax.ha.it.smsalarm.handler.SoundHandler;
import ax.ha.it.smsalarm.handler.VibrationHandler;
import ax.ha.it.smsalarm.provider.WidgetProvider;
import ax.ha.it.smsalarm.service.AcknowledgeNotificationService;
import ax.ha.it.smsalarm.service.NotificationService;
import ax.ha.it.smsalarm.util.Logger;
import ax.ha.it.smsalarm.util.Utils;
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
	// Debug actions to skip abort broadcast, by not disabling this while dispatching a test SMS will cause an exception. Also if this action is set
	// then the income SMS will not be stored into the inbox
	public static final String ACTION_SKIP_ABORT_BROADCAST = "ax.ha.it.smsalarm.SKIP_ABORT_BROADCAST";

	// URI to SMS inbox
	private static final String SMS_INBOX_URI = "content://sms/inbox";

	// Name of SMS log file
	private static final String SMS_LOG_FILE = "smslog.txt";

	// How long we should acquire a wake lock
	private static final int WAKE_LOCKER_ACQUIRE_TIME = 20000;

	// Objects needed shared preferences, noise and KitKat handling
	private final SharedPreferencesHandler prefHandler = SharedPreferencesHandler.getInstance();
	private final KitKatHandler kitKatHandler = KitKatHandler.getInstance();

	// Lists of Strings containing primary- and secondary SMS numbers
	private List<String> primarySmsNumbers = new ArrayList<String>();
	private List<String> secondarySmsNumbers = new ArrayList<String>();

	// List of Strings containing free texts triggering an alarm
	private List<String> primaryFreeTexts = new ArrayList<String>();
	private List<String> secondaryFreeTexts = new ArrayList<String>();

	// List of string containing regular expressions triggering an alarm
	private List<String> primaryRegexs = new ArrayList<String>();
	private List<String> secondaryRegexs = new ArrayList<String>();

	// To handle an incoming alarm properly
	private boolean enableAlarmAck = false;
	private boolean enableSMSDebugLogging = false;
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

				// Get rid of any eventual white spaces in incoming phone number
				msgHeader = Utils.removeSpaces(msgHeader);

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

				// At last if SMS Debug logging is enabled do the logging of income SMS
				if (enableSMSDebugLogging) {
					// Instantiate a new Logger object at this point, only if it's needed, we don't want to do unnecessary work in the receiver
					new Logger(SMS_LOG_FILE).log2File(msgHeader + "\t" + msgBody);
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

			// Due to previous abort we have to store the SMS manually in phones inbox
			// for some reason this must also be done even if application runs on KitKat, this is strange because abortBroadcast() should be totally
			// ignored on that version, therefore the SMS should be placed in inbox without this snippet. Almost seems like a bug in Android....
			ContentValues values = new ContentValues();
			values.put("address", msgHeader);
			values.put("body", msgBody);
			context.getContentResolver().insert(Uri.parse(SMS_INBOX_URI), values);
		}

		// Create a new alarm from this income SMS(alarm)...
		Alarm alarm = new Alarm(msgHeader, msgBody, triggerText, alarmType);

		// ...get database access and insert the alarm into database
		DatabaseHandler db = new DatabaseHandler(context);
		alarm = db.insertAlarm(alarm);

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

		// Play alarm signal and vibrate
		SoundHandler.getInstance().alarm(context, alarmType);
		VibrationHandler.getInstance().alarm(context, alarmType);

		// Acknowledge is enabled and it is a primary alarm, show acknowledge notification, else show "ordinary" notification
		if (enableAlarmAck && alarmType.equals(AlarmType.PRIMARY)) {
			// Start intent, AcknowledgeNotificationService with the received alarm as extra
			Intent ackNotIntent = new Intent(context, AcknowledgeNotificationService.class);
			ackNotIntent.putExtra(Alarm.TAG, alarm);
			context.startService(ackNotIntent);
		} else {
			// Start intent, NotificationService
			Intent notIntent = new Intent(context, NotificationService.class);
			notIntent.putExtra(Alarm.TAG, alarm);
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
		primaryRegexs = (List<String>) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_REGULAR_EXPRESSIONS_KEY, DataType.LIST, context);
		secondaryRegexs = (List<String>) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_REGULAR_EXPRESSIONS_KEY, DataType.LIST, context);
		enableAlarmAck = (Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.ENABLE_ACK_KEY, DataType.BOOLEAN, context);
		enableSMSDebugLogging = (Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.ENABLE_SMS_DEBUG_LOGGING, DataType.BOOLEAN, context);
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
	 * @see #checkRegexAlarm(Context)
	 */
	private boolean checkAlarm(Context context) {
		// Figure out if we got an alarm and return appropriate value
		boolean isSmsAlarm = checkSmsAlarm(context);
		boolean isFreeTextAlarm = checkFreeTextAlarm(context);
		boolean isRegexAlarm = checkRegexAlarm(context);

		return isSmsAlarm || isFreeTextAlarm || isRegexAlarm;
	}

	/**
	 * To check if received SMS is an <b><i>Alarm</i></b>. The check is done by a <b><i>equality control</i></b> of the senders phone number.
	 * 
	 * @param context
	 *            The Context in which the receiver is running.
	 * @return <code>true</code> if income SMS was an <b><i>Number Triggered</i></b> alarm else <code>false</code>.
	 */
	private boolean checkSmsAlarm(Context context) {
		// Needed to figure out if income SMS is an alarm
		boolean isAlarm = false;

		// First check for primary alarm...
		for (String primarySmsNumber : primarySmsNumbers) {
			// If msgHeader(senders phone number) exists in the list of primary SMS numbers, store alarm type and indicate that the SMS is an alarm.
			// Only needed to check this also if the income SMS hasn't been recognized as an alarm yet
			if (!isAlarm && msgHeader.equals(primarySmsNumber)) {
				alarmType = AlarmType.PRIMARY;
				isAlarm = true;
			}
		}

		// Income SMS already figured out to be a primary alarm as it triggered on number, no need for further checks
		if (!isAlarm) {
			// / ...then secondary alarm if income SMS wasn't resolved as a primary alarm from number triggering
			for (String secondarySmsNumber : secondarySmsNumbers) {
				if (!isAlarm && msgHeader.equals(secondarySmsNumber)) {
					alarmType = AlarmType.SECONDARY;
					isAlarm = true;
				}
			}
		}

		return isAlarm;
	}

	/**
	 * To check if received SMS is a <b><i>Alarm</i></b>.The check is done by a <b><i>equality control</i></b> of the <b><i>words</i></b> within the
	 * income SMS.
	 * 
	 * @param context
	 *            The Context in which the receiver is running.
	 * @return <code>true</code> if income SMS was an <b><i>Free Text Triggered</i></b> alarm else <code>false</code>.
	 */
	private boolean checkFreeTextAlarm(Context context) {
		// Needed to figure out if income SMS is an alarm
		boolean isAlarm = false;

		// First check for primary alarm...
		for (String primaryFreeText : primaryFreeTexts) {
			// If any of the words within the msgBody exists in the list of primary free texts, store alarm, set the trigger texts and indicate alarm
			if (findWordEqualsIgnore(primaryFreeText, msgBody)) {
				alarmType = AlarmType.PRIMARY;

				isAlarm = true;
			}
		}

		// Income SMS already figured out to be a primary alarm as it triggered on free text, no need for further checks
		if (!isAlarm) {
			// Only proceed if current, resolved alarm type isn't primary, we don't want to down grade a primary alarm
			if (!AlarmType.PRIMARY.equals(alarmType)) {
				// Check if received message trigger primary alarm on regular expression, if so don't proceed, this is to prevent alarm being set to
				// secondary when it's actually a primary alarm, this is a bit ugly but it works
				if (notPrimaryRegexAlarm()) {
					// ...then secondary alarm if income SMS wasn't resolved as a primary alarm from free text triggering
					for (String secondaryFreeText : secondaryFreeTexts) {
						if (findWordEqualsIgnore(secondaryFreeText, msgBody)) {
							alarmType = AlarmType.SECONDARY;

							isAlarm = true;
						}
					}
				}
			}
		}

		return isAlarm;
	}

	/**
	 * To check if received SMS is a <b><i>Alarm</i></b>.The check is done by a <b><i>regular expression pattern matching</i></b> of the
	 * <b><i>contents</i></b> of the income SMS.
	 * 
	 * @param context
	 *            The Context in which the receiver is running.
	 * @return <code>true</code> if income SMS was an <b><i>Regular Expression Match Triggered</i></b> alarm else <code>false</code>.
	 */
	private boolean checkRegexAlarm(Context context) {
		boolean isAlarm = false;

		// First check for primary alarm..
		for (String primaryRegex : primaryRegexs) {
			// If contents within the msgBody does match any of the regular expressions within the list of primary regular expressions, store alarm,
			// set the trigger regular expression and indicate alarm
			if (matchRegex(primaryRegex, msgBody)) {
				alarmType = AlarmType.PRIMARY;

				isAlarm = true;
			}
		}

		// Income SMS already figured out to be a primary alarm as it triggered on regular expression, no need for further checks
		if (!isAlarm) {
			// Only if current, resolved alarm type isn't primary, we don't want to down grade a primary alarm
			if (!AlarmType.PRIMARY.equals(alarmType)) {
				// ...then secondary alarm if income SMS wasn't resolved as a primary alarm from regular expression triggering
				for (String secondaryRegex : secondaryRegexs) {
					if (matchRegex(secondaryRegex, msgBody)) {
						alarmType = AlarmType.SECONDARY;

						isAlarm = true;
					}
				}
			}
		}

		return isAlarm;
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
	 * This method does also set the triggering text, the text which triggered an alarm, in this case a specific word was found.<br>
	 * <b><i>Note. Method is not case sensitive.</i></b>
	 * 
	 * @param wordToFind
	 *            Word to find.
	 * @param textToParse
	 *            Text to look for word in.
	 * @return <code>true</code> if word is found, else <code>false</code>.
	 */
	private boolean findWordEqualsIgnore(String wordToFind, String textToParse) {
		boolean result = false;

		if (wordToFind != null && textToParse != null) {
			if ((wordToFind.length() != 0) && (textToParse.length() != 0)) {
				List<String> words = Arrays.asList(textToParse.split(" "));

				for (String word : words) {
					if (wordToFind.equalsIgnoreCase(word)) {
						result = true;

						// Set triggering text
						setTriggerText(word);
					}
				}
			}
		}

		return result;
	}

	/**
	 * To check if <code>String</code>(textToParse) passed in as argument has any text sequences that matches the regular expression
	 * <code>String</code>(regex) passed in as argument.<br>
	 * This method does also set the triggering text, the text which triggered an alarm, in this case a regular expression pattern match.
	 * 
	 * @param regex
	 *            Regular expression to find matches of.
	 * @param textToParse
	 *            Text to find matches of regular expression in.
	 * @return <code>true</code> if regular expression has any matches, else <code>false</code>.
	 */
	private boolean matchRegex(String regex, String textToParse) {
		boolean result = false;

		if (regex != null && textToParse != null) {
			if ((regex.length() != 0) && (textToParse.length() != 0)) {
				// Compile regular expression into a pattern and try to find a match
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(textToParse);

				// Iterate over all occurrences, often this is just once
				while (matcher.find()) {
					result = true;

					// Set triggering text
					setTriggerText(matcher.group());
				}
			}
		}

		return result;
	}

	/**
	 * Convenience method to check that any of the primary regular expressions doesn't match any text sequence within the received message. In short
	 * that a primary alarm will not be triggered due to regular expression pattern matching.<br>
	 * Note. This method is used to prevent a secondary alarm being triggered for free texts, so it's a bit special.
	 * 
	 * @return <code>true</code> if received message doesn't contains any text sequences matching any of the regular expressions set for primary alarm
	 *         triggering, else <code>false</code>.
	 */
	private boolean notPrimaryRegexAlarm() {
		boolean result = true;

		for (String primaryRegex : primaryRegexs) {
			if (primaryRegex != null && msgBody != null) {
				if ((primaryRegex.length() != 0) && (msgBody.length() != 0)) {
					// Compile regular expression into a pattern and try to find a match
					Pattern pattern = Pattern.compile(primaryRegex);
					Matcher matcher = pattern.matcher(msgBody);

					if (matcher.find()) {
						result = false;
					}
				}
			}
		}

		return result;
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