package utils;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.eztrip.model.ScenerySpot;
import com.loopj.android.http.*;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.smssdk.gui.DefaultContactViewItem;

/**
 * Created by liuxiaoran on 2015/2/26.
 */
public class FindSpotService {

    public static final String TAG = "FindSpotService";

    /**
     * 通过景点等级返回景点列表
     *
     * @param level
     * @return
     */
    public static void getScenerySpotsByLevel(final ArrayList<ScenerySpot> arrayList, String level, final RecyclerView.Adapter adapter) {
        //现在是北京市的
        final String url = URLConstants.SCENERY_LIST + "&cityId=" + "1_1" + "&grade=" + level;

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                String result = new String(responseBody);
                Log.v(TAG, "success" + result);
                try {
                    JSONObject object = new JSONObject(result);
                    JSONObject scenery = object.getJSONObject("result");
                    Log.v(TAG, "" + scenery);
                    JSONArray sceneryList = scenery.getJSONArray("sceneryList");
                    Log.v(TAG, "" + sceneryList);
                    for (int i = 0; i < sceneryList.length(); i++) {
                        JSONObject oj = sceneryList.getJSONObject(i);
                        Log.v(TAG, "" + oj);
                        arrayList.add(new ScenerySpot(oj.getString("title"), oj.getString("price_min"), oj.getString("comm_cnt"), oj.getString("url"),
                                oj.getString("imgurl"), oj.getString("intro"), oj.getString("address")));
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.v(TAG, "failed" + error);
            }

        });

    }
}
