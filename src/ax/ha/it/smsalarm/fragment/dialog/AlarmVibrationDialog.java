/**
 * Copyright (c) 2015 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.fragment.dialog;

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
import ax.ha.it.smsalarm.alarm.Alarm.AlarmType;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.DataType;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.PrefKey;
import ax.ha.it.smsalarm.handler.VibrationHandler;

/**
 * {@link DialogFragment} which let's the user select <b><i>Alarm Vibration Pattern</i></b> for the different {@link AlarmType}.
 *
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 * @see #ALARM_VIBRATION
 * @see #ALARM_VIBRATION_DIALOG_TAG
 * @see #PRIMARY_ALARM_VIBRATION_DIALOG_REQUEST_CODE
 * @see #SECONDARY_ALARM_VIBRATION_DIALOG_REQUEST_CODE
 */
public class AlarmVibrationDialog extends DialogFragment {
	private static final String LOG_TAG = AlarmVibrationDialog.class.getSimpleName();

	// Used as a key when putting data into intents, dialog tag can come in handy for classes using this dialog
	public static final String ALARM_VIBRATION = "alarmVibration";
	public static final String ALARM_VIBRATION_DIALOG_TAG = "alarmVibrationDialog";

	// Request codes used for this dialog
	public static final int PRIMARY_ALARM_VIBRATION_DIALOG_REQUEST_CODE = 17;
	public static final int SECONDARY_ALARM_VIBRATION_DIALOG_REQUEST_CODE = 18;

	// Need to get instances of both Shared Preferences and Vibration Handler
	private final SharedPreferencesHandler prefHandler = SharedPreferencesHandler.getInstance();
	private final VibrationHandler vibrationHandler = VibrationHandler.getInstance();

	// Must have application context
	private Context context;

	// Must keep track of what alarm vibration is selected in the list
	private int selectedAlarmVibrationId = -1;

	// Need to have a list of all available vibration patterns
	private final List<String> vibrations;

	/**
	 * To create a new instance of {@link AlarmVibrationDialog}.
	 */
	public AlarmVibrationDialog() {
		// Want to get the list of all alarm vibration patterns
		vibrations = vibrationHandler.getVibrationPatterns();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set context here, it's safe because this dialog fragment has been attached to it's container, hence we have access to context
		context = getActivity();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// If not null, the fragment is being re-created, get id of selected item from saved instance, if exist, else resolve it
		if (savedInstanceState != null) {
			// Check if we got any data in saved instance associated with certain key
			selectedAlarmVibrationId = savedInstanceState.getInt(ALARM_VIBRATION);
		} else {
			selectedAlarmVibrationId = resolveSelectedAlarmVibrationId();
		}

		// Setup the dialog with correct resources, listeners and values
		// @formatter:off
		return new AlertDialog.Builder(context)
				.setIcon(android.R.drawable.ic_dialog_info)	// Set icon
				.setTitle(R.string.ALARM_VIBRATION_PROMPT_TITLE)		// Set title
				// @formatter:on

				// Set items in list view from a list of all available vibration patterns
				.setSingleChoiceItems(vibrations.toArray(new String[vibrations.size()]), selectedAlarmVibrationId, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int listPosition) {
						// Store selected alarm vibration id
						selectedAlarmVibrationId = listPosition;

						// Preview the vibration
						vibrationHandler.previewVibration(context, vibrations.get(selectedAlarmVibrationId));
					}
				})

				.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Want to stop the vibration from vibrating
						vibrationHandler.cancelVibrator();

						// Create an intent and put the selected alarm vibration in it and associate it with a certain key
						Intent intent = new Intent();
						intent.putExtra(ALARM_VIBRATION, vibrations.get(selectedAlarmVibrationId));

						// Make a call to this dialog fragments owning fragments onActivityResult with correct request code, result code and intent
						getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
					}
				})

				.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						vibrationHandler.cancelVibrator();
						getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, null);
					}
				})

				.create();
	}

	/**
	 * To resolve <b><i>Selected Alarm Vibration Id</i></b> in this {@link AlarmVibrationDialog}'s {@link ListView}. What kind of alarms vibration
	 * this should be resolved from depends on what <b><i>Target Request Code</i></b> the </code>AlarmVibrationDialog</code> has been set to. The
	 * supported Target request codes are:<br>
	 * - {@link AlarmVibrationDialog#PRIMARY_ALARM_VIBRATION_DIALOG_REQUEST_CODE}<br>
	 * - {@link AlarmVibrationDialog#SECONDARY_ALARM_VIBRATION_DIALOG_REQUEST_CODE}
	 * <p>
	 * If the Alarm Vibration Dialog has been set with any other Target Request Code a value of <code>-1</code> is returned, this is also the case if
	 * the alarm signal for some reason wasn't found in the list of alarm vibration patterns that populates the <code>ListView</code>.
	 * 
	 * @return Resolved Selected Alarm Vibration Id if it can be resolved, else -1.
	 */
	private int resolveSelectedAlarmVibrationId() {
		switch (getTargetRequestCode()) {
			case (PRIMARY_ALARM_VIBRATION_DIALOG_REQUEST_CODE):
				return vibrations.indexOf(prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_ALARM_VIBRATION_KEY, DataType.STRING, context, VibrationHandler.VIBRATION_PATTERN_SMS_ALARM));
			case (SECONDARY_ALARM_VIBRATION_DIALOG_REQUEST_CODE):
				return vibrations.indexOf(prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_ALARM_VIBRATION_KEY, DataType.STRING, context, VibrationHandler.VIBRATION_PATTERN_SMS_ALARM));
			default:
				if (SmsAlarm.DEBUG) {
					Log.e(LOG_TAG + ":resolveAlarmVibrationId()", "This AlarmVibrationDialog has been set with wrong target request code, hence alarm vibration id cannot be resolved, returning -1 (no default selection)");
				}
				return -1;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle arg0) {
		super.onSaveInstanceState(arg0);
		arg0.putInt(ALARM_VIBRATION, selectedAlarmVibrationId);
	}
}
