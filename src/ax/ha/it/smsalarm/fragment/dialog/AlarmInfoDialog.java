/**
 * Copyright (c) 2015 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.fragment.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.alarm.Alarm;
import ax.ha.it.smsalarm.alarm.Alarm.AlarmType;

/**
 * {@link DialogFragment} which shows all details of {@link Alarm} set to this dialog..
 *
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 * @see #ALARM_INFO
 * @see #ALARM_INFO_DIALOG_TAG
 * @see #SECONDARY_ALARM_VIBRATION_DIALOG_REQUEST_CODE
 */
public class AlarmInfoDialog extends DialogFragment {
	// Used as a key when putting data into bundles and intents, dialog tag can come in handy for classes using this dialog
	public static final String ALARM_INFO = "alarmInfo";
	public static final String ALARM_INFO_DIALOG_TAG = "alarmInfoDialog";

	// Request code used for this dialog
	public static final int ALARM_INFO_DIALOG_REQUEST_CODE = 19;

	// Need to hold an alarm
	private Alarm alarm;

	// Must have application context
	private Context context;

	/**
	 * Creates a new instance of {@link AlarmInfoDialog}.
	 */
	public AlarmInfoDialog() {
		// Just empty...
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set context here, it's safe because this dialog fragment has been attached to it's container, hence we have access to context
		context = getActivity();

		// Get the alarm from bundle
		Bundle arguments = getArguments();
		alarm = (Alarm) arguments.getParcelable(ALARM_INFO);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Setup the TextViews, beginning with the titles...
		TextView alarmTypeTitleTextView = new TextView(context);
		TextView senderTitleTextView = new TextView(context);
		TextView receivedTitleTextView = new TextView(context);
		TextView acknowledgedTitleTextView = new TextView(context);
		TextView triggerTextTitleTextView = new TextView(context);
		TextView alarmTitleTextView = new TextView(context);

		// ...then the actual information...
		TextView alarmTypeTextView = new TextView(context);
		TextView senderTextView = new TextView(context);
		TextView receivedTextView = new TextView(context);
		TextView acknowledgedTextView = new TextView(context);
		TextView triggerTextTextView = new TextView(context);
		TextView alarmTextView = new TextView(context);

		// ...and setting text to the titles using StringBuilder
		StringBuilder sb = new StringBuilder();
		sb.append(getString(R.string.ALARM_INFO_ALARM_TYPE_TITLE));
		sb.append(getString(R.string.COLON));
		alarmTypeTitleTextView.setText(sb.toString());

		sb = new StringBuilder();
		sb.append(getString(R.string.ALARM_INFO_SENDER_TITLE));
		sb.append(getString(R.string.COLON));
		senderTitleTextView.setText(sb.toString());

		sb = new StringBuilder();
		sb.append(getString(R.string.ALARM_INFO_RECEIVED_TITLE));
		sb.append(getString(R.string.COLON));
		receivedTitleTextView.setText(sb.toString());

		sb = new StringBuilder();
		sb.append(getString(R.string.ALARM_INFO_ACKNOWLEDGED_TITLE));
		sb.append(getString(R.string.COLON));
		acknowledgedTitleTextView.setText(sb.toString());

		sb = new StringBuilder();
		sb.append(getString(R.string.ALARM_INFO_TRIGGER_TEXT_TITLE));
		sb.append(getString(R.string.COLON));
		triggerTextTitleTextView.setText(sb.toString());

		sb = new StringBuilder();
		sb.append(getString(R.string.ALARM_INFO_ALARM_TITLE));
		sb.append(getString(R.string.COLON));
		alarmTitleTextView.setText(sb.toString());

		// ...then setting the actual information from the alarm to the TextViews
		alarmTypeTextView.setText(resolveAlarmTypeLocalized(alarm.getAlarmType()));
		senderTextView.setText(alarm.getSender());
		receivedTextView.setText(alarm.getReceivedLocalized());
		acknowledgedTextView.setText(alarm.getAcknowledgedLocalized());
		triggerTextTextView.setText(alarm.getTriggerText());
		alarmTextView.setText(alarm.getMessage());

		// Initialize the different LayoutParameters needed to build a correct dialog
		LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

		// Note. Layout parameters for title TextViews will have a weight of 0 and textViews containing the actual info will have a weight of 1
		LinearLayout.LayoutParams titleTextViewLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0);
		LinearLayout.LayoutParams infoTextViewLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

		// Set margin to the layout parameters for the textViews that holds the information and set LayoutParameters...
		infoTextViewLayoutParams.setMargins(5, 0, 0, 0);

		// ...beginning by setting LayoutParameters to title TextViews...
		alarmTypeTitleTextView.setLayoutParams(titleTextViewLayoutParams);
		senderTitleTextView.setLayoutParams(titleTextViewLayoutParams);
		receivedTitleTextView.setLayoutParams(titleTextViewLayoutParams);
		acknowledgedTitleTextView.setLayoutParams(titleTextViewLayoutParams);
		triggerTextTitleTextView.setLayoutParams(titleTextViewLayoutParams);
		alarmTitleTextView.setLayoutParams(titleTextViewLayoutParams);

		// ...then the other TextViews
		alarmTypeTextView.setLayoutParams(infoTextViewLayoutParams);
		senderTextView.setLayoutParams(infoTextViewLayoutParams);
		receivedTextView.setLayoutParams(infoTextViewLayoutParams);
		acknowledgedTextView.setLayoutParams(infoTextViewLayoutParams);
		triggerTextTextView.setLayoutParams(infoTextViewLayoutParams);
		alarmTextView.setLayoutParams(infoTextViewLayoutParams);

		// Now start build up the actual user interface by first setting up a ScrollView and a LinearLayout, also configure them
		ScrollView scrollView = new ScrollView(context);
		LinearLayout linearLayout = new LinearLayout(context);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		linearLayout.setPadding(5, 5, 5, 5);
		linearLayout.setLayoutParams(linearLayoutParams);

		// Then make one other LinearLayout at the time, set proper LayoutParameters to it and correct TextViews
		LinearLayout ll0 = new LinearLayout(context);
		ll0.setOrientation(LinearLayout.HORIZONTAL);
		ll0.setLayoutParams(linearLayoutParams);
		ll0.addView(alarmTypeTitleTextView);
		ll0.addView(alarmTypeTextView);
		linearLayout.addView(ll0);

		LinearLayout ll1 = new LinearLayout(context);
		ll1.setOrientation(LinearLayout.HORIZONTAL);
		ll1.setLayoutParams(linearLayoutParams);
		ll1.addView(senderTitleTextView);
		ll1.addView(senderTextView);
		linearLayout.addView(ll1);

		LinearLayout ll2 = new LinearLayout(context);
		ll2.setOrientation(LinearLayout.HORIZONTAL);
		ll2.setLayoutParams(linearLayoutParams);
		ll2.addView(receivedTitleTextView);
		ll2.addView(receivedTextView);
		linearLayout.addView(ll2);

		LinearLayout ll3 = new LinearLayout(context);
		ll3.setOrientation(LinearLayout.HORIZONTAL);
		ll3.setLayoutParams(linearLayoutParams);
		ll3.addView(acknowledgedTitleTextView);
		ll3.addView(acknowledgedTextView);
		linearLayout.addView(ll3);

		LinearLayout ll4 = new LinearLayout(context);
		ll4.setOrientation(LinearLayout.HORIZONTAL);
		ll4.setLayoutParams(linearLayoutParams);
		ll4.addView(triggerTextTitleTextView);
		ll4.addView(triggerTextTextView);
		linearLayout.addView(ll4);

		LinearLayout ll5 = new LinearLayout(context);
		ll5.setOrientation(LinearLayout.HORIZONTAL);
		ll5.setLayoutParams(linearLayoutParams);
		ll5.addView(alarmTitleTextView);
		ll5.addView(alarmTextView);
		linearLayout.addView(ll5);

		// At last add the "main" LinearLayout to the ScrollView
		scrollView.addView(linearLayout);

		// Setup the dialog with correct resources, listeners and values
		// @formatter:off
		return new AlertDialog.Builder(context)
				.setIcon(android.R.drawable.ic_dialog_info) 	// Set icon
				.setTitle(R.string.ALARM_INFO_PROMPT_TITLE) 	// Set title
				.setView(scrollView) 							// Bind dialog to Layout
				// @formatter:on

				.setNeutralButton(R.string.OK, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// DO NOTHING!
					}
				})

				.create();
	}

	/**
	 * Convenience method to resolve a <b><i>Localized</i></b> text representation of given {@link AlarmType}.
	 * 
	 * @param alarmType
	 *            <code>AlarmType</code> to resolve localized text from.
	 * @return Given <code>AlarmType</code> localized into a {@link String}.
	 */
	public String resolveAlarmTypeLocalized(AlarmType alarmType) {
		switch (alarmType) {
			case PRIMARY:
				return getString(R.string.TITLE_PRIMARY_ALARM);
			case SECONDARY:
				return getString(R.string.TITLE_SECONDARY_ALARM);
			default:
				return getString(R.string.TITLE_UNDEFINED_ALARM);
		}
	}
}
