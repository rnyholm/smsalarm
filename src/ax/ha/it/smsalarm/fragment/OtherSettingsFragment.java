package ax.ha.it.smsalarm.fragment;

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import ax.ha.it.smsalarm.activity.SmsAlarm;
import ax.ha.it.smsalarm.fragment.dialog.RescueServiceDialog;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.DataType;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.PrefKey;

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

	// Need to handle shared shared preferences
	private final SharedPreferencesHandler prefHandler = SharedPreferencesHandler.getInstance();

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
		// Just empty...
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set context here, it's safe because this fragment has been attached to it's container, hence we have access to context
		context = getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
			// If the locale on device is German(DE) set pixels top to -6dp else -9dp
			if ("de".equals(Locale.getDefault().getLanguage())) {
				// -6dp calculated to pixels
				pixelsTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -6, resources.getDisplayMetrics());
			} else {
				// -9dp calculated to pixels
				pixelsTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -9, resources.getDisplayMetrics());
			}

			// Wrap content, both on height and width
			// Set layout parameters for the enable Sms Alarm info TextView
			RelativeLayout.LayoutParams paramsEnableSmsAlarmInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			paramsEnableSmsAlarmInfoTextView.setMargins(pixelsLeft, pixelsTop, pixelsRight, 0);
			paramsEnableSmsAlarmInfoTextView.addRule(RelativeLayout.BELOW, enableSmsAlarmCheckBox.getId());
			paramsEnableSmsAlarmInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, enableSmsAlarmCheckBox.getId());

			// Apply the previously configured layout parameters to the correct TextViews
			enableSmsAlarmInfoTextView.setLayoutParams(paramsEnableSmsAlarmInfoTextView);
		} else { // The device has API level < 17, we just need to check if the locale is German
			// If the locale on device is German(DE) we need to adjust the margin top for the information TextViews for the CheckBoxes to -6dp
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
			}
		}

		// Set some attributes to the fireDepartmentEditText
		rescueServiceEditText.setEnabled(false);
		rescueServiceEditText.setClickable(false);
		rescueServiceEditText.setFocusable(false);
		rescueServiceEditText.setBackgroundColor(Color.WHITE);
		rescueServiceEditText.setTextColor(Color.BLACK);
	}

	@Override
	public void fetchSharedPrefs() {
		enableSmsAlarm = (Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.ENABLE_SMS_ALARM_KEY, DataType.BOOLEAN, context);
		rescueService = (String) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.RESCUE_SERVICE_KEY, DataType.STRING, context);
	}

	@Override
	public void updateFragmentView() {
		updateRescueServiceEditText();
		updateEnableSmsAlarmCheckBox();
	}

	@Override
	public void setListeners() {
		// Set listener to Edit Rescue Service Button
		editRescueServiceButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				RescueServiceDialog dialog = new RescueServiceDialog();
				dialog.setTargetFragment(OtherSettingsFragment.this, RescueServiceDialog.RESCUE_SERVICE_DIALOG_REQUEST_CODE);
				dialog.show(getFragmentManager(), RescueServiceDialog.RESCUE_SERVICE_DIALOG_TAG);
			}
		});

		// Set listener to Enable Sms Alarm CheckBox
		enableSmsAlarmCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// Set CheckBox depending on it's checked status and store variable
				if (enableSmsAlarmCheckBox.isChecked()) {
					enableSmsAlarm = true;
				} else {
					enableSmsAlarm = false;
				}

				// Store value to shared preferences
				prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.ENABLE_SMS_ALARM_KEY, enableSmsAlarm, context);
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Only interested in OK results, don't care at all about the others
		if (resultCode == Activity.RESULT_OK) {
			// Only interested in certain request codes...
			switch (requestCode) {
				case (RescueServiceDialog.RESCUE_SERVICE_DIALOG_REQUEST_CODE):
					rescueService = data.getStringExtra(RescueServiceDialog.RESCUE_SERVICE);

					// Store to shared preferences
					prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.RESCUE_SERVICE_KEY, rescueService, context);

					// Update affected UI widgets
					updateRescueServiceEditText();

					break;
				default:
					if (SmsAlarm.DEBUG) {
						Log.e(LOG_TAG + ":onActivityResult()", "An unsupported result occurred, result code: \"" + resultCode + "\" and request code: \"" + requestCode + "\"");
					}
			}
		}
	}

	/**
	 * To update rescue service {@link EditText} with correct value.
	 */
	private void updateRescueServiceEditText() {
		rescueServiceEditText.setText(rescueService);
	}

	/**
	 * To update enable Sms Alarm {@link CheckBox} correctly.
	 */
	private void updateEnableSmsAlarmCheckBox() {
		// Update enable Sms Alarm CheckBox(default checked=true)
		if (!enableSmsAlarm) {
			enableSmsAlarmCheckBox.setChecked(false);
		}
	}
}