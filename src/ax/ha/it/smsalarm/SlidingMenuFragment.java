package ax.ha.it.smsalarm;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.actionbarsherlock.app.SherlockListFragment;

public class SlidingMenuFragment extends SherlockListFragment implements ExpandableListView.OnChildClickListener {

	private ExpandableListView sectionListView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		List<Section> sectionList = createMenu();

		View view = inflater.inflate(R.layout.slidingmenu_fragment, container, false);
		this.sectionListView = (ExpandableListView) view.findViewById(R.id.slidingmenu_view);
		this.sectionListView.setGroupIndicator(null);

		SectionListAdapter sectionListAdapter = new SectionListAdapter(this.getActivity(), sectionList);
		this.sectionListView.setAdapter(sectionListAdapter);

		this.sectionListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				return true;
			}
		});

		this.sectionListView.setOnChildClickListener(this);

		int count = sectionListAdapter.getGroupCount();
		for (int position = 0; position < count; position++) {
			this.sectionListView.expandGroup(position);
		}

		return view;
	}

	private List<Section> createMenu() {
		List<Section> sectionList = new ArrayList<Section>();

		Section oDemoSection = new Section("Demos");
		oDemoSection.addSectionItem(101, "List/Detail (Fragment)", "slidingmenu_friends");
		oDemoSection.addSectionItem(102, "Airport (AsyncTask)", "slidingmenu_airport");

		Section oGeneralSection = new Section("General");
		oGeneralSection.addSectionItem(201, "Settings", "slidingmenu_settings");
		oGeneralSection.addSectionItem(202, "Rate this app", "slidingmenu_rating");
		oGeneralSection.addSectionItem(203, "Eula", "slidingmenu_eula");
		oGeneralSection.addSectionItem(204, "Quit", "slidingmenu_quit");

		sectionList.add(oDemoSection);
		sectionList.add(oGeneralSection);
		return sectionList;
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

		switch ((int) id) {
			case 101:
				// TODO
				break;
			case 102:
				// TODO
				break;
			case 201:
				// TODO
				break;
			case 202:
				// TODO
				break;
			case 203:
				// TODO
				break;
			case 204:
				// TODO
				break;
		}

		return false;
	}
}