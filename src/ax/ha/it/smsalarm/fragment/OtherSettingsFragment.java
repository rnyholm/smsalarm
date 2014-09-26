package ax.ha.it.smsalarm.fragment;

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
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
import ax.ha.it.smsalarm.fragment.dialog.RescueServiceDialog;
import ax.ha.it.smsalarm.handler.LogHandler;
import ax.ha.it.smsalarm.handler.LogHandler.LogPriorities;
import ax.ha.it.smsalarm.handler.PreferencesHandler;
import ax.ha.it.smsalarm.handler.PreferencesHandler.DataTypes;
import ax.ha.it.smsalarm.handler.PreferencesHandler.PrefKeys;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * {@link Fragment} containing all the views and user interface widgets for the <b><i>Other Settings</i></b>. <code>Fragment</code> does also contain
 * all logic for the widgets.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class OtherSettingsFragment extends SherlockFragment implements ApplicationFragment {
	private static final String LOG_TAG = OtherSettingsFragment.class.getSimpleName();

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
	 * To create a new instance of {@link OtherSettingsFragment}.
	 */
	public OtherSettingsFragment() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":OtherSettingsFragment()", "Creating a new Other settings fragment");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate()", "Setting Context to fragment");

		// Set context here, it's safe because this fragment has been attached to it's container, hence we have access to context
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

		editRescueServiceButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().editRescueServiceButton.OnClickListener().onClick()", "Edit rescue service button pressed");

				RescueServiceDialog dialog = new RescueServiceDialog();
				dialog.setTargetFragment(OtherSettingsFragment.this, RescueServiceDialog.RESCUE_SERVICE_DIALOG_REQUEST_CODE);
				dialog.show(getFragmentManager(), RescueServiceDialog.RESCUE_SERVICE_DIALOG_TAG);
			}
		});

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

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onActivityResult()", "Handling of activity result is about to begin, request code: \"" + Integer.toString(requestCode) + "\" and result code: \"" + Integer.toString(resultCode) + "\"");

		// Only interested in OK results, don't care at all about the others
		if (resultCode == Activity.RESULT_OK) {
			// Only interested in certain request codes...
			switch (requestCode) {
				case (RescueServiceDialog.RESCUE_SERVICE_DIALOG_REQUEST_CODE):
					rescueService = data.getStringExtra(RescueServiceDialog.RESCUE_SERVICE);

					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onActivityResult()", "Result code: \"" + Activity.RESULT_OK + "\"(Activity.RESULT_OK), data: \"" + rescueService + "\" fetched using key: \"" + RescueServiceDialog.RESCUE_SERVICE + "\" is about to be persisted into shared preferences");

					try {
						// Store to shared preferences
						prefHandler.setPrefs(PrefKeys.SHARED_PREF, PrefKeys.RESCUE_SERVICE_KEY, rescueService, context);
					} catch (IllegalArgumentException e) {
						logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":onActivityResult()", "An Object of unsupported instance was given as argument to PreferencesHandler.setPrefs()", e);
					}

					// Update affected UI widgets
					updateRescueServiceEditText();

					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onActivityResult()", "New RESCUE SERVICE name has been stored from user input . New RESCUE SERVICE name is: \"" + rescueService + "\"");
					break;
				default:
					logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":onActivityResult()", "An unsupported result occurred, result code: \"" + Integer.toString(resultCode) + "\" and request code: \"" + requestCode + "\"");
			}
		}
	}

	/**
	 * To update rescue service <code>EditText</code> with correct value.
	 */
	private void updateRescueServiceEditText() {
		rescueServiceEditText.setText(rescueService);
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateRescueServiceEditText()", "Rescue service was updated. The edittext was set to: " + rescueService);
	}

	/**
	 * To update enable Sms Alarm <code>CheckBox</code> correctly.
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