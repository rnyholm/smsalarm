package ax.ha.it.smsalarm.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import ax.ha.it.smsalarm.activity.SmsAlarm;

/**
 * Class responsible for any special handling that needs to be done according to <b><i>KitKat</i></b>'s (and higher) retarded behavior when receiving
 * an SMS.<br>
 * <b><i>KitKatHandler is a singleton.</i></b>
 * <p>
 * <b><i>Note!<br>
 * This class and it's functionality is still in BETA state.</i></b>
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.2.1
 */
public class KitKatHandler {

	/**
	 * The different valid <b><i>Notificationbar</i></b> actions.
	 * 
	 * @author Robert Nyholm <robert.nyholm@aland.net>
	 * @version 2.3.1
	 * @since 2.2.1
	 */
	private enum NotificationBarAction {
		EXPAND, COLLAPSE;
	}

	// Singleton instance of this class
	private static KitKatHandler INSTANCE;

	private static final String LOG_TAG = KitKatHandler.class.getSimpleName();

	// Wished delay for a ringer mode switch and a notifications bar expand/collapse
	public static final int RINGER_MODE_DELAY = 7000;
	private static final int NOTIFICATIONS_BAR_EXPAND_COLLAPSE_DELAY = 1000;

	// Ringer mode handling
	private AudioManager am;

	// Context is needed
	private Context context;

	// Devices original ringer mode are stored in this variable during ringer mode change
	private int RINGER_MODE = -1;

	// Indicates whether a ringer mode switch is in progress or not
	private boolean ringerModeSwitchInProgress = false;

	/**
	 * Creates a new instance of {@link KitKatHandler}.
	 */
	private KitKatHandler() {
		// Just empty...
	}

	/**
	 * To get the <b><i>singleton</i></b> instance of {@link KitKatHandler}.
	 * 
	 * @return Instance of <code>KitKatHandler</code>.
	 */
	public static KitKatHandler getInstance() {
		// If instance of this object is null create a new one
		if (INSTANCE == null) {
			INSTANCE = new KitKatHandler();
		}

		return INSTANCE;
	}

	/**
	 * To handle the retarded SMS behavior of <b><i>Android API lvl's KitKat and higher</i></b>. What happens is that the device is put to
	 * <code>AudioManager.RINGER_MODE_SILENT</code> for a certain amount of time. This is to make it look like only <b><i>Sms Alarm</i></b> received
	 * the SMS. <br>
	 * The notifications bar is expanded and after a certain amount of time collapsed, this is to pop away the default SMS applications LED
	 * notification from the queue so out LED notification from Sms Alarm goes through.
	 * 
	 * @param context
	 *            Context from which to get system services.
	 */
	public void handleKitKat(Context context) {
		// Set context, needed later, also get AudioManager from context
		this.context = context;
		am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

		setSilentModeWithDelay();
		expandCollapseNotificationsBarWithDelay();
	}

	/**
	 * To expand the NotifactionBar.
	 */
	private void expandNotificationBar() {
		handleNotificationBar(NotificationBarAction.EXPAND);
	}

	/**
	 * To collapse the NotifactionBar.
	 */
	private void collapseNotificationBar() {
		handleNotificationBar(NotificationBarAction.COLLAPSE);
	}

	/**
	 * To handle the NotifactionBar according to given {@link NotificationBarAction}. Following actions are supported:
	 * <ul>
	 * <li><code>NotificationBarAction.EXPAND</code></li>
	 * <li><code>NotificationBarAction.COLLAPSE</code></li>
	 * </ul>
	 * 
	 * @param notificationBarAction
	 *            Wanted action of NotificationBar.
	 */
	private void handleNotificationBar(NotificationBarAction notificationBarAction) {
		// Check if we have a real context to get system service from
		if (isInitialized()) {
			// Get the status bar service from context
			Object statusBarService = context.getSystemService("statusbar");

			try {
				// Get the status bar manager
				Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");

				// Switch through the different notifications bar actions
				switch (notificationBarAction) {
					case EXPAND:
						// Get method for expanding notifications bar and invoke it
						Method expandNotificationsPanel = statusbarManager.getMethod("expandNotificationsPanel");
						expandNotificationsPanel.invoke(statusBarService);

						break;
					case COLLAPSE:
						Method collapsePanels = statusbarManager.getMethod("collapsePanels");
						collapsePanels.invoke(statusBarService);

						break;
					default:
						if (SmsAlarm.DEBUG) {
							Log.e(LOG_TAG + ":handleNotificationBar()", "An unsupported \"NotificationBarAction\" was given as argument");
						}
				}
			} catch (ClassNotFoundException e) {
				if (SmsAlarm.DEBUG) {
					Log.e(LOG_TAG + ":handleNotificationBar()", "An exception occurred while getting class for name:\"android.app.StatusBarManager\"", e);
				}
			} catch (NoSuchMethodException e) {
				if (SmsAlarm.DEBUG) {
					Log.e(LOG_TAG + ":handleNotificationBar()", "An exception occurred while getting method", e);
				}
			} catch (IllegalAccessException e) {
				if (SmsAlarm.DEBUG) {
					Log.e(LOG_TAG + ":handleNotificationBar()", "An exception occurred while accessing method", e);
				}
			} catch (InvocationTargetException e) {
				if (SmsAlarm.DEBUG) {
					Log.e(LOG_TAG + ":handleNotificationBar()", "An exception occurred while invocing method", e);
				}
			}
		}
	}

	/**
	 * To expand the NotificationBar for a certain amount of time. After elapsed time the NotificationBar is collapsed.
	 */
	private void expandCollapseNotificationsBarWithDelay() {
		// Expand the NotificationBar
		expandNotificationBar();

		Timer myTimer = new Timer();
		myTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				// After elapsed time the NotificationBar is collapsed
				collapseNotificationBar();
			}
		}, NOTIFICATIONS_BAR_EXPAND_COLLAPSE_DELAY);
	}

	/**
	 * To change device's <code>RINGER_MODE</code> to {@link AudioManager#RINGER_MODE_SILENT} for a certain amount of time, after elapsed time the
	 * ringer mode is restored.
	 */
	private void setSilentModeWithDelay() {
		// If we successfully switched ringer mode we start the delay
		if (!ringerModeSwitchInProgress && setSilent()) {
			// Object is running
			ringerModeSwitchInProgress = true;

			Timer myTimer = new Timer();
			myTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					restoreRingerMode();
				}
			}, RINGER_MODE_DELAY);
		}
	}

	/**
	 * To set devices ringer mode into {@link AudioManager#RINGER_MODE_SILENT}.
	 * 
	 * @return <code>true</code> if ringer mode change was successful else <code>false</code>.
	 */
	private boolean setSilent() {
		// Only if object is initialized, otherwise we get a NPE
		if (isInitialized()) {
			// Store devices original ringer mode, in case we want to restore it later
			RINGER_MODE = am.getRingerMode();

			// Set ringer mode to silent
			am.setRingerMode(AudioManager.RINGER_MODE_SILENT);

			// Success return true
			return true;
		}

		// For some reason the ringer mode change didn't go through, most likely the AudioManager is null
		return false;
	}

	/**
	 * To restore the devices <code>RINGER_MODE</code> as it was before the device was set to {@link AudioManager#RINGER_MODE_SILENT}. Of course this
	 * is only done if it's needed.
	 */
	private void restoreRingerMode() {
		// Only needed if this object is initialized and it holds a valid ringer mode
		if (isInitialized() && isValidRingerMode()) {
			am.setRingerMode(RINGER_MODE);

			// Set default values
			RINGER_MODE = -1;
			ringerModeSwitchInProgress = false;
		}
	}

	/**
	 * To figure out if this object is <b><i>idle</i></b> or not. By not being idle means that a ringer mode switch <b><i>is in progress</i></b>.
	 * 
	 * @return <code>true</code> if object is idle else <code>false</code>.
	 */
	public boolean isIdle() {
		return !ringerModeSwitchInProgress;
	}

	/**
	 * Helper method to figure out if this object has been correctly initialized.<br>
	 * This object is seen as initialized if it's {@link Context} and {@link AudioManager} is not <code>null</code>.
	 * 
	 * @return <code>true</code> if correctly initialized else <code>false</code>.
	 */
	private boolean isInitialized() {
		if (context != null && am != null) {
			return true;
		}

		return false;
	}

	/**
	 * Helper method to figure out if this objects <b><i>RINGER_MODE</i></b> is valid. In other words some of following:
	 * <p>
	 * <ul>
	 * <li>{@link AudioManager#RINGER_MODE_SILENT}</li>
	 * <li>{@link AudioManager#RINGER_MODE_VIBRATE}</li>
	 * <li>{@link AudioManager#RINGER_MODE_NORMAL}</li>
	 * </ul>
	 * 
	 * @return <code>true</code> if this object holds a valid ringer mode else <code>false</code>.
	 */
	private boolean isValidRingerMode() {
		if (RINGER_MODE >= AudioManager.RINGER_MODE_SILENT && RINGER_MODE <= AudioManager.RINGER_MODE_NORMAL) {
			return true;
		}

		return false;
	}
}
