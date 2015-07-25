/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.fragment.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import ax.ha.it.smsalarm.R;

/**
 * {@link DialogFragment} which let's the user remove a <b><i>Phone Number</i></b> from the list of <b><i>Primary or Secondary Alarm Trigger Phone
 * Numbers</i></b>, this depends on which <b><i>RequestCode</i></b> this <code>RemoveSmsNumberDialog</code> is set to.<br>
 *
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 * @see AddSmsNumberDialog
 * @see EditSmsNumberDialog
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

	// Must have application context
	private Context context;

	// SMS number to be removed
	private String phoneNumber = "";

	/**
	 * Creates and returns a new instance of {@link RemoveSmsNumberDialog}, with given phone number in it.
	 * 
	 * @param phoneNumber
	 *            Phone number to be placed within this dialog upon creation.
	 * @return New instance of <code>RemoveSmsNumberDialog</code> prepared with given phone number as argument.
	 */
	public static RemoveSmsNumberDialog newInstance(String phoneNumber) {
		RemoveSmsNumberDialog dialogFragment = new RemoveSmsNumberDialog();
		Bundle args = new Bundle();
		args.putString(REMOVE_SMS_NUMBER, phoneNumber);
		dialogFragment.setArguments(args);
		return dialogFragment;
	}

	/**
	 * To create a new instance of {@link RemoveSmsNumberDialog}.
	 */
	public RemoveSmsNumberDialog() {
		// Just empty
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set context here, it's safe because this dialog fragment has been attached to it's container, hence we have access to context
		context = getActivity();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Get the phone number from the arguments, it should definitely be there but check to be sure
		if (getArguments() != null && getArguments().getString(REMOVE_SMS_NUMBER) != null) {
			phoneNumber = getArguments().getString(REMOVE_SMS_NUMBER);
		}

		// Need to resolve correct message in dialog depending on request code
		String message = "";
		switch (getTargetRequestCode()) {
			case (REMOVE_PRIMARY_SMS_NUMBER_DIALOG_REQUEST_CODE):
				message = getString(R.string.REMOVE_PRIMARY_PHONE_NUMBER_DIALOG_MESSAGE) + " " + phoneNumber + "?";
				break;
			case (REMOVE_SECONDARY_SMS_NUMBER_DIALOG_REQUEST_CODE):
				message = getString(R.string.REMOVE_SECONDARY_PHONE_NUMBER_DIALOG_MESSAGE) + " " + phoneNumber + "?";
				break;
			default:
				Log.e(LOG_TAG + ":onCreateDialog()", "Cannot resolve dialog message due to an unsupported request code: \"" + getTargetRequestCode() + "\"");
		}

		// Setup the dialog with correct resources, listeners and values
		// @formatter:off
		return new AlertDialog.Builder(context)
				.setIcon(android.R.drawable.ic_dialog_alert)			// Set icon
				.setTitle(R.string.REMOVE_PHONE_NUMBER_DIALOG_TITLE)	// Set title
				.setMessage(message)									// Set resolved message
				// @formatter:on

				.setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						// Create an intent and put data from this dialogs number string and associate it with a certain key
						Intent intent = new Intent();
						intent.putExtra(REMOVE_SMS_NUMBER, phoneNumber);

						// Make a call to this dialog fragments owning fragments onAcitivityResult with correct request code, result code and intent
						getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
					}
				})

				.setNegativeButton(R.string.NO, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, null);
					}
				})

				.create();
	}
}
