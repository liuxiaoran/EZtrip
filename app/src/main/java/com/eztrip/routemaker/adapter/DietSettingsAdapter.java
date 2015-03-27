package com.eztrip.routemaker.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.eztrip.R;
import com.eztrip.findspot.RestaurantList;
import com.eztrip.findspot.ShowRestaurant;
import com.eztrip.model.RouteData;

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
                Intent intent = new Intent();
                Bundle b = new Bundle();
                b.putInt("period",position);
                b.putString("latitude", RouteData.dietTempInfo[position].latitude);
                b.putString("longitude",RouteData.dietTempInfo[position].longitude);
                intent.setClass(context, RestaurantList.class);
                intent.putExtras(b);
                b.putString("source","change");
                context.startActivity(intent);
                Toast.makeText(context, "修改 " + RouteData.dietTempInfo[position].detail, Toast.LENGTH_LONG).show();
            }
        });
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 跳转到餐厅信息页面
//                Toast.makeText(context, "详细信息 " + RouteData.dietTempInfo[position].detail, Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                Bundle b = new Bundle();
                b.putSerializable("restaurant",RouteData.dietTempInfo[position]);
                b.putString("source","see");
                intent.putExtras(b);
                intent.setClass(context, ShowRestaurant.class);
                context.startActivity(intent);
            }
        });
        return convertView;
    }

    class ViewHolder {
        ImageView change;
        TextView detail;
        ImageView delete;
    }

    class HeaderViewHolder {
        TextView date;
    }
}
