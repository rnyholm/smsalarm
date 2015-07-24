/**
 * Copyright (c) 2015 Robert Nyholm. All rights reserved.
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
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.activity.SmsAlarm;
import ax.ha.it.smsalarm.ui.NoBlanksEditText;
import ax.ha.it.smsalarm.util.InitializableString;

/**
 * {@link DialogFragment} which let's the user edit a <b><i>Phone Number</i></b> in the list of <b><i>Primary or Secondary Alarm Trigger Phone
 * Numbers</i></b>, this depends on which <b><i>RequestCode</i></b> this <code>EditSmsNumberDialog</code> is set to.
 *
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 * @see AddSmsNumberDialog
 * @see RemoveSmsNumberDialog
 * @see #EDIT_SMS_NUMBER
 * @see #EDIT_SMS_NUMBER_DIALOG_TAG
 * @see #EDIT_PRIMARY_SMS_NUMBER_DIALOG_REQUEST_CODE
 * @see #EDIT_SECONDARY_SMS_NUMBER_DIALOG_REQUEST_CODE
 */
public class EditSmsNumberDialog extends DialogFragment {
	public static final String LOG_TAG = EditSmsNumberDialog.class.getSimpleName();

	// Used as a key when putting data into bundles and intents, dialog tag can come in handy for classes using this dialog
	public static final String EDIT_SMS_NUMBER = "addSmsNumber";
	public static final String EDIT_SMS_NUMBER_DIALOG_TAG = "addSmsNumberDialog";

	// Request codes used for this dialog
	public static final int EDIT_PRIMARY_SMS_NUMBER_DIALOG_REQUEST_CODE = 23;
	public static final int EDIT_SECONDARY_SMS_NUMBER_DIALOG_REQUEST_CODE = 24;

	// Must have application context
	private Context context;

	// Must be declared as a class variable as it will be used when handling instance states
	private NoBlanksEditText inputEditText;

	// Need an initializable string to keep track of the phone number to be replaced and the new one
	private InitializableString initializableString;

	/**
	 * Creates and returns a new instance of {@link EditSmsNumberDialog}, with given {@link InitializableString} in it. A
	 * <code>InitializableString</code> is needed as it provides a way to retrieve the initial value (phone number), this value is needed to be able
	 * to know what string that should be replaced.
	 * 
	 * @param initializableString
	 *            The value from this object will be placed within the dialogs {@link EditText} upon creation.
	 * @return New instance of <code>EditSmsNumberDialog</code> prepared with given <code>InitializableString</code> as argument.
	 */
	public static EditSmsNumberDialog newInstance(InitializableString initializableString) {
		EditSmsNumberDialog dialogFragment = new EditSmsNumberDialog();
		Bundle args = new Bundle();
		args.putParcelable(EDIT_SMS_NUMBER, initializableString);
		dialogFragment.setArguments(args);
		return dialogFragment;
	}

	/**
	 * Creates a new instance of {@link EditSmsNumberDialog}.
	 */
	public EditSmsNumberDialog() {
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
		// Setup the EditText
		// @formatter:off
		inputEditText = new NoBlanksEditText(context);
		inputEditText.setHint(R.string.PHONE_NUMBER_DIALOG_HINT);	// Set hint to EditText
		inputEditText.setInputType(InputType.TYPE_CLASS_TEXT);		// Set input type to EditText
		// @formatter:on

		// If saved instance state isn't null, try to get the number from them, if they exists. Else try to get the number from arguments as they can
		// be set upon creation of this dialog. In other cases the edit text field showing the number will be empty
		if (savedInstanceState != null) {
			// Check if we got any data in saved instance associated with certain key or if we got any arguments
			if (savedInstanceState.getParcelable(EDIT_SMS_NUMBER) != null) {
				// Get the string container and set the text
				initializableString = savedInstanceState.getParcelable(EDIT_SMS_NUMBER);
				inputEditText.setText(initializableString.getValue());
			}
		} else if (getArguments() != null) {
			if (getArguments().getParcelable(EDIT_SMS_NUMBER) != null) {
				initializableString = getArguments().getParcelable(EDIT_SMS_NUMBER);
				inputEditText.setText(initializableString.getValue());
			}
		}

		// Need to resolve correct message in dialog depending on request code
		String message = "";
		switch (getTargetRequestCode()) {
			case (EDIT_PRIMARY_SMS_NUMBER_DIALOG_REQUEST_CODE):
				message = getString(R.string.EDIT_PRIMARY_PHONE_NUMBER_DIALOG_MESSAGE);
				break;
			case (EDIT_SECONDARY_SMS_NUMBER_DIALOG_REQUEST_CODE):
				message = getString(R.string.EDIT_SECONDARY_PHONE_NUMBER_DIALOG_MESSAGE);
				break;
			default:
				if (SmsAlarm.DEBUG) {
					Log.e(LOG_TAG + ":onCreateDialog()", "Cannot resolve dialog message due to an unsupported request code: \"" + getTargetRequestCode() + "\"");
				}
		}

		// Setup the dialog with correct resources, listeners and values
		// @formatter:off
		return new AlertDialog.Builder(context)
				.setIcon(android.R.drawable.ic_dialog_info)			// Set icon
				.setTitle(R.string.EDIT_PHONE_NUMBER_DIALOG_TITLE)	// Set title
				.setMessage(message)								// Set resolved message
				.setView(inputEditText)								// Bind dialog to input
				// @formatter:on

				.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						// Store value from edit text to the String container
						initializableString.setValue(inputEditText.getText().toString());

						// Create an intent and put data from this dialogs EditText and associate it with a certain key
						Intent intent = new Intent();
						intent.putExtra(EDIT_SMS_NUMBER, initializableString);

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

	@Override
	public void onSaveInstanceState(Bundle arg0) {
		super.onSaveInstanceState(arg0);

		// Store value from edit text to String container then put the Parcelable
		initializableString.setValue(inputEditText.getText().toString());
		arg0.putParcelable(EDIT_SMS_NUMBER, initializableString);
	}
}
