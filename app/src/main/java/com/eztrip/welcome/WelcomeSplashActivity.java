package com.eztrip.welcome;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Window;
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
public class WelcomeSplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        if (checkIsFirstIn()) {

            //第一次进入系统
            setContentView(R.layout.welcome_splash_activity);
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //在欢迎界面屏蔽BACK键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return false;
    }
}
