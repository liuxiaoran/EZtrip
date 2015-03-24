package com.eztrip.usercenter;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.eztrip.R;

/**
 * Created by liuxiaoran on 15/3/24.
 */
public class UserCenter extends ActionBarActivity {

    private ListView mainListView;

    private String[] listViewContent = {"我的行囊", "收藏的景点", "设置"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.usercenter_main_activity);
        mainListView = (ListView) findViewById(R.id.usercenter_main_lv);
        mainListView.setAdapter(new ListViewAdapter());
        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

    }

    class ListViewAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return listViewContent.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.usercenter_listview_item, null);
            }
            TextView textView = (TextView) convertView.findViewById(R.id.usercenter_listview_item_tv);
            textView.setText(listViewContent[position]);
            return convertView;
        }
    }
}
