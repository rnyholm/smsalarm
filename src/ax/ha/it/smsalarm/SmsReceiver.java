/*
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
import android.telephony.SmsMessage;
import ax.ha.it.smsalarm.LogHandler.LogPriorities;

/**
 * Class extending <code>BroadcastReceiver</code>, receives SMS and handles them
 * accordingly to application settings and SMS senders phone number.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.1
 * @since 0.9beta
 * @date 2013-06-30
 * 
 */
public class SmsReceiver extends BroadcastReceiver {

	// Log tag string
	private final String LOG_TAG = "SmsReceiver";

	// Constants representing different datatypes used by class
	// PreferencesHandler
	private final int INTEGER = 0;
	private final int STRING = 1;
	private final int BOOLEAN = 2;
	private final int LIST = 3;

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
	private String type = "";

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
	 * @exception Exception
	 *                If List is out of bounds
	 * 
	 * @see #smsHandler(Context)
	 * @see #getSmsReceivePrefs(Context)
	 * @see {@link LogHandler#logCat(ax.ha.it.smsalarm.LogHandler.LogPriorities, String, String)}
	 * @see {@link LogHandler#logCatTxt(ax.ha.it.smsalarm.LogHandler.LogPriorities, String, String)}
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

			// Variable indicating if an alarm is triggered or not
			boolean alarmTriggered = false;

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

				// If we got a SMS from the same primary number as the
				// application
				// listens on
				if (this.msgHeader.equals(this.primaryListenNumber)) {
					// Log information
					this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":onReceive()", "SMS fulfilled the criteria for a PRIMARY alarm. SMS received from: \"" + this.msgHeader + "\" with message: \"" + this.msgBody + "\"");

					// Put alarm type to shared preferences
					this.prefHandler.setPrefs(this.prefHandler.getSHARED_PREF(), this.prefHandler.getLARM_TYPE_KEY(), "primary", context);

					// Set larm typ to this object
					this.type = "primary";

					// Continue handling of received SMS
					this.smsHandler(context);

					// Set flag indicating that an alarm has been triggered
					alarmTriggered = true;
				}
				// If list with secondary listen numbers is not empty check if
				// we
				// got a SMS from one of the secondaryListenNumbers
				else if (!this.secondaryListenNumbers.isEmpty()) {
					try {
						// Loop through each element in list
						for (int i = 0; i < this.secondaryListenNumbers.size(); i++) {

							// If msg header equals a element in list
							// application
							// has received a SMS from a secondary listen number
							if (this.msgHeader.equals(this.secondaryListenNumbers.get(i))) {
								// Log information
								this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":onReceive()", "SMS fulfilled the criteria for a SECONDARY alarm. SMS received from: \"" + this.msgHeader + "\" with message: \"" + this.msgBody + "\"");

								// Put alarm type to shared preferences
								this.prefHandler.setPrefs(this.prefHandler.getSHARED_PREF(), this.prefHandler.getLARM_TYPE_KEY(), "secondary", context);

								// Set larm typ to this object
								this.type = "secondary";

								// Continue handling of received SMS
								this.smsHandler(context);

								// Set flag indicating that an alarm has been
								// triggered
								alarmTriggered = true;
							}
						}
					} catch (Exception e) {
						// An exception occurred print stack trace and log it
						e.printStackTrace();
						this.logger.logCatTxt(this.logger.getERROR(), this.LOG_TAG + ":onReceive()", "Failed to iterate through list of secondary alarms, operation endend with exception", e);
					}
				}

				// If no alarm has been triggered log it with proper message
				if (!alarmTriggered) {
					this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":onReceive()", "SMS didn't fulfill criterias for either PRIMARY or SECONDARY alarm, no action taken");
				}
			}
		} else { // <--Sms Alarm isn't enabled
			// Log that Sms Alarm is enabled
			this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":onReceive()", "Sms Alarm is not enabled, do nothing");
		}
	}

	/**
	 * <Method to handle incoming SMS. Aborts the systems broadcast and stores
	 * the SMS in the device inbox. This method is also responsible for playing
	 * ringtone via
	 * <code>ax.ha.it.smsalarm.SmsAlarm#playMsgToneVibrate(Context, int, boolean)</code>
	 * , vibrate and start <code>intent</code>.
	 * 
	 * @param context
	 *            Context
	 * 
	 * @see ax.ha.it.smsalarm#NoiseHandler.makeNoise(Context, int, boolean,
	 *      boolean)
	 * @see #onReceive(Context, Intent)
	 * @see #getSmsReceivePrefs(Context)
	 */
	private void smsHandler(Context context) {
		// To prevent the OS from ever see the incoming SMS
		abortBroadcast();

		// Log message for debugging/information purpose
		this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":smsHandler()", "ABORTED OPERATING SYSTEMS BROADCAST");

		// Pattern for regular expression like this; dd.dd.dddd dd:dd:dd: d.d,
		// alarm from alarmcentralen has this pattern
		Pattern p = Pattern.compile("(\\d{2}).(\\d{2}).(\\d{4})(\\s)(\\d{2}):(\\d{2}):(\\d{2})(\\s)(\\d{1}).(\\d{1})");
		Matcher m = p.matcher(this.msgBody);

		// Due to previous abort we have to store the SMS manually in phones
		// inbox
		ContentValues values = new ContentValues();
		values.put("address", this.msgHeader);
		values.put("body", this.msgBody);
		context.getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
		this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":smsHandler()", "SMS stored in devices inbox");

		// Play message tone and vibrate, different method calls depending on
		// alarm type
		if (this.type.equals("primary")) {
			this.noiseHandler.makeNoise(context, this.primaryMessageToneId, useOsSoundSettings, this.playToneTwice);
		} else if (this.type.equals("secondary")) {
			this.noiseHandler.makeNoise(context, this.secondaryMessageToneId, useOsSoundSettings, this.playToneTwice);
		} else {
			// UNSUPPORTED LARM TYPE OCCURRED
			this.logger.logCatTxt(this.logger.getERROR(), this.LOG_TAG + ":smsHandler()", "An unsupported alarm type has occurred, can't play tone or vibrate(trig SmsAlarm.playMsgToneVibrate(Context, int, boolean))");
		}

		// If message contain a string with correct pattern, remove the date and
		// time stamp in message
		if (m.find()) {
			this.msgBody = this.msgBody.replace(m.group(1).toString() + "." + m.group(2).toString() + "." + m.group(3).toString() + m.group(4).toString() + m.group(5).toString() + ":" + m.group(6).toString() + ":" + m.group(7).toString() + m.group(8).toString() + m.group(9).toString() + "."
					+ m.group(10).toString(), "");

			this.logger.logCatTxt(this.logger.getWARN(), this.LOG_TAG + ":smsHandler()", "SMS cleaned from unnecessary information");
		}

		// If Alarm acknowledge is enabled and alarm type equals primary, store
		// full alarm message
		if (this.enableAlarmAck == true && this.type.equals("primary")) {
			// Log message
			this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":smsHandler()", "Alarm acknowledgement is enabled and alarm is of type primary, store full SMS to shared preferences");

			// Enable acknowledge is enabled and alarm is of type primary
			this.prefHandler.setPrefs(this.prefHandler.getSHARED_PREF(), this.prefHandler.getFULL_MESSAGE_KEY(), this.msgBody, context);
		}
		// Store message's body in shared prefs so it can be shown in
		// notification
		this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":smsHandler()", "Store SMS to shared preferences for show in notification bar");
		this.prefHandler.setPrefs(this.prefHandler.getSHARED_PREF(), this.prefHandler.getMESSAGE_KEY(), this.msgBody, context);

		// Acknowledge is enabled and it is a primary alarm, show acknowledge
		// notification, else show "ordinary" notification
		if (this.enableAlarmAck == true && this.type.equals("primary")) {
			this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":smsHandler()", "Preparing intent for the AcknowledgeNotificationHelper.class");
			// Start intent, AcknowledgeNotificationHelper - a helper to show
			// acknowledge notification
			Intent ackNotIntent = new Intent(context, AcknowledgeNotificationHelper.class);
			context.startService(ackNotIntent);
		} else {
			this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":smsHandler()", "Preparing intent for the NotificationHelper.class");

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
	 */
	@SuppressWarnings("unchecked")
	private void getSmsReceivePrefs(Context context) {
		// Some logging
		this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":getSmsReceivePrefs()", "Start retrieving shared preferences needed by class SmsReceiver");

		// Get shared preferences needed by SmsReceiver
		this.primaryListenNumber = (String) this.prefHandler.getPrefs(this.prefHandler.getSHARED_PREF(), this.prefHandler.getPRIMARY_LISTEN_NUMBER_KEY(), this.STRING, context);
		this.secondaryListenNumbers = (List<String>) this.prefHandler.getPrefs(this.prefHandler.getSHARED_PREF(), this.prefHandler.getSECONDARY_LISTEN_NUMBERS_KEY(), this.LIST, context);
		this.primaryMessageToneId = (Integer) this.prefHandler.getPrefs(this.prefHandler.getSHARED_PREF(), this.prefHandler.getPRIMARY_MESSAGE_TONE_KEY(), this.INTEGER, context);
		this.secondaryMessageToneId = (Integer) this.prefHandler.getPrefs(this.prefHandler.getSHARED_PREF(), this.prefHandler.getSECONDARY_MESSAGE_TONE_KEY(), this.INTEGER, context);
		this.useOsSoundSettings = (Boolean) this.prefHandler.getPrefs(this.prefHandler.getSHARED_PREF(), this.prefHandler.getUSE_OS_SOUND_SETTINGS_KEY(), this.BOOLEAN, context);
		this.enableAlarmAck = (Boolean) this.prefHandler.getPrefs(this.prefHandler.getSHARED_PREF(), this.prefHandler.getENABLE_ACK_KEY(), this.BOOLEAN, context);
		this.playToneTwice = (Boolean) this.prefHandler.getPrefs(this.prefHandler.getSHARED_PREF(), this.prefHandler.getPLAY_TONE_TWICE_KEY(), this.BOOLEAN, context);
		this.enableSmsAlarm = (Boolean) this.prefHandler.getPrefs(this.prefHandler.getSHARED_PREF(), this.prefHandler.getENABLE_SMS_ALARM_KEY(), this.BOOLEAN, context);

		this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":getSmsReceivePrefs()", "Shared preferences retrieved");
	}
}