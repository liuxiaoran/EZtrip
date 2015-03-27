package com.eztrip.findspot;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.eztrip.R;
import com.eztrip.model.RouteData;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import utils.RouteMakerService;

/**
 * Created by Steve on 2015/3/27.
 */
public class HotelList extends ActionBarActivity{

    private ListView listView;
    private Adapter adapter;
    private ProgressBar progressBar;
    public ArrayList<RouteData.DietTemp> dietList = new ArrayList<>();
    private String latitude, longitude;
    private int period;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_list);
        initView();
        Bundle b=  getIntent().getExtras();
        latitude = b.getString("latitude");
        longitude = b.getString("longitude");
        period = b.getInt("period");
        RouteMakerService.getNearbyRestaurants(period,latitude,longitude,dietList,progressBar,adapter);
    }

    private void initView() {

        mToolbar = (Toolbar) findViewById(R.id.restaurant_list_toolbar);
        setSupportActionBar(mToolbar);
        adapter = new Adapter();
        progressBar = (ProgressBar)findViewById(R.id.show_restaurant_level_progressbar);
        progressBar.setVisibility(View.VISIBLE);
        listView = (ListView)findViewById(R.id.show_restaurant_listview);
        listView.setAdapter(adapter);
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

    class Adapter extends BaseAdapter {

        @Override
        public int getCount() {
            return dietList.size();
        }

        @Override
        public Object getItem(int position) {
            return dietList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if(convertView == null)
                convertView = LayoutInflater.from(HotelList.this).inflate(R.layout.restaurant_list_item,null);
            TextView title,specialty,address;
            ImageView image;
            title = (TextView)convertView.findViewById(R.id.card_restaurant_titletv);
            specialty = (TextView)convertView.findViewById(R.id.card_restaurant_specialtytv);
            address = (TextView)convertView.findViewById(R.id.card_restaurant_positiontv);
            image = (ImageView)convertView.findViewById(R.id.card_restaurant_iv);
            title.setText(dietList.get(position).detail);
            specialty.setText("特色：" + dietList.get(position).recommendDishes);
            address.setText("地址：" + dietList.get(position).address);
            Picasso.with(HotelList.this).load(dietList.get(position).imgsrc).placeholder(R.drawable.main_foreground)
                    .error(R.drawable.image_error).into(image);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HotelList.this,ShowRestaurant.class);
                    Bundle b = new Bundle();
                    b.putSerializable("restaurant",dietList.get(position));
                    b.putString("source","change");
                    b.putInt("index",period);
                    intent.putExtras(b);
                    startActivity(intent);
                }
            });
            return convertView;
        }
    }
}