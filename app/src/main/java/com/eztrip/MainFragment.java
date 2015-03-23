package com.eztrip;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.BDNotifyListener;//假如用到位置提醒功能，需要import该类
import com.eztrip.citylist.CityList;
import com.eztrip.model.Clock;
import com.eztrip.model.RouteData;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import utils.BaiduLocationHelper.LocationHelper;

/**
 * Created by liuxiaoran on 2015/2/27.
 * 首页
 */
public class MainFragment extends Fragment {

    public static Context context;

    private final static int REQUEST_SELECT_CITY_CODE = 1;

    private LinearLayout recommandLayout;

    public final static String TAG = "MainFragment";
    private MyLocationListener locationListener;

    private TextView start_tv, destination_tv;

    public static MainFragment newInstance(Context context) {
        MainFragment.context = context;
        return new MainFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.main_fragment_home, null);

        start_tv = (TextView) view.findViewById(R.id.start_place_tv);
        destination_tv = (TextView) view.findViewById(R.id.destination_tv);

        destination_tv.setOnClickListener(new CickListener());


        locationListener = new MyLocationListener();
        LocationHelper.registerHelper(context, locationListener);

        // 创建下面的几条推荐路线
        recommandLayout = (LinearLayout) view.findViewById(R.id.recommand_route_linearlayout);
        View view1 = inflater.inflate(R.layout.main_fragment_recommand_layout, null);
        ImageView imageView1 = (ImageView) view1.findViewById(R.id.main_fragment_recommand_iv);
        imageView1.setImageResource(R.drawable.main_fragment_recommand_image1);
        TextView textView1 = (TextView) view1.findViewById(R.id.main_fragment_recommand_tv);
        textView1.setText("北京");
        view1.setTag("1");
        view1.setOnClickListener(new CickListener());
        recommandLayout.addView(view1);




        return view;
    }

    class MyLocationListener implements BDLocationListener {


        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

            if (bdLocation == null)
                return;
            else {

                start_tv.setText(start_tv.getText() + bdLocation.getCity());

                MyContext.startCity = bdLocation.getCity();
//                Log.v(TAG, bdLocation.getProvince()); //获取省份信息
//                Log.v(TAG, bdLocation.getCity());     //获取城市信息
//                Log.v(TAG, bdLocation.getDistrict()); //获取区县信息



            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 1) {

            String city = data.getStringExtra("city");
            Log.v(TAG, city);
            destination_tv.setText(destination_tv.getText().toString().substring(0, 2) + " " + city);
            //在MyContext中存储目的城市
            MyContext.destinationCity = city;

        }

    }

    class CickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.start_place_tv) {

            } else if (v.getId() == R.id.destination_tv) {
                Intent intent = new Intent(getActivity(), CityList.class);
                startActivityForResult(intent, REQUEST_SELECT_CITY_CODE);
            } else if (((String) v.getTag()).equals("1")) {
                RouteData.SingleEvent singleEvent1 = new RouteData.SingleEvent(1, RouteData.ActivityType.SPOT, new Clock(9, 0), new Clock(11, 0), "天安门");
                RouteData.SingleEvent singleEvent2 = new RouteData.SingleEvent(1, RouteData.ActivityType.SPOT, new Clock(9, 0), new Clock(11, 0), "毛主席纪念堂");
                RouteData.SingleEvent singleEvent3 = new RouteData.SingleEvent(1, RouteData.ActivityType.SPOT, new Clock(9, 0), new Clock(11, 0), "故宫");
                RouteData.SingleEvent singleEvent4 = new RouteData.SingleEvent(1, RouteData.ActivityType.SPOT, new Clock(9, 0), new Clock(11, 0), "北海公园");

                RouteData.SingleEvent singleEvent5 = new RouteData.SingleEvent(2, RouteData.ActivityType.SPOT, new Clock(9, 0), new Clock(11, 0), "王府井");
                RouteData.SingleEvent singleEvent6 = new RouteData.SingleEvent(2, RouteData.ActivityType.SPOT, new Clock(9, 0), new Clock(11, 0), "颐和园");
                RouteData.SingleEvent singleEvent7 = new RouteData.SingleEvent(2, RouteData.ActivityType.SPOT, new Clock(9, 0), new Clock(11, 0), "时光的记忆");
                RouteData.SingleEvent singleEvent8 = new RouteData.SingleEvent(2, RouteData.ActivityType.SPOT, new Clock(9, 0), new Clock(11, 0), "老北京庙会自由活动");


                RouteData.SingleEvent singleEvent9 = new RouteData.SingleEvent(3, RouteData.ActivityType.SPOT, new Clock(9, 0), new Clock(11, 0), "天坛");
                RouteData.SingleEvent singleEvent10 = new RouteData.SingleEvent(3, RouteData.ActivityType.SPOT, new Clock(9, 0), new Clock(11, 0), "北京前门大街");


            } else if (v.getTag().equals("2")) {


            }
        }
    }


}
