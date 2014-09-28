/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.fragment.dialog.AddSmsNumberDialog;
import ax.ha.it.smsalarm.fragment.dialog.RemoveSmsNumberDialog;
import ax.ha.it.smsalarm.handler.LogHandler;
import ax.ha.it.smsalarm.handler.LogHandler.LogPriorities;
import ax.ha.it.smsalarm.handler.PreferencesHandler;
import ax.ha.it.smsalarm.handler.PreferencesHandler.DataType;
import ax.ha.it.smsalarm.handler.PreferencesHandler.PrefKey;
import ax.ha.it.smsalarm.util.Util;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * {@link Fragment} containing all the views and user interface widgets for the <b><i>Sms Settings</i></b>. <code>Fragment</code> does also contain
 * all logic for the widgets.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class SmsSettingsFragment extends SherlockFragment implements ApplicationFragment {
	private static final String LOG_TAG = SmsSettingsFragment.class.getSimpleName();

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
	 * To create a new instance of {@link SmsSettingsFragment}.
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

		primarySmsNumbers = (List<String>) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_NUMBERS_KEY, DataType.LIST, context);
		secondarySmsNumbers = (List<String>) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_NUMBERS_KEY, DataType.LIST, context);

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":fetchSharedPrefs()", "Shared preferences fetched");
	}

	@Override
	public void updateFragmentView() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateFragmentView()", "Whole fragments user interface is about to be updated");

		// Update primary SMS and secondary numbers Spinner
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

				// Showing dialog with correct request code
				showAddSmsNumberDialog(AddSmsNumberDialog.ADD_PRIMARY_SMS_NUMBER_DIALOG_REQUEST_CODE);
			}
		});

		// Set listener to removePrimarySmsNumberButton
		removePrimarySmsNumberButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().removePrimarySmsNumberButton.OnClickListener().onClick()", "Remove PRIMARY sms number button pressed");

				// Only show delete dialog if primary SMS numbers exists, else show toast
				if (!primarySmsNumbers.isEmpty()) {
					// Resolve SMS number to be removed
					String primarySmsNumberToBeRemoved = primarySmsNumbers.get(primarySmsNumberSpinner.getSelectedItemPosition());
					showRemoveSmsNumberDialog(RemoveSmsNumberDialog.REMOVE_PRIMARY_SMS_NUMBER_DIALOG_REQUEST_CODE, primarySmsNumberToBeRemoved);
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
				showAddSmsNumberDialog(AddSmsNumberDialog.ADD_SECONDARY_SMS_NUMBER_DIALOG_REQUEST_CODE);
			}
		});

		// Set listener to removeSecondarySmsNumberButton
		removeSecondarySmsNumberButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().removeSecondarySmsNumberButton.OnClickListener().onClick()", "Remove SECONDARY sms number button pressed");

				// Only show delete dialog if secondary SMS numbers exists, else show toast
				if (!secondarySmsNumbers.isEmpty()) {
					String secondarySmsNumberToBeRemoved = secondarySmsNumbers.get(secondarySmsNumberSpinner.getSelectedItemPosition());
					showRemoveSmsNumberDialog(RemoveSmsNumberDialog.REMOVE_SECONDARY_SMS_NUMBER_DIALOG_REQUEST_CODE, secondarySmsNumberToBeRemoved);
				} else {
					Toast.makeText(context, R.string.NO_SECONDARY_NUMBER_EXISTS, Toast.LENGTH_LONG).show();
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().removeSecondarySmsNumberButton.OnClickListener().onClick()", "Cannot build and show dialog because the list of SECONDARY sms numbers are");
				}
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onActivityResult()", "Handling of activity result is about to begin, request code: \"" + Integer.toString(requestCode) + "\" and result code: \"" + Integer.toString(resultCode) + "\"");

		// Only interested in OK results, don't care at all about the others
		if (resultCode == Activity.RESULT_OK) {
			// Boolean indicating if there are duplicates of the primary and secondary SMS listen numbers
			boolean duplicatedNumbers = false;
			// To store the entered SMS number for further handling
			String newSmsNumber = "";

			// Only interested in certain request codes...
			switch (requestCode) {
				case (AddSmsNumberDialog.ADD_PRIMARY_SMS_NUMBER_DIALOG_REQUEST_CODE):
					newSmsNumber = data.getStringExtra(AddSmsNumberDialog.ADD_SMS_NUMBER);

					// If input doesn't exist in the list of secondarySmsNumbers and input isn't empty
					if (!Util.existsIn(newSmsNumber, secondarySmsNumbers) && !newSmsNumber.equals("")) {
						// Iterate through all strings in the list of primarySmsNumbers to check if number already exists
						for (String number : primarySmsNumbers) {
							// If a string in the list is equal with the input then it'sduplicated
							if (number.equalsIgnoreCase(newSmsNumber)) {
								duplicatedNumbers = true;
							}
						}

						// Store input if duplicated numbers is false
						if (!duplicatedNumbers) {
							// Add given input to list
							primarySmsNumbers.add(newSmsNumber);

							// Store to shared preferences
							prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_NUMBERS_KEY, primarySmsNumbers, context);

							// Update affected UI widgets
							updatePrimarySmsNumberSpinner();
							logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onActivityResult()", "New PRIMARY sms number has been stored from user input to the list of PRIMARY sms numbers. New PRIMARY sms number is: \"" + newSmsNumber + "\"");
						} else {
							Toast.makeText(context, R.string.NUMBER_ALREADY_IN_PRIMARY_LIST, Toast.LENGTH_LONG).show();
							logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onActivityResult()", "Given PRIMARY sms number(" + newSmsNumber + ") already exists in the list of PRIMARY sms numbers and therefore cannot be stored. Showing dialog again");

							showAddSmsNumberDialog(AddSmsNumberDialog.ADD_PRIMARY_SMS_NUMBER_DIALOG_REQUEST_CODE);
						}
					} else {
						// Empty input was given
						if (newSmsNumber.equals("")) {
							Toast.makeText(context, R.string.NUMBER_IS_NEEDED, Toast.LENGTH_LONG).show();
							logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onActivityResult()", "Given PRIMARY sms number is empty and therefore cannot be stored. Showing dialog again again");
						} else { // Given primary number exists in the list of secondary numbers
							Toast.makeText(context, R.string.DUPLICATED_NUMBERS, Toast.LENGTH_LONG).show();
							logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onActivityResult()", "Given PRIMARY sms number(" + newSmsNumber + ") already exists in the list of SECONDARY sms numbers and therefore cannot be stored. Showing dialog again");
						}

						showAddSmsNumberDialog(AddSmsNumberDialog.ADD_PRIMARY_SMS_NUMBER_DIALOG_REQUEST_CODE);
					}
					break;
				case (AddSmsNumberDialog.ADD_SECONDARY_SMS_NUMBER_DIALOG_REQUEST_CODE):
					newSmsNumber = data.getStringExtra(AddSmsNumberDialog.ADD_SMS_NUMBER);

					if (!Util.existsIn(newSmsNumber, primarySmsNumbers) && !newSmsNumber.equals("")) {
						for (String number : secondarySmsNumbers) {
							if (number.equalsIgnoreCase(newSmsNumber)) {
								duplicatedNumbers = true;
							}
						}

						if (!duplicatedNumbers) {
							secondarySmsNumbers.add(newSmsNumber);

							// Store to shared preferences
							prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_NUMBERS_KEY, secondarySmsNumbers, context);

							updateSecondarySmsNumberSpinner();
							logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onActivityResult()", "New SECONDARY sms number has been stored from user input to the list of SECONDARY sms numbers. New SECONDARY sms number is: \"" + newSmsNumber + "\"");
						} else {
							Toast.makeText(context, R.string.NUMBER_ALREADY_IN_SECONDARY_LIST, Toast.LENGTH_LONG).show();
							logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onActivityResult()", "Given SECONDARY sms number(" + newSmsNumber + ") already exists in the list of SECONDARY sms numbers and therefore cannot be stored. Showing dialog again");
							showAddSmsNumberDialog(AddSmsNumberDialog.ADD_SECONDARY_SMS_NUMBER_DIALOG_REQUEST_CODE);
						}
					} else {
						if (newSmsNumber.equals("")) {
							Toast.makeText(context, R.string.NUMBER_IS_NEEDED, Toast.LENGTH_LONG).show();
							logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onActivityResult()", "Given SECONDARY sms number is empty and therefore cannot be stored. Showing dialog again");
						} else {
							Toast.makeText(context, R.string.DUPLICATED_NUMBERS, Toast.LENGTH_LONG).show();
							logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onActivityResult()", "Given SECONDARY sms number(" + newSmsNumber + ") already exists in the list of PRIMARY sms numbers and therefore cannot be stored. Showing dialog again");
						}

						showAddSmsNumberDialog(AddSmsNumberDialog.ADD_SECONDARY_SMS_NUMBER_DIALOG_REQUEST_CODE);
					}
					break;
				case (RemoveSmsNumberDialog.REMOVE_PRIMARY_SMS_NUMBER_DIALOG_REQUEST_CODE):
					// Remove SMS number in list that equals the SMS number got from intent data
					primarySmsNumbers.remove(data.getStringExtra(RemoveSmsNumberDialog.REMOVE_SMS_NUMBER));

					// Store to shared preferences
					prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_NUMBERS_KEY, primarySmsNumbers, context);

					updatePrimarySmsNumberSpinner();
					break;
				case (RemoveSmsNumberDialog.REMOVE_SECONDARY_SMS_NUMBER_DIALOG_REQUEST_CODE):
					secondarySmsNumbers.remove(data.getStringExtra(RemoveSmsNumberDialog.REMOVE_SMS_NUMBER));

					prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_NUMBERS_KEY, secondarySmsNumbers, context);

					updateSecondarySmsNumberSpinner();
					break;
				default:
					logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":onActivityResult()", "An unsupported result occurred, result code: \"" + Integer.toString(resultCode) + "\" and request code: \"" + requestCode + "\"");
			}
		}
	}

	/**
	 * Convenience method to create a new instance of {@link AddSmsNumberDialog} and show it.
	 * 
	 * @param requestCode
	 *            Request code for the created <code>AddSmsNumberDialog</code>.
	 */
	private void showAddSmsNumberDialog(int requestCode) {
		AddSmsNumberDialog dialog = new AddSmsNumberDialog();
		dialog.setTargetFragment(SmsSettingsFragment.this, requestCode);
		dialog.show(getFragmentManager(), AddSmsNumberDialog.ADD_SMS_NUMBER_DIALOG_TAG);
	}

	/**
	 * Convenience method to create a new instance of {@link RemoveSmsNumberDialog} and show it.
	 * 
	 * @param requestCode
	 *            Request code for the created <code>RemoveSmsNumberDialog</code>.
	 * @param smsNumber
	 *            SMS number to be removed.
	 */
	private void showRemoveSmsNumberDialog(int requestCode, String smsNumber) {
		// Must pass over SMS number to be removed
		Bundle arguments = new Bundle();
		arguments.putString(RemoveSmsNumberDialog.REMOVE_SMS_NUMBER, smsNumber);

		// Create Dialog as usual, but put arguments in it also
		RemoveSmsNumberDialog dialog = new RemoveSmsNumberDialog();
		dialog.setArguments(arguments);
		dialog.setTargetFragment(SmsSettingsFragment.this, requestCode);
		dialog.show(getFragmentManager(), RemoveSmsNumberDialog.REMOVE_SMS_NUMBER_DIALOG_TAG);
	}

	/**
	 * To update primary SMS number <code>Spinner</code> with correct values.
	 * 
	 * @see #updateSecondarySmsNumberSpinner()
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
	 * To update secondary SMS number <code>Spinner</code> with correct values.
	 * 
	 * @see #updatePrimarySmsNumberSpinner()
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
