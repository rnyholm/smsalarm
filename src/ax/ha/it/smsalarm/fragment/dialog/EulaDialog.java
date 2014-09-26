package ax.ha.it.smsalarm.fragment.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.activity.Splash;
import ax.ha.it.smsalarm.handler.LogHandler;
import ax.ha.it.smsalarm.handler.LogHandler.LogPriorities;

/**
 * {@link DialogFragment} that shows the <b><i>End User License Agreement</i></b> and let's the user either accept or decline it.
 *
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 * @see Splash#doNegativeClick()
 * @see Splash#doNegativeClick()
 * @see #EULA_DIALOG_TAG
 * @see #EULA_DIALOG_REQUEST_CODE
 */
public class EulaDialog extends DialogFragment {
	private static final String LOG_TAG = EulaDialog.class.getSimpleName();

	// Dialog tag can come in handy for classes using this dialog
	public static final String EULA_DIALOG_TAG = "eulaDialog";

	// For logging
	private LogHandler logger = LogHandler.getInstance();

	// Must have application context
	private Context context;

	/**
	 * To create a new instance of {@link EulaDialog}.
	 */
	public EulaDialog() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":EulaDialog()", "Creating a new EULA dialog fragment");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreate()", "Setting Context to dialog fragment");

		// Set context here, it's safe because this dialog fragment has been attached to it's container, hence we have access to context
		context = getActivity();

		// This dialog the user MUST close by pressing either accept or decline
		setCancelable(false);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreateDialog()", "Creating and initializing dialog fragment");

		// Setup the dialog with correct resources, listeners and values
		// @formatter:off
		return new AlertDialog.Builder(context)
				.setIcon(android.R.drawable.ic_dialog_info) // Set icon
				.setTitle(R.string.EULA_TITLE) 				// Set title
				.setMessage(R.string.EULA) 					// Set message
				// @formatter:on

				.setPositiveButton(R.string.EULA_AGREE, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreateDialog()", "Positive Button pressed");

						if (context instanceof Splash) {
							// Call method in activity owning this DialogFragment to do the actual on click handling
							((Splash) context).doPositiveClick();
						} else {
							logger.logCatTxt(LogPriorities.ERROR, LOG_TAG, "Can't handle positive button pressed, context (getActivity()) is of incorrect instance: \"" + context.getClass().getSimpleName() + "\" expected is: \"" + Splash.class.getSimpleName() + "\"");
						}
					}
				})

				.setNegativeButton(R.string.EULA_DECLINE, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreateDialog()", "Negative Button pressed");

						if (context instanceof Splash) {
							((Splash) context).doNegativeClick();
						} else {
							logger.logCatTxt(LogPriorities.ERROR, LOG_TAG, "Can't handle negative button pressed, context (getActivity()) is of incorrect instance: \"" + context.getClass().getSimpleName() + "\" expected is: \"" + Splash.class.getSimpleName() + "\"");
						}
					}
				})

				.create();
	}
}
