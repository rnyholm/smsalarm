/**
 * Copyright (c) 2015 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.fragment;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.alarm.Alarm;
import ax.ha.it.smsalarm.alarm.Alarm.AlarmType;
import ax.ha.it.smsalarm.alarm.log.adapter.AlarmLogItemAdapter;
import ax.ha.it.smsalarm.alarm.log.model.AlarmLogItem;
import ax.ha.it.smsalarm.fragment.dialog.AlarmInfoDialog;
import ax.ha.it.smsalarm.handler.DatabaseHandler;

import com.actionbarsherlock.app.SherlockListFragment;

/**
 * {@link Fragment} containing all the views and user interface widgets for the list of {@link AlarmLogItem}'s within the application. An
 * <code>AlarmLogItem</code> is simply an {@link Alarm} object wrapped into another object(<code>AlarmLogItem</code>) along with some utility
 * information needed for proper presentation. Also holds logic for opening a {@link AlarmInfoDialog}, displaying all info about the
 * <code>Alarm</code>.<br>
 * This particular <code>AlarmLogFragment</code> shows <code>Alarm</code>'s of both {@link AlarmType#PRIMARY} and {@link AlarmType#SECONDARY}, the
 * inherited classes {@link PrimaryAlarmLogFragment} and {@link SecondaryAlarmLogFragment} shows <code>Alarm</code>'s of respective
 * <code>AlarmType</code>.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 * @see AlarmLogItem
 * @see AlarmLogItemAdapter
 * @see PrimaryAlarmLogFragment
 * @see SecondaryAlarmLogFragment
 */
public class AlarmLogFragment extends SherlockListFragment {

	/**
	 * Creates a new instance of {@link AlarmLogFragment}.
	 */
	public AlarmLogFragment() {
		// Just empty...
	}

	/**
	 * To get the correct {@link View} upon creation of a {@link AlarmLogFragment} object.
	 */
	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.alarm_log_list, null);
	}

	/**
	 * To complete the creation of a {@link AlarmLogFragment} object by setting correct adapter({@link AlarmLogItemAdapter}) and populate the
	 * <code>AlarmLogFragment</code> with {@link AlarmLogItem}'s, containing {@link Alarm}'s of {@link AlarmType#PRIMARY} and
	 * {@link AlarmType#SECONDARY}.
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		AlarmLogItemAdapter adapter = new AlarmLogItemAdapter(getActivity());
		createAlarmLogItems(adapter, EnumSet.<AlarmType> of(AlarmType.PRIMARY, AlarmType.SECONDARY));
		setListAdapter(adapter);
	}

	/**
	 * To create the {@link AlarmLogItem}'s that goes into the list of Alarm Log items in the application. The created <code>AlarmLogItem</code>'s
	 * will be added to the given {@link AlarmLogItemAdapter}.
	 * 
	 * @param adapter
	 *            AlarmLogItemAdapter to which the created AlarmLogItem are added.
	 * @param alarmTypes
	 *            {@link EnumSet} of {@link AlarmType}'s containing all types of alarm thats wanted to be displayed in the alarm log.
	 */
	protected void createAlarmLogItems(AlarmLogItemAdapter adapter, EnumSet<AlarmType> alarmTypes) {
		// Initialize database handler object from context
		DatabaseHandler db = new DatabaseHandler(getActivity());

		// Fetch all alarms in an organized way
		TreeMap<String, HashMap<String, List<Alarm>>> organisedAlarms = db.fetchAllAlarmsSorted(alarmTypes);

		// Iterator for iterating over the years
		Iterator<Entry<String, HashMap<String, List<Alarm>>>> it0 = organisedAlarms.entrySet().iterator();

		// Iterate through the map and populate the adapter with data
		while (it0.hasNext()) {
			// Fetch entry alarms per year
			Entry<String, HashMap<String, List<Alarm>>> alarmsPerYearEntry = it0.next();

			// Get the map containing alarms per month
			HashMap<String, List<Alarm>> alarmsPerMonth = alarmsPerYearEntry.getValue();

			// Iterator to iterate over alarms per month
			Iterator<Entry<String, List<Alarm>>> it1 = alarmsPerMonth.entrySet().iterator();

			// Iterate over alarms per month
			while (it1.hasNext()) {
				// Fetch entry alarms per month
				Entry<String, List<Alarm>> alarmsPerMonthEntry = it1.next();

				// Get the alarms
				List<Alarm> alarms = alarmsPerMonthEntry.getValue();

				// Add a "header" item to the adapter
				adapter.add(new AlarmLogItem(alarmsPerYearEntry.getKey(), alarmsPerMonthEntry.getKey(), alarms.size()));

				// Iterate over every alarm and add them to the adapter
				for (Alarm alarm : alarms) {
					adapter.add(new AlarmLogItem(alarm));
				}
			}
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO: Decide if acknowledge UI or if the alarm info dialog should be shown

		// Get alarm from selected alarm info
		Alarm alarm = ((AlarmLogItem) getListView().getItemAtPosition(position)).getAlarm();

		// Must pass over alarm which information should be shown
		Bundle arguments = new Bundle();
		arguments.putParcelable(AlarmInfoDialog.ALARM_INFO, alarm);

		// Create dialog as usual, but put arguments in it also
		AlarmInfoDialog dialog = new AlarmInfoDialog();
		dialog.setArguments(arguments);
		dialog.setTargetFragment(AlarmLogFragment.this, AlarmInfoDialog.ALARM_INFO_DIALOG_REQUEST_CODE);
		dialog.show(getFragmentManager(), AlarmInfoDialog.ALARM_INFO_DIALOG_TAG);
	}
}
