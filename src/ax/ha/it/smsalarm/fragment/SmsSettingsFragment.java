/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.activity.SmsAlarm;
import ax.ha.it.smsalarm.fragment.dialog.AddSmsNumberDialog;
import ax.ha.it.smsalarm.fragment.dialog.EditSmsNumberDialog;
import ax.ha.it.smsalarm.fragment.dialog.RemoveSmsNumberDialog;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.DataType;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.PrefKey;
import ax.ha.it.smsalarm.util.InitializableString;
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

	// To handle shared preferences
	private final SharedPreferencesHandler prefHandler = SharedPreferencesHandler.getInstance();

	// Must have the application context
	private Context context;

	// The Buttons...
	private Button addPrimarySmsNumberButton;
	private Button editPrimarySmsNumberButton;
	private Button removePrimarySmsNumberButton;
	private Button addSecondarySmsNumberButton;
	private Button editSecondarySmsNumberButton;
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
		// Just empty...
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set context here, it's safe because this fragment has been attached to its container, hence we have access to context
		context = getActivity();
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
		// Finding Button views
		addPrimarySmsNumberButton = (Button) view.findViewById(R.id.addPrimarySmsNumber_btn);
		editPrimarySmsNumberButton = (Button) view.findViewById(R.id.editPrimarySmsNumber_btn);
		removePrimarySmsNumberButton = (Button) view.findViewById(R.id.deletePrimarySmsNumber_btn);
		addSecondarySmsNumberButton = (Button) view.findViewById(R.id.addSecondarySmsNumber_btn);
		editSecondarySmsNumberButton = (Button) view.findViewById(R.id.editSecondarySmsNumber_btn);
		removeSecondarySmsNumberButton = (Button) view.findViewById(R.id.deleteSecondarySmsNumber_btn);

		// Finding Spinner views
		primarySmsNumberSpinner = (Spinner) view.findViewById(R.id.primarySmsNumberSpinner_sp);
		secondarySmsNumberSpinner = (Spinner) view.findViewById(R.id.secondarySmsNumberSpinner_sp);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void fetchSharedPrefs() {
		primarySmsNumbers = (List<String>) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_NUMBERS_KEY, DataType.LIST, context);
		secondarySmsNumbers = (List<String>) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_NUMBERS_KEY, DataType.LIST, context);
	}

	@Override
	public void updateFragmentView() {
		// Update primary SMS and secondary numbers Spinner
		updatePrimarySmsNumberSpinner();
		updateSecondarySmsNumberSpinner();
	}

	@Override
	public void setListeners() {
		// Set listener to Add Primary SMS Number Button
		addPrimarySmsNumberButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Showing dialog with correct request code
				showAddSmsNumberDialog(AddSmsNumberDialog.ADD_PRIMARY_SMS_NUMBER_DIALOG_REQUEST_CODE);
			}
		});

		// Set listener to Edit Primary SMS Number Button
		editPrimarySmsNumberButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Resolve primary SMS number to be edited
				String primarySmsNumberToBeEdited = primarySmsNumbers.get(primarySmsNumberSpinner.getSelectedItemPosition());
				showEditSmsNumberDialog(EditSmsNumberDialog.EDIT_PRIMARY_SMS_NUMBER_DIALOG_REQUEST_CODE, primarySmsNumberToBeEdited);
			}
		});

		// Set listener to Remove Primary SMS Number Button
		removePrimarySmsNumberButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Only show delete dialog if primary SMS numbers exists, else show toast
				if (!primarySmsNumbers.isEmpty()) {
					// Resolve SMS number to be removed
					String primarySmsNumberToBeRemoved = primarySmsNumbers.get(primarySmsNumberSpinner.getSelectedItemPosition());
					showRemoveSmsNumberDialog(RemoveSmsNumberDialog.REMOVE_PRIMARY_SMS_NUMBER_DIALOG_REQUEST_CODE, primarySmsNumberToBeRemoved);
				} else {
					Toast.makeText(context, R.string.NO_PRIMARY_NUMBER_EXISTS, Toast.LENGTH_LONG).show();
				}
			}
		});

		// Set listener to Add Secondary SMS Number Button
		addSecondarySmsNumberButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showAddSmsNumberDialog(AddSmsNumberDialog.ADD_SECONDARY_SMS_NUMBER_DIALOG_REQUEST_CODE);
			}
		});

		// Set listener to Edit Secondary SMS Number Button
		editSecondarySmsNumberButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String secondarySmsNumberToBeEdited = secondarySmsNumbers.get(secondarySmsNumberSpinner.getSelectedItemPosition());
				showEditSmsNumberDialog(EditSmsNumberDialog.EDIT_SECONDARY_SMS_NUMBER_DIALOG_REQUEST_CODE, secondarySmsNumberToBeEdited);
			}
		});

		// Set listener to Remove Secondary SMS Number Button
		removeSecondarySmsNumberButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Only show delete dialog if secondary SMS numbers exists, else show toast
				if (!secondarySmsNumbers.isEmpty()) {
					String secondarySmsNumberToBeRemoved = secondarySmsNumbers.get(secondarySmsNumberSpinner.getSelectedItemPosition());
					showRemoveSmsNumberDialog(RemoveSmsNumberDialog.REMOVE_SECONDARY_SMS_NUMBER_DIALOG_REQUEST_CODE, secondarySmsNumberToBeRemoved);
				} else {
					Toast.makeText(context, R.string.NO_SECONDARY_NUMBER_EXISTS, Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Only interested in OK results, don't care at all about the others
		if (resultCode == Activity.RESULT_OK) {
			// To store the entered SMS number for further handling
			String newSmsNumber = "";

			// To store the edited SMS phone number and the phone number to be replaced for further handling
			InitializableString initializableString;

			// Only interested in certain request codes...
			switch (requestCode) {
				case (AddSmsNumberDialog.ADD_PRIMARY_SMS_NUMBER_DIALOG_REQUEST_CODE):
					newSmsNumber = data.getStringExtra(AddSmsNumberDialog.ADD_SMS_NUMBER);

					// If input doesn't exist in the list of secondarySmsNumbers and input isn't empty
					if (!Util.existsInIgnoreCases(newSmsNumber, secondarySmsNumbers) && !"".equals(newSmsNumber)) {
						// If the new number exists in the list of primary SMS numbers then it's duplicated
						if (!Util.existsInIgnoreCases(newSmsNumber, primarySmsNumbers)) {
							// Add given input to list
							primarySmsNumbers.add(newSmsNumber);

							// Store to shared preferences
							prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_NUMBERS_KEY, primarySmsNumbers, context);

							// Update affected UI widgets
							updatePrimarySmsNumberSpinner();
						} else {
							Toast.makeText(context, R.string.TOAST_PHONE_NUMBER_ALREADY_IN_PRIMARY_LIST, Toast.LENGTH_LONG).show();

							// Showing dialog again as the entered number already exists
							showAddSmsNumberDialog(AddSmsNumberDialog.ADD_PRIMARY_SMS_NUMBER_DIALOG_REQUEST_CODE);
						}
					} else {
						// Empty input was given
						if ("".equals(newSmsNumber)) {
							Toast.makeText(context, R.string.TOAST_PHONE_NUMBER_MISSING, Toast.LENGTH_LONG).show();
						} else { // Given primary number exists in the list of secondary numbers
							Toast.makeText(context, R.string.TOAST_DUPLICATED_PHONE_NUMBERS, Toast.LENGTH_LONG).show();
						}

						showAddSmsNumberDialog(AddSmsNumberDialog.ADD_PRIMARY_SMS_NUMBER_DIALOG_REQUEST_CODE);
					}
					break;
				case (AddSmsNumberDialog.ADD_SECONDARY_SMS_NUMBER_DIALOG_REQUEST_CODE):
					newSmsNumber = data.getStringExtra(AddSmsNumberDialog.ADD_SMS_NUMBER);

					if (!Util.existsInIgnoreCases(newSmsNumber, primarySmsNumbers) && !"".equals(newSmsNumber)) {
						if (!Util.existsInIgnoreCases(newSmsNumber, secondarySmsNumbers)) {
							secondarySmsNumbers.add(newSmsNumber);
							prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_NUMBERS_KEY, secondarySmsNumbers, context);
							updateSecondarySmsNumberSpinner();
						} else {
							Toast.makeText(context, R.string.TOAST_PHONE_NUMBER_ALREADY_IN_SECONDARY_LIST, Toast.LENGTH_LONG).show();
							showAddSmsNumberDialog(AddSmsNumberDialog.ADD_SECONDARY_SMS_NUMBER_DIALOG_REQUEST_CODE);
						}
					} else {
						if ("".equals(newSmsNumber)) {
							Toast.makeText(context, R.string.TOAST_PHONE_NUMBER_MISSING, Toast.LENGTH_LONG).show();
						} else {
							Toast.makeText(context, R.string.TOAST_DUPLICATED_PHONE_NUMBERS, Toast.LENGTH_LONG).show();
						}

						showAddSmsNumberDialog(AddSmsNumberDialog.ADD_SECONDARY_SMS_NUMBER_DIALOG_REQUEST_CODE);
					}
					break;
				case (EditSmsNumberDialog.EDIT_PRIMARY_SMS_NUMBER_DIALOG_REQUEST_CODE):
					initializableString = (InitializableString) data.getParcelableExtra(EditSmsNumberDialog.EDIT_SMS_NUMBER);

					if (!"".equals(initializableString.getValue()) && !Util.existsInIgnoreCases(initializableString.getValue(), secondarySmsNumbers)) {
						if (!Util.existsInIgnoreCases(initializableString.getValue(), primarySmsNumbers)) {
							// Replace existing element in list of primary SMS phone numbers with the new one
							Collections.replaceAll(primarySmsNumbers, initializableString.getInitialValue(), initializableString.getValue());

							// Store to shared preferences
							prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_NUMBERS_KEY, primarySmsNumbers, context);

							// Update affected UI widgets
							updatePrimarySmsNumberSpinner();
						} else {
							Toast.makeText(context, R.string.TOAST_PHONE_NUMBER_ALREADY_IN_PRIMARY_LIST, Toast.LENGTH_LONG).show();
							showEditSmsNumberDialog(EditSmsNumberDialog.EDIT_PRIMARY_SMS_NUMBER_DIALOG_REQUEST_CODE, initializableString.getInitialValue());
						}
					} else {
						// Empty input was given
						if ("".equals(initializableString.getValue())) {
							Toast.makeText(context, R.string.TOAST_PHONE_NUMBER_MISSING, Toast.LENGTH_LONG).show();
						} else { // Given primary number exists in the list of secondary numbers
							Toast.makeText(context, R.string.TOAST_DUPLICATED_PHONE_NUMBERS, Toast.LENGTH_LONG).show();
						}

						showEditSmsNumberDialog(EditSmsNumberDialog.EDIT_PRIMARY_SMS_NUMBER_DIALOG_REQUEST_CODE, initializableString.getInitialValue());
					}
					break;
				case (EditSmsNumberDialog.EDIT_SECONDARY_SMS_NUMBER_DIALOG_REQUEST_CODE):
					initializableString = (InitializableString) data.getParcelableExtra(EditSmsNumberDialog.EDIT_SMS_NUMBER);

					if (!"".equals(initializableString.getValue()) && !Util.existsInIgnoreCases(initializableString.getValue(), primarySmsNumbers)) {
						if (!Util.existsInIgnoreCases(initializableString.getValue(), secondarySmsNumbers)) {
							Collections.replaceAll(secondarySmsNumbers, initializableString.getInitialValue(), initializableString.getValue());
							prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_NUMBERS_KEY, secondarySmsNumbers, context);
							updateSecondarySmsNumberSpinner();
						} else {
							Toast.makeText(context, R.string.TOAST_PHONE_NUMBER_ALREADY_IN_SECONDARY_LIST, Toast.LENGTH_LONG).show();
							showEditSmsNumberDialog(EditSmsNumberDialog.EDIT_SECONDARY_SMS_NUMBER_DIALOG_REQUEST_CODE, initializableString.getInitialValue());
						}
					} else {
						if ("".equals(initializableString.getValue())) {
							Toast.makeText(context, R.string.TOAST_PHONE_NUMBER_MISSING, Toast.LENGTH_LONG).show();
						} else {
							Toast.makeText(context, R.string.TOAST_DUPLICATED_PHONE_NUMBERS, Toast.LENGTH_LONG).show();
						}

						showEditSmsNumberDialog(EditSmsNumberDialog.EDIT_SECONDARY_SMS_NUMBER_DIALOG_REQUEST_CODE, initializableString.getInitialValue());
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
					if (SmsAlarm.DEBUG) {
						Log.e(LOG_TAG + ":onActivityResult()", "An unsupported result occurred, result code: \"" + resultCode + "\" and request code: \"" + requestCode + "\"");
					}
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
	 * Convenience method to create a new instance of {@link EditSmsNumberDialog} and show it.
	 * 
	 * @param requestCode
	 *            Request code for the created <code>EditSmsNumberDialog</code>.
	 * @param smsNumberToBeEdited
	 *            SMS number to be edited.
	 */
	private void showEditSmsNumberDialog(int requestCode, String smsNumberToBeEdited) {
		EditSmsNumberDialog dialog = EditSmsNumberDialog.newInstance(new InitializableString(smsNumberToBeEdited));
		dialog.setTargetFragment(SmsSettingsFragment.this, requestCode);
		dialog.show(getFragmentManager(), EditSmsNumberDialog.EDIT_SMS_NUMBER_DIALOG_TAG);
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
		// Create a new instance of RemoveSmsNumberDialog prepared with correct phone number
		RemoveSmsNumberDialog dialog = RemoveSmsNumberDialog.newInstance(smsNumber);
		dialog.setTargetFragment(SmsSettingsFragment.this, requestCode);
		dialog.show(getFragmentManager(), RemoveSmsNumberDialog.REMOVE_SMS_NUMBER_DIALOG_TAG);
	}

	/**
	 * To update primary SMS number {@link Spinner} with correct values.
	 * 
	 * @see #updateSecondarySmsNumberSpinner()
	 */
	private void updatePrimarySmsNumberSpinner() {
		// Check if there are primary SMS numbers and build up a proper spinner according to that information
		if (!primarySmsNumbers.isEmpty()) {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, primarySmsNumbers);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			primarySmsNumberSpinner.setAdapter(adapter);
		} else {
			// Only add item to list if it's empty
			if (emptySmsNumbers.isEmpty()) {
				emptySmsNumbers.add(getString(R.string.ADD_PHONE_NUMBER_HINT));
			}

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, emptySmsNumbers);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			primarySmsNumberSpinner.setAdapter(adapter);
		}
	}

	/**
	 * To update secondary SMS number {@link Spinner}> with correct values.
	 * 
	 * @see #updatePrimarySmsNumberSpinner()
	 */
	private void updateSecondarySmsNumberSpinner() {
		// Check if there are secondary sms numbers and build up a proper spinner according to that information
		if (!secondarySmsNumbers.isEmpty()) {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, secondarySmsNumbers);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			secondarySmsNumberSpinner.setAdapter(adapter);
		} else {
			// Only add item to list if it's empty
			if (emptySmsNumbers.isEmpty()) {
				emptySmsNumbers.add(getString(R.string.ADD_PHONE_NUMBER_HINT));
			}

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, emptySmsNumbers);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			secondarySmsNumberSpinner.setAdapter(adapter);
		}
	}
}
