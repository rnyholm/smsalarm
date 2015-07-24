/**
 * Copyright (c) 2014 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ax.ha.it.smsalarm.R;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * {@link Fragment} containing all the <b><i>About</i></b> view.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 */
public class AboutFragment extends SherlockFragment {

	/**
	 * To create a new instance of {@link AboutFragment}.
	 */
	public AboutFragment() {
		// Just empty...
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.about, container, false);

		TextView buildTextView = (TextView) view.findViewById(R.id.aboutBuild_tv);
		TextView versionTextView = (TextView) view.findViewById(R.id.aboutVersion_tv);

		// Set correct text, build and version number, to the TextViews
		buildTextView.setText(String.format(getString(R.string.ABOUT_BUILD), getString(R.string.APPLICATION_BUILD)));
		versionTextView.setText(String.format(getString(R.string.ABOUT_VERSION), getString(R.string.APPLICATION_VERSION)));

		return view;
	}
}
