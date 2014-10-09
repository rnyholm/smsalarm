/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.slidingmenu.model;

import ax.ha.it.smsalarm.fragment.SlidingMenuFragment;
import ax.ha.it.smsalarm.slidingmenu.adapter.SlidingMenuAdapter;

import com.google.common.base.Optional;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

/**
 * Class representing an item in <b><i>Sms Alarm</i></b>'s {@link SlidingMenu}.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 * @see SlidingMenuFragment
 * @see SlidingMenuAdapter
 */
public class SlidingMenuItem {
	// Id, title, and icon resource parts of a menu item
	private int id = -1;
	private String title;
	private Optional<Integer> optionalIconResource;

	/**
	 * Creates a new instance of {@link SlidingMenuItem} with just a title.<br>
	 * By doing this the menu item will get a icon resource that's <code>absent</code> and an id of <b>-1</b>.
	 * 
	 * @param title
	 *            Title of this menu item.
	 */
	public SlidingMenuItem(String title) {
		this.title = title;
		this.optionalIconResource = Optional.absent();
	}

	/**
	 * Creates a new instance of {@link SlidingMenuItem} with a id, title and icon resource.
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
	}

	/**
	 * To get id of Sliding menu item.
	 * 
	 * @return Id of Sliding menu item.
	 */
	public int getId() {
		return id;
	}

	/**
	 * To get title of Sliding menu item.
	 * 
	 * @return Title of Sliding menu item.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * To get icon resource of Sliding menu item.
	 * 
	 * @return {@link Optional}, either an <code>absent</code> one if no icon resource exists or a <code>present</code> one with Sliding menu items
	 *         icon resource.
	 */
	public Optional<Integer> getIconResource() {
		return optionalIconResource;
	}

	@Override
	public String toString() {
		return "SlidingMenuItem [id=" + id + ", title=" + title + ", optionalIconResource=" + (optionalIconResource.isPresent() ? Integer.toString(optionalIconResource.get()) : "icon absent") + "]";
	}
}