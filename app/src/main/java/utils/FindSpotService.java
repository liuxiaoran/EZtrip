package utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;

import com.eztrip.MainActivity;
import com.eztrip.R;
import com.eztrip.database.CityDao;
import com.eztrip.findspot.FindSpotMainFragment;
import com.eztrip.model.City;
import com.eztrip.model.ScenerySpot;
import com.thinkland.sdk.android.DataCallBack;
import com.thinkland.sdk.android.JuheData;
import com.thinkland.sdk.android.Parameters;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

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
//    public static void getScenerySpotsByLevel(final ArrayList<ScenerySpot> arrayList, String level, final RecyclerView.Adapter adapter) {
//        //现在是北京市的
//        final String url = URLConstants.SCENERY_LIST + "&cityId=" + "1_1" + "&grade=" + level;
//
//        AsyncHttpClient client = new AsyncHttpClient();
//        client.get(url, new AsyncHttpResponseHandler() {
//
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//
//                String result = new String(responseBody);
//                Log.v(TAG, "success" + result);
//                try {
//                    JSONObject object = new JSONObject(result);
//                    addSpotIntoList(arrayList, object);
//                    adapter.notifyDataSetChanged();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                Log.v(TAG, "failed" + error);
//            }
//
//        });
//
//    }
    public static void getScenerySpotsByLevel(final ArrayList<ScenerySpot> arrayList, String level, final BaseAdapter adapter, final ProgressBar progressBar) {
        Parameters params = new Parameters();
        params.add("pname", APIConstants.PACKAGE_NAME);
        params.add("v", "1");
        params.add("cityId", "1_1");
        params.add("grade", level);
        JuheData.executeWithAPI(APIConstants.ID, APIConstants.SCENERYLIST_IP, JuheData.GET, params, new DataCallBack() {
            @Override
            public void resultLoaded(int err, String reason, String result) {
                if (err == 0) {
                    try {
                        JSONObject object = new JSONObject(result);
                        addSpotIntoList(arrayList, object);
                        Log.v("test", arrayList.size() + "  size");
                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.INVISIBLE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.v("failed", "failed" + result + "  " + err);
                }
            }
        });
    }

    //通过查询框查询之后 得到结果
    public static void getSearchSceneryList(final ArrayList<ScenerySpot> arrayList, String query, final FindSpotMainFragment fragment) {
        Parameters params = new Parameters();
        params.add("pname", APIConstants.PACKAGE_NAME);
        params.add("v", "1");
        params.add("title", query);
        JuheData.executeWithAPI(APIConstants.ID, APIConstants.SCENERYLIST_IP, JuheData.GET, params, new DataCallBack() {
            @Override
            public void resultLoaded(int error, String reason, String result) {
                if (error == 0) {
                    try {
                        JSONObject object = new JSONObject(result);
                        addSpotIntoList(arrayList, object);
                        fragment.popQueryListDialog();


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.v(TAG, "failed" + result);
                }
            }
        });


    }

    /**
     * 获得城市列表,并将城市列表写入数据库
     */
    public static void getCityListAndWriteToDB(final Activity activity) {

        final GB2Alpha gb2Alpha = new GB2Alpha();
        Parameters parameters = new Parameters();
        parameters.add("pname", APIConstants.PACKAGE_NAME);
        parameters.add("v", 1);
        JuheData.executeWithAPI(APIConstants.ID, APIConstants.AREALIST_IP, JuheData.GET, parameters, new DataCallBack() {
            @Override
            public void resultLoaded(int error, String reason, String result) {
                if (error == 0) {

                    try {
                        JSONObject object = new JSONObject(result);
                        JSONObject resultObject = object.getJSONObject("result");
                        JSONArray areaList = resultObject.getJSONArray("areaList");
                        for (int i = 0; i < areaList.length(); i++) {
                            JSONObject areaObject = areaList.getJSONObject(i);
                            JSONArray nameList = areaObject.getJSONArray("name");
                            String name = nameList.get(0).toString();
                            int level = areaObject.getInt("level");
                            String cid = areaObject.getString("id");
                            String fid = areaObject.getString("fid");
                            String firstName = gb2Alpha.String2Alpha(name).charAt(0) + "";
                            //有些复杂的字取得的一个字是0，将这些城市不加入数据库中
                            if (!firstName.equals("0")) {
                                City city = new City(name, cid, fid, level, firstName);
                                CityDao cityDao = new CityDao(activity);
                                cityDao.addCity(city);
                            }


                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Intent i = new Intent(activity, MainActivity.class);
                    activity.startActivity(i);
                    activity.finish();
                    activity.overridePendingTransition(R.anim.welcome_anim_begin, R.anim.welcome_anim_end);

                }
            }
        });


    }

    public static void addSpotIntoList(ArrayList<ScenerySpot> arrayList, JSONObject object) {
        JSONObject scenery = null;
        try {
            scenery = object.getJSONObject("result");
            JSONArray sceneryList = scenery.getJSONArray("sceneryList");
            for (int i = 0; i < sceneryList.length(); i++) {
                JSONObject oj = sceneryList.getJSONObject(i);
                arrayList.add(new ScenerySpot(oj.getString("title"), oj.getString("price_min"), oj.getString("comm_cnt"), oj.getString("url"),
                        oj.getString("imgurl"), oj.getString("intro"), oj.getString("address")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public static ScenerySpot getSpot(JSONObject object) {
        ScenerySpot scenerySpot = null;
        JSONObject scenery = null;
        try {
            scenery = object.getJSONObject("result");
            JSONArray sceneryList = scenery.getJSONArray("sceneryList");

            JSONObject oj = sceneryList.getJSONObject(0);  // 找列表的第一个
            scenerySpot = new ScenerySpot(oj.getString("title"), oj.getString("price_min"), oj.getString("comm_cnt"), oj.getString("url"),
                    oj.getString("imgurl"), oj.getString("intro"), oj.getString("address"), oj.getString("grade"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return scenerySpot;
    }


}
