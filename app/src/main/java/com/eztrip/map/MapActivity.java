package com.eztrip.map;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.eztrip.R;
import com.eztrip.model.RouteData;

/**
 * Created by Steve on 2015/3/23.
 */
public class MapActivity extends Activity {
    BaiduMap mBaiduMap = null;
    MapView mMapView = null;
    int nodeIndex = -1;//节点索引,供浏览节点时使用
    RouteLine route = null;
    OverlayManager routeOverlay = null;
    private TextView popupText = null;//泡泡view
    LinearLayout preNextLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mMapView = (MapView) findViewById(R.id.bmapView);
        preNextLayout = (LinearLayout)findViewById(R.id.pre_next_layout);
        mBaiduMap = mMapView.getMap();
        Bundle b = getIntent().getExtras();
        String type = b.getString("type");
        if(type.equals("point")) {
            String latitude = b.getString("latitude");
            String longitude = b.getString("longitude");
            mBaiduMap.clear();
            mBaiduMap.addOverlay(new MarkerOptions().position(new LatLng(Double.parseDouble(latitude),Double.parseDouble(longitude)))
                    .icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.ic_marka)));
            mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(new LatLng(Double.parseDouble(latitude),Double.parseDouble(longitude))));
            preNextLayout.setVisibility(View.GONE);
        }else if(type.equals("route")) {
            int index = b.getInt("index");
            Log.e("index",Integer.toString(index));
            if(RouteData.trafficInfo.equals(MapActivity.this.getResources().getString(R.string.routemaker_trafficsettings_public))) {
                route = RouteData.singleEvents.get(index).transitRouteLine;
                TransitRouteOverlay overlay = new TransitRouteOverlay(mBaiduMap) {
                    @Override
                    public BitmapDescriptor getStartMarker() {
                        return BitmapDescriptorFactory.fromResource(R.drawable.ic_st);
                    }

                    @Override
                    public BitmapDescriptor getTerminalMarker() {
                        return BitmapDescriptorFactory.fromResource(R.drawable.ic_en);
                    }
                };
                mBaiduMap.setOnMarkerClickListener(overlay);
                routeOverlay = overlay;
                overlay.setData(RouteData.singleEvents.get(index).getTransitRouteLine(MapActivity.this));
                overlay.addToMap();
                overlay.zoomToSpan();
            }else {
                route = RouteData.singleEvents.get(index).getDrivingRouteLine(MapActivity.this);
                DrivingRouteOverlay overlay = new DrivingRouteOverlay(mBaiduMap) {
                    @Override
                    public BitmapDescriptor getStartMarker() {
                        return BitmapDescriptorFactory.fromResource(R.drawable.ic_st);
                    }

                    @Override
                    public BitmapDescriptor getTerminalMarker() {
                        return BitmapDescriptorFactory.fromResource(R.drawable.ic_en);
                    }
                };
                mBaiduMap.setOnMarkerClickListener(overlay);
                routeOverlay = overlay;
                overlay.setData(RouteData.singleEvents.get(index).getDrivingRouteLine(MapActivity.this));
                overlay.addToMap();
                overlay.zoomToSpan();
            }
        }
    }

    /**
     * 节点浏览示例
     *
     * @param v
     */
    public void nodeClick(View v) {
        if (route == null ||
                route.getAllStep() == null) {
            return;
        }
        if (nodeIndex == -1 && v.getId() == R.id.pre) {
            return;
        }
        //设置节点索引
        if (v.getId() == R.id.next) {
            if (nodeIndex < route.getAllStep().size() - 1) {
                nodeIndex++;
            } else {
                return;
            }
        } else if (v.getId() == R.id.pre) {
            if (nodeIndex > 0) {
                nodeIndex--;
            } else {
                return;
            }
        }
        //获取节结果信息
        LatLng nodeLocation = null;
        String nodeTitle = null;
        Object step = route.getAllStep().get(nodeIndex);
        if (step instanceof DrivingRouteLine.DrivingStep) {
            nodeLocation = ((DrivingRouteLine.DrivingStep) step).getEntrace().getLocation();
            nodeTitle = ((DrivingRouteLine.DrivingStep) step).getInstructions();
        } else if (step instanceof WalkingRouteLine.WalkingStep) {
            nodeLocation = ((WalkingRouteLine.WalkingStep) step).getEntrace().getLocation();
            nodeTitle = ((WalkingRouteLine.WalkingStep) step).getInstructions();
        } else if (step instanceof TransitRouteLine.TransitStep) {
            nodeLocation = ((TransitRouteLine.TransitStep) step).getEntrace().getLocation();
            nodeTitle = ((TransitRouteLine.TransitStep) step).getInstructions();
        }

        if (nodeLocation == null || nodeTitle == null) {
            return;
        }
        //移动节点至中心
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(nodeLocation));
        // show popup
        popupText = new TextView(MapActivity.this);
        popupText.setBackgroundResource(R.drawable.popup);
        popupText.setTextColor(0xFF000000);
        popupText.setText(nodeTitle);
        mBaiduMap.showInfoWindow(new InfoWindow(popupText, nodeLocation, 0));

    }
}
