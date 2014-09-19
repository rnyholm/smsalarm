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

import com.actionbarsherlock.app.SherlockFragment;

/**
 * <code>Fragment</code> containing all the views and user interface widgets for the <b><i>Other Settings</i></b>. Fragment does also contain all
 * logic for the widgets.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class OtherSettingsFragment extends SherlockFragment implements ApplicationFragment {
	private final String LOG_TAG = getClass().getSimpleName();

	// Objects needed for logging, shared preferences handling and noise handling
	private final LogHandler logger = LogHandler.getInstance();
	private final PreferencesHandler prefHandler = PreferencesHandler.getInstance();

	// Must have the application context
	private Context context;

	// The EditText...
	private EditText rescueServiceEditText;

	// ...Button...
	private Button editRescueServiceButton;

	// ...CheckBox...
	private CheckBox enableSmsAlarmCheckBox;

	// ...and TextView
	private TextView enableSmsAlarmInfoTextView;

	// To store the name of rescue service, or organization
	private String rescueService = "";

	// Indicating whether Sms Alarm should be enabled or not
	private boolean enableSmsAlarm = true;

	/**
	 * To create a new <code>OtherSettingsFragment</code>.
	 * 
	 * @param context
	 *            Context
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 */
	public OtherSettingsFragment() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":OtherSettingsFragment()", "Creating a new Other settings fragment");
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
		View view = inflater.inflate(R.layout.other_settings, container, false);

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
		rescueServiceEditText = (EditText) view.findViewById(R.id.rescueServiceName_et);

		// Finding Button view
		editRescueServiceButton = (Button) view.findViewById(R.id.editRescueServiceName_btn);

		// Finding CheckBox view
		enableSmsAlarmCheckBox = (CheckBox) view.findViewById(R.id.enableSmsAlarm_chk);

		// Finding TextView, view
		enableSmsAlarmInfoTextView = (TextView) view.findViewById(R.id.enableSmsAlarmHint_tv);

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
				// Logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "The device has german(de) locale, set different margin-top on information TextViews for the checkboxes than other locales");
				// -6dp calculated to pixels
				pixelsTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -6, resources.getDisplayMetrics());
			} else {
				// -9dp calculated to pixels
				pixelsTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -9, resources.getDisplayMetrics());
			}

			// Wrap content, both on height and width
			// Set layout parameters for the enable sms alarm info textview
			RelativeLayout.LayoutParams paramsEnableSmsAlarmInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			paramsEnableSmsAlarmInfoTextView.setMargins(pixelsLeft, pixelsTop, pixelsRight, 0);
			paramsEnableSmsAlarmInfoTextView.addRule(RelativeLayout.BELOW, enableSmsAlarmCheckBox.getId());
			paramsEnableSmsAlarmInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, enableSmsAlarmCheckBox.getId());

			// Apply the previously configured layout parameters to the correct textviews
			enableSmsAlarmInfoTextView.setLayoutParams(paramsEnableSmsAlarmInfoTextView);

			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "API level > 16, edit margins on information TextViews for the checkboxes");
		} else { // The device has API level < 17, we just need to check if the locale is german
			// If the locale on device is german(de) we need to adjust the margin top for the information textviews for the checkboxes to -6dp
			if ("de".equals(Locale.getDefault().getLanguage())) {
				Resources resources = getResources();

				int pixelsLeft = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 38, resources.getDisplayMetrics());
				int pixelsRight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, resources.getDisplayMetrics());
				int pixelsTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -6, resources.getDisplayMetrics());

				RelativeLayout.LayoutParams paramsEnableSmsAlarmInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				paramsEnableSmsAlarmInfoTextView.setMargins(pixelsLeft, pixelsTop, pixelsRight, 0);
				paramsEnableSmsAlarmInfoTextView.addRule(RelativeLayout.BELOW, enableSmsAlarmCheckBox.getId());
				paramsEnableSmsAlarmInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, enableSmsAlarmCheckBox.getId());

				enableSmsAlarmInfoTextView.setLayoutParams(paramsEnableSmsAlarmInfoTextView);

				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "API level < 17 but the device has german(de) locale, set different margin-top on information TextViews for the checkboxes to fit the language");
			}
		}

		// Set some attributes to the fireDepartmentEditText
		rescueServiceEditText.setEnabled(false);
		rescueServiceEditText.setClickable(false);
		rescueServiceEditText.setFocusable(false);
		rescueServiceEditText.setBackgroundColor(Color.WHITE);
		rescueServiceEditText.setTextColor(Color.BLACK);

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "All Views found for Fragment:\"" + LOG_TAG + "\"");
	}

	@Override
	public void fetchSharedPrefs() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":fetchSharedPrefs()", "Start fetching shared preferences needed by this fragment");

		try {
			enableSmsAlarm = (Boolean) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.ENABLE_SMS_ALARM_KEY, DataTypes.BOOLEAN, context, true);
			rescueService = (String) prefHandler.getPrefs(PrefKeys.SHARED_PREF, PrefKeys.RESCUE_SERVICE_KEY, DataTypes.STRING, context);
		} catch (IllegalArgumentException e) {
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":fetchSharedPrefs()", "An unsupported datatype was given as argument to PreferencesHandler.getPrefs()", e);
		}

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":fetchSharedPrefs()", "Shared preferences fetched");
	}

	@Override
	public void updateFragmentView() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateFragmentView()", "Whole fragments user interface is about to be updated");

		updateRescueServiceEditText();
		updateEnableSmsAlarmCheckBox();

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateFragmentView()", "Fragment updated");
	}

	@Override
	public void setListeners() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners()", "Setting listeners to the different user interface widgets");

		// Set listener to editRescueServiceButton
		editRescueServiceButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().editRescueServiceButton.OnClickListener().onClick()", "Edit rescue service button pressed");
				createRescueServiceInputDialog();
			}
		});

		// Set listener to enableSmsAlarmCheckBox
		enableSmsAlarmCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().enableSmsAlarmCheckBox.onCheckedChange()", "Enable Sms Alarm checkbox pressed(or checkbox initialized)");

				// Set CheckBox depending on it's checked status and store variable
				if (enableSmsAlarmCheckBox.isChecked()) {
					// Store value to variable
					enableSmsAlarm = true;
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().enableSmsAlarmCheckBox.onCheckedChange()", "Enable SmsAlarm checkbox \"Checked\"(" + enableSmsAlarm + ")");
				} else {
					enableSmsAlarm = false;
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().enableSmsAlarmCheckBox.onCheckedChange()", "Enable SmsAlarm checkbox \"Unchecked\"(" + enableSmsAlarm + ")");
				}

				try {
					// Store value to shared preferences
					prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.ENABLE_SMS_ALARM_KEY, enableSmsAlarm, context);
				} catch (IllegalArgumentException e) {
					logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":setListeners().enableSmsAlarmCheckBox.onCheckedChange()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
				}
			}
		});
	}

	private void createRescueServiceInputDialog() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createRescueServiceInputDialog()", "Start building dialog for input of rescue service");

		// Build up the alert dialog
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		final EditText inputEditText = new EditText(context);

		// @formatter:off
		// Configure dialog and EditText
		dialog.setIcon(android.R.drawable.ic_dialog_info);			// Set icon
		dialog.setTitle(R.string.RESCUE_SERVICE_PROMPT_TITLE);		// Set title
		dialog.setMessage(R.string.RESCUE_SERVICE_PROMPT_MESSAGE);	// Set message
		dialog.setCancelable(false);								// Set dialog to non cancelable
		dialog.setView(inputEditText);								// Bind dialog to EditText
		inputEditText.setHint(R.string.RESCUE_SERVICE_NAME_HINT);	// Set hint to EditText
		inputEditText.setInputType(InputType.TYPE_CLASS_TEXT);		// Set input type to EditText
		// @formatter:on

		// Set a positive button and listen on it
		dialog.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createRescueServiceInputDialog().PositiveButton.OnClickListener().onClick()", "Positive Button pressed");

				// Get the rescue service from the EditText
				rescueService = inputEditText.getText().toString();

				try {
					// Store to shared preferences
					prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.RESCUE_SERVICE_KEY, rescueService, context);
				} catch (IllegalArgumentException e) {
					logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":createRescueServiceInputDialog().PositiveButton.OnClickListener().onClick()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
				}

				// Update affected UI widgets
				updateRescueServiceEditText();
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createRescueServiceInputDialog().PositiveButton.OnClickListener().onClick()", "New RESCUESERVICE name has been stored from user input . New RESCUESERVICE name is: \"" + rescueService + "\"");
			}
		});

		// Set a neutral button, due to documentation it has same functionality as "back" button
		dialog.setNeutralButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// DO NOTHING, except logging
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createRescueServiceInputDialog().NeutralButton.OnClickListener().onClick()", "Neutral Button pressed in dialog, nothing done");
			}
		});

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":createRescueServiceInputDialog()", "Showing dialog");

		// Show it
		dialog.show();
	}

	/**
	 * To update rescue service <code>EditText</code> with correct value.
	 * 
	 * @see LogHandler.logCat(int, String , String)
	 */
	private void updateRescueServiceEditText() {
		rescueServiceEditText.setText(rescueService);
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateRescueServiceEditText()", "Rescue service was updated. The edittext was set to: " + rescueService);
	}

	/**
	 * To update enable Sms Alarm <code>CheckBox</code> correctly.
	 * 
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 */
	private void updateEnableSmsAlarmCheckBox() {
		// Update enable Sms Alarm CheckBox(default checked=true)
		if (!enableSmsAlarm) {
			enableSmsAlarmCheckBox.setChecked(false);
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateEnableSmsAlarmCheckBox()", "Enable SmsAlarm checkbox \"Unchecked\"(" + enableSmsAlarm + ")");
		} else {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateEnableSmsAlarmCheckBox()", "Enable SmsAlarm checkbox \"Checked\"(" + enableSmsAlarm + ")");
		}

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateEnableSmsAlarmCheckBox()", "Enable SmsAlarm checkbox updated");
	}
}