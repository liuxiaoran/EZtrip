package com.eztrip.findspot;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.eztrip.MyContext;
import com.eztrip.R;
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
 * Created by liuxiaoran on 2015/2/25.
 * 处理一个单独的景点的展示
 */
public class ShowScenerySpot extends ActionBarActivity implements View.OnClickListener {

    private static String TAG = "SearchResultActivity";
    private ImageView sceneryIv;
    private TextView titleTv, commTv, priceTv, gradeTv, addressTv, introTv;
    private ScenerySpot targetSpot;
    private Button addBtn, lookBtn, collectBtn;
    private int mScreenWidth;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.findspot_activity_showspot);

        initView();
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        mScreenWidth = metric.widthPixels; // 屏幕宽度（像素）

        Intent intent = getIntent();
        if (intent.getBooleanExtra("isSearch", false)) {
            //是从查找框进来的
            //处理search intent
            handleIntent(intent.getStringExtra("query"));
        } else {
            ScenerySpot scenerySpot = (ScenerySpot) intent.getSerializableExtra("spot");
            fillViewsContent(scenerySpot);
        }


    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("景点");
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

    private void handleIntent(String query) {

        //use the query to search
        getSpotFromInternet(query);


    }

    private void initView() {

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        sceneryIv = (ImageView) findViewById(R.id.showscenery_scenery_iv);
        titleTv = (TextView) findViewById(R.id.showscenery_title_tv);
        commTv = (TextView) findViewById(R.id.showscenery_comm_tv);
        priceTv = (TextView) findViewById(R.id.showscenery_price_tv);
        gradeTv = (TextView) findViewById(R.id.showscenery_grade_tv);
        addressTv = (TextView) findViewById(R.id.showscenery_address_tv);
        introTv = (TextView) findViewById(R.id.showscenery_intro_tv);
        addBtn = (Button) findViewById(R.id.showscenery_add_btn);
        lookBtn = (Button) findViewById(R.id.showscenery_look_btn);
        collectBtn = (Button) findViewById(R.id.showscenery_collection_btn);
        addBtn.setOnClickListener(this);
        lookBtn.setOnClickListener(this);
        collectBtn.setOnClickListener(this);

    }

    private void fillViewsContent(ScenerySpot targetSpot) {

        //修改控件值
        Picasso.with(ShowScenerySpot.this).load(targetSpot.getImgurl()).resize(mScreenWidth - 6, 200).error(R.drawable.main_foreground).placeholder(R.drawable.main_foreground)
                .into(sceneryIv);
        titleTv.setText(targetSpot.getTitle());
        commTv.setText("去过：" + targetSpot.getComm_cnt() + "人");
        priceTv.setText("价格：" + targetSpot.getPrice_min() + "RMB");
        gradeTv.setText("等级: " + targetSpot.getGrade());
        addressTv.setText("地址: " + targetSpot.getAddress());
        introTv.setText("介绍: " + targetSpot.getIntro());
        addBtn.setTag(targetSpot);
        lookBtn.setTag(targetSpot.getUrl());
        collectBtn.setTag(targetSpot);
    }


    private void getSpotFromInternet(String title) {

        Parameters params = new Parameters();
        params.add("pname", APIConstants.PACKAGE_NAME);
        params.add("v", "1");
        params.add("cityId", "1_1");
        params.add("title", title);
        JuheData.executeWithAPI(APIConstants.ID, APIConstants.SCENERYLIST_IP, JuheData.GET, params, new DataCallBack() {
            @Override
            public void resultLoaded(int err, String reason, String result) {

                if (err == 0) {
                    JSONObject object = null;
                    try {
                        object = new JSONObject(result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    targetSpot = FindSpotService.getSpot(object);
                    fillViewsContent(targetSpot);
                } else {
                    //request error
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.showscenery_add_btn) {
            //加入行囊
            TravelBag bag = TravelBag.getInstance();
            bag.addScenery((ScenerySpot) view.getTag());

        } else if (view.getId() == R.id.showscenery_look_btn) {
            // lookBtn
            Intent intent = new Intent(this, SceneryWebView.class);
            intent.putExtra("url", view.getTag().toString());
            startActivity(intent);
        } else {
            // collection btn
            HashMap hashMap = ((ScenerySpot) view.getTag()).toHashMap();
            hashMap.put("id", MyContext.newInstance(getApplicationContext()).getCurrentUser().getId());
            EztripHttpUtil.post(URLConstants.USER_COLECTION_SPOT, hashMap, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Toast.makeText(getApplicationContext(), "收藏成功", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                }
            });


        }
    }
}
