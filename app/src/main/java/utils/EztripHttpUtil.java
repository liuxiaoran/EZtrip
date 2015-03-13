package utils;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.HashMap;
import java.util.List;

/**
 * Created by liuxiaoran on 15/3/13.
 * <p/>
 * 封装loopj  异步的http
 */
public class EztripHttpUtil {

    private static AsyncHttpClient httpClient;

    public EztripHttpUtil() {
        httpClient = new AsyncHttpClient();
    }

    public static void get(String url, HashMap hashMap, AsyncHttpResponseHandler handler) {
        RequestParams params = new RequestParams(hashMap);
        httpClient.get(url, params, handler);

    }

    public static void post(String url, HashMap hashMap, AsyncHttpResponseHandler handler) {
        RequestParams params = new RequestParams(hashMap);
        httpClient.post(url, params, handler);
    }

    /**
     * 上传一个list
     * example:
     * List<Map<String, String>> listOfMaps = new ArrayList<Map<String,
     * String>>();
     * Map<String, String> user1 = new HashMap<String, String>();
     * user1.put("age", "30");
     * user1.put("gender", "male");
     * Map<String, String> user2 = new HashMap<String, String>();
     * user2.put("age", "25");
     * user2.put("gender", "female");
     * listOfMaps.add(user1);
     * listOfMaps.add(user2);
     * params.put("users", listOfMaps); // url params: "users[][age]=30&users[][gender]=male&users[][age]=25&users[][gender]=female"
     *
     * @param url
     * @param key     list的key
     * @param list    arraylist
     * @param handler
     */
    public static void get(String url, String key, List<HashMap<String, String>> list, AsyncHttpResponseHandler handler) {

        RequestParams params = new RequestParams(key, list);
        httpClient.get(url, params, handler);

    }

    public static void post(String url, String key, List<HashMap<String, String>> list, AsyncHttpResponseHandler handler) {

        RequestParams params = new RequestParams(key, list);
        httpClient.post(url, params, handler);

    }
}
