package com.eztrip.routemaker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.eztrip.R;
import com.eztrip.model.ScenerySpot;
import com.eztrip.model.TravelBag;
import com.eztrip.routemaker.RouteMakerFragment;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Steve on 2015/2/25.
 * 基本设置中景点列表的adapter
 */
public class BasicSettingsSpotAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<HashMap<String, String>> text;
    private ArrayList<ScenerySpot> scenerySpotArrayList;
    private ListView listView;

    public BasicSettingsSpotAdapter(Context context, ArrayList<HashMap<String, String>> text, ListView listView) {
        this.context = context;
        this.text = text;
        this.listView = listView;
        this.scenerySpotArrayList = TravelBag.getInstance().getScenerySpotList();
    }

    @Override
    public int getCount() {
// TODO Auto-generated method stub
        return text.size();
    }

    @Override
    public Object getItem(int position) {
// TODO Auto-generated method stub
        return text.get(position);
    }

    @Override
    public long getItemId(int position) {
// TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
// TODO Auto-generated method stub
        final int index = position;
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.routemaker_basicsettings_spot, null);
        }
        final TextView textView = (TextView) view
                .findViewById(R.id.routemaker_basicsettings_spotlist_name);
        textView.setText(text.get(position).get("name"));
        textView.setText(scenerySpotArrayList.get(position).title);
        final ImageView imageView = (ImageView) view
                .findViewById(R.id.routemaker_basicsettings_spotlist_namesimple_item_delete);
        imageView.setTag(position);
        imageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                text.remove(index);
                notifyDataSetChanged();
                RouteMakerFragment.adaptListViewHeight(listView, BasicSettingsSpotAdapter.this);
            }
        });
        return view;
    }
}

