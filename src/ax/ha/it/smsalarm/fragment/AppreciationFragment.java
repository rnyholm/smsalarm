/**
 * Copyright (c) 2015 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.fragment;

import com.actionbarsherlock.app.SherlockFragment;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.application.SmsAlarmApplication.GoogleAnalyticsHandler;

/**
 * {@link Fragment} containing all the views and user interface widgets for the <b><i>Appreciation Possibility</i></b>. <code>Fragment</code> does also contain all logic for the widgets.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.4.1
 * @since 2.3.1
 */
public class AppreciationFragment extends SherlockFragment implements ApplicationFragment {
	private static final String LOG_TAG = AppreciationFragment.class.getSimpleName();

	private static final String GOOGLE_PLAY_URI = "market://details?id=";

	// Must have the application context
	private Context context;

	// The TextView
	private TextView reviewNowTextView;

	/**
	 * Creates a new instance of {@link AppreciationFragment}.
	 */
	public AppreciationFragment() {
		// Just empty...
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set context here, it's safe because this fragment has been attached to it's container, hence we have access to context
		context = getActivity();
	}

	@Override
	public void onResume() {
		super.onResume();

		// Set correct screen name and send hit to Google Analytics
		GoogleAnalyticsHandler.setScreenNameAndSendScreenViewHit(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.appreciation, container, false);

		// @formatter:off
		// Ensure this fragment has data, UI-widgets and logic set before the view is returned
		fetchSharedPrefs(); // Fetch shared preferences needed by objects in this fragment
		findViews(view); // Find UI widgets and link link them to objects in this fragment
		setListeners(); // Set necessary listeners
		updateFragmentView(); // Update all UI widgets with fetched data from shared preferences
		// @formatter:on

		return view;
	}

	@Override
	public void findViews(View view) {
		// Find the different views
		reviewNowTextView = (TextView) view.findViewById(R.id.appreciationReview_tv);

		// Want to make the review now text to look like a link, for real it will start an activity while pressed
		SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
		spannableStringBuilder.append(reviewNowTextView.getText());
		spannableStringBuilder.setSpan(new URLSpan(""), 0, spannableStringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		reviewNowTextView.setText(spannableStringBuilder, TextView.BufferType.SPANNABLE);

		// Set this Clickable in order to get the "click-feedback"
		reviewNowTextView.setClickable(true);
	}

	@Override
	public void fetchSharedPrefs() {
		// Nothing to fetch for this fragment...
	}

	@Override
	public void updateFragmentView() {
		// Nothing to update for this fragment...
	}

	@Override
	public void setListeners() {
		// Listener for the review application now, starting Google Play
		reviewNowTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startGooglePlay();
			}
		});
	}

	/**
	 * Convenience method to launch the Google Play application and redirect it to the Sms Alarm application over there.
	 */
	private void startGooglePlay() {
		// Get correct URI to google play from package name
		Uri uri = Uri.parse(GOOGLE_PLAY_URI + context.getPackageName());
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);

		try {
			// Start Google Play
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			// For some reason launch of Google Play failed, log it and show toast
			Log.e(LOG_TAG + ":startGooglePlay()", "Seems like the Google Play application is missing on this device", e);
			Toast.makeText(context, getString(R.string.TOAST_UNABLE_TO_START_GOOGLE_PLAY), Toast.LENGTH_LONG).show();
		}
	}
}