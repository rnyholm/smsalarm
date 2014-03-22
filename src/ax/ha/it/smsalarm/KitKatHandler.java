package ax.ha.it.smsalarm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.media.AudioManager;
import ax.ha.it.smsalarm.LogHandler.LogPriorities;

/**
 * This class is responsible for any special handling that needs to be done according to
 * <b><i>KitKat</i></b>'s (and higher) retarded behavior when receiving an sms.<br>
 * <b><i>LogHandler is a singleton.</i></b>
 * <p>
 * 
 * <b><i>Note!<br>
 * This class and it's functionality is still in BETA state.</i></b>
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.2.1
 * @since 2.2.1
 */
public class KitKatHandler {

	/**
	 * Valid notifications bar actions.
	 * 
	 * @author Robert Nyholm <robert.nyholm@aland.net>
	 * @version 2.2.1
	 * @since 2.2.1
	 */
	private enum NotificationsBarActions {
		EXPAND, COLLAPSE;
	}

	// Singleton instance of this class
	private static KitKatHandler INSTANCE;

	// Log tag
	private final String LOG_TAG = getClass().getSimpleName();

	// Wished delay for a ringer mode switch and a notifications bar expand/collapse
	private final static int RINGER_MODE_DELAY = 7000;
	private final static int NOTIFICATIONS_BAR_EXPAND_COLLAPSE_DELAY = 1000;

	// Variable used to log messages
	private final LogHandler logger = LogHandler.getInstance();

	// Ringer mode handling
	private AudioManager am;

	// Context is needed
	private Context context;

	// Devices original ringer mode are stored in this variable during ringer mode change
	private int RINGER_MODE = -1;

	// Indicates whether a ringer mode switch is in progress or not
	private boolean ringerModeSwitchInProgress = false;

	/**
	 * Construct a new <code>KitKatHandler</code>. Private constructor, is private due to it's
	 * singleton pattern.
	 * 
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 */
	private KitKatHandler() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":KitKatHandler()", "New instance of KitKatHandler created");
	}

	/**
	 * Method to get the singleton instance of this class.
	 * 
	 * @return Singleton instance of LogHandler
	 */
	public static KitKatHandler getInstance() {
		// If instance of this object is null create a new one
		if (INSTANCE == null) {
			INSTANCE = new KitKatHandler();
		}

		return INSTANCE;
	}

	/**
	 * To handle the retarded sms behavior of <b><i>Android API lvl's KitKat and higher</i></b>.
	 * What happens is that the device is put to <code>AudioManager.RINGER_MODE_SILENT</code> for a
	 * certain amount of time. This is to make it look like only <b><i>SmsAlarm</i></b> received the
	 * sms. <br>
	 * The notifications bar is expanded and after a certain amount of time collapsed, this is to
	 * pop away the default sms applications LED notification from the queue so out LED notification
	 * goes through.
	 * 
	 * @param context
	 *            Context from which to get system services.
	 * 
	 * @see #setSilent()
	 * @see #expandCollapseNotificationsBarWithDelay()
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 */
	public void handleKitKat(Context context) {
		// Set context, we need it later, also get audiomanager from context
		this.context = context;
		am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":handleKitKat()", "Special handling for KitKat and higher is about to be done");

		setSilentModeWithDelay();
		expandCollapseNotificationsBarWithDelay();
	}

	/**
	 * To expand the notifications bar.
	 * 
	 * @see #handleNotificationsBar(NotificationsBarActions)
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 */
	private void expandNotificationsBar() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":expandNotificationsBar()", "Notifications bar is about to be expanded");
		handleNotificationsBar(NotificationsBarActions.EXPAND);
	}

	/**
	 * To collapse the notifications bar.
	 * 
	 * @see #handleNotificationsBar(NotificationsBarActions)
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 */
	private void collapseNotificationsBar() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":expandNotificationsBar()", "Notifications bar is about to be collapsed");
		handleNotificationsBar(NotificationsBarActions.COLLAPSE);
	}

	/**
	 * To handle the notifications bar according to given <code>NotificationsBarActions</code>.
	 * Following actions are supported:
	 * <ul>
	 * <li><code>NotificationsBarActions.EXPAND</code></li>
	 * <li><code>NotificationsBarActions.COLLAPSE</code></li>
	 * </ul>
	 * 
	 * @param notificationsBarAction
	 * 
	 * @see #isInitialized()
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 * @see LogHandler#logCatTxt(LogPriorities, String, String)
	 */
	private void handleNotificationsBar(NotificationsBarActions notificationsBarAction) {
		// Check if we have a real context to get system service from
		if (isInitialized()) {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":handleNotificationsBar()", "Notifications bar is about to be handled according to given NotificationsBarAction:\"" + notificationsBarAction.toString() + "\"");
			// Get the status bar service from context
			Object statusBarService = context.getSystemService("statusbar");

			try {
				// Get the status bar manager
				Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");

				// Switch through the different notifications bar actions
				switch (notificationsBarAction) {
					case EXPAND:
						// Get method for expanding notifications bar and invoke it
						Method expandNotificationsPanel = statusbarManager.getMethod("expandNotificationsPanel");
						expandNotificationsPanel.invoke(statusBarService);

						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":handleNotificationsBar()", "Notifications bar was expanded");
						break;
					case COLLAPSE:
						Method collapsePanels = statusbarManager.getMethod("collapsePanels");
						collapsePanels.invoke(statusBarService);

						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":handleNotificationsBar()", "Notifications bar was collapsed");
						break;
					default:
						throw new IllegalArgumentException("An unsupported NotificationBarActions was given as argument, given argument:\"" + notificationsBarAction.toString() + "\"");
				}
			} catch (ClassNotFoundException e) {
				logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":handleNotificationsBar()", "An exception occurred while getting class for name:\"android.app.StatusBarManager\"", e);
			} catch (NoSuchMethodException e) {
				logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":handleNotificationsBar()", "An exception occurred while getting method", e);
			} catch (IllegalAccessException e) {
				logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":handleNotificationsBar()", "An exception occurred while accessing method", e);
			} catch (InvocationTargetException e) {
				logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":handleNotificationsBar()", "An exception occurred while invocing method", e);
			} catch (IllegalArgumentException e) {
				logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":handleNotificationsBar()", "An exception occurred while choosing correct method according to NotificationsBarAction", e);
			}
		}
	}

	/**
	 * To expand the notifications bar for a certain amount of time. After elapsed time the
	 * notifications bar is collapsed.
	 * 
	 * @see #expandNotificationsBar()
	 * @see #collapseNotificationsBar()
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 */
	private void expandCollapseNotificationsBarWithDelay() {
		// If we successfully switched ringer mode we start the delay
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":expandCollapseNotificationsBarWithDelay()", "Notifications bar is about to be expanded for a time of:\"" + NOTIFICATIONS_BAR_EXPAND_COLLAPSE_DELAY + "\"ms");

		// Expand the notifications bar
		expandNotificationsBar();

		Timer myTimer = new Timer();
		myTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				// After elapsed time the notifications bar is collapsed
				collapseNotificationsBar();
			}
		}, NOTIFICATIONS_BAR_EXPAND_COLLAPSE_DELAY);
	}

	/**
	 * To change device's <code>RINGER_MODE</code> to <code>AudioManager.RINGER_MODE_SILENT</code>
	 * for a certain amount of time, after elapsed time the ringer mode is restored.
	 * 
	 * @see #setSilent()
	 * @see #restoreRingerMode()
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 */
	private void setSilentModeWithDelay() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setSilentModeWithDelay()", "Ringer mode is about to be changed for a time of:\"" + RINGER_MODE_DELAY + "\"ms");

		// If we successfully switched ringer mode we start the delay
		if (!ringerModeSwitchInProgress && setSilent()) {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setSilentModeWithDelay()", "Ringer mode successfully changed, starting delay");
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
	 * To set devices ringer mode into <code>AudioManager.RINGER_MODE_SILENT</code>.
	 * 
	 * @return <code>true</code> if ringer mode change was successful else <code>false</code>.
	 * 
	 * @see #isInitialized()
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 * @see LogHandler#logCatTxt(LogPriorities, String, String)
	 */
	private boolean setSilent() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setSilent()", "Device is about to be set into RINGER_MODE_SILENT");

		// Only if object is initialized, otherwise we get a NPE
		if (isInitialized()) {
			// Store devices original ringer mode, in case we want to restore it later
			RINGER_MODE = am.getRingerMode();
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setSilent()", "Original ringer mode is:\"" + Integer.toString(RINGER_MODE) + "\" and has been stored for future usage");

			// Set ringer mode to silent
			am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setSilent()", "Device has successfully been set into RINGER_MODE_SILENT");

			// Success return true
			return true;
		}

		// For some reason the ringer mode change didn't go through, most likely the
		// audiomanager is null
		logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":setSilent()", "Failed to put device into RINGER_MODE_SILENT");
		return false;
	}

	/**
	 * To get this objects ringer mode delay in ms.
	 * 
	 * @return This objects <code>RINGER_MODE_DELAY</code>.
	 */
	public int getRingerModeDelay() {
		return RINGER_MODE_DELAY;
	}

	/**
	 * To restore the devices ringer as it was before the device was set to
	 * <code>AudioManager.RINGER_MODE_SILENT</code>. Of course this is only done if it's needed.
	 * 
	 * @see #isInitialized()
	 * @see #isValidRingerMode()
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 */
	private void restoreRingerMode() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":restoreRingerMode()", "Original ringer mode is about to restored");

		// Only needed if this object is initialized and it holds a valid ringer mode
		if (isInitialized() && isValidRingerMode()) {
			am.setRingerMode(RINGER_MODE);
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":restoreRingerMode()", "Ringer mode:\"" + RINGER_MODE + "\" has been set to device");

			// Set default values
			RINGER_MODE = -1;
			ringerModeSwitchInProgress = false;

			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":restoreRingerMode()", "Ringer mode has been restored");
		}
	}

	/**
	 * To figure out if this object is idle or not. By not being idle means that a ringer mode
	 * switch <b><i>is in progress</i></b>.
	 * 
	 * @return <code>true</code> if object is idle else <code>false</code>.
	 */
	public boolean isIdle() {
		return !ringerModeSwitchInProgress;
	}

	/**
	 * Helper method to figure out if this object has been correctly initialized. This object is
	 * seen as initialized if it's <code>Context</code> and <code>AudioManager</code> isn't
	 * <code>null</code>.
	 * 
	 * @return <code>true</code> if correctly initialized else <code>false</code>.
	 * 
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 * @see LogHandler#logCatTxt(LogPriorities, String, String)
	 */
	private boolean isInitialized() {
		if (context != null && am != null) {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":isInitialized()", "Context and AudioManager has been correctly initialized, returning true");
			return true;
		}

		logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":isInitialized()", "Context and AudioManager has not been correctly initialized, returning false");
		return false;
	}

	/**
	 * Helper method to figure out if this objects <b><i>RINGER_MODE</i></b> is valid. In other
	 * words some of following:
	 * <p>
	 * <ul>
	 * <li>AudioManager.RINGER_MODE_SILENT</li>
	 * <li>AudioManager.RINGER_MODE_VIBRATE</li>
	 * <li>AudioManager.RINGER_MODE_NORMAL</li>
	 * </ul>
	 * 
	 * @return <code>true</code> if this object holds a valid ringer mode else <code>false</code>.
	 * 
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 * @see LogHandler#logCatTxt(LogPriorities, String, String)
	 */
	private boolean isValidRingerMode() {
		if (RINGER_MODE >= AudioManager.RINGER_MODE_SILENT && RINGER_MODE <= AudioManager.RINGER_MODE_NORMAL) {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":isValidRingerMode()", "Ringer mode is valid, returning true");
			return true;
		}

		logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":isValidRingerMode()", "Ringer mode is not valid, returning false");
		return false;
	}
}
