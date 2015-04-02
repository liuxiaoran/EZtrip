package com.eztrip.findspot;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.eztrip.R;
import com.eztrip.model.RouteData;
import com.squareup.picasso.Picasso;

/**
 * Created by Steve on 2015/3/25.
 */
public class ShowRestaurant extends ActionBarActivity implements View.OnClickListener {
    private ImageView sceneryIv;
    private TextView titleTv, commentTv, addressTv, recommendTV;
    private RelativeLayout replaceRestaurantLayout;
    private Button addBtn;
    private int mScreenWidth;
    private Toolbar mToolbar;
    private RouteData.DietTemp restaurant;
    public static final int REPLACE_HOTEL = 2;
    private String source;
    private int period;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showrestaurant);

        initView();
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        mScreenWidth = metric.widthPixels; // 屏幕宽度（像素）

        Bundle b = getIntent().getExtras();
        restaurant = (RouteData.DietTemp) b.getSerializable("restaurant");
        source = b.getString("source");
        if (source.equals("change"))
            period = b.getInt("period");
        else
            period = -1;
        fillViewsContent(restaurant);
        Log.e("position4", Integer.toString(period));
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("饭店信息");
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

        mToolbar = (Toolbar) findViewById(R.id.show_restaurant_toolbar);
        setSupportActionBar(mToolbar);

        sceneryIv = (ImageView) findViewById(R.id.showscenery_scenery_iv);
        titleTv = (TextView) findViewById(R.id.showscenery_title_tv);
        commentTv = (TextView) findViewById(R.id.show_restaurant_comm_tv);
        addressTv = (TextView) findViewById(R.id.showscenery_contact_tv);
        recommendTV = (TextView) findViewById(R.id.showscenery_specialty_tv);
        addBtn = (Button) findViewById(R.id.showscenery_add_btn);
        replaceRestaurantLayout = (RelativeLayout) findViewById(R.id.layout_replace);
        addBtn.setOnClickListener(this);

    }

    private void fillViewsContent(RouteData.DietTemp restaurant) {

        //修改控件值
        Picasso.with(ShowRestaurant.this).load(restaurant.imgsrc).resize(mScreenWidth - 6, 200).error(R.drawable.main_foreground)
                .into(sceneryIv);
        titleTv.setText(restaurant.detail);
        Log.e("good", Integer.toString(restaurant.goodRemarks));
        Log.e("common", Integer.toString(restaurant.commonRemarks));
        Log.e("bad", Integer.toString(restaurant.badRemarks));
        commentTv.setText("好评：" + restaurant.goodRemarks + "，中评：" + restaurant.commonRemarks + "，差评：" + restaurant.badRemarks);
        addressTv.setText("地址: " + restaurant.address + "\n电话：" + restaurant.phone);
        recommendTV.setText(restaurant.recommendDishes);
        addBtn.setTag(restaurant);
        if (!source.equals("change"))
            replaceRestaurantLayout.setVisibility(View.GONE);

    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.showscenery_add_btn) {
            //
            RouteData.dietTempInfo[period] = new RouteData.DietTemp(restaurant);
            Toast.makeText(ShowRestaurant.this, "当前饭店已改变", Toast.LENGTH_LONG).show();
            Log.e("period" + Integer.toString(period), RouteData.dietTempInfo[period].detail);
            Intent intent = new Intent();
            intent.putExtra("replace_restaurant", true);
            setResult(REPLACE_HOTEL, intent);
            finish();
        }
    }
}
