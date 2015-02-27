package com.eztrip.findspot;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.eztrip.R;

/**
 * Created by liuxiaoran on 2015/2/25.
 * 处理一个单独的景点的展示
 */
public class SearchResultActivity extends ActionBarActivity {

    private static String TAG = "SearchResultActivity";
    private ImageView sceneryIv;
    private TextView titleTv, commTv, priceTv, gradeTv, addressTv, introTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.findspot_activity_showspot);

        initView();

        //处理search intent
        handleIntent(getIntent());
    }


    private void handleIntent(Intent intent) {
        String query = intent.getStringExtra(SearchManager.QUERY);
        //use the query to search
        Log.v(TAG, query);
    }

    private void initView() {
        sceneryIv = (ImageView) findViewById(R.id.showscenery_scenery_iv);
        titleTv = (TextView) findViewById(R.id.showscenery_title_tv);
        commTv = (TextView) findViewById(R.id.showscenery_comm_tv);
        priceTv = (TextView) findViewById(R.id.showscenery_price_tv);
        gradeTv = (TextView) findViewById(R.id.showscenery_grade_tv);
        addressTv = (TextView) findViewById(R.id.showscenery_address_tv);
        introTv = (TextView) findViewById(R.id.showscenery_intro_tv);
    }

    private void getSpotByInternet() {


    }

}
