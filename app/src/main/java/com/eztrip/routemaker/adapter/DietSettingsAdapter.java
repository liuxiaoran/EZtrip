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
import com.eztrip.routemaker.data.RouteData;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by Steve on 2015/2/26.
 * 饭店设置中饭店列表的adapter
 */
public class DietSettingsAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private Context context;
    private LayoutInflater inflater;

    public DietSettingsAdapter(Context context) {
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
        int day = RouteData.dietTempInfo[i].period / 3 + 1;
        headerText += "第" + Integer.toString(day) + "天";
        holder.date.setText(headerText);
        return view;
    }

    @Override
    public long getHeaderId(int i) {
        return RouteData.dietTempInfo[i].period / 3;
    }

    @Override
    public int getCount() {
        return RouteData.dietTempInfo.length;
    }

    @Override
    public Object getItem(int position) {
        return RouteData.dietTempInfo[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
//        if (convertView == null) {
        holder = new ViewHolder();
        convertView = inflater.inflate(R.layout.routemaker_diet_item, parent, false);
        holder.change = (ImageView) convertView.findViewById(R.id.item_change);
        holder.detail = (TextView) convertView.findViewById(R.id.item_content);
        holder.delete = (ImageView) convertView.findViewById(R.id.item_delete);
        holder.map = (ImageView) convertView.findViewById(R.id.item_map);
//        } else {
//            holder = (ViewHolder) convertView.getTag();
//        }
        String timePeriod;
        switch (RouteData.dietTempInfo[position].period % 3) {
            case 0:
                timePeriod = "早餐：";
                break;
            case 1:
                timePeriod = "午餐：";
                break;
            case 2:
                timePeriod = "晚餐：";
                break;
            default:
                timePeriod = "";
                break;
        }
        holder.detail.setText(timePeriod + RouteData.dietTempInfo[position].detail);
        holder.map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 跳转到地图页面
                Toast.makeText(context, "地图定位 " + RouteData.dietTempInfo[position].detail, Toast.LENGTH_LONG).show();
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RouteData.dietTempInfo[position].detail = "无";
                notifyDataSetChanged();
            }
        });
        holder.change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 跳转到餐厅信息页面
                Toast.makeText(context, "修改 " + RouteData.dietTempInfo[position].detail, Toast.LENGTH_LONG).show();
            }
        });
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 跳转到餐厅信息页面
                Toast.makeText(context, "详细信息 " + RouteData.dietTempInfo[position].detail, Toast.LENGTH_LONG).show();
            }
        });
        return convertView;
    }

    class ViewHolder {
        ImageView change;
        ImageView map;
        TextView detail;
        ImageView delete;
    }

    class HeaderViewHolder {
        TextView date;
    }
}
