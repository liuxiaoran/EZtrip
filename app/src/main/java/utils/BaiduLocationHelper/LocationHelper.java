package utils.BaiduLocationHelper;

import android.content.Context;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

/**
 * Created by liuxiaoran on 15/3/16.
 */
public class LocationHelper {


    public Context context;
    public LocationClient mLocationClient = null;

    public BDLocationListener locationListener;

    private static final String TAG = "LocalHelper";

    public static void registerHelper(Context context, BDLocationListener locationListener) {


        new LocationHelper(context, locationListener);
    }

    public LocationHelper(Context context, BDLocationListener locationListener) {

        this.context = context;
        this.locationListener = locationListener;

        mLocationClient = new LocationClient(context.getApplicationContext());     //声明LocationClient类
        mLocationClient.start();
        mLocationClient.registerLocationListener(locationListener);    //注册监听函数

        LocationClientOption locationOption = new LocationClientOption();
        locationOption.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
        locationOption.setIsNeedAddress(true);
        mLocationClient.setLocOption(locationOption);
    }

    public void stopLocation() {
        mLocationClient.stop();
    }


}
