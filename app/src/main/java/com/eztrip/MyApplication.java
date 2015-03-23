package com.eztrip;

/**
 * Created by liuxiaoran on 2015/2/28.
 */

import android.app.Application;
import android.content.Context;

import com.thinkland.sdk.android.SDKInitializer;

public class MyApplication extends Application {

    private static MyApplication mInstance;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        if (mInstance == null)
            mInstance = this;

        //juhe 数据初始化
        SDKInitializer.initialize(getApplicationContext());

        //baidu 地图初始化
        com.baidu.mapapi.SDKInitializer.initialize(getApplicationContext());


    }


}