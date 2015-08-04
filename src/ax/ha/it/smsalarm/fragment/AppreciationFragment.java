/**
 * Copyright (c) 2015 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.fragment;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import ax.ha.it.smsalarm.fragment.dialog.ConfirmDonationDialog;
import ax.ha.it.smsalarm.util.Logger;
import ax.ha.it.smsalarm.vending.billing.util.IabHelper;
import ax.ha.it.smsalarm.vending.billing.util.IabResult;
import ax.ha.it.smsalarm.vending.billing.util.Inventory;
import ax.ha.it.smsalarm.vending.billing.util.Purchase;

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

	// Name of log file
	private static final String LOG_FILE = "iablog.txt";

	private static final String GOOGLE_PLAY_URI = "market://details?id=";

	// Request code for the purchase flow
	public static final int DONATION_REQUEST_CODE = 101;

	// To map amount's and product name(id)'s
	private HashMap<String, String> amountsAndProducts = new HashMap<String, String>();

	// Must have the application context
	private Context context;

	// Need to have a IabHelper in order to handle the billing
	private IabHelper iabHelper;

	// Instance of TokenGenerator, it's quite heavy to initialize so once we get hold of it we want to keep it
	private TokenGenerator tokenGenerator;

	// The TextView...
	private TextView reviewNowTextView;

	// ...Spinner...
	private Spinner donationSizeSpinner;

	// ...and Button
	private Button donateButton;

	// Indicator variable set after the setup has been done of the IabHelper, this telling whether or not setup was successful or not, if successful
	// enable donate button else not
	private boolean enableDonations = true;

	// The generated developer payload must be stored in order for correct verification of the purchase can be made
	private String developerPayload = "";

	private Logger logger;

	/**
	 * Creates a new instance of {@link AppreciationFragment}.
	 */
	public AppreciationFragment() {
		logger = new Logger(LOG_FILE);

		// Build up the products and amounts map
		amountsAndProducts.put("1€", "donate_xsmall");
		amountsAndProducts.put("3€", "donate_small");
		amountsAndProducts.put("5€", "donate_medium");
		amountsAndProducts.put("10€", "donate_large");
		amountsAndProducts.put("15€", "donate_xlarge");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set context here, it's safe because this fragment has been attached to its container, hence we have access to context
		context = getActivity();

		// Initialize a token generator for later use
		tokenGenerator = new TokenGenerator();

		// Compute the application key and initialize the Iab Helper
		String base64EncodedPublicKey = PublicRSAKeyUtil.computeKey();
		iabHelper = new IabHelper(context, base64EncodedPublicKey);

		// Enable debug logging if application is built in a debug environment
		iabHelper.enableDebugLogging(SmsAlarm.DEBUG, LOG_TAG);

		// This listener is set here because the setup runs asynchronous and the specified listener will be called once setup completes
		iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			@Override
			public void onIabSetupFinished(IabResult result) {
				if (result.isSuccess()) {
					// In case there are unconsumed purchases, fetch them and consume them
					iabHelper.queryInventoryAsync(queryInventoryListener);
				} else {
					// Some problem occurred while setting up the IabHelper, log it, show toast and indicate that the donate button should be disabled
					enableDonations = false;

					Log.e(LOG_TAG + ":onCreate()", "An error occurred while setting up the IabHelper, setup ended in result: \"" + result + "\"");
					Toast.makeText(context, getString(R.string.TOAST_UNABLE_TO_SETUP_GOOGLE_IN_APP_BILLING), Toast.LENGTH_LONG).show();

					// Update the UI widgets in case this operation is done after the user interface has been painted
					updateFragmentView();
				}
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Unbind the IabHelper, Note. Important!
		if (iabHelper != null) {
			iabHelper.dispose();
			iabHelper = null;
		}
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
		// Listener for the review application now, starting Google Play
		reviewNowTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startGooglePlay();
			}
		});

		// Listener for the donate button, launching the purchase flow
		donateButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Get the chosen amount for donation from spinner
				String donationAmount = (String) donationSizeSpinner.getSelectedItem();

				// Create new instance of ConfirmDonationDialog prepared with selected donation amount
				ConfirmDonationDialog dialog = ConfirmDonationDialog.newInstance(donationAmount);
				dialog.setTargetFragment(AppreciationFragment.this, ConfirmDonationDialog.CONFIRM_DONATION_DIALOG_REQUEST_CODE);
				dialog.show(getFragmentManager(), ConfirmDonationDialog.CONFIRM_DONATION_DIALOG_TAG);
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Only precede if the IabHelper is fully initialized
		if (iabHelper != null) {
			if (!iabHelper.handleActivityResult(requestCode, resultCode, data)) {
				// Any handling of activity results not related to In-app Billing goes here...
				// Only interested in OK results, don't care at all about the others
				if (resultCode == Activity.RESULT_OK) {
					// Only interested in certain request codes...
					switch (requestCode) {
						case (ConfirmDonationDialog.CONFIRM_DONATION_DIALOG_REQUEST_CODE):
							// Fetch the confirmed donation amount
							String confirmedDonationAmount = data.getStringExtra(ConfirmDonationDialog.CONFIRM_DONATION);

							// Generate token and store it for further processing
							developerPayload = tokenGenerator.nextToken();

							// Launch the purchase flow. Note, this method must be called from the UI thread
							iabHelper.launchPurchaseFlow(getActivity(), amountsAndProducts.get(confirmedDonationAmount), DONATION_REQUEST_CODE, purchaseFinishedListener, developerPayload);

							break;
						default:
							Log.e(LOG_TAG + ":onActivityResult()", "An unsupported result occurred, result code: \"" + resultCode + "\" and request code: \"" + requestCode + "\"");
					}
				}
			} else {
				// Activity results related to In-app Billing goes here...
			}
		}
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

	/**
	 * To update the {@link Spinner} containing all possible amounts to donate, with correct values.
	 */
	private void updateDonationSizeSpinner() {
		// Fetch all the amounts
		List<String> amounts = new ArrayList<String>(amountsAndProducts.keySet());

		// Sort the list to ensure correct order of display
		Collections.sort(amounts, new Comparator<String>() {
			@Override
			public int compare(String a1, String a2) {
				// Safe to it this way because we know the strings contains only digits and € signs
				a1 = a1.replace("€", "");
				a2 = a2.replace("€", "");

				if (Integer.valueOf(a1) < Integer.valueOf(a2)) {
					return -1;
				} else if (Integer.valueOf(a1) > Integer.valueOf(a2)) {
					return 1;
				}

				return 0;
			}
		});

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, amounts);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		donationSizeSpinner.setAdapter(adapter);
	}

	// Callback for when a purchase is finished
	IabHelper.OnIabPurchaseFinishedListener purchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
		@Override
		public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
			// Don't proceed the purchase any further if IabHelper has been disposed
			if (iabHelper != null) {
				// Check for any errors
				if (result.isSuccess()) {
					if (verifyDeveloperPayload(purchase)) {
						// Everything was fine, consume the donation to make it possible to donate the same amount again. Note, this is safe to call
						// from UI thread
						iabHelper.consumeAsync(purchase, consumeFinishedListener);

						// As the purchase went through a toast can be shown telling user a successful donation has been made
						Toast.makeText(context, getString(R.string.TOAST_DONATION_SUCCESSFUL), Toast.LENGTH_LONG).show();
					} else { // Developer payload couldn't be verified
						// Log(both to LogCat and file)
						Log.e(LOG_TAG + ".OnIabPurchaseFinishedListener.class:onIabPurchaseFinished()", "Error purchasing, developer payload couldn't be verified. Payload of request: \"" + developerPayload + "\", payload of response: \"" + purchase.getDeveloperPayload() + "\"");
						logger.log2File("Error purchasing, developer payload couldn't be verified. Payload of request: \"" + developerPayload + "\", payload of response: \"" + purchase.getDeveloperPayload() + "\"");

						// Show a toast at last
						Toast.makeText(context, getString(R.string.TOAST_DONATION_FAILED_AUTHENTICITY_VERIFICATION_FAIL), Toast.LENGTH_LONG).show();
					}
				} else { // An error occurred during purchase
					// User cancelled purchase, show a more user friendly toast
					if (result.getResponse() == IabHelper.IABHELPER_USER_CANCELLED) {
						Toast.makeText(context, R.string.TOAST_DONATION_FAILED_PURCHASE_ERROR_PURCHASE_CANCELED, Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(context, getString(R.string.TOAST_DONATION_FAILED_PURCHASE_ERROR), Toast.LENGTH_LONG).show();
					}

					// Log the error as usual
					Log.e(LOG_TAG + ".OnIabPurchaseFinishedListener.class:onIabPurchaseFinished()", "Error purchasing, message of result: \"" + result.getMessage() + "\"");
					logger.log2File("Error purchasing, result: \"" + result.getMessage() + "\"");
				}
			}
		}
	};

	// Callback for when a consumption is complete
	IabHelper.OnConsumeFinishedListener consumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
		@Override
		public void onConsumeFinished(Purchase purchase, IabResult result) {
			// Don't proceed with the consume process if IabHelper has been disposed
			if (iabHelper != null) {
				// Don't care about the success case as the purchase of the donation went through it's fine
				if (result.isFailure()) {
					// Log this error to LogCat and file
					Log.e(LOG_TAG + ".ConsumeFinishedListener.class", "Error consuming, result: \"" + result.getMessage() + "\"");
					logger.log2File("Error consuming purchase, result: \"" + result.getMessage() + "\"");
				}
			}
		}
	};

	// Callback for when a inventory query has finished
	IabHelper.QueryInventoryFinishedListener queryInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
		@Override
		public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
			// Don't proceed with the query inventory process if IabHelper has been disposed
			if (iabHelper != null) {
				if (result.isSuccess()) {
					// Go through each product and get the purchases
					for (String key : amountsAndProducts.keySet()) {
						Purchase purchase = inventory.getPurchase(amountsAndProducts.get(key));

						// If there exists a purchase then consume it
						if (purchase != null) {
							iabHelper.consumeAsync(purchase, consumeFinishedListener);
						}
					}
				} else {
					// Log this error to LogCat and file
					Log.e(LOG_TAG + ".QueryInventoryFinishedListener.class", "Error querying inventory, result: \"" + result.getMessage() + "\"");
					logger.log2File("Error querying inventory, result: \"" + result.getMessage() + "\"");
				}
			}
		}
	};

	/**
	 * To verify that given {@link Purchase} contains a developer payload matching the one the purchase flow was launched with.
	 * 
	 * @param purchase
	 *            <code>Purchase</code> which developer payload is checked.
	 * @return <code>true</code> if the purchase's developer payload matches, else <code>false</code>.
	 */
	private boolean verifyDeveloperPayload(Purchase purchase) {
		// The generated developer payload must have some sane content
		if (developerPayload == null || developerPayload.length() < 1) {
			return false;
		}

		// Fetch developer payload from purchase
		String purchaseDeveloperPayload = purchase.getDeveloperPayload();

		// The same goes for the developer payload originating from context
		if (purchaseDeveloperPayload == null || purchaseDeveloperPayload.length() < 1) {
			return false;
		}

		return developerPayload.equals(purchase.getDeveloperPayload());
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
	@SuppressLint("TrulyRandom")
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
