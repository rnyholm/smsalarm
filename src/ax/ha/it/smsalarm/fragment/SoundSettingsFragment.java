/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.activity.SmsAlarm;
import ax.ha.it.smsalarm.fragment.dialog.AlarmSignalDialog;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.DataType;
import ax.ha.it.smsalarm.handler.SharedPreferencesHandler.PrefKey;
import ax.ha.it.smsalarm.handler.SoundHandler;
import ax.ha.it.smsalarm.util.Util;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * {@link Fragment} containing all the views and user interface widgets for the <b><i>Sound Settings</i></b>. <code>Fragment</code> does also contain
 * all logic for the widgets.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class SoundSettingsFragment extends SherlockFragment implements ApplicationFragment {
	private static final String LOG_TAG = SoundSettingsFragment.class.getSimpleName();

	// Objects needed for shared preferences handling and sound handling
	private final SharedPreferencesHandler prefHandler = SharedPreferencesHandler.getInstance();
	private final SoundHandler soundHandler = SoundHandler.getInstance();

	// Must have the application context
	private Context context;

	// The LinearLayouts
	private LinearLayout primaryAlarmSignalSelectionLinearLayout;
	private LinearLayout secondaryAlarmSignalSelectionLinearLayout;
	private LinearLayout primaryAlarmVibrationSelectionLinearLayout;
	private LinearLayout secondaryAlarmVibrationSelectionLinearLayout;

	// ...CheckBoxes...
	private CheckBox soundSettingCheckBox;
	private CheckBox playAlarmSignalTwiceCheckBox;
	private CheckBox playAlarmSignalRepeatedlyCheckBox;

	// ...and TextViews
	private TextView selectedPrimaryAlarmSignalTextView;
	private TextView selectedSecondaryAlarmSignalTextView;
	private TextView selectedPrimaryAlarmVibrationTextView;
	private TextView selectedSecondaryAlarmVibrationTextView;
	private TextView soundSettingInfoTextView;
	private TextView playAlarmSignalTwiceInfoTextView;
	private TextView playAlarmSignalRepeatedlyInfoTextView;

	// Boolean variables to store whether to use OS sound settings or not, if alarm signal should be played twice or repeatedly
	private boolean useOsSoundSettings = false;
	private boolean playAlarmSignalTwice = false;
	private boolean playAlarmSignalRepeatedly = false;

	// To store the selected alarm signals
	private String primaryAlarmSignal;
	private String secondaryAlarmSignal;

	// List holding the paths to the user added alarm signals
	private List<String> userAddedAlarmSignals = new ArrayList<String>();

	/**
	 * To create a new instance of {@link SoundSettingsFragment}.
	 */
	public SoundSettingsFragment() {
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
		View view = inflater.inflate(R.layout.sound_settings, container, false);

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
		// Finding the LinearLayout views
		primaryAlarmSignalSelectionLinearLayout = (LinearLayout) view.findViewById(R.id.primaryAlarmSignalSelection_ll);
		secondaryAlarmSignalSelectionLinearLayout = (LinearLayout) view.findViewById(R.id.secondaryAlarmSignalSelection_ll);
		primaryAlarmVibrationSelectionLinearLayout = (LinearLayout) view.findViewById(R.id.primaryAlarmVibrationSelection_ll);
		secondaryAlarmVibrationSelectionLinearLayout = (LinearLayout) view.findViewById(R.id.secondaryAlarmVibrationSelection_ll);

		// Finding CheckBox views
		soundSettingCheckBox = (CheckBox) view.findViewById(R.id.useSysSoundSettings_chk);
		playAlarmSignalTwiceCheckBox = (CheckBox) view.findViewById(R.id.playAlarmSignalTwiceSetting_chk);
		playAlarmSignalRepeatedlyCheckBox = (CheckBox) view.findViewById(R.id.playAlarmSignalRepeatedlySetting_chk);

		// Finding TextView, views
		selectedPrimaryAlarmSignalTextView = (TextView) view.findViewById(R.id.selectedPrimaryAlarmSignal_tv);
		selectedSecondaryAlarmSignalTextView = (TextView) view.findViewById(R.id.selectedSecondaryAlarmSignal_tv);
		selectedPrimaryAlarmVibrationTextView = (TextView) view.findViewById(R.id.selectedPrimaryAlarmVibration_tv);
		selectedSecondaryAlarmVibrationTextView = (TextView) view.findViewById(R.id.selectedSecondaryAlarmVibration_tv);
		soundSettingInfoTextView = (TextView) view.findViewById(R.id.useSysSoundSettingsHint_tv);
		playAlarmSignalTwiceInfoTextView = (TextView) view.findViewById(R.id.playAlarmSignalTwiceSettingHint_tv);
		playAlarmSignalRepeatedlyInfoTextView = (TextView) view.findViewById(R.id.playAlarmSignalRepeatedlySettingHint_tv);

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

			// Set layout parameters for the sound settings info TextView
			// Wrap content, both on height and width
			RelativeLayout.LayoutParams paramsSoundSettingInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			// Margins left, top, right, bottom
			paramsSoundSettingInfoTextView.setMargins(pixelsLeft, pixelsTop, pixelsRight, 0);
			// Add rule, below UI widget
			paramsSoundSettingInfoTextView.addRule(RelativeLayout.BELOW, soundSettingCheckBox.getId());
			// Add rule, align left of UI widget
			paramsSoundSettingInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, soundSettingCheckBox.getId());

			// Set layout parameters for the play alarm signal twice TextView
			RelativeLayout.LayoutParams paramsPlayAlarmSignalTwiceInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			paramsPlayAlarmSignalTwiceInfoTextView.setMargins(pixelsLeft, pixelsTop, pixelsRight, 0);
			paramsPlayAlarmSignalTwiceInfoTextView.addRule(RelativeLayout.BELOW, playAlarmSignalTwiceCheckBox.getId());
			paramsPlayAlarmSignalTwiceInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, playAlarmSignalTwiceCheckBox.getId());

			// Set layout parameters for the play alarm signal repeatedly TextView
			RelativeLayout.LayoutParams paramsPlayAlarmSignalRepeatedlyInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			paramsPlayAlarmSignalRepeatedlyInfoTextView.setMargins(pixelsLeft, pixelsTop, pixelsRight, 0);
			paramsPlayAlarmSignalRepeatedlyInfoTextView.addRule(RelativeLayout.BELOW, playAlarmSignalRepeatedlyCheckBox.getId());
			paramsPlayAlarmSignalRepeatedlyInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, playAlarmSignalRepeatedlyCheckBox.getId());

			// Apply the previously configured layout parameters to the correct TextViews
			soundSettingInfoTextView.setLayoutParams(paramsSoundSettingInfoTextView);
			playAlarmSignalTwiceInfoTextView.setLayoutParams(paramsPlayAlarmSignalTwiceInfoTextView);
			playAlarmSignalRepeatedlyInfoTextView.setLayoutParams(paramsPlayAlarmSignalRepeatedlyInfoTextView);
		} else { // The device has API level < 17, we just need to check if the locale is German
			// If the locale on device is German(DE) we need to adjust the margin top for the information TextViews for the CheckBoxes to -6dp
			if ("de".equals(Locale.getDefault().getLanguage())) {
				Resources resources = getResources();

				int pixelsLeft = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 38, resources.getDisplayMetrics());
				int pixelsRight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, resources.getDisplayMetrics());
				int pixelsTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -6, resources.getDisplayMetrics());

				RelativeLayout.LayoutParams paramsSoundSettingInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				paramsSoundSettingInfoTextView.setMargins(pixelsLeft, pixelsTop, pixelsRight, 0);
				paramsSoundSettingInfoTextView.addRule(RelativeLayout.BELOW, soundSettingCheckBox.getId());
				paramsSoundSettingInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, soundSettingCheckBox.getId());

				RelativeLayout.LayoutParams paramsPlayAlarmSignalTwiceInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				paramsPlayAlarmSignalTwiceInfoTextView.setMargins(pixelsLeft, pixelsTop, pixelsRight, 0);
				paramsPlayAlarmSignalTwiceInfoTextView.addRule(RelativeLayout.BELOW, playAlarmSignalTwiceCheckBox.getId());
				paramsPlayAlarmSignalTwiceInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, playAlarmSignalTwiceCheckBox.getId());

				RelativeLayout.LayoutParams paramsPlayAlarmSignalRepeatedlyInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				paramsPlayAlarmSignalRepeatedlyInfoTextView.setMargins(pixelsLeft, pixelsTop, pixelsRight, 0);
				paramsPlayAlarmSignalRepeatedlyInfoTextView.addRule(RelativeLayout.BELOW, playAlarmSignalTwiceCheckBox.getId());
				paramsPlayAlarmSignalRepeatedlyInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, playAlarmSignalTwiceCheckBox.getId());

				soundSettingInfoTextView.setLayoutParams(paramsSoundSettingInfoTextView);
				playAlarmSignalTwiceInfoTextView.setLayoutParams(paramsPlayAlarmSignalTwiceInfoTextView);
				playAlarmSignalRepeatedlyInfoTextView.setLayoutParams(paramsPlayAlarmSignalRepeatedlyInfoTextView);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void fetchSharedPrefs() {
		useOsSoundSettings = (Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.USE_OS_SOUND_SETTINGS_KEY, DataType.BOOLEAN, context);
		playAlarmSignalTwice = (Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.PLAY_TONE_TWICE_KEY, DataType.BOOLEAN, context);
		playAlarmSignalRepeatedly = (Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.PLAY_ALARM_SIGNAL_REPEATEDLY_KEY, DataType.BOOLEAN, context);
		primaryAlarmSignal = (String) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_ALARM_SIGNAL_KEY, DataType.STRING, context, soundHandler.resolveAlarmSignal(context, SoundHandler.DEFAULT_PRIMARY_ALARM_SIGNAL_ID));
		secondaryAlarmSignal = (String) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_ALARM_SIGNAL_KEY, DataType.STRING, context, soundHandler.resolveAlarmSignal(context, SoundHandler.DEFAULT_SECONDARY_ALARM_SIGNAL_ID));
		userAddedAlarmSignals = (List<String>) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.USER_ADDED_ALARM_SIGNALS_KEY, DataType.LIST, context);

		// Check that all alarm signals exists
		validateUserAddedAlarmSignals();
	}

	@Override
	public void updateFragmentView() {
		updateSelectedPrimaryAlarmSignalTextView();
		updateSelectedSecondaryAlarmSignalTextView();
		updateUseOsSoundSettingsCheckBox();
		updatePlayAlarmSignalTwiceCheckBox();
		updatePlayAlarmSignalRepeatedlyCheckBox();
	}

	@Override
	public void setListeners() {
		// Set listener to Primary Alarm Signal selection LinearLayout
		primaryAlarmSignalSelectionLinearLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showAlarmSignalDialog(AlarmSignalDialog.PRIMARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE);
			}
		});

		// Set listener to Secondary Alarm Signal selection LinearLayout
		secondaryAlarmSignalSelectionLinearLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showAlarmSignalDialog(AlarmSignalDialog.SECONDARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE);
			}
		});

		// Set listener to Primary Alarm Vibration selection LinearLayout
		primaryAlarmVibrationSelectionLinearLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// @formatter:off
				new AlertDialog.Builder(context)
				.setIcon(android.R.drawable.ic_dialog_info) 
				.setTitle("Shit sherlock...")
				.setMessage("This feature of awesomeness does not hold any implementation yet, stay tuned..")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Do nothing...
					}
				})
				.create()
				.show();;
				// @formatter:on
			}
		});

		// Set listener to Secondary Alarm Vibration selection LinearLayout
		secondaryAlarmVibrationSelectionLinearLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// @formatter:off
				new AlertDialog.Builder(context)
				.setIcon(android.R.drawable.ic_dialog_info) 
				.setTitle("Shit sherlock...")
				.setMessage("This feature of awesomeness does not hold any implementation yet, stay tuned..")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Do nothing...
					}
				})
				.create()
				.show();;
				// @formatter:on
			}
		});

		// Set listener to Sound Settings CheckBox
		soundSettingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// Set CheckBox depending on it's checked status and store variable
				if (soundSettingCheckBox.isChecked()) {
					useOsSoundSettings = true;
				} else {
					useOsSoundSettings = false;
				}

				// Store value to shared preferences
				prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.USE_OS_SOUND_SETTINGS_KEY, useOsSoundSettings, context);
			}
		});

		// Set listener to Play Alarm Signal Twice CheckBox
		playAlarmSignalTwiceCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (playAlarmSignalTwiceCheckBox.isChecked()) {
					playAlarmSignalTwice = true;
				} else {
					playAlarmSignalTwice = false;
				}

				prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PLAY_TONE_TWICE_KEY, playAlarmSignalTwice, context);

				// Update some UI components
				togglePlayAlarmSignalRepeatedlyComponents();
			}
		});

		// Set listener to the Play Alarm Signal Repeatedly CheckBox
		playAlarmSignalRepeatedlyCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (playAlarmSignalRepeatedlyCheckBox.isChecked()) {
					playAlarmSignalRepeatedly = true;
				} else {
					playAlarmSignalRepeatedly = false;
				}

				prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PLAY_ALARM_SIGNAL_REPEATEDLY_KEY, playAlarmSignalRepeatedly, context);
				togglePlayAlarmSignalTwiceComponents();
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Only interested in OK results, don't care at all about the others. Except the add alarm signal request codes, everyone of them must be
		// taken care of, regardless if the result is OK or NOT OK
		if (resultCode == Activity.RESULT_OK || requestCode == AlarmSignalDialog.ADD_ALARM_SIGNAL_FROM_PRIMARY_DIALOG_REQUEST_CODE || requestCode == AlarmSignalDialog.ADD_ALARM_SIGNAL_FROM_SECONDARY_DIALOG_REQUEST_CODE) {
			// Only interested in certain request codes...
			switch (requestCode) {
				case (AlarmSignalDialog.PRIMARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE):
					// Get the chosen primary alarm signal
					primaryAlarmSignal = data.getStringExtra(AlarmSignalDialog.ALARM_SIGNAL);

					// Store to shared preferences
					prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_ALARM_SIGNAL_KEY, primaryAlarmSignal, context);

					// Update selected primary alarm signal TextView
					updateSelectedPrimaryAlarmSignalTextView();
					break;
				case (AlarmSignalDialog.SECONDARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE):
					secondaryAlarmSignal = data.getStringExtra(AlarmSignalDialog.ALARM_SIGNAL);
					prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_ALARM_SIGNAL_KEY, secondaryAlarmSignal, context);

					updateSelectedSecondaryAlarmSignalTextView();
					break;
				case (AlarmSignalDialog.ADD_ALARM_SIGNAL_FROM_PRIMARY_DIALOG_REQUEST_CODE):
					// Fall through to next case as the same actions are taken for both
				case (AlarmSignalDialog.ADD_ALARM_SIGNAL_FROM_SECONDARY_DIALOG_REQUEST_CODE):
					// Resolve request code from intents request code, in case the user added an own alarm signal
					int resolvedRequestCode = AlarmSignalDialog.fromIntentToDialogRequestCode(requestCode);

					if (resolvedRequestCode == AlarmSignalDialog.PRIMARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE || resolvedRequestCode == AlarmSignalDialog.SECONDARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE) {
						// Handle the OK result by checking if selected alarm signal already exists, if not add it to the list and store it to shared
						// preferences
						if (resultCode == Activity.RESULT_OK) {
							// Get the path to the selected alarm signal
							String alarmSignal = data.getData().getPath();

							// If it doesn't exist in the list of user added alarm signals
							if (!Util.existsInConsiderCases(alarmSignal, userAddedAlarmSignals)) {
								// Add the new alarm signal to the list
								userAddedAlarmSignals.add(alarmSignal);

								// Store it to shared preferences
								prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.USER_ADDED_ALARM_SIGNALS_KEY, userAddedAlarmSignals, context);

								// Check that all alarm signals exists
								validateUserAddedAlarmSignals();
							} else {
								Toast.makeText(context, R.string.PATH_ALREADY_IN_ALARM_SIGNAL_PATH_LIST, Toast.LENGTH_LONG).show();
							}
						}

						// Show dialog again
						showAlarmSignalDialog(resolvedRequestCode);
					}
					break;
				default:
					if (SmsAlarm.DEBUG) {
						Log.e(LOG_TAG + ":onActivityResult()", "An unsupported result occurred, result code: \"" + resultCode + "\" and request code: \"" + requestCode + "\"");
					}
			}
		}
	}

	/**
	 * To validate that <b><i>User Added Alarm Signals</i></b> exists on the device. If they don't they will be removed, the changes will be persisted
	 * to {@link SharedPreferences} and a message will be created and displayed for the user as a {@link Toast} explaining which alarm signals that
	 * was removed and why.<br>
	 * <b><i>Note.</i></b> If the selected alarm signal for either primary or secondary alarm has been removed, then the default alarm signal for
	 * corresponding alarm type will be set as the selected alarm signal, and also stored to <code>SharedPreferences</code>.
	 */
	private void validateUserAddedAlarmSignals() {
		// Need a temporary list of alarm signal, the once that doesn't exist on the file system will be placed here for removal
		List<String> missingAlarmSignals = new ArrayList<String>();

		// Check that all user added alarm signals exist in the file system if not add them to another list for later removal
		for (String alarmSignalPath : userAddedAlarmSignals) {
			if (!Util.fileExists(alarmSignalPath)) {
				missingAlarmSignals.add(alarmSignalPath);
			}
		}

		// If there was any missing alarm signals
		if (!missingAlarmSignals.isEmpty()) {
			// Build up a message telling the user that some signals has been removed and why
			// The built up string will look like: Signal_1, Signal_2, Signal_3 has been removed...
			StringBuilder missingAlarmSignalsMessage = new StringBuilder();

			for (String missingAlarmSignalPath : missingAlarmSignals) {
				if (missingAlarmSignalsMessage.length() > 0) {
					missingAlarmSignalsMessage.append(", ");
				}

				missingAlarmSignalsMessage.append(Util.getBaseFileName(missingAlarmSignalPath));
			}

			missingAlarmSignalsMessage.append(" ");

			// Remove the missing alarm signal paths
			userAddedAlarmSignals.removeAll(missingAlarmSignals);

			// Store it to shared preferences
			prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.USER_ADDED_ALARM_SIGNALS_KEY, userAddedAlarmSignals, context);

			// If the selected alarm signal for primary or secondary alarm was removed, set it to the default
			if (Util.existsInConsiderCases(primaryAlarmSignal, missingAlarmSignals)) {
				primaryAlarmSignal = soundHandler.resolveAlarmSignal(context, SoundHandler.DEFAULT_PRIMARY_ALARM_SIGNAL_ID);
				prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_ALARM_SIGNAL_KEY, primaryAlarmSignal, context);
			}

			if (Util.existsInConsiderCases(secondaryAlarmSignal, missingAlarmSignals)) {
				secondaryAlarmSignal = soundHandler.resolveAlarmSignal(context, SoundHandler.DEFAULT_SECONDARY_ALARM_SIGNAL_ID);
				prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_ALARM_SIGNAL_KEY, secondaryAlarmSignal, context);
			}

			// Show toast for the user
			Toast.makeText(context, missingAlarmSignalsMessage.toString() + getText(R.string.ALARM_SIGNALS_REMOVED_DUE_TO_MISSING_PATHS), Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Convenience method to create a new instance of {@link AlarmSignalDialog} and show it.
	 * 
	 * @param requestCode
	 *            Request code for the created <code>AlarmSignalDialog</code>.
	 */
	private void showAlarmSignalDialog(int requestCode) {
		AlarmSignalDialog dialog = new AlarmSignalDialog();
		dialog.setTargetFragment(SoundSettingsFragment.this, requestCode);
		dialog.show(getFragmentManager(), AlarmSignalDialog.ALARM_SIGNAL_DIALOG_TAG);
	}

	/**
	 * To update selected <b><i>Primary Alarm</i></b> {@link TextView} with correct value.
	 */
	private void updateSelectedPrimaryAlarmSignalTextView() {
		selectedPrimaryAlarmSignalTextView.setText(Util.getBaseFileName(primaryAlarmSignal));
	}

	/**
	 * To update selected <b><i>Secondary Alarm</i></b> {@link TextView} with correct value.
	 */
	private void updateSelectedSecondaryAlarmSignalTextView() {
		selectedSecondaryAlarmSignalTextView.setText(Util.getBaseFileName(secondaryAlarmSignal));
	}

	/**
	 * To update use OS sound settings {@link CheckBox} with correct value.
	 */
	private void updateUseOsSoundSettingsCheckBox() {
		if (useOsSoundSettings) {
			soundSettingCheckBox.setChecked(true);
		}
	}

	/**
	 * To update play alarm signal twice {@link CheckBox} with correct value.
	 */
	private void updatePlayAlarmSignalTwiceCheckBox() {
		if (playAlarmSignalTwice) {
			playAlarmSignalTwiceCheckBox.setChecked(true);
		}
	}

	/**
	 * To update play alarm signal repeatedly {@link CheckBox} with correct value.
	 */
	private void updatePlayAlarmSignalRepeatedlyCheckBox() {
		if (playAlarmSignalRepeatedly) {
			playAlarmSignalRepeatedlyCheckBox.setChecked(true);
		}
	}

	/**
	 * To toggle the play alarm signal twice user interface components between being <b><i>enabled or disabled</i></b>. This depends on if the setting
	 * to play alarm signal repeatedly has been set <b><i>true</i></b> or <b><i>false</i></b>.<br>
	 * If it has been set to <code>true</code> then the UI components will be <b><i>disabled</i></b> else <b><i>enabled</i></b>.
	 */
	private void togglePlayAlarmSignalTwiceComponents() {
		playAlarmSignalTwiceCheckBox.setEnabled(playAlarmSignalRepeatedly ? false : true);
		playAlarmSignalTwiceInfoTextView.setEnabled(playAlarmSignalRepeatedly ? false : true);
	}

	/**
	 * To toggle the play alarm signal repeatedly user interface components between being <b><i>enabled or disabled</i></b>. This depends on if the
	 * setting to play alarm signal twice has been set <b><i>true</i></b> or <b><i>false</i></b>. <br>
	 * If it has been set to <code>true</code> then the UI components will be <b><i>disabled</i></b> else <b><i>enabled</i></b>.
	 */
	private void togglePlayAlarmSignalRepeatedlyComponents() {
		playAlarmSignalRepeatedlyCheckBox.setEnabled(playAlarmSignalTwice ? false : true);
		playAlarmSignalRepeatedlyInfoTextView.setEnabled(playAlarmSignalTwice ? false : true);
	}
}
