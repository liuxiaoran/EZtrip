package com.eztrip.main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
public class RecommendRouteDetailFragment extends Fragment {

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

        private static final int TYPE_1 = 1;
        private static final int TYPE_2 = 2;

        public MyAdapter(Context context) {
            inflater = LayoutInflater.from(context);

        }

        @Override
        public int getItemViewType(int position) {
            if (position % 2 == 0) {
                return TYPE_1;
            } else {
                return TYPE_2;
            }

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

            int type = getItemViewType(position);
            TrafficViewHolder trafficViewHolder = null;
            ViewHolder holder = null;
            Log.v(TAG, "" + position);
            if (convertView == null) {

                // 按当前所需的样式，确定new的布局
                switch (type) {
                    case TYPE_1:
                        holder = new ViewHolder();
                        convertView = inflater.inflate(R.layout.list_item_layout, parent, false);
                        holder.sceneryNameTv = (TextView) convertView.findViewById(R.id.main_listview_item_scenery_name_tv);
                        holder.descriptionTv = (TextView) convertView.findViewById(R.id.main_listview_item_scenery_introduction_tv);
                        convertView.setTag(holder);
                        break;
                    case TYPE_2:
                        trafficViewHolder = new TrafficViewHolder();
                        convertView = inflater.inflate(R.layout.list_traffic_item_layout, parent, false);
                        trafficViewHolder.distanceTv = (TextView) convertView.findViewById(R.id.main_fragment_recommend_distance_tv);
                        trafficViewHolder.navigationTv = (TextView) convertView.findViewById(R.id.main_fragment_recommend_navigation_tv);
                        convertView.setTag(trafficViewHolder);
                        break;

                }



            } else {
                switch (type) {
                    case TYPE_1:
                        holder = (ViewHolder) convertView.getTag();
                        break;
                    case TYPE_2:
                        trafficViewHolder = (TrafficViewHolder) convertView.getTag();
                        break;
                }

            }

            // 设置资源
            switch (type) {
                case TYPE_1:
                    RouteData.SingleEvent event = (RouteData.SingleEvent) routeList.get(position);
                    holder.sceneryNameTv.setText(event.title);
                    holder.descriptionTv.setText(event.detail);
                    break;
                case TYPE_2:
                    trafficViewHolder.distanceTv.setText("1");
                    trafficViewHolder.navigationTv.setText("2");
                    break;
            }


            return convertView;
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {
            HeaderViewHolder holder;
            if (convertView == null) {
                holder = new HeaderViewHolder();
                convertView = inflater.inflate(R.layout.main_recomemnd_route_detail_header, parent, false);
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


        /**
         * holder 中的成员是布局中的控件引用
         */
        class HeaderViewHolder {
            TextView dayTv;
        }

        class ViewHolder {
            TextView sceneryNameTv;
            TextView descriptionTv;
        }

        class TrafficViewHolder {
            TextView distanceTv;
            TextView navigationTv;
        }

    }

    private void setShowFragment() {
        ((MainActivity) getActivity()).setShownFragment(this);
    }

    public boolean fragmentBackPress() {
//        getFragmentManager().beginTransaction().hide(this).commit();
////        FragmentManager manager = getFragmentManager();
////        if (manager.getBackStackEntryCount() > 0) {
////            getFragmentManager().popBackStackImmediate();
////        }
        getFragmentManager().popBackStack();
        return true;
    }
}
