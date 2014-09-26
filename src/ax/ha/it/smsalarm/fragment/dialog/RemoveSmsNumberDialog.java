package ax.ha.it.smsalarm.fragment.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.handler.LogHandler;
import ax.ha.it.smsalarm.handler.LogHandler.LogPriorities;

/**
 * {@link DialogFragment} which let's the user remove a <b><i>Phone Number</i></b> from the list of <b><i>Primary or Secondary Alarm Trigger Phone
 * Numbers</i></b>, this depends on which <b><i>RequestCode</i></b> this <code>RemoveSmsNumberDialog</code> is set to.<br>
 *
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 * @see AddSmsNumberDialog
 * @see #REMOVE_SMS_NUMBER
 * @see #REMOVE_SMS_NUMBER_DIALOG_TAG
 * @see #REMOVE_PRIMARY_SMS_NUMBER_DIALOG_REQUEST_CODE
 * @see #REMOVE_SECONDARY_SMS_NUMBER_DIALOG_REQUEST_CODE
 */
public class RemoveSmsNumberDialog extends DialogFragment {
	private static final String LOG_TAG = RemoveSmsNumberDialog.class.getSimpleName();

	// Used as a key when putting data into bundles and intents, dialog tag can come in handy for classes using this dialog
	public static final String REMOVE_SMS_NUMBER = "removeSmsNumber";
	public static final String REMOVE_SMS_NUMBER_DIALOG_TAG = "removeSmsNumberDialog";

	// Request codes used for this dialog
	public static final int REMOVE_PRIMARY_SMS_NUMBER_DIALOG_REQUEST_CODE = 3;
	public static final int REMOVE_SECONDARY_SMS_NUMBER_DIALOG_REQUEST_CODE = 4;

	// For logging
	private LogHandler logger = LogHandler.getInstance();

	// Must have application context
	private Context context;

	// SMS number to be removed
	private String number = "";

	/**
	 * To create a new instance of {@link RemoveSmsNumberDialog}.
	 */
	public RemoveSmsNumberDialog() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":RemoveSmsNumberDialog()", "Creating a new Remove Sms Number dialog fragment");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate()", "Setting Context to dialog fragment");

		// Set context here, it's safe because this dialog fragment has been attached to it's container, hence we have access to context
		context = getActivity();

		// Must get the SMS number from bundle for two reasons:
		// 1. For user experience, SMS number to be removed will be shown in the dialog
		// 2. To avoid some weird "this flagged string will be removed condition" in caller class, SMS number passed over from this dialog will be
		// handled by calling class and it's removal logic
		Bundle arguments = getArguments();
		number = arguments.getString(REMOVE_SMS_NUMBER);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreateDialog()", "Creating and initializing dialog fragment");

		// Need to resolve correct message in dialog depending on request code
		String message = "";
		switch (getTargetRequestCode()) {
			case (REMOVE_PRIMARY_SMS_NUMBER_DIALOG_REQUEST_CODE):
				message = getString(R.string.DELETE_PRIMARY_NUMBER_PROMPT_MESSAGE) + " " + number + "?";
				break;
			case (REMOVE_SECONDARY_SMS_NUMBER_DIALOG_REQUEST_CODE):
				message = getString(R.string.DELETE_SECONDARY_NUMBER_PROMPT_MESSAGE) + " " + number + "?";
				break;
			default:
				logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":onCreateDialog()", "Cannot resolve dialog message due to an unsupported request code: \"" + getTargetRequestCode() + "\"");
		}

		// Setup the dialog with correct resources, listeners and values
		// @formatter:off
		return new AlertDialog.Builder(context)
				.setIcon(android.R.drawable.ic_dialog_alert)	// Set icon
				.setTitle(R.string.DELETE_NUMBER_PROMPT_TITLE)	// Set title
				.setMessage(message)							// Set resolved message
				// @formatter:on

				.setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreateDialog.PositiveButton.OnClickListener().onClick()", "Positive Button pressed");

						// Create an intent and put data from this dialogs number string and associate it with a certain key
						Intent intent = new Intent();
						intent.putExtra(REMOVE_SMS_NUMBER, number);

						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreateDialog().PositiveButton.OnClickListener().onClick()", "Intent created with extra, key: \"" + REMOVE_SMS_NUMBER + "\" and data: \"" + number + "\"");

						// Make a call to this dialog fragments owning fragments onAcitivityResult with correct request code, result code and intent
						getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
					}
				})

				.setNegativeButton(R.string.NO, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreateDialog().NegativeButton.OnClickListener().onClick()", "Negative Button pressed");
						getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, null);
					}
				})

				.create();
	}
}
