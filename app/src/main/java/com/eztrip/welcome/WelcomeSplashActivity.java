package com.eztrip.welcome;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.eztrip.MainActivity;
import com.eztrip.MyContext;
import com.eztrip.R;

import utils.FindSpotService;

/**
 * Created by liuxiaoran on 15/3/24.
 */
public class WelcomeSplashActivity extends Activity implements Animation.AnimationListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        if (checkIsFirstIn()) {
            setContentView(R.layout.welcome_splash_activity);
            //是第一次进入系统
            //第一次进入系统
            //将城市列表写入数据库
            FindSpotService.getCityListAndWriteToDB(this);
            MyContext.newInstance(getApplicationContext()).getSharedPreferences().edit().putBoolean("firstin", false).commit();

        } else {

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            this.finish();
        }


    }

    public boolean checkIsFirstIn() {
        return MyContext.newInstance(getApplicationContext()).getSharedPreferences().getBoolean("firstin", true);


    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        //动画结束时结束欢迎界面并转到软件的主界面
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        this.finish();
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //在欢迎界面屏蔽BACK键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return false;
    }
}
