/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.fragment.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import ax.ha.it.smsalarm.R;

/**
 * {@link DialogFragment} which asks the user for a confirmation to mock the {@link SharedPreferences}.
 *
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 * @see #CONFIRM_MOCK_SHARED_PREFERENCES_TAG
 * @see #CONFIRM_MOCK_SHARED_PREFERENCES_CODE
 */
public class ConfirmMockSharedPreferencesDialog extends DialogFragment {
	// Dialog tag can come in handy for classes using this dialog
	public static final String CONFIRM_MOCK_SHARED_PREFERENCES_TAG = "confirmMockSharedPreferences";

	// Request code used for this dialog
	public static final int CONFIRM_MOCK_SHARED_PREFERENCES_CODE = 14;

	// Must have application context
	private Context context;

	/**
	 * To create a new instance of {@link ConfirmMockSharedPreferencesDialog}.
	 */
	public ConfirmMockSharedPreferencesDialog() {
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
			.setIcon(android.R.drawable.ic_dialog_alert)						// Set icon
			.setTitle(R.string.DEBUG_CONFIRM_MOCK_SHARED_PREFERENCES_TITLE)		// Set title
			.setMessage(R.string.DEBUG_CONFIRM_MOCK_SHARED_PREFERENCES_MESSAGE)	// Set message
			// @formatter:on

				.setPositiveButton(R.string.DEBUG_YES, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Make a call to this dialog fragments owning fragments onAcitivityResult with correct request code, result code and intent
						getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
					}
				})

				.setNegativeButton(R.string.DEBUG_NO, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, null);
					}
				})

				.create();
	}
}
