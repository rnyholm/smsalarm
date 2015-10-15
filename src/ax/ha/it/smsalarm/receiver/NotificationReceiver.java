/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.receiver;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import ax.ha.it.smsalarm.activity.Acknowledge;
import ax.ha.it.smsalarm.alarm.Alarm;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.PrefKey;
import ax.ha.it.smsalarm.handler.SoundHandler;
import ax.ha.it.smsalarm.handler.VibrationHandler;
import ax.ha.it.smsalarm.service.FlashNotificationService;

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
	// The valid actions for intents entering this receiver
	public static final String ACTION_OPEN_INBOX = "ax.ha.it.smsalarm.OPEN_INBOX";
	public static final String ACTION_ACKNOWLEDGE = "ax.ha.it.smsalarm.ACKNOWLEDGE";

	// Components package and class for launching the MMS inbox properly
	private static final String MMS_INBOX_PACKAGE = "com.android.mms";
	private static final String MMS_INBOX_CLASS = "com.android.mms.ui.ConversationList";

	@Override
	public void onReceive(Context context, Intent intent) {
		// Stop flash notification
		FlashNotificationService.stopFlashNotificationService(context);

		// Stop alarm signal from being played, if it's played..
		SoundHandler.getInstance().stopMediaPlayer(context);
		// ...also cancel the vibration if it vibrates
		VibrationHandler.getInstance().cancelVibrator();

		// Get action from intent, if it holds any, and figure out correct action depending on it
		String intentAction = intent.getAction();

		// Open SMS directory
		if (ACTION_OPEN_INBOX.equals(intentAction)) {
			// Setup next intent
			Intent inboxIntent = new Intent(Intent.ACTION_MAIN);
			inboxIntent.setComponent(new ComponentName(MMS_INBOX_PACKAGE, MMS_INBOX_CLASS));
			inboxIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Must set this flag in order to start activity from outside an activity

			// Use application context to start activity
			context.getApplicationContext().startActivity(inboxIntent);
		} else if (ACTION_ACKNOWLEDGE.equals(intentAction)) { // Start acknowledge activity
			// Reset Shared Preference HAS_CALLED, to ensure that activity acknowledge not will place a acknowledge call in onResume() first time the
			// user interface is loaded. This is done here because this is only relevant if application is set to acknowledge
			SharedPreferencesHandler.getInstance().storePrefs(PrefKey.SHARED_PREF, PrefKey.HAS_CALLED_KEY, false, context);

			// Get the alarm which absolutely should exist
			Alarm alarm = (Alarm) intent.getParcelableExtra(Alarm.TAG);

			// Build up the new intent and pass over the alarm to acknowledge activity
			Intent acknowledgeIntent = new Intent(context, Acknowledge.class);
			acknowledgeIntent.putExtra(Alarm.TAG, alarm);
			acknowledgeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.getApplicationContext().startActivity(acknowledgeIntent);
		}
	}
}
