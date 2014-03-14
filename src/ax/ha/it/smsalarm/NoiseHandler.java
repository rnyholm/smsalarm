/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.Vibrator;
import ax.ha.it.smsalarm.LogHandler.LogPriorities;

/**
 * This class is responsible for all sound and vibration handling. This means
 * playing tones, vibrate and so on, depending on application and phone
 * settings.<br>
 * <b><i>NoiseHandler is a singleton.</i></b>
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.2
 * @since 2.0
 * 
 * @see RingerModeHandler
 */
public class NoiseHandler {
	// Singleton instance of this class
	private static NoiseHandler INSTANCE;

	// Log tag
	private final String LOG_TAG = getClass().getSimpleName();

	// Variable used to log messages
	private final LogHandler logger;

	// Initialize a MediaPlayer object
	private final MediaPlayer mPlayer = new MediaPlayer();
	
	// Vibrator variable
	private Vibrator vibrator;
	
	// Handler for ringer mode
	private final RingerModeHandler ringerModeHandler;

	/**
	 * Private constructor, is private due to it's singleton pattern.
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	private NoiseHandler(Context context) {
		// Get instance of logger
		logger = LogHandler.getInstance();
		
		ringerModeHandler = new RingerModeHandler(context);

		// Log information
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":NoiseHandler()", "New instance of NoiseHandler created");
	}

	/**
	 * Method to get the singleton instance of this class.
	 * 
	 * @return Singleton instance of NoiseHandler
	 */
	public static NoiseHandler getInstance(Context context) {
		// If instance of this object is null create a new one
		if (INSTANCE == null) {
			INSTANCE = new NoiseHandler(context);
		}

		return INSTANCE;
	}
	
	/**
	 * To return this objects <code>RingerModeHandler</code>.
	 * 
	 * @return This objects ringer mode handler.
	 * 
	 * @see RingerModeHandler
	 */
	public RingerModeHandler getRingerModeHandler() {
		return ringerModeHandler;
	}	

	/**
	 * Method to play message tone and vibrate, depending on application
	 * settings. This method also takes in account the operating systems sound
	 * settings, this depends on a input parameter.
	 * 
	 * @param context
	 *            Context
	 * @param id
	 *            ToneId as Integer
	 * @param useSoundSettings
	 *            If this method should take consideration to the device's sound
	 *            settings as Boolean
	 * @param playToneTwice
	 *            Indication whether message tone should be played once or
	 *            twice, this is the same for vibration also
	 * 
	 * @exception IllegalArgumentException
	 *                Can occur when setting data source for media player or
	 *                preparing media player
	 * @exception IllegalStateException
	 *                Can occur when setting data source for media player or
	 *                preparing media player
	 * @exception IOException
	 *                Can occur when setting data source for media player or
	 *                preparing media player. Also when resolving message tone
	 *                id
	 *              
 	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
 	 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String, String) logCatTxt(LogPriorities, String, String)
 	 * @see ax.ha.it.smsalarm.LogHandler#logCatTxt(LogPriorities, String, String, Throwable) logCatTxt(LogPriorities, String, String, Throwable)
	 */
	public void makeNoise(Context context, int id, boolean useSoundSettings, boolean playToneTwice) {
		// Log information
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "Preparing to play message tone and vibrate");
		
		while (!ringerModeHandler.isIdle()) {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "RingerModeHandler running, waiting....");
		}
		
		// Check if mediaplayer isn't playing already, in case it's playing we don't need to set up the player
		if(!mPlayer.isPlaying()) {
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
			float currentRingVolume = ((float) am.getStreamVolume(AudioManager.STREAM_RING)/(float) am.getStreamMaxVolume(AudioManager.STREAM_RING));
			// To store alarm volume in, calculate it at the same time
			int alarmVolume = (int) (currentRingVolume * am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
			
			// Custom vibration pattern
			long[] pattern = { 0, 5000, 500, 5000, 500, 5000, 500, 5000 };
	
			// Log information
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "MediaPlayer initalized, Audio Service initalized, Vibrator initalized, AssetFileDescriptor initalized. Correct alarm volume has been calculated. Vibration pattern variables is set.");
	
			// Resolve correct tone depending on id
			try {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "Try to resolve message tone");
				afd = context.getAssets().openFd("tones/alarm/" + msgToneLookup(context, id) + ".mp3");
			} catch (IOException e) {
				// Log IO Exception
				logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":makeNoise()", "An IOException occurred while resolving message tone from id", e);
			}
	
			// Set data source for mPlayer, common both for debug and ordinary mode
			try {
				mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "Data source has been set for Media Player");
			} catch (IllegalArgumentException e) {
				// Log IllegalArgumentException
				logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":makeNoise()", "An IllegalArgumentException occurred while setting data source for media player", e);
			} catch (IllegalStateException e) {
				// Log IllegalStateException
				logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":makeNoise()", "An IllegalStateException occurred while setting data source for media player", e);
			} catch (IOException e) {
				// Log IO Exception
				logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":makeNoise()", "An IOException occurred while setting data source for media player", e);
			}
	
			// Prepare mPlayer, also common for debug and ordinary mode
			try {
				mPlayer.prepare();
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "Media Player prepared");
			} catch (IllegalStateException e) {
				// Log IllegalStateException
				logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":makeNoise()", "An IllegalStateException occurred while preparing media player", e);
			} catch (IOException e) {
				// Log IO Exception
				logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":makeNoise()", "An IOException occurred while preparing media player", e);
			}
	
			// If false then just play message tone once else twice
			if (!playToneTwice) {
				toBePlayed = 1;
				// Log information
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "Message tone will be played once, if device not in RINGER_MODE_SILENT  or application is set to not consider device's sound settings");
			} else {
				toBePlayed = 2;
				// Log information
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "Message tone will be played twice, if device not in RINGER_MODE_SILENT or application is set to not consider device's sound settings");
			}
	
			/*
			 * If application use systems sound settings, check if phone is in
			 * normal, silent or vibration mode else don't check phones status and
			 * play tone and vibrate even if phone is in silent or vibrate mode
			 */
			if (useSoundSettings) {
				// Log information
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "Application is set to take into account device's sound settings");
	
				// Decide if phone are in normal, vibrate or silent state and take action
				switch (am.getRingerMode()) {
				case AudioManager.RINGER_MODE_SILENT:
					// Do nothing except log information
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "Device is in \"RINGER_MODE_SILENT\", don't vibrate or play message tone");
					// Reset mediaplayer
					mPlayer.reset();
					
					break;
				case AudioManager.RINGER_MODE_VIBRATE:
					// Vibrate, -1 = no repeat
					vibrator.vibrate(pattern, -1);
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "Device is in \"RINGER_MODE_VIBRATE\", just vibrate");
					
					break;
				case AudioManager.RINGER_MODE_NORMAL:
					// Log information
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "Device is in \"RINGER_MODE_NORMAL\", vibrate and play message tone");
					// Set correct volume to mediaplayer
					am.setStreamVolume(AudioManager.STREAM_MUSIC, alarmVolume, 0);	
					// Log information
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "Alarm volume has been set to: \"" + am.getStreamVolume(AudioManager.STREAM_MUSIC) + "\", MediaPlayer is about to start");	
					// Vibrate, -1 = no repeat
					vibrator.vibrate(pattern, -1);
					// Start play message tone
					mPlayer.start();
					
					break;
				default: // <--Unsupported RINGER_MODE
					// Do nothing except log information
					logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":makeNoise()", "Device is in a \"UNSUPPORTED\" ringer mode, can't decide what to do");
				}
			} else { // If not take into account OS sound setting, always ring at highest volume and vibrate
				// Log information
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "Application is set to don't take into account device's sound settings. Play message tone at max volume and vibrate");
				// Set maximum volume to audiomanager
				am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
				// Log information
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "Alarm volume has been set to: \"" + am.getStreamVolume(AudioManager.STREAM_MUSIC) + "\", MediaPlayer is about to start");
				// Vibrate, -1 = no repeat
				vibrator.vibrate(pattern, -1);
				// Start play message tone
				mPlayer.start();	
			}
	
			// Listen to completion, in other words when media player has finished and reset media volume and media player
			mPlayer.setOnCompletionListener(new OnCompletionListener() {
				// Counter variable to count number of times played, we have already played the message tone once
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
						// Log information
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise().MediaPlayer.onCompletion()", "Media player and all sound levels have been restored");
					}
				}
			});
		} else {
			// Log information
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":makeNoise()", "MediaPlayer is already playing, leave makeNoise()");
		}
	}

	/**
	 * Method to lookup proper message tone from media.
	 * 
	 * @param context
	 *            Context
	 * @param toneId
	 *            ToneId as Integer
	 * @return toneId Message tone as String
	 * 
	 * @see ax.ha.it.smsalarm.LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 */
	public String msgToneLookup(Context context, int toneId) {
		// Resolve message tone from id
		String[] tonesArr = context.getResources().getStringArray(R.array.tones);

		// Some logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":msgToneLookup()", "Message tone has been resolved from id");

		return tonesArr[toneId];
	}
	
	/**
	 * This class is responsible for changing <code>RINGER_MODE</code> if needed. 
	 * Class makes it possible to change ringer mode <b><i>permanently</i></b> or under
	 * a <b><i>certain amount of time</i></b>. Class also keeps track of ringer mode before
	 * change to make it possible to restore it if needed.
	 * 
	 * @author Robert Nyholm <robert.nyholm@aland.net>
	 * @version 2.2
	 * @since 2.2
	 */
	public class RingerModeHandler {
		// Log tag
		private final String LOG_TAG = getClass().getSimpleName();
		
		// Wished delay for a ringer mode switch
		private final static int DELAY = 6000;
		
		// Variable used to log messages
		private final LogHandler logger = LogHandler.getInstance();
		
		// Ringer mode handling
		private final AudioManager am;
		
		// Handler to manage delay
		private final Handler handler = new Handler();
	
		// Devices "normal" ringer mode are stored in this variable during ringer mode change
		private int RINGER_MODE = -1;
		
		// Indicates whether this object is running or idle
		private boolean idle = true;
		
		/**
		 * Construct a new <code>RingerModeHandler</code> object with correct <code>Context</code>.
		 * 
		 * @param context Context from which RingerModeHandler get <code>systemService(Context.AUDIO_SERVICE)</code> from.
		 * 
		 * @see LogHandler#logCat(LogPriorities, String, String)
		 */
		public RingerModeHandler(Context context) {
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":RingerModeHandler()", "New instance of RingerModeHandler created");
			am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		}	
		
		/**
		 * To change device's <code>RINGER_MODE</code> for a certain amount of time,
		 * after elapsed time the ringer mode is restored. Argument in decides which
		 * ringer mode device should change to.
		 * 
		 * @param ringerMode Ringer mode to set to device.
		 * 
		 * @see #setSilent()
		 * @see #setVibrate()
		 * @see #setNormal()
		 * @see LogHandler#logCat(LogPriorities, String, String)
		 * @see LogHandler#logCatTxt(LogPriorities, String, String)
		 */
		public void setRingerModeWithDelay(int ringerMode) {
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":setRingerModeWithDelay()", "Ringer mode is about to be changed for a time of:\"" + DELAY + "\"ms");
			boolean ringerModeSuccessfullyChanged = false;
			
			// Only if object is idle
			if (idle) {
				this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":setRingerModeWithDelay()", "RingerModeHandler is idle, continue with ringer mode change");
				
				// Switch through ringer mode and set correct ringer mode,
				// ringer mode switch success are stored to boolean
				switch (ringerMode) {
				case (AudioManager.RINGER_MODE_NORMAL):
					ringerModeSuccessfullyChanged = setNormal();
					break;
				case (AudioManager.RINGER_MODE_VIBRATE):
					ringerModeSuccessfullyChanged = setVibrate();
					break;
				case (AudioManager.RINGER_MODE_SILENT):
					ringerModeSuccessfullyChanged = setSilent();
					break;
				default:
					this.logger.logCatTxt(LogPriorities.ERROR, this.LOG_TAG + ":setRingerModeWithDelay()", "An unsopported ringer mode was given as argument, ringer mode:\"" + Integer.toString(ringerMode) + "\"");
					break;
				}
			}
			
			// If we successfully switched ringer mode we start the delay
			if (ringerModeSuccessfullyChanged) {
				this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":setRingerModeWithDelay()", "Ringer mode successfully changed, setting RingerModeHandler to not idle and starting delay");
				// Object is running
				idle = false;
				
//				 // do something long
//			    Runnable runnable = new Runnable() {
//			      @Override
//			      public void run() {
//			    	  try {
//						Thread.sleep(DELAY);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//			      }
//			    };
//			    new Thread(runnable).start();
			    
			    Timer myTimer = new Timer();
			    myTimer.schedule(new TimerTask() {          
				    @Override
				    public void run() {
				    	restore(true);
				    }
			    }, DELAY); // initial delay 1 second, interval 1 second
				
//				handler = new Handler();
//				handler.postDelayed(new Runnable() {
//				    @Override
//				    public void run() {
//				    	// Restore this object and devices ringer mode
//				    	restore(true);
//				    }
//				}, DELAY);	
			}
		}
		
		/**
		 * To set devices ringer mode into <code>AudioManager.RINGER_MODE_SILENT</code>.
		 * 
		 * @return <code>true</code> if ringer mode change was successful else <code>false</code>.
		 * 
		 * @see LogHandler#logCat(LogPriorities, String, String)
		 * @see LogHandler#logCatTxt(LogPriorities, String, String)
		 */
		public boolean setSilent() {
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":setSilent()", "Device is about to be set into RINGER_MODE_SILENT");
			
			// Only if object is initialized, otherwise we get a NPE
			if (isInitialized()) {
				// Store devices "normal" ringer mode, we in case we want to restore it later
				RINGER_MODE = am.getRingerMode();
				this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":setSilent()", "\"Normal\" ringer mode is:\"" + Integer.toString(RINGER_MODE) + "\" and has been stored for future usage");
				
				// Set ringer mode to silent
				am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
				this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":setSilent()", "Device has successfully been set into RINGER_MODE_SILENT");
				
				// Success return true
				return true;
			}
			
			// For some reason the ringer mode change didn't go through, most likely the audiomanager is null
			this.logger.logCatTxt(LogPriorities.ERROR, this.LOG_TAG + ":setSilent()", "Failed to put device into RINGER_MODE_SILENT");
			return false;
		}
		
		/**
		 * To set devices ringer mode into <code>AudioManager.RINGER_MODE_VIBRATE</code>.
		 * 
		 * @return <code>true</code> if ringer mode change was successful else <code>false</code>.
		 * 
		 * @see LogHandler#logCat(LogPriorities, String, String)
		 * @see LogHandler#logCatTxt(LogPriorities, String, String)
		 */
		public boolean setVibrate() {
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":setVibrate()", "Device is about to be set into RINGER_MODE_VIBRATE");
			
			if (isInitialized()) {
				RINGER_MODE = am.getRingerMode();
				this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":setVibrate()", "\"Normal\" ringer mode is:\"" + Integer.toString(RINGER_MODE) + "\" and has been stored for future usage");
				
				am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
				this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":setVibrate()", "Device has successfully been set into RINGER_MODE_VIBRATE");
				
				return true;
			}
			
			this.logger.logCatTxt(LogPriorities.ERROR, this.LOG_TAG + ":setVibrate()", "Failed to put device into RINGER_MODE_VIBRATE");
			return false;
		}
		
		/**
		 * To set devices ringer mode into <code>AudioManager.RINGER_MODE_NORMAL</code>.
		 * 
		 * @return <code>true</code> if ringer mode change was successful else <code>false</code>.
		 * 
		 * @see LogHandler#logCat(LogPriorities, String, String)
		 * @see LogHandler#logCatTxt(LogPriorities, String, String)
		 */
		public boolean setNormal() {
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":setNormal()", "Device is about to be set into RINGER_MODE_NORMAL");
			
			if (isInitialized()) {
				RINGER_MODE = am.getRingerMode();
				this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":setNormal()", "\"Normal\" ringer mode is:\"" + Integer.toString(RINGER_MODE) + "\" and has been stored for future usage");
				
				am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":setNormal()", "Device has successfully been set into RINGER_MODE_NORMAL");
				
				return true;
			}
			
			this.logger.logCatTxt(LogPriorities.ERROR, this.LOG_TAG + ":setNormal()", "Failed to put device into RINGER_MODE_NORMAL");
			return false;
		}
		
		/**
		 * To restore this object to it's default state(state when it was created),
		 * the method also restores the device's "normal" ringer mode if needed.
		 * 
		 * @param restoreRingerMode If devices ringer mode also should be restored if possible.
		 * 
		 * @see LogHandler#logCat(LogPriorities, String, String)
		 */
		public void restore(boolean restoreRingerMode) {
			this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":restore()", "RingerModeHandler is about to restored and \"normal\" is about to be set to device");
			
			// Only needed if this object is initialized
			if (isInitialized()) {
				// If this object holds a valid ringer mode the devices ringer 
				// mode has been changed and we are able to restore it if thats wished
				if (isValidRingerMode() && restoreRingerMode) {
					am.setRingerMode(RINGER_MODE);
					this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":restore()", "Ringer mode:\"" + RINGER_MODE + "\" has been set as the devices \"normal\" ringer mode");
				}
				
				// Set default values
				RINGER_MODE = -1;
				idle = true;
				
				this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":restore()", "RingerModeHandler has been restored");
			}
		}
		
		/**
		 * To figure out if this object is idle or not. By not being idle means
		 * that a ringer mode has changed for only a specific amount of time.
		 * 
		 * @return <code>true</code> if object is idle else <code>false</code>.
		 */
		public boolean isIdle() {
			return idle;
		}
		
		/**
		 * Helper method to figure out if this object has been correctly initialized.
		 * 
		 * @return <code>true</code> if correctly initialized else <code>false</code>.
		 * 
		 * @see LogHandler#logCat(LogPriorities, String, String)
		 * @see LogHandler#logCatTxt(LogPriorities, String, String)
		 */
		private boolean isInitialized() {
			if (am != null) {
				this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":isInitialized()", "AudioManager has been correctly initialized, returning true");
				return true;
			}
			
			this.logger.logCatTxt(LogPriorities.ERROR, this.LOG_TAG + ":isInitialized()", "AudioManager has not been correctly initialized, returning false");
			return false;
		}
		
		/**
		 * Helper method to figure out if this objects <b><i>RINGER_MODE</i></b> is valid.
		 * In other words some of following:<p>
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
				this.logger.logCat(LogPriorities.DEBUG, this.LOG_TAG + ":isValidRingerMode()", "Ringer mode is valid, returning true");
				return true;
			}
			
			this.logger.logCatTxt(LogPriorities.ERROR, this.LOG_TAG + ":isValidRingerMode()", "Ringer mode is not valid, returning false");
			return false;
		}
	}
}
