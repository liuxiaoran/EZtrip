package com.eztrip.findspot;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.eztrip.R;
import com.eztrip.model.ScenerySpot;

import java.util.ArrayList;

/**
 * Created by liuxiaoran on 15/3/9.
 */
public class QueryListDialogAdapter extends BaseAdapter implements View.OnClickListener {

    private Context context;
    private ArrayList<ScenerySpot> spotList;

    public QueryListDialogAdapter(Context context, ArrayList spotList) {
        this.context = context;
        this.spotList = spotList;
    }

    @Override
    public int getCount() {
        return spotList.size();
    }

    @Override
    public Object getItem(int position) {
        return spotList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = View.inflate(context, R.layout.findspot_dialog_queryresult_item, null);
        ((TextView) convertView.findViewById(R.id.query_dialog_item_tv)).setText(spotList.get(position).getTitle());
        ImageView imageView = (ImageView) convertView.findViewById(R.id.query_dialog_item_iv);
        imageView.setTag(position);
        imageView.setOnClickListener(this);
        convertView.setOnClickListener(this);
        return convertView;

    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(context, ShowScenerySpot.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("spot", spotList.get((int) v.getTag()));
        intent.putExtras(bundle);
        context.startActivity(intent);

    }
}
