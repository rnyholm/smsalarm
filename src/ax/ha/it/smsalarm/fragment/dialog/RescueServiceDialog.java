package ax.ha.it.smsalarm.fragment.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.widget.EditText;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.handler.LogHandler;
import ax.ha.it.smsalarm.handler.LogHandler.LogPriorities;

/**
 * {@link DialogFragment} which let's the user add or remove the <b><i>Rescue Service Name</i></b>.
 *
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 * @see #RESCUE_SERVICE
 * @see #RESCUE_SERVICE_DIALOG_TAG
 * @see #RESCUE_SERVICE_DIALOG_REQUEST_CODE
 */
public class RescueServiceDialog extends DialogFragment {
	private static final String LOG_TAG = RescueServiceDialog.class.getSimpleName();

	// Used as a key when putting data into bundles and intents, dialog tag can come in handy for classes using this dialog
	public static final String RESCUE_SERVICE = "rescueService";
	public static final String RESCUE_SERVICE_DIALOG_TAG = "rescueServiceDialog";

	// Request code used for this dialog
	public static final int RESCUE_SERVICE_DIALOG_REQUEST_CODE = 12;

	// For logging
	private LogHandler logger = LogHandler.getInstance();

	// Must have application context
	private Context context;

	// Must be declared as class variable as it will be used when handling instance states
	private EditText inputEditText;

	/**
	 * To create a new instance of {@link RescueServiceDialog}.
	 */
	public RescueServiceDialog() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":RescueServiceDialog()", "Creating a new Rescue Service dialog fragment");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate()", "Setting Context to dialog fragment");

		// Set context here, it's safe because this dialog fragment has been attached to it's container, hence we have access to context
		context = getActivity();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreateDialog()", "Creating and initializing dialog fragment");

		// Setup the EditText
		// @formatter:off
		inputEditText = new EditText(context);
		inputEditText.setHint(R.string.RESCUE_SERVICE_NAME_HINT); 	// Set hint to EditText
		inputEditText.setInputType(InputType.TYPE_CLASS_TEXT);		// Set input type to EditText
		// @formatter:on

		// If not null, the fragment is being re-created, get data from saved instance, if exist.
		// If saved instance doesn't contain certain key or it's associated value the edittext field will be empty
		if (savedInstanceState != null) {
			// Check if we got any data in saved instance associated with certain key
			if (savedInstanceState.getCharSequence(RESCUE_SERVICE) != null) {
				inputEditText.setText(savedInstanceState.getCharSequence(RESCUE_SERVICE).toString());
			}
		}

		// Setup the dialog with correct resources, listeners and values
		// @formatter:off
		return new AlertDialog.Builder(context)
				.setIcon(android.R.drawable.ic_dialog_info) 		// Set icon
				.setTitle(R.string.RESCUE_SERVICE_PROMPT_TITLE) 	// Set title
				.setMessage(R.string.RESCUE_SERVICE_PROMPT_MESSAGE) // Set message
				.setView(inputEditText) 							// Bind dialog to EditText
				// @formatter:on

				.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreateDialog().PositiveButton.OnClickListener().onClick()", "Positive Button pressed");

						// Create an intent and put data from this dialogs EditText and associate it with a certain key
						Intent intent = new Intent();
						intent.putExtra(RESCUE_SERVICE, inputEditText.getText().toString());

						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreateDialog().PositiveButton.OnClickListener().onClick()", "Intent created with extra, key: \"" + RESCUE_SERVICE + "\" and data: \"" + inputEditText.getText().toString() + "\"");

						// Make a call to this dialog fragments owning fragments onAcitivityResult with correct request code, result code and intent
						getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
					}
				})

				.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreateDialog().NegativeButton.OnClickListener().onClick()", "Negative Button pressed");
						getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, null);
					}
				})

				.create();
	}

	@Override
	public void onSaveInstanceState(Bundle arg0) {
		super.onSaveInstanceState(arg0);
		arg0.putCharSequence(RESCUE_SERVICE, inputEditText.getText().toString());

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onSaveInstanceState()", "Data has been stored to bundle on key: \"" + RESCUE_SERVICE + "\" with data: \"" + inputEditText.getText().toString() + "\"");
	}
}
