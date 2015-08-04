/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.fragment;

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import ax.ha.it.smsalarm.fragment.dialog.OrganizationDialog;
import ax.ha.it.smsalarm.handler.CameraHandler;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.DataType;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.PrefKey;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.common.base.Optional;

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
	private EditText organizationEditText;

	// ...Button...
	private Button editOrganizationButton;

	// ...CheckBoxes...
	private CheckBox enableSmsAlarmCheckBox;
	private CheckBox enableSMSDebugLoggingCheckBox;
	private CheckBox useFlashNotificationCheckBox;

	// ...and TextViews
	private TextView enableSmsAlarmInfoTextView;
	private TextView enableSMSDebugLoggingInfoTextView;
	private TextView useFlashNotificationInfoTextView;

	// To store the name of organization
	private String organization = "";

	// To indicate whether Sms Alarm should be enabled or not, if SMS debug logging and flash notifications should be used
	private boolean enableSmsAlarm = true;
	private boolean enableSMSDebugLogging = false;
	private boolean useFlashNotification = false;

	// Could contain an error describing why Flash Notification isn't supported, if absent it's supported
	private Optional<String> flashNotificationSupportError;

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
		// Check the support for flash notification and store the result
		flashNotificationSupportError = checkFlashNotificationSupport();
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
		organizationEditText = (EditText) view.findViewById(R.id.organization_et);

		// Finding Button view
		editOrganizationButton = (Button) view.findViewById(R.id.editOrganization_btn);

		// Finding CheckBox views
		enableSmsAlarmCheckBox = (CheckBox) view.findViewById(R.id.enableSmsAlarm_chk);
		enableSMSDebugLoggingCheckBox = (CheckBox) view.findViewById(R.id.enableSMSDebugLogging_chk);
		useFlashNotificationCheckBox = (CheckBox) view.findViewById(R.id.useFlashNotification_chk);

		// Finding TextView, views
		enableSmsAlarmInfoTextView = (TextView) view.findViewById(R.id.enableSmsAlarmHint_tv);
		enableSMSDebugLoggingInfoTextView = (TextView) view.findViewById(R.id.enableSMSDebugLoggingHint_tv);
		useFlashNotificationInfoTextView = (TextView) view.findViewById(R.id.useFlashNotificationHint_tv);

		// If Android API level is greater than Jelly Bean
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
			// We need to get some Android resources in order to calculate proper pixel dimensions from dp
			Resources resources = getResources();

			// Calculate pixel dimensions for the different margins
			// 32dp calculated to pixels
			int pixelsLeft = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, resources.getDisplayMetrics());
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
			paramsEnableSmsAlarmInfoTextView.setMargins(pixelsLeft, pixelsTop, 0, 0);
			paramsEnableSmsAlarmInfoTextView.addRule(RelativeLayout.BELOW, enableSmsAlarmCheckBox.getId());
			paramsEnableSmsAlarmInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, enableSmsAlarmCheckBox.getId());

			// Set layout parameters for the enable SMS debug logging info TextView
			RelativeLayout.LayoutParams paramsEnableSmsDebugLoggingInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			paramsEnableSmsDebugLoggingInfoTextView.setMargins(pixelsLeft, pixelsTop, 0, 0);
			paramsEnableSmsDebugLoggingInfoTextView.addRule(RelativeLayout.BELOW, enableSMSDebugLoggingCheckBox.getId());
			paramsEnableSmsDebugLoggingInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, enableSMSDebugLoggingCheckBox.getId());

			// Set layout parameters for the use flash notification info TextView
			RelativeLayout.LayoutParams paramsUseFlashNotificationInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			paramsUseFlashNotificationInfoTextView.setMargins(pixelsLeft, pixelsTop, 0, 0);
			paramsUseFlashNotificationInfoTextView.addRule(RelativeLayout.BELOW, useFlashNotificationCheckBox.getId());
			paramsUseFlashNotificationInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, useFlashNotificationCheckBox.getId());

			// Apply the previously configured layout parameters to the correct TextViews
			enableSmsAlarmInfoTextView.setLayoutParams(paramsEnableSmsAlarmInfoTextView);
			enableSMSDebugLoggingInfoTextView.setLayoutParams(paramsEnableSmsDebugLoggingInfoTextView);
			useFlashNotificationInfoTextView.setLayoutParams(paramsUseFlashNotificationInfoTextView);
		} else { // The device has API level < 17, we just need to check if the locale is German
			// If the locale on device is German(DE) we need to adjust the margin top for the information TextViews for the CheckBoxes to -6dp
			if ("de".equals(Locale.getDefault().getLanguage())) {
				Resources resources = getResources();

				int pixelsLeft = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 38, resources.getDisplayMetrics());
				int pixelsTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -6, resources.getDisplayMetrics());

				RelativeLayout.LayoutParams paramsEnableSmsAlarmInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				paramsEnableSmsAlarmInfoTextView.setMargins(pixelsLeft, pixelsTop, 0, 0);
				paramsEnableSmsAlarmInfoTextView.addRule(RelativeLayout.BELOW, enableSmsAlarmCheckBox.getId());
				paramsEnableSmsAlarmInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, enableSmsAlarmCheckBox.getId());

				RelativeLayout.LayoutParams paramsEnableSmsDebugLoggingInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				paramsEnableSmsDebugLoggingInfoTextView.setMargins(pixelsLeft, pixelsTop, 0, 0);
				paramsEnableSmsDebugLoggingInfoTextView.addRule(RelativeLayout.BELOW, enableSMSDebugLoggingCheckBox.getId());
				paramsEnableSmsDebugLoggingInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, enableSMSDebugLoggingCheckBox.getId());

				RelativeLayout.LayoutParams paramsUseFlashNotificationInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				paramsUseFlashNotificationInfoTextView.setMargins(pixelsLeft, pixelsTop, 0, 0);
				paramsUseFlashNotificationInfoTextView.addRule(RelativeLayout.BELOW, useFlashNotificationCheckBox.getId());
				paramsUseFlashNotificationInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, useFlashNotificationCheckBox.getId());

				enableSmsAlarmInfoTextView.setLayoutParams(paramsEnableSmsAlarmInfoTextView);
				enableSMSDebugLoggingInfoTextView.setLayoutParams(paramsEnableSmsDebugLoggingInfoTextView);
				useFlashNotificationInfoTextView.setLayoutParams(paramsUseFlashNotificationInfoTextView);
			}
		}

		// Set some attributes to the organization EditText
		organizationEditText.setEnabled(false);
		organizationEditText.setClickable(false);
		organizationEditText.setFocusable(false);
		organizationEditText.setBackgroundColor(Color.WHITE);
		organizationEditText.setTextColor(Color.BLACK);

		// If Flash Notification isn't supported we need to disable the possibilities to enable it and show the reason why it's not enabled
		if (flashNotificationSupportError.isPresent()) {
			useFlashNotificationCheckBox.setEnabled(false);
			useFlashNotificationInfoTextView.setText(flashNotificationSupportError.get());
			useFlashNotificationInfoTextView.setEnabled(false);
		}
	}

	@Override
	public void fetchSharedPrefs() {
		enableSmsAlarm = (Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.ENABLE_SMS_ALARM_KEY, DataType.BOOLEAN, context);
		enableSMSDebugLogging = (Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.ENABLE_SMS_DEBUG_LOGGING, DataType.BOOLEAN, context);
		useFlashNotification = (Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.USE_FLASH_NOTIFICATION, DataType.BOOLEAN, context);
		organization = (String) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.ORGANIZATION_KEY, DataType.STRING, context);
	}

	@Override
	public void updateFragmentView() {
		updateOrganizationEditText();
		updateEnableSmsAlarmCheckBox();
		updateEnableSMSDebugLoggingCheckBox();
		updateUseFlashNotificationCheckBox();
	}

	@Override
	public void setListeners() {
		// Set listener to Edit Organization Button
		editOrganizationButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				OrganizationDialog dialog = OrganizationDialog.newInstance(organization);
				dialog.setTargetFragment(OtherSettingsFragment.this, OrganizationDialog.ORGANIZATION_DIALOG_REQUEST_CODE);
				dialog.show(getFragmentManager(), OrganizationDialog.ORGANIZATION_DIALOG_TAG);
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

		// Set listener to Enable SMS Debug Logging CheckBox
		enableSMSDebugLoggingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (enableSMSDebugLoggingCheckBox.isChecked()) {
					enableSMSDebugLogging = true;
				} else {
					enableSMSDebugLogging = false;
				}

				prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.ENABLE_SMS_DEBUG_LOGGING, enableSMSDebugLogging, context);
			}
		});

		// Set listener to Use Flash Notification CheckBox
		useFlashNotificationCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (useFlashNotificationCheckBox.isChecked()) {
					useFlashNotification = true;
				} else {
					useFlashNotification = false;
				}

				prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.USE_FLASH_NOTIFICATION, useFlashNotification, context);
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Only interested in OK results, don't care at all about the others
		if (resultCode == Activity.RESULT_OK) {
			// Only interested in certain request codes...
			switch (requestCode) {
				case (OrganizationDialog.ORGANIZATION_DIALOG_REQUEST_CODE):
					organization = data.getStringExtra(OrganizationDialog.ORGANIZATION);

					// Store to shared preferences
					prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.ORGANIZATION_KEY, organization, context);

					// Update affected UI widgets
					updateOrganizationEditText();

					break;
				default:
					Log.e(LOG_TAG + ":onActivityResult()", "An unsupported result occurred, result code: \"" + resultCode + "\" and request code: \"" + requestCode + "\"");
			}
		}
	}

	/**
	 * To update organization {@link EditText} with correct value.
	 */
	private void updateOrganizationEditText() {
		organizationEditText.setText(organization);
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

	private void updateEnableSMSDebugLoggingCheckBox() {
		// Update enable SMS Debug Logging CheckBox
		enableSMSDebugLoggingCheckBox.setChecked(enableSMSDebugLogging);
	}

	/**
	 * To update use flash notification {@link CheckBox} correctly.
	 */
	private void updateUseFlashNotificationCheckBox() {
		// Update Use Flash Notification CheckBox
		useFlashNotificationCheckBox.setChecked(useFlashNotification);
	}

	/**
	 * To check whether or not the device fulfills the requirements to dispatch a <b><i>Flash Notification</i></b>.<br>
	 * Following requirements needs to be fulfilled:<br>
	 * - Build version needs to be at least <b><i>GINGERBREAD</i></b><br>
	 * - The device needs to have system feature <b><i>PackageManager.FEATURE_CAMERA</i></b><br>
	 * - The device needs to have system feature <b><i>PackageManager.FEATURE_CAMERA_FLASH</i></b><br>
	 * - The device needs to have a <b><i>Back Facing Camera</i></b>, since the back facing cameras flash will be used
	 * <p>
	 * If and only if the mention requirements are fulfilled the device supports flash notifications, in those cases an {@link Optional#absent()} is
	 * returned.
	 * 
	 * @return An {@link Optional#absent()} if the device supports Flash Notifications, else an {@link Optional#of(Object)} were the
	 *         <code>Object</code> is a {@link String} describing the error will be returned.
	 */
	private Optional<String> checkFlashNotificationSupport() {
		// CameraHandler uses functionality introduced in Gingerbread
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
			return Optional.<String> of(context.getString(R.string.FLASH_NOTIFICATION_NOT_SUPPORTED_MIN_API_LEVEL));
		}

		// Get the package manager in order to figure out if needed system services exists
		PackageManager packageManager = context.getPackageManager();

		// Device doesn't have a camera
		if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			return Optional.<String> of(context.getString(R.string.FLASH_NOTIFICATION_NOT_SUPPORTED_MISSING_CAMERA));
		}

		// Device doesn't have flash
		if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
			return Optional.<String> of(context.getString(R.string.FLASH_NOTIFICATION_NOT_SUPPORTED_MISSING_CAMERA_FLASH));
		}

		// Device doesn't have any back facing camera, note this method uses functionality introduced in Android API Level 9 hence it is important to
		// call this after devices Android version has been checked
		if (!CameraHandler.findBackFacingCamera().isPresent()) {
			return Optional.<String> of(context.getString(R.string.FLASH_NOTIFICATION_NOT_SUPPORTED_MISSING_BACK_FACING_CAMERA));
		}

		// Device does support flash notification returning absent
		return Optional.<String> absent();
	}
}