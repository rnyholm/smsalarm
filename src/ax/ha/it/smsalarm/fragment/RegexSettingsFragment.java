/**
 * Copyright (c) 2015 Robert Nyholm. All rights reserved.
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
import ax.ha.it.smsalarm.application.SmsAlarmApplication.GoogleAnalyticsHandler;
import ax.ha.it.smsalarm.fragment.dialog.AddRegexDialog;
import ax.ha.it.smsalarm.fragment.dialog.EditRegexDialog;
import ax.ha.it.smsalarm.fragment.dialog.RemoveRegexDialog;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.DataType;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.PrefKey;
import ax.ha.it.smsalarm.util.InitializableString;
import ax.ha.it.smsalarm.util.Utils;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * {@link Fragment} containing all the views and user interface widgets for the <b><i>Regular Expression Settings</i></b>. <code>Fragment</code> does
 * also contain all logic for the widgets.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class RegexSettingsFragment extends SherlockFragment implements ApplicationFragment {
	private static final String LOG_TAG = RegexSettingsFragment.class.getSimpleName();

	// To handle shared preferences
	private final SharedPreferencesHandler prefHandler = SharedPreferencesHandler.getInstance();

	// Must have the application context
	private Context context;

	// The Buttons...
	private Button addPrimaryRegexButton;
	private Button editPrimaryRegexButton;
	private Button removePrimaryRegexButton;
	private Button addSecondaryRegexButton;
	private Button editSecondaryRegexButton;
	private Button removeSecondaryRegexButton;

	// ... and Spinners
	private Spinner primaryRegexSpinner;
	private Spinner secondaryRegexSpinner;

	// List of strings containing primary- and secondary regular expressions
	private List<String> primaryRegexs = new ArrayList<String>();
	private List<String> secondaryRegexs = new ArrayList<String>();
	private final List<String> emptyRegexs = new ArrayList<String>(); // <-- A "dummy" list just containing one element, on string

	/**
	 * To create a new instance of {@link RegexSettingsFragment}.
	 */
	public RegexSettingsFragment() {
		// Just empty...
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set context here, it's safe because this fragment has been attached to its container, hence we have access to context
		context = getActivity();
	}

	@Override
	public void onResume() {
		super.onResume();

		// Set correct screen name and send hit to Google Analytics
		GoogleAnalyticsHandler.setScreenNameAndSendScreenViewHit(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.regex_settings, container, false);

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
		addPrimaryRegexButton = (Button) view.findViewById(R.id.addPrimaryRegex_btn);
		editPrimaryRegexButton = (Button) view.findViewById(R.id.editPrimaryRegex_btn);
		removePrimaryRegexButton = (Button) view.findViewById(R.id.deletePrimaryRegex_btn);
		addSecondaryRegexButton = (Button) view.findViewById(R.id.addSecondaryRegex_btn);
		editSecondaryRegexButton = (Button) view.findViewById(R.id.editSecondaryRegex_btn);
		removeSecondaryRegexButton = (Button) view.findViewById(R.id.deleteSecondaryRegex_btn);

		// Finding Spinner views
		primaryRegexSpinner = (Spinner) view.findViewById(R.id.primaryRegexSpinner_sp);
		secondaryRegexSpinner = (Spinner) view.findViewById(R.id.secondaryRegexSpinner_sp);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void fetchSharedPrefs() {
		primaryRegexs = (List<String>) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_REGULAR_EXPRESSIONS_KEY, DataType.LIST, context);
		secondaryRegexs = (List<String>) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_REGULAR_EXPRESSIONS_KEY, DataType.LIST, context);
	}

	@Override
	public void updateFragmentView() {
		updatePrimaryRegexSpinner();
		updateSecondaryRegexSpinner();
	}

	@Override
	public void setListeners() {
		// Set listener to Add Primary Regular Expression Button
		addPrimaryRegexButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Showing dialog with correct request code
				showAddRegexDialog(AddRegexDialog.ADD_PRIMARY_REGEX_DIALOG_REQUEST_CODE);
			}
		});

		// Set listener to Edit Primary Regular Expression Button
		editPrimaryRegexButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Resolve regular expression to be edited
				String primaryRegexToBeEdited = primaryRegexs.get(primaryRegexSpinner.getSelectedItemPosition());
				showEditRegexDialog(EditRegexDialog.EDIT_PRIMARY_REGEX_DIALOG_REQUEST_CODE, primaryRegexToBeEdited);
			}
		});

		// Set listener to Remove Primary Regular Expression Button
		removePrimaryRegexButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Resolve regular expression to be removed
				String primaryRegexToBeRemoved = primaryRegexs.get(primaryRegexSpinner.getSelectedItemPosition());
				showRemoveRegexDialog(RemoveRegexDialog.REMOVE_PRIMARY_REGEX_DIALOG_REQUEST_CODE, primaryRegexToBeRemoved);
			}
		});

		// Set listener to Add Secondary Regular Expression Button
		addSecondaryRegexButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showAddRegexDialog(AddRegexDialog.ADD_SECONDARY_REGEX_DIALOG_REQUEST_CODE);
			}
		});

		// Set listener to Edit Secondary Regular Expression Button
		editSecondaryRegexButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Resolve regular expression to be edited
				String secondaryRegexToBeEdited = secondaryRegexs.get(secondaryRegexSpinner.getSelectedItemPosition());
				showEditRegexDialog(EditRegexDialog.EDIT_SECONDARY_REGEX_DIALOG_REQUEST_CODE, secondaryRegexToBeEdited);
			}
		});

		// Set listener to Remove Secondary Regular Expression Button
		removeSecondaryRegexButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String secondaryRegexToBeRemoved = secondaryRegexs.get(secondaryRegexSpinner.getSelectedItemPosition());
				showRemoveRegexDialog(RemoveRegexDialog.REMOVE_SECONDARY_REGEX_DIALOG_REQUEST_CODE, secondaryRegexToBeRemoved);
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Only interested in OK results, don't care at all about the others
		if (resultCode == Activity.RESULT_OK) {
			// To store the entered regular expression for further handling
			String newRegex = "";

			// To store the edited regular expression and the regular expression to be replaced for further handling
			InitializableString initializableString;

			// Only interested in certain request codes...
			switch (requestCode) {
				case (AddRegexDialog.ADD_PRIMARY_REGEX_DIALOG_REQUEST_CODE):
					newRegex = data.getStringExtra(AddRegexDialog.ADD_REGEX);

					// If input doesn't exist in the list of secondaryRegexs and input isn't empty
					if (!Utils.existsInConsiderCases(newRegex, secondaryRegexs) && !"".equals(newRegex)) {
						// Store input if the list of primaryRegexs doesn't contain the new regular expression
						if (!Utils.existsInConsiderCases(newRegex, primaryRegexs)) {
							// Add given input to list
							primaryRegexs.add(newRegex);

							// Store to shared preferences
							prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_REGULAR_EXPRESSIONS_KEY, primaryRegexs, context);

							// Update affected UI widgets
							updatePrimaryRegexSpinner();
						} else {
							Toast.makeText(context, R.string.TOAST_REGEX_ALREADY_IN_PRIMARY_LIST, Toast.LENGTH_LONG).show();

							// Showing dialog again with correct request code
							showAddRegexDialog(AddRegexDialog.ADD_PRIMARY_REGEX_DIALOG_REQUEST_CODE);
						}
					} else {
						// Empty input was given
						if ("".equals(newRegex)) {
							Toast.makeText(context, R.string.TOAST_REGEX_MISSING, Toast.LENGTH_LONG).show();
						} else { // Given primary regular expression exists in the list of secondary regular expressions
							Toast.makeText(context, R.string.TOAST_DUPLICATED_REGEX, Toast.LENGTH_LONG).show();
						}

						showAddRegexDialog(AddRegexDialog.ADD_PRIMARY_REGEX_DIALOG_REQUEST_CODE);
					}
					break;
				case (AddRegexDialog.ADD_SECONDARY_REGEX_DIALOG_REQUEST_CODE):
					newRegex = data.getStringExtra(AddRegexDialog.ADD_REGEX);

					if (!Utils.existsInConsiderCases(newRegex, primaryRegexs) && !"".equals(newRegex)) {
						if (!Utils.existsInConsiderCases(newRegex, secondaryRegexs)) {
							secondaryRegexs.add(newRegex);
							prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_REGULAR_EXPRESSIONS_KEY, secondaryRegexs, context);
							updateSecondaryRegexSpinner();
						} else {
							Toast.makeText(context, R.string.TOAST_REGEX_ALREADY_IN_SECONDARY_LIST, Toast.LENGTH_LONG).show();
							showAddRegexDialog(AddRegexDialog.ADD_SECONDARY_REGEX_DIALOG_REQUEST_CODE);
						}
					} else {
						if ("".equals(newRegex)) {
							Toast.makeText(context, R.string.TOAST_REGEX_MISSING, Toast.LENGTH_LONG).show();
						} else {
							Toast.makeText(context, R.string.TOAST_DUPLICATED_REGEX, Toast.LENGTH_LONG).show();
						}

						showAddRegexDialog(AddRegexDialog.ADD_SECONDARY_REGEX_DIALOG_REQUEST_CODE);
					}
					break;
				case (EditRegexDialog.EDIT_PRIMARY_REGEX_DIALOG_REQUEST_CODE):
					initializableString = (InitializableString) data.getParcelableExtra(EditRegexDialog.EDIT_REGEX);

					if (!"".equals(initializableString.getValue()) && !Utils.existsInConsiderCases(initializableString.getValue(), secondaryRegexs)) {
						if (!Utils.existsInConsiderCases(initializableString.getValue(), primaryRegexs)) {
							// Replace existing element in list of primary regular expressions with the new one
							Collections.replaceAll(primaryRegexs, initializableString.getInitialValue(), initializableString.getValue());

							// Store to shared preferences
							prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_REGULAR_EXPRESSIONS_KEY, primaryRegexs, context);

							// Update affected UI widgets
							updatePrimaryRegexSpinner();
						} else {
							Toast.makeText(context, R.string.TOAST_REGEX_ALREADY_IN_PRIMARY_LIST, Toast.LENGTH_LONG).show();
							showEditRegexDialog(EditRegexDialog.EDIT_PRIMARY_REGEX_DIALOG_REQUEST_CODE, initializableString.getInitialValue());
						}
					} else {
						// Empty input was given
						if ("".equals(initializableString.getValue())) {
							Toast.makeText(context, R.string.TOAST_REGEX_MISSING, Toast.LENGTH_LONG).show();
						} else { // Given primary regular expression exists in the list of secondary regular expressions
							Toast.makeText(context, R.string.TOAST_DUPLICATED_REGEX, Toast.LENGTH_LONG).show();
						}

						showEditRegexDialog(EditRegexDialog.EDIT_PRIMARY_REGEX_DIALOG_REQUEST_CODE, initializableString.getInitialValue());
					}
					break;
				case (EditRegexDialog.EDIT_SECONDARY_REGEX_DIALOG_REQUEST_CODE):
					initializableString = (InitializableString) data.getParcelableExtra(EditRegexDialog.EDIT_REGEX);

					if (!"".equals(initializableString.getValue()) && !Utils.existsInConsiderCases(initializableString.getValue(), primaryRegexs)) {
						if (!Utils.existsInConsiderCases(initializableString.getValue(), secondaryRegexs)) {
							Collections.replaceAll(secondaryRegexs, initializableString.getInitialValue(), initializableString.getValue());
							prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_REGULAR_EXPRESSIONS_KEY, secondaryRegexs, context);
							updateSecondaryRegexSpinner();
						} else {
							Toast.makeText(context, R.string.TOAST_REGEX_ALREADY_IN_SECONDARY_LIST, Toast.LENGTH_LONG).show();
							showEditRegexDialog(EditRegexDialog.EDIT_SECONDARY_REGEX_DIALOG_REQUEST_CODE, initializableString.getInitialValue());
						}
					} else {
						if ("".equals(initializableString.getValue())) {
							Toast.makeText(context, R.string.TOAST_REGEX_MISSING, Toast.LENGTH_LONG).show();
						} else {
							Toast.makeText(context, R.string.TOAST_DUPLICATED_REGEX, Toast.LENGTH_LONG).show();
						}

						showEditRegexDialog(EditRegexDialog.EDIT_SECONDARY_REGEX_DIALOG_REQUEST_CODE, initializableString.getInitialValue());
					}
					break;
				case (RemoveRegexDialog.REMOVE_PRIMARY_REGEX_DIALOG_REQUEST_CODE):
					// Remove regular expression in list that equals the regular expression got from intent data
					primaryRegexs.remove(data.getStringExtra(RemoveRegexDialog.REMOVE_REGEX));
					prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_REGULAR_EXPRESSIONS_KEY, primaryRegexs, context);
					updatePrimaryRegexSpinner();
					break;
				case (RemoveRegexDialog.REMOVE_SECONDARY_REGEX_DIALOG_REQUEST_CODE):
					secondaryRegexs.remove(data.getStringExtra(RemoveRegexDialog.REMOVE_REGEX));
					prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_REGULAR_EXPRESSIONS_KEY, secondaryRegexs, context);
					updateSecondaryRegexSpinner();
					break;
				default:
					Log.e(LOG_TAG + ":onActivityResult()", "An unsupported result occurred, result code: \"" + resultCode + "\" and request code: \"" + requestCode + "\"");
			}
		}
	}

	/**
	 * Convenience method to create a new instance of {@link AddRegexDialog} and show it.
	 * 
	 * @param requestCode
	 *            Request code for the created <code>AddRegexDialog</code>.
	 */
	private void showAddRegexDialog(int requestCode) {
		AddRegexDialog dialog = new AddRegexDialog();
		dialog.setTargetFragment(RegexSettingsFragment.this, requestCode);
		dialog.show(getFragmentManager(), AddRegexDialog.ADD_REGEX_DIALOG_TAG);
	}

	/**
	 * Convenience method to create a new instance of {@link EditRegexDialog} and show it.
	 * 
	 * @param requestCode
	 *            Request code for the created <code>EditRegexDialog</code>.
	 * @param regexToBeEdited
	 *            Regular expression to be edited.
	 */
	private void showEditRegexDialog(int requestCode, String regexToBeEdited) {
		EditRegexDialog dialog = EditRegexDialog.newInstance(new InitializableString(regexToBeEdited));
		dialog.setTargetFragment(RegexSettingsFragment.this, requestCode);
		dialog.show(getFragmentManager(), EditRegexDialog.EDIT_REGEX_DIALOG_TAG);
	}

	/**
	 * Convenience method to create a new instance of {@link RemoveRegexDialog} and show it.
	 * 
	 * @param requestCode
	 *            Request code for the created <code>RemoveRegexDialog</code>.
	 * @param regexToBeRemoved
	 *            Regular expression to be removed.
	 */
	private void showRemoveRegexDialog(int requestCode, String regexToBeRemoved) {
		// Create a new instance of RemoveRegexDialog prepared with correct regular expression
		RemoveRegexDialog dialog = RemoveRegexDialog.newInstance(regexToBeRemoved);
		dialog.setTargetFragment(RegexSettingsFragment.this, requestCode);
		dialog.show(getFragmentManager(), RemoveRegexDialog.REMOVE_REGEX_DIALOG_TAG);
	}

	/**
	 * To update primary regular expressions <code>Spinner</code> with correct values.
	 * 
	 * @see #updateSecondaryRegexSpinner()
	 */
	private void updatePrimaryRegexSpinner() {
		// Check if there are primary regular expressions and build up a proper spinner according to that information
		if (!primaryRegexs.isEmpty()) {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, primaryRegexs);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			primaryRegexSpinner.setAdapter(adapter);
		} else {
			// Only add item to list if it's empty
			if (emptyRegexs.isEmpty()) {
				emptyRegexs.add(getString(R.string.ADD_REGEX_HINT));
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, emptyRegexs);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			primaryRegexSpinner.setAdapter(adapter);
		}

		updateButtons();
	}

	/**
	 * To update secondary regular expressions {@link Spinner} with correct values.
	 * 
	 * @see #updatePrimaryRegexSpinner()
	 */
	private void updateSecondaryRegexSpinner() {
		if (!secondaryRegexs.isEmpty()) {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, secondaryRegexs);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			secondaryRegexSpinner.setAdapter(adapter);
		} else {
			// Only add item to list if it's empty
			if (emptyRegexs.isEmpty()) {
				emptyRegexs.add(getString(R.string.ADD_REGEX_HINT));
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, emptyRegexs);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			secondaryRegexSpinner.setAdapter(adapter);
		}

		updateButtons();
	}

	/**
	 * To update buttons within this fragment, mainly it enables or disables the <b><i>Edit</i></b> and <b><i>Remove</i></b> buttons.
	 */
	private void updateButtons() {
		// Toggle the buttons depending whether or not it's corresponding list got any elements within it
		editPrimaryRegexButton.setEnabled(!primaryRegexs.isEmpty());
		removePrimaryRegexButton.setEnabled(!primaryRegexs.isEmpty());

		editSecondaryRegexButton.setEnabled(!secondaryRegexs.isEmpty());
		removeSecondaryRegexButton.setEnabled(!secondaryRegexs.isEmpty());
	}
}