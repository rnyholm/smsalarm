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
 * <code>Fragment</code> containing all the views and user interface widgets for the <b><i>Free Text Settings</i></b>. Fragment does also contain all
 * logic for the widgets.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class FreeTextSettingsFragment extends SherlockFragment implements ApplicationFragment {
	private final String LOG_TAG = getClass().getSimpleName();

	// Objects needed for logging and shared preferences handling
	private final LogHandler logger = LogHandler.getInstance();
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
	 * To create a new <code>FreeTextSettingsFragment</code>.
	 * 
	 * @param context
	 *            Context
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 */
	public FreeTextSettingsFragment() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":FreeTextSettingsFragment()", "Creating a new Free text settings fragmen");
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
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "Start finding Views by their Id");

		// Declare and initialize variables of type button
		addPrimaryFreeTextButton = (Button) view.findViewById(R.id.addPrimaryFreeText_btn);
		removePrimaryFreeTextButton = (Button) view.findViewById(R.id.deletePrimaryFreeText_btn);
		addSecondaryFreeTextButton = (Button) view.findViewById(R.id.addSecondaryFreeText_btn);
		removeSecondaryFreeTextButton = (Button) view.findViewById(R.id.deleteSecondaryFreeText_btn);

		// Declare and initialize variables of type Spinner
		primaryFreeTextSpinner = (Spinner) view.findViewById(R.id.primaryFreeTextSpinner_sp);
		secondaryFreeTextSpinner = (Spinner) view.findViewById(R.id.secondaryFreeTextSpinner_sp);

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "All Views found for Fragment:\"" + LOG_TAG + "\"");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void fetchSharedPrefs() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":fetchSharedPrefs()", "Start fetching shared preferences needed by this fragment");

		try {
			primaryFreeTexts = (List<String>) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_LISTEN_FREE_TEXTS_KEY, DataTypes.LIST, context);
			secondaryFreeTexts = (List<String>) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.SECONDARY_LISTEN_FREE_TEXTS_KEY, DataTypes.LIST, context);
		} catch (IllegalArgumentException e) {
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":fetchSharedPrefs()", "An unsupported datatype was given as argument to PreferencesHandler.getPrefs()", e);
		}

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":fetchSharedPrefs()", "Shared preferences fetched");
	}

	@Override
	public void updateFragmentView() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateFragmentView()", "Whole fragments user interface is about to be updated");

		// Update primary- and secondary free text Spinner
		updatePrimaryFreeTextSpinner();
		updateSecondaryFreeTextSpinner();

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateFragmentView()", "Fragment updated");
	}

	@Override
	public void setListeners() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners()", "Setting listeners to the different user interface widgets");

		// Set listener to addPrimaryFreeTextButton
		addPrimaryFreeTextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().addPrimaryFreeTextButton.OnClickListener().onClick()", "Add PRIMARY free text button pressed");
				createFreeTextPrimaryInputDialog();
			}
		});

		// Set listener to removePrimaryFreeTextButton
		removePrimaryFreeTextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().removePrimaryFreeTextButton.OnClickListener().onClick()", "Remove PRIMARY free text button pressed");

				// Only show delete dialog if primary free texts exists
				if (!primaryFreeTexts.isEmpty()) {
					createFreeTextPrimaryRemoveDialog();
				} else {
					Toast.makeText(context, R.string.NO_PRIMARY_FREE_TEXT_EXISTS, Toast.LENGTH_LONG).show();
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().removePrimaryFreeTextButton.OnClickListener().onClick()", "Cannot build and show dialog because the list of PRIMARY free texts are empty");
				}
			}
		});

		// Set listener to addSecondaryFreeTextButton
		addSecondaryFreeTextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().addSecondaryFreeTextButton.OnClickListener().onClick()", "Add SECONDARY free text button pressed");
				createFreeTextSecondaryInputDialog();
			}
		});

		// Set listener to removeSecondaryFreeTextButton
		removeSecondaryFreeTextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().removeSecondaryFreeTextButton.OnClickListener().onClick()", "Remove SECONDARY free text button pressed");
				// Only show delete dialog if secondary free texts exists
				if (!secondaryFreeTexts.isEmpty()) {
					createFreeTextSecondaryRemoveDialog();
				} else {
					Toast.makeText(context, R.string.NO_SECONDARY_FREE_TEXT_EXISTS, Toast.LENGTH_LONG).show();
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().removePrimaryFreeTextButton.OnClickListener().onClick()", "Cannot build and show dialog because the list of SECONDARY free texts are");
				}
			}
		});
	}

	/**
	 * To build up and display a dialog which let's the user add a free text to the list of <b><i>Primary alarm triggering free texts</i></b>. <br>
	 * <b><i>Note. The input of this dialog doesn't accept blankspaces({@link Util.NoBlanksInputEditText})</i></b>.
	 * 
	 * @see #createFreeTextPrimaryRemoveDialog()
	 * @see #updatePrimaryFreeTextSpinner()
	 * @see PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context) setPrefs(PrefKeys, PrefKeys, Object, Context)
	 * @see LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see LogHandler#logCatTxt(LogPriorities, String, String, Throwable) logCatTxt(LogPriorities, String, String,Throwable)
	 */
	private void createFreeTextPrimaryInputDialog() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createFreeTextPrimaryInputDialog()", "Start building dialog for input of Primary Free Text");

		// Build up the alert dialog
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		final EditText noBlanksInputEditText = new NoBlanksInputEditText(context);

		// @formatter:off
		// Configure dialog and edit text
		dialog.setIcon(android.R.drawable.ic_dialog_info);				// Set icon
		dialog.setTitle(R.string.FREE_TEXT_PROMPT_TITLE);				// Set title
		dialog.setMessage(R.string.PRIMARY_FREE_TEXT_PROMPT_MESSAGE);	// Set message
		dialog.setCancelable(false);									// Set dialog to non cancelable
		dialog.setView(noBlanksInputEditText);							// Bind dialog to input
		noBlanksInputEditText.setHint(R.string.FREE_TEXT_PROMPT_HINT);	// Set hint to edit text
		noBlanksInputEditText.setInputType(InputType.TYPE_CLASS_TEXT);	// Set input type to edit text
		// @formatter:on

		// Set a positive button and listen on it
		dialog.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createFreeTextPrimaryInputDialog().PositiveButton.OnClickListener().onClick()", "Positive Button pressed");

				// Boolean indicating if there are duplicates of the primary and secondary free texts
				boolean duplicatedFreeTexts = false;
				// Store input
				String input = noBlanksInputEditText.getText().toString();

				// If input doesn't exist in the list of secondaryFreeTexts and input isn't empty
				if (!Util.existsIn(input, secondaryFreeTexts) && !input.equals("")) {
					// Iterate through all strings in the list of primaryFreeTexts to check if text already exists
					for (String text : primaryFreeTexts) {
						// If a string in the list is equal with the input then it's duplicated
						if (text.equalsIgnoreCase(input)) {
							duplicatedFreeTexts = true;
						}
					}

					// Store input if duplicated free texts is false
					if (!duplicatedFreeTexts) {
						// Add given input to list
						primaryFreeTexts.add(input);

						try {
							// Store to shared preferences
							prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_LISTEN_FREE_TEXTS_KEY, primaryFreeTexts, context);
						} catch (IllegalArgumentException e) {
							logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":createFreeTextPrimaryInputDialog().PositiveButton.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
						}

						// Update affected UI widgets
						updatePrimaryFreeTextSpinner();
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createFreeTextPrimaryInputDialog().PositiveButton.OnClickListener().onClick()", "New PRIMARY free text has been stored from user input to the list of PRIMARY free texts. New PRIMARY free text is: \"" + input + "\"");
					} else {
						Toast.makeText(context, R.string.FREE_TEXT_ALREADY_IN_PRIMARY_LIST, Toast.LENGTH_LONG).show();
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createFreeTextPrimaryInputDialog().PositiveButton.OnClickListener().onClick()", "Given PRIMARY free text(" + input + ") already exists in the list of PRIMARY free texts and therefore cannot be stored. Showing dialog again");

						createFreeTextPrimaryInputDialog();
					}
				} else {
					// Empty input was given
					if (input.equals("")) {
						Toast.makeText(context, R.string.TEXT_IS_NEEDED, Toast.LENGTH_LONG).show();
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createFreeTextPrimaryInputDialog().PositiveButton.OnClickListener().onClick()", "Given PRIMARY free text is empty and therefore cannot be stored. Showing dialog again");
					} else { // Given primary free text exists in the list of secondary free texts
						Toast.makeText(context, R.string.DUPLICATED_FREE_TEXTS, Toast.LENGTH_LONG).show();
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createFreeTextPrimaryInputDialog().PositiveButton.OnClickListener().onClick()", "Given PRIMARY free text(" + input + ") already exists in the list of SECONDARY free texts and therefore cannot be stored. Showing dialog again");
					}

					createFreeTextPrimaryInputDialog();
				}
			}
		});

		// Set a neutral button, due to documentation it has same functionality as "back" button
		dialog.setNeutralButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// DO NOTHING, except logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createFreeTextPrimaryInputDialog().NeutralButton.OnClickListener().onClick()", "Neutral Button pressed in dialog, nothing done");
			}
		});

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createFreeTextPrimaryInputDialog()", "Showing dialog");

		// Show it
		dialog.show();
	}

	/**
	 * To build up and display a dialog which let's the user add a free text to the list of <b><i>Primary alarm triggering free texts</i></b>. <br>
	 * <b><i>Note. The input of this dialog doesn't accept blankspaces({@link Util.NoBlanksInputEditText})</i></b>.
	 * 
	 * @see #createFreeTextSecondaryRemoveDialog()
	 * @see #updateSecondaryFreeTextSpinner()
	 * @see PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context) setPrefs(PrefKeys, PrefKeys, Object, Context)
	 * @see LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see LogHandler#logCatTxt(LogPriorities, String, String, Throwable) logCatTxt(LogPriorities, String, String,Throwable)
	 */
	private void createFreeTextSecondaryInputDialog() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createFreeTextSecondaryInputDialog()", "Start building dialog for input of Secondary Free Text");

		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		final EditText noBlanksInputEditText = new NoBlanksInputEditText(context);

		dialog.setIcon(android.R.drawable.ic_dialog_info);
		dialog.setTitle(R.string.FREE_TEXT_PROMPT_TITLE);
		dialog.setMessage(R.string.SECONDARY_FREE_TEXT_PROMPT_MESSAGE);
		dialog.setCancelable(false);
		dialog.setView(noBlanksInputEditText);
		noBlanksInputEditText.setHint(R.string.FREE_TEXT_PROMPT_HINT);
		noBlanksInputEditText.setInputType(InputType.TYPE_CLASS_TEXT);

		dialog.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createFreeTextSecondaryInputDialog().PositiveButton.OnClickListener().onClick()", "Positive Button pressed");

				boolean duplicatedFreeTexts = false;
				String input = noBlanksInputEditText.getText().toString();

				if (!Util.existsIn(input, primaryFreeTexts) && !input.equals("")) {
					for (String text : secondaryFreeTexts) {
						if (text.equalsIgnoreCase(input)) {
							duplicatedFreeTexts = true;
						}
					}

					if (!duplicatedFreeTexts) {
						secondaryFreeTexts.add(input);

						try {
							prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.SECONDARY_LISTEN_FREE_TEXTS_KEY, secondaryFreeTexts, context);
						} catch (IllegalArgumentException e) {
							logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":createFreeTextSecondaryInputDialog().PositiveButton.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
						}

						updateSecondaryFreeTextSpinner();
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createFreeTextSecondaryInputDialog().PositiveButton.OnClickListener().onClick()", "New SECONDARY free text has been stored from user input to the list of SECONDARY free texts. New SECONDARY free text is: \"" + input + "\"");
					} else {
						Toast.makeText(context, R.string.FREE_TEXT_ALREADY_IN_SECONDARY_LIST, Toast.LENGTH_LONG).show();
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createFreeTextSecondaryInputDialog().PositiveButton.OnClickListener().onClick()", "Given SECONDARY free text(" + input + ") already exists in the list of SECONDARY free texts and therefore cannot be stored. Showing dialog again");

						createFreeTextSecondaryInputDialog();
					}
				} else {
					if (input.equals("")) {
						Toast.makeText(context, R.string.TEXT_IS_NEEDED, Toast.LENGTH_LONG).show();
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createFreeTextSecondaryInputDialog().PositiveButton.OnClickListener().onClick()", "Given SECONDARY free text is empty and therefore cannot be stored. Showing dialog again");
					} else {
						Toast.makeText(context, R.string.DUPLICATED_FREE_TEXTS, Toast.LENGTH_LONG).show();
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createFreeTextSecondaryInputDialog().PositiveButton.OnClickListener().onClick()", "Given SECONDARY free text(" + input + ") already exists in the list of PRIMARY free texts and therefore cannot be stored. Showing dialog again");
					}

					createFreeTextSecondaryInputDialog();
				}
			}
		});

		dialog.setNeutralButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// DO NOTHING, except logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createFreeTextSecondaryInputDialog().NeutralButton.OnClickListener().onClick()", "Neutral Button pressed in dialog, nothing done");
			}
		});

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createFreeTextSecondaryInputDialog()", "Showing dialog");
		dialog.show();
	}

	/**
	 * To build up and display a dialog which let's the user remove a free text from the list of <b><i>Primary alarm triggering free texts</i></b>. <br>
	 *
	 * @see #createFreeTextPrimaryInputDialog()
	 * @see #updatePrimaryFreeTextSpinner()
	 * @see PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context) setPrefs(PrefKeys, PrefKeys, Object, Context)
	 * @see LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see LogHandler#logCatTxt(LogPriorities, String, String, Throwable) logCatTxt(LogPriorities, String, String,Throwable)
	 */
	private void createFreeTextPrimaryRemoveDialog() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createFreeTextPrimaryRemoveDialog()", "Start building dialog for removing a Primary Free Text");

		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setIcon(android.R.drawable.ic_dialog_alert);
		dialog.setTitle(R.string.DELETE_FREE_TEXT_PROMPT_TITLE);
		dialog.setMessage(getString(R.string.DELETE_PRIMARY_FREE_TEXT_PROMPT_MESSAGE) + " " + primaryFreeTexts.get(primaryFreeTextSpinner.getSelectedItemPosition()) + "?");
		dialog.setCancelable(false);

		dialog.setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createFreeTextPrimaryRemoveDialog().PosButton.OnClickListener().onClick()", "Positive Button pressed");
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createFreeTextPrimaryRemoveDialog().PosButton.OnClickListener().onClick()", "PRIMARY free text: \"" + primaryFreeTexts.get(primaryFreeTextSpinner.getSelectedItemPosition()) + "\" is about to be removed from the list of PRIMARY free texts");

				// Delete free text from list
				primaryFreeTexts.remove(primaryFreeTextSpinner.getSelectedItemPosition());
				try {
					// Store to shared preferences
					prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.PRIMARY_LISTEN_FREE_TEXTS_KEY, primaryFreeTexts, context);
				} catch (IllegalArgumentException e) {
					logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":createFreeTextPrimaryRemoveDialog().PosButton.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
				}
				// Update affected UI widgets
				updatePrimaryFreeTextSpinner();
			}
		});

		dialog.setNeutralButton(R.string.NO, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// DO NOTHING, except logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createFreeTextPrimaryRemoveDialog().NeutralButton.OnClickListener().onClick()", "Neutral Button pressed in dialog, nothing done");
			}
		});

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createFreeTextPrimaryRemoveDialog()", "Showing dialog");
		dialog.show();
	}

	/**
	 * To build up and display a dialog which let's the user remove a free text from the list of <b><i>Primary alarm triggering free texts</i></b>. <br>
	 *
	 * @see #createFreeTextSecondaryInputDialog()
	 * @see #updateSecondaryFreeTextSpinner()
	 * @see PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context) setPrefs(PrefKeys, PrefKeys, Object, Context)
	 * @see LogHandler#logCat(LogPriorities, String, String) logCat(LogPriorities, String, String)
	 * @see LogHandler#logCatTxt(LogPriorities, String, String, Throwable) logCatTxt(LogPriorities, String, String,Throwable)
	 */
	private void createFreeTextSecondaryRemoveDialog() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createFreeTextSecondaryRemoveDialog()", "Start building dialog for removing a Secondary Free Text");

		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setIcon(android.R.drawable.ic_dialog_alert);
		dialog.setTitle(R.string.DELETE_FREE_TEXT_PROMPT_TITLE);
		dialog.setMessage(getString(R.string.DELETE_SECONDARY_FREE_TEXT_PROMPT_MESSAGE) + " " + secondaryFreeTexts.get(secondaryFreeTextSpinner.getSelectedItemPosition()) + "?");
		dialog.setCancelable(false);

		dialog.setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createFreeTextSecondaryRemoveDialog().PosButton.OnClickListener().onClick()", "Positive Button pressed");
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createFreeTextSecondaryRemoveDialog().PosButton.OnClickListener().onClick()", "SECONDARY free text: \"" + secondaryFreeTexts.get(secondaryFreeTextSpinner.getSelectedItemPosition()) + "\" is about to be removed from the list of SECONDARY free texts");

				secondaryFreeTexts.remove(secondaryFreeTextSpinner.getSelectedItemPosition());
				try {
					prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.SECONDARY_LISTEN_FREE_TEXTS_KEY, secondaryFreeTexts, context);
				} catch (IllegalArgumentException e) {
					logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":createFreeTextSecondaryRemoveDialog().PosButton.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
				}
				updateSecondaryFreeTextSpinner();
			}
		});

		dialog.setNeutralButton(R.string.NO, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// DO NOTHING, except logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createFreeTextSecondaryRemoveDialog().NeutralButton.OnClickListener().onClick()", "Neutral Button pressed in dialog, nothing done");
			}
		});

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createFreeTextSecondaryRemoveDialog()", "Showing dialog");
		dialog.show();
	}

	/**
	 * To update primary free texts <code>Spinner</code> with correct values.
	 * 
	 * @see #updateSecondaryFreeTextSpinner()
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 */
	private void updatePrimaryFreeTextSpinner() {
		// Check if there are primary free test and build up a proper spinner according to that information
		if (!primaryFreeTexts.isEmpty()) {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, primaryFreeTexts);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			primaryFreeTextSpinner.setAdapter(adapter);

			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updatePrimaryFreeTextSpinner()", "Populate PRIMARY free text spinner with values: " + primaryFreeTexts);
		} else {
			// Only add item to list if it's empty
			if (emptyFreeTexts.isEmpty()) {
				emptyFreeTexts.add(getString(R.string.ENTER_FREE_TEXT_HINT));
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, emptyFreeTexts);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			primaryFreeTextSpinner.setAdapter(adapter);

			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updatePrimaryFreeTextSpinner()", "List with PRIMARY free texts is empty, populating spinner with an empty list");
		}

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updatePrimaryFreeTextSpinner()", "PRIMARY free text spinner updated");
	}

	/**
	 * To update secondary free texts <code>Spinner</code> with correct values.
	 * 
	 * @see #updatePrimaryFreeTextSpinner()
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 */
	private void updateSecondaryFreeTextSpinner() {
		// Check if there are primary listen free test and build up a proper spinner according to that information
		if (!secondaryFreeTexts.isEmpty()) {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, secondaryFreeTexts);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			secondaryFreeTextSpinner.setAdapter(adapter);

			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateSecondaryFreeTextSpinner()", "Populate SECONDARY free text spinner with values: " + secondaryFreeTexts);
		} else {
			// Only add item to list if it's empty
			if (emptyFreeTexts.isEmpty()) {
				emptyFreeTexts.add(getString(R.string.ENTER_FREE_TEXT_HINT));
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, emptyFreeTexts);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			secondaryFreeTextSpinner.setAdapter(adapter);

			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateSecondaryFreeTextSpinner()", "List with SECONDARY free texts is empty, populating spinner with an empty list");
		}

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateSecondaryFreeTextSpinner()", "SECONDARY free text spinner updated");
	}
}