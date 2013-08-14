/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm;

import java.util.ArrayList;
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
 * @version 2.1
 * @since 0.9beta
 * @date 2013-08-14
 * 
 */
public class SmsReceiver extends BroadcastReceiver {
	// Log tag string
	private final String LOG_TAG = this.getClass().getSimpleName();

	// Objects needed for logging, shared preferences and noise handling
	private LogHandler logger = LogHandler.getInstance();
	private PreferencesHandler prefHandler = PreferencesHandler.getInstance();
	private NoiseHandler noiseHandler = NoiseHandler.getInstance();

	// String containing the primary listen number
	private String primaryListenNumber = "";

	// List of Strings containing secondaryListenNumbers
	private List<String> secondaryListenNumbers = new ArrayList<String>();

	// Variables needed to handle an incoming alarm properly
	private int primaryMessageToneId = 0;
	private int secondaryMessageToneId = 0;
	private boolean useOsSoundSettings = false;
	private boolean enableAlarmAck = false;
	private boolean playToneTwice = false;
	private boolean enableSmsAlarm = false;

	// Variable to store type of alarm as string
	private AlarmTypes alarmType;

	// To store incoming SMS phone number and body(message)
	private String msgHeader = "";
	private String msgBody = "";

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
	 * @see #getSmsReceivePrefs(Context)
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String, String, Throwable) logCatTxt(LogPriorities, String, String, Throwable)
	 * @see ax.ha.it.smsalarm.PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context) setPrefs(PrefKeys, PrefKeys, Object, Context)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		// Log message for debugging/information purpose
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onReceive()", "SMS received");

		// Retrieve shared preferences
		this.getSmsReceivePrefs(context);

		// Only if Sms Alarm is enabled
		if (this.enableSmsAlarm) {
			// Log that Sms Alarm is enabled
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onReceive()", "Sms Alarm is enabled, continue handle SMS");

			// Catch the SMS passed in
			Bundle bundle = intent.getExtras();
			SmsMessage[] msgs = null;

			if (bundle != null) {
				// Get some data from the SMS
				Object[] pdus = (Object[]) bundle.get("pdus");
				msgs = new SmsMessage[pdus.length];

				for (int i = 0; i < msgs.length; i++) {
					msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
					this.msgHeader = msgs[i].getOriginatingAddress();
					this.msgBody += msgs[i].getMessageBody().toString();
				}

				/*
				 * If number has a country code, recognized by +NUM, replace it
				 * with 0. Following countries are supported: Finland, Åland,
				 * Sweden, Norway, Denmark, France and Germany. In each case
				 * information is logged.
				 */
				if (this.msgHeader.contains("+358")) { // <--Finland
					this.msgHeader = this.msgHeader.replace("+358", "0");
					this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onReceive()", "Countrycode +358 found, replaced by 0");
				} else if (this.msgHeader.contains("+33")) { // <--France
					this.msgHeader = this.msgHeader.replace("+33", "0");
					this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onReceive()", "Countrycode +33 found, replaced by 0");
				} else if (this.msgHeader.contains("+45")) { // <--Denmark
					this.msgHeader = this.msgHeader.replace("+45", "0");
					this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onReceive()", "Countrycode +45 found, replaced by 0");
				} else if (this.msgHeader.contains("+46")) { // <--Sweden
					this.msgHeader = this.msgHeader.replace("+46", "0");
					this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onReceive()", "Countrycode +46 found, replaced by 0");
				} else if (this.msgHeader.contains("+47")) { // <--Norway
					this.msgHeader = this.msgHeader.replace("+47", "0");
					this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onReceive()", "Countrycode +47 found, replaced by 0");
				} else if (this.msgHeader.contains("+49")) { // <--Germany
					this.msgHeader = this.msgHeader.replace("+49", "0");
					this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onReceive()", "Countrycode +49 found, replaced by 0");
				} else {
					this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onReceive()", "No countrycod found, nothing replaced");
				}

				// If we got a SMS from the same primary number as the application listens on
				if (this.msgHeader.equals(this.primaryListenNumber)) {
					// Log information
					this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onReceive()", "SMS fulfilled the criteria for a PRIMARY alarm. SMS received from: \"" + this.msgHeader + "\" with message: \"" + this.msgBody + "\"");

					try {
						// Put alarm type to shared preferences
						this.prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.LARM_TYPE_KEY, AlarmTypes.PRIMARY.ordinal(), context);
					} catch(IllegalArgumentException e) {
						logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":onReceive()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
					}
					
					// Set larm typ to this object
					this.alarmType = AlarmTypes.PRIMARY;

					// Continue handling of received SMS
					this.smsHandler(context);
				}
				// If list with secondary listen numbers is not empty check if we got a SMS from one of the secondaryListenNumbers
				else if (!this.secondaryListenNumbers.isEmpty()) {
					try {
						// Loop through each element in list
						for (int i = 0; i < this.secondaryListenNumbers.size(); i++) {
							/*
							 * If msg header equals a element in list application has 
							 * received a SMS from a secondary listen number
							 */
							if (this.msgHeader.equals(this.secondaryListenNumbers.get(i))) {
								// Log information
								this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onReceive()", "SMS fulfilled the criteria for a SECONDARY alarm. SMS received from: \"" + this.msgHeader + "\" with message: \"" + this.msgBody + "\"");
								
								try {
									// Put alarm type to shared preferences
									this.prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.LARM_TYPE_KEY, AlarmTypes.SECONDARY.ordinal(), context);
								} catch(IllegalArgumentException e) {
									logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":onReceive()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
								}

								// Set larm typ to this object
								this.alarmType = AlarmTypes.SECONDARY;

								// Continue handling of received SMS
								this.smsHandler(context);
							}
						}
					} catch (Exception e) {
						// Log exception
						this.logger.logCatTxt(LogPriorities.ERROR, this.LOG_TAG + ":onReceive()", "Failed to iterate through list of secondary alarms, operation endend with exception", e);
					}
				}
			}
		} else { // <--Sms Alarm isn't enabled
			// Log that Sms Alarm is enabled
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":onReceive()", "Sms Alarm is not enabled, do nothing");
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
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":smsHandler()", "ABORTED OPERATING SYSTEMS BROADCAST");
		
		// Declare and initialize database handler object and store alarm to database
		DatabaseHandler db = new DatabaseHandler(context);
		db.addAlarm(new Alarm(this.msgHeader, this.msgBody));
		// Get all alarms from database and log them to to html file
		logger.logAlarm(db.getAllAlarm(), context);
		
		// Update all widgets associated with this application
		WidgetProvider.updateWidgets(context);
		
		// PowerManager to detect whether screen is on or off, if it's off we need to wake it
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		if (!pm.isScreenOn()) {
			// Log message for debugging/information purpose
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":smsHandler()", "Screen is:\"OFF\", need to acquire WakeLock");
			// Wake up device by acquire a wakelock and then release it after given time
			WakeLocker.acquireAndRelease(context, 20000);
		}

		// Pattern for regular expression like this; dd.dd.dddd dd:dd:dd: d.d, alarm from alarmcentralen.ax has this pattern
		Pattern p = Pattern.compile("(\\d{2}).(\\d{2}).(\\d{4})(\\s)(\\d{2}):(\\d{2}):(\\d{2})(\\s)(\\d{1}).(\\d{1})");
		Matcher m = p.matcher(this.msgBody);

		// Due to previous abort we have to store the SMS manually in phones inbox
		ContentValues values = new ContentValues();
		values.put("address", this.msgHeader);
		values.put("body", this.msgBody);
		context.getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
		// Debug logging
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":smsHandler()", "SMS stored in devices inbox");

		// Play message tone and vibrate, different method calls depending on alarm type
		if (this.alarmType.equals(AlarmTypes.PRIMARY)) {
			this.noiseHandler.makeNoise(context, this.primaryMessageToneId, useOsSoundSettings, this.playToneTwice);
		} else if (this.alarmType.equals(AlarmTypes.SECONDARY)) {
			this.noiseHandler.makeNoise(context, this.secondaryMessageToneId, useOsSoundSettings, this.playToneTwice);
		} else {
			// UNSUPPORTED LARM TYPE OCCURRED
			this.logger.logCatTxt(LogPriorities.ERROR, this.LOG_TAG + ":smsHandler()", "An unsupported alarm type has occurred, can't decide what to do");
		}

		// If Alarm acknowledge is enabled and alarm type equals primary, store full alarm message
		if (this.enableAlarmAck && this.alarmType.equals(AlarmTypes.PRIMARY)) {
			// Debug logging
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":smsHandler()", "Alarm acknowledgement is enabled and alarm is of type primary, store full SMS to shared preferences");

			try {
				// Enable acknowledge is enabled and alarm is of type primary
				this.prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.FULL_MESSAGE_KEY, this.msgBody, context);
			} catch(IllegalArgumentException e) {
				logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":smsHandler()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
			}
		}
		
		// If message contain a string with correct pattern, remove the date and time stamp in message
		if (m.find()) {
			this.msgBody = this.msgBody.replace(m.group(1).toString() + "." + m.group(2).toString() + "." + m.group(3).toString() + m.group(4).toString() + m.group(5).toString() + ":" + m.group(6).toString() + ":" + m.group(7).toString() + m.group(8).toString() + m.group(9).toString() + "."
					+ m.group(10).toString(), "");
			// Debug logging
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":smsHandler()", "SMS cleaned from unnecessary information");
		}
		
		// Debug logging
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":smsHandler()", "Store SMS to shared preferences for show in notification bar");
		
		// Store message's body in shared prefs so it can be shown in notification
		try {
			this.prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.MESSAGE_KEY, this.msgBody, context);
		} catch(IllegalArgumentException e) {
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":smsHandler()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
		}

		// Acknowledge is enabled and it is a primary alarm, show acknowledge notification, else show "ordinary" notification
		if (this.enableAlarmAck && this.alarmType.equals(AlarmTypes.PRIMARY)) {
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":smsHandler()", "Preparing intent for the AcknowledgeNotificationHelper.class");				
			// Start intent, AcknowledgeNotificationHelper - a helper to show acknowledge notification
			Intent ackNotIntent = new Intent(context, AcknowledgeNotificationHelper.class);
			context.startService(ackNotIntent);
		} else {
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":smsHandler()", "Preparing intent for the NotificationHelper.class");		
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
		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":getSmsReceivePrefs()", "Start retrieving shared preferences needed by class SmsReceiver");
		
		try {
			// Get shared preferences needed by SmsReceiver
			this.primaryListenNumber = (String) this.prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_LISTEN_NUMBER_KEY, DataTypes.STRING, context);
			this.secondaryListenNumbers = (List<String>) this.prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.SECONDARY_LISTEN_NUMBERS_KEY, DataTypes.LIST, context);
			this.primaryMessageToneId = (Integer) this.prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_MESSAGE_TONE_KEY, DataTypes.INTEGER, context);
			this.secondaryMessageToneId = (Integer) this.prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.SECONDARY_MESSAGE_TONE_KEY, DataTypes.INTEGER, context, 1);
			this.useOsSoundSettings = (Boolean) this.prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.USE_OS_SOUND_SETTINGS_KEY, DataTypes.BOOLEAN, context);
			this.enableAlarmAck = (Boolean) this.prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.ENABLE_ACK_KEY, DataTypes.BOOLEAN, context);
			this.playToneTwice = (Boolean) this.prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.PLAY_TONE_TWICE_KEY, DataTypes.BOOLEAN, context);
			this.enableSmsAlarm = (Boolean) this.prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.ENABLE_SMS_ALARM_KEY, DataTypes.BOOLEAN, context, true);
		} catch(IllegalArgumentException e) {
			logger.logCatTxt(LogPriorities.ERROR, this.LOG_TAG + ":getSmsReceivePrefs()", "An unsupported datatype was given as argument to PreferencesHandler.getPrefs()", e);
		} 

		this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":getSmsReceivePrefs()", "Shared preferences retrieved");
	}
}