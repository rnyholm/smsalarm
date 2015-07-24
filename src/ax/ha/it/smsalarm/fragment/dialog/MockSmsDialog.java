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
 * {@link DialogFragment} which let's the user mock a SMS (both sender and message) for testing purpose.
 *
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 * @see #SMS_SENDER
 * @see #SMS_BODY
 * @see #MOCK_SMS_DIALOG_TAG
 * @see #MOCK_SMS_DIALOG_REQUEST_CODE
 */
public class MockSmsDialog extends DialogFragment {
	// Used as a key when putting data into bundles and intents, dialog tag can come in handy for classes using this dialog
	public static final String SMS_SENDER = "smsSender";
	public static final String SMS_BODY = "smsBody";
	public static final String MOCK_SMS_DIALOG_TAG = "mockSmsDialog";

	// Request code used for this dialog
	public static final int MOCK_SMS_DIALOG_REQUEST_CODE = 13;

	// Must have application context
	private Context context;

	// Must be declared as class variable as it will be used when handling instance states
	private EditText smsSenderEditText;
	private EditText smsBodyEditText;

	/**
	 * To create a new instance of {@link MockSmsDialog}.
	 */
	public MockSmsDialog() {
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
		// A TextView to keep track on SMS length, initial value of 0
		final TextView charCountTextView = new TextView(context);
		charCountTextView.setText("0");

		// Another TextView is needed to display the maximum numbers of characters, so the end result of both TextViews will look like 53/160
		TextView maxCharsTextView = new TextView(context);
		maxCharsTextView.setText("/" + Util.SINGLE_SMS_MAX_CHARACTERS);

		// Need to define input filters, and it will contain one filter which only allows maximum 160 characters
		InputFilter[] filters = new InputFilter[1];
		filters[0] = new InputFilter.LengthFilter(Util.SINGLE_SMS_MAX_CHARACTERS);

		// Setup the EditTexts
		// @formatter:off
		smsSenderEditText = new EditText(context);
		smsSenderEditText.setHint(R.string.DEBUG_MOCK_SMS_SENDER_DIALOG_HINT); 	// Set hint to EditText
		smsSenderEditText.setInputType(InputType.TYPE_CLASS_TEXT);		 	// Set input type to EditText
		
		smsBodyEditText = new EditText(context);
		smsBodyEditText.setHint(R.string.DEBUG_MOCK_SMS_BODY_DIALOG_HINT);
		smsBodyEditText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		smsBodyEditText.setMinLines(4); 																			// Set minimum lines
		smsBodyEditText.setLines(4); 	
		smsBodyEditText.setFilters(filters);																		// Set filter, only allowing max 160 characters in the EditText// Set lines
		smsBodyEditText.setGravity(Gravity.TOP | Gravity.LEFT); 												 	// Set gravity to top/left in order to get the caret at the beginning
		smsBodyEditText.setHorizontallyScrolling(false);															// Don't want to have horizontal scrolling
		smsBodyEditText.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)); 	// Set layout parameters for correct wrapping and matching of content
		// @formatter:on

		// Must add a text changed listener in order to update the current number of characters TextView
		smsBodyEditText.addTextChangedListener(new TextWatcher() {
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
				charCountTextView.setText(String.valueOf(smsBodyEditText.getText().toString().length()));
			}
		});

		// If not null, the fragment is being re-created, get data from saved instance, if exist.
		// If saved instance doesn't contain certain key or it's associated value the EditText field will be empty
		if (savedInstanceState != null) {
			// Check if we got any data in saved instance associated with certain key
			if (savedInstanceState.getCharSequence(SMS_SENDER) != null) {
				smsSenderEditText.setText(savedInstanceState.getCharSequence(SMS_SENDER).toString());
			}

			if (savedInstanceState.getCharSequence(SMS_BODY) != null) {
				smsBodyEditText.setText(savedInstanceState.getCharSequence(SMS_BODY).toString());

				// Also update the TextView showing number of characters
				charCountTextView.setText(String.valueOf(smsBodyEditText.getText().toString().length()));
			}
		}

		// Build up the layout for the dialog
		TextView smsSenderTextView = new TextView(context);
		TextView smsBodyTextView = new TextView(context);

		smsSenderTextView.setText(R.string.DEBUG_MOCK_SMS_SENDER_DIALOG_TITLE);
		smsBodyTextView.setText(R.string.DEBUG_MOCK_SMS_BODY_DIALOG_TITLE);

		// Need a layout for the TextViews displaying the current number of characters and the maximum numbers of characters
		LinearLayout charCountLayout = new LinearLayout(context);
		charCountLayout.setOrientation(LinearLayout.HORIZONTAL);
		charCountLayout.setGravity(Gravity.RIGHT);
		charCountLayout.addView(charCountTextView);
		charCountLayout.addView(maxCharsTextView);

		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(5, 5, 5, 5);
		layout.addView(smsSenderTextView);
		layout.addView(smsSenderEditText);
		layout.addView(smsBodyTextView);
		layout.addView(smsBodyEditText);
		layout.addView(charCountLayout);

		// Setup the dialog with correct resources, listeners and values
		// @formatter:off
		return new AlertDialog.Builder(context)
				.setIcon(android.R.drawable.ic_dialog_info) 			// Set icon
				.setTitle(R.string.DEBUG_DISPATCH_MOCK_SMS_DIALOG_TITLE) 		// Set title
				.setView(layout) 										// Bind dialog to Layout
				// @formatter:on

				.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						// Create an intent and put data from this dialogs EditTexts and associate it with a certain key
						Intent intent = new Intent();
						intent.putExtra(SMS_SENDER, smsSenderEditText.getText().toString());
						intent.putExtra(SMS_BODY, smsBodyEditText.getText().toString());

						// Make a call to this dialog fragments owning fragments onAcitivityResult with correct request code, result code and intent
						getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
					}
				})

				.setNegativeButton(R.string.DEBUG_CANCEL, new DialogInterface.OnClickListener() {
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
		arg0.putCharSequence(SMS_SENDER, smsSenderEditText.getText().toString());
		arg0.putCharSequence(SMS_BODY, smsBodyEditText.getText().toString());
	}
}
