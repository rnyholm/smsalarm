/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.handler.LogHandler;
import ax.ha.it.smsalarm.handler.LogHandler.LogPriorities;
import ax.ha.it.smsalarm.handler.PreferencesHandler;
import ax.ha.it.smsalarm.handler.PreferencesHandler.DataTypes;
import ax.ha.it.smsalarm.handler.PreferencesHandler.PrefKeys;

import com.actionbarsherlock.app.SherlockFragment;

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

	/**
	 * To update primary sms number <code>Spinner</code> with correct values.
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
