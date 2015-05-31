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
import android.text.InputType;
import android.widget.EditText;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.ui.NoBlanksEditText;

/**
 * {@link DialogFragment} which let's the user add or remove the <b><i>Acknowledge Phone Number</i></b>.
 *
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 * @see #ACKNOWLEDGE_NUMBER
 * @see #ACKNOWLEDGE_NUMBER_DIALOG_TAG
 * @see #ACKNOWLEDGE_NUMBER_DIALOG_REQUEST_CODE
 */
public class AcknowledgeNumberDialog extends DialogFragment {
	// Used as a key when putting data into bundles and intents, dialog tag can come in handy for classes using this dialog
	public static final String ACKNOWLEDGE_NUMBER = "acknowledgeNumber";
	public static final String ACKNOWLEDGE_NUMBER_DIALOG_TAG = "acknowledgeNumberDialog";

	// Request code used for this dialog
	public static final int ACKNOWLEDGE_NUMBER_DIALOG_REQUEST_CODE = 11;

	// Must have application context
	private Context context;

	// Must be declared as class variable as it will be used when handling instance states
	private NoBlanksEditText inputEditText;

	/**
	 * Creates and returns a new instance of {@link AcknowledgeNumberDialog}, with given acknowledge number in it.
	 * 
	 * @param acknowledgeNumber
	 *            Acknowledge number to be placed within this dialogs {@link EditText} upon creation.
	 * @return New instance of <code>AcknowledgeNumberDialog</code> prepared with given acknowledge number as argument.
	 */
	public static AcknowledgeNumberDialog newInstance(String acknowledgeNumber) {
		AcknowledgeNumberDialog dialogFragment = new AcknowledgeNumberDialog();
		Bundle args = new Bundle();
		args.putString(ACKNOWLEDGE_NUMBER, acknowledgeNumber);
		dialogFragment.setArguments(args);
		return dialogFragment;
	}

	/**
	 * To create a new instance of {@link AcknowledgeNumberDialog}.
	 */
	public AcknowledgeNumberDialog() {
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
		inputEditText.setHint(R.string.NUMBER_PROMPT_HINT);		// Set hint to EditText
		inputEditText.setInputType(InputType.TYPE_CLASS_PHONE);	// Set input type to EditText
		// @formatter:on

		// If saved instance state isn't null, try to get the number from them, if they exists. Else try to get the number from arguments as they can
		// be set upon creation of this dialog. In other cases the edit text field showing the number will be empty
		if (savedInstanceState != null) {
			// Check if we got any data in saved instance associated with certain key or if we got any arguments
			if (savedInstanceState.getCharSequence(ACKNOWLEDGE_NUMBER) != null) {
				setTextAndChangeSelection(savedInstanceState.getCharSequence(ACKNOWLEDGE_NUMBER).toString());
			}
		} else if (getArguments() != null) {
			if (getArguments().getString(ACKNOWLEDGE_NUMBER) != null) {
				setTextAndChangeSelection(getArguments().getString(ACKNOWLEDGE_NUMBER));
			}
		}

		// Setup the dialog with correct resources, listeners and values
		// @formatter:off
		return new AlertDialog.Builder(context)
				.setIcon(android.R.drawable.ic_dialog_info) 	// Set icon
				.setTitle(R.string.NUMBER_PROMPT_TITLE) 		// Set title
				.setMessage(R.string.ACK_NUMBER_PROMPT_MESSAGE) // Set message
				.setView(inputEditText) 						// Bind dialog to EditText
				// @formatter:on

				.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						// Create an intent and put data from this dialogs EditText and associate it with a certain key
						Intent intent = new Intent();
						intent.putExtra(ACKNOWLEDGE_NUMBER, inputEditText.getText().toString());

						// Make a call to this dialog fragments owning fragments onActivityResult with correct request code, result code and intent
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
		arg0.putCharSequence(ACKNOWLEDGE_NUMBER, inputEditText.getText().toString());
	}

	/**
	 * Convenience method to set a text to this {@link AcknowledgeNumberDialog}'s {@link EditText} for input. This method will also moves the cursor
	 * to the end of the text in the <code>EditText</code>.
	 * 
	 * @param text
	 *            Text To be placed in the <code>EditText</code> within this dialog.
	 */
	private void setTextAndChangeSelection(String text) {
		inputEditText.setText(text);
		inputEditText.setSelection(inputEditText.length());
	}
}
