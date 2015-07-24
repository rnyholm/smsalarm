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
import ax.ha.it.smsalarm.fragment.dialog.AddFreeTextDialog;
import ax.ha.it.smsalarm.fragment.dialog.EditFreeTextDialog;
import ax.ha.it.smsalarm.fragment.dialog.RemoveFreeTextDialog;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.DataType;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.PrefKey;
import ax.ha.it.smsalarm.util.InitializableString;
import ax.ha.it.smsalarm.util.Util;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * {@link Fragment} containing all the views and user interface widgets for the <b><i>Free Text Settings</i></b>. <code>Fragment</code> does also
 * contain all logic for the widgets.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class FreeTextSettingsFragment extends SherlockFragment implements ApplicationFragment {
	private static final String LOG_TAG = FreeTextSettingsFragment.class.getSimpleName();

	// To handle shared preferences
	private final SharedPreferencesHandler prefHandler = SharedPreferencesHandler.getInstance();

	// Must have the application context
	private Context context;

	// The Buttons...
	private Button addPrimaryFreeTextButton;
	private Button editPrimaryFreeTextButton;
	private Button removePrimaryFreeTextButton;
	private Button addSecondaryFreeTextButton;
	private Button editSecondaryFreeTextButton;
	private Button removeSecondaryFreeTextButton;

	// ... and Spinners
	private Spinner primaryFreeTextSpinner;
	private Spinner secondaryFreeTextSpinner;

	// List of strings containing primary- and secondary free texts
	private List<String> primaryFreeTexts = new ArrayList<String>();
	private List<String> secondaryFreeTexts = new ArrayList<String>();
	private final List<String> emptyFreeTexts = new ArrayList<String>(); // <-- A "dummy" list just containing one element, on string

	/**
	 * To create a new instance of {@link FreeTextSettingsFragment}.
	 */
	public FreeTextSettingsFragment() {
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
		View view = inflater.inflate(R.layout.free_text_settings, container, false);

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
		addPrimaryFreeTextButton = (Button) view.findViewById(R.id.addPrimaryFreeText_btn);
		editPrimaryFreeTextButton = (Button) view.findViewById(R.id.editPrimaryFreeText_btn);
		removePrimaryFreeTextButton = (Button) view.findViewById(R.id.deletePrimaryFreeText_btn);
		addSecondaryFreeTextButton = (Button) view.findViewById(R.id.addSecondaryFreeText_btn);
		editSecondaryFreeTextButton = (Button) view.findViewById(R.id.editSecondaryFreeText_btn);
		removeSecondaryFreeTextButton = (Button) view.findViewById(R.id.deleteSecondaryFreeText_btn);

		// Finding Spinner views
		primaryFreeTextSpinner = (Spinner) view.findViewById(R.id.primaryFreeTextSpinner_sp);
		secondaryFreeTextSpinner = (Spinner) view.findViewById(R.id.secondaryFreeTextSpinner_sp);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void fetchSharedPrefs() {
		primaryFreeTexts = (List<String>) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_FREE_TEXTS_KEY, DataType.LIST, context);
		secondaryFreeTexts = (List<String>) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_FREE_TEXTS_KEY, DataType.LIST, context);
	}

	@Override
	public void updateFragmentView() {
		updatePrimaryFreeTextSpinner();
		updateSecondaryFreeTextSpinner();
	}

	@Override
	public void setListeners() {
		// Set listener to Add Primary Free Text Button
		addPrimaryFreeTextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Showing dialog with correct request code
				showAddFreeTextDialog(AddFreeTextDialog.ADD_PRIMARY_FREE_TEXT_DIALOG_REQUEST_CODE);
			}
		});

		// Set listener to Edit Primary Free Text Button
		editPrimaryFreeTextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Resolve free text to be edited
				String primaryFreeTextToBeEdited = primaryFreeTexts.get(primaryFreeTextSpinner.getSelectedItemPosition());
				showEditFreeTextDialog(EditFreeTextDialog.EDIT_PRIMARY_FREE_TEXT_DIALOG_REQUEST_CODE, primaryFreeTextToBeEdited);
			}
		});

		// Set listener to Remove Primary Free Text Button
		removePrimaryFreeTextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Resolve free text to be removed
				String primaryFreeTextToBeRemoved = primaryFreeTexts.get(primaryFreeTextSpinner.getSelectedItemPosition());
				showRemoveFreeTextDialog(RemoveFreeTextDialog.REMOVE_PRIMARY_FREE_TEXT_DIALOG_REQUEST_CODE, primaryFreeTextToBeRemoved);
			}
		});

		// Set listener to Add Secondary Free Text Button
		addSecondaryFreeTextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showAddFreeTextDialog(AddFreeTextDialog.ADD_SECONDARY_FREE_TEXT_DIALOG_REQUEST_CODE);
			}
		});

		// Set listener to Edit Secondary Free Text Button
		editSecondaryFreeTextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Resolve free text to be edited
				String secondaryFreeTextToBeEdited = secondaryFreeTexts.get(secondaryFreeTextSpinner.getSelectedItemPosition());
				showEditFreeTextDialog(EditFreeTextDialog.EDIT_SECONDARY_FREE_TEXT_DIALOG_REQUEST_CODE, secondaryFreeTextToBeEdited);
			}
		});

		// Set listener to Remove Secondary Free Text Button
		removeSecondaryFreeTextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String secondaryFreeTextToBeRemoved = secondaryFreeTexts.get(secondaryFreeTextSpinner.getSelectedItemPosition());
				showRemoveFreeTextDialog(RemoveFreeTextDialog.REMOVE_SECONDARY_FREE_TEXT_DIALOG_REQUEST_CODE, secondaryFreeTextToBeRemoved);
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Only interested in OK results, don't care at all about the others
		if (resultCode == Activity.RESULT_OK) {
			// To store the entered free text for further handling
			String newFreeText = "";

			// To store the edited free text and the free text to be replaced for further handling
			InitializableString initializableString;

			// Only interested in certain request codes...
			switch (requestCode) {
				case (AddFreeTextDialog.ADD_PRIMARY_FREE_TEXT_DIALOG_REQUEST_CODE):
					newFreeText = data.getStringExtra(AddFreeTextDialog.ADD_FREE_TEXT);

					// If input doesn't exist in the list of secondaryFreeTexts and input isn't empty
					if (!Util.existsInIgnoreCases(newFreeText, secondaryFreeTexts) && !"".equals(newFreeText)) {
						// Store input if the list of primaryFreeTexts doesn't contain the new free text
						if (!Util.existsInIgnoreCases(newFreeText, primaryFreeTexts)) {
							// Add given input to list
							primaryFreeTexts.add(newFreeText);

							// Store to shared preferences
							prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_FREE_TEXTS_KEY, primaryFreeTexts, context);

							// Update affected UI widgets
							updatePrimaryFreeTextSpinner();
						} else {
							Toast.makeText(context, R.string.TOAST_FREE_TEXT_ALREADY_IN_PRIMARY_LIST, Toast.LENGTH_LONG).show();

							// Showing dialog again with correct request code
							showAddFreeTextDialog(AddFreeTextDialog.ADD_PRIMARY_FREE_TEXT_DIALOG_REQUEST_CODE);
						}
					} else {
						// Empty input was given
						if ("".equals(newFreeText)) {
							Toast.makeText(context, R.string.TOAST_FREE_TEXT_MISSING, Toast.LENGTH_LONG).show();
						} else { // Given primary free text exists in the list of secondary free texts
							Toast.makeText(context, R.string.TOAST_DUPLICATED_FREE_TEXTS, Toast.LENGTH_LONG).show();
						}

						showAddFreeTextDialog(AddFreeTextDialog.ADD_PRIMARY_FREE_TEXT_DIALOG_REQUEST_CODE);
					}
					break;
				case (AddFreeTextDialog.ADD_SECONDARY_FREE_TEXT_DIALOG_REQUEST_CODE):
					newFreeText = data.getStringExtra(AddFreeTextDialog.ADD_FREE_TEXT);

					if (!Util.existsInIgnoreCases(newFreeText, primaryFreeTexts) && !"".equals(newFreeText)) {
						if (!Util.existsInIgnoreCases(newFreeText, secondaryFreeTexts)) {
							secondaryFreeTexts.add(newFreeText);
							prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_FREE_TEXTS_KEY, secondaryFreeTexts, context);
							updateSecondaryFreeTextSpinner();
						} else {
							Toast.makeText(context, R.string.TOAST_FREE_TEXT_ALREADY_IN_SECONDARY_LIST, Toast.LENGTH_LONG).show();
							showAddFreeTextDialog(AddFreeTextDialog.ADD_SECONDARY_FREE_TEXT_DIALOG_REQUEST_CODE);
						}
					} else {
						if ("".equals(newFreeText)) {
							Toast.makeText(context, R.string.TOAST_FREE_TEXT_MISSING, Toast.LENGTH_LONG).show();
						} else {
							Toast.makeText(context, R.string.TOAST_DUPLICATED_FREE_TEXTS, Toast.LENGTH_LONG).show();
						}

						showAddFreeTextDialog(AddFreeTextDialog.ADD_SECONDARY_FREE_TEXT_DIALOG_REQUEST_CODE);
					}
					break;
				case (EditFreeTextDialog.EDIT_PRIMARY_FREE_TEXT_DIALOG_REQUEST_CODE):
					initializableString = (InitializableString) data.getParcelableExtra(EditFreeTextDialog.EDIT_FREE_TEXT);

					if (!"".equals(initializableString.getValue()) && !Util.existsInIgnoreCases(initializableString.getValue(), secondaryFreeTexts)) {
						if (!Util.existsInIgnoreCases(initializableString.getValue(), primaryFreeTexts)) {
							// Replace existing element in list of primary free texts with the new one
							Collections.replaceAll(primaryFreeTexts, initializableString.getInitialValue(), initializableString.getValue());

							// Store to shared preferences
							prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_FREE_TEXTS_KEY, primaryFreeTexts, context);

							// Update affected UI widgets
							updatePrimaryFreeTextSpinner();
						} else {
							Toast.makeText(context, R.string.TOAST_FREE_TEXT_ALREADY_IN_PRIMARY_LIST, Toast.LENGTH_LONG).show();
							showEditFreeTextDialog(EditFreeTextDialog.EDIT_PRIMARY_FREE_TEXT_DIALOG_REQUEST_CODE, initializableString.getInitialValue());
						}
					} else {
						// Empty input was given
						if ("".equals(initializableString.getValue())) {
							Toast.makeText(context, R.string.TOAST_FREE_TEXT_MISSING, Toast.LENGTH_LONG).show();
						} else { // Given primary free text exists in the list of secondary free texts
							Toast.makeText(context, R.string.TOAST_DUPLICATED_FREE_TEXTS, Toast.LENGTH_LONG).show();
						}

						showEditFreeTextDialog(EditFreeTextDialog.EDIT_PRIMARY_FREE_TEXT_DIALOG_REQUEST_CODE, initializableString.getInitialValue());
					}
					break;
				case (EditFreeTextDialog.EDIT_SECONDARY_FREE_TEXT_DIALOG_REQUEST_CODE):
					initializableString = (InitializableString) data.getParcelableExtra(EditFreeTextDialog.EDIT_FREE_TEXT);

					if (!"".equals(initializableString.getValue()) && !Util.existsInIgnoreCases(initializableString.getValue(), primaryFreeTexts)) {
						if (!Util.existsInIgnoreCases(initializableString.getValue(), secondaryFreeTexts)) {
							Collections.replaceAll(secondaryFreeTexts, initializableString.getInitialValue(), initializableString.getValue());
							prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_FREE_TEXTS_KEY, secondaryFreeTexts, context);
							updateSecondaryFreeTextSpinner();
						} else {
							Toast.makeText(context, R.string.TOAST_FREE_TEXT_ALREADY_IN_SECONDARY_LIST, Toast.LENGTH_LONG).show();
							showEditFreeTextDialog(EditFreeTextDialog.EDIT_SECONDARY_FREE_TEXT_DIALOG_REQUEST_CODE, initializableString.getInitialValue());
						}
					} else {
						if ("".equals(initializableString.getValue())) {
							Toast.makeText(context, R.string.TOAST_FREE_TEXT_MISSING, Toast.LENGTH_LONG).show();
						} else {
							Toast.makeText(context, R.string.TOAST_DUPLICATED_FREE_TEXTS, Toast.LENGTH_LONG).show();
						}

						showEditFreeTextDialog(EditFreeTextDialog.EDIT_SECONDARY_FREE_TEXT_DIALOG_REQUEST_CODE, initializableString.getInitialValue());
					}
					break;
				case (RemoveFreeTextDialog.REMOVE_PRIMARY_FREE_TEXT_DIALOG_REQUEST_CODE):
					// Remove free text in list that equals the free text got from intent data
					primaryFreeTexts.remove(data.getStringExtra(RemoveFreeTextDialog.REMOVE_FREE_TEXT));
					prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_FREE_TEXTS_KEY, primaryFreeTexts, context);
					updatePrimaryFreeTextSpinner();
					break;
				case (RemoveFreeTextDialog.REMOVE_SECONDARY_FREE_TEXT_DIALOG_REQUEST_CODE):
					secondaryFreeTexts.remove(data.getStringExtra(RemoveFreeTextDialog.REMOVE_FREE_TEXT));
					prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_FREE_TEXTS_KEY, secondaryFreeTexts, context);
					updateSecondaryFreeTextSpinner();
					break;
				default:
					Log.e(LOG_TAG + ":onActivityResult()", "An unsupported result occurred, result code: \"" + resultCode + "\" and request code: \"" + requestCode + "\"");
			}
		}
	}

	/**
	 * Convenience method to create a new instance of {@link AddFreeTextDialog} and show it.
	 * 
	 * @param requestCode
	 *            Request code for the created <code>AddFreeTextDialog</code>.
	 */
	private void showAddFreeTextDialog(int requestCode) {
		AddFreeTextDialog dialog = new AddFreeTextDialog();
		dialog.setTargetFragment(FreeTextSettingsFragment.this, requestCode);
		dialog.show(getFragmentManager(), AddFreeTextDialog.ADD_FREE_TEXT_DIALOG_TAG);
	}

	/**
	 * Convenience method to create a new instance of {@link EditFreeTextDialog} and show it.
	 * 
	 * @param requestCode
	 *            Request code for the created <code>EditFreeTextDialog</code>.
	 * @param freeTextToBeEdited
	 *            Free text to be edited.
	 */
	private void showEditFreeTextDialog(int requestCode, String freeTextToBeEdited) {
		EditFreeTextDialog dialog = EditFreeTextDialog.newInstance(new InitializableString(freeTextToBeEdited));
		dialog.setTargetFragment(FreeTextSettingsFragment.this, requestCode);
		dialog.show(getFragmentManager(), EditFreeTextDialog.EDIT_FREE_TEXT_DIALOG_TAG);
	}

	/**
	 * Convenience method to create a new instance of {@link RemoveFreeTextDialog} and show it.
	 * 
	 * @param requestCode
	 *            Request code for the created <code>RemoveFreeTextDialog</code>.
	 * @param freeTextToBeRemoved
	 *            Free text to be removed.
	 */
	private void showRemoveFreeTextDialog(int requestCode, String freeTextToBeRemoved) {
		// Create a new instance of RemoveFreeTextDialog prepared with correct free text
		RemoveFreeTextDialog dialog = RemoveFreeTextDialog.newInstance(freeTextToBeRemoved);
		dialog.setTargetFragment(FreeTextSettingsFragment.this, requestCode);
		dialog.show(getFragmentManager(), RemoveFreeTextDialog.REMOVE_FREE_TEXT_DIALOG_TAG);
	}

	/**
	 * To update primary free texts <code>Spinner</code> with correct values.
	 * 
	 * @see #updateSecondaryFreeTextSpinner()
	 */
	private void updatePrimaryFreeTextSpinner() {
		// Check if there are primary free test and build up a proper spinner according to that information
		if (!primaryFreeTexts.isEmpty()) {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, primaryFreeTexts);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			primaryFreeTextSpinner.setAdapter(adapter);
		} else {
			// Only add item to list if it's empty
			if (emptyFreeTexts.isEmpty()) {
				emptyFreeTexts.add(getString(R.string.ADD_FREE_TEXT_HINT));
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, emptyFreeTexts);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			primaryFreeTextSpinner.setAdapter(adapter);
		}

		updateButtons();
	}

	/**
	 * To update secondary free texts {@link Spinner} with correct values.
	 * 
	 * @see #updatePrimaryFreeTextSpinner()
	 */
	private void updateSecondaryFreeTextSpinner() {
		// Check if there are primary listen free test and build up a proper spinner according to that information
		if (!secondaryFreeTexts.isEmpty()) {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, secondaryFreeTexts);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			secondaryFreeTextSpinner.setAdapter(adapter);
		} else {
			// Only add item to list if it's empty
			if (emptyFreeTexts.isEmpty()) {
				emptyFreeTexts.add(getString(R.string.ADD_FREE_TEXT_HINT));
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, emptyFreeTexts);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			secondaryFreeTextSpinner.setAdapter(adapter);
		}

		updateButtons();
	}

	/**
	 * To update buttons within this fragment, mainly it enables or disables the <b><i>Edit</i></b> and <b><i>Remove</i></b> buttons.
	 */
	private void updateButtons() {
		// Toggle the buttons depending whether or not it's corresponding list got any elements within it
		editPrimaryFreeTextButton.setEnabled(!primaryFreeTexts.isEmpty());
		removePrimaryFreeTextButton.setEnabled(!primaryFreeTexts.isEmpty());

		editSecondaryFreeTextButton.setEnabled(!secondaryFreeTexts.isEmpty());
		removeSecondaryFreeTextButton.setEnabled(!secondaryFreeTexts.isEmpty());
	}
}