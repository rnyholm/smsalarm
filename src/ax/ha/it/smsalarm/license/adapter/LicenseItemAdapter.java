/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.license.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.fragment.OpenSourceFragment;
import ax.ha.it.smsalarm.license.model.LicenseItem;

/**
 * An adapter for wrapping {@link LicenseItem}'s into a neat {@link ListView}.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 * @see LicenseItem
 * @see OpenSourceFragment
 */
public class LicenseItemAdapter extends ArrayAdapter<LicenseItem> {

	/**
	 * Creates a new instance of {@link LicenseItemAdapter} with given {@link Context}.
	 * 
	 * @param context
	 *            The Context in which the adapter is used.
	 */
	public LicenseItemAdapter(Context context) {
		super(context, 0);
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LicenseItem licenseItem = getItem(position);

		if (isItemSectionTitle(position)) {
			// Get correct convert view, this differs depending on if current license item is a section
			// title or an ordinary license item
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.license_section, null);

			// Set correct text to layout
			TextView title = (TextView) convertView.findViewById(R.id.licenseSectionTitle_tv);
			title.setText(licenseItem.getTitle());
		} else {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.license_item, null);

			TextView title = (TextView) convertView.findViewById(R.id.licenseItemTitle_tv);
			title.setText(licenseItem.getTitle());

			TextView URI = (TextView) convertView.findViewById(R.id.licenseItemURI_tv);
			URI.setText(licenseItem.getURI());
		}

		// Return the modified view
		return convertView;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	/**
	 * To figure out if license item at given position should be enabled or not. If license item is a sections title it should be disabled else
	 * enabled.
	 * 
	 * @param position
	 *            Position from where to get license item to be checked.
	 * @return <code>true</code> if if license item at given position should be enabled else <code>false</code>.
	 * @see LicenseItemAdapter#isItemSectionTitle(int)
	 */
	@Override
	public boolean isEnabled(int position) {
		// Titles are not enabled
		if (isItemSectionTitle(position)) {
			return false;
		}

		return true;
	}

	/**
	 * To figure out if license item at given position is a sections title or a license item in the section. This is decided whether or not the
	 * license item has a <b><i>URI</i></b> or not. If an the license items id is missing then this licensed item is a sections title, else not.
	 * 
	 * @param position
	 *            Position from where to get license item to be checked.
	 * @return <code>true</code> if license item at given position is a sections title else return <code>false</code>.
	 */
	private boolean isItemSectionTitle(int position) {
		if (getItem(position).isURIMissing()) {
			return true;
		}

		return false;
	}
}
