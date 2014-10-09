package ax.ha.it.smsalarm.fragment.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import ax.ha.it.smsalarm.pojo.Alarm.AlarmType;
import ax.ha.it.smsalarm.R;

/**
 * {@link DialogFragment} which let's the user select <b><i>Alarm Signal</i></b> for the different {@link AlarmType}.
 *
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 * @see #ALARM_SIGNAL
 * @see #ALARM_SIGNAL_DIALOG_TAG
 * @see #PRIMARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE
 * @see #SECONDARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE
 */
public class AlarmSignalDialog extends DialogFragment {
	// Used as a key when putting data into intents, dialog tag can come in handy for classes using this dialog
	public static final String ALARM_SIGNAL = "alarmSignal";
	public static final String ALARM_SIGNAL_DIALOG_TAG = "alarmSignalDialog";

	// Request codes used for this dialog
	public static final int PRIMARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE = 9;
	public static final int SECONDARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE = 10;

	// Must have application context
	private Context context;

	/**
	 * To create a new instance of {@link AlarmSignalDialog}.
	 */
	public AlarmSignalDialog() {
		// Just empty...
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set context here, it's safe because this dialog fragment has been attached to it's container, hence we have access to context
		context = getActivity();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Setup the dialog with correct resources, listeners and values
		// @formatter:off
		return new AlertDialog.Builder(context)
				.setIcon(android.R.drawable.ic_dialog_info)	// Set icon
				.setTitle(R.string.TONE_PROMPT_TITLE)		// Set title
				// @formatter:on

				// Set items to list view from resource containing alarm signals
				.setItems(R.array.tones, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int listPosition) {
						// Create an intent and put data from this dialogs Spinner and associate it with a certain key
						Intent intent = new Intent();
						intent.putExtra(ALARM_SIGNAL, listPosition);

						// Make a call to this dialog fragments owning fragments onAcitivityResult with correct request code, result code and intent
						getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
					}
				})

				.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, null);
					}
				})

				.create();
	}
}
