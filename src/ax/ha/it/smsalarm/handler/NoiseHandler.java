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
import android.util.Log;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.activity.SmsAlarm;

/**
 * Class responsible for all sound and vibration handling. This means playing alarm signal, vibrate and so on, depending on application and phone
 * settings.<br>
 * <b><i>NoiseHandler is a singleton.</i></b>
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.0
 */
public class NoiseHandler {
	// Singleton instance of this class
	private static NoiseHandler INSTANCE;

	private static final String LOG_TAG = NoiseHandler.class.getSimpleName();

	// A limit time for how long we can wait for the KitKat handler to be idle, this works as a
	// security to be sure that noise always going to be made
	private static final long NOISE_DELAY_LIMIT = 10000;

	// Initialize a MediaPlayer object
	private final MediaPlayer mediaPlayer = new MediaPlayer();

	// Need to handle vibration
	private Vibrator vibrator;

	// Handler needed for KitKat and later releases special treatment
	private final KitKatHandler kitKatHandler;

	/**
	 * Creates a new instance of {@link NoiseHandler}.
	 */
	private NoiseHandler() {
		// Get instance of KitKat handler
		kitKatHandler = KitKatHandler.getInstance();
	}

	/**
	 * To get the <b><i>singleton</i></b> instance of {@link NoiseHandler}.
	 * 
	 * @return Instance of <code>NoiseHandler</code>.
	 */
	public static NoiseHandler getInstance() {
		// If instance of this object is null create a new one
		if (INSTANCE == null) {
			INSTANCE = new NoiseHandler();
		}

		return INSTANCE;
	}

	/**
	 * To play alarm signal and vibrate, depending on application settings. This method also takes the operating systems sound settings into account,
	 * depends on a input parameter.
	 * 
	 * @param context
	 *            The Context in which {@link NoiseHandler} runs.
	 * @param id
	 *            Id of alarm signal.
	 * @param useSoundSettings
	 *            If this method should take consideration to the device's sound settings or not.
	 * @param playAlarmSignalTwice
	 *            Indication whether the alarm signal/vibration should be played once or twice.
	 */
	public void doNoise(Context context, int id, boolean useSoundSettings, boolean playAlarmSignalTwice) {
		// Need to know when called this method
		long startTimeMillis = System.currentTimeMillis();

		// Need to wait until KitKat handler is in idle mode
		while (!kitKatHandler.isIdle() && (System.currentTimeMillis() < (startTimeMillis + NOISE_DELAY_LIMIT))) {
			if (SmsAlarm.DEBUG) {
				Log.d(LOG_TAG + ":doNoise()", "KitKatHandler running, waiting....");
			}
		}

		// Check if MediaPlayer isn't playing already, in case it's playing it's not needed to set up the player
		if (!mediaPlayer.isPlaying()) {
			// Declarations of different objects needed by makeNoise
			// AudioManager used to get and set different volume levels
			final AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			// AssetFileDescriptor to get mp3 file
			AssetFileDescriptor afd = null;
			// Set vibrator from context
			vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
			// Store original media volume
			final int originalMediaVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
			// Variable indicating how many times the alarm signal should be played
			final int toBePlayed;
			// Calculate current ring volume in percent
			float currentRingVolume = ((float) am.getStreamVolume(AudioManager.STREAM_RING) / (float) am.getStreamMaxVolume(AudioManager.STREAM_RING));
			// To store alarm volume in, calculate it at the same time
			int alarmVolume = (int) (currentRingVolume * am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));

			// Custom vibration pattern
			long[] pattern = { 0, 5000, 500, 5000, 500, 5000, 500, 5000 };

			// Resolve correct alarm signal depending on id
			try {
				afd = context.getAssets().openFd("alarm-signals/" + resolveAlarmSignal(context, id) + ".mp3");
			} catch (IOException e) {
				if (SmsAlarm.DEBUG) {
					Log.e(LOG_TAG + ":doNoise()", "An error occurred while setting correct alarm signal to AssetFileDescriptor", e);
				}
			}

			// Set data source for media player and prepare it
			try {
				mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
				mediaPlayer.prepare();
			} catch (IllegalStateException e) {
				if (SmsAlarm.DEBUG) {
					Log.e(LOG_TAG + ":doNoise()", "Mediaplayer is in an illegal state for setting datasource or preparing it", e);
				}
			} catch (IOException e) {
				if (SmsAlarm.DEBUG) {
					Log.e(LOG_TAG + ":doNoise()", "An error occurred while setting datasource to mediaplayer or preparing it", e);
				}
			}

			// If false then just play alarm signal once else twice
			if (!playAlarmSignalTwice) {
				toBePlayed = 1;
			} else {
				toBePlayed = 2;
			}

			// If application use systems sound settings, check if phone is in normal, silent or vibration mode else don't check phones status and
			// play tone and vibrate even if phone is in silent or vibrate mode
			if (useSoundSettings) {
				// Decide if phone are in normal, vibrate or silent state and take action
				switch (am.getRingerMode()) {
					case AudioManager.RINGER_MODE_SILENT:
						// Reset media player
						mediaPlayer.reset();

						break;
					case AudioManager.RINGER_MODE_VIBRATE:
						// Vibrate, -1 = no repeat
						vibrator.vibrate(pattern, -1);

						break;
					case AudioManager.RINGER_MODE_NORMAL:
						// Set correct volume to media player
						am.setStreamVolume(AudioManager.STREAM_MUSIC, alarmVolume, 0);

						// Vibrate, -1 = no repeat
						vibrator.vibrate(pattern, -1);

						// Start play message tone
						mediaPlayer.start();

						break;
					default: // Unsupported RINGER_MODE
						if (SmsAlarm.DEBUG) {
							Log.d(LOG_TAG + ":doNoise()", "Device is in a \"UNSUPPORTED\" ringer mode, can't decide what to do");
						}
				}
			} else { // If not take OS sound setting into account, always ring at highest volume and vibrate
				// Set maximum volume to audio manager
				am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
				// Vibrate, -1 = no repeat
				vibrator.vibrate(pattern, -1);
				// Start play alarm signal
				mediaPlayer.start();
			}

			// Listen to completion, in other words when media player has finished and reset media volume and media player
			mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				// Counter variable to count number of times played, we have already played the alarm signal once
				int timesPlayed = 1;

				@Override
				public void onCompletion(MediaPlayer mediaPlayer) {
					// If alarm signal havn't been played enough times, else release media player
					if (timesPlayed < toBePlayed) {
						// Add to counter
						timesPlayed++;
						// Seek to beginning of message tone
						mediaPlayer.seekTo(0);
						// Start play message tone
						mediaPlayer.start();
					} else {
						am.setStreamVolume(AudioManager.STREAM_MUSIC, originalMediaVolume, 0);
						mediaPlayer.reset();
					}
				}
			});
		}
	}

	/**
	 * To lookup correct alarm signal from given id.
	 * 
	 * @param context
	 *            The Context in which {@link NoiseHandler} runs.
	 * @param alarmSignalId
	 *            Id of alarm signal.
	 * @return Resolved alarm signal.
	 */
	public String resolveAlarmSignal(Context context, int alarmSignalId) {
		// Resolve alarm signal from id
		String[] alarmSignals = context.getResources().getStringArray(R.array.alarm_signals);
		return alarmSignals[alarmSignalId];
	}
}
