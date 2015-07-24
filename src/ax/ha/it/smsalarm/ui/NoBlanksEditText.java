/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.ui;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * A {@link EditText} <b><i>Not</i></b> allowing blankspaces within it.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class NoBlanksEditText extends CursorAdjustingEditText {

	/**
	 * Creates a new instance of {@link NoBlanksEditText} with given context.
	 * 
	 * @param context
	 *            The Context in which this implementation of <code>EditText</code> will operate.
	 */
	public NoBlanksEditText(Context context) {
		super(context);

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
				// Store input from EditText as a string stripped from whitespace
				String result = editable.toString().replaceAll(" ", "");

				// If input from EditText and result don't have the same length whitespace has been
				// stripped, we need to set text to EditText and move cursor to correct position
				if (!editable.toString().equals(result)) {
					setText(result);
					setSelection(result.length());
				}
			}
		});
	}
}