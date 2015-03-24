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
import com.eztrip.findspot.ShowScenerySpot;
import com.eztrip.map.MapActivity;
import com.eztrip.model.RouteData;
import com.eztrip.model.ScenerySpot;
import com.eztrip.model.TravelBag;

import java.util.Collections;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by Steve on 2015/2/26.
 * 景点设置中的景点列表的adapter
 */
public class SpotSettingsAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private Context context;
    private LayoutInflater inflater;

    public SpotSettingsAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getHeaderView(int i, View view, ViewGroup viewGroup) {
        HeaderViewHolder holder;
        holder = new HeaderViewHolder();
        view = inflater.inflate(R.layout.routemaker_day_header, viewGroup, false);
        holder.date = (TextView) view.findViewById(R.id.day_header_date);
        view.setTag(holder);
        String headerText = new String();
        int day = RouteData.spotTempInfo.get(i).period / 3;
        int dayPeriod = RouteData.spotTempInfo.get(i).period % 3;
        headerText += "第" + (Integer.toString(day + 1)) + "天";
        switch (dayPeriod) {
            case 0:
                headerText += "上午";
                break;
            case 1:
                headerText += "下午";
                break;
            case 2:
                headerText += "晚上";
                break;
            default:
                break;
        }
        holder.date.setText(headerText);
        return view;
    }

    @Override
    public long getHeaderId(int i) {
        return RouteData.spotTempInfo.get(i).period;
    }

    @Override
    public int getCount() {
        return RouteData.spotTempInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return RouteData.spotTempInfo.get(position);
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
        convertView = inflater.inflate(R.layout.routemaker_spot_item, parent, false);
        holder.change = (ImageView) convertView.findViewById(R.id.item_change);
        holder.detail = (TextView) convertView.findViewById(R.id.item_content);
        holder.up = (ImageView) convertView.findViewById(R.id.item_up);
        holder.down = (ImageView) convertView.findViewById(R.id.item_down);
        holder.type = (ImageView) convertView.findViewById(R.id.item_type);
        convertView.setTag(holder);
//        } else {
//            holder = (ViewHolder) convertView.getTag();
//        }
        holder.detail.setText(RouteData.spotTempInfo.get(position).detail);
        if (RouteData.spotTempInfo.get(position).type == RouteData.ActivityType.SPOT) {
            holder.change.setVisibility(View.GONE);
            holder.type.setImageResource(R.drawable.ic_spot);
            holder.up.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position != 0) {
                        moveListItem(position, -1);
                        notifyDataSetChanged();
                    }
                }
            });
            holder.down.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position != RouteData.spotTempInfo.size() - 1) {
                        moveListItem(position, 1);
                        notifyDataSetChanged();
                    }
                }
            });
        } else if (RouteData.spotTempInfo.get(position).type == RouteData.ActivityType.ACCOMMODATION) {
            holder.up.setVisibility(View.GONE);
            holder.down.setVisibility(View.GONE);
            holder.type.setImageResource(R.drawable.ic_accomodation);
            holder.change.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO 跳转到宾馆信息页面
                    Toast.makeText(context, "修改" + RouteData.spotTempInfo.get(position).detail, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            holder.up.setVisibility(View.GONE);
            holder.down.setVisibility(View.GONE);
            holder.change.setVisibility(View.GONE);
            if (RouteData.spotTempPeriodItemCount[RouteData.spotTempInfo.get(position).period] != 1) {
                convertView.setVisibility(View.GONE);
            }
            else
                convertView.setVisibility(View.VISIBLE);
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 跳转到景点信息页面
                Intent intent = new Intent(context, ShowScenerySpot.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("spot", TravelBag.getInstance().getScenerySpotList().get(position));
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
        return convertView;
    }

    private void moveListItem(int position, int direction) {
        if (RouteData.spotTempInfo.get(position).period == RouteData.spotTempInfo.get(position + direction).period && RouteData.spotTempInfo.get(position + direction).detail.equals("无")) {
            swapListItem(position, direction);
            position = position + direction;
        }
        if (position != 0)
            if (RouteData.spotTempInfo.get(position + direction).period != RouteData.spotTempInfo.get(position).period) {
                notifyEmptyStateChanged(RouteData.spotTempInfo.get(position).period, +direction);
                RouteData.spotTempInfo.get(position).period = RouteData.spotTempInfo.get(position + direction).period;
            } else
                swapListItem(position, direction);
    }

    private void swapListItem(int position, int direction) {
        Collections.swap(RouteData.spotTempInfo, position, position + direction);
    }

    private void notifyEmptyStateChanged(int period, int direction) {
//        RouteData.spotTempPeriodItemCount[period]--;
//        RouteData.spotTempPeriodItemCount[period + direction]++;
    }

    class ViewHolder {
        ImageView type;
        ImageView up;
        ImageView down;
        ImageView change;
        TextView detail;
    }

    class HeaderViewHolder {
        TextView date;
    }
}
