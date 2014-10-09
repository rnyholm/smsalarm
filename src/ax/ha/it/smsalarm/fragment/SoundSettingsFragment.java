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
import ax.ha.it.smsalarm.BuildConfig;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.fragment.dialog.AlarmSignalDialog;
import ax.ha.it.smsalarm.handler.NoiseHandler;
import ax.ha.it.smsalarm.handler.PreferencesHandler;
import ax.ha.it.smsalarm.handler.PreferencesHandler.DataType;
import ax.ha.it.smsalarm.handler.PreferencesHandler.PrefKey;
import ax.ha.it.smsalarm.pojo.Alarm.AlarmType;

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

	// Objects needed for shared preferences handling and noise handling
	private final PreferencesHandler prefHandler = PreferencesHandler.getInstance();
	private final NoiseHandler noiseHandler = NoiseHandler.getInstance();

	// Must have the application context
	private Context context;

	// The EditTexts...
	private EditText selectedAlarmSignalEditText;

	// ...Buttons...
	private Button editAlarmSignalButton;
	private Button listenAlarmSignalButton;

	// ...CheckBoxes...
	private CheckBox soundSettingCheckBox;
	private CheckBox playAlarmSignalTwiceSettingCheckBox;

	// ...Spinners...
	private Spinner alarmTypeSpinner;

	// ...and TextViews
	private TextView soundSettingInfoTextView;
	private TextView playAlarmSignalTwiceInfoTextView;

	// Boolean variables to store whether to use OS sound settings or not and if alarm signal should be played twice
	private boolean useOsSoundSettings = false;
	private boolean playAlarmSignalTwice = false;

	// Integer holding alarm type spinners positions
	private int alarmTypeSpinnerPos = 0;

	// Integer to store which alarm signal id to be used
	private int primaryAlarmSignalId = 0;
	private int secondaryAlarmSignalId = 1;

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
		// Finding EditText views
		selectedAlarmSignalEditText = (EditText) view.findViewById(R.id.alarmSignal_et);

		// Finding Button Views
		editAlarmSignalButton = (Button) view.findViewById(R.id.editAlarmSignal_btn);
		listenAlarmSignalButton = (Button) view.findViewById(R.id.listenAlarmSignal_btn);

		// Finding CheckBox views
		soundSettingCheckBox = (CheckBox) view.findViewById(R.id.useSysSoundSettings_chk);
		playAlarmSignalTwiceSettingCheckBox = (CheckBox) view.findViewById(R.id.playAlarmSignalTwiceSetting_chk);

		// Finding TextView, views
		soundSettingInfoTextView = (TextView) view.findViewById(R.id.useSysSoundSettingsHint_tv);
		playAlarmSignalTwiceInfoTextView = (TextView) view.findViewById(R.id.playAlarmSignalTwiceSettingHint_tv);

		// Finding Spinner views
		alarmTypeSpinner = (Spinner) view.findViewById(R.id.alarmTypeSpinner_sp);

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

			// Set layout parameters for the play tone twice TextView
			RelativeLayout.LayoutParams paramsPlayToneTwiceInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			paramsPlayToneTwiceInfoTextView.setMargins(pixelsLeft, pixelsTop, pixelsRight, 0);
			paramsPlayToneTwiceInfoTextView.addRule(RelativeLayout.BELOW, playAlarmSignalTwiceSettingCheckBox.getId());
			paramsPlayToneTwiceInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, playAlarmSignalTwiceSettingCheckBox.getId());

			// Apply the previously configured layout parameters to the correct TextViews
			soundSettingInfoTextView.setLayoutParams(paramsSoundSettingInfoTextView);
			playAlarmSignalTwiceInfoTextView.setLayoutParams(paramsPlayToneTwiceInfoTextView);
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

				RelativeLayout.LayoutParams paramsPlayToneTwiceInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				paramsPlayToneTwiceInfoTextView.setMargins(pixelsLeft, pixelsTop, pixelsRight, 0);
				paramsPlayToneTwiceInfoTextView.addRule(RelativeLayout.BELOW, playAlarmSignalTwiceSettingCheckBox.getId());
				paramsPlayToneTwiceInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, playAlarmSignalTwiceSettingCheckBox.getId());

				soundSettingInfoTextView.setLayoutParams(paramsSoundSettingInfoTextView);
				playAlarmSignalTwiceInfoTextView.setLayoutParams(paramsPlayToneTwiceInfoTextView);
			}
		}

		// Set some attributes to the selectedToneEditText
		selectedAlarmSignalEditText.setEnabled(false);
		selectedAlarmSignalEditText.setClickable(false);
		selectedAlarmSignalEditText.setFocusable(false);
		selectedAlarmSignalEditText.setBackgroundColor(Color.WHITE);
		selectedAlarmSignalEditText.setTextColor(Color.BLACK);
	}

	@Override
	public void fetchSharedPrefs() {
		primaryAlarmSignalId = (Integer) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_MESSAGE_TONE_KEY, DataType.INTEGER, context);
		secondaryAlarmSignalId = (Integer) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_MESSAGE_TONE_KEY, DataType.INTEGER, context, 1);
		useOsSoundSettings = (Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.USE_OS_SOUND_SETTINGS_KEY, DataType.BOOLEAN, context);
		playAlarmSignalTwice = (Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.PLAY_TONE_TWICE_KEY, DataType.BOOLEAN, context);
	}

	@Override
	public void updateFragmentView() {
		// Update EditText field, CheckBoxes and Spinner
		updateAlarmTypesSpinner();
		updateSelectedAlarmSignalEditText();
		updateUseOsSoundSettingsCheckBox();
		updatePlayAlarmSignalTwiceCheckBox();
	}

	@Override
	public void setListeners() {
		// Set listener to Edit Alarm Signal Button
		editAlarmSignalButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Get alarm type Spinner position as AlarmType and go through that
				switch (AlarmType.of(alarmTypeSpinnerPos)) {
					case PRIMARY:
						showAlarmSignalDialog(AlarmSignalDialog.PRIMARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE);
						break;
					case SECONDARY:
						showAlarmSignalDialog(AlarmSignalDialog.SECONDARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE);
						break;
					default:
						if (BuildConfig.DEBUG) {
							Log.e(LOG_TAG, "An unsupported alarm type spinner position occurred, spinner position is: \"" + alarmTypeSpinnerPos + "\", can't show dialog");
						}
				}
			}
		});

		// Set listener to Listen Alarm Signal Button
		listenAlarmSignalButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (AlarmType.of(alarmTypeSpinnerPos)) {
					case PRIMARY:
						// Play alarm signal and vibrate
						noiseHandler.doNoise(context, primaryAlarmSignalId, useOsSoundSettings, false);
						break;
					case SECONDARY:
						noiseHandler.doNoise(context, secondaryAlarmSignalId, useOsSoundSettings, false);
						break;
					default:
						if (BuildConfig.DEBUG) {
							Log.e(LOG_TAG, "An unsupported alarm type spinner position occurred, spinner position is: \"" + alarmTypeSpinnerPos + "\", can't listen at alarm signal");
						}
				}
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
		playAlarmSignalTwiceSettingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (playAlarmSignalTwiceSettingCheckBox.isChecked()) {
					playAlarmSignalTwice = true;
				} else {
					playAlarmSignalTwice = false;
				}

				prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PLAY_TONE_TWICE_KEY, playAlarmSignalTwice, context);
			}
		});

		// Set listener to Alarm Type Spinner
		alarmTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				// Store alarm type Spinners position
				alarmTypeSpinnerPos = alarmTypeSpinner.getSelectedItemPosition();
				// Update selected alarm signal EditText widget
				updateSelectedAlarmSignalEditText();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// DO NOTHING!
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Only interested in OK results, don't care at all about the others
		if (resultCode == Activity.RESULT_OK) {
			// Only interested in certain request codes...
			switch (requestCode) {
				case (AlarmSignalDialog.PRIMARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE):
					primaryAlarmSignalId = data.getIntExtra(AlarmSignalDialog.ALARM_SIGNAL, 0);

					// Store to shared preferences
					prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_MESSAGE_TONE_KEY, primaryAlarmSignalId, context);

					// Update selected tone EditText
					updateSelectedAlarmSignalEditText();
					break;
				case (AlarmSignalDialog.SECONDARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE):
					secondaryAlarmSignalId = data.getIntExtra(AlarmSignalDialog.ALARM_SIGNAL, 1);
					prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_MESSAGE_TONE_KEY, secondaryAlarmSignalId, context);

					updateSelectedAlarmSignalEditText();
					break;
				default:
					if (BuildConfig.DEBUG) {
						Log.e(LOG_TAG + ":onActivityResult()", "An unsupported result occurred, result code: \"" + resultCode + "\" and request code: \"" + requestCode + "\"");
					}
			}
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
	 * To update selected alarm signal {@link EditText} with correct value.
	 */
	private void updateSelectedAlarmSignalEditText() {
		// Set message tone to the selectedToneEditText, depending on which value spinner has.
		if (alarmTypeSpinnerPos == 0) {
			selectedAlarmSignalEditText.setText(noiseHandler.resolveAlarmSignal(context, primaryAlarmSignalId));
		} else if (alarmTypeSpinnerPos == 1) {
			selectedAlarmSignalEditText.setText(noiseHandler.resolveAlarmSignal(context, secondaryAlarmSignalId));
		} else {
			if (BuildConfig.DEBUG) {
				Log.e(LOG_TAG + ":updateSelectedAlarmSignalEditText()", "Invalid alarm type spinner position occurred. Current alarm type spinner position is: \"" + alarmTypeSpinnerPos + "\"");
			}
		}
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
			playAlarmSignalTwiceSettingCheckBox.setChecked(true);
		}
	}

	/**
	 * To update alarm type {@link Spinner} with values alarm type values from resources.
	 */
	private void updateAlarmTypesSpinner() {
		// Fill alarm types spinner with values using adapter
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.alarms, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		alarmTypeSpinner.setAdapter(adapter);
	}
}
