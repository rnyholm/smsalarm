/**
 * Copyright (c) 2015 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.activity.SmsAlarm;
import ax.ha.it.smsalarm.vending.billing.util.IabHelper;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * {@link Fragment} containing all the views and user interface widgets for the <b><i>Appreciation Possibilities</i></b>. <code>Fragment</code> does
 * also contain all logic for the widgets.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class AppreciationFragment extends SherlockFragment implements ApplicationFragment {
	private static final String LOG_TAG = AppreciationFragment.class.getSimpleName();

	private static final String DONATE_XSMALL = "donate_xsmall";
	private static final String DONATE_SMALL = "donate_small";
	private static final String DONATE_MEDIUM = "donate_medium";
	private static final String DONATE_LARGE = "donate_large";
	private static final String DONATE_XLARGE = "donate_xlarge";

	// Must have the application context
	private Context context;

	// Need to have a IabHelper in order to handle the billing
	IabHelper iabHelper;

	/**
	 * Creates a new instance of {@link AppreciationFragment}.
	 */
	public AppreciationFragment() {
		// Just empty...
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set context here, it's safe because this fragment has been attached to its container, hence we have access to context
		context = getActivity();

		// Get the application key and initialize the Iab Helper
		String base64EncodedPublicKey = getString(R.string.APPLICATION_LICENSE_KEY);
		iabHelper = new IabHelper(context, base64EncodedPublicKey);

		// Enable debug logging if application is built in a debug environment
		iabHelper.enableDebugLogging(SmsAlarm.DEBUG, LOG_TAG);
	}

	@Override
	public void findViews(View view) {
		// TODO Auto-generated method stub
	}

	@Override
	public void fetchSharedPrefs() {
		// TODO Auto-generated method stub
	}

	@Override
	public void updateFragmentView() {
		// TODO Auto-generated method stub
	}

	@Override
	public void setListeners() {
		// TODO Auto-generated method stub
	}
}
