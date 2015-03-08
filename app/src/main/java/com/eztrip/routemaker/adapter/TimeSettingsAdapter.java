package com.eztrip.routemaker.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.eztrip.R;
import com.eztrip.model.RouteData;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by Steve on 2015/2/26.
 * 修改时间中计划列表的adapter
 */
public class TimeSettingsAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private Context context;
    private LayoutInflater inflater;

    public TimeSettingsAdapter(Context context) {
        this.context = context;
        this.inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getHeaderView(int i, View view, ViewGroup viewGroup) {
        HeaderViewHolder holder;
        if (view == null) {
            holder = new HeaderViewHolder();
            view = inflater.inflate(R.layout.routemaker_day_header, viewGroup, false);
            holder.date = (TextView) view.findViewById(R.id.day_header_date);
            view.setTag(holder);
        } else
            holder = (HeaderViewHolder) view.getTag();
        String headerText = new String();
        int day = RouteData.singleEvents.get(i).day;
        headerText += "第" + Integer.toString(day + 1) + "天";
        holder.date.setText(headerText);
        return view;
    }

    @Override
    public long getHeaderId(int i) {
        return RouteData.singleEvents.get(i).day;
    }

    @Override
    public int getCount() {
        return RouteData.singleEvents.size();
    }

    @Override
    public Object getItem(int position) {
        return RouteData.singleEvents.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.routemaker_time_item, parent, false);
            holder.map = (ImageView) convertView.findViewById(R.id.item_map);
            holder.change = (ImageView) convertView.findViewById(R.id.item_change);
            holder.detail = (TextView) convertView.findViewById(R.id.item_content);
            holder.type = (ImageView) convertView.findViewById(R.id.item_type);
            holder.time = (TextView) convertView.findViewById(R.id.item_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (RouteData.singleEvents.get(position).type == RouteData.ActivityType.ACCOMMODATION) {
            holder.type.setImageResource(R.drawable.ic_accomodation);
        } else if (RouteData.singleEvents.get(position).type == RouteData.ActivityType.SPOT) {
            holder.type.setImageResource(R.drawable.ic_spot);
        } else if (RouteData.singleEvents.get(position).type == RouteData.ActivityType.DIET) {
            holder.type.setImageResource(R.drawable.ic_dining);
        } else if (RouteData.singleEvents.get(position).type == RouteData.ActivityType.TRAFFIC) {
            holder.type.setImageResource(R.drawable.ic_traffic);
        } else if (RouteData.singleEvents.get(position).type == RouteData.ActivityType.OTHERS) {
            holder.type.setImageResource(R.drawable.ic_spot_others);
        }
        holder.detail.setText(RouteData.singleEvents.get(position).detail);
        holder.time.setText(RouteData.singleEvents.get(position).startTime + "-" + RouteData.singleEvents.get(position).finishTime);
        holder.map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 跳转到地图页面
                Toast.makeText(context, "地图定位 " + RouteData.singleEvents.get(position).detail, Toast.LENGTH_LONG).show();
            }
        });
        holder.change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 跳转到餐厅信息页面
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                View view = inflater.inflate(R.layout.routemaker_timesettings_timepicker, null);
                builder.setView(view);
                builder.setTitle(RouteData.singleEvents.get(position).detail);
                final TimePicker startTime = (TimePicker) view.findViewById(R.id.timepicker_starttime);
                final TimePicker finishTime = (TimePicker) view.findViewById(R.id.timepicker_finishtime);
                int startHour = Integer.parseInt(RouteData.singleEvents.get(position).startTime.split(":")[0]);
                final int startMinute = Integer.parseInt(RouteData.singleEvents.get(position).startTime.split(":")[1]);
                int finishHour = Integer.parseInt(RouteData.singleEvents.get(position).finishTime.split(":")[0]);
                int finishMinute = Integer.parseInt(RouteData.singleEvents.get(position).finishTime.split(":")[1]);
                startTime.setCurrentHour(startHour);
                startTime.setCurrentMinute(startMinute);
                finishTime.setCurrentHour(finishHour);
                finishTime.setCurrentMinute(finishMinute);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int newStartHour = startTime.getCurrentHour();
                        int newStartMinute = startTime.getCurrentMinute();
                        int newFinishHour = finishTime.getCurrentHour();
                        int newFinishMinute = finishTime.getCurrentMinute();
                        int startMinuteCount = newStartHour * 60 + newStartMinute;
                        int finishMinuteCount = newFinishHour * 60 + newFinishMinute;
                        if (startMinuteCount >= finishMinuteCount)
                            Toast.makeText(context, "请将开始时间设置在结束时间之前", Toast.LENGTH_LONG).show();
                        else {
                            RouteData.singleEvents.get(position).startTime = new StringBuilder().append(Integer.toString(newStartHour)).append(":").append((newStartMinute < 10) ? "0" + newStartMinute : newStartMinute).toString();
                            RouteData.singleEvents.get(position).finishTime = new StringBuilder().append(Integer.toString(newFinishHour)).append(":").append((newFinishMinute < 10) ? "0" + newFinishMinute : newFinishMinute).toString();
                            TimeSettingsAdapter.this.notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 跳转到景点信息页面
                Toast.makeText(context, "详细信息 " + RouteData.singleEvents.get(position).detail, Toast.LENGTH_LONG).show();
            }
        });
        return convertView;

    }

    class ViewHolder {
        ImageView change;
        ImageView map;
        TextView detail;
        ImageView type;
        TextView time;
    }

    class HeaderViewHolder {
        TextView date;
    }
}
