package ax.ha.it.smsalarm.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.handler.LogHandler;
import ax.ha.it.smsalarm.handler.LogHandler.LogPriorities;
import ax.ha.it.smsalarm.handler.PreferencesHandler;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * <code>Fragment</code> containing all the views and user interface widgets for the <b><i>Sound Settings</i></b>. Fragment does also contain all
 * logic for the widgets.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class SoundSettingsFragment extends SherlockFragment implements ApplicationFragment {
	// Log tag string
	private final String LOG_TAG = getClass().getSimpleName();

	// Objects needed for logging and shared preferences handling
	private final LogHandler logger = LogHandler.getInstance();
	private final PreferencesHandler prefHandler = PreferencesHandler.getInstance();

	// Must have the application context
	private final Context context;

	/**
	 * To create a new <code>SoundSettingsFragment</code> with given context.
	 * 
	 * @param context
	 *            Context
	 * @see LogHandler#logCat(LogPriorities, String, String)
	 */
	public SoundSettingsFragment(Context context) {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":SoundSettingsFragment()", "Creating a new Sound settings fragment with given context:  \"" + context + "\"");
		this.context = context;
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
