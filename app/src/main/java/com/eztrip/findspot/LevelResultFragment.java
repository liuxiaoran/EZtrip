package com.eztrip.findspot;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.eztrip.R;
import com.eztrip.model.ScenerySpot;
import com.eztrip.model.TravelBag;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;

import utils.FindSpotService;

/**
 * Created by liuxiaoran on 2015/2/26.
 */
public class LevelResultFragment extends Fragment implements View.OnClickListener {

    private String level;
    public ArrayList<ScenerySpot> scenerySpotArrayList = new ArrayList<>();

    public ProgressBar progressBar;
    public static final String TAG = "LevelResultFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        level = getArguments().getString("level");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        MyAdapter adapter = new MyAdapter();
        View view = inflater.inflate(R.layout.findspot_fragment_levelresult, null);
        progressBar = (ProgressBar) view.findViewById(R.id.findspot_level_progressbar);
        progressBar.setVisibility(View.VISIBLE);

        FindSpotService.getScenerySpotsByLevel(scenerySpotArrayList, level, adapter, progressBar);


        ListView listView = (ListView) view.findViewById(R.id.findspot_listview);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v(TAG, "  " + position);
                Intent intent = new Intent(getActivity(), ShowScenerySpot.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("spot", (ScenerySpot) view.getTag());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });


        return view;
    }

    public class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return scenerySpotArrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.findspot_cardview_sceneryspotitem, null);
            }

            TextView titleTv, priceTv, commTv, positionTv;
            ImageView sceneryIv;
            Button addBtn, lookBtn;
            titleTv = (TextView) convertView.findViewById(R.id.card_titletv);
            priceTv = (TextView) convertView.findViewById(R.id.card_pricetv);
            commTv = (TextView) convertView.findViewById(R.id.card_commtv);
            positionTv = (TextView) convertView.findViewById(R.id.card_positontv);
            sceneryIv = (ImageView) convertView.findViewById(R.id.card_iv);
            addBtn = (Button) convertView.findViewById(R.id.card_add_btn);
            lookBtn = (Button) convertView.findViewById(R.id.card_look_btn);

            titleTv.setText(scenerySpotArrayList.get(position).getTitle());
            priceTv.setText("价格：" + scenerySpotArrayList.get(position).getPrice_min());
            commTv.setText("评论数：" + scenerySpotArrayList.get(position).getComm_cnt());
            positionTv.setText("地址：" + scenerySpotArrayList.get(position).getAddress());

            Picasso.with(getActivity()).load(scenerySpotArrayList.get(position).getImgurl()).placeholder(R.drawable.main_foreground)
                    .error(R.drawable.image_error).into(sceneryIv);
            lookBtn.setTag(scenerySpotArrayList.get(position));
            addBtn.setOnClickListener(LevelResultFragment.this);
            lookBtn.setOnClickListener(LevelResultFragment.this);
            final int ppositon = position;
            convertView.setTag(scenerySpotArrayList.get(position));

            //给convertView 设置点击事件，因为listview的子view中有checkbox和button,所以listview不能获取点击事件
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), ShowScenerySpot.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("spot", scenerySpotArrayList.get(ppositon));
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });


            return convertView;
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.card_add_btn) {
            //  加入行囊
            TravelBag bag = TravelBag.getInstance();
            bag.addScenery((ScenerySpot) view.getTag());
            Toast.makeText(getActivity(), "加入成功", Toast.LENGTH_LONG).show();

        } else if (view.getId() == R.id.card_look_btn) {
            Intent intent = new Intent(getActivity(), ShowScenerySpot.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("spot", (ScenerySpot) view.getTag());
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }


}