package com.eztrip.main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.eztrip.MainActivity;
import com.eztrip.R;
import com.eztrip.model.RouteData;

import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by liuxiaoran on 15/3/26.
 */
public class RecommandRouteDetailFragment extends Fragment {

    /**
     * 首页传递过来的路线的list
     */
    private ArrayList<RouteData.SingleEvent> routeList;
    private static final String TAG = "recommand";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setShowFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.main_activity_recommand_route_detail, null);

        Bundle bundle = getArguments();
        routeList = (ArrayList<RouteData.SingleEvent>) bundle.getSerializable("recommand_route");
        Log.v(TAG, routeList.size() + "");
        StickyListHeadersListView stickyList = (StickyListHeadersListView) view.findViewById(R.id.main_recommand_route_detail_lv);
        MyAdapter adapter = new MyAdapter(getActivity());
        stickyList.setAdapter(adapter);
        return view;
    }

    /**
     * stickylistheader  adpater
     */
    public class MyAdapter extends BaseAdapter implements StickyListHeadersAdapter {

        private LayoutInflater inflater;

        public MyAdapter(Context context) {
            inflater = LayoutInflater.from(context);

        }

        @Override
        public int getCount() {
            return routeList.size();
        }

        @Override
        public Object getItem(int position) {
            return routeList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.list_item_layout, parent, false);
                holder.sceneryNameTv = (TextView) convertView.findViewById(R.id.main_listview_item_scenery_name_tv);
                holder.descriptionTv = (TextView) convertView.findViewById(R.id.main_listview_item_scenery_introduction_tv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            RouteData.SingleEvent event = (RouteData.SingleEvent) routeList.get(position);
            holder.sceneryNameTv.setText(event.title);
            holder.descriptionTv.setText(event.detail);


            return convertView;
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {
            HeaderViewHolder holder;
            if (convertView == null) {
                holder = new HeaderViewHolder();
                convertView = inflater.inflate(R.layout.main_recomamnd_route_detail_header, parent, false);
                holder.dayTv = (TextView) convertView.findViewById(R.id.main_recommand_route_detail_tv);
                convertView.setTag(holder);
            } else {
                holder = (HeaderViewHolder) convertView.getTag();
            }

            String headerText = "Day:" + routeList.get(position).day;
            holder.dayTv.setText(headerText);
            return convertView;
        }

        @Override
        public long getHeaderId(int position) {
            //return the first character of the country as ID because this is what headers are based upon
            return routeList.get(position).day;
        }

        class HeaderViewHolder {
            TextView dayTv;
        }

        class ViewHolder {
            TextView sceneryNameTv;
            TextView descriptionTv;
        }

    }

    private void setShowFragment() {
        ((MainActivity) getActivity()).setShownFragment(this);
    }

    public boolean fragmentBackPress() {
//        getFragmentManager().beginTransaction().hide(this).commit();
        FragmentManager manager = getFragmentManager();
        if (manager.getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStackImmediate();
        }
        return true;
    }
}
