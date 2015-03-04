/**
 * Copyright (c) 2015 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.activity.SmsAlarm;
import ax.ha.it.smsalarm.alarm.Alarm.AlarmType;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.DataType;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.PrefKey;
import ax.ha.it.smsalarm.util.Util;

/**
 * Class responsible for all sound handling, this means responsible for all interactions with the {@link MediaPlayer} in this application.<br>
 * <b><i> {@link SoundHandler} is a singleton, eagerly initialized to avoid concurrent modification.</i></b>
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class SoundHandler {
	// Singleton instance of this class, eagerly initialized
	private static final SoundHandler INSTANCE = new SoundHandler();

	private static final String LOG_TAG = SoundHandler.class.getSimpleName();

	// Constant defining path to alarm signals within assets
	private static final String PATH_TO_ALARM_SIGNAL_ASSETS = "alarm-signals/";
	private static final String MP3_EXTENSION = ".mp3";

	// Constants used as keys when storing respective volumes into a HashMap
	private static final String ORIGINAL_MEDIA_VOLUME = "originalMediaVolume";
	private static final String CALCULATED_MEDIA_VOLUME = "calculatedMediaVolume";

	// Default Id's of the different alarm type ID's
	public static final int DEFAULT_PRIMARY_ALARM_SIGNAL_ID = 0; // This Id is used as fall back in case user added alarm signal isn't found
	public static final int DEFAULT_SECONDARY_ALARM_SIGNAL_ID = 1;

	// A limit time for how long we can wait for the KitKat handler to be idle, this works as a
	// security to be sure that noise always going to be made
	private static final long NOISE_DELAY_LIMIT = 10000;

	// Need to access some shared preferences
	private SharedPreferencesHandler prefHandler = SharedPreferencesHandler.getInstance();

	// Must have objects of media player and audio manager available throughout the class
	private MediaPlayer mediaPlayer;
	private AudioManager audioManager;

	// Convenience map to store the different volumes
	private HashMap<String, Integer> volumes = new HashMap<String, Integer>();

	/**
	 * Creates a new instance of {@link SoundHandler}.
	 */
	private SoundHandler() {
		if (INSTANCE != null) {
			if (SmsAlarm.DEBUG) {
				Log.e(LOG_TAG + ":SoundHandler()", "SoundHandler already instantiated");
			}
		}
	}

	/**
	 * To get the <b><i>singleton</i></b> instance of {@link SoundHandler}.
	 * 
	 * @return Instance of <code>SoundHandler</code>.
	 */
	public static SoundHandler getInstance() {
		return INSTANCE;
	}

	/**
	 * To play given <code>alarmSignal</code> with this {@link SoundHandler}'s instance of {@link MediaPlayer}.
	 * 
	 * @param context
	 *            The Context in which {@link SoundHandler} runs.
	 * @param alarmSignal
	 *            The alarm signal to be played.
	 */
	public void previewSignal(Context context, String alarmSignal) {
		// AudioManager used to get and set different volume levels
		audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

		switch (audioManager.getRingerMode()) {
			case (AudioManager.RINGER_MODE_NORMAL):
				// Stop MediaPlayer if it's already playing
				stopMediaPlayer(context);

				// Calculate the different volumes
				volumes = calculateVolume(audioManager);

				// Ensure we got a fresh MediaPlayer object
				mediaPlayer = new MediaPlayer();

				// Listen to completion, in other words when media player has finished and reset media volume and media player
				mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer mp) {
						restoreMediaVolume(audioManager);
						mediaPlayer.reset();
						mediaPlayer.release();
						mediaPlayer = null;
					}
				});

				// Prepare the media player
				prepareMediPlayer(context, alarmSignal);

				// Set correct media volume
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumes.get(CALCULATED_MEDIA_VOLUME), 0);

				// At last, play the signal
				mediaPlayer.start();

				break;
			default:
				// All other ringer modes than RINGER_MODE_NORMAL ends up here as it's not wanted to make any kind of sound when the device is in any
				// other mode
				if (SmsAlarm.DEBUG) {
					Log.e(LOG_TAG + ":previewSignal()", "Device is not in RINGER_MODE_NORMAL, hence it's not wanted to make any kind of sound");
				}
		}
	}

	/**
	 * To play the actual <b><i>Alarm</i></b>. This method is similar to {@link #previewSignal(Context, String)} but the main difference is that it
	 * resolves what <b><i>Alarm Signal</i></b> that should be played depending on given {@link AlarmType}. <br>
	 * Besides this difference this method does also handle the <b><i>KitKat</i></b> retarded behavior.
	 * 
	 * @param context
	 *            The Context in which {@link SoundHandler} runs.
	 * @param alarmType
	 *            Type of alarm from which correct alarm signal is resolved.
	 * @see #previewSignal(Context, String)
	 * @see {@link KitKatHandler}
	 */
	public void alarm(final Context context, AlarmType alarmType) {
		// Only do further handling if given AlarmType is supported
		if (AlarmType.PRIMARY.equals(alarmType) || AlarmType.SECONDARY.equals(alarmType)) {
			// Need to know when called this method
			long startTimeMillis = System.currentTimeMillis();

			// Need to wait until KitKat handler is in idle mode
			while (!KitKatHandler.getInstance().isIdle() && (System.currentTimeMillis() < (startTimeMillis + NOISE_DELAY_LIMIT))) {
				if (SmsAlarm.DEBUG) {
					Log.d(LOG_TAG + ":alarm()", "KitKatHandler running, waiting....");
				}
			}

			// AudioManager used to get and set different volume levels
			audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			// If SmsAlarm is setup to follow the devices sound settings or not
			boolean useOsSoundSettings = (Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.USE_OS_SOUND_SETTINGS_KEY, DataType.BOOLEAN, context);

			// Only if device is in normal ringer mode or if SmsAlarm is setup to not follow the devices sound settings
			if (AudioManager.RINGER_MODE_NORMAL == audioManager.getRingerMode() || !useOsSoundSettings) {
				// Alarm signal to be played
				String alarmSignal;

				// Resolve correct alarm signal id
				if (AlarmType.PRIMARY.equals(alarmType)) {
					alarmSignal = (String) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_ALARM_SIGNAL_KEY, DataType.STRING, context, resolveAlarmSignal(context, DEFAULT_PRIMARY_ALARM_SIGNAL_ID));
				} else {
					alarmSignal = (String) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_ALARM_SIGNAL_KEY, DataType.STRING, context, resolveAlarmSignal(context, DEFAULT_SECONDARY_ALARM_SIGNAL_ID));
				}

				// In case media player is already running stop it, could be that the user is previewing some signals
				stopMediaPlayer(context);

				// Calculate the different volumes
				volumes = calculateVolume(audioManager);

				// Ensure we got a fresh MediaPlayer object
				mediaPlayer = new MediaPlayer();

				// Set DataSource and prepare MediaPlayer
				prepareMediPlayer(context, alarmSignal);

				// If application use systems sound settings, check if phone is in normal, silent or vibration mode else don't check phones status and
				// play tone and vibrate even if phone is in silent or vibrate mode
				if (useOsSoundSettings) {
					// Set calculated media volume
					audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumes.get(CALCULATED_MEDIA_VOLUME), 0);
					mediaPlayer.start();
				} else { // If not take OS sound setting into account, always ring at highest volume and vibrate
					// Set maximum volume to audio manager
					audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
					mediaPlayer.start();
				}

				mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					// Variable indicating how many times the alarm signal should be played
					int toBePlayed = (Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.PLAY_ALARM_SIGNAL_TWICE_KEY, DataType.BOOLEAN, context) ? 2 : 1;
					// Counter variable to count number of times played, we have already played the alarm signal once
					int timesPlayed = 1;

					// Figure out if the alarm signal should be played repeatedly
					boolean playAlarmSignalRepeatedly = (Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.PLAY_ALARM_SIGNAL_REPEATEDLY_KEY, DataType.BOOLEAN, context);

					@Override
					public void onCompletion(MediaPlayer mp) {
						// If alarm signal havn't been played enough times or if it's wanted to play alarm signal repeatedly, else release media
						// player
						if (timesPlayed < toBePlayed || playAlarmSignalRepeatedly) {
							// Add to counter
							timesPlayed++;
							// Seek to beginning of message tone
							mediaPlayer.seekTo(0);
							// Start play message tone
							mediaPlayer.start();
						} else {
							restoreMediaVolume(audioManager);
							mediaPlayer.reset();
							mediaPlayer.release();
							mediaPlayer = null;
						}
					}
				});
			}
		} else {
			// This is weird, log this case
			if (SmsAlarm.DEBUG) {
				Log.e(LOG_TAG + ":alarm()", "Method called with the unsupported AlarmType: \"" + alarmType.toString() + "\", check why. However application can't decide how to handle this case");
			}
		}
	}

	/**
	 * To lookup correct <b><i>Alarm Signal Names</i></b> from given alarm signal id.
	 * 
	 * @param context
	 *            The Context in which {@link SoundHandler} runs.
	 * @param alarmSignalId
	 *            Id of alarm signal.
	 * @return Resolved alarm signal name.
	 */
	public String resolveAlarmSignal(Context context, int alarmSignalId) {
		String[] alarmSignals = context.getResources().getStringArray(R.array.alarm_signals);
		return alarmSignals[alarmSignalId];
	}

	/**
	 * To calculate the <b><i>Media Volume</i></b> to be used when this {@link SoundHandler} plays an alarm signal. This volume is calculated from the
	 * current ring volume, this is to get the media volume to follow the devices sound settings.<br>
	 * the original media volume is also stored and returned.
	 * 
	 * @param audioManager
	 *            AudioManager from where to fetch the different volumes.
	 * @return A {@link HashMap} containing the original and calculated media volume. The values are keyed on {@link #ORIGINAL_MEDIA_VOLUME} and
	 *         {@link #CALCULATED_MEDIA_VOLUME}.
	 */
	private HashMap<String, Integer> calculateVolume(AudioManager audioManager) {
		// Get and store the original media volume
		int originalMediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

		// Calculate current ring volume in percent
		float currentRingVolume = ((float) audioManager.getStreamVolume(AudioManager.STREAM_RING) / (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_RING));

		// Calculate and store the correct sound volume to use, according to the current ring volume
		int calculatedMediaVolume = (int) (currentRingVolume * audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));

		// Store the different volumes on their respective keys
		HashMap<String, Integer> mediaVolumesMap = new HashMap<String, Integer>();
		mediaVolumesMap.put(ORIGINAL_MEDIA_VOLUME, originalMediaVolume);
		mediaVolumesMap.put(CALCULATED_MEDIA_VOLUME, calculatedMediaVolume);

		return mediaVolumesMap;
	}

	/**
	 * To restore the devices <b><i>Media Volume</i></b> to the original media volume, what it was before any alarm signal was previewed or alarmed.
	 * <p>
	 * <b><i>Note. Safe to invoke any time as it checks that a original Media Volume actually has been set. If it hasn't, nothing happens.</i></b>
	 * 
	 * @param audioManager
	 *            {@link AudioManager} to set volumes with.
	 * @see #ORIGINAL_MEDIA_VOLUME
	 */
	private void restoreMediaVolume(AudioManager audioManager) {
		if (volumes != null && volumes.containsKey(ORIGINAL_MEDIA_VOLUME)) {
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumes.get(ORIGINAL_MEDIA_VOLUME), 0);
		}
	}

	/**
	 * To prepare this {@link SoundHandler}'s instance of {@link MediaPlayer} by setting a correct <b><i>Data Source</i></b> to it and by calling
	 * {@link MediaPlayer#prepare()} on it.<br>
	 * The data source will be set with given {@link AssetFileDescriptor}'s data.
	 * 
	 * @param afd
	 *            The asset file descriptor to prepare <code>MediaPlayer</code> with.
	 * @see #prepareMediaPlayer(String)
	 * @see #prepareMediPlayer(Context, String)
	 */
	private void prepareMediaPlayer(AssetFileDescriptor afd) {
		// Set data source for media player and prepare it
		try {
			if (mediaPlayer != null && afd != null) {
				mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
				mediaPlayer.prepare();
			}
		} catch (IllegalStateException e) {
			if (SmsAlarm.DEBUG) {
				Log.e(LOG_TAG + ":prepareMediaPlayer()", "Mediaplayer is in an illegal state for setting datasource or preparing it", e);
			}
		} catch (IOException e) {
			if (SmsAlarm.DEBUG) {
				Log.e(LOG_TAG + ":prepareMediaPlayer()", "An error occurred while setting datasource to mediaplayer or preparing it", e);
			}
		}
	}

	/**
	 * To prepare this {@link SoundHandler}'s instance of {@link MediaPlayer} by setting a correct <b><i>Data Source</i></b> to it and by calling
	 * {@link MediaPlayer#prepare()} on it.<br>
	 * The data source will be set with given <code>alarmSignal</code>.
	 * 
	 * @param alarmSignal
	 *            The alarm signal to prepare <code>MediaPlayer</code> with.
	 * @see #prepareMediaPlayer(AssetFileDescriptor)
	 * @see #prepareMediPlayer(Context, String)
	 */
	private void prepareMediaPlayer(String alarmSignal) {
		// Set data source for media player and prepare it
		try {
			if (mediaPlayer != null && alarmSignal != null) {
				mediaPlayer.setDataSource(alarmSignal);
				mediaPlayer.prepare();
			}
		} catch (IllegalStateException e) {
			if (SmsAlarm.DEBUG) {
				Log.e(LOG_TAG + ":prepareMediaPlayer()", "Mediaplayer is in an illegal state for setting datasource or preparing it", e);
			}
		} catch (IOException e) {
			if (SmsAlarm.DEBUG) {
				Log.e(LOG_TAG + ":prepareMediaPlayer()", "An error occurred while setting datasource to mediaplayer or preparing it", e);
			}
		}
	}

	/**
	 * To prepare this {@link SoundHandler}'s instance of {@link MediaPlayer} by setting a correct <b><i>Data Source</i></b> to it and by calling
	 * {@link MediaPlayer#prepare()} on it.<br>
	 * This method figures out if given <code>alarmSignal</code> is <b><i>User Added</i></b> or <b><i>Application Provided</i></b>, and prepares the
	 * <code>MediaPlayer</code> correctly corresponding to that info.<br>
	 * If the given <code>alarmSignal</code> wasn't found on the device then a default application provided alarm signal is used.
	 * 
	 * @param context
	 *            The Context in which {@link SoundHandler} runs.
	 * @param alarmSignal
	 *            The alarm signal to prepare <code>MediaPlayer</code> with.
	 * @see #prepareMediaPlayer(AssetFileDescriptor)
	 * @see #prepareMediaPlayer(String)
	 */
	@SuppressWarnings("unchecked")
	private void prepareMediPlayer(Context context, String alarmSignal) {
		// Fetch all user added alarm signal paths
		List<String> userAddedAlarmSignals = (List<String>) SharedPreferencesHandler.getInstance().fetchPrefs(PrefKey.SHARED_PREF, PrefKey.USER_ADDED_ALARM_SIGNALS_KEY, DataType.LIST, context);

		boolean prepareWithAfd = true;

		// If file exists then it's a user added alarm signal that's going to be played, hence we don't need to set up any assets
		// file descriptor
		if (Util.existsInConsiderCases(alarmSignal, userAddedAlarmSignals)) {
			// Check that the file actually exists
			if (Util.fileExists(alarmSignal)) {
				prepareWithAfd = false;
			} else {
				// As the file wasn't found on the system set the alarm signal to the default that we know exists
				alarmSignal = resolveAlarmSignal(context, DEFAULT_PRIMARY_ALARM_SIGNAL_ID);
			}
		}

		// The given alarm signal was either not found or the alarm signal simply is an application defined one, hence it's need to be loaded from the
		// application assets
		if (prepareWithAfd) {
			// AssetFileDescriptor to get needed assets
			AssetFileDescriptor afd = null;

			// Open file descriptor with correct path
			try {
				afd = context.getAssets().openFd(PATH_TO_ALARM_SIGNAL_ASSETS + alarmSignal + MP3_EXTENSION);
			} catch (IOException e) {
				if (SmsAlarm.DEBUG) {
					Log.e(LOG_TAG + ":previewSignal()", "An error occurred while opening the FileDescriptor", e);
				}
			}

			// Set DataSource and prepare MediaPlayer
			prepareMediaPlayer(afd);
		} else {
			// User defined alarm signal just set prepare media player with that one
			prepareMediaPlayer(alarmSignal);
		}
	}

	/**
	 * To stop this {@link SoundHandler}'s instance of {@link MediaPlayer} if it's not <code>null</code> and if it's <b><i>playing</i></b>.<br>
	 * The <code>MediaPlayer</code> will be fully released by invoking following methods:
	 * <ul>
	 * <li>{@link MediaPlayer#stop()}</li>
	 * <li>{@link MediaPlayer#reset()}</li>
	 * <li>{@link MediaPlayer#release()}</li>
	 * </ul>
	 * <p>
	 * At last the <code>MediaPlayer</code> object will be <code>nullified</code>.
	 * <p>
	 * This method does also restore the original <b><i>Media Volume</i></b>.
	 * 
	 * @param context
	 *            The Context in which {@link SoundHandler} runs.
	 * @see #restoreMediaVolume(AudioManager)
	 */
	public void stopMediaPlayer(Context context) {
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			// Stop and release all MediaPlayer resources
			mediaPlayer.stop();
			mediaPlayer.reset();
			mediaPlayer.release();
			mediaPlayer = null;

			// Get AudioManager in order to restore media volume
			audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			// Restore the media volume
			restoreMediaVolume(audioManager);
		}
	}

	/**
	 * Convenience method to figure out what <b><i>Ringer Mode</i></b> the device is in.
	 * 
	 * @param context
	 *            Context from which system service {@link Context#AUDIO_SERVICE} are taken.
	 * @return Devices current ringer mode.
	 */
	public static int getRingerMode(Context context) {
		return ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE)).getRingerMode();
	}
}
