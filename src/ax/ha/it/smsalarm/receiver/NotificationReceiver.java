package ax.ha.it.smsalarm.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import ax.ha.it.smsalarm.activity.SmsAlarm;
import ax.ha.it.smsalarm.handler.CameraHandler;
import ax.ha.it.smsalarm.service.NotificationService;

public class NotificationReceiver extends BroadcastReceiver {
	private static final String LOG_TAG = NotificationReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(LOG_TAG + ":onReceive()", "Received intent");

		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent flashIntent = new Intent(context, FlashAlarmReceiver.class);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, flashIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		// Cancel alarms and release the camera - IMPORTANT
		alarmManager.cancel(pendingIntent);
		CameraHandler.getInstance(context).releaseCamera();

		int notificationAction = flashIntent.getIntExtra(NotificationService.NOTIFICATION_ACTION, 1);

		switch (notificationAction) {
			case (NotificationService.NOTIFICATION_PRESSED):
				// This string and intent opens the messaging directory on the Android device, however due to this page;
				// http://stackoverflow.com/questions/3708737/go-to-inbox-in-android fetched 21.10-11, this way of achieve the
				// "go to messaging directory" is highly unrecommended.Thats because this method uses undocumented API and is not part of the Android
				// core.This may or may not work on some devices and versions!
				String SMS_MIME_TYPE = "vnd.android-dir/mms-sms";
				Intent inboxIntent = new Intent(Intent.ACTION_MAIN);
				inboxIntent.setType(SMS_MIME_TYPE);

				context.startActivity(inboxIntent);
				break;
			case (NotificationService.NOTIFICATION_DISMISSED):

				break;
			default:
				if (SmsAlarm.DEBUG) {
					Log.d(LOG_TAG + ":onReceive()", "An unsupported intent extra was fetched. Fetched value is: \"" + notificationAction + "\"");
				}
		}
	}
}
