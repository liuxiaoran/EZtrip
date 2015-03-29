package com.eztrip.findspot;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
    public ArrayList<RouteData.Hotel> hotelList = new ArrayList<>();
    private Toolbar mToolbar;
    private final String[] levelData = {"5", "4", "3", "2", "1"};
    private Spinner levelSpinner;
    private String level;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_list);
        initView();
        RouteMakerService.getHotels(progressBar,adapter,hotelList,level);
    }

    private void initView() {

        mToolbar = (Toolbar) findViewById(R.id.hotel_list_toolbar);
        setSupportActionBar(mToolbar);
        level = levelData[0];
        adapter = new Adapter();
        progressBar = (ProgressBar)findViewById(R.id.show_hotel_level_progressbar);
        progressBar.setVisibility(View.VISIBLE);
        listView = (ListView)findViewById(R.id.show_hotel_listview);
        listView.setAdapter(adapter);
        levelSpinner = (Spinner)findViewById(R.id.hotel_list_level_spn);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(HotelList.this, android.R.layout.simple_spinner_item, levelData);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        levelSpinner.setAdapter(spinnerAdapter);
        levelSpinner.setPrompt("请选择宾馆星级：");

        levelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                level = levelData[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void searchHotel(View v) {
        progressBar.setVisibility(View.VISIBLE);
        RouteMakerService.getHotels(progressBar,adapter,hotelList,level);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("宾馆列表");
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
            return hotelList.size();
        }

        @Override
        public Object getItem(int position) {
            return hotelList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if(convertView == null)
                convertView = LayoutInflater.from(HotelList.this).inflate(R.layout.hotel_list_item,null);
            TextView title,satisfaction,address;
            ImageView image;
            title = (TextView)convertView.findViewById(R.id.card_hotel_titletv);
            satisfaction = (TextView)convertView.findViewById(R.id.card_hotel_satisfactiontv);
            address = (TextView)convertView.findViewById(R.id.card_hotel_positiontv);
            image = (ImageView)convertView.findViewById(R.id.card_hotel_iv);
            title.setText(hotelList.get(position).name);
            satisfaction.setText("特色：" + hotelList.get(position).satisfaction);
            address.setText("地址：" + hotelList.get(position).address);
            Log.e("position",Integer.toString(position) + hotelList.get(position).address);
            Picasso.with(HotelList.this).load(hotelList.get(position).imgsrc).placeholder(R.drawable.main_foreground)
                    .error(R.drawable.image_error).into(image);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HotelList.this,ShowHotel.class);
                    Bundle b = new Bundle();
                    b.putSerializable("hotel",hotelList.get(position));
                    b.putString("source","change");
                    intent.putExtras(b);
                    startActivity(intent);
                }
            });
            return convertView;
        }
    }
}