package com.eztrip.findspot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.eztrip.MyContext;
import com.eztrip.R;
import com.eztrip.model.RouteData;
import com.eztrip.model.ScenerySpot;
import com.eztrip.model.TravelBag;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Picasso;
import com.thinkland.sdk.android.DataCallBack;
import com.thinkland.sdk.android.JuheData;
import com.thinkland.sdk.android.Parameters;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import utils.APIConstants;
import utils.EztripHttpUtil;
import utils.FindSpotService;
import utils.URLConstants;

/**
 * Created by Steve on 2015/3/25.
 */
public class ShowHotel extends ActionBarActivity implements View.OnClickListener {
    private ImageView sceneryIv;
    private TextView titleTv, satisfactionTv, gradeTv, addressTv, introTv;
    private RelativeLayout replaceHotelLayout;
    private Button addBtn, lookBtn;
    private int mScreenWidth;
    private Toolbar mToolbar;
    private RouteData.Hotel hotel;
    public final int REPLACE_HOTEL = 1;
    private String source;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showhotel);

        initView();
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        mScreenWidth = metric.widthPixels; // 屏幕宽度（像素）

        Intent intent = getIntent();
        hotel = (RouteData.Hotel) intent.getSerializableExtra("hotel");
        source = intent.getStringExtra("source");
            fillViewsContent(hotel);


    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("宾馆信息");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Only show items in the action bar relevant to this screen
        // if the drawer is not showing. Otherwise, let the drawer
        // decide what to show in the action bar.
        getMenuInflater().inflate(R.menu.main, menu);
        restoreActionBar();


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //实现android.R.home键的功能
        if (item.getItemId() == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(item);

    }

    private void initView() {

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        sceneryIv = (ImageView) findViewById(R.id.showscenery_scenery_iv);
        titleTv = (TextView) findViewById(R.id.showscenery_title_tv);
        satisfactionTv = (TextView) findViewById(R.id.showscenery_satisfaction_tv);
        gradeTv = (TextView) findViewById(R.id.showscenery_grade_tv);
        addressTv = (TextView) findViewById(R.id.showscenery_address_tv);
        introTv = (TextView) findViewById(R.id.showscenery_intro_tv);
        addBtn = (Button) findViewById(R.id.showscenery_add_btn);
        lookBtn = (Button) findViewById(R.id.showscenery_look_btn);
        replaceHotelLayout = (RelativeLayout)findViewById(R.id.layout_replace);
        addBtn.setOnClickListener(this);
        lookBtn.setOnClickListener(this);

    }

    private void fillViewsContent(RouteData.Hotel hotel) {

        //修改控件值
        Picasso.with(ShowHotel.this).load(hotel.imgsrc).resize(mScreenWidth - 6, 200).error(R.drawable.main_foreground).placeholder(R.drawable.main_foreground)
                .into(sceneryIv);
        titleTv.setText(hotel.name);
        satisfactionTv.setText("满意度：" + hotel.satisfaction);
        gradeTv.setText("星级: " + hotel.grade);
        addressTv.setText("地址: " + hotel.address);
        introTv.setText("介绍: " + hotel.intro);
        addBtn.setTag(hotel);
        lookBtn.setTag(hotel.url);
        if(!source.equals("change"))
            replaceHotelLayout.setVisibility(View.GONE);

    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.showscenery_add_btn) {
            //
            RouteData.hotelInfo = new RouteData.Hotel(hotel);
            for(int i = 0; i < RouteData.spotTempInfo.size(); i++) {
                if(RouteData.spotTempInfo.get(i).type.equals(RouteData.ActivityType.ACCOMMODATION)) {
                    RouteData.spotTempInfo.get(i).setSpotTemp(RouteData.ActivityType.ACCOMMODATION, i, hotel.name, 0, hotel.address);
                }
            }
            Toast.makeText(ShowHotel.this,"当前宾馆信息已改变",Toast.LENGTH_LONG).show();

        } else if (view.getId() == R.id.showscenery_look_btn) {
            // lookBtn
            Intent intent = new Intent(this, SceneryWebView.class);
            if(view.getTag().toString() != null) {
                intent.putExtra("url", view.getTag().toString());
                startActivity(intent);
            } else {
                Toast.makeText(ShowHotel.this,"获取详细信息失败",Toast.LENGTH_LONG).show();
            }
        }
    }
}
