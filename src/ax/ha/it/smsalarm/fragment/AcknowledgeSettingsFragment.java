/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.fragment;

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.activity.Acknowledge.AcknowledgeMethod;
import ax.ha.it.smsalarm.activity.SmsAlarm;
import ax.ha.it.smsalarm.fragment.dialog.AcknowledgeMessageDialog;
import ax.ha.it.smsalarm.fragment.dialog.AcknowledgeNumberDialog;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.DataType;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.PrefKey;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * {@link Fragment} containing all the views and user interface widgets for the <b><i>Acknowledge Settings</i></b>. <code>Fragment</code> does also
 * contain all logic for the widgets.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class AcknowledgeSettingsFragment extends SherlockFragment implements ApplicationFragment {
	private static final String LOG_TAG = AcknowledgeSettingsFragment.class.getSimpleName();

	// To handle shared preferences
	private final SharedPreferencesHandler prefHandler = SharedPreferencesHandler.getInstance();

	// Must have the application context
	private Context context;

	// The EditText...
	private EditText acknowledgeNumberEditText;
	private EditText acknowledgeMessageEditText;

	// ...Button...
	private Button acknowledgeNumberButton;
	private Button acknowledgeMessageButton;

	// ...CheckBox...
	private CheckBox enableAcknowledgeCheckBox;

	// ...TextView...
	private TextView enableAcknowledgeInfoTextView;

	// ...and Spinner
	private Spinner acknowledgeMethodSpinner;

	// Indicating whether acknowledgement of alarm should be used or not
	private boolean useAlarmAcknowledge = false;

	// What number and message that should be used while acknowledge
	private String acknowledgeNumber = "";
	private String acknowledgeMessage = "";

	// What method acknowledgement should be done with, CALL is default
	private AcknowledgeMethod acknowledgeMethod = AcknowledgeMethod.CALL;

	/**
	 * To create a new instance of {@link AcknowledgeSettingsFragment}.
	 */
	public AcknowledgeSettingsFragment() {
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
		// Finding EditText views
		acknowledgeNumberEditText = (EditText) view.findViewById(R.id.ackSettingsAckNumber_et);
		acknowledgeMessageEditText = (EditText) view.findViewById(R.id.ackSettingsAckMessage_et);

		// Finding Button view
		acknowledgeNumberButton = (Button) view.findViewById(R.id.editAckNumber_btn);
		acknowledgeMessageButton = (Button) view.findViewById(R.id.editAckMessage_btn);

		// Finding CheckBox view
		enableAcknowledgeCheckBox = (CheckBox) view.findViewById(R.id.ackSettingsAckEnable_chk);

		// Finding TextView
		enableAcknowledgeInfoTextView = (TextView) view.findViewById(R.id.ackSettingsAckEnableHint_tv);

		// Finding Spinner
		acknowledgeMethodSpinner = (Spinner) view.findViewById(R.id.ackSettingsAckMethodSpinner_sp);

		// Populate the spinner with items directly after it has been found
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.ack_methods, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		acknowledgeMethodSpinner.setAdapter(adapter);

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

			// Set layout parameters for the enable acknowledge info TextView
			// Wrap content, both on height and width
			RelativeLayout.LayoutParams paramsEnableAckInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			paramsEnableAckInfoTextView.setMargins(pixelsLeft, pixelsTop, 0, 0);
			paramsEnableAckInfoTextView.addRule(RelativeLayout.BELOW, enableAcknowledgeCheckBox.getId());
			paramsEnableAckInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, enableAcknowledgeCheckBox.getId());

			// Apply the previously configured layout parameters to the correct TextViews
			enableAcknowledgeInfoTextView.setLayoutParams(paramsEnableAckInfoTextView);
		} else { // The device has API level < 17, we just need to check if the locale is German
			// If the locale on device is German(DE) we need to adjust the margin top for the information TextViews for the CheckBoxes to -6dp
			if ("de".equals(Locale.getDefault().getLanguage())) {
				Resources resources = getResources();

				int pixelsLeft = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 38, resources.getDisplayMetrics());
				int pixelsTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -6, resources.getDisplayMetrics());

				RelativeLayout.LayoutParams paramsEnableAckInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				paramsEnableAckInfoTextView.setMargins(pixelsLeft, pixelsTop, 0, 0);
				paramsEnableAckInfoTextView.addRule(RelativeLayout.BELOW, enableAcknowledgeCheckBox.getId());
				paramsEnableAckInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, enableAcknowledgeCheckBox.getId());

				enableAcknowledgeInfoTextView.setLayoutParams(paramsEnableAckInfoTextView);
			}
		}

		// Set some attributes to the EditTexts
		acknowledgeNumberEditText.setEnabled(false);
		acknowledgeNumberEditText.setClickable(false);
		acknowledgeNumberEditText.setFocusable(false);
		acknowledgeNumberEditText.setBackgroundColor(Color.WHITE);

		acknowledgeMessageEditText.setEnabled(false);
		acknowledgeMessageEditText.setClickable(false);
		acknowledgeMessageEditText.setFocusable(false);
		acknowledgeMessageEditText.setBackgroundColor(Color.WHITE);
	}

	@Override
	public void fetchSharedPrefs() {
		useAlarmAcknowledge = (Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.ENABLE_ACK_KEY, DataType.BOOLEAN, context);
		acknowledgeNumber = (String) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.ACK_NUMBER_KEY, DataType.STRING, context);
		acknowledgeMessage = (String) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.ACK_MESSAGE_KEY, DataType.STRING, context);
		acknowledgeMethod = AcknowledgeMethod.of((Integer) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.ACK_METHOD_KEY, DataType.INTEGER, context));
	}

	@Override
	public void updateFragmentView() {
		// Update widgets in relation to alarm acknowledgment
		updateAcknowledgeNumberEditText();
		updateAcknowledgeMessageEditText();
		updateAcknowledgeMethodSpinner();
		updateAcknowledgeWidgets();
	}

	@Override
	public void setListeners() {
		// Set listener to the acknowledge number button
		acknowledgeNumberButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AcknowledgeNumberDialog dialog = new AcknowledgeNumberDialog();
				dialog.setTargetFragment(AcknowledgeSettingsFragment.this, AcknowledgeNumberDialog.ACKNOWLEDGE_NUMBER_DIALOG_REQUEST_CODE);
				dialog.show(getFragmentManager(), AcknowledgeNumberDialog.ACKNOWLEDGE_NUMBER_DIALOG_TAG);
			}
		});

		// Set listener to the acknowledge message button
		acknowledgeMessageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AcknowledgeMessageDialog dialog = new AcknowledgeMessageDialog();
				dialog.setTargetFragment(AcknowledgeSettingsFragment.this, AcknowledgeMessageDialog.ACKNOWLEDGE_MESSAGE_DIALOG_REQUEST_CODE);
				dialog.show(getFragmentManager(), AcknowledgeMessageDialog.ACKNOWLEDGE_MESSAGE_DIALOG_TAG);
			}
		});

		// Set listener to the acknowledge method spinner
		acknowledgeMethodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// Get the new acknowledge method
				acknowledgeMethod = AcknowledgeMethod.of(acknowledgeMethodSpinner.getSelectedItemPosition());

				// Persist the acknowledge method
				prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.ACK_METHOD_KEY, acknowledgeMethod.ordinal(), context);

				// Update affected UI widgets
				updateAcknowledgeWidgets();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// DO NOTHING!
			}
		});

		// Set listener to Enable Acknowledge CheckBox
		enableAcknowledgeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// Set CheckBox depending on it's checked status and store variable
				if (enableAcknowledgeCheckBox.isChecked()) {
					useAlarmAcknowledge = true;
				} else {
					useAlarmAcknowledge = false;
				}

				// Persist the recently set value
				prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.ENABLE_ACK_KEY, useAlarmAcknowledge, context);

				// Update affected UI widgets
				updateAcknowledgeWidgets();
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Only interested in OK results, don't care at all about the others
		if (resultCode == Activity.RESULT_OK) {
			// Only interested in certain request codes...
			switch (requestCode) {
				case (AcknowledgeNumberDialog.ACKNOWLEDGE_NUMBER_DIALOG_REQUEST_CODE):
					acknowledgeNumber = data.getStringExtra(AcknowledgeNumberDialog.ACKNOWLEDGE_NUMBER);

					prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.ACK_NUMBER_KEY, acknowledgeNumber, context);
					updateAcknowledgeNumberEditText();

					break;
				case (AcknowledgeMessageDialog.ACKNOWLEDGE_MESSAGE_DIALOG_REQUEST_CODE):
					acknowledgeMessage = data.getStringExtra(AcknowledgeMessageDialog.ACKNOWLEDGE_MESSAGE);

					prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.ACK_MESSAGE_KEY, acknowledgeMessage, context);
					updateAcknowledgeMessageEditText();

					break;
				default:
					if (SmsAlarm.DEBUG) {
						Log.e(LOG_TAG + ":onActivityResult()", "An unsupported result occurred, result code: \"" + resultCode + "\" and request code: \"" + requestCode + "\"");
					}
			}
		}
	}

	/**
	 * To update acknowledge number {@link EditText} with correct value.
	 */
	private void updateAcknowledgeNumberEditText() {
		acknowledgeNumberEditText.setText(acknowledgeNumber);
	}

	/**
	 * To update acknowledge message {@link EditText} with correct value.
	 */
	private void updateAcknowledgeMessageEditText() {
		acknowledgeMessageEditText.setText(acknowledgeMessage);
	}

	/**
	 * To update acknowledge method {@link Spinner} by setting correct selection.
	 */
	private void updateAcknowledgeMethodSpinner() {
		try {
			acknowledgeMethodSpinner.setSelection(acknowledgeMethod.ordinal());
		} catch (IndexOutOfBoundsException e) {
			if (SmsAlarm.DEBUG) {
				Log.e(LOG_TAG + ":updateAcknowledgeMethodSpinner()", "An acknowledge method with an index outside the spinners bounds has been set. Acknowledge method: \"" + acknowledgeMethod.toString() + "\"(ordinal:\"" + acknowledgeMethod.ordinal() + "\")", e);
			}
		}
	}

	/**
	 * To update widgets with relations to alarm acknowledgement. These are widgets of type {@link CheckBox}, {@link Button}, {@link EditText} and
	 * {@link Spinner}.
	 */
	private void updateAcknowledgeWidgets() {
		// Set CheckBox state and acknowledge method Spinner enabled depending on if acknowledgement should be used or not
		enableAcknowledgeCheckBox.setChecked(useAlarmAcknowledge);
		acknowledgeMethodSpinner.setEnabled(useAlarmAcknowledge);

		// Set states and look of the user interface depending on if acknowledgement should be used or not
		if (useAlarmAcknowledge && (AcknowledgeMethod.CALL.equals(acknowledgeMethod) || AcknowledgeMethod.SMS.equals(acknowledgeMethod))) {
			acknowledgeNumberButton.setEnabled(true);
			acknowledgeNumberEditText.setTextColor(Color.BLACK);

			// Only if user wants to use acknowledge by SMS
			if (AcknowledgeMethod.SMS.equals(acknowledgeMethod)) {
				acknowledgeMessageButton.setEnabled(true);
				acknowledgeMessageEditText.setTextColor(Color.BLACK);
			} else {
				acknowledgeMessageButton.setEnabled(false);
				acknowledgeMessageEditText.setTextColor(Color.GRAY);
			}
		} else {
			acknowledgeNumberButton.setEnabled(false);
			acknowledgeNumberEditText.setTextColor(Color.GRAY);
			acknowledgeMessageButton.setEnabled(false);
			acknowledgeMessageEditText.setTextColor(Color.GRAY);
		}
	}
}