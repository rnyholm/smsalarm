/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.slidingmenu.model;

import ax.ha.it.smsalarm.fragment.SlidingMenuFragment;
import ax.ha.it.smsalarm.handler.LogHandler;
import ax.ha.it.smsalarm.handler.LogHandler.LogPriorities;
import ax.ha.it.smsalarm.slidingmenu.adapter.SlidingMenuAdapter;

import com.google.common.base.Optional;

/**
 * Class representing an item in applications <code>SlidingMenu</code>.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 * @see SlidingMenuFragment
 * @see SlidingMenuAdapter
 */
public class SlidingMenuItem {
	// Logging information
	private String LOG_TAG = getClass().getSimpleName();
	private LogHandler logger = LogHandler.getInstance();

	private int id = -1;
	private String title;
	private Optional<Integer> optionalIconResource;

	/**
	 * To create a new menu item with just a title, this means this menu items icon resource will be
	 * absent. This menu item will also get a id of <b>-1</b>.
	 * 
	 * @param title
	 *            Title of this menu item.
	 */
	public SlidingMenuItem(String title) {
		this.title = title;
		this.optionalIconResource = Optional.absent();
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":SlidingMenuItem()", "A new menu item has been created with id: \"" + this.id + "\", title: \"" + this.title + "\" and an absent icon resource");
	}

	/**
	 * To create a new menu item with a id, title and a icon resource.
	 * 
	 * @param id
	 *            Id of this menu item.
	 * @param title
	 *            Title of this menu item.
	 * @param iconResource
	 *            Icon resource of this menu item.
	 */
	public SlidingMenuItem(int id, String title, int iconResource) {
		this.id = id;
		this.title = title;
		this.optionalIconResource = Optional.fromNullable(iconResource);

		// Set correct logmessage
		if (this.optionalIconResource.isPresent()) {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":SlidingMenuItem()", "A new menu item has been created with id: \"" + this.id + "\", title: \"" + this.title + "\" and optional icon resource: \"" + Integer.toString(this.optionalIconResource.get()) + "\"");
		} else {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":SlidingMenuItem()", "A new menu item has been created with id: \"" + this.id + "\", title: \"" + this.title + "\" and an absent icon resource");
		}
	}

	/**
	 * To get this menu items id.
	 * 
	 * @return This menu items id.
	 */
	public int getId() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getId()", "Menu items id: \"" + id + "\" will be returned");
		return id;
	}

	/**
	 * To get this menu items title.
	 * 
	 * @return This menu items title.
	 */
	public String getTitle() {
		logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getTitle()", "Menu items title: \"" + title + "\" will be returned");
		return title;
	}

	/**
	 * To get this menu items <code>Optional</code> icon resource.
	 * 
	 * @return This menu items <code>Optional</code> icon resource.
	 */
	public Optional<Integer> getIconResource() {
		if (this.optionalIconResource.isPresent()) {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getIconResource()", "Menu items optional icon resource: \"" + Integer.toString(this.optionalIconResource.get()) + "\" will be returned");
		} else {
			logger.logCat(LogPriorities.DEBUG, LOG_TAG + ":getIconResource()", "An absent icon resource will be returned");
		}
		return optionalIconResource;
	}
}