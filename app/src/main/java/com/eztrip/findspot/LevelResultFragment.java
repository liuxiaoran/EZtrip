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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.eztrip.R;
import com.eztrip.model.ScenerySpot;
import com.eztrip.model.TravelBag;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import utils.FindSpotService;

/**
 * Created by liuxiaoran on 2015/2/26.
 */
public class LevelResultFragment extends Fragment implements View.OnClickListener {

    private String level;
    public ArrayList<ScenerySpot> scenerySpotArrayList = new ArrayList<>();
    public static final String TAG = "LevelResultFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        level = getArguments().getString("level");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        MyAdapter adapter = new MyAdapter();
        FindSpotService.getScenerySpotsByLevel(scenerySpotArrayList, level, adapter);
        // 得到 recyclerview
        View view = inflater.inflate(R.layout.findspot_fragment_levelresult, null);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.card_add_btn) {
            //TODO: 加入行囊
            TravelBag bag = TravelBag.getDefaultTravelBag();
            bag.addScenery((ScenerySpot) view.getTag());

        } else if (view.getId() == R.id.card_look_btn) {
            Intent intent = new Intent(getActivity(), SceneryWebView.class);
            intent.putExtra("url", view.getTag().toString());
            startActivity(intent);
        }
    }

    class MyAdapter extends RecyclerView.Adapter<ViewHolder> {

        ArrayList<ScenerySpot> data;

        public MyAdapter() {
            this.data = LevelResultFragment.this.scenerySpotArrayList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            // 加载Item的布局.布局中用到的真正的CardView.
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.findspot_cardview_sceneryspotitem, viewGroup, false);
            // ViewHolder参数一定要是Item的Root节点.
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            viewHolder.titleTv.setText(data.get(i).getTitle());
            viewHolder.priceTv.setText("价格：" + data.get(i).getPrice_min());
            viewHolder.commTv.setText("评论数：" + data.get(i).getComm_cnt());
            viewHolder.positionTv.setText("地址：" + data.get(i).getAddress());

            Picasso.with(getActivity()).load(data.get(i).getImgurl()).into(viewHolder.sceneryIv);
            viewHolder.lookBtn.setTag(data.get(i).getUrl());
            viewHolder.addBtn.setTag(data.get(i));
            viewHolder.addBtn.setOnClickListener(LevelResultFragment.this);
            viewHolder.lookBtn.setOnClickListener(LevelResultFragment.this);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public TextView titleTv, priceTv, commTv, positionTv;
        public ImageView sceneryIv;
        public Button addBtn, lookBtn;

        public ViewHolder(View itemView) {
            // super这个参数一定要注意,必须为Item的根节点.否则会出现莫名的FC.
            super(itemView);
            titleTv = (TextView) itemView.findViewById(R.id.card_titletv);
            priceTv = (TextView) itemView.findViewById(R.id.card_pricetv);
            commTv = (TextView) itemView.findViewById(R.id.card_commtv);
            positionTv = (TextView) itemView.findViewById(R.id.card_positontv);
            sceneryIv = (ImageView) itemView.findViewById(R.id.card_iv);
            addBtn = (Button) itemView.findViewById(R.id.card_add_btn);
            lookBtn = (Button) itemView.findViewById(R.id.card_look_btn);
        }
    }
}