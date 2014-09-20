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
import ax.ha.it.smsalarm.handler.PreferencesHandler;

public class RescueServiceDialog extends DialogFragment {
	private static final String LOG_TAG = RescueServiceDialog.class.getSimpleName();

	public static final String RESCUE_SERVICE = "rescueService";

	private LogHandler logger = LogHandler.getInstance();
	private PreferencesHandler prefHandler = PreferencesHandler.getInstance();

	private Context context;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = getActivity();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		// Setup the EditText
		// @formatter:off
		final EditText inputEditText = new EditText(context);
		inputEditText.setHint(R.string.RESCUE_SERVICE_NAME_HINT); 	// Set hint to EditText
		inputEditText.setInputType(InputType.TYPE_CLASS_TEXT);		// Set input type to EditText
		// @formatter:on

		// Setup the dialog with correct resources, listeners and values
		// @formatter:off
		return new AlertDialog.Builder(context)
				.setIcon(android.R.drawable.ic_dialog_info) 		// Set icon
				.setTitle(R.string.RESCUE_SERVICE_PROMPT_TITLE) 	// Set title
				.setMessage(R.string.RESCUE_SERVICE_PROMPT_MESSAGE) // Set message
				.setCancelable(false)								// Set dialog to non cancelable
				.setView(inputEditText) 							// Bind dialog to EditText
				// @formatter:on

				.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createRescueServiceInputDialog().PositiveButton.OnClickListener().onClick()", "Positive Button pressed");

						Intent intent = new Intent();
						intent.putExtra(RESCUE_SERVICE, inputEditText.getText().toString());

						getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);

//						// Get the rescue service from the EditText
//						rescueService = inputEditText.getText().toString();
//
//						try {
//							// Store to shared preferences
//							prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.RESCUE_SERVICE_KEY, rescueService, context);
//						} catch (IllegalArgumentException e) {
//							logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":createRescueServiceInputDialog().PositiveButton.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
//						}
//
//						// Update affected UI widgets
//						updateRescueServiceEditText();
//						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createRescueServiceInputDialog().PositiveButton.OnClickListener().onClick()", "New RESCUESERVICE name has been stored from user input . New RESCUESERVICE name is: \"" + rescueService + "\"");
					}
				})

				.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						// DO NOTHING, except logging
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createRescueServiceInputDialog().NeutralButton.OnClickListener().onClick()", "Neutral Button pressed in dialog, nothing done");
						getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, null);
					}
				}).create();

//		// Build up the alert dialog
//		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
//		final EditText inputEditText = new EditText(context);
//
//		// @formatter:off
//		// Configure dialog and EditText
//		dialog.setIcon(android.R.drawable.ic_dialog_info);			// Set icon
//		dialog.setTitle(R.string.RESCUE_SERVICE_PROMPT_TITLE);		// Set title
//		dialog.setMessage(R.string.RESCUE_SERVICE_PROMPT_MESSAGE);	// Set message
//		dialog.setCancelable(false);								// Set dialog to non cancelable
//		dialog.setView(inputEditText);								// Bind dialog to EditText
//		inputEditText.setHint(R.string.RESCUE_SERVICE_NAME_HINT);	// Set hint to EditText
//		inputEditText.setInputType(InputType.TYPE_CLASS_TEXT);		// Set input type to EditText
//		// @formatter:on
//
//		// Set a positive button and listen on it
//		dialog.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int whichButton) {
//				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createRescueServiceInputDialog().PositiveButton.OnClickListener().onClick()", "Positive Button pressed");
//
//				// Get the rescue service from the EditText
//				rescueService = inputEditText.getText().toString();
//
//				try {
//					// Store to shared preferences
//					prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.RESCUE_SERVICE_KEY, rescueService, context);
//				} catch (IllegalArgumentException e) {
//					logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":createRescueServiceInputDialog().PositiveButton.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
//				}
//
//				// Update affected UI widgets
//				updateRescueServiceEditText();
//				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createRescueServiceInputDialog().PositiveButton.OnClickListener().onClick()", "New RESCUESERVICE name has been stored from user input . New RESCUESERVICE name is: \"" + rescueService + "\"");
//			}
//		});
//
//		// Set a neutral button, due to documentation it has same functionality as "back" button
//		dialog.setNeutralButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int whichButton) {
//				// DO NOTHING, except logging
//				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createRescueServiceInputDialog().NeutralButton.OnClickListener().onClick()", "Neutral Button pressed in dialog, nothing done");
//			}
//		});
//
//		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createRescueServiceInputDialog()", "Showing dialog");
//
//		// Show it
//		dialog.show();
	}
}
