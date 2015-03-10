package utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Steve on 2015/3/9.
 */
public class RouteMakerService {
    /**
     * @param city     The name of the city.
     * @param activity The activity which calls this method
     * @return user's collection of spots in that city
     */
    public static ArrayList<String> getSpotsCollection(String city, final Activity activity) {
        final ArrayList<String> result = new ArrayList<>();
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
                        result.add(object.getString("name"));
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
}