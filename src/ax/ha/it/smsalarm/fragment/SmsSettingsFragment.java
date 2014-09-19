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
	private final String LOG_TAG = getClass().getSimpleName();

	// Objects needed for logging and shared preferences handling
	private final LogHandler logger = LogHandler.getInstance();
	private final PreferencesHandler prefHandler = PreferencesHandler.getInstance();

	// Must have the application context
	private Context context;

	// The Buttons...
	private Button addPrimarySmsNumberButton;
	private Button removePrimarySmsNumberButton;
	private Button addSecondarySmsNumberButton;
	private Button removeSecondarySmsNumberButton;

	// ...and Spinners
	private Spinner primarySmsNumberSpinner;
	private Spinner secondarySmsNumberSpinner;

	// List of strings containing primary- and secondary sms numbers
	private List<String> primarySmsNumbers = new ArrayList<String>();
	private List<String> secondarySmsNumbers = new ArrayList<String>();
	private final List<String> emptySmsNumbers = new ArrayList<String>(); // A "dummy" list just containing one element, one string

	/**
	 * To create a new <code>SmsSettingsFragment</code>.
	 * 
	 * @param context
	 *            Context
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 */
	public SmsSettingsFragment() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":SmsSettingsFragment()", "Creating a new Sms settings fragment");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate()", "Setting Context to fragment");

		// Set context here, it's safe because this fragment has been attached to its container, hence we have access to context
		context = getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreateView()", "Creating and initializing view for this fragment");
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
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "Start finding Views by their Id");

		// Finding button views
		addPrimarySmsNumberButton = (Button) view.findViewById(R.id.addPrimarySmsNumber_btn);
		removePrimarySmsNumberButton = (Button) view.findViewById(R.id.deletePrimarySmsNumber_btn);
		addSecondarySmsNumberButton = (Button) view.findViewById(R.id.addSecondarySmsNumber_btn);
		removeSecondarySmsNumberButton = (Button) view.findViewById(R.id.deleteSecondarySmsNumber_btn);

		// Finding Spinner views
		primarySmsNumberSpinner = (Spinner) view.findViewById(R.id.primarySmsNumberSpinner_sp);
		secondarySmsNumberSpinner = (Spinner) view.findViewById(R.id.secondarySmsNumberSpinner_sp);

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "All Views found for Fragment:\"" + LOG_TAG + "\"");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void fetchSharedPrefs() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":fetchSharedPrefs()", "Start fetching shared preferences needed by this fragment");

		try {
			primarySmsNumbers = (List<String>) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_LISTEN_NUMBERS_KEY, DataTypes.LIST, context);
			secondarySmsNumbers = (List<String>) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.SECONDARY_LISTEN_NUMBERS_KEY, DataTypes.LIST, context);
		} catch (IllegalArgumentException e) {
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":fetchSharedPrefs()", "An unsupported datatype was given as argument to PreferencesHandler.getPrefs()", e);
		}

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":fetchSharedPrefs()", "Shared preferences fetched");
	}

	@Override
	public void updateFragmentView() {
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
				createSmsPrimaryInputDialog();
			}
		});

		// Set listener to removePrimarySmsNumberButton
		removePrimarySmsNumberButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().removePrimarySmsNumberButton.OnClickListener().onClick()", "Remove PRIMARY sms number button pressed");

				// Only show delete dialog if primary sms numbers exists, else show toast
				if (!primarySmsNumbers.isEmpty()) {
					createSmsPrimaryRemoveDialog();
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
				createSmsSecondaryInputDialog();
			}
		});

		// Set listener to removeSecondarySmsNumberButton
		removeSecondarySmsNumberButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().removeSecondarySmsNumberButton.OnClickListener().onClick()", "Remove SECONDARY sms number button pressed");

				// Only show delete dialog if secondary sms numbers exists, else show toast
				if (!secondarySmsNumbers.isEmpty()) {
					createSmsSecondaryRemoveDialog();
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
	 * @see #createSmsPrimaryRemoveDialog()
	 * @see #updatePrimarySmsNumberSpinner()
	 * @see PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context)
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 * @see LogHandler#logCatTxt(LogPriorities, String, String, Throwable)
	 */
	private void createSmsPrimaryInputDialog() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createSmsPrimaryInputDialog()", "Start building dialog for input of Sms Primary number");

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
		noBlanksInputEditText.setInputType(InputType.TYPE_CLASS_TEXT); 	// Set input type to edit text
		// @formatter:on

		// Set a positive button and listen on it
		dialog.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createSmsPrimaryInputDialog().PositiveButton.OnClickListener().onClick()", "Positive Button pressed");

				// Boolean indicating if there are duplicates of primary and secondary sms numbers
				boolean duplicatedNumbers = false;
				// Store input
				String input = noBlanksInputEditText.getText().toString();

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
							logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":createSmsPrimaryInputDialog().PositiveButton.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
						}

						// Update affected UI widgets
						updatePrimarySmsNumberSpinner();
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createSmsPrimaryInputDialog().PositiveButton.OnClickListener().onClick()", "New PRIMARY sms number has been stored from user input to the list of PRIMARY sms numbers. New PRIMARY sms number is: \"" + input + "\"");
					} else {
						Toast.makeText(context, R.string.NUMBER_ALREADY_IN_PRIMARY_LIST, Toast.LENGTH_LONG).show();
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createSmsPrimaryInputDialog().PositiveButton.OnClickListener().onClick()", "Given PRIMARY sms number(" + input + ") already exists in the list of PRIMARY sms numbers and therefore cannot be stored. Showing dialog again");

						createSmsPrimaryInputDialog();
					}
				} else {
					// Empty input was given
					if (input.equals("")) {
						Toast.makeText(context, R.string.NUMBER_IS_NEEDED, Toast.LENGTH_LONG).show();
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createSmsPrimaryInputDialog().PositiveButton.OnClickListener().onClick()", "Given PRIMARY sms number is empty and therefore cannot be stored. Showing dialog again again");
					} else { // Given primary number exists in the list of secondary numbers
						Toast.makeText(context, R.string.DUPLICATED_NUMBERS, Toast.LENGTH_LONG).show();
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createSmsPrimaryInputDialog().PositiveButton.OnClickListener().onClick()", "Given PRIMARY sms number(" + input + ") already exists in the list of SECONDARY sms numbers and therefore cannot be stored. Showing dialog again");
					}

					createSmsPrimaryInputDialog();
				}
			}
		});

		// Set a neutral button, due to documentation it has same functionality as "back" button
		dialog.setNeutralButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// DO NOTHING, except logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createSmsPrimaryInputDialog().NeutralButton.OnClickListener().onClick()", "Neutral Button pressed in dialog, nothing done");
			}
		});

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createSmsPrimaryInputDialog()", "Showing dialog");

		// Show it
		dialog.show();
	}

	/**
	 * To build up and display a dialog which let's the user add a phone number to the list of <b><i>Secondary alarm triggering phone numbers</i></b>. <br>
	 * <b><i>Note. The input of this dialog doesn't accept blankspaces({@link Util.NoBlanksInputEditText})</i></b>.
	 * 
	 * @see #createSmsSecondaryRemoveDialog()
	 * @see #updateSecondarySmsNumberSpinner()
	 * @see PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context)
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 * @see LogHandler#logCatTxt(LogPriorities, String, String, Throwable)
	 */
	private void createSmsSecondaryInputDialog() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createSmsSecondaryInputDialog()", "Start building dialog for input of Sms Secondary number");

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
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createSmsSecondaryInputDialog().PositiveButton.OnClickListener().onClick()", "Positive Button pressed");

				boolean duplicatedNumbers = false;
				String input = noBlanksInputEditText.getText().toString();

				if (!Util.existsIn(input, primarySmsNumbers) && !input.equals("")) {
					for (String number : secondarySmsNumbers) {
						if (number.equalsIgnoreCase(input)) {
							duplicatedNumbers = true;
						}
					}

					if (!duplicatedNumbers) {
						secondarySmsNumbers.add(input);

						try {
							// Store to shared preferences
							prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.SECONDARY_LISTEN_NUMBERS_KEY, secondarySmsNumbers, context);
						} catch (IllegalArgumentException e) {
							logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":createSmsSecondaryInputDialog().PositiveButton.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
						}

						updateSecondarySmsNumberSpinner();
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createSmsSecondaryInputDialog().PositiveButton.OnClickListener().onClick()", "New SECONDARY sms number has been stored from user input to the list of SECONDARY sms numbers. New SECONDARY sms number is: \"" + input + "\"");
					} else {
						Toast.makeText(context, R.string.NUMBER_ALREADY_IN_SECONDARY_LIST, Toast.LENGTH_LONG).show();
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createSmsSecondaryInputDialog().PositiveButton.OnClickListener().onClick()", "Given SECONDARY sms number(" + input + ") already exists in the list of SECONDARY sms numbers and therefore cannot be stored. Showing dialog again");
						createSmsSecondaryInputDialog();
					}
				} else {
					if (input.equals("")) {
						Toast.makeText(context, R.string.NUMBER_IS_NEEDED, Toast.LENGTH_LONG).show();
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createSmsSecondaryInputDialog().PositiveButton.OnClickListener().onClick()", "Given SECONDARY sms number is empty and therefore cannot be stored. Showing dialog again");
					} else {
						Toast.makeText(context, R.string.DUPLICATED_NUMBERS, Toast.LENGTH_LONG).show();
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createSmsSecondaryInputDialog().PositiveButton.OnClickListener().onClick()", "Given SECONDARY sms number(" + input + ") already exists in the list of PRIMARY sms numbers and therefore cannot be stored. Showing dialog again");
					}

					createSmsSecondaryInputDialog();
				}
			}
		});

		dialog.setNeutralButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// DO NOTHING, except logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createSmsSecondaryInputDialog().NeutralButton.OnClickListener().onClick()", "Neutral Button pressed in dialog, nothing done");
			}
		});

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createSmsSecondaryInputDialog()", "Showing dialog");
		dialog.show();
	}

	/**
	 * To build up and display a dialog which let's the user remove a phone number from the list of <b><i>Primary alarm triggering phone
	 * numbers</i></b>. <br>
	 *
	 * @see #createSmsPrimaryInputDialog()
	 * @see #updatePrimarySmsNumberSpinner()
	 * @see PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context)
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 * @see LogHandler#logCatTxt(LogPriorities, String, String, Throwable)
	 */
	private void createSmsPrimaryRemoveDialog() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createSmsPrimaryRemoveDialog()", "Start building dialog for removing a Primary Sms number");

		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setIcon(android.R.drawable.ic_dialog_alert);
		dialog.setTitle(R.string.DELETE_NUMBER_PROMPT_TITLE);
		dialog.setMessage(getString(R.string.DELETE_PRIMARY_NUMBER_PROMPT_MESSAGE) + " " + primarySmsNumbers.get(primarySmsNumberSpinner.getSelectedItemPosition()) + "?");
		dialog.setCancelable(false);

		dialog.setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createSmsPrimaryRemoveDialog().PosButton.OnClickListener().onClick()", "Positive Button pressed");
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createSmsPrimaryRemoveDialog().PosButton.OnClickListener().onClick()", "PRIMARY sms number: \"" + primarySmsNumbers.get(primarySmsNumberSpinner.getSelectedItemPosition()) + "\" is about to be removed from the list of PRIMARY sms numbers");

				// Delete number from list
				primarySmsNumbers.remove(primarySmsNumberSpinner.getSelectedItemPosition());
				try {
					// Store to shared preferences
					prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_LISTEN_NUMBERS_KEY, primarySmsNumbers, context);
				} catch (IllegalArgumentException e) {
					logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":createSmsPrimaryRemoveDialog().PosButton.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
				}

				// Update affected UI widgets
				updatePrimarySmsNumberSpinner();
			}
		});

		dialog.setNeutralButton(R.string.NO, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// DO NOTHING, except logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createSmsPrimaryRemoveDialog().NeutralButton.OnClickListener().onClick()", "Neutral Button pressed in dialog, nothing done");
			}
		});

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createSmsPrimaryRemoveDialog()", "Showing dialog");
		dialog.show();
	}

	/**
	 * To build up and display a dialog which let's the user remove a phone number from the list of <b><i>Secondary alarm triggering phone
	 * numbers</i></b>. <br>
	 *
	 * @see #createSmsSecondaryInputDialog()
	 * @see #updateSecondarySmsNumberSpinner()
	 * @see PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context)
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 * @see LogHandler#logCatTxt(LogPriorities, String, String, Throwable)
	 */
	private void createSmsSecondaryRemoveDialog() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createSmsSecondaryRemoveDialog()", "Start building dialog for removing a Secondary Sms number");

		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(R.string.DELETE_NUMBER_PROMPT_TITLE);
		dialog.setMessage(getString(R.string.DELETE_SECONDARY_NUMBER_PROMPT_MESSAGE) + " " + secondarySmsNumbers.get(secondarySmsNumberSpinner.getSelectedItemPosition()) + "?");
		dialog.setCancelable(false);

		dialog.setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createSmsSecondaryRemoveDialog().PosButton.OnClickListener().onClick()", "Positive Button pressed");
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createSmsSecondaryRemoveDialog().PosButton.OnClickListener().onClick()", "SECONDARY sms number: \"" + secondarySmsNumbers.get(secondarySmsNumberSpinner.getSelectedItemPosition()) + "\" is about to be removed from the list of SECONDARY sms numbers");

				secondarySmsNumbers.remove(secondarySmsNumberSpinner.getSelectedItemPosition());
				try {
					prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.SECONDARY_LISTEN_NUMBERS_KEY, secondarySmsNumbers, context);
				} catch (IllegalArgumentException e) {
					logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":createSmsSecondaryRemoveDialog().PosButton.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
				}
				updateSecondarySmsNumberSpinner();
			}
		});

		dialog.setNeutralButton(R.string.NO, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// DO NOTHING, except logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createSmsSecondaryRemoveDialog().NeutralButton.OnClickListener().onClick()", "Neutral Button pressed in dialog, nothing done");
			}
		});

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createSmsSecondaryRemoveDialog()", "Showing dialog");
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
		// Check if there are secondary sms numbers and build up a proper spinner according to that information
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
