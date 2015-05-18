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
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.util.Util;

/**
 * {@link DialogFragment} which let's the user add or remove an <b><i>Acknowledge Message</i></b>.
 *
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 * @see #ACKNOWLEDGE_MESSAGE
 * @see #ACKNOWLEDGE_MESSAGE_DIALOG_TAG
 * @see #ACKNOWLEDGE_MESSAGE_DIALOG_REQUEST_CODE
 */
public class AcknowledgeMessageDialog extends DialogFragment {
	// Used as a key when putting data into bundles and intents, dialog tag can come in handy for classes using this dialog
	public static final String ACKNOWLEDGE_MESSAGE = "acknowledgeMessage";
	public static final String ACKNOWLEDGE_MESSAGE_DIALOG_TAG = "acknowledgeMessageDialog";

	// Request code used for this dialog
	public static final int ACKNOWLEDGE_MESSAGE_DIALOG_REQUEST_CODE = 21;

	// Must have application context
	private Context context;

	// Must be declared as class variable as it will be used when handling instance states
	private EditText inputEditText;

	/**
	 * To create a new instance of {@link AcknowledgeMessageDialog}
	 */
	public AcknowledgeMessageDialog() {
		// Just empty..
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set context here, it's safe because this dialog fragment has been attached to it's container, hence we have access to context
		context = getActivity();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// A TextView to keep track on SMS length, initial value of 0
		final TextView charCountTextView = new TextView(context);
		charCountTextView.setText("0");

		// Another TextView is needed to display the maximum numbers of characters, so the end result of both TextViews will look like 53/160
		TextView maxCharsTextView = new TextView(context);
		maxCharsTextView.setText("/" + Util.SINGLE_SMS_MAX_CHARACTERS);

		// Need to define input filters, and it will contain one filter which only allows maximum 160 characters
		InputFilter[] filters = new InputFilter[1];
		filters[0] = new InputFilter.LengthFilter(Util.SINGLE_SMS_MAX_CHARACTERS);

		// Setup the EditText
		// @formatter:off
		inputEditText = new EditText(context);
		inputEditText.setHint(R.string.MESSAGE_PROMPT_HINT);														// Set hint to EditText
		inputEditText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);	// Set input type to EditText
		inputEditText.setMinLines(4); 																				// Set minimum lines
		inputEditText.setLines(4); 																				// Set lines
		inputEditText.setFilters(filters);																		// Set filter, only allowing max 160 characters in the EditText
		inputEditText.setGravity(Gravity.TOP | Gravity.LEFT); 												 	// Set gravity to top/left in order to get the caret at the beginning
		inputEditText.setHorizontallyScrolling(false);															// Don't want to have horizontal scrolling
		inputEditText.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)); 	// Set layout parameters for correct wrapping and matching of content
		// @formatter:on

		// Must add a text changed listener in order to update the current number of characters TextView
		inputEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// DO NOTHING!
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// DO NOTHING!
			}

			@Override
			public void afterTextChanged(Editable s) {
				// Just update the TextView showing number of characters with the length of text in EditText field
				charCountTextView.setText(String.valueOf(inputEditText.getText().toString().length()));
			}
		});

		// If not null, the fragment is being re-created, get data from saved instance, if exist.
		// If saved instance doesn't contain certain key or it's associated value the EditText field will be empty
		if (savedInstanceState != null) {
			// Check if we got any data in saved instance associated with certain key
			if (savedInstanceState.getCharSequence(ACKNOWLEDGE_MESSAGE) != null) {
				inputEditText.setText(savedInstanceState.getCharSequence(ACKNOWLEDGE_MESSAGE).toString());

				// Place cursor at the end of the text within the EditText
				inputEditText.setSelection(inputEditText.length());

				// Also update the TextView showing number of characters
				charCountTextView.setText(String.valueOf(inputEditText.getText().toString().length()));
			}
		}

		// Need a layout for the TextViews displaying the current number of characters and the maximum numbers of characters
		LinearLayout charCountLayout = new LinearLayout(context);
		charCountLayout.setOrientation(LinearLayout.HORIZONTAL);
		charCountLayout.setGravity(Gravity.RIGHT);
		charCountLayout.addView(charCountTextView);
		charCountLayout.addView(maxCharsTextView);

		// Build up the dialogs layout
		LinearLayout dialogLayout = new LinearLayout(context);
		dialogLayout.setOrientation(LinearLayout.VERTICAL);
		dialogLayout.setPadding(5, 5, 5, 5);
		dialogLayout.addView(inputEditText);
		dialogLayout.addView(charCountLayout);

		// Setup the dialog with correct resources, listeners and values
		// @formatter:off
		return new AlertDialog.Builder(context)
				.setIcon(android.R.drawable.ic_dialog_info) 		// Set icon
				.setTitle(R.string.MESSAGE_PROMPT_TITLE) 			// Set title
				.setMessage(R.string.ACK_MESSAGE_PROMPT_MESSAGE) 	// Set message
				.setView(dialogLayout) 								// Bind dialog to built up Layout
				// @formatter:on

				.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						// Create an intent and put data from this dialogs EditText and associate it with a certain key
						Intent intent = new Intent();
						intent.putExtra(ACKNOWLEDGE_MESSAGE, inputEditText.getText().toString());

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
		arg0.putCharSequence(ACKNOWLEDGE_MESSAGE, inputEditText.getText().toString());
	}
}
