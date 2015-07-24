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
import ax.ha.it.smsalarm.ui.CursorAdjustingEditText;

/**
 * {@link DialogFragment} which let's the user add or remove the <b><i>Organization</i></b>.
 *
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 * @see #ORGANIZATION
 * @see #ORGANIZATION_DIALOG_TAG
 * @see #ORGANIZATION_DIALOG_REQUEST_CODE
 */
public class OrganizationDialog extends DialogFragment {
	// Used as a key when putting data into bundles and intents, dialog tag can come in handy for classes using this dialog
	public static final String ORGANIZATION = "organization";
	public static final String ORGANIZATION_DIALOG_TAG = "organizationDialog";

	// Request code used for this dialog
	public static final int ORGANIZATION_DIALOG_REQUEST_CODE = 12;

	// Must have application context
	private Context context;

	// Must be declared as class variable as it will be used when handling instance states
	private CursorAdjustingEditText inputEditText;

	/**
	 * Creates and returns a new instance of {@link OrganizationDialog}, with given organization in it.
	 * 
	 * @param organization
	 *            Organization to be placed within this dialogs {@link EditText} upon creation.
	 * @return New instance of <code>OrganizationDialog</code> prepared with given organization as argument.
	 */
	public static OrganizationDialog newInstance(String organization) {
		OrganizationDialog dialogFragment = new OrganizationDialog();
		Bundle args = new Bundle();
		args.putString(ORGANIZATION, organization);
		dialogFragment.setArguments(args);
		return dialogFragment;
	}

	/**
	 * To create a new instance of {@link OrganizationDialog}.
	 */
	public OrganizationDialog() {
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
		inputEditText = new CursorAdjustingEditText(context);
		inputEditText.setHint(R.string.ORGANIZATION_DIALOG_HINT); 	// Set hint to EditText
		inputEditText.setInputType(InputType.TYPE_CLASS_TEXT);		// Set input type to EditText
		// @formatter:on

		// If saved instance state isn't null, try to get the organization name from them, if they exists. Else try to get the organization name from
		// arguments as they can be set upon creation of this dialog. In other cases the edit text field showing the organization name will be empty
		if (savedInstanceState != null) {
			// Check if we got any data in saved instance associated with certain key or if we got any arguments
			if (savedInstanceState.getCharSequence(ORGANIZATION) != null) {
				inputEditText.setText(savedInstanceState.getCharSequence(ORGANIZATION).toString());
			}
		} else if (getArguments() != null) {
			if (getArguments().getString(ORGANIZATION) != null) {
				inputEditText.setText(getArguments().getString(ORGANIZATION));
			}
		}

		// Setup the dialog with correct resources, listeners and values
		// @formatter:off
		return new AlertDialog.Builder(context)
				.setIcon(android.R.drawable.ic_dialog_info) 		// Set icon
				.setTitle(R.string.ORGANIZATION_DIALOG_TITLE) 		// Set title
				.setMessage(R.string.ORGANIZATION_DIALOG_MESSAGE) 	// Set message
				.setView(inputEditText) 							// Bind dialog to EditText
				// @formatter:on

				.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						// Create an intent and put data from this dialogs EditText and associate it with a certain key
						Intent intent = new Intent();
						intent.putExtra(ORGANIZATION, inputEditText.getText().toString());

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
		arg0.putCharSequence(ORGANIZATION, inputEditText.getText().toString());
	}
}
