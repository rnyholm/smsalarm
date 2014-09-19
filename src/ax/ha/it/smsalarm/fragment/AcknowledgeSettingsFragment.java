package ax.ha.it.smsalarm.fragment;

import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.handler.LogHandler;
import ax.ha.it.smsalarm.handler.LogHandler.LogPriorities;
import ax.ha.it.smsalarm.handler.PreferencesHandler;
import ax.ha.it.smsalarm.handler.PreferencesHandler.DataTypes;
import ax.ha.it.smsalarm.handler.PreferencesHandler.PrefKeys;
import ax.ha.it.smsalarm.ui.NoBlanksInputEditText;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * <code>Fragment</code> containing all the views and user interface widgets for the <b><i>Acknowledge Settings</i></b>. Fragment does also contain
 * all logic for the widgets.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class AcknowledgeSettingsFragment extends SherlockFragment implements ApplicationFragment {
	// Log tag
	private final String LOG_TAG = getClass().getSimpleName();

	// Objects needed for logging and shared preferences handling
	private final LogHandler logger = LogHandler.getInstance();
	private final PreferencesHandler prefHandler = PreferencesHandler.getInstance();

	// Must have the application context
	private Context context;

	// The EditText...
	private EditText ackNumberEditText;

	// ...Button...
	private Button ackNumberButton;

	// ...CheckBox...
	private CheckBox enableAckCheckBox;

	// ...and the TextView
	private TextView enableAckInfoTextView;

	// Indicating whether acknowledgement of alarm should be used or not
	private boolean useAlarmAcknowledge = false;

	// This number should acknowledgement be made to
	private String acknowledgeNumber = "";

	/**
	 * To create a new <code>AcknowledgeSettingsFragment</code>.
	 * 
	 * @param context
	 *            Context
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 */
	public AcknowledgeSettingsFragment() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":AcknowledgeSettingsFragment()", "Creating a new Acknowledge settings fragment");
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
		View view = inflater.inflate(R.layout.acknowledge_settings, container, false);

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

		// Finding EditText view
		ackNumberEditText = (EditText) view.findViewById(R.id.ackNumber_et);

		// Finding Button view
		ackNumberButton = (Button) view.findViewById(R.id.editAckNumber_btn);

		// Finding CheckBox view
		enableAckCheckBox = (CheckBox) view.findViewById(R.id.enableAcknowledge_chk);

		// Finding TextView
		enableAckInfoTextView = (TextView) view.findViewById(R.id.enableAcknowledgeHint_tv);

		// If Android API level is greater than Jelly Bean
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
			// We need to get some Android resources in order to calculate proper pixel dimensions from dp
			Resources resources = getResources();

			// Calculate pixel dimensions for the different margins
			// 32dp calculated to pixels
			int pixelsLeft = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, resources.getDisplayMetrics());
			// 5dp calculated to pixels
			int pixelsRight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, resources.getDisplayMetrics());
			int pixelsTop = 0;
			// If the locale on device is german(de) set pixelstop to -6dp else -9dp
			if ("de".equals(Locale.getDefault().getLanguage())) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "The device has german(de) locale, set different margin-top on information textviews for the checkboxes than other locales");
				// -6dp calculated to pixels
				pixelsTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -6, resources.getDisplayMetrics());
			} else {
				// -9dp calculated to pixels
				pixelsTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -9, resources.getDisplayMetrics());
			}

			// Set layout parameters for the enable acknowledge info textview
			// Wrap content, both on height and width
			RelativeLayout.LayoutParams paramsEnableAckInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			paramsEnableAckInfoTextView.setMargins(pixelsLeft, pixelsTop, pixelsRight, 0);
			paramsEnableAckInfoTextView.addRule(RelativeLayout.BELOW, enableAckCheckBox.getId());
			paramsEnableAckInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, enableAckCheckBox.getId());

			// Apply the previously configured layout parameters to the correct textviews
			enableAckInfoTextView.setLayoutParams(paramsEnableAckInfoTextView);

			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "API level > 16, edit margins on information TextViews for the checkboxes");
		} else { // The device has API level < 17, we just need to check if the locale is german
			// If the locale on device is german(de) we need to adjust the margin top for the information textviews for the checkboxes to -6dp
			if ("de".equals(Locale.getDefault().getLanguage())) {
				Resources resources = getResources();

				int pixelsLeft = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 38, resources.getDisplayMetrics());
				int pixelsRight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, resources.getDisplayMetrics());
				int pixelsTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -6, resources.getDisplayMetrics());

				RelativeLayout.LayoutParams paramsEnableAckInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				paramsEnableAckInfoTextView.setMargins(pixelsLeft, pixelsTop, pixelsRight, 0);
				paramsEnableAckInfoTextView.addRule(RelativeLayout.BELOW, enableAckCheckBox.getId());
				paramsEnableAckInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, enableAckCheckBox.getId());

				enableAckInfoTextView.setLayoutParams(paramsEnableAckInfoTextView);

				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "API level < 17 but the device has german(de) locale, set different margin-top on information TextViews for the checkboxes to fit the language");
			}
		}

		// Set some attributes to the ackNumberEditText
		ackNumberEditText.setEnabled(false);
		ackNumberEditText.setClickable(false);
		ackNumberEditText.setFocusable(false);
		ackNumberEditText.setBackgroundColor(Color.WHITE);

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "All Views found for Fragment:\"" + LOG_TAG + "\"");
	}

	@Override
	public void fetchSharedPrefs() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":fetchSharedPrefs()", "Start fetching shared preferences needed by this fragment");

		try {
			useAlarmAcknowledge = (Boolean) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.ENABLE_ACK_KEY, DataTypes.BOOLEAN, context);
			acknowledgeNumber = (String) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.ACK_NUMBER_KEY, DataTypes.STRING, context);
		} catch (IllegalArgumentException e) {
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":fetchSharedPrefs()", "An unsupported datatype was given as argument to PreferencesHandler.getPrefs()", e);
		}

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":fetchSharedPrefs()", "Shared preferences fetched");
	}

	@Override
	public void updateFragmentView() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateFragmentView()", "Whole fragments user interface is about to be updated");

		// Update acknowledge number EditText and other widgets in relation to alarm acknowledgment
		updateAcknowledgeNumberEditText();
		updateAcknowledgeWidgets();

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateFragmentView()", "Fragment updated");
	}

	@Override
	public void setListeners() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners()", "Setting listeners to the different user interface widgets");

		// Set listener to ackNumberButton
		ackNumberButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().ackNumberButton.OnClickListener().onClick()", "Edit acknowledge number button pressed");
				createAcknowledgeNumberInputDialog();
			}
		});

		// Set listener to enableAckCheckBox
		enableAckCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().enableAckCheckBox.onCheckedChange()", "Enable acknowledge checkbox pressed(or checkbox initialized)");

				// Set checkbox depending on it's checked status and store variable
				if (enableAckCheckBox.isChecked()) {
					// Store value to variable
					useAlarmAcknowledge = true;
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().enableAckCheckBox.onCheckedChange()", "Use OS sound settings checkbox \"Checked\"(" + useAlarmAcknowledge + ")");
				} else {
					useAlarmAcknowledge = false;
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().enableAckCheckBox.onCheckedChange()", "Use OS sound settings checkbox \"Unchecked\"(" + useAlarmAcknowledge + ")");
				}

				try {
					// Store value to shared preferences
					prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.ENABLE_ACK_KEY, useAlarmAcknowledge, context);
				} catch (IllegalArgumentException e) {
					logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":setListeners().enableAckCheckBox.onCheckedChange()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
				}

				updateAcknowledgeWidgets();
			}
		});
	}

	/**
	 * To update acknowledge number <code>EditText</code> with correct value.
	 * 
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 */
	private void updateAcknowledgeNumberEditText() {
		ackNumberEditText.setText(acknowledgeNumber);
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateAcknowledgeNumberEditText()", "Acknowledge number updated. The edittext was set to: " + acknowledgeNumber);
	}

	/**
	 * To update widgets with relations to alarm acknowledgement. These are widgets of type <code>CheckBox</code>, <code>Button</code> and
	 * <code>EditText</code>.
	 * 
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 */
	private void updateAcknowledgeWidgets() {
		// If acknowledge should be used, the button for setting acknowledge phone number should be enabled
		// and the edittext field showing this number should also be "ungreyed"
		if (useAlarmAcknowledge) {
			enableAckCheckBox.setChecked(true);
			ackNumberButton.setEnabled(true);
			ackNumberEditText.setTextColor(Color.BLACK);
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateAcknowledgeWidgets()", "Enable acknowledge checkbox \"Checked\"(" + useAlarmAcknowledge + "), acknowledge number button is \"Enabled\" and acknowledge number edittext is \"Enabled\"");
		} else {
			ackNumberButton.setEnabled(false);
			ackNumberEditText.setTextColor(Color.GRAY);
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateAcknowledgeWidgets()", "Enable acknowledge checkbox \"Unchecked\"(" + useAlarmAcknowledge + "), acknowledge number button is \"Disabled\" and acknowledge number edittext is \"Disabled\"");
		}

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateAcknowledgeWidgets()", "Acknowledge alarm UI widgets updated");
	}

	/**
	 * To build up and display a dialog which let's the user assign an acknowledge phone number.
	 * 
	 * @see #updateAcknowledgeNumberEditText()
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 * @see LogHandler#logCatTxt(LogPriorities, String, String, Throwable)
	 * @see PreferencesHandler#setPrefs(PrefKeys, PrefKeys, Object, Context)
	 */
	private void createAcknowledgeNumberInputDialog() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createAcknowledgeNumberInputDialog()", "Start building dialog for input of acknowledge number");

		// Build up the alert dialog
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		final EditText noBlanksInputEditText = new NoBlanksInputEditText(context);

		// @formatter:off
		// Configure dialog and EditText
		dialog.setIcon(android.R.drawable.ic_dialog_info);				// Set icon
		dialog.setTitle(R.string.NUMBER_PROMPT_TITLE);					// Set title
		dialog.setMessage(R.string.ACK_NUMBER_PROMPT_MESSAGE);			// Set message
		dialog.setCancelable(false);									// Set dialog to non cancelable
		dialog.setView(noBlanksInputEditText);							// Bind dialog to input
		noBlanksInputEditText.setHint(R.string.NUMBER_PROMPT_HINT);		// Set hint to EditText
		noBlanksInputEditText.setInputType(InputType.TYPE_CLASS_PHONE);	// Set input type to EditText
		// @formatter:on

		// Set a positive button and listen on it
		dialog.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createAcknowledgeNumberInputDialog().PositiveButton.OnClickListener().onClick()", "Positive Button pressed");

				// Get the acknoeledge number from the EditText
				acknowledgeNumber = noBlanksInputEditText.getText().toString();

				try {
					// Store to shared preferences
					prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.ACK_NUMBER_KEY, acknowledgeNumber, context);
				} catch (IllegalArgumentException e) {
					logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":createAcknowledgeNumberInputDialog().PositiveButton.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
				}
				// Update affected UI widgets
				updateAcknowledgeNumberEditText();
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createAcknowledgeNumberInputDialog().PositiveButton.OnClickListener().onClick()", "New ACKNOWLEDGE phone number has been stored from user input . New ACKNOWLEDGE phone number is: \"" + acknowledgeNumber + "\"");
			}
		});

		// Set a neutral button, due to documentation it has same functionality as "back" button
		dialog.setNeutralButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// DO NOTHING, except logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createAcknowledgeNumberInputDialog().NeutralButton.OnClickListener().onClick()", "Neutral Button pressed in dialog, nothing done");
			}
		});

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createAcknowledgeNumberInputDialog()", "Showing dialog");

		// Show it
		dialog.show();
	}
}