/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.receiver;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import ax.ha.it.smsalarm.activity.Acknowledge;
import ax.ha.it.smsalarm.handler.FlashAlarmHandler;

/**
 * Class responsible for all actions in conjunction with all {@link Notification} interaction, more exactly Notifications dispatched from
 * <code>ax.ha.it.smsalarm</code>. Whether the user deletes the Notification or if it gets pressed the application will enter this
 * {@link BroadcastReceiver}. Then depending on the <code>Action</code> of the {@link Intent} further actions are taken.<br>
 * This class is also responsible for stopping any <b><i>Flash Alarm Notifications</i></b>.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class NotificationReceiver extends BroadcastReceiver {
	private static final String LOG_TAG = NotificationReceiver.class.getSimpleName();

	// The valid actions for intents entering this receiver
	public static final String ACTION_OPEN_INBOX = "ax.ha.it.smsalarm.OPEN_INBOX";
	public static final String ACTION_ACKNOWLEDGE = "ax.ha.it.smsalarm.ACKNOWLEDGE";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(LOG_TAG + ":onReceive()", "Received intent");

		// Stop flash alarm notification
		FlashAlarmHandler.flashAlarmStop(context);

		// Get action from intent, if it holds any, and figure out correct action depending on it
		String intentAction = intent.getAction();

		// Open SMS directory
		if (ACTION_OPEN_INBOX.equals(intentAction)) {
			Log.d(LOG_TAG + ":onReceive()", "Intent with " + ACTION_OPEN_INBOX + " received");
			// This string and intent opens the messaging directory on the Android device, however due to this page;
			// http://stackoverflow.com/questions/3708737/go-to-inbox-in-android fetched 21.10-11, this way of achieve the
			// "go to messaging directory" is highly unrecommended.Thats because this method uses undocumented API and is not part of the Android
			// core.This may or may not work on some devices and versions!
			String SMS_MIME_TYPE = "vnd.android-dir/mms-sms";

			// Setup next intent
			Intent inboxIntent = new Intent(Intent.ACTION_MAIN);
			inboxIntent.setType(SMS_MIME_TYPE);
			inboxIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Must set this flag in order to start activity from outside an activity

			// Use application context to start activity
			context.getApplicationContext().startActivity(inboxIntent);
		} else if (ACTION_ACKNOWLEDGE.equals(intentAction)) { // Start acknowledge activity
			Log.d(LOG_TAG + ":onReceive()", "Intent with " + ACTION_OPEN_INBOX + " received");
			Intent acknowledgeIntent = new Intent(context, Acknowledge.class);
			acknowledgeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.getApplicationContext().startActivity(acknowledgeIntent);
		}
	}
}
