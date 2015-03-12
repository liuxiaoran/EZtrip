package com.eztrip;

/**
 * Created by liuxiaoran on 2015/2/28.
 */

import com.thinkland.sdk.android.SDKInitializer;

import android.app.Application;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        SDKInitializer.initialize(getApplicationContext());
    }


}