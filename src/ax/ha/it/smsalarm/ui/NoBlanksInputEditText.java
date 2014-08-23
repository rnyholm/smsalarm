/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.ui;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import ax.ha.it.smsalarm.handler.LogHandler;
import ax.ha.it.smsalarm.handler.LogHandler.LogPriorities;

/**
 * An <code>EditText</code> not allowing blankspaces within it.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class NoBlanksInputEditText extends EditText {
	// Log tag string
	private final String LOG_TAG = getClass().getSimpleName();

	// Get LogHandler instance
	LogHandler logger = LogHandler.getInstance();

	public NoBlanksInputEditText(Context context) {
		super(context);

		// Logging in debug purpose
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":NoBlanksInputEditText()", "A new \"NoBlanksInputEditText\" has been created with following context: \"" + context + "\"");

		// To listen on text changes
		addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// Do nothing here!
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// Do nothing here!
			}

			@Override
			public void afterTextChanged(Editable editable) {
				// Store input from edittext as a string stripped from whitespace
				String result = editable.toString().replaceAll(" ", "");

				// If input from edittext and result don't have the same length whitespace has been
				// stripped, we need to set text to edittext and move cursor to correct position
				if (!editable.toString().equals(result)) {
					setText(result);
					setSelection(result.length());
				}
			}
		});
	}
}