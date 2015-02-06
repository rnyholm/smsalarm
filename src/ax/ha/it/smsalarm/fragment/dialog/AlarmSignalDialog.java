/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.fragment.dialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.ListView;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.activity.SmsAlarm;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.DataType;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.PrefKey;
import ax.ha.it.smsalarm.handler.SoundHandler;
import ax.ha.it.smsalarm.pojo.Alarm.AlarmType;
import ax.ha.it.smsalarm.util.Util;

/**
 * {@link DialogFragment} which let's the user select <b><i>Alarm Signal</i></b> for the different {@link AlarmType}. THis dialog also let's the user
 * add an own alarm signal from the device with the only restriction that the alarm signal <b><i>must be in format mpeg format</i></b>.
 *
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 * @see #ALARM_SIGNAL
 * @see #ALARM_SIGNAL_DIALOG_TAG
 * @see #PRIMARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE
 * @see #SECONDARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE
 * @see #ADD_ALARM_SIGNAL_FROM_PRIMARY_DIALOG_REQUEST_CODE
 * @see #ADD_ALARM_SIGNAL_FROM_SECONDARY_DIALOG_REQUEST_CODE
 */
public class AlarmSignalDialog extends DialogFragment {
	private static final String LOG_TAG = AlarmSignalDialog.class.getSimpleName();

	// Used as a key when putting data into intents, dialog tag can come in handy for classes using this dialog
	public static final String ALARM_SIGNAL = "alarmSignal";
	public static final String ALARM_SIGNAL_DIALOG_TAG = "alarmSignalDialog";

	// Accepted MIME type for alarm signal selection
	private static final String ALARM_SIGNAL_MIME_TYPE = "audio/mpeg";

	// Request codes used for this dialog
	public static final int PRIMARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE = 9;
	public static final int SECONDARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE = 10;

	// Request codes used for adding alarm signals, different codes in order to be able to build up correct dialog again after an alarm signal has
	// been chosen
	public static final int ADD_ALARM_SIGNAL_FROM_PRIMARY_DIALOG_REQUEST_CODE = 15;
	public static final int ADD_ALARM_SIGNAL_FROM_SECONDARY_DIALOG_REQUEST_CODE = 16;

	// Need to get instances of both Shared Preferences and Sound Handler
	private final SharedPreferencesHandler prefHandler = SharedPreferencesHandler.getInstance();
	private final SoundHandler soundHandler = SoundHandler.getInstance();

	// Must have application context
	private Context context;

	// Must keep track of what alarm signal is selected in the list
	private int selectedAlarmSignalId = -1;

	// List holding the paths to the user added alarm signals
	private List<String> appAlarmSignals = new ArrayList<String>();
	private List<String> userAddedAlarmSignals = new ArrayList<String>();
	private List<String> allAlarmSignals = new ArrayList<String>();

	/**
	 * To create a new instance of {@link AlarmSignalDialog}.
	 */
	public AlarmSignalDialog() {
		// Just empty...
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set context here, it's safe because this dialog fragment has been attached to it's container, hence we have access to context
		context = getActivity();

		// Get all application provided and user added alarm signals, also smack them together into one sperate list
		appAlarmSignals = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.alarm_signals)));
		userAddedAlarmSignals = (List<String>) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.USER_ADDED_ALARM_SIGNALS_KEY, DataType.LIST, context);
		allAlarmSignals.addAll(appAlarmSignals);
		allAlarmSignals.addAll(userAddedAlarmSignals);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		// If not null, the fragment is being re-created, get id of selected item from saved instance, if exist, else resolve it
		if (savedInstanceState != null) {
			// Check if we got any data in saved instance associated with certain key
			selectedAlarmSignalId = savedInstanceState.getInt(ALARM_SIGNAL);
		} else {
			selectedAlarmSignalId = resolveSelectedAlarmSignalId();
		}

		// Create a new list containing all application and user added alarm signal, but the user added alarm signals will be formatted for a nicer
		// display in the list view
		List<String> alarmSignals = new ArrayList<String>();
		alarmSignals.addAll(appAlarmSignals);

		for (String userAddedAlarmSignal : userAddedAlarmSignals) {
			alarmSignals.add(Util.getBaseFileName(userAddedAlarmSignal));
		}

		// Setup the dialog with correct resources, listeners and values
		// @formatter:off
		return new AlertDialog.Builder(context)
				.setIcon(android.R.drawable.ic_dialog_info)	// Set icon
				.setTitle(R.string.TONE_PROMPT_TITLE)		// Set title
				// @formatter:on

				// Set items to list view from resource containing alarm signals
				.setSingleChoiceItems(alarmSignals.toArray(new String[alarmSignals.size()]), selectedAlarmSignalId, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int listPosition) {
						// Store the selected alarm signal id
						selectedAlarmSignalId = listPosition;

						// Play selected tone
						soundHandler.previewSignal(context, allAlarmSignals.get(listPosition));
					}
				})

				.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Want to stop media player if it's already playing
						soundHandler.stopMediaPlayer();

						// Create an intent and put selected alarm signal id in it and associate it with a certain key
						Intent intent = new Intent();
						intent.putExtra(ALARM_SIGNAL, allAlarmSignals.get(selectedAlarmSignalId));

						// Make a call to this dialog fragments owning fragments onAcitivityResult with correct request code, result code and intent
						getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
					}
				})

				.setNeutralButton(R.string.ADD, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						soundHandler.stopMediaPlayer();

						// Resolve the request code
						int resolvedRequestCode = fromDialogToIntentRequestCode(getTargetRequestCode());

						// Sanity check, resolved request code cannot be anything else
						if (resolvedRequestCode == ADD_ALARM_SIGNAL_FROM_PRIMARY_DIALOG_REQUEST_CODE || resolvedRequestCode == ADD_ALARM_SIGNAL_FROM_SECONDARY_DIALOG_REQUEST_CODE) {
							// To add a new alarm signal by using the existing Android file choosing functionality with the help of intents, this will
							// get handled in SmsAlarm.onActivityResult();
							Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
							intent.setType(ALARM_SIGNAL_MIME_TYPE);
							getActivity().startActivityForResult(Intent.createChooser(intent, null), resolvedRequestCode);
						}
					}
				})

				.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						soundHandler.stopMediaPlayer();
						getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, null);
					}
				})

				.create();
	}

	/**
	 * Convenience method to resolve the given <code>dialogRequestCode</code> into a <code>requestCode</code> used for the {@link Intent} with
	 * <b><i>Action</i></b> {@link Intent#ACTION_GET_CONTENT}. This intent is used for letting the user select an own <b><i>Alarm Signal</i></b>.<br>
	 * If for some reason the given <code>dialogRequestCode</code> couldn't be resolved the value of <b><i>-1</i></b> will be returned.
	 * 
	 * @param dialogRequestCode
	 *            Request code of the dialog that will be resolved into the request code for the corresponding <code>Intent</code>. Valid values are
	 *            {@link #PRIMARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE} and {@value #SECONDARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE}
	 * @return Resolved request code of either {@link #ADD_ALARM_SIGNAL_FROM_PRIMARY_DIALOG_REQUEST_CODE} or
	 *         {@link #ADD_ALARM_SIGNAL_FROM_SECONDARY_DIALOG_REQUEST_CODE} if it could be resolved, else <b><i>-1</i></b>.
	 */
	public static int fromDialogToIntentRequestCode(int dialogRequestCode) {
		int requestCode = -1;

		// Resolve correct request code
		if (dialogRequestCode == PRIMARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE) {
			requestCode = ADD_ALARM_SIGNAL_FROM_PRIMARY_DIALOG_REQUEST_CODE;
		} else if (dialogRequestCode == SECONDARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE) {
			requestCode = ADD_ALARM_SIGNAL_FROM_SECONDARY_DIALOG_REQUEST_CODE;
		}

		return requestCode;
	}

	/**
	 * Convenience method to resolve the given <code>intentRequestCode</code> into a <code>requestCode</code> used for as the
	 * {@link AlarmSignalDialog}'s request code.<br>
	 * If for some reason the given <code>intentRequestCode</code> couldn't be resolved the value of <b><i>-1</i></b> will be returned.
	 * 
	 * @param intentRequestCode
	 *            Request code of the {@link Intent} that will be resolved into the request code for the corresponding <code>AlarmSignalDialog</code>.
	 *            Valid values are {@link #ADD_ALARM_SIGNAL_FROM_PRIMARY_DIALOG_REQUEST_CODE} and
	 *            {@link #ADD_ALARM_SIGNAL_FROM_SECONDARY_DIALOG_REQUEST_CODE}
	 * @return Resolved request code of either {@link #PRIMARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE} or
	 *         {@link #SECONDARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE} if it could be resolved, else <b><i>-1</i></b>.
	 */
	public static int fromIntentToDialogRequestCode(int intentRequestCode) {
		int requestCode = -1;

		// Resolve correct request code
		if (intentRequestCode == ADD_ALARM_SIGNAL_FROM_PRIMARY_DIALOG_REQUEST_CODE) {
			requestCode = PRIMARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE;
		} else if (intentRequestCode == ADD_ALARM_SIGNAL_FROM_SECONDARY_DIALOG_REQUEST_CODE) {
			requestCode = SECONDARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE;
		}

		return requestCode;
	}

	/**
	 * To resolve <b><i>Selected Alarm Signal Id</i></b> in this {@link AlarmSignalDialog}'s {@link ListView}. What kind of alarms signal this should
	 * be resolved from depends on what <b><i>Target Request Code</i></b> the </code>AlarmSignalDialog</code> has been set to. The supported Target
	 * request codes are:<br>
	 * - {@link AlarmSignalDialog#PRIMARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE}<br>
	 * - {@link AlarmSignalDialog#SECONDARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE}
	 * <p>
	 * If the Alarm Signal Dialog has been set with any other Target Request Code a value of <code>-1</code> is returned, this is also the case if the
	 * alarm signal for some reason wasn't found in the list of alarm signals that populates the <code>ListView</code>.
	 * 
	 * @return Resolved Selected Alarm Signal Id if it can be resolved, else -1.
	 */
	private int resolveSelectedAlarmSignalId() {
		switch (getTargetRequestCode()) {
			case (PRIMARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE):
				return allAlarmSignals.indexOf(prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_ALARM_SIGNAL_KEY, DataType.STRING, context, soundHandler.resolveAlarmSignal(context, SoundHandler.DEFAULT_PRIMARY_ALARM_SIGNAL_ID)));
			case (SECONDARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE):
				return allAlarmSignals.indexOf(prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_ALARM_SIGNAL_KEY, DataType.STRING, context, soundHandler.resolveAlarmSignal(context, SoundHandler.DEFAULT_SECONDARY_ALARM_SIGNAL_ID)));
			default:
				if (SmsAlarm.DEBUG) {
					Log.e(LOG_TAG + ":resolveAlarmSignalId()", "This AlarmSignalDialog has been set with wrong target request code, hence alarm signal id cannot be resolved, returning -1 (no default selection)");
				}
				return -1;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle arg0) {
		super.onSaveInstanceState(arg0);
		arg0.putInt(ALARM_SIGNAL, selectedAlarmSignalId);
	}
}
