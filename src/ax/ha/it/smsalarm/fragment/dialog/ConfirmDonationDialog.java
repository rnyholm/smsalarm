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
import ax.ha.it.smsalarm.R;

/**
 * {@link DialogFragment} which acts as a confirmation that the user really want's to make a donation of given value.
 *
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 * @see #CONFIRM_DONATION
 * @see #CONFIRM_DONATION_DIALOG_TAG
 * @see #CONFIRM_DONATION_DIALOG_REQUEST_CODE
 */
public class ConfirmDonationDialog extends DialogFragment {
	// Used as a key when putting data into bundles and intents, dialog tag can come in handy for classes using this dialog
	public static final String CONFIRM_DONATION = "confirmDonation";
	public static final String CONFIRM_DONATION_DIALOG_TAG = "confirmDonationDialog";

	// Request code used for this dialog
	public static final int CONFIRM_DONATION_DIALOG_REQUEST_CODE = 25;

	// Must have application context
	private Context context;

	// Need to have the amount of donation
	private String donationAmount;

	/**
	 * Creates and returns a new instance of {@link ConfirmDonationDialog}, with given <code>donationAmount</code> in it.
	 * 
	 * @param donationAmount
	 *            The donation amount to be placed within the created dialog.
	 * @return New instance of <code>ConfirmDonationDialog</code> prepared with given <code>donationAmount</code> as argument.
	 */
	public static ConfirmDonationDialog newInstance(String donationAmount) {
		ConfirmDonationDialog dialogFragment = new ConfirmDonationDialog();
		Bundle args = new Bundle();
		args.putString(CONFIRM_DONATION, donationAmount);
		dialogFragment.setArguments(args);
		return dialogFragment;
	}

	/**
	 * Creates a new instance of {@link ConfirmDonationDialog}.
	 */
	public ConfirmDonationDialog() {
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
		// Get the donation amount from arguments, it should definitely be there but check to be sure
		if (getArguments() != null && getArguments().getString(CONFIRM_DONATION) != null) {
			donationAmount = getArguments().getString(CONFIRM_DONATION);
		}

		// Setup the dialog with correct resources, listeners and values
		// @formatter:off
		return new AlertDialog.Builder(context)
				.setIcon(android.R.drawable.ic_dialog_info)												// Set icon
				.setTitle(R.string.DONATE_DIALOG_TITLE)													// Set title
				.setMessage(getString(R.string.CONFIRM_DONATION_AMOUNT_DIALOG_MESSAGE, donationAmount))	// Resolve and set message
				// @formatter:on

				.setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						// Create an intent and place confirmed donation amount within it
						Intent intent = new Intent();
						intent.putExtra(CONFIRM_DONATION, donationAmount);

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
