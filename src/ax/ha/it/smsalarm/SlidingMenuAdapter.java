/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import ax.ha.it.smsalarm.LogHandler.LogPriorities;

import com.google.common.base.Optional;

/**
 * An adapter for wrapping <code>SlidingMenuItem</code>'s into a neath <code>ListView</code>.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.2.1
 * @see SlidingMenuFragment
 * @see SlidingMenuItem
 */
public class SlidingMenuAdapter extends ArrayAdapter<SlidingMenuItem> {
	// Logging information
	private String LOG_TAG = getClass().getSimpleName();
	private LogHandler logger = LogHandler.getInstance();

	/**
	 * To create a new adapter from given <code>Context</code>.
	 * 
	 * @param context
	 *            Context
	 */
	public SlidingMenuAdapter(Context context) {
		super(context, 0);
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":SlidingMenuAdapter()", "A new adapter for the sliding menu items has been created");
	}

	/**
	 * To get the correct <code>View</code> depending on the given argument, for this class the only
	 * arguments taken into account is <code>position</code> and <code>convertView</code>. Convert
	 * view is set to correct layout depending on <code>SlidingMenuItem</code> at the given
	 * position.
	 * 
	 * @param position
	 *            Actual position for which it's wanted to get view for.
	 * @param convertView
	 *            View to be configured and returned.
	 * @param parent
	 *            Any <code>ViewGroup</code> that the convertView has as parent.
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getView()", "Start setting up view for position: \"" + Integer.toString(position) + "\"");

		SlidingMenuItem menuItem = getItem(position);

		if (isItemSectionTitle(position)) {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getView()", "Item at current position is a section title, setting up view for that");
			// Get correct convert view, this differs depending on if current menu item is a section
			// title or an ordinary menu item
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.menu_section, null);

			// Set correct text to layout
			TextView title = (TextView) convertView.findViewById(R.id.sectionTitle_tv);
			title.setText(menuItem.getTitle());
		} else {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getView()", "Item at current position is a menu item, setting up view for that");
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.menu_item, null);

			// Set correct icon to layout, it's safe to do a get() on an optional in this case
			// because we know that it has present data already
			ImageView icon = (ImageView) convertView.findViewById(R.id.menuItemIcon_iv);
			icon.setImageResource(menuItem.getIconResource().get());

			TextView title = (TextView) convertView.findViewById(R.id.menuItemTitle_tv);
			title.setText(menuItem.getTitle());
		}

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getView()", "View for position: \"" + Integer.toString(position) + "\" has ben correctly set, returning it");

		// Return the modified view
		return convertView;
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.BaseAdapter#areAllItemsEnabled()
	 */
	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	/**
	 * To figure out if menu item at given position should be enabled or not. If menu item is a
	 * sections title it should be disabled else enabled.
	 * 
	 * @param position
	 *            Position from where to get menu item to be checked.
	 * @return <code>true</code> if if menu item at given position should be enabled else
	 *         <code>false</code>.
	 * @see SlidingMenuAdapter#isItemSectionTitle(int)
	 */
	@Override
	public boolean isEnabled(int position) {
		if (isItemSectionTitle(position)) {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":isEnabled()", "Menu item should not be enabled, returning false");
			return false;
		}

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":isEnabled()", "Menu item should be enabled, returning true");
		return true;
	}

	/**
	 * To figure out if menu item at given position is a sections title or a menu item in the
	 * section.<b> This is decided whether or not the menu item holds a <code>icon resource</code>
	 * or not (<code>Optional.isPresent()</code>). If an <code>absent</code> icon resource was found
	 * then this menu item is a sections title, else not.
	 * 
	 * @param position
	 *            Position from where to get menu item to be checked.
	 * @return <code>true</code> if menu item at given position is a sections title else return
	 *         <code>false</code>.
	 */
	private boolean isItemSectionTitle(int position) {
		Optional<Integer> optionalIconResource = getItem(position).getIconResource();

		if (optionalIconResource.isPresent()) {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":isItemSectionTitle()", "Menu item is not a menu sections title");
			return false;
		}

		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":isItemSectionTitle()", "Menu item is a menu sections title");
		return true;
	}
}
