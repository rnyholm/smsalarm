/**
 * Copyright (c) 2015 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.fragment;

import java.util.EnumSet;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import ax.ha.it.smsalarm.alarm.Alarm;
import ax.ha.it.smsalarm.alarm.Alarm.AlarmType;
import ax.ha.it.smsalarm.alarm.log.adapter.AlarmLogItemAdapter;
import ax.ha.it.smsalarm.alarm.log.model.AlarmLogItem;

/**
 * An extension of {@link AlarmLogFragment}, which holds exactly the same logic and functionality as it's superclass. The only difference is that this
 * {@link Fragment} is only showing a log of {@link AlarmLogItem}'s which contains {@link Alarm}'s of {@link AlarmType#PRIMARY}.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 * @see AlarmLogFragment
 * @see SecondaryAlarmLogFragment
 */
public class PrimaryAlarmLogFragment extends AlarmLogFragment {
	/**
	 * To complete the creation of a {@link AlarmLogFragment} object by setting correct adapter({@link AlarmLogItemAdapter}) and populate the
	 * <code>AlarmLogFragment</code> with {@link AlarmLogItem}'s, containing {@link Alarm}'s of {@link AlarmType#PRIMARY}.
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		AlarmLogItemAdapter adapter = new AlarmLogItemAdapter(getActivity());
		createAlarmLogItems(adapter, EnumSet.<AlarmType> of(AlarmType.PRIMARY));
		setListAdapter(adapter);
	}
}
