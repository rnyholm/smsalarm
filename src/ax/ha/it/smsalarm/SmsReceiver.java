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
import android.os.Bundle;
import android.os.PowerManager;
import android.telephony.SmsMessage;
import ax.ha.it.smsalarm.LogHandler.LogPriorities;
import ax.ha.it.smsalarm.PreferencesHandler.DataTypes;
import ax.ha.it.smsalarm.PreferencesHandler.PrefKeys;

/**
 * Class extending <code>BroadcastReceiver</code>, receives SMS and handles them
 * accordingly to application settings and SMS senders phone number.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.2
 * @since 0.9beta
 */
public class SmsReceiver extends BroadcastReceiver {
	// Log tag string
	private final String LOG_TAG = getClass().getSimpleName();

	// Objects needed for logging, shared preferences and noise handling
	private LogHandler logger = LogHandler.getInstance();
	private PreferencesHandler prefHandler = PreferencesHandler.getInstance();
	private NoiseHandler noiseHandler = NoiseHandler.getInstance();

	// Lists of Strings containing primary and secondaryListenNumbers
	private List<String> primaryListenSmsNumbers = new ArrayList<String>();
	private List<String> secondaryListenSmsNumbers = new ArrayList<String>();
	
	// List of Strings containing free texts triggering an alarm
	private List<String> primaryListenFreeTexts = new ArrayList<String>();
	private List<String> secondaryListenFreeTexts = new ArrayList<String>();

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
	 * Overridden method to receive <code>intent</code>, reacts on incoming SMS.
	 * This receiver take proper actions depending on application settings and
	 * SMS senders phone number.
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
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String, String, Throwable) logCatTxt(LogPriorities, String, String, Throwable)
	 * @see ax.ha.it.smsalarm.PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context) setPrefs(PrefKeys, PrefKeys, Object, Context)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		// Log message for debugging/information purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onReceive()", "SMS received");

		// Retrieve shared preferences
		getSmsReceivePrefs(context);

		// Only if Sms Alarm is enabled
		if (enableSmsAlarm) {
			// Log that Sms Alarm is enabled
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onReceive()", "Sms Alarm is enabled, continue handle SMS");

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
				checkAlarm(context);

				// Check if the income SMS was any alarm
				if (alarmType.equals(AlarmTypes.PRIMARY)) {
					// Log information
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onReceive()", "SMS fulfilled the criteria for a PRIMARY alarm, handle SMS further");

					// Continue handling of received SMS
					smsHandler(context);
				} else if (alarmType.equals(AlarmTypes.SECONDARY)) {
					// Log information
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onReceive()", "SMS fulfilled the criteria for a SECONDARY alarm, handle SMS further");

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
	 * Method to handle incoming SMS. Aborts the systems broadcast and stores
	 * the SMS in the device inbox. This method is also responsible for playing
	 * ringtone via <code>{@link ax.ha.it.smsalarm.NoiseHandler#makeNoise(Context, int, boolean, boolean)}</code>
	 * , vibrate and start <code>intent</code>.
	 * 
	 * @param context Context
	 * 
	 * @see #onReceive(Context, Intent)
	 * @see #getSmsReceivePrefs(Context)
	 * @see ax.ha.it.smsalarm.NoiseHandler#makeNoise(Context, int, boolean, boolean) makeNoise(Context, int, boolean, boolean)
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String, String) logCatTxt(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String, String, Throwable) logCatTxt(LogPriorities, String, String, Throwable)
	 * @see ax.ha.it.smsalarm.LogHandler#logAlarm(List, Context) logAlarm(List, Context)
	 * @see ax.ha.it.smsalarm.PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context) setPrefs(PrefKeys, PrefKeys, Object, Context)
	 * @see ax.ha.it.smsalarm.WakeLocker#acquire(Context) acquire(Context)
	 * @see ax.ha.it.smsalarm.WakeLocker#release() release()
	 * @see ax.ha.it.smsalarm.DatabaseHandler ax.ha.it.smsalarm.DatabaseHandler
	 * @see ax.ha.it.smsalarm.DatabaseHandler#addAlarm(Alarm) addAlarm(Alarm)
	 * @see ax.ha.it.smsalarm.DatabaseHandler#getAllAlarm() getAllAlarm()
	 * @see ax.ha.it.smsalarm.Alarm ax.ha.it.smsalarm.Alarm
	 * @see ax.ha.it.smsalarm.AlarmTypes ax.ha.it.smsalarm.AlarmTypes
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
			// Wake up device by acquire a wakelock and then release it after given time
			WakeLocker.acquireAndRelease(context, 20000);
		}

		// Pattern for regular expression like this; dd.dd.dddd dd:dd:dd: d.d, alarm from alarmcentralen.ax has this pattern
		Pattern p = Pattern.compile("(\\d{2}).(\\d{2}).(\\d{4})(\\s)(\\d{2}):(\\d{2}):(\\d{2})(\\s)(\\d{1}).(\\d{1})");
		Matcher m = p.matcher(msgBody);

		// Due to previous abort we have to store the SMS manually in phones inbox
		ContentValues values = new ContentValues();
		values.put("address", msgHeader);
		values.put("body", msgBody);
		context.getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
		// Debug logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":smsHandler()", "SMS stored in devices inbox");

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
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":smsHandler()", "Alarm acknowledgement is enabled and alarm is of type primary, store full SMS to shared preferences");

			try {
				// Enable acknowledge is enabled and alarm is of type primary
				prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.FULL_MESSAGE_KEY, msgBody, context);
			} catch(IllegalArgumentException e) {
				logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":smsHandler()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
			}
		}
		
		// If message contain a string with correct pattern, remove the date and time stamp in message
		if (m.find()) {
			msgBody = msgBody.replace(m.group(1).toString() + "." + m.group(2).toString() + "." + m.group(3).toString() + m.group(4).toString() + m.group(5).toString() + ":" + m.group(6).toString() + ":" + m.group(7).toString() + m.group(8).toString() + m.group(9).toString() + "."
					+ m.group(10).toString(), "");
			// Debug logging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":smsHandler()", "SMS cleaned from unnecessary information");
		}
		
		// Debug logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":smsHandler()", "Store SMS to shared preferences for show in notification bar");
		
		// Store message's body in shared prefs so it can be shown in notification
		try {
			prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.MESSAGE_KEY, msgBody, context);
		} catch(IllegalArgumentException e) {
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":smsHandler()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
		}

		// Acknowledge is enabled and it is a primary alarm, show acknowledge notification, else show "ordinary" notification
		if (enableAlarmAck && alarmType.equals(AlarmTypes.PRIMARY)) {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":smsHandler()", "Preparing intent for the AcknowledgeNotificationHelper.class");				
			// Start intent, AcknowledgeNotificationHelper - a helper to show acknowledge notification
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
	 * Method used to get all shared preferences needed by class SmsReceiver
	 * 
	 * @param context
	 *            Context
	 * 
	 * @see #onReceive(Context, Intent)
	 * @see #smsHandler(Context)
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String, String, Throwable) logCatTxt(LogPriorities, String, String, Throwable)
	 * @see ax.ha.it.smsalarm.PreferencesHandler#getPrefs(PrefKeys, PrefKeys, DataTypes, Context) getPrefs(PrefKeys, PrefKeys, DataTypes, Context)
	 */
	@SuppressWarnings("unchecked")
	private void getSmsReceivePrefs(Context context) {
		// Some logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getSmsReceivePrefs()", "Start retrieving shared preferences needed by class SmsReceiver");
		
		try {
			// Get shared preferences needed by SmsReceiver
			primaryListenSmsNumbers = (List<String>) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_LISTEN_NUMBERS_KEY, DataTypes.LIST, context);
			secondaryListenSmsNumbers = (List<String>) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.SECONDARY_LISTEN_NUMBERS_KEY, DataTypes.LIST, context);
			primaryListenFreeTexts = (List<String>) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_LISTEN_FREE_TEXTS_KEY, DataTypes.LIST, context);
			secondaryListenFreeTexts = (List<String>) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.SECONDARY_LISTEN_FREE_TEXTS_KEY, DataTypes.LIST, context);
			primaryMessageToneId = (Integer) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_MESSAGE_TONE_KEY, DataTypes.INTEGER, context);
			secondaryMessageToneId = (Integer) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.SECONDARY_MESSAGE_TONE_KEY, DataTypes.INTEGER, context, 1);
			useOsSoundSettings = (Boolean) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.USE_OS_SOUND_SETTINGS_KEY, DataTypes.BOOLEAN, context);
			enableAlarmAck = (Boolean) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.ENABLE_ACK_KEY, DataTypes.BOOLEAN, context);
			playToneTwice = (Boolean) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.PLAY_TONE_TWICE_KEY, DataTypes.BOOLEAN, context);
			enableSmsAlarm = (Boolean) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.ENABLE_SMS_ALARM_KEY, DataTypes.BOOLEAN, context, true);
		} catch(IllegalArgumentException e) {
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":getSmsReceivePrefs()", "An unsupported datatype was given as argument to PreferencesHandler.getPrefs()", e);
		} 

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getSmsReceivePrefs()", "Shared preferences retrieved");
	}
	
	/**
	 * To check if income SMS fulfill criteria for either a <b><i>PRIMARY</i></b>
	 * or <b><i>SECONDARY</i></b> alarm.
	 * 
	 * @param context
	 *            Context
	 * 
	 * @see #checkSmsNumberAlarm(Context)
	 * @see #checkFreeTextAlarm(Context)
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	private void checkAlarm(Context context) {
		// Log message for debugging/information purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":checkAlarm()", "Checking if income SMS is an alarm");
		
		// Figure out if we got an alarm
		checkSmsNumberAlarm(context);
		checkFreeTextAlarm(context);
	}
	
	/**
	 * To check if received SMS is any alarm.
	 * The check is done by a equality control of the senders phone number
	 * and the phone numbers read from <code>SharedPreferences</code>. 
	 * 
	 * @param context
	 *            Context
	 * 
	 * @see #setAlarmType(AlarmTypes, Context)
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String, String, Throwable) logCatTxt(LogPriorities, String, String, Throwable)
	 * @see ax.ha.it.smsalarm.PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context) setPrefs(PrefKeys, PrefKeys, Object, Context)
	 */
	private void checkSmsNumberAlarm(Context context) {
		// Log message for debugging/information purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":checkSmsNumberAlarm()", "Checking if sender of income SMS should trigger an alarm");
		
		boolean isAlarm = false;
		
		for (String primaryListenSmsNumber : primaryListenSmsNumbers) {
			if (msgHeader.equals(primaryListenSmsNumber)) {
				// Log information
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":checkSmsNumberAlarm()", "SMS fulfilled the criteria for a PRIMARY alarm. SMS received from: \"" + msgHeader + "\" with message: \"" + msgBody + "\", triggered on number: " + primaryListenSmsNumber);
				// Set helper variable
				isAlarm = true;
			}
		}
		
		// Only set alarm type if we are sure that income SMS triggered on any primary listen sms number
		if (isAlarm) {
			// Set correct AlarmType
			setAlarmType(AlarmTypes.PRIMARY, context);			
		}
		
		// Only check if income SMS hasn't already been checked as PRIMARY alarm
		if (!AlarmTypes.PRIMARY.equals(alarmType)) {			
			for (String secondaryListenSmsNumber : secondaryListenSmsNumbers) {
				// If msg header equals a element in list application has received a SMS from a secondary listen number
				if (msgHeader.equals(secondaryListenSmsNumber)) {
					// Log information
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":checkSmsNumberAlarm()", "SMS fulfilled the criteria for a SECONDARY alarm. SMS received from: \"" + msgHeader + "\" with message: \"" + msgBody + "\", triggered on number: " + secondaryListenSmsNumber);
					isAlarm = true;
				}
			}
			
			if (isAlarm) {
				setAlarmType(AlarmTypes.SECONDARY, context);		
			}
		}
	}
	
	/**
	 * To check if received SMS is any alarm.
	 * The check is done by a controlling if any of the free texts(words) is found 
	 * in received SMS.
	 * 
	 * @param context
	 *            Context
	 * 
	 * @see #setTriggerText(String)
	 * @see #setAlarmType(AlarmTypes, Context)
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String, String, Throwable) logCatTxt(LogPriorities, String, String, Throwable)
	 * @see ax.ha.it.smsalarm.PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context) setPrefs(PrefKeys, PrefKeys, Object, Context)
	 */
	private void checkFreeTextAlarm(Context context) {
		// Log message for debugging/information purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":checkFreeTextAlarm()", "Checking if income SMS is holding any text triggering a free text alarm");
		
		// Helper variable to avoid setting variable in each iteration
		boolean isAlarm = false;
		
		for (String primaryFreeText : primaryListenFreeTexts) {
			if (findWordEqualsIgnore(primaryFreeText, msgBody)) {
				// Log information
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":checkFreeTextAlarm()", "SMS fulfilled the criteria for a PRIMARY alarm. SMS received from: \"" + msgHeader + "\" with message: \"" + msgBody + "\", triggered on freetext: " + primaryFreeText);
				
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
			for (String secondaryFreeText : secondaryListenFreeTexts) {
				if (findWordEqualsIgnore(secondaryFreeText, msgBody)) {
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":checkFreeTextAlarm()", "SMS fulfilled the criteria for a SECONDARY alarm. SMS received from: \"" + msgHeader + "\" with message: \"" + msgBody + "\", triggered on freetext: " + secondaryFreeText);					
					setTriggerText(secondaryFreeText);
					isAlarm = true;
				}
			}
			
			if (isAlarm) {
				setAlarmType(AlarmTypes.SECONDARY, context);		
			}
		}
	}
	
	/**
	 * Convenience method to set this objects <code>AlarmTypes</code>.
	 * 
	 * @param alarmType 
	 * 				Alarm type to be set.
	 * @param context 
	 * 				context
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String, String, Throwable) logCatTxt(LogPriorities, String, String, Throwable)
	 * @see ax.ha.it.smsalarm.PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context) setPrefs(PrefKeys, PrefKeys, Object, Context)
	 */
	private void setAlarmType(AlarmTypes alarmType, Context context) {
		// Log message for debugging/information purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setAlarmType()", "Setting alarm type to:\"" + alarmType.name() + "\"");
		
		// Set the given alarm type
		this.alarmType = alarmType;		
		
		try {
			// Put alarm type to shared preferences
			prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.LARM_TYPE_KEY, alarmType.ordinal(), context);
		} catch(IllegalArgumentException e) {
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":setAlarmType()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
		}				
	}
	
	/**
	 * Convenience method to set this objects trigger text.
	 *  
	 * @param triggerText Text to be set as trigger text.
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	private void setTriggerText(String triggerText) {
		// Log information
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setTriggerText()", "Setting trigger text:\"" + triggerText + "\"");
		
		// If empty just add trigger text else concatenate and add trigger text
		if (this.triggerText.isEmpty()) {
			this.triggerText = triggerText;
		} else {
			this.triggerText = this.triggerText + ", " + triggerText;
		}		
		
		// Log information
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setTriggerText()", "Trigger text set to:\"" + triggerText + "\"");
	}
	
	/**
	 * To check if <code>String</code>(textToParse) passed in as argument
	 * contains another <code>String</code>(wordToFind) passed in as argument.
	 * This method only checks whole words and not a <code>CharSequence</code>.
	 * Method is not case sensitive.
	 * 
	 * @param wordToFind
	 *            Word to find.
	 * @param textToParse
	 *            Text to look for word in.
	 * 
	 * @return <code>true</code> if word is found else <code>false</code>.
	 * 
	 * @throws NullPointerException
	 *             if either or both params <code>wordToFind</code> and
	 *             <code>textToParse</code> is null.
	 * @throws IllegalArgumentException
	 *             if either or both params <code>wordToFind</code> and
	 *             <code>textToParse</code> is empty.
	 */
	private boolean findWordEqualsIgnore(String wordToFind, String textToParse) {
		if (wordToFind != null && textToParse != null) {
			if (!wordToFind.isEmpty() && !textToParse.isEmpty()) {
				List<String> words = new ArrayList<String>();
				words = Arrays.asList(textToParse.split(" "));

				for (String word : words) {
					if (wordToFind.equalsIgnoreCase(word)) {
						return true;
					}
				}

				return false;
			} else {
				throw new IllegalArgumentException("One or both of given arguments are empty. Arguments --> wordToFind = " + wordToFind + ", textToParse = " + textToParse);
			}
		} else {
			throw new NullPointerException("One or both of given arguments are null. Arguments --> wordToFind = " + wordToFind + ", textToParse = " + textToParse);
		}
	}
}