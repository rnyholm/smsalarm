package ax.ha.it.smsalarm.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.handler.LogHandler;
import ax.ha.it.smsalarm.handler.LogHandler.LogPriorities;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * <code>Fragment</code> containing all the <b><i>About</i></b> view.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class AboutFragment extends SherlockFragment {
	private final String LOG_TAG = getClass().getSimpleName();

	private final LogHandler logger = LogHandler.getInstance();

	/**
	 * To create a new <code>AboutFragment</code>.
	 * 
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 */
	public AboutFragment() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":AboutFragment()", "Creating a new About fragment");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":onCreateView()", "Creating view for this fragment");
		View view = inflater.inflate(R.layout.about, container, false);

		TextView buildTextView = (TextView) view.findViewById(R.id.aboutBuild_tv);
		TextView versionTextView = (TextView) view.findViewById(R.id.aboutVersion_tv);

		// Set correct text, build and version number, to the TextViews
		buildTextView.setText(String.format(getString(R.string.ABOUT_BUILD), getString(R.string.APP_BUILD)));
		versionTextView.setText(String.format(getString(R.string.ABOUT_VERSION), getString(R.string.APP_VERSION)));

		return view;
	}
}
