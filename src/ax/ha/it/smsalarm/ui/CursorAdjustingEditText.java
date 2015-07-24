/**
 * Copyright (c) 2015 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.ui;

import android.content.Context;
import android.widget.EditText;

/**
 * A {@link EditText} that will automatically adjust(move) the cursor at the end of the text within this {@link CursorAdjustingEditText} after text
 * has been set to it.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class CursorAdjustingEditText extends EditText {

	/**
	 * Creates a new instance of {@link CursorAdjustingEditText} with given context.
	 * 
	 * @param context
	 *            The Context in which this implementation of <code>EditText</code> will operate.
	 */
	public CursorAdjustingEditText(Context context) {
		// Just a super duper call
		super(context);
	}

	/**
	 * Setting text to this {@link CursorAdjustingEditText}. The cursor are moved to the end of the previously set text as well. The text is set using
	 * the {@link EditText#setText(CharSequence)}.
	 * 
	 * @param text
	 *            Text to be set to this object.
	 */
	public void setText(String text) {
		super.setText(text);

		// Set the selection(move cursor to end of this edit text)
		setSelection(length());
	}
}
