package com.eztrip.findspot;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.eztrip.R;
import com.eztrip.model.ScenerySpot;
import com.eztrip.model.TravelBag;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

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
    private Button addBtn, lookBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.findspot_activity_showspot);

        initView();

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


    private void handleIntent(String query) {

        //use the query to search
        getSpotFromInternet(query);


    }

    private void initView() {
        sceneryIv = (ImageView) findViewById(R.id.showscenery_scenery_iv);
        titleTv = (TextView) findViewById(R.id.showscenery_title_tv);
        commTv = (TextView) findViewById(R.id.showscenery_comm_tv);
        priceTv = (TextView) findViewById(R.id.showscenery_price_tv);
        gradeTv = (TextView) findViewById(R.id.showscenery_grade_tv);
        addressTv = (TextView) findViewById(R.id.showscenery_address_tv);
        introTv = (TextView) findViewById(R.id.showscenery_intro_tv);
        addBtn = (Button) findViewById(R.id.showscenery_add_btn);
        lookBtn = (Button) findViewById(R.id.showscenery_look_btn);
        addBtn.setOnClickListener(this);
        lookBtn.setOnClickListener(this);


    }

    private void fillViewsContent(ScenerySpot targetSpot) {

        //修改控件值
        Picasso.with(ShowScenerySpot.this).load(targetSpot.getImgurl()).into(sceneryIv);
        titleTv.setText(targetSpot.getTitle());
        commTv.setText("去过：" + targetSpot.getComm_cnt() + "人");
        priceTv.setText("价格：" + targetSpot.getPrice_min() + "RMB");
        gradeTv.setText("等级: " + targetSpot.getGrade());
        addressTv.setText("地址: " + targetSpot.getAddress());
        introTv.setText("介绍: " + targetSpot.getIntro());
        addBtn.setTag(targetSpot);
        lookBtn.setTag(targetSpot.getUrl());
    }

    private void getSpotFromInternet(String title) {

        String url = URLConstants.SCENERY_LIST + "&cityId=" + "1_1" + "&title=" + title;

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                JSONObject object = null;
                try {
                    object = new JSONObject(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                targetSpot = FindSpotService.getSpot(object);
                fillViewsContent(targetSpot);


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.showscenery_add_btn) {
            //加入行囊
            TravelBag bag = TravelBag.getDefaultTravelBag();
            bag.addScenery((ScenerySpot) view.getTag());

        } else {
            // lookBtn
            Intent intent = new Intent(this, SceneryWebView.class);
            intent.putExtra("url", view.getTag().toString());
            startActivity(intent);
        }
    }
}
