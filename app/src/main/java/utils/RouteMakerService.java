package utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.eztrip.model.RouteData;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.thinkland.sdk.android.DataCallBack;
import com.thinkland.sdk.android.JuheData;
import com.thinkland.sdk.android.Parameters;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
        AsyncHttpClient client = new AsyncHttpClient();
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

    /**
     * Get one restaurant at a place whose latitude and longitude are known
     *
     * @param period
     * @param latitude  the latitude of the spot
     * @param longitude the longitude of the spot
     * @return
     * @see com.eztrip.model.RouteData.SpotTemp.period
     */
    public static RouteData.DietTemp getOneNearbyRestaurant(final int period, String latitude, String longitude) {
        Parameters parameters = new Parameters();
        parameters.add("lng", longitude);
        parameters.add("lat", latitude);
        parameters.add("radius", 1000);
        final RouteData.DietTemp[] diet = new RouteData.DietTemp[1];
        diet[0] = new RouteData.DietTemp();
        JuheData.executeWithAPI(APIConstants.DIET_INFO_ID, APIConstants.DIET_INFO_IP, JuheData.GET, parameters, new DataCallBack() {
            @Override
            public void resultLoaded(int err, String reason, String result) {
                if (err == 0) {
                    try {
                        JSONObject object = new JSONObject(result);
                        JSONArray list = object.getJSONArray("result");
                        JSONObject restaurant = list.getJSONObject(0);
                        diet[0] = new RouteData.DietTemp(period,
                                restaurant.getString("name"),
                                restaurant.getString("address"),
                                restaurant.getString("latitude"),
                                restaurant.getString("address"),
                                restaurant.getString("phone"),
                                restaurant.getString("photos"),
                                Integer.parseInt(restaurant.getString("very_good_remarks")) + Integer.parseInt(restaurant.getString("good_remarks")),
                                Integer.parseInt(restaurant.getString("common_remarks")),
                                Integer.parseInt(restaurant.getString("bad_remarks")) + Integer.parseInt(restaurant.getString("very_bad_remarks")),
                                new StringBuilder(restaurant.getString("recommend_dishes")).append(restaurant.getString("recommended_products").equals("") || restaurant.getString("recommend_dishes").equals("") ? "" : ",").append((restaurant.getString("recommended_products"))).toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return diet[0];
    }

    public static void getHotel() {
        Parameters parameters = new Parameters();
        parameters.add("pname", APIConstants.PACKAGE_NAME);
        parameters.add("v", "1");
        JuheData.executeWithAPI(APIConstants.ID, APIConstants.TOUR_CITY_LIST_IP, JuheData.GET, parameters, new DataCallBack() {
            @Override
            public void resultLoaded(int err, String reason, String result) {
                if (err == 0) {
                    try {
                        String cityID = new String();
                        final JSONObject object = new JSONObject(result);
                        JSONArray list = object.getJSONObject("result").getJSONArray("areaList");
                        for (int i = 0; i < list.length(); i++) {
                            if (((JSONObject) ((JSONObject) list.get(i)).getJSONArray("name").get(0)).toString().contains(RouteData.city)) {
                                cityID = ((JSONObject) list.get(i)).getString("id");
                            }
                        }
                        if (cityID.equals("")) {
                            RouteData.hotelInfo.name = "无";
                        } else {
                            Parameters hotelParameters = new Parameters();
                            hotelParameters.add("cityId", cityID);
                            hotelParameters.add("pname", APIConstants.PACKAGE_NAME);
                            hotelParameters.add("v", 1);
                            JuheData.executeWithAPI(APIConstants.ID, APIConstants.HOTEL_LIST_IP, JuheData.GET, hotelParameters, new DataCallBack() {
                                @Override
                                public void resultLoaded(int err2, String reason2, String result2) {
                                    if (err2 == 0) {
                                        try {
                                            JSONObject object1 = new JSONObject(result2);
                                            JSONArray hotelList = object1.getJSONObject("result").getJSONArray("hotelList");
                                            JSONObject hotel = (JSONObject) hotelList.get(new Random().nextInt(hotelList.length()));
                                            RouteData.hotelInfo = new RouteData.Hotel();
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