package com.eztrip;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.BDNotifyListener;//假如用到位置提醒功能，需要import该类

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import utils.BaiduLocationHelper.LocationHelper;

/**
 * Created by liuxiaoran on 2015/2/27.
 * 首页
 */
public class MainFragment extends Fragment {

    public static Context context;

    public final static String TAG = "MainFragment";
    private MyLocationListener locationListener;

    public static MainFragment newInstance(Context context) {
        MainFragment.context = context;
        return new MainFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.main_fragment_home, null);

        locationListener = new MyLocationListener();

        LocationHelper.registerHelper(context, locationListener);

        return view;
    }

    class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation == null)
                return;
            else {
                Log.v(TAG, bdLocation.getProvince()); //获取省份信息
                Log.v(TAG, bdLocation.getCity());     //获取城市信息
                Log.v(TAG, bdLocation.getDistrict()); //获取区县信息

            }
        }
    }
}
