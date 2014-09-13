/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.handler;

import java.io.IOException;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Vibrator;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.handler.LogHandler.LogPriorities;

/**
 * This class is responsible for all sound and vibration handling. This means playing alarm signal, vibrate and so on, depending on application and
 * phone settings.<br>
 * <b><i>NoiseHandler is a singleton.</i></b>
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.0
 * @see RingerModeHandler
 */
public class NoiseHandler {
	// Singleton instance of this class
	private static NoiseHandler INSTANCE;

	// Log tag
	private final String LOG_TAG = getClass().getSimpleName();

	// A limit time for how long we can wait for the kit kat handler to be idle, this works as a
	// security to be sure that noise always going to be made
	private final long NOISE_DELAY_LIMIT = 10000;

	// Variable used to log messages
	private final LogHandler logger;

	// Initialize a MediaPlayer object
	private final MediaPlayer mPlayer = new MediaPlayer();

	// Vibrator variable
	private Vibrator vibrator;

	// Handler needed for kitkat and later releases special treatment
	private final KitKatHandler kitKatHandler;

	/**
	 * Private constructor, is private due to it's singleton pattern.
	 * 
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 */
	private NoiseHandler() {
		// Get instance of logger
		logger = LogHandler.getInstance();
		// Get instance of kitkathandler
		kitKatHandler = KitKatHandler.getInstance();

		// Log information
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":NoiseHandler()", "New instance of NoiseHandler created");
	}

	/**
	 * Method to get the singleton instance of this class.
	 * 
	 * @return Singleton instance of NoiseHandler
	 */
	public static NoiseHandler getInstance() {
		// If instance of this object is null create a new one
		if (INSTANCE == null) {
			INSTANCE = new NoiseHandler();
		}

		return INSTANCE;
	}

	/**
	 * To play alarm signal and vibrate, depending on application settings. This method also takes in account the operating systems sound settings,
	 * this depends on a input parameter.
	 * 
	 * @param context
	 *            Context
	 * @param id
	 *            ToneId as Integer
	 * @param useSoundSettings
	 *            If this method should take consideration to the device's sound settings as Boolean
	 * @param playAlarmSignalTwice
	 *            Indication whether alarm signal should be played once or twice, this is the same for vibration also
	 * @exception IllegalArgumentException
	 *                Can occur when setting data source for media player or preparing media player
	 * @exception IllegalStateException
	 *                Can occur when setting data source for media player or preparing media player
	 * @exception IOException
	 *                Can occur when setting data source for media player or preparing media player. Also when resolving alarm signal id
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 * @see LogHandler#logCatTxt(LogPriorities, String, String)
	 * @see LogHandler#logCatTxt(LogPriorities, String, String, Throwable)
	 */
	public void makeNoise(Context context, int id, boolean useSoundSettings, boolean playAlarmSignalTwice) {
		// Log information
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "Preparing to play alarm signal and vibrate");

		// Need to know when called this method
		long startTimeMillis = System.currentTimeMillis();

		// Need to wait until kitkat handler is in idle mode
		while (!kitKatHandler.isIdle() && (System.currentTimeMillis() < (startTimeMillis + NOISE_DELAY_LIMIT))) {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "KitKatHandler running, waiting....");
		}

		// Check if mediaplayer isn't playing already, in case it's playing we don't need to set up the player
		if (!mPlayer.isPlaying()) {
			// Declarations of different objects needed by makeNoise
			// AudioManager used to get and set different volume levels
			final AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			// AssetFileDescriptor to get mp3 file
			AssetFileDescriptor afd = null;
			// Set vibrator from context
			vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
			// Store original media volume
			final int originalMediaVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
			// Variable indicating how many times message tone should be played
			final int toBePlayed;
			// Calculate current ring volume in percent
			float currentRingVolume = ((float) am.getStreamVolume(AudioManager.STREAM_RING) / (float) am.getStreamMaxVolume(AudioManager.STREAM_RING));
			// To store alarm volume in, calculate it at the same time
			int alarmVolume = (int) (currentRingVolume * am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));

			// Custom vibration pattern
			long[] pattern = { 0, 5000, 500, 5000, 500, 5000, 500, 5000 };

			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "MediaPlayer initalized, Audio Service initalized, Vibrator initalized, AssetFileDescriptor initalized. Correct alarm volume has been calculated. Vibration pattern variables is set.");

			// Resolve correct tone depending on id
			try {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "Try to resolve alarm signal");
				afd = context.getAssets().openFd("tones/alarm/" + resolveAlarmSignal(context, id) + ".mp3");
			} catch (IOException e) {
				logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":makeNoise()", "An IOException occurred while resolving alarm signal from id", e);
			}

			// Set data source for mPlayer, common both for debug and ordinary mode
			try {
				mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "Data source has been set for Media Player");
			} catch (IllegalArgumentException e) {
				logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":makeNoise()", "An IllegalArgumentException occurred while setting data source for media player", e);
			} catch (IllegalStateException e) {
				logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":makeNoise()", "An IllegalStateException occurred while setting data source for media player", e);
			} catch (IOException e) {
				logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":makeNoise()", "An IOException occurred while setting data source for media player", e);
			}

			// Prepare mPlayer, also common for debug and ordinary mode
			try {
				mPlayer.prepare();
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "Media Player prepared");
			} catch (IllegalStateException e) {
				logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":makeNoise()", "An IllegalStateException occurred while preparing media player", e);
			} catch (IOException e) {
				logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":makeNoise()", "An IOException occurred while preparing media player", e);
			}

			// If false then just play message tone once else twice
			if (!playAlarmSignalTwice) {
				toBePlayed = 1;
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "Message tone will be played once, if device not in RINGER_MODE_SILENT  or application is set to not consider device's sound settings");
			} else {
				toBePlayed = 2;
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "Message tone will be played twice, if device not in RINGER_MODE_SILENT or application is set to not consider device's sound settings");
			}

			// If application use systems sound settings, check if phone is in normal, silent or vibration mode else don't check phones status and
			// play tone and vibrate even if phone is in silent or vibrate mode
			if (useSoundSettings) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "Application is set to take into account device's sound settings");

				// Decide if phone are in normal, vibrate or silent state and take action
				switch (am.getRingerMode()) {
					case AudioManager.RINGER_MODE_SILENT:
						// Do nothing except log information
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "Device is in \"RINGER_MODE_SILENT\", don't vibrate or play alarm signal");

						// Reset mediaplayer
						mPlayer.reset();

						break;
					case AudioManager.RINGER_MODE_VIBRATE:
						// Vibrate, -1 = no repeat
						vibrator.vibrate(pattern, -1);

						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "Device is in \"RINGER_MODE_VIBRATE\", just vibrate");

						break;
					case AudioManager.RINGER_MODE_NORMAL:
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "Device is in \"RINGER_MODE_NORMAL\", vibrate and play alarm signal");
						// Set correct volume to mediaplayer
						am.setStreamVolume(AudioManager.STREAM_MUSIC, alarmVolume, 0);

						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "Alarm volume has been set to: \"" + am.getStreamVolume(AudioManager.STREAM_MUSIC) + "\", MediaPlayer is about to start");

						// Vibrate, -1 = no repeat
						vibrator.vibrate(pattern, -1);

						// Start play message tone
						mPlayer.start();

						break;
					default: // Unsupported RINGER_MODE
						// DO NOTHING, except log information
						logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":makeNoise()", "Device is in a \"UNSUPPORTED\" ringer mode, can't decide what to do");
				}
			} else { // If not take into account OS sound setting, always ring at highest volume and vibrate
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "Application is set to don't take into account device's sound settings. Play alarm signal at max volume and vibrate");

				// Set maximum volume to audiomanager
				am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "Alarm volume has been set to: \"" + am.getStreamVolume(AudioManager.STREAM_MUSIC) + "\", MediaPlayer is about to start");

				// Vibrate, -1 = no repeat
				vibrator.vibrate(pattern, -1);
				// Start play alarm signal
				mPlayer.start();
			}

			// Listen to completion, in other words when media player has finished and reset media volume and media player
			mPlayer.setOnCompletionListener(new OnCompletionListener() {
				// Counter variable to count number of times played, we have already played the alarm signal once
				int timesPlayed = 1;

				/**
				 * Listener to listen when message tone has finished playing.
				 */
				@Override
				public void onCompletion(MediaPlayer mPlayer) {
					// If message tone havn't been played enough times, else release mediaplayer
					if (timesPlayed < toBePlayed) {
						// Add to counter
						timesPlayed++;
						// Seek to beginning of message tone
						mPlayer.seekTo(0);
						// Start play message tone
						mPlayer.start();
					} else {
						am.setStreamVolume(AudioManager.STREAM_MUSIC, originalMediaVolume, 0);
						mPlayer.reset();

						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise().MediaPlayer.onCompletion()", "Media player and all sound levels have been restored");
					}
				}
			});
		} else {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "MediaPlayer is already playing, leave makeNoise()");
		}
	}

	/**
	 * Method to lookup proper alarm signal from given is.
	 * 
	 * @param context
	 *            Context
	 * @param alarmSignalId
	 *            Id of alarm signal as Integer
	 * @return Resolved alarm signal
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 */
	public String resolveAlarmSignal(Context context, int alarmSignalId) {
		// Resolve alarm signal from id
		String[] tonesArr = context.getResources().getStringArray(R.array.tones);

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":resolveAlarmSignal()", "Alarm signal has been resolved from id");

		return tonesArr[alarmSignalId];
	}
}
