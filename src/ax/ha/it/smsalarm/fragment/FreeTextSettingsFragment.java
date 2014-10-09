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
import ax.ha.it.smsalarm.fragment.dialog.RemoveFreeTextDialog;
import ax.ha.it.smsalarm.handler.PreferencesHandler;
import ax.ha.it.smsalarm.handler.PreferencesHandler.DataType;
import ax.ha.it.smsalarm.handler.PreferencesHandler.PrefKey;
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
	private final PreferencesHandler prefHandler = PreferencesHandler.getInstance();

	// Must have the application context
	private Context context;

	// The Buttons...
	private Button addPrimaryFreeTextButton;
	private Button removePrimaryFreeTextButton;
	private Button addSecondaryFreeTextButton;
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
		removePrimaryFreeTextButton = (Button) view.findViewById(R.id.deletePrimaryFreeText_btn);
		addSecondaryFreeTextButton = (Button) view.findViewById(R.id.addSecondaryFreeText_btn);
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

		// Set listener to Remove Primary Free Text Button
		removePrimaryFreeTextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Only show delete dialog if primary free texts exists
				if (!primaryFreeTexts.isEmpty()) {
					// Resolve free text to be removed
					String primaryFreeTextToBeRemoved = primaryFreeTexts.get(primaryFreeTextSpinner.getSelectedItemPosition());
					showRemoveFreeTextDialog(RemoveFreeTextDialog.REMOVE_PRIMARY_FREE_TEXT_DIALOG_REQUEST_CODE, primaryFreeTextToBeRemoved);
				} else {
					Toast.makeText(context, R.string.NO_PRIMARY_FREE_TEXT_EXISTS, Toast.LENGTH_LONG).show();
				}
			}
		});

		// Set listener to Add Secondary Free Text Button
		addSecondaryFreeTextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showAddFreeTextDialog(AddFreeTextDialog.ADD_SECONDARY_FREE_TEXT_DIALOG_REQUEST_CODE);
			}
		});

		// Set listener to Remove Secondary Free Text Button
		removeSecondaryFreeTextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Only show delete dialog if secondary free texts exists
				if (!secondaryFreeTexts.isEmpty()) {
					String secondaryFreeTextToBeRemoved = secondaryFreeTexts.get(secondaryFreeTextSpinner.getSelectedItemPosition());
					showRemoveFreeTextDialog(RemoveFreeTextDialog.REMOVE_SECONDARY_FREE_TEXT_DIALOG_REQUEST_CODE, secondaryFreeTextToBeRemoved);
				} else {
					Toast.makeText(context, R.string.NO_SECONDARY_FREE_TEXT_EXISTS, Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Only interested in OK results, don't care at all about the others
		if (resultCode == Activity.RESULT_OK) {
			// Boolean indicating if there are duplicates of the primary and secondary free texts
			boolean duplicatedFreeTexts = false;
			// To store the entered free text for further handling
			String newFreeText = "";

			// Only interested in certain request codes...
			switch (requestCode) {
				case (AddFreeTextDialog.ADD_PRIMARY_FREE_TEXT_DIALOG_REQUEST_CODE):
					newFreeText = data.getStringExtra(AddFreeTextDialog.ADD_FREE_TEXT);

					// If input doesn't exist in the list of secondaryFreeTexts and input isn't empty
					if (!Util.existsIn(newFreeText, secondaryFreeTexts) && !newFreeText.equals("")) {
						// Iterate through all strings in the list of primaryFreeTexts to check if text already exists
						for (String text : primaryFreeTexts) {
							// If a string in the list is equal with the new free text then it's duplicated
							if (text.equalsIgnoreCase(newFreeText)) {
								duplicatedFreeTexts = true;
							}
						}

						// Store input if duplicated free texts is false
						if (!duplicatedFreeTexts) {
							// Add given input to list
							primaryFreeTexts.add(newFreeText);

							// Store to shared preferences
							prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_LISTEN_FREE_TEXTS_KEY, primaryFreeTexts, context);

							// Update affected UI widgets
							updatePrimaryFreeTextSpinner();
						} else {
							Toast.makeText(context, R.string.FREE_TEXT_ALREADY_IN_PRIMARY_LIST, Toast.LENGTH_LONG).show();

							// Showing dialog again with correct request code
							showAddFreeTextDialog(AddFreeTextDialog.ADD_PRIMARY_FREE_TEXT_DIALOG_REQUEST_CODE);
						}
					} else {
						// Empty input was given
						if (newFreeText.equals("")) {
							Toast.makeText(context, R.string.TEXT_IS_NEEDED, Toast.LENGTH_LONG).show();
						} else { // Given primary free text exists in the list of secondary free texts
							Toast.makeText(context, R.string.DUPLICATED_FREE_TEXTS, Toast.LENGTH_LONG).show();
						}

						showAddFreeTextDialog(AddFreeTextDialog.ADD_PRIMARY_FREE_TEXT_DIALOG_REQUEST_CODE);
					}
					break;
				case (AddFreeTextDialog.ADD_SECONDARY_FREE_TEXT_DIALOG_REQUEST_CODE):
					newFreeText = data.getStringExtra(AddFreeTextDialog.ADD_FREE_TEXT);

					if (!Util.existsIn(newFreeText, primaryFreeTexts) && !newFreeText.equals("")) {
						for (String text : secondaryFreeTexts) {
							if (text.equalsIgnoreCase(newFreeText)) {
								duplicatedFreeTexts = true;
							}
						}

						if (!duplicatedFreeTexts) {
							secondaryFreeTexts.add(newFreeText);
							prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_LISTEN_FREE_TEXTS_KEY, secondaryFreeTexts, context);
							updateSecondaryFreeTextSpinner();
						} else {
							Toast.makeText(context, R.string.FREE_TEXT_ALREADY_IN_SECONDARY_LIST, Toast.LENGTH_LONG).show();
							showAddFreeTextDialog(AddFreeTextDialog.ADD_SECONDARY_FREE_TEXT_DIALOG_REQUEST_CODE);
						}
					} else {
						if (newFreeText.equals("")) {
							Toast.makeText(context, R.string.TEXT_IS_NEEDED, Toast.LENGTH_LONG).show();
						} else {
							Toast.makeText(context, R.string.DUPLICATED_FREE_TEXTS, Toast.LENGTH_LONG).show();
						}

						showAddFreeTextDialog(AddFreeTextDialog.ADD_SECONDARY_FREE_TEXT_DIALOG_REQUEST_CODE);
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
	 * Convenience method to create a new instance of {@link RemoveFreeTextDialog} and show it.
	 * 
	 * @param requestCode
	 *            Request code for the created <code>RemoveFreeTextDialog</code>.
	 * @param freeTextToBeRemoved
	 *            Free text to be removed.
	 */
	private void showRemoveFreeTextDialog(int requestCode, String freeTextToBeRemoved) {
		// Must pass over free text to be removed
		Bundle arguments = new Bundle();
		arguments.putString(RemoveFreeTextDialog.REMOVE_FREE_TEXT, freeTextToBeRemoved);

		// Create Dialog as usual, but put arguments in it also
		RemoveFreeTextDialog dialog = new RemoveFreeTextDialog();
		dialog.setArguments(arguments);
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
				emptyFreeTexts.add(getString(R.string.ENTER_FREE_TEXT_HINT));
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, emptyFreeTexts);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			primaryFreeTextSpinner.setAdapter(adapter);
		}
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
				emptyFreeTexts.add(getString(R.string.ENTER_FREE_TEXT_HINT));
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, emptyFreeTexts);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			secondaryFreeTextSpinner.setAdapter(adapter);
		}
	}
}