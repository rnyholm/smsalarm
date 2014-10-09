/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.slidingmenu.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.fragment.SlidingMenuFragment;
import ax.ha.it.smsalarm.slidingmenu.model.SlidingMenuItem;

import com.google.common.base.Optional;

/**
 * An adapter for wrapping {@link SlidingMenuItem}'s into a neat {@link ListView}.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 * @see SlidingMenuFragment
 * @see SlidingMenuItem
 */
public class SlidingMenuAdapter extends ArrayAdapter<SlidingMenuItem> {

	/**
	 * Creates a new instance of {@link SlidingMenuAdapter} with given {@link Context}.
	 * 
	 * @param context
	 *            The Context in which the adapter is used.
	 */
	public SlidingMenuAdapter(Context context) {
		super(context, 0);
	}

	/**
	 * To get the correct {@link View} depending on the given argument, for this class the only arguments taken into account is <code>position</code>
	 * and <code>convertView</code>. Convert view is set to correct layout depending on {{@link SlidingMenuItem} at the given position.
	 * 
	 * @param position
	 *            Actual position for which it's wanted to get view for.
	 * @param convertView
	 *            View to be configured and returned.
	 * @param parent
	 *            Any {@link ViewGroup} that the convertView has as parent.
	 */
	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SlidingMenuItem menuItem = getItem(position);

		if (isItemSectionTitle(position)) {
			// Get correct convert view, this differs depending on if current menu item is a section
			// title or an ordinary menu item
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.menu_section, null);

			// Set correct text to layout
			TextView title = (TextView) convertView.findViewById(R.id.sectionTitle_tv);
			title.setText(menuItem.getTitle());
		} else {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.menu_item, null);

			// Set correct icon to layout, it's safe to do a get() on an optional in this case
			// because we know that it has present data already
			ImageView icon = (ImageView) convertView.findViewById(R.id.menuItemIcon_iv);
			icon.setImageResource(menuItem.getIconResource().get());

			TextView title = (TextView) convertView.findViewById(R.id.menuItemTitle_tv);
			title.setText(menuItem.getTitle());
		}

		// Return the modified view
		return convertView;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	/**
	 * To figure out if menu item at given position should be enabled or not. If menu item is a sections title it should be disabled else enabled.
	 * 
	 * @param position
	 *            Position from where to get menu item to be checked.
	 * @return <code>true</code> if if menu item at given position should be enabled else <code>false</code>.
	 * @see SlidingMenuAdapter#isItemSectionTitle(int)
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
	 * To figure out if menu item at given position is a sections title or a menu item in the section. This is decided whether or not the menu item
	 * holds a <code>icon resource</code> or not (<code>Optional.isPresent()</code>). If an <code>absent</code> icon resource was found then this menu
	 * item is a sections title, else not.
	 * 
	 * @param position
	 *            Position from where to get menu item to be checked.
	 * @return <code>true</code> if menu item at given position is a sections title else return <code>false</code>.
	 */
	private boolean isItemSectionTitle(int position) {
		Optional<Integer> optionalIconResource = getItem(position).getIconResource();

		if (optionalIconResource.isPresent()) {
			return false;
		}

		return true;
	}
}
