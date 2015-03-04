package ax.ha.it.smsalarm.fragment;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import ax.ha.it.smsalarm.R;
import ax.ha.it.smsalarm.alarm.Alarm;
import ax.ha.it.smsalarm.alarm.log.adapter.AlarmLogItemAdapter;
import ax.ha.it.smsalarm.alarm.log.model.AlarmLogItem;
import ax.ha.it.smsalarm.handler.DatabaseHandler;

import com.actionbarsherlock.app.SherlockListFragment;

public class AlarmLogFragment extends SherlockListFragment {
	private static final String LOG_TAG = AlarmLogFragment.class.getSimpleName();

	// To get database access
	private DatabaseHandler db;

	/**
	 * Creates a new instance of {@link AlarmLogFragment}.
	 */
	public AlarmLogFragment() {
		// Just empty...
	}

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.alarm_log_list, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		AlarmLogItemAdapter adapter = new AlarmLogItemAdapter(getActivity());
		createAlarmLogItems(adapter);
		setListAdapter(adapter);
	}

	private void createAlarmLogItems(AlarmLogItemAdapter adapter) {
		// Initialize database handler object from context
		db = new DatabaseHandler(getActivity());

		TreeMap<String, HashMap<String, List<Alarm>>> organisedAlarms = db.fetchAllAlarmsOrginised();

		Iterator<Entry<String, HashMap<String, List<Alarm>>>> it0 = organisedAlarms.entrySet().iterator();
		while (it0.hasNext()) {
			Entry<String, HashMap<String, List<Alarm>>> alarmsPerYearEntry = it0.next();

			HashMap<String, List<Alarm>> alarmsPerMonth = alarmsPerYearEntry.getValue();

			Iterator<Entry<String, List<Alarm>>> it1 = alarmsPerMonth.entrySet().iterator();
			while (it1.hasNext()) {
				Entry<String, List<Alarm>> alarmsPerMonthEntry = it1.next();

				adapter.add(new AlarmLogItem(alarmsPerYearEntry.getKey(), alarmsPerMonthEntry.getKey()));

				for (Alarm alarm : alarmsPerMonthEntry.getValue()) {
					adapter.add(new AlarmLogItem(alarm));
				}
			}
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d(LOG_TAG + "onListItemClick()", "Alarm log item clicked but method not implemented yet");
	}
}
