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
import ax.ha.it.smsalarm.enumeration.AlarmType;
import ax.ha.it.smsalarm.fragment.dialog.AlarmSignalDialog;
import ax.ha.it.smsalarm.handler.LogHandler;
import ax.ha.it.smsalarm.handler.LogHandler.LogPriorities;
import ax.ha.it.smsalarm.handler.NoiseHandler;
import ax.ha.it.smsalarm.handler.PreferencesHandler;
import ax.ha.it.smsalarm.handler.PreferencesHandler.DataType;
import ax.ha.it.smsalarm.handler.PreferencesHandler.PrefKey;

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

	// Objects needed for logging, shared preferences handling and noise handling
	private final LogHandler logger = LogHandler.getInstance();
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
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":SoundSettingsFragment()", "Creating a new Sound settings fragment");
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
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "Start finding Views by their Id");

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
			// If the locale on device is german(de) set pixelstop to -6dp else -9dp
			if ("de".equals(Locale.getDefault().getLanguage())) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "The device has german(de) locale, set different margin-top on information textviews for the checkboxes than other locales");
				// -6dp calculated to pixels
				pixelsTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -6, resources.getDisplayMetrics());
			} else {
				// -9dp calculated to pixels
				pixelsTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -9, resources.getDisplayMetrics());
			}

			// Set layout parameters for the sound settings info textview
			// Wrap content, both on height and width
			RelativeLayout.LayoutParams paramsSoundSettingInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			// Margins left, top, right, bottom
			paramsSoundSettingInfoTextView.setMargins(pixelsLeft, pixelsTop, pixelsRight, 0);
			// Add rule, below UI widget
			paramsSoundSettingInfoTextView.addRule(RelativeLayout.BELOW, soundSettingCheckBox.getId());
			// Add rule, align left of UI widget
			paramsSoundSettingInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, soundSettingCheckBox.getId());

			// Set layout parameters for the play tone twice textview
			RelativeLayout.LayoutParams paramsPlayToneTwiceInfoTextView = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			paramsPlayToneTwiceInfoTextView.setMargins(pixelsLeft, pixelsTop, pixelsRight, 0);
			paramsPlayToneTwiceInfoTextView.addRule(RelativeLayout.BELOW, playAlarmSignalTwiceSettingCheckBox.getId());
			paramsPlayToneTwiceInfoTextView.addRule(RelativeLayout.ALIGN_LEFT, playAlarmSignalTwiceSettingCheckBox.getId());

			// Apply the previously configured layout parameters to the correct textviews
			soundSettingInfoTextView.setLayoutParams(paramsSoundSettingInfoTextView);
			playAlarmSignalTwiceInfoTextView.setLayoutParams(paramsPlayToneTwiceInfoTextView);

			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "API level > 16, edit margins on information textviews for the checkboxes");
		} else { // The device has API level < 17, we just need to check if the locale is german
			// If the locale on device is german(de) we need to adjust the margin top for the information textviews for the checkboxes to -6dp
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

				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "API level < 17 but the device has german(de) locale, set different margin-top on information TextViews for the Checkboxes to fit the language");
			}
		}

		// Set some attributes to the selectedToneEditText
		selectedAlarmSignalEditText.setEnabled(false);
		selectedAlarmSignalEditText.setClickable(false);
		selectedAlarmSignalEditText.setFocusable(false);
		selectedAlarmSignalEditText.setBackgroundColor(Color.WHITE);
		selectedAlarmSignalEditText.setTextColor(Color.BLACK);

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":findViews()", "All Views found for Fragment:\"" + LOG_TAG + "\"");
	}

	@Override
	public void fetchSharedPrefs() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":fetchSharedPrefs()", "Start fetching shared preferences needed by this fragment");

		primaryAlarmSignalId = (Integer) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_MESSAGE_TONE_KEY, DataType.INTEGER, context);
		secondaryAlarmSignalId = (Integer) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_MESSAGE_TONE_KEY, DataType.INTEGER, context, 1);
		useOsSoundSettings = (Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.USE_OS_SOUND_SETTINGS_KEY, DataType.BOOLEAN, context);
		playAlarmSignalTwice = (Boolean) prefHandler.fetchPrefs(PrefKey.SHARED_PREF, PrefKey.PLAY_TONE_TWICE_KEY, DataType.BOOLEAN, context);

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":fetchSharedPrefs()", "Shared preferences fetched");
	}

	@Override
	public void updateFragmentView() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateFragmentView()", "Whole fragments user interface is about to be updated");

		// Update EditText field, CheckBoxes and Spinner
		updateAlarmTypesSpinner();
		updateSelectedAlarmSignalEditText();
		updateUseOsSoundSettingsCheckBox();
		updatePlayAlarmSignalTwiceCheckBox();

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateFragmentView()", "Fragment updated");
	}

	@Override
	public void setListeners() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners()", "Setting listeners to the different user interface widgets");

		// Set listener to editAlarmSignalButton
		editAlarmSignalButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().editAlarmSignalButton.OnClickListener().onClick()", "Edit alarm signal button pressed");

				// Get alarm type Spinner position as AlarmType and go through that
				switch (AlarmType.of(alarmTypeSpinnerPos)) {
					case PRIMARY:
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().editAlarmSignalButton.OnClickListener().onClick()", "Alarm type spinner position is: \"PRIMARY ALARM SPINNER POSITION\", setting targetframe with request code:\"" + Integer.toString(AlarmSignalDialog.PRIMARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE) + "\"");
						showAlarmSignalDialog(AlarmSignalDialog.PRIMARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE);
						break;
					case SECONDARY:
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().editAlarmSignalButton.OnClickListener().onClick()", "Alarm type spinner position is: \"SECONDARY ALARM SPINNER POSITION\", setting targetframe with request code:\"" + Integer.toString(AlarmSignalDialog.SECONDARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE) + "\"");
						showAlarmSignalDialog(AlarmSignalDialog.SECONDARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE);
						break;
					default:
						// DO NOTHING, except log message
						logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":setListeners().editAlarmSignalButton.OnClickListener().onClick()", "An unsupported alarm type spinner position occurred, spinner position is: \"" + Integer.toString(alarmTypeSpinnerPos) + "\", can't show dialog");
				}
			}
		});

		// Set listener to listenAlarmSignalButton
		listenAlarmSignalButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (AlarmType.of(alarmTypeSpinnerPos)) {
					case PRIMARY:
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().listenAlarmSignalButton.OnClickListener().onClick()", "Listen on alarm signal button pressed. Alarm signal for PRIMARY alarm will be played");

						// Play alarm signal and vibrate
						noiseHandler.makeNoise(context, primaryAlarmSignalId, useOsSoundSettings, false);
						break;
					case SECONDARY:
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().listenAlarmSignalButton.OnClickListener().onClick()", "Listen on alarm signal button pressed. Alarm signal for SECONDARY alarm will be played");
						noiseHandler.makeNoise(context, secondaryAlarmSignalId, useOsSoundSettings, false);
						break;
					default:
						// DO NOTHING, except log message
						logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":setListeners().listenAlarmSignalButton.OnClickListener().onClick()", "Invalid spinner position occurred. Spinner position is: \"" + Integer.toString(alarmTypeSpinnerPos) + "\"");
				}
			}
		});

		// Set listener to soundSettingCheckBox
		soundSettingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().soundSettingCheckBox.onCheckedChange()", "Use OS sound settings checkbox pressed(or checkbox initialized)");

				// Set CheckBox depending on it's checked status and store variable
				if (soundSettingCheckBox.isChecked()) {
					// Store value to variable
					useOsSoundSettings = true;
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().soundSettingCheckBox.onCheckedChange()", "Use OS sound settings checkbox \"Checked\"(" + useOsSoundSettings + ")");
				} else {
					useOsSoundSettings = false;
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().soundSettingCheckBox.onCheckedChange()", "Use OS sound settings checkbox \"Unchecked\"(" + useOsSoundSettings + ")");
				}

				// Store value to shared preferences
				prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.USE_OS_SOUND_SETTINGS_KEY, useOsSoundSettings, context);
			}
		});

		// Set listener to playToneTwiceSettingCheckBox
		playAlarmSignalTwiceSettingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().playToneTwiceSettingCheckBox.onCheckedChange()", "Play tone twice checkbox pressed(or checkbox initialized)");

				if (playAlarmSignalTwiceSettingCheckBox.isChecked()) {
					playAlarmSignalTwice = true;
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().playToneTwiceSettingCheckBox.onCheckedChange()", "Play tone twice checkbox \"Checked\"(" + playAlarmSignalTwice + ")");
				} else {
					playAlarmSignalTwice = false;
					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().playToneTwiceSettingCheckBox.onCheckedChange()", "Play tone twice checkbox \"Unhecked\"(" + playAlarmSignalTwice + ")");
				}

				prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PLAY_TONE_TWICE_KEY, playAlarmSignalTwice, context);
			}
		});

		// Set listener to alarm type Spinner
		alarmTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":setListeners().alarmTypeSpinner.OnItemSelectedListener().onItemSelected()", "Item in alarm type spinner pressed(or spinner initialized)");

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
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onActivityResult()", "Handling of activity result is about to begin, request code: \"" + Integer.toString(requestCode) + "\" and result code: \"" + Integer.toString(resultCode) + "\"");

		// Only interested in OK results, don't care at all about the others
		if (resultCode == Activity.RESULT_OK) {
			// Only interested in certain request codes...
			switch (requestCode) {
				case (AlarmSignalDialog.PRIMARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE):
					primaryAlarmSignalId = data.getIntExtra(AlarmSignalDialog.ALARM_SIGNAL, 0);

					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onActivityResult()", "Result code: \"" + Activity.RESULT_OK + "\"(Activity.RESULT_OK), data: \"" + Integer.toString(primaryAlarmSignalId) + "\" fetched using key: \"" + AlarmSignalDialog.ALARM_SIGNAL + "\" is about to be persisted into shared preferences");

					// Store to shared preferences
					prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.PRIMARY_MESSAGE_TONE_KEY, primaryAlarmSignalId, context);

					// Update selected tone EditText
					updateSelectedAlarmSignalEditText();
					break;
				case (AlarmSignalDialog.SECONDARY_ALARM_SIGNAL_DIALOG_REQUEST_CODE):
					secondaryAlarmSignalId = data.getIntExtra(AlarmSignalDialog.ALARM_SIGNAL, 1);

					logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onActivityResult()", "Result code: \"" + Activity.RESULT_OK + "\"(Activity.RESULT_OK), data: \"" + Integer.toString(secondaryAlarmSignalId) + "\" fetched using key: \"" + AlarmSignalDialog.ALARM_SIGNAL + "\" is about to be persisted into shared preferences");

					prefHandler.storePrefs(PrefKey.SHARED_PREF, PrefKey.SECONDARY_MESSAGE_TONE_KEY, secondaryAlarmSignalId, context);

					updateSelectedAlarmSignalEditText();
					break;
				default:
					logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":onActivityResult()", "An unsupported result occurred, result code: \"" + Integer.toString(resultCode) + "\" and request code: \"" + requestCode + "\"");
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
	 * To update selected alarm signal <code>EditText</code> with correct value.
	 */
	private void updateSelectedAlarmSignalEditText() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateSelectedAlarmSignalEditText()", "Alarm type spinner position is: " + Integer.toString(alarmTypeSpinnerPos));

		// Set message tone to the selectedToneEditText, depending on which value spinner has.
		if (alarmTypeSpinnerPos == 0) {
			selectedAlarmSignalEditText.setText(noiseHandler.resolveAlarmSignal(context, primaryAlarmSignalId));
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateSelectedAlarmSignalEditText()", "Selected tone edittext updated");
		} else if (alarmTypeSpinnerPos == 1) {
			selectedAlarmSignalEditText.setText(noiseHandler.resolveAlarmSignal(context, secondaryAlarmSignalId));
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateSelectedAlarmSignalEditText()", "Selected tone edittext updated");
		} else {
			// DO NOTHING, except logging
			logger.logCatTxt(LogPriorities.ERROR, LOG_TAG + ":updateSelectedAlarmSignalEditText()", "Invalid spinner position occurred. Current tone spinner position is: \"" + Integer.toString(alarmTypeSpinnerPos) + "\"");
		}

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateSelectedAlarmSignalEditText()", "Alarm signal edittext updated");
	}

	/**
	 * To update use OS sound settings <code>CheckBox</code> with correct value.
	 */
	private void updateUseOsSoundSettingsCheckBox() {
		// Update use OS sound settings CheckBox
		if (useOsSoundSettings) {
			soundSettingCheckBox.setChecked(true);
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateUseOsSoundSettingsCheckBox()", "Use OS sound settings checkbox \"Checked\"(" + useOsSoundSettings + ")");
		} else {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateUseOsSoundSettingsCheckBox()", "Use OS sound settings checkbox \"Unchecked\"(" + useOsSoundSettings + ")");
		}

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateUseOsSoundSettingsCheckBox()", "Use OS sound settings checkbox updated");
	}

	/**
	 * To update play alarm signal twice <code>CheckBox</code> with correct value.
	 */
	private void updatePlayAlarmSignalTwiceCheckBox() {
		// Update play alarm signal twice CheckBox
		if (playAlarmSignalTwice) {
			playAlarmSignalTwiceSettingCheckBox.setChecked(true);
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updatePlayAlarmSignalTwiceCheckBox()", "Play tone twice checkbox \"Checked\"(" + playAlarmSignalTwice + ")");
		} else {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updatePlayAlarmSignalTwiceCheckBox()", "Play tone twice checkbox \"Unchecked\"(" + playAlarmSignalTwice + ")");
		}

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updatePlayAlarmSignalTwiceCheckBox()", "Play alarm signal twice checkbox updated");
	}

	/**
	 * To update alarm type <code>Spinner</code> with values alarm type values from resources.
	 */
	private void updateAlarmTypesSpinner() {
		// Fill alarm types spinner with values using adapter
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.alarms, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		alarmTypeSpinner.setAdapter(adapter);

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":updateAlarmTypesSpinner()", "Alarm types spinner updated");
	}
}
