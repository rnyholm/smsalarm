/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import ax.ha.it.smsalarm.handler.CameraHandler;

/**
 * Class responsible for switching the <b><i>Camera Flash Light</i></b> on and off for a certain amount of time, according to received intent.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 * @see CameraHandler
 */
public class FlashNotificationReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		CameraHandler.getInstance(context).toggleCameraFlash();
	}
}
