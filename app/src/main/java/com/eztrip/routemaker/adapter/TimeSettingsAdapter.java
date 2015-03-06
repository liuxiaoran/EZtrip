package com.eztrip.routemaker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
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
                Toast.makeText(context, "修改时间 " + RouteData.singleEvents.get(position).detail, Toast.LENGTH_LONG).show();
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
