package utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.eztrip.findspot.RestaurantList;
import com.eztrip.model.RouteData;
import com.eztrip.routemaker.RouteMakerFragment;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.thinkland.sdk.android.DataCallBack;
import com.thinkland.sdk.android.JuheData;
import com.thinkland.sdk.android.Parameters;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by Steve on 2015/3/9.
 */
public class RouteMakerService {
    /**
     * @param city     The name of the city.
     * @param activity The activity which calls this method
     * @return user's collection of spots in that city
     */
    public static List<HashMap<String, String>> getSpotsCollection(String city, final Activity activity) {
        final List<HashMap<String, String>> result = new ArrayList<>();
        SharedPreferences sharedPreferences = activity.getSharedPreferences("user", Activity.MODE_PRIVATE);
        String id = sharedPreferences.getString("id", "0");
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("city", city);
        params.add("id", id);
        client.get(activity, URLConstants.USER_COLECTION_SPOT, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    JSONArray array = new JSONObject(new String(responseBody)).getJSONArray("detail");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = (JSONObject) array.get(i);
                        HashMap<String, String> spot = new HashMap<>();
                        spot.put("name", object.getString("name"));
                        spot.put("address", object.getString("address"));
                        result.add(spot);
                    }
                } catch (JSONException e) {
                    Toast.makeText(activity, "数据出现错误", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(activity, "连接服务器失败，无法得到收藏信息", Toast.LENGTH_SHORT).show();
            }
        });
        return result;
    }

    /**
     * @param spot     the name of the spot
     * @param activity the activity which calls this method
     * @return the preferred visiting time of the spot, the unit is minute
     */

    public static int getVisitTime(String spot, final Activity activity) {
        final int[] time = {0};
        SyncHttpClient client = new SyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("name", spot);
        client.get(activity, URLConstants.SPOT_VISIT_TIME, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    int result = new JSONObject(new String(responseBody)).getInt("time");
                    time[0] = result;
                } catch (JSONException e) {
                    Toast.makeText(activity, "数据出现错误", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Looper.prepare();
                Toast.makeText(activity, "连接服务器失败", Toast.LENGTH_SHORT).show();
            }
        });
        return time[0];
    }

    /**
     * @param content  request content
     * @param activity
     * @return a boolean indicates whether the data is pushed to the server
     */
    public static boolean submitRoute(String content, final Activity activity) {
        final int[] success = {0};
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("content", content);
        client.post(activity, URLConstants.ROUTE_SUBMIT, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                success[0] = 1;
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                success[0] = 0;
                Toast.makeText(activity, "连接服务器失败", Toast.LENGTH_SHORT).show();
            }
        });
        return success[0] == 1;
    }

    public static void getNearbyRestaurants(final int period, String latitude, String longitude,final ArrayList<RouteData.DietTemp> dietList, final ProgressBar progressBar, final BaseAdapter adapter) {
        Parameters parameters = new Parameters();
        parameters.add("lng", longitude);
        parameters.add("lat", latitude);
        parameters.add("radius", 1000);
        JuheData.executeWithAPI(APIConstants.DIET_INFO_ID, APIConstants.DIET_INFO_IP, JuheData.GET, parameters, new DataCallBack() {
            @Override
            public void resultLoaded(int err, String reason, String result) {

                if (err == 0) {
                    try {
                        JSONObject object = new JSONObject(result);
                        JSONArray list = object.getJSONArray("result");
                        for(int i = 0; i < list.length(); i++) {
                            JSONObject restaurant = list.getJSONObject(i);
                            dietList.add(new RouteData.DietTemp(period,
                                    restaurant.getString("name"),
                                    restaurant.getString("latitude"),
                                    restaurant.getString("longitude"),
                                    restaurant.getString("address"),
                                    restaurant.getString("phone"),
                                    restaurant.getString("photos"),
                                    Integer.parseInt(restaurant.getString("very_good_remarks").equals("")?"0":restaurant.getString("very_good_remarks")) + Integer.parseInt(restaurant.getString("good_remarks").equals("")?"0":restaurant.getString("good_remarks")),
                                    Integer.parseInt(restaurant.getString("common_remarks").equals("")?"0":restaurant.getString("common_remarks")),
                                    Integer.parseInt(restaurant.getString("bad_remarks").equals("")?"0":restaurant.getString("bad_remarks")) + Integer.parseInt(restaurant.getString("very_bad_remarks").equals("")?"0":restaurant.getString("very_bad_remarks")),
                                    new StringBuilder(restaurant.getString("recommended_dishes")).append(restaurant.getString("recommended_products").equals("") || restaurant.getString("recommended_dishes").equals("") ? "" : ",").append((restaurant.getString("recommended_products"))).toString()));
                        }
                        progressBar.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Get one restaurant at a place whose latitude and longitude are known
     *
     * @param period
     * @param latitude  the latitude of the spot
     * @param longitude the longitude of the spot
     * @return
     * @see com.eztrip.model.RouteData.SpotTemp.period
     */
    public static void getOneNearbyRestaurant(final int period, String latitude, String longitude, final RouteMakerFragment.MyHandler handler) {
        Parameters parameters = new Parameters();
        parameters.add("lng", longitude);
        parameters.add("lat", latitude);
        parameters.add("radius", 1000);
        JuheData.executeWithAPI(APIConstants.DIET_INFO_ID, APIConstants.DIET_INFO_IP, JuheData.GET, parameters, new DataCallBack() {
            @Override
            public void resultLoaded(int err, String reason, String result) {

                if (err == 0) {
                    try {
                        JSONObject object = new JSONObject(result);
                        Log.e("reason2",object.getString("reason"));
                        JSONArray list = object.getJSONArray("result");
                        JSONObject restaurant = list.getJSONObject(new Random().nextInt(list.length()));
                        RouteData.dietTempInfo[period] = new RouteData.DietTemp(period,
                                restaurant.getString("name"),
                                restaurant.getString("latitude"),
                                restaurant.getString("longitude"),
                                restaurant.getString("address"),
                                restaurant.getString("phone"),
                                restaurant.getString("photos"),
                                Integer.parseInt(restaurant.getString("very_good_remarks").equals("")?"0":restaurant.getString("very_good_remarks")) + Integer.parseInt(restaurant.getString("good_remarks").equals("")?"0":restaurant.getString("good_remarks")),
                                Integer.parseInt(restaurant.getString("common_remarks").equals("")?"0":restaurant.getString("common_remarks")),
                                Integer.parseInt(restaurant.getString("bad_remarks").equals("")?"0":restaurant.getString("bad_remarks")) + Integer.parseInt(restaurant.getString("very_bad_remarks").equals("")?"0":restaurant.getString("very_bad_remarks")),
                                new StringBuilder(restaurant.getString("recommended_dishes")).append(restaurant.getString("recommended_products").equals("") || restaurant.getString("recommended_dishes").equals("") ? "" : ",").append((restaurant.getString("recommended_products"))).toString());
                                Message m = new Message();
                                Bundle b = new Bundle();
                                b.putBoolean("minus", true);
                                b.putString("source", "spot");
                                m.setData(b);
                                handler.handleMessage(m);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void getHotel(final Handler handler) {
        Parameters parameters = new Parameters();
        parameters.add("pname", APIConstants.PACKAGE_NAME);
        parameters.add("v", "1");
        JuheData.executeWithAPI(APIConstants.ID, APIConstants.TOUR_CITY_LIST_IP, JuheData.GET, parameters, new DataCallBack() {
            @Override
            public void resultLoaded(final int err, String reason, String result) {
                Log.e("err",Integer.toString(err));
                if (err == 0) {
                    try {
                        final JSONObject object = new JSONObject(result);
                        String cityID = new String();
                        Log.e("reason",object.getString("reason"));
                        if(object.getString("reason").equals("key请求次数超限！")) {
                            RouteData.hotelInfo = new RouteData.Hotel();
                            RouteData.hotelInfo.name = "东直门智选假日酒店";
                            Message m = new Message();
                            Bundle b = new Bundle();
                            b.putBoolean("minus", true);
                            b.putString("source", "basic");
                            m.setData(b);
                            handler.handleMessage(m);
                            return ;
                        }
                        JSONArray list = object.getJSONObject("result").getJSONArray("areaList");
                        for (int i = 0; i < list.length(); i++) {
                            if ((((JSONObject) list.get(i)).getJSONArray("name").get(0)).toString().contains(RouteData.city)) {
                                cityID = ((JSONObject) list.get(i)).getString("id");
                            }
                        }
                        if (cityID.equals("")) {
                            RouteData.hotelInfo = new RouteData.Hotel();
                            RouteData.hotelInfo.name = "无";
                            Message m = new Message();
                            Bundle b = new Bundle();
                            b.putBoolean("minus", true);
                            b.putString("source", "basic");
                            m.setData(b);
                            handler.handleMessage(m);
                        } else {
                            Parameters hotelParameters = new Parameters();
                            hotelParameters.add("cityId", cityID);
                            hotelParameters.add("pname", APIConstants.PACKAGE_NAME);
                            hotelParameters.add("v", 1);
                            JuheData.executeWithAPI(APIConstants.ID, APIConstants.HOTEL_LIST_IP, JuheData.GET, hotelParameters, new DataCallBack() {
                                @Override
                                public void resultLoaded(int err2, String reason2, String result2) {
                                    Log.e("err2",Integer.toString(err2));
                                    if (err2 == 0) {
                                        try {
                                            JSONObject object1 = new JSONObject(result2);
                                            JSONArray hotelList = object1.getJSONObject("result").getJSONArray("hotelList");
                                            JSONObject hotel = (JSONObject) hotelList.get(new Random().nextInt(hotelList.length()));
                                            RouteData.hotelInfo = new RouteData.Hotel();
                                            RouteData.hotelInfo.name = hotel.getString("title");
                                            RouteData.hotelInfo.imgsrc = hotel.getString("imgurl");
                                            RouteData.hotelInfo.satisfaction = hotel.getString("manyidu");
                                            RouteData.hotelInfo.address = hotel.getString("address");
                                            RouteData.hotelInfo.grade = hotel.getInt("grade");
                                            RouteData.hotelInfo.intro = hotel.getString("intro");
                                            Message m = new Message();
                                            Bundle b = new Bundle();
                                            b.putBoolean("minus", true);
                                            b.putString("source", "basic");
                                            m.setData(b);
                                            handler.handleMessage(m);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}