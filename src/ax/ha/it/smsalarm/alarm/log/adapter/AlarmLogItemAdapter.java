/**
 * Copyright (c) 2015 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm.alarm.log.adapter;

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
import ax.ha.it.smsalarm.alarm.Alarm;
import ax.ha.it.smsalarm.alarm.Alarm.AlarmType;
import ax.ha.it.smsalarm.alarm.log.model.AlarmLogItem;
import ax.ha.it.smsalarm.fragment.AlarmLogFragment;

/**
 * An adapter for wrapping {@link AlarmLogItem}'s into a neat {@link ListView}.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.3.1
 * @since 2.3.1
 * @see AlarmLogItem
 * @see AlarmLogFragment
 */
public class AlarmLogItemAdapter extends ArrayAdapter<AlarmLogItem> {

	/**
	 * Creates a new instance of {@link AlarmLogItemAdapter} with given {@link Context}.
	 * 
	 * @param context
	 *            The Context in which the adapter is used.
	 */
	public AlarmLogItemAdapter(Context context) {
		super(context, 0);
	}

	@SuppressLint({ "InflateParams", "ViewHolder" })
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		AlarmLogItem alarmLogItem = getItem(position);

		// If current alarm log item is a section title
		if (alarmLogItem.isSectionTitle()) {
			// Get correct convert view, this differs depending on if current alarm log item is a section
			// title or an ordinary alarm item
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.alarm_log_section, null);

			// Set correct text to layout
			TextView title = (TextView) convertView.findViewById(R.id.alarmLogSectionTitle_tv);
			title.setText(alarmLogItem.getMonthReceived() + " " + alarmLogItem.getYearReceived());
		} else {
			Alarm alarm = alarmLogItem.getAlarm();

			convertView = LayoutInflater.from(getContext()).inflate(R.layout.alarm_log_item, null);

			ImageView icon = (ImageView) convertView.findViewById(R.id.alarmLogItemIcon_iv);
			icon.setImageResource(AlarmType.PRIMARY.equals(alarm.getAlarmType()) ? R.drawable.ic_primary_alarm : R.drawable.ic_secondary_alarm);

			TextView sender = (TextView) convertView.findViewById(R.id.alarmLogSender_tv);
			sender.setText(alarm.getSender());

			TextView message = (TextView) convertView.findViewById(R.id.alarmLogMessage_tv);
			message.setText(alarm.getMessage());

			TextView date = (TextView) convertView.findViewById(R.id.alarmLogDate_tv);
			date.setText(alarm.getReceivedForLog());
		}

		// Return the modified view
		return convertView;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}
}
