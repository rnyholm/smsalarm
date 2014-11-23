/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.license.model;

import ax.ha.it.smsalarm.fragment.OpenSourceFragment;
import ax.ha.it.smsalarm.license.adapter.LicenseItemAdapter;

/**
 * Class representing a license <b><i>source/library/software</i></b> item that's used in <b><i>Sms Alarm</i></b>.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 * @see LicenseItemAdapter
 * @see OpenSourceFragment
 */
public class LicenseItem {
	// Title, and URI resource of a licensed item
	private String title;
	private String URI;

	/**
	 * Creates a new instance of {@link LicenseItem} with just a title.
	 * 
	 * @param title
	 *            Title of the license item.
	 */
	public LicenseItem(String title) {
		this.title = title;
		this.URI = "";
	}

	/**
	 * Creates a new instance of {@link LicenseItem} with a title and URI.
	 * 
	 * @param title
	 *            Title of the license item.
	 * @param URI
	 *            URI to the license items web site.
	 */
	public LicenseItem(String title, String URI) {
		this.title = title;
		this.URI = URI;
	}

	/**
	 * To get title of License item.
	 * 
	 * @return Title of License item.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * To get URI of License item.
	 * 
	 * @return URI of License item.
	 */
	public String getURI() {
		return URI;
	}

	/**
	 * To find out if license items URI is missing, decided by the length of the URI, 0 = missing.
	 * 
	 * @return <code>true</code> if it's missing, else <code>false</code>.
	 */
	public boolean isURIMissing() {
		return URI.length() == 0;
	}
}
