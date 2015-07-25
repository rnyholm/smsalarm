/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.license.adapter.LicenseItemAdapter;
import ax.ha.it.smsalarm.license.model.LicenseItem;
import ax.ha.it.smsalarm.util.Utils;

import com.actionbarsherlock.app.SherlockListFragment;

/**
 * {@link Fragment} containing all the views and user interface widgets for the list of {@link LicenseItem}'s within the application. Also holds logic
 * for opening web browser and redirecting user to License Items URI.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 * @see LicenseItem
 * @see LicenseItemAdapter
 */
public class OpenSourceFragment extends SherlockListFragment {

	/**
	 * Creates a new instance of {@link OpenSourceFragment}.
	 */
	public OpenSourceFragment() {
		// Just empty...
	}

	/**
	 * To get the correct {@link View} upon creation of a {@link OpenSourceFragment} object.
	 */
	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.license_list, null);
	}

	/**
	 * To complete the creation of a {@link OpenSourceFragment} object by setting correct adapter({@link LicenseItemAdapter}) and populate the
	 * <code>OpenSourceFragment</code> with {@link LicenseItem}'s.
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Create adapter, menu items and at last set correct adapter to this fragment
		LicenseItemAdapter adapter = new LicenseItemAdapter(getActivity());
		createLicensedItems(adapter);
		setListAdapter(adapter);
	}

	/**
	 * To create the {@link LicenseItem}'s that goes into the list of License items in the application. The created <code>LicenseItem</code>'s will be
	 * added to the given {@link LicenseItemAdapter}.
	 * 
	 * @param adapter
	 *            LicenseItemAdapter to which the created LicenseItem's are added.
	 */
	private void createLicensedItems(LicenseItemAdapter adapter) {
		// Create license items and add them to given adapter
		adapter.add(new LicenseItem(getString(R.string.LICENSE_APACHE_TITLE)));
		adapter.add(new LicenseItem(getString(R.string.LICENSE_APACHE_V2_TITLE), getString(R.string.LICENSE_APACHE_V2_URI)));
		adapter.add(new LicenseItem(getString(R.string.LICENSE_OTHER_TITLE)));
		adapter.add(new LicenseItem(getString(R.string.LICENSE_OTHER_SLIDING_MENU_TITLE), getString(R.string.LICENSE_OTHER_SLIDING_MENU_URI)));
		adapter.add(new LicenseItem(getString(R.string.LICENSE_OTHER_ACTIONBAR_SHERLOCK_TITLE), getString(R.string.LICENSE_OTHER_ACTIONBAR_SHERLOCK_URI)));
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		LicenseItem licenseItem = (LicenseItem) getListView().getItemAtPosition(position);

		// Only if license item got an URI
		if (!licenseItem.isURIMissing()) {
			Utils.browseURI(getActivity(), licenseItem.getURI());
		}
	}
}
