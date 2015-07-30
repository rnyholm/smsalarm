/**
 * Copyright (c) 2015 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.fragment;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.activity.SmsAlarm;
import ax.ha.it.smsalarm.vending.billing.util.IabHelper;
import ax.ha.it.smsalarm.vending.billing.util.IabResult;

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

	private static final String SKU_XSMALL = "donate_xsmall";
	private static final String SKU_SMALL = "donate_small";
	private static final String SKU_MEDIUM = "donate_medium";
	private static final String SKU_LARGE = "donate_large";
	private static final String SKU_XLARGE = "donate_xlarge";

	// To map the product name(id) with an amount
	private HashMap<String, String> productsAndAmounts = new HashMap<String, String>();

	// Must have the application context
	private Context context;

	// Need to have a IabHelper in order to handle the billing
	IabHelper iabHelper;

	// The TextView...
	private TextView reviewNowTextView;

	// ...Spinner...
	private Spinner donationSizeSpinner;

	// ...and Button
	private Button donateButton;

	// Indicator variable set after the setup has been done of the IabHelper, this telling whether or not setup was successful or not, if successful
	// enable donate button else not
	private boolean enableDonations = true;

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
		String base64EncodedPublicKey = PublicRSAKeyUtil.computeKey();
		iabHelper = new IabHelper(context, base64EncodedPublicKey);

		// Enable debug logging if application is built in a debug environment
		iabHelper.enableDebugLogging(SmsAlarm.DEBUG, LOG_TAG);

		// This listener is set here because the setup runs asynchronous and the specified listener will be called once setup completes
		iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			@Override
			public void onIabSetupFinished(IabResult result) {
				if (!result.isSuccess()) {
					// Some problem occurred while setting up the IabHelper, log it, show toast and indicate that the donate button should be disabled
					enableDonations = false;

					Log.e(LOG_TAG + ":onCreate()", "An error occurred while setting up the IabHelper, setup ended in result: \"" + result + "\"");
					Toast.makeText(context, getString(R.string.TOAST_UNABLE_TO_SETUP_GOOGLE_IN_APP_BILLING), Toast.LENGTH_LONG).show();

					// Update the UI widgets in case this operation is done after the user interface has ben painted
					updateFragmentView();
				}
			}
		});

		// Build up the products and amounts map
		productsAndAmounts.put(SKU_XSMALL, "1€");
		productsAndAmounts.put(SKU_SMALL, "3€");
		productsAndAmounts.put(SKU_MEDIUM, "5€");
		productsAndAmounts.put(SKU_LARGE, "10€");
		productsAndAmounts.put(SKU_XLARGE, "15€");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.appreciation, container, false);

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
		// Find the different views
		reviewNowTextView = (TextView) view.findViewById(R.id.appreciationReview_tv);
		donationSizeSpinner = (Spinner) view.findViewById(R.id.appreciationDonationSizeSpinner_sp);
		donateButton = (Button) view.findViewById(R.id.donateButton_btn);

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
		// Add null safety to this as it may be triggered before the views have been found
		if (donationSizeSpinner != null && donateButton != null) {
			updateDonationSizeSpinner();

			// Enable/Disable Spinner and Button for donation depending on whether or not the IabHelper was successfully initialized
			donationSizeSpinner.setEnabled(enableDonations);
			donateButton.setEnabled(enableDonations);
		}
	}

	@Override
	public void setListeners() {
		reviewNowTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startGooglePlay();
			}
		});
		// TODO Auto-generated method stub
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Only precede if the IabHelper is fully initialized
		if (iabHelper != null) {
			if (!iabHelper.handleActivityResult(requestCode, resultCode, data)) {
				// Any handling of activity results not related to In-app Billing goes here...
				super.onActivityResult(requestCode, resultCode, data);
			} else {
				// TODO Auto-generated method stub
			}
		}
	}

	/**
	 * Convenience method to launch the Google Play application and redirect it to the Sms Alarm application over there.
	 */
	private void startGooglePlay() {
		// Get correct URI to google play from package name
		Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
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

	/**
	 * To update the {@link Spinner} containing all possible amounts to donate, with correct values.
	 */
	private void updateDonationSizeSpinner() {
		// Fetch all the amounts into one list
		List<String> amounts = new ArrayList<String>();
		amounts.addAll(productsAndAmounts.values());

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, amounts);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		donationSizeSpinner.setAdapter(adapter);
	}

	/**
	 * Utility class to compute the Public Base64-encoded RSA License Key.
	 * 
	 * @author Robert Nyholm <robert.nyholm@aland.net>
	 * @version 2.3.1
	 * @since 2.3.1
	 */
	private final static class PublicRSAKeyUtil {
		/**
		 * To compute the Public Base64-encoded RSA License Key and return it.
		 * 
		 * @return Computed Public Base64-encoded RSA License Key.
		 */
		public static String computeKey() {
			return getBeginningPart() + getMiddlePart() + getEndingPart();
		}

		/**
		 * To get the beginning part of the Public Base64-encoded RSA License Key.
		 * 
		 * @return Beginning of the Key.
		 */
		private static String getBeginningPart() {
			// TODO: REMOVE THIS LINE ONCE ENSURED IT WORKS! -- MIIBIjANBgkqhkiG9w0BAQEFA

			char[] chars = "miibiJanbGKQHKIg9W0baqefa".toCharArray();

			for (int i = 0; i < chars.length; i++) {
				char c = chars[i];

				// Only swap cases of non-digit characters
				if (!Character.isDigit(c)) {
					if (Character.isUpperCase(c)) {
						chars[i] = Character.toLowerCase(c);
					} else if (Character.isLowerCase(c)) {
						chars[i] = Character.toUpperCase(c);
					}
				}
			}

			// Return the swapped string
			return new String(chars);
		}

		/**
		 * To get the Ending part of the Public Base64-encoded RSA License Key.
		 * 
		 * @return Ending of the Key.
		 */
		private static String getEndingPart() {
			// TODO: REMOVE THIS LINE ONCE ENSURED IT WORKS! -- l3QhAFhExQ8WksaNI4QIDAQAB
			return new StringBuilder("BAQADIQ4INaskW8QxEhFAhQ3l").reverse().toString();
		}

		/**
		 * To get the Middle part of the Public Base64-encoded RSA License Key.
		 * 
		 * @return Middle of the Key.
		 */
		private static String getMiddlePart() {
			return "AOCAQ8AMIIBCgKCAQEApzFAV0cbzGHJ10GxIaX7XKwyNFX4fEFkPqyXyED0sSbTx+CpQ0pGRcyQPAiURnKoW5/6ztu175s037CZRgjNiKAjiqsOKS2J8o8V728jR7xnUHOeEd7d2M65iaP+OX26Gaz+GE3CuCMbWGnM24RojJUJv0ZHOx/N2F70Q3WZ97r3bugiul1yCER/k5//IJK9gAdlyGl8k2HmT6XkdxP68boJv5/tDCczqeO788HOBD2pOqXC8oYQI7fxI8KMzfPvrKVp3N3F+GlwND1VOiL5fLxcFJuoLH3fPwAC4sb7VZzDdPgrAiFESWXeGMKM88tQh7y";
		}
	}

	/**
	 * Utility class to create random and unique tokens using the {@link SecureRandom}, a bit more expensive but a lot safer.
	 * 
	 * @author Robert Nyholm <robert.nyholm@aland.net>
	 * @version 2.3.1
	 * @since 2.3.1
	 */
	private static final class TokenGenerator {
		private SecureRandom random = new SecureRandom();

		/**
		 * To get the next token.
		 * 
		 * @return Next token.
		 */
		public String nextToken() {
			return new BigInteger(130, random).toString(32);
		}
	}
}
