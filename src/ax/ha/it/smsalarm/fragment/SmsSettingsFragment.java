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
import android.text.InputType;
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
import ax.ha.it.smsalarm.handler.LogHandler;
import ax.ha.it.smsalarm.handler.LogHandler.LogPriorities;
import ax.ha.it.smsalarm.handler.PreferencesHandler;
import ax.ha.it.smsalarm.handler.PreferencesHandler.DataTypes;
import ax.ha.it.smsalarm.handler.PreferencesHandler.PrefKeys;
import ax.ha.it.smsalarm.ui.NoBlanksInputEditText;
import ax.ha.it.smsalarm.util.Util;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * <code>Fragment</code> containing all the views and user interface widgets for the <b><i>Sms Settings</i></b>. Fragment does also contain all logic
 * for the widgets.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class SmsSettingsFragment extends SherlockFragment implements ApplicationFragment {
	// Log tag string
	private final String LOG_TAG = getClass().getSimpleName();

	// Objects needed for logging and shared preferences handling
	private final LogHandler logger = LogHandler.getInstance();
	private final PreferencesHandler prefHandler = PreferencesHandler.getInstance();

	// Must have the application context
	private final Context context;

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
	 *            Context
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

		// @formatter:off
		// Ensure this fragment has data, UI-widgets and logic set before the view is returned
		fetchSharedPrefs();		// Fetch shared preferences needed by objects in this fragment
		findViews(view);		// Find UI widgets and link link them to objects in this fragment
		setListeners();			// Set necessary listeners
		updateFragmentView();	// Update all UI widgets with fetched data from shared preferences
		// @formatter:on

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

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "All Views found");
	}

	@SuppressWarnings("unchecked")
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
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners()", "Setting listeners to the different user interface widgets");

		// Set listener to addPrimarySmsNumberButton
		addPrimarySmsNumberButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().addPrimarySmsNumberButton.OnClickListener().onClick()", "Add PRIMARY sms number button pressed");
				buildAndShowSmsPrimaryInputDialog();
			}
		});

		// Set listener to removePrimarySmsNumberButton
		removePrimarySmsNumberButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().removePrimarySmsNumberButton.OnClickListener().onClick()", "Remove PRIMARY sms number button pressed");

				// Only show delete dialog if primary sms numbers exists, else show toast
				if (!primarySmsNumbers.isEmpty()) {
					buildAndShowSmsPrimaryRemoveDialog();
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
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().addSecondarySmsNumberButton.OnClickListener().onClick()", "Add SECONDARY sms number button pressed");
				buildAndShowSmsSecondaryInputDialog();
			}
		});

		// Set listener to removeSecondarySmsNumberButton
		removeSecondarySmsNumberButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().removeSecondarySmsNumberButton.OnClickListener().onClick()", "Remove SECONDARY sms number button pressed");

				// Only show delete dialog if secondary sms numbers exists, else show toast
				if (!secondarySmsNumbers.isEmpty()) {
					buildAndShowSmsSecondaryRemoveDialog();
				} else {
					Toast.makeText(context, R.string.NO_SECONDARY_NUMBER_EXISTS, Toast.LENGTH_LONG).show();
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().removeSecondarySmsNumberButton.OnClickListener().onClick()", "Cannot build and show dialog because the list of SECONDARY sms numbers are");
				}
			}
		});
	}

	/**
	 * To build up and display a dialog which let's the user add a phone number to the list of <b><i>Primary alarm triggering phone numbers</i></b>. <br>
	 * <b><i>Note. The input of this dialog doesn't accept blankspaces({@link Util.NoBlanksInputEditText})</i></b>.
	 * 
	 * @see #buildAndShowSmsPrimaryRemoveDialog()
	 * @see #updatePrimarySmsNumberSpinner()
	 * @see PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context) setPrefs(PrefKeys, PrefKeys, Object, Context)
	 * @see LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see LogHandler#logCatTxt(LogPriorities, String, String, Throwable) logCatTxt(LogPriorities, String, String,Throwable)
	 */
	private void buildAndShowSmsPrimaryInputDialog() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowSmsPrimaryInputDialog()", "Start building dialog for input of Sms Primary number");

		// Build up the alert dialog
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		final EditText noBlanksInputEditText = new NoBlanksInputEditText(context);

		// @formatter:off
		// Configure dialog and edit text
		dialog.setIcon(android.R.drawable.ic_dialog_info); 				// Set icon
		dialog.setTitle(R.string.NUMBER_PROMPT_TITLE); 					// Set title
		dialog.setMessage(R.string.PRIMARY_NUMBER_PROMPT_MESSAGE); 		// Set message
		dialog.setCancelable(false); 									// Set dialog to non cancelable
		dialog.setView(noBlanksInputEditText); 							// Bind dialog to input
		noBlanksInputEditText.setHint(R.string.NUMBER_PROMPT_HINT); 	// Set hint to edit text
		noBlanksInputEditText.setInputType(InputType.TYPE_CLASS_TEXT); 	// Set Input type to edit text
		// @formatter:on

		// Set a positive button and listen on it
		dialog.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
			// To store input from dialogs edit text field
			String input = "";

			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowSmsPrimaryInputDialog().PositiveButton.OnClickListener().onClick()", "Positive Button pressed");

				// Boolean indicating if there are duplicates of primary and secondary sms numbers
				boolean duplicatedNumbers = false;

				// Store input
				input = noBlanksInputEditText.getText().toString();

				// If input doesn't exist in the list of secondarySmsNumbers and input isn't empty
				if (!Util.existsIn(input, secondarySmsNumbers) && !input.equals("")) {
					// Iterate through all strings in the list of primarySmsNumbers to check if number already exists
					for (String number : primarySmsNumbers) {
						// If a string in the list is equal with the input then it'sduplicated
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
							logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":buildAndShowSmsPrimaryInputDialog().PositiveButton.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
						}
						// Update affected UI widgets
						updatePrimarySmsNumberSpinner();
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowSmsPrimaryInputDialog().PositiveButton.OnClickListener().onClick()", "New PRIMARY sms number has been stored from user input to the list of PRIMARY sms numbers. New PRIMARY sms number is: \"" + input + "\"");
					} else {
						Toast.makeText(context, R.string.NUMBER_ALREADY_IN_PRIMARY_LIST, Toast.LENGTH_LONG).show();
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowSmsPrimaryInputDialog().PositiveButton.OnClickListener().onClick()", "Given PRIMARY sms number(" + input + ") already exists in the list of PRIMARY sms numbers and therefore cannot be stored. Showing dialog again");
						buildAndShowSmsPrimaryInputDialog();
					}
				} else {
					// Empty input was given
					if (input.equals("")) {
						Toast.makeText(context, R.string.NUMBER_IS_NEEDED, Toast.LENGTH_LONG).show();
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowSmsPrimaryInputDialog().PositiveButton.OnClickListener().onClick()", "Given PRIMARY sms number is empty and therefore cannot be stored. Showing dialog again again");
					} else { // Given primary number exists in the list of secondary numbers
						Toast.makeText(context, R.string.DUPLICATED_NUMBERS, Toast.LENGTH_LONG).show();
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowSmsPrimaryInputDialog().PositiveButton.OnClickListener().onClick()", "Given PRIMARY sms number(" + input + ") already exists in the list of SECONDARY sms numbers and therefore cannot be stored. Showing dialog again");
					}
					buildAndShowSmsPrimaryInputDialog();
				}

			}
		});

		// Set a neutral button, due to documentation it has same functionality as "back" button
		dialog.setNeutralButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// DO NOTHING, except logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowSmsPrimaryInputDialog().NeutralButton.OnClickListener().onClick()", "Neutral Button pressed in dialog, nothing done");
			}
		});

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowSmsPrimaryInputDialog()", "Showing dialog");

		// Show it
		dialog.show();
	}

	/**
	 * To build up and display a dialog which let's the user add a phone number to the list of <b><i>Secondary alarm triggering phone numbers</i></b>. <br>
	 * <b><i>Note. The input of this dialog doesn't accept blankspaces({@link Util.NoBlanksInputEditText})</i></b>.
	 * 
	 * @see #buildAndShowSmsSecondaryRemoveDialog()
	 * @see #updateSecondarySmsNumberSpinner()
	 * @see PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context) setPrefs(PrefKeys, PrefKeys, Object, Context)
	 * @see LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see LogHandler#logCatTxt(LogPriorities, String, String, Throwable) logCatTxt(LogPriorities, String, String,Throwable)
	 */
	private void buildAndShowSmsSecondaryInputDialog() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowSmsSecondaryInputDialog()", "Start building dialog for input of Sms Secondary number");

		// Build up the alert dialog
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		final EditText noBlanksInputEditText = new NoBlanksInputEditText(context);

		dialog.setIcon(android.R.drawable.ic_dialog_info);
		dialog.setTitle(R.string.NUMBER_PROMPT_TITLE);
		dialog.setMessage(R.string.SECONDARY_NUMBER_PROMPT_MESSAGE);
		dialog.setCancelable(false);
		dialog.setView(noBlanksInputEditText);
		noBlanksInputEditText.setHint(R.string.NUMBER_PROMPT_HINT);
		noBlanksInputEditText.setInputType(InputType.TYPE_CLASS_TEXT);

		dialog.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
			String input = "";

			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowSmsSecondaryInputDialog().PositiveButton.OnClickListener().onClick()", "Positive Button pressed");

				// Boolean indicating if there are duplicates of primary and secondary sms numbers
				boolean duplicatedNumbers = false;

				// Store input
				input = noBlanksInputEditText.getText().toString();

				// If input doesn't exist in the list of primarySmsNumbers and input isn't empty
				if (!Util.existsIn(input, primarySmsNumbers) && !input.equals("")) {
					// Iterate through all strings in the list of secondarySmsNumbers to check if number already exists
					for (String number : secondarySmsNumbers) {
						// If a string in the list is equal with the input then it's duplicated
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
							logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":buildAndShowSmsSecondaryInputDialog().PositiveButton.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
						}
						// Update affected UI widgets
						updateSecondarySmsNumberSpinner();
						// Log
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowSmsSecondaryInputDialog().PositiveButton.OnClickListener().onClick()", "New SECONDARY sms number has been stored from user input to the list of SECONDARY sms numbers. New SECONDARY sms number is: \"" + input + "\"");
					} else {
						Toast.makeText(context, R.string.NUMBER_ALREADY_IN_SECONDARY_LIST, Toast.LENGTH_LONG).show();
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowSmsSecondaryInputDialog().PositiveButton.OnClickListener().onClick()", "Given SECONDARY sms number(" + input + ") already exists in the list of SECONDARY sms numbers and therefore cannot be stored. Showing dialog again");
						buildAndShowSmsSecondaryInputDialog();
					}
				} else {
					// Empty input was given
					if (input.equals("")) {
						Toast.makeText(context, R.string.NUMBER_IS_NEEDED, Toast.LENGTH_LONG).show();
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowSmsSecondaryInputDialog().PositiveButton.OnClickListener().onClick()", "Given SECONDARY sms number is empty and therefore cannot be stored. Showing dialog again");
					} else { // Given primary number exists in the list of secondary numbers
						Toast.makeText(context, R.string.DUPLICATED_NUMBERS, Toast.LENGTH_LONG).show();
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowSmsSecondaryInputDialog().PositiveButton.OnClickListener().onClick()", "Given SECONDARY sms number(" + input + ") already exists in the list of PRIMARY sms numbers and therefore cannot be stored. Showing dialog again");
					}
					buildAndShowSmsSecondaryInputDialog();
				}
			}
		});

		dialog.setNeutralButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// DO NOTHING, except logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowSmsSecondaryInputDialog().NeutralButton.OnClickListener().onClick()", "Neutral Button pressed in dialog, nothing done");
			}
		});

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowSmsSecondaryInputDialog()", "Showing dialog");
		dialog.show();
	}

	/**
	 * To build up and display a dialog which let's the user remove a phone number from the list of <b><i>Primary alarm triggering phone
	 * numbers</i></b>. <br>
	 *
	 * @see #buildAndShowSmsPrimaryInputDialog()
	 * @see #updatePrimarySmsNumberSpinner()
	 * @see PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context) setPrefs(PrefKeys, PrefKeys, Object, Context)
	 * @see LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see LogHandler#logCatTxt(LogPriorities, String, String, Throwable) logCatTxt(LogPriorities, String, String,Throwable)
	 */
	private void buildAndShowSmsPrimaryRemoveDialog() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowSmsPrimaryRemoveDialog()", "Start building dialog for removing a Primary Sms number");

		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setIcon(android.R.drawable.ic_dialog_alert);
		dialog.setTitle(R.string.DELETE_NUMBER_PROMPT_TITLE);
		dialog.setMessage(getString(R.string.DELETE_PRIMARY_NUMBER_PROMPT_MESSAGE) + " " + primarySmsNumbers.get(primarySmsNumberSpinner.getSelectedItemPosition()) + "?");
		dialog.setCancelable(false);

		dialog.setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowSmsPrimaryRemoveDialog().PosButton.OnClickListener().onClick()", "Positive Button pressed");
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowSmsPrimaryRemoveDialog().PosButton.OnClickListener().onClick()", "PRIMARY sms number: \"" + primarySmsNumbers.get(primarySmsNumberSpinner.getSelectedItemPosition()) + "\" is about to be removed from the list of PRIMARY sms numbers");

				// Delete number from list
				primarySmsNumbers.remove(primarySmsNumberSpinner.getSelectedItemPosition());
				try {
					// Store to shared preferences
					prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_LISTEN_NUMBERS_KEY, primarySmsNumbers, context);
				} catch (IllegalArgumentException e) {
					logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":buildAndShowSmsPrimaryRemoveDialog().PosButton.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
				}
				// Update affected UI widgets
				updatePrimarySmsNumberSpinner();
			}
		});

		dialog.setNeutralButton(R.string.NO, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// DO NOTHING, except logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowSmsPrimaryRemoveDialog().NeutralButton.OnClickListener().onClick()", "Neutral Button pressed in dialog, nothing done");
			}
		});

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowSmsPrimaryRemoveDialog()", "Showing dialog");
		dialog.show();
	}

	/**
	 * To build up and display a dialog which let's the user remove a phone number from the list of <b><i>Secondary alarm triggering phone
	 * numbers</i></b>. <br>
	 *
	 * @see #buildAndShowSmsSecondaryInputDialog()
	 * @see #updateSecondarySmsNumberSpinner()
	 * @see PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context) setPrefs(PrefKeys, PrefKeys, Object, Context)
	 * @see LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see LogHandler#logCatTxt(LogPriorities, String, String, Throwable) logCatTxt(LogPriorities, String, String,Throwable)
	 */
	private void buildAndShowSmsSecondaryRemoveDialog() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowSmsSecondaryRemoveDialog()", "Start building dialog for removing a Secondary Sms number");

		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(R.string.DELETE_NUMBER_PROMPT_TITLE);
		dialog.setMessage(getString(R.string.DELETE_SECONDARY_NUMBER_PROMPT_MESSAGE) + " " + secondarySmsNumbers.get(secondarySmsNumberSpinner.getSelectedItemPosition()) + "?");
		dialog.setCancelable(false);

		dialog.setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// Log information
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowSmsSecondaryRemoveDialog().PosButton.OnClickListener().onClick()", "Positive Button pressed");

				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowSmsSecondaryRemoveDialog().PosButton.OnClickListener().onClick()", "SECONDARY sms number: \"" + secondarySmsNumbers.get(secondarySmsNumberSpinner.getSelectedItemPosition()) + "\" is about to be removed from the list of SECONDARY sms numbers");
				secondarySmsNumbers.remove(secondarySmsNumberSpinner.getSelectedItemPosition());
				try {
					prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.SECONDARY_LISTEN_NUMBERS_KEY, secondarySmsNumbers, context);
				} catch (IllegalArgumentException e) {
					logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":buildAndShowSmsSecondaryRemoveDialog().PosButton.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
				}
				updateSecondarySmsNumberSpinner();
			}
		});

		dialog.setNeutralButton(R.string.NO, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// DO NOTHING, except logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowSmsSecondaryRemoveDialog().NeutralButton.OnClickListener().onClick()", "Neutral Button pressed in dialog, nothing done");
			}
		});

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":buildAndShowSmsSecondaryRemoveDialog()", "Showing dialog");
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

			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updatePrimarySmsNumberSpinner()", "Populate PRIMARY sms number spinner with values: " + primarySmsNumbers);
		} else {
			// Only add item to list if it's empty
			if (emptySmsNumbers.isEmpty()) {
				emptySmsNumbers.add(getString(R.string.ENTER_PHONE_NUMBER_HINT));
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, emptySmsNumbers);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			primarySmsNumberSpinner.setAdapter(adapter);

			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updatePrimarySmsNumberSpinner()", "List with PRIMARY sms numbers is empty, populating spinner with an empty list");
		}

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

			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateSecondarySmsNumberSpinner()", "Populate SECONDARY sms number spinner with values: " + secondarySmsNumbers);
		} else {
			// Only add item to list if it's empty
			if (emptySmsNumbers.isEmpty()) {
				emptySmsNumbers.add(getString(R.string.ENTER_PHONE_NUMBER_HINT));
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, emptySmsNumbers);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			secondarySmsNumberSpinner.setAdapter(adapter);

			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateSecondarySmsNumberSpinner()", "List with SECONDARY sms numbers is empty, populating spinner with an empty list");
		}

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateSecondarySmsNumberSpinner()", "SECONDARY sms numbers spinner updated");
	}
}
