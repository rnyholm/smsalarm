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
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.handler.LogHandler;
import ax.ha.it.smsalarm.handler.LogHandler.LogPriorities;
import ax.ha.it.smsalarm.ui.NoBlanksInputEditText;

/**
 * {@link DialogFragment} which lets the user add or remove the <b><i>Acknowledge Phone Number</i></b>.
 *
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class AcknowledgeNumberDialog extends DialogFragment {
	private static final String LOG_TAG = AcknowledgeNumberDialog.class.getSimpleName();
	// Used as a key when putting data into bundles and intents
	public static final String ACKNOWLEDGE_NUMBER = "acknowledgeNumber";

	// For for logging
	private LogHandler logger = LogHandler.getInstance();

	// Must have application context
	private Context context;

	// Must be declared as class variable as it will be used when handling instance states
	private NoBlanksInputEditText inputEditText;

	/**
	 * To create a new instance of {@link AcknowledgeNumberDialog}.
	 */
	public AcknowledgeNumberDialog() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":AcknowledgeNumberDialog()", "Creating a new Acknowledge Number dialog fragment");
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
		inputEditText = new NoBlanksInputEditText(context);
		inputEditText.setHint(R.string.NUMBER_PROMPT_HINT);		// Set hint to EditText
		inputEditText.setInputType(InputType.TYPE_CLASS_PHONE);	// Set input type to EditText
		// @formatter:on

		// If not null, the fragment is being re-created, get data from saved instance, if exist.
		// If saved instance doesn't contain certain key or it's associated value the edittext field will be empty
		if (savedInstanceState != null) {
			// Check if we got any data in saved instance associated with certain key
			if (savedInstanceState.getCharSequence(ACKNOWLEDGE_NUMBER) != null) {
				inputEditText.setText(savedInstanceState.getCharSequence(ACKNOWLEDGE_NUMBER).toString());
			}
		}

		// Setup the dialog with correct resources, listeners and values
		// @formatter:off
		return new AlertDialog.Builder(context)
				.setIcon(android.R.drawable.ic_dialog_info) 	// Set icon
				.setTitle(R.string.NUMBER_PROMPT_TITLE) 		// Set title
				.setMessage(R.string.ACK_NUMBER_PROMPT_MESSAGE) // Set message
				.setCancelable(false)							// Set dialog to non cancelable
				.setView(inputEditText) 						// Bind dialog to EditText
				// @formatter:on

				.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreateDialog().PositiveButton.OnClickListener().onClick()", "Positive Button pressed");

						// Create an intent and put data from this dialogs EditText and associate it with a certain key
						Intent intent = new Intent();
						intent.putExtra(ACKNOWLEDGE_NUMBER, inputEditText.getText().toString());

						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreateDialog().PositiveButton.OnClickListener().onClick()", "Intent created with extra, key: \"" + ACKNOWLEDGE_NUMBER + "\" and data: \"" + inputEditText.getText().toString() + "\"");

						// Make a call to this dialog fragments owning fragments onAcitivittyResult with correct request code, result code and intent
						getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
					}
				})

				.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreateDialog().NegativeButton.OnClickListener().onClick()", "Neutral Button pressed");
						getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, null);
					}
				})

				.create();
	}

	@Override
	public void onSaveInstanceState(Bundle arg0) {
		super.onSaveInstanceState(arg0);
		arg0.putCharSequence(ACKNOWLEDGE_NUMBER, inputEditText.getText().toString());

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onSaveInstanceState()", "Data has been stored to bundle on key: \"" + ACKNOWLEDGE_NUMBER + "\" with data: \"" + inputEditText.getText().toString() + "\"");
	}
}
