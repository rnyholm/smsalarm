/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.telephony.SmsMessage;
import ax.ha.it.smsalarm.enumeration.AlarmTypes;
import ax.ha.it.smsalarm.handler.DatabaseHandler;
import ax.ha.it.smsalarm.handler.KitKatHandler;
import ax.ha.it.smsalarm.handler.LogHandler;
import ax.ha.it.smsalarm.handler.NoiseHandler;
import ax.ha.it.smsalarm.handler.PreferencesHandler;
import ax.ha.it.smsalarm.handler.LogHandler.LogPriorities;
import ax.ha.it.smsalarm.handler.PreferencesHandler.DataTypes;
import ax.ha.it.smsalarm.handler.PreferencesHandler.PrefKeys;

/**
 * Class extending <code>BroadcastReceiver</code>, receives sms and handles them accordingly to
 * application settings and sms senders phone number.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.2.1
 * @since 0.9beta
 */
public class SmsReceiver extends BroadcastReceiver {
	// Log tag string
	private final String LOG_TAG = getClass().getSimpleName();

	// How long we should acquire wakelock
	private final int WAKE_LOCKER_ACQUIRE_TIME = 20000;

	// Objects needed for logging, shared preferences and noise handling
	private final LogHandler logger = LogHandler.getInstance();
	private final PreferencesHandler prefHandler = PreferencesHandler.getInstance();
	private final NoiseHandler noiseHandler = NoiseHandler.getInstance();
	private final KitKatHandler kitKatHandler = KitKatHandler.getInstance();

	// Lists of Strings containing primary- and secondary sms numbers
	private List<String> primarySmsNumbers = new ArrayList<String>();
	private List<String> secondarySmsNumbers = new ArrayList<String>();

	// List of Strings containing free texts triggering an alarm
	private List<String> primaryFreeTexts = new ArrayList<String>();
	private List<String> secondaryFreeTexts = new ArrayList<String>();

	// Variables needed to handle an incoming alarm properly
	private int primaryMessageToneId = 0;
	private int secondaryMessageToneId = 0;
	private boolean useOsSoundSettings = false;
	private boolean enableAlarmAck = false;
	private boolean playToneTwice = false;
	private boolean enableSmsAlarm = false;

	// Variable to store type of alarm as string
	private AlarmTypes alarmType = AlarmTypes.UNDEFINED;

	// To store incoming SMS phone number and body(message)
	private String msgHeader = "";
	private String msgBody = "";

	// Text which triggered an alarm if freetext triggering is used
	private String triggerText = "";

	/**
	 * Overridden method to receive <code>intent</code>, reacts on incoming sms. This receiver take
	 * proper actions depending on application settings and sms senders phone number.
	 * 
	 * @param context
	 *            Context
	 * @param intent
	 *            Intent
	 * 
	 * @see #smsHandler(Context)
	 * @see #removeCountryCode()
	 * @see #checkAlarm(Context)
	 * @see #getSmsReceivePrefs(Context)
	 * @see NoiseHandler#getRingerModeHandler()
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities,
	 *      String, String)
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCatTxt(LogPriorities, String, String, Throwable)
	 *      logCatTxt(LogPriorities, String, String, Throwable)
	 * @see ax.ha.it.smsalarm.handler.PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context)
	 *      setPrefs(PrefKeys, PrefKeys, Object, Context)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		// Log message for debugging/information purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onReceive()", "Sms received");

		// Retrieve shared preferences
		getSmsReceivePrefs(context);

		// Only if Sms Alarm is enabled
		if (enableSmsAlarm) {
			// Log that Sms Alarm is enabled
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onReceive()", "Sms Alarm is enabled, continue handle Sms");

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
					// If Android API level is greater or equals to KitKat
					// Necessary that we do check this as soon as possible
					if (isKitKatOrHigher()) {
						// Log message for debugging/information purpose
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onReceive()", "Android version \"KitKat\" or higher detected, some special treatment needed");
						kitKatHandler.handleKitKat(context);
					}
				}

				// Check if the income SMS was any alarm
				if (alarmType.equals(AlarmTypes.PRIMARY)) {
					// Log information
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onReceive()", "SMS fulfilled the criteria for a PRIMARY alarm, handle Sms further");

					// Continue handling of received SMS
					smsHandler(context);
				} else if (alarmType.equals(AlarmTypes.SECONDARY)) {
					// Log information
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onReceive()", "SMS fulfilled the criteria for a SECONDARY alarm, handle Sms further");

					// Continue handling of received SMS
					smsHandler(context);
				} else {
					// Log information
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onReceive()", "SMS with AlarmType: \"UNDEFINED\" received, do nothing");
				}
			}
		} else { // <--Sms Alarm isn't enabled
			// Log that Sms Alarm is not enabled
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onReceive()", "Sms Alarm is not enabled, do nothing");
		}
	}

	/**
	 * Method to handle incoming sms. Aborts the systems broadcast and stores the sms in the device
	 * inbox. This method is also responsible for playing ringtone via
	 * <code>{@link ax.ha.it.smsalarm.handler.NoiseHandler#makeNoise(Context, int, boolean, boolean)}</code>
	 * , vibrate and start <code>intent</code>.
	 * 
	 * @param context
	 *            Context
	 * 
	 * @see #onReceive(Context, Intent)
	 * @see #getSmsReceivePrefs(Context)
	 * @see ax.ha.it.smsalarm.handler.NoiseHandler#makeNoise(Context, int, boolean, boolean)
	 *      makeNoise(Context, int, boolean, boolean)
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities,
	 *      String, String)
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCatTxt(LogPriorities, String, String)
	 *      logCatTxt(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCatTxt(LogPriorities, String, String, Throwable)
	 *      logCatTxt(LogPriorities, String, String, Throwable)
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logAlarm(List, Context) logAlarm(List, Context)
	 * @see ax.ha.it.smsalarm.handler.PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context)
	 *      setPrefs(PrefKeys, PrefKeys, Object, Context)
	 * @see ax.ha.it.smsalarm.WakeLocker#acquire(Context) acquire(Context)
	 * @see ax.ha.it.smsalarm.WakeLocker#release() release()
	 * @see ax.ha.it.smsalarm.handler.DatabaseHandler ax.ha.it.smsalarm.DatabaseHandler
	 * @see ax.ha.it.smsalarm.handler.DatabaseHandler#addAlarm(Alarm) addAlarm(Alarm)
	 * @see ax.ha.it.smsalarm.handler.DatabaseHandler#getAllAlarm() getAllAlarm()
	 * @see ax.ha.it.smsalarm.Alarm ax.ha.it.smsalarm.Alarm
	 * @see ax.ha.it.smsalarm.enumeration.AlarmTypes ax.ha.it.smsalarm.AlarmTypes
	 */
	private void smsHandler(Context context) {
		// To prevent the OS from ever see the incoming SMS
		abortBroadcast();

		// Log message for debugging/information purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":smsHandler()", "ABORTED OPERATING SYSTEMS BROADCAST");

		// Declare and initialize database handler object and store alarm to database
		DatabaseHandler db = new DatabaseHandler(context);
		db.addAlarm(new Alarm(msgHeader, msgBody, triggerText, alarmType));
		// Get all alarms from database and log them to to html file
		logger.logAlarm(db.getAllAlarm(), context);

		// Update all widgets associated with this application
		WidgetProvider.updateWidgets(context);

		// PowerManager to detect whether screen is on or off, if it's off we need to wake it
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		if (!pm.isScreenOn()) {
			// Log message for debugging/information purpose
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":smsHandler()", "Screen is:\"OFF\", need to acquire WakeLock");

			// Wake up device by acquire a wakelock and then release it after given time, the time
			// depends on if device runs on KitKat(or higher) or not
			if (isKitKatOrHigher()) {
				WakeLocker.acquireAndRelease(context, (WAKE_LOCKER_ACQUIRE_TIME + kitKatHandler.getRingerModeDelay()));
			} else {
				WakeLocker.acquireAndRelease(context, WAKE_LOCKER_ACQUIRE_TIME);
			}
		}

		// Pattern for regular expression like this; dd.dd.dddd dd:dd:dd: d.d, alarm from
		// alarmcentralen.ax has this pattern
		Pattern p = Pattern.compile("(\\d{2}).(\\d{2}).(\\d{4})(\\s)(\\d{2}):(\\d{2}):(\\d{2})(\\s)(\\d{1}).(\\d{1})");
		Matcher m = p.matcher(msgBody);

		// Due to previous abort we have to store the sms manually in phones inbox
		ContentValues values = new ContentValues();
		values.put("address", msgHeader);
		values.put("body", msgBody);
		context.getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
		// Debug logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":smsHandler()", "Sms stored in devices inbox");

		// Play message tone and vibrate, different method calls depending on alarm type
		if (alarmType.equals(AlarmTypes.PRIMARY)) {
			noiseHandler.makeNoise(context, primaryMessageToneId, useOsSoundSettings, playToneTwice);
		} else if (alarmType.equals(AlarmTypes.SECONDARY)) {
			noiseHandler.makeNoise(context, secondaryMessageToneId, useOsSoundSettings, playToneTwice);
		} else {
			// UNSUPPORTED LARM TYPE OCCURRED
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":smsHandler()", "An unsupported alarm type has occurred, can't decide what to do");
		}

		// If Alarm acknowledge is enabled and alarm type equals primary, store full alarm message
		if (enableAlarmAck && alarmType.equals(AlarmTypes.PRIMARY)) {
			// Debug logging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":smsHandler()", "Alarm acknowledgement is enabled and alarm is of type PRIMARY, store full sms to shared preferences");

			try {
				// Enable acknowledge is enabled and alarm is of type primary
				prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.FULL_MESSAGE_KEY, msgBody, context);
			} catch (IllegalArgumentException e) {
				logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":smsHandler()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
			}
		}

		// If message contain a string with correct pattern, remove the date and time stamp in
		// message
		if (m.find()) {
			msgBody = msgBody.replace(m.group(1).toString() + "." + m.group(2).toString() + "." + m.group(3).toString() + m.group(4).toString() + m.group(5).toString() + ":" + m.group(6).toString() + ":" + m.group(7).toString() + m.group(8).toString() + m.group(9).toString() + "." + m.group(10).toString(), "");
			// Debug logging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":smsHandler()", "Sms cleaned from unnecessary information");
		}

		// Debug logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":smsHandler()", "Store sms to shared preferences for show in notification bar");

		// Store message's body in shared prefs so it can be shown in notification
		try {
			prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.MESSAGE_KEY, msgBody, context);
		} catch (IllegalArgumentException e) {
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":smsHandler()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
		}

		// Acknowledge is enabled and it is a primary alarm, show acknowledge notification, else
		// show "ordinary" notification
		if (enableAlarmAck && alarmType.equals(AlarmTypes.PRIMARY)) {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":smsHandler()", "Preparing intent for the AcknowledgeNotificationHelper.class");
			// Start intent, AcknowledgeNotificationHelper - a helper to show acknowledge
			// notification
			Intent ackNotIntent = new Intent(context, AcknowledgeNotificationHelper.class);
			context.startService(ackNotIntent);
		} else {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":smsHandler()", "Preparing intent for the NotificationHelper.class");
			// Start intent, NotificationHelper - a helper to show notification
			Intent notIntent = new Intent(context, NotificationHelper.class);
			context.startService(notIntent);
		}
	}

	/**
	 * Method used to get all shared preferences needed by class SmsReceiver.
	 * 
	 * @param context
	 *            Context
	 * 
	 * @see #onReceive(Context, Intent)
	 * @see #smsHandler(Context)
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities,
	 *      String, String)
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCatTxt(LogPriorities, String, String, Throwable)
	 *      logCatTxt(LogPriorities, String, String, Throwable)
	 * @see ax.ha.it.smsalarm.handler.PreferencesHandler#getPrefs(PrefKeys, PrefKeys, DataTypes, Context)
	 *      getPrefs(PrefKeys, PrefKeys, DataTypes, Context)
	 */
	@SuppressWarnings("unchecked")
	private void getSmsReceivePrefs(Context context) {
		// Some logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getSmsReceivePrefs()", "Start retrieving shared preferences needed by class SmsReceiver");

		try {
			// Get shared preferences needed by SmsReceiver
			primarySmsNumbers = (List<String>) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_LISTEN_NUMBERS_KEY, DataTypes.LIST, context);
			secondarySmsNumbers = (List<String>) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.SECONDARY_LISTEN_NUMBERS_KEY, DataTypes.LIST, context);
			primaryFreeTexts = (List<String>) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_LISTEN_FREE_TEXTS_KEY, DataTypes.LIST, context);
			secondaryFreeTexts = (List<String>) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.SECONDARY_LISTEN_FREE_TEXTS_KEY, DataTypes.LIST, context);
			primaryMessageToneId = (Integer) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_MESSAGE_TONE_KEY, DataTypes.INTEGER, context);
			secondaryMessageToneId = (Integer) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.SECONDARY_MESSAGE_TONE_KEY, DataTypes.INTEGER, context, 1);
			useOsSoundSettings = (Boolean) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.USE_OS_SOUND_SETTINGS_KEY, DataTypes.BOOLEAN, context);
			enableAlarmAck = (Boolean) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.ENABLE_ACK_KEY, DataTypes.BOOLEAN, context);
			playToneTwice = (Boolean) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.PLAY_TONE_TWICE_KEY, DataTypes.BOOLEAN, context);
			enableSmsAlarm = (Boolean) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.ENABLE_SMS_ALARM_KEY, DataTypes.BOOLEAN, context, true);
		} catch (IllegalArgumentException e) {
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":getSmsReceivePrefs()", "An unsupported datatype was given as argument to PreferencesHandler.getPrefs()", e);
		}

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getSmsReceivePrefs()", "Shared preferences retrieved");
	}

	/**
	 * To check if income sms fulfill criteria for either a <b><i>PRIMARY</i></b> or
	 * <b><i>SECONDARY</i></b> alarm.
	 * 
	 * @param context
	 *            Context
	 * 
	 * @return <code>true</code> if income sms was an alarm else <code>false</code>.
	 * 
	 * @see #checkSmsAlarm(Context)
	 * @see #checkFreeTextAlarm(Context)
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities,
	 *      String, String)
	 */
	private boolean checkAlarm(Context context) {
		// Log message for debugging/information purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":checkAlarm()", "Checking if income sms is an alarm");

		// Figure out if we got an alarm and return appropriate value
		boolean isSmsAlarm = checkSmsAlarm(context);
		boolean isFreeTextAlarm = checkFreeTextAlarm(context);

		return isSmsAlarm || isFreeTextAlarm;
	}

	/**
	 * To check if received sms is any alarm. The check is done by a equality control of the senders
	 * phone number and the phone numbers read from <code>SharedPreferences</code>.
	 * 
	 * @param context
	 *            Context
	 * 
	 * @return <code>true</code> if income sms was an alarm else <code>false</code>.
	 * 
	 * @see #setAlarmType(AlarmTypes, Context)
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities,
	 *      String, String)
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCatTxt(LogPriorities, String, String, Throwable)
	 *      logCatTxt(LogPriorities, String, String, Throwable)
	 * @see ax.ha.it.smsalarm.handler.PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context)
	 *      setPrefs(PrefKeys, PrefKeys, Object, Context)
	 */
	private boolean checkSmsAlarm(Context context) {
		// Log message for debugging/information purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":checkSmsNumberAlarm()", "Checking if sender of income sms should trigger an alarm");

		// Helper variable to avoid setting variable in each iteration
		boolean isAlarm = false;

		for (String primarySmsNumber : primarySmsNumbers) {
			if (msgHeader.equals(primarySmsNumber)) {
				// Log information
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":checkSmsNumberAlarm()", "Sms fulfilled the criteria for a PRIMARY alarm. Sms received from: \"" + msgHeader + "\" with message: \"" + msgBody + "\", triggered on number: " + primarySmsNumber);
				// Set helper variable
				isAlarm = true;
			}
		}

		// Only set alarm type if we are sure that income sms triggered on any primary sms number
		if (isAlarm) {
			// Set correct AlarmType
			setAlarmType(AlarmTypes.PRIMARY, context);
		}

		// Only check if income sms hasn't already been checked as PRIMARY alarm
		if (!AlarmTypes.PRIMARY.equals(alarmType)) {
			for (String secondarySmsNumber : secondarySmsNumbers) {
				// If msg header equals a element in list application has received a SMS from a
				// secondary number
				if (msgHeader.equals(secondarySmsNumber)) {
					// Log information
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":checkSmsNumberAlarm()", "Sms fulfilled the criteria for a SECONDARY alarm. Sms received from: \"" + msgHeader + "\" with message: \"" + msgBody + "\", triggered on number: " + secondarySmsNumber);
					isAlarm = true;
				}
			}

			if (isAlarm) {
				setAlarmType(AlarmTypes.SECONDARY, context);
			}
		}

		return isAlarm;
	}

	/**
	 * To check if received Sms is any alarm. The check is done by a controlling if any of the free
	 * texts(words) is found in received Sms.
	 * 
	 * @param context
	 *            Context
	 * 
	 * @return <code>true</code> if income sms was an alarm else <code>false</code>.
	 * 
	 * @see #setTriggerText(String)
	 * @see #setAlarmType(AlarmTypes, Context)
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities,
	 *      String, String)
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCatTxt(LogPriorities, String, String, Throwable)
	 *      logCatTxt(LogPriorities, String, String, Throwable)
	 * @see ax.ha.it.smsalarm.handler.PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context)
	 *      setPrefs(PrefKeys, PrefKeys, Object, Context)
	 */
	private boolean checkFreeTextAlarm(Context context) {
		// Log message for debugging/information purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":checkFreeTextAlarm()", "Checking if income sms is holding any text triggering a free text alarm");

		// Helper variable to avoid setting variable in each iteration
		boolean isAlarm = false;

		for (String primaryFreeText : primaryFreeTexts) {
			if (findWordEqualsIgnore(primaryFreeText, msgBody)) {
				// Log information
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":checkFreeTextAlarm()", "Sms fulfilled the criteria for a PRIMARY alarm. Sms received from: \"" + msgHeader + "\" with message: \"" + msgBody + "\", triggered on freetext: " + primaryFreeText);

				// Set correct trigger text
				setTriggerText(primaryFreeText);

				// Set helper variable
				isAlarm = true;
			}
		}

		// Only set alarm type if we are sure that income SMS triggered on free text
		if (isAlarm) {
			// Set correct AlarmType
			setAlarmType(AlarmTypes.PRIMARY, context);
		}

		// Only check if income SMS hasn't already been checked as PRIMARY alarm
		if (!AlarmTypes.PRIMARY.equals(alarmType)) {
			for (String secondaryFreeText : secondaryFreeTexts) {
				if (findWordEqualsIgnore(secondaryFreeText, msgBody)) {
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":checkFreeTextAlarm()", "Sms fulfilled the criteria for a SECONDARY alarm. Sms received from: \"" + msgHeader + "\" with message: \"" + msgBody + "\", triggered on freetext: " + secondaryFreeText);
					setTriggerText(secondaryFreeText);
					isAlarm = true;
				}
			}

			if (isAlarm) {
				setAlarmType(AlarmTypes.SECONDARY, context);
			}
		}

		return isAlarm;
	}

	/**
	 * Convenience method to set this objects <code>AlarmTypes</code>.
	 * 
	 * @param alarmType
	 *            Alarm type to be set.
	 * @param context
	 *            context
	 * 
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities,
	 *      String, String)
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCatTxt(LogPriorities, String, String, Throwable)
	 *      logCatTxt(LogPriorities, String, String, Throwable)
	 * @see ax.ha.it.smsalarm.handler.PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context)
	 *      setPrefs(PrefKeys, PrefKeys, Object, Context)
	 */
	private void setAlarmType(AlarmTypes alarmType, Context context) {
		// Log message for debugging/information purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setAlarmType()", "Setting alarm type to:\"" + alarmType.name() + "\"");

		// Set the given alarm type
		this.alarmType = alarmType;

		try {
			// Put alarm type to shared preferences
			prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.LARM_TYPE_KEY, alarmType.ordinal(), context);
		} catch (IllegalArgumentException e) {
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":setAlarmType()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
		}
	}

	/**
	 * Convenience method to set this objects trigger text.
	 * 
	 * @param triggerText
	 *            Text to be set as trigger text.
	 * 
	 * @see ax.ha.it.smsalarm.handler.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities,
	 *      String, String)
	 */
	private void setTriggerText(String triggerText) {
		// Log information
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setTriggerText()", "Setting trigger text:\"" + triggerText + "\"");

		// If empty just add trigger text else concatenate and add trigger text
		if (this.triggerText.length() == 0) {
			this.triggerText = triggerText;
		} else {
			this.triggerText = this.triggerText + ", " + triggerText;
		}

		// Log information
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setTriggerText()", "Trigger text set to:\"" + triggerText + "\"");
	}

	/**
	 * To check if <code>String</code>(textToParse) passed in as argument contains another
	 * <code>String</code>(wordToFind) passed in as argument. This method only checks whole words
	 * and not a <code>CharSequence</code>. Method is not case sensitive.
	 * 
	 * @param wordToFind
	 *            Word to find.
	 * @param textToParse
	 *            Text to look for word in.
	 * 
	 * @return <code>true</code> if word is found else <code>false</code>.
	 * 
	 * @throws NullPointerException
	 *             if either or both params <code>wordToFind</code> and <code>textToParse</code> is
	 *             null.
	 * @throws IllegalArgumentException
	 *             if either or both params <code>wordToFind</code> and <code>textToParse</code> is
	 *             empty.
	 */
	private boolean findWordEqualsIgnore(String wordToFind, String textToParse) {
		if (wordToFind != null && textToParse != null) {
			if ((wordToFind.length() != 0) && (textToParse.length() != 0)) {
				List<String> words = new ArrayList<String>();
				words = Arrays.asList(textToParse.split(" "));

				for (String word : words) {
					if (wordToFind.equalsIgnoreCase(word)) {
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findWordEqualsIgnore()", "Word \"" + wordToFind + "\" was found in text=\"" + textToParse + "\", returning true");
						return true;
					}
				}

				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findWordEqualsIgnore()", "Word \"" + wordToFind + "\" was not found in text=\"" + textToParse + "\", returning false");
				return false;
			} else {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findWordEqualsIgnore()", "WordToFind and/or textToPArse are empty, wordToFind=\"" + wordToFind + "\", textToParse=\"" + textToParse + "\"");
				return false;
			}
		} else {
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":findWordEqualsIgnore()", "WordToFind and/or textToPArse is null, wordToFind=\"" + wordToFind + "\", textToParse=\"" + textToParse + "\"");
			return false;
		}
	}

	/**
	 * Convenience method to figure out if <code>Build.VERSION.SDK_INT</code> equals to
	 * <code>Build.VERSION_CODES.KITKAT</code> or higher.
	 * 
	 * @return <code>true</code> if <code>Build.VERSION.SDK_INT</code> equals to
	 *         <code>Build.VERSION_CODES.KITKAT</code> else <code>false</code>.
	 * 
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 */
	private boolean isKitKatOrHigher() {
		// Android API level is greater or equals to KitKat
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":isKitKatOrHigher()", "Android version \"KitKat\" or higher detected, return true");
			return true;
		}

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":isKitKatOrHigher()", "Android version lower than \"KitKat\" detected, return false");
		return false;
	}
}