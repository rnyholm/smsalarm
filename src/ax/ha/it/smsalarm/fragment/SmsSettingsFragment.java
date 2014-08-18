/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.Util;
import ax.ha.it.smsalarm.enumeration.DialogTypes;
import ax.ha.it.smsalarm.handler.LogHandler;
import ax.ha.it.smsalarm.handler.LogHandler.LogPriorities;
import ax.ha.it.smsalarm.handler.PreferencesHandler;
import ax.ha.it.smsalarm.handler.PreferencesHandler.DataTypes;
import ax.ha.it.smsalarm.handler.PreferencesHandler.PrefKeys;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * <code>Fragment</code> containing all the views and user interface widgets for the <b><i>Sms Settings</i></b>. Fragment does also contain all logic
 * for the widgets.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class SmsSettingsFragment extends SherlockFragment implements SmsAlarmFragment {
	// Log tag string
	private final String LOG_TAG = getClass().getSimpleName();

	// Objects needed for logging and shared preferences handling
	private final LogHandler logger = LogHandler.getInstance();
	private final PreferencesHandler prefHandler = PreferencesHandler.getInstance();

	// Must have the application context
	private Context context;

	// The Button objects
	private Button addPrimarySmsNumberButton;
	private Button removePrimarySmsNumberButton;
	private Button addSecondarySmsNumberButton;
	private Button removeSecondarySmsNumberButton;

	// The Spinner objects
	private Spinner primarySmsNumberSpinner;
	private Spinner secondarySmsNumberSpinner;

	// List of strings containing primary- and secondary sms numbers
	private List<String> primarySmsNumbers = new ArrayList<String>();
	private List<String> secondarySmsNumbers = new ArrayList<String>();
	private final List<String> emptySmsNumbers = new ArrayList<String>(); // <-- A "dummy" list just containing one element, one string

	/**
	 * To create a new <code>SmsSettingsFragment</code> with given context.
	 * 
	 * @param context
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 */
	public SmsSettingsFragment(Context context) {
		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":SmsSettingsFragment()", "Creating a new Sms settings fragment with given context:  \"" + context + "\"");
		this.context = context;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.sms_settings, container, false);

		fetchSharedPrefs();
		findViews(view);
		setListeners();
		updateFragmentView();

		return view;
	}

	@Override
	public void findViews(View view) {
		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "Start finding Views by their ID");

		// Declare and initialize variables of type button
		addPrimarySmsNumberButton = (Button) view.findViewById(R.id.addPrimarySmsNumber_btn);
		removePrimarySmsNumberButton = (Button) view.findViewById(R.id.deletePrimarySmsNumber_btn);
		addSecondarySmsNumberButton = (Button) view.findViewById(R.id.addSecondarySmsNumber_btn);
		removeSecondarySmsNumberButton = (Button) view.findViewById(R.id.deleteSecondarySmsNumber_btn);

		// Declare and initialize variables of type Spinner
		primarySmsNumberSpinner = (Spinner) view.findViewById(R.id.primarySmsNumberSpinner_sp);
		secondarySmsNumberSpinner = (Spinner) view.findViewById(R.id.secondarySmsNumberSpinner_sp);

		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "All Views found");
	}

	@Override
	public void fetchSharedPrefs() {
		// Some logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":fetchSharedPrefs()", "Start retrieving shared preferences needed by this fragment");

		try {
			// Get shared preferences needed by this fragment
			primarySmsNumbers = (List<String>) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_LISTEN_NUMBERS_KEY, DataTypes.LIST, context);
			secondarySmsNumbers = (List<String>) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.SECONDARY_LISTEN_NUMBERS_KEY, DataTypes.LIST, context);
		} catch (IllegalArgumentException e) {
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":fetchSharedPrefs()", "An unsupported datatype was given as argument to PreferencesHandler.getPrefs()", e);
		}

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":fetchSharedPrefs()", "Shared preferences retrieved");
	}

	@Override
	public void updateFragmentView() {
		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateFragmentView()", "Whole fragments user interface is about to be updated");

		// Update primary sms and secondary numbers Spinner
		updatePrimarySmsNumberSpinner();
		updateSecondarySmsNumberSpinner();

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateFragmentView()", "Fragment updated");
	}

	@Override
	public void setListeners() {
		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners()", "Setting listeners to the different user interface widgets");

		// Set listener to addPrimarySmsNumberButton
		addPrimarySmsNumberButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().addPrimarySmsNumberButton.OnClickListener().onClick()", "Add PRIMARY sms number button pressed");
				// Build up and show input dialog of type primary number
				buildAndShowInputDialog(DialogTypes.SMS_PRIMARY);
			}
		});

		// Set listener to removePrimarySmsNumberButton
		removePrimarySmsNumberButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().removePrimarySmsNumberButton.OnClickListener().onClick()", "Remove PRIMARY sms number button pressed");

				// Only show delete dialog if primary sms numbers exists, else show toast
				if (!primarySmsNumbers.isEmpty()) {
					// Show alert dialog(prompt user for deleting number)
					buildAndShowDeleteDialog(DialogTypes.SMS_PRIMARY);
				} else {
					Toast.makeText(context, R.string.NO_PRIMARY_NUMBER_EXISTS, Toast.LENGTH_LONG).show();
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().removePrimarySmsNumberButton.OnClickListener().onClick()", "Cannot build and show dialog because the list of PRIMARY sms numbers are empty");
				}
			}
		});

		// Set listener to addSecondarySmsNumberButton
		addSecondarySmsNumberButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().addSecondarySmsNumberButton.OnClickListener().onClick()", "Add SECONDARY sms number button pressed");
				// Build up and show input dialog of type secondary number
				buildAndShowInputDialog(DialogTypes.SMS_SECONDARY);
			}
		});

		// Set listener to removeSecondarySmsNumberButton
		removeSecondarySmsNumberButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().removeSecondarySmsNumberButton.OnClickListener().onClick()", "Remove SECONDARY sms number button pressed");

				// Only show delete dialog if secondary sms numbers exists, else show toast
				if (!secondarySmsNumbers.isEmpty()) {
					// Show alert dialog(prompt user for deleting number)
					buildAndShowDeleteDialog(DialogTypes.SMS_SECONDARY);
				} else {
					Toast.makeText(context, R.string.NO_SECONDARY_NUMBER_EXISTS, Toast.LENGTH_LONG).show();
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().removeSecondarySmsNumberButton.OnClickListener().onClick()", "Cannot build and show dialog because the list of SECONDARY sms numbers are");
				}
			}
		});
	}

	/**
	 * To build up one of the two input dialogs for the different phone numbers. The supported types are:
	 * <p>
	 * <ul>
	 * <li><b><i>DialogTypes.SMS_PRIMARY</b></i></li>
	 * <li><b><i>DialogTypes.SMS_SECONDARY</b></i></li>
	 * </ul>
	 * <p>
	 * If a dialog type are given as parameter thats not supported a dummy dialog will be built and shown.<br>
	 * This method also stores given data into shared preferences directly.
	 * 
	 * @param type
	 *            Type of dialog to build up and show
	 * @see #buildAndShowDeleteDialog()
	 * @see #updatePrimarySmsNumberSpinner()
	 * @see #updateSecondarySmsNumberSpinner()
	 * @see PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context) setPrefs(PrefKeys, PrefKeys, Object, Context)
	 * @see LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see LogHandler#logCatTxt(LogPriorities, String, String, Throwable) logCatTxt(LogPriorities, String, String,Throwable)
	 * @see DialogTypes
	 */
	private void buildAndShowInputDialog(final DialogTypes type) {
		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog()", "Start building dialog");

		// Build up the alert dialog
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);

		// Set some attributes
		dialog.setIcon(android.R.drawable.ic_dialog_info);

		// Set an EditText view to get user input
		final EditText inputEditText = new EditText(context);
		final EditText noBlanksInputEditText = new EditText(context);

		// Set a textwatcher to the edittext removing any whitespace characters
		noBlanksInputEditText.addTextChangedListener(new TextWatcher() {
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
					noBlanksInputEditText.setText(result);
					noBlanksInputEditText.setSelection(result.length());
				}
			}
		});

		/*
		 * Switch through the different dialog types and set correct strings and edittext to the dialog. If dialog type is non supported a default
		 * dialog DUMMY is built up.
		 */
		switch (type) {
			case SMS_PRIMARY:
				// Set title
				dialog.setTitle(R.string.NUMBER_PROMPT_TITLE);
				// Set message
				dialog.setMessage(R.string.PRIMARY_NUMBER_PROMPT_MESSAGE);
				// Set hint to edittext
				noBlanksInputEditText.setHint(R.string.NUMBER_PROMPT_HINT);
				// Set Input type to edittext
				noBlanksInputEditText.setInputType(InputType.TYPE_CLASS_TEXT);
				// Set dialog to non cancelable
				dialog.setCancelable(false);
				// Bind dialog to input
				dialog.setView(noBlanksInputEditText);
				// Logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog()", "Dialog attributes is set for dialog type SMS_PRIMARY");
				break;
			case SMS_SECONDARY:
				dialog.setTitle(R.string.NUMBER_PROMPT_TITLE);
				dialog.setMessage(R.string.SECONDARY_NUMBER_PROMPT_MESSAGE);
				noBlanksInputEditText.setHint(R.string.NUMBER_PROMPT_HINT);
				noBlanksInputEditText.setInputType(InputType.TYPE_CLASS_TEXT);
				dialog.setCancelable(false);
				dialog.setView(noBlanksInputEditText);
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog()", "Dialog attributes is set for dialog type SMS_SECONDARY");
				break;
			default: // <--Unsupported dialog type. Displaying a dummy dialog!
				dialog.setTitle("Congratulations!");
				dialog.setMessage("Somehow you got this dialog to show up! I bet a monkey must have been messing around with the code;-)");
				dialog.setCancelable(false);
				logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":buildAndShowInputDialog()", "A UNSUPPORTED dialog type has been given as parameter, a DUMMY dialog will be built and shown");
		}

		// Set a positive button and listen on it
		dialog.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
			// To store input from dialogs edittext field
			String input = "";

			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// Log information
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "Positive Button pressed");

				// Boolean indicating if there are duplicates of primary and secondary sms numbers
				// and also of the primary and secondary free texts
				boolean duplicatedNumbers = false;
				boolean duplicatedFreeTexts = false;

				/*
				 * Switch through the different dialog types and set proper input handling to each of them. If dialog type is non supported no input
				 * is taken.
				 */
				switch (type) {
					case SMS_PRIMARY:
						// Store input
						input = noBlanksInputEditText.getText().toString();
						// If input doesn't exist in the list of secondarySmsNumbers and input isn't
						// empty
						if (!Util.existsIn(input, secondarySmsNumbers) && !input.equals("")) {
							// Iterate through all strings in the list of primarySmsNumbers to check
							// if number already exists
							for (String number : primarySmsNumbers) {
								// If a string in the list is equal with the input then it's
								// duplicated
								if (number.equalsIgnoreCase(input)) {
									duplicatedNumbers = true;
								}
							}

							// Store input if duplicated numbers is false
							if (!duplicatedNumbers) {
								// Add given input to list
								primarySmsNumbers.add(input);
								try {
									// Store to shared preferences
									prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_LISTEN_NUMBERS_KEY, primarySmsNumbers, context);
								} catch (IllegalArgumentException e) {
									logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
								}
								// Update affected UI widgets
								updatePrimarySmsNumberSpinner();
								// Log
								logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "New PRIMARY sms number has been stored from user input to the list of PRIMARY sms numbers. New PRIMARY sms number is: \"" + input + "\"");
							} else {
								Toast.makeText(context, R.string.NUMBER_ALREADY_IN_PRIMARY_LIST, Toast.LENGTH_LONG).show();
								logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "Given PRIMARY sms number(" + input + ") already exists in the list of PRIMARY sms numbers and therefore cannot be stored. Showing dialog of type SMS_PRIMARY again");
								buildAndShowInputDialog(type);
							}
						} else {
							// Empty input was given
							if (input.equals("")) {
								Toast.makeText(context, R.string.NUMBER_IS_NEEDED, Toast.LENGTH_LONG).show();
								logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "Given PRIMARY sms number is empty and therefore cannot be stored. Showing dialog of type SMS_PRIMARY again");
							} else { // Given primary number exists in the list of secondary numbers
								Toast.makeText(context, R.string.DUPLICATED_NUMBERS, Toast.LENGTH_LONG).show();
								logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "Given PRIMARY sms number(" + input + ") already exists in the list of SECONDARY sms numbers and therefore cannot be stored. Showing dialog of type SMS_PRIMARY again");
							}
							buildAndShowInputDialog(type);
						}
						break;
					case SMS_SECONDARY:
						// Store input
						input = noBlanksInputEditText.getText().toString();
						// If input doesn't exist in the list of primarySmsNumbers and input isn't
						// empty
						if (!Util.existsIn(input, primarySmsNumbers) && !input.equals("")) {
							// Iterate through all strings in the list of secondarySmsNumbers to
							// check if number already exists
							for (String number : secondarySmsNumbers) {
								// If a string in the list is equal with the input then it's
								// duplicated
								if (number.equalsIgnoreCase(input)) {
									duplicatedNumbers = true;
								}
							}

							// Store input if duplicated numbers is false
							if (!duplicatedNumbers) {
								// Add given input to list
								secondarySmsNumbers.add(input);
								try {
									// Store to shared preferences
									prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.SECONDARY_LISTEN_NUMBERS_KEY, secondarySmsNumbers, context);
								} catch (IllegalArgumentException e) {
									logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
								}
								// Update affected UI widgets
								updateSecondarySmsNumberSpinner();
								// Log
								logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "New SECONDARY sms number has been stored from user input to the list of SECONDARY sms numbers. New SECONDARY sms number is: \"" + input + "\"");
							} else {
								Toast.makeText(context, R.string.NUMBER_ALREADY_IN_SECONDARY_LIST, Toast.LENGTH_LONG).show();
								logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "Given SECONDARY sms number(" + input + ") already exists in the list of SECONDARY sms numbers and therefore cannot be stored. Showing dialog of type SMS_SECONDARY again");
								buildAndShowInputDialog(type);
							}
						} else {
							// Empty input was given
							if (input.equals("")) {
								Toast.makeText(context, R.string.NUMBER_IS_NEEDED, Toast.LENGTH_LONG).show();
								logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "Given SECONDARY sms number is empty and therefore cannot be stored. Showing dialog of type SMS_SECONDARY again");
							} else { // Given primary number exists in the list of secondary numbers
								Toast.makeText(context, R.string.DUPLICATED_NUMBERS, Toast.LENGTH_LONG).show();
								logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "Given SECONDARY sms number(" + input + ") already exists in the list of PRIMARY sms numbers and therefore cannot be stored. Showing dialog of type SMS_SECONDARY again");
							}
							buildAndShowInputDialog(type);
						}
						break;
					default: // <--Unsupported dialog type
						logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "Nothing is stored beacause given dialog type is UNSUPPORTED, given dialog is of type number: \"" + Integer.toString(type.ordinal()) + "\"");
				}
			}
		});

		// Only set neutral button if dialog type is supported
		if (type.ordinal() >= DialogTypes.SMS_PRIMARY.ordinal() && type.ordinal() <= DialogTypes.SMS_SECONDARY.ordinal()) {
			// Set a neutral button, due to documentation it has same functionality as "back" button
			dialog.setNeutralButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					// DO NOTHING, except logging
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog().NeutralButton.OnClickListener().onClick()", "Neutral Button pressed in dialog, nothing done");
				}
			});
		}

		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowInputDialog()", "Showing dialog");

		// Show it
		dialog.show();
	}

	/**
	 * To build up one of the two remove dialogs for the different phone numbers. The supported types are:
	 * <p>
	 * <ul>
	 * <li><b><i>DialogTypes.SMS_PRIMARY</b></i></li>
	 * <li><b><i>DialogTypes.SMS_SECONDARY</b></i></li>
	 * </ul>
	 * <p>
	 * If a dialog type are given as parameter thats not supported a dummy dialog will be built and shown.<br>
	 * This method also stores given data into shared preferences directly.
	 * 
	 * @param type
	 *            Type of dialog to build up and show
	 * @see #buildAndShowInputDialog()
	 * @see #updatePrimarySmsNumberSpinner()
	 * @see #updateSecondarySmsNumberSpinner()
	 * @see PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context) setPrefs(PrefKeys, PrefKeys, Object, Context)
	 * @see LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see LogHandler#logCatTxt(LogPriorities, String, String, Throwable) logCatTxt(LogPriorities, String, String,Throwable)
	 * @see DialogTypes
	 */
	private void buildAndShowDeleteDialog(final DialogTypes type) {
		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowDeleteDialog()", "Start building delete dialog");

		// Build up the alert dialog
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);

		// Set some attributes, title and message containing actual number
		dialog.setIcon(android.R.drawable.ic_dialog_alert);

		/*
		 * Switch through the different dialog types and set correct strings and edittext to the dialog. If dialog type is non supported a default
		 * dialog DUMMY is built up.
		 */
		switch (type) {
			case SMS_PRIMARY:
				// Set title
				dialog.setTitle(R.string.DELETE_NUMBER_PROMPT_TITLE);
				// Build and set message
				dialog.setMessage(getString(R.string.DELETE_PRIMARY_NUMBER_PROMPT_MESSAGE) + " " + primarySmsNumbers.get(primarySmsNumberSpinner.getSelectedItemPosition()) + "?");
				// Set dialog to non cancelable
				dialog.setCancelable(false);
				// Logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowDeleteDialog()", "Dialog attributes is set for dialog type SMS_PRIMARY");
				break;
			case SMS_SECONDARY:
				dialog.setTitle(R.string.DELETE_NUMBER_PROMPT_TITLE);
				dialog.setMessage(getString(R.string.DELETE_SECONDARY_NUMBER_PROMPT_MESSAGE) + " " + secondarySmsNumbers.get(secondarySmsNumberSpinner.getSelectedItemPosition()) + "?");
				dialog.setCancelable(false);
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowDeleteDialog()", "Dialog attributes is set for dialog type SMS_SECONDARY");
				break;
			default: // <--Unsupported dialog type. Displaying a dummy dialog!
				dialog.setTitle("Congratulations!");
				dialog.setMessage("Somehow you got this dialog to show up! I bet a monkey must have been messing around with the code;-)");
				dialog.setCancelable(false);
				logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":buildAndShowDeleteDialog()", "A UNSUPPORTED dialog type has been given as parameter, a DUMMY dialog will be built and shown");
		}

		// Set a positive button and listen on it
		dialog.setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// Log information
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowDeleteDialog().PosButton.OnClickListener().onClick()", "Positive Button pressed");

				/*
				 * Switch through the different dialog types and set proper input handling to each of them. If dialog type is non supported no input
				 * is taken.
				 */
				switch (type) {
					case SMS_PRIMARY:
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowDeleteDialog().PosButton.OnClickListener().onClick()", "PRIMARY sms number: \"" + primarySmsNumbers.get(primarySmsNumberSpinner.getSelectedItemPosition()) + "\" is about to be removed from the list of PRIMARY sms numbers");
						// Delete number from list
						primarySmsNumbers.remove(primarySmsNumberSpinner.getSelectedItemPosition());
						try {
							// Store to shared preferences
							prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_LISTEN_NUMBERS_KEY, primarySmsNumbers, context);
						} catch (IllegalArgumentException e) {
							logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":buildAndShowDeleteDialog().PosButton.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
						}
						// Update affected UI widgets
						updatePrimarySmsNumberSpinner();
						break;
					case SMS_SECONDARY:
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowDeleteDialog().PosButton.OnClickListener().onClick()", "SECONDARY sms number: \"" + secondarySmsNumbers.get(secondarySmsNumberSpinner.getSelectedItemPosition()) + "\" is about to be removed from the list of SECONDARY sms numbers");
						secondarySmsNumbers.remove(secondarySmsNumberSpinner.getSelectedItemPosition());
						try {
							prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.SECONDARY_LISTEN_NUMBERS_KEY, secondarySmsNumbers, context);
						} catch (IllegalArgumentException e) {
							logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":buildAndShowDeleteDialog().PosButton.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
						}
						updateSecondarySmsNumberSpinner();
						break;
					default: // <--Unsupported dialog type
						logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":buildAndShowDeleteDialog().PositiveButton.OnClickListener().onClick()", "Nothing is stored beacause given dialog type is UNSUPPORTED, given dialog is of type number: \"" + type.name() + "\"");
				}
			}
		});

		// Only set neutral button if dialog type is supported
		if (type.ordinal() >= DialogTypes.SMS_PRIMARY.ordinal() && type.ordinal() <= DialogTypes.SMS_SECONDARY.ordinal()) {
			// Set a neutral button, due to documentation it has same functionality as "back" button
			dialog.setNeutralButton(R.string.NO, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					// DO NOTHING, except logging
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowDeleteDialog().NeutralButton.OnClickListener().onClick()", "Neutral Button pressed in dialog, nothing done");
				}
			});
		}

		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowDeleteDialog()", "Showing dialog");

		// Show it
		dialog.show();
	}

	/**
	 * To update primary sms number <code>Spinner</code> with correct values.
	 * 
	 * @see #updateSecondarySmsNumberSpinner()
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 */
	private void updatePrimarySmsNumberSpinner() {
		// Check if there are primary sms numbers and build up a proper spinner according to that
		// information
		if (!primarySmsNumbers.isEmpty()) {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, primarySmsNumbers);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			primarySmsNumberSpinner.setAdapter(adapter);
			// Logging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updatePrimarySmsNumberSpinner()", "Populate PRIMARY sms number spinner with values: " + primarySmsNumbers);
		} else {
			// Only add item to list if it's empty
			if (emptySmsNumbers.isEmpty()) {
				emptySmsNumbers.add(getString(R.string.ENTER_PHONE_NUMBER_HINT));
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, emptySmsNumbers);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			primarySmsNumberSpinner.setAdapter(adapter);
			// Logging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updatePrimarySmsNumberSpinner()", "List with PRIMARY sms numbers is empty, populating spinner with an empty list");
		}

		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updatePrimarySmsNumberSpinner()", "PRIMARY sms numbers spinner updated");
	}

	/**
	 * To update secondary sms number <code>Spinner</code> with correct values.
	 * 
	 * @see #updatePrimarySmsNumberSpinner()
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 */
	private void updateSecondarySmsNumberSpinner() {
		// Check if there are secondary sms numbers and build up a proper spinner according to that
		// information
		if (!secondarySmsNumbers.isEmpty()) {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, secondarySmsNumbers);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			secondarySmsNumberSpinner.setAdapter(adapter);
			// Logging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateSecondarySmsNumberSpinner()", "Populate SECONDARY sms number spinner with values: " + secondarySmsNumbers);
		} else {
			// Only add item to list if it's empty
			if (emptySmsNumbers.isEmpty()) {
				emptySmsNumbers.add(getString(R.string.ENTER_PHONE_NUMBER_HINT));
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, emptySmsNumbers);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			secondarySmsNumberSpinner.setAdapter(adapter);
			// Logging
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateSecondarySmsNumberSpinner()", "List with SECONDARY sms numbers is empty, populating spinner with an empty list");
		}

		// Logging
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateSecondarySmsNumberSpinner()", "SECONDARY sms numbers spinner updated");
	}
}
