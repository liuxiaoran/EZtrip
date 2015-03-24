package utils;

import android.app.Activity;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.eztrip.R;
import com.eztrip.model.Clock;
import com.eztrip.model.RouteData;
import com.eztrip.routemaker.RouteMakerFragment;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Steve on 2015/2/25.
 * dispose data after each step of routemaker period
 */
public class RouteAutoGenerator {
    public static final String success = "success";
    public static final String failure = "failure";
    public static RoutePlanSearch[][] kSearch;

    public static ArrayList<RouteData.SpotTemp> executeBasicSettings(String cityName, ArrayList<HashMap<String, String>> spots, int totalDay, String trafficInfo, String dietInfo, final Activity activity) {
        RouteData.city = cityName;
        RouteData.trafficInfo = trafficInfo;
        RouteData.dietInfo = dietInfo;
        RouteData.setSpotTempInfoInstance(spots.size(), totalDay);
        RouteData.dayLength = totalDay;
        RouteData.basicSettingsSpot = (ArrayList<HashMap<String, String>>) spots.clone();
        final ArrayList<RouteData.SpotTemp> spotList = new ArrayList<>(spots.size());
        int[] position = new int[spots.size()]; // the position each item in spotList should be sorted by time
        int totalVisitTime = 0;
        for (int i = 0; i < spots.size(); i++) {
            RouteData.SpotTemp spot = new RouteData.SpotTemp();
            //TODO:
//            spot.setSpotTemp(RouteData.ActivityType.SPOT, -1, spots.get(i).get("name"), RouteMakerService.getVisitTime(spot.detail, activity), spots.get(i).get("address"));
            spot.setSpotTemp(RouteData.ActivityType.SPOT, -1, spots.get(i).get("name"), 120, spots.get(i).get("address"));
            spotList.add(spot);
            totalVisitTime += spot.recommendTime;
        }
//        final ArrayList<HashMap<String, Double>> spotPosition = new ArrayList<>();
//        GeoCoder mSearch = GeoCoder.newInstance();
//        for (int i = 0; i < spotList.size(); i++) {
//            final int index = i;
//            OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
//                public void onGetGeoCodeResult(GeoCodeResult result) {
//                    if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
//                        //没有检索到结果
//                        //TODO:抛异常
//                    }
//                    //获取地理编码结果
//                    HashMap<String, Double> latLngInfo = new HashMap<>();
//                    latLngInfo.put("latitude", result.getLocation().latitude);
//                    latLngInfo.put("longitude", result.getLocation().longitude);
//                    spotPosition.add(index, latLngInfo);
//                }
//
//                @Override
//                public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
//                    if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
//                        //没有找到检索结果
//                    }
//                    //获取反向地理编码结果
//                }
//            };
//            mSearch.setOnGetGeoCodeResultListener(listener);
//            mSearch.geocode(new GeoCodeOption()
//                    .city(RouteData.city)
//                    .address(spotList.get(i).address));
//        }
//        mSearch.destroy();
        return spotList;
    }

    public static void getSpotTimeAndHotel(final RouteMakerFragment.MyHandler handler, ArrayList<RouteData.SpotTemp> spotList, final Activity activity) {
        RouteData.distance = new HashMap[spotList.size()][];
        kSearch = new RoutePlanSearch[spotList.size()][];
        for (int i = 0; i < spotList.size(); i++) {
            RouteData.distance[i] = new HashMap[spotList.size()];
            kSearch[i] = new RoutePlanSearch[spotList.size()];
            for (int j = 0; j < spotList.size(); j++) {
                RouteData.distance[i][j] = new HashMap<String, Object>();
                kSearch[i][j] = RoutePlanSearch.newInstance();
            }
        }
        for (int i = 0; i < spotList.size() - 1; i++) {
            for (int j = i + 1; j < spotList.size(); j++) {
                final int i1 = i, j1 = j;
                OnGetRoutePlanResultListener listener = new OnGetRoutePlanResultListener() {
                    public void onGetWalkingRouteResult(WalkingRouteResult result) {
                        RouteData.distance[i1][j1].put("walking route", result.getRouteLines().get(0).getAllStep());
                    }

                    public void onGetTransitRouteResult(TransitRouteResult result) {
                        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                            //TODO: 抛异常
                        }
                        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                            //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                            //result.getSuggestAddrInfo()
                        }
                        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                            RouteData.distance[i1][j1].put("bus route", result.getRouteLines().get(0).getAllStep());
                            RouteData.distance[i1][j1].put("bus time", result.getRouteLines().get(0).getDuration() / 60);
                            if (RouteData.trafficInfo.equals(activity.getResources().getString(R.string.routemaker_trafficsettings_public))) {
                                Message m = new Message();
                                Bundle b = new Bundle();
                                b.putBoolean("minus", true);
                                b.putString("source", "basic");
                                m.setData(b);
                                handler.handleMessage(m);
                            }
                        }
                    }

                    public void onGetDrivingRouteResult(DrivingRouteResult result) {
                        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                            RouteData.distance[i1][j1].put("drive route", result.getRouteLines().get(0).getAllStep());
                            RouteData.distance[i1][j1].put("drive time", result.getRouteLines().get(0).getDuration() / 60);
                            if (RouteData.trafficInfo.equals(activity.getResources().getString(R.string.routemaker_trafficsettings_private))) {
                                Message m = new Message();
                                Bundle b = new Bundle();
                                b.putBoolean("minus", true);
                                b.putString("source", "basic");
                                m.setData(b);
                                m.sendToTarget();
                            }
                        }
                    }
                };
                kSearch[i1][j1].setOnGetRoutePlanResultListener(listener);
                PlanNode stNode = PlanNode.withCityNameAndPlaceName(RouteData.city, spotList.get(i).detail);
                PlanNode enNode = PlanNode.withCityNameAndPlaceName(RouteData.city, spotList.get(j).detail);
                if (RouteData.trafficInfo.equals(activity.getResources().getString(R.string.routemaker_trafficsettings_private))) {
                    kSearch[i1][j1].drivingSearch((new DrivingRoutePlanOption())
                            .from(stNode)
                            .to(enNode));
                } else {
                    kSearch[i1][j1].transitSearch((new TransitRoutePlanOption())
                            .from(stNode)
                            .city("北京")
                            .to(enNode));
                }
            }
        }
        RouteMakerService.getHotel(handler);
    }

    public static String generateSpotSettingsPlan(ArrayList<RouteData.SpotTemp> spotList, Activity activity) {
        for (int i = 0; i < spotList.size() - 1; i++) {
            for (int j = i + 1; j < spotList.size(); j++) {
                if (RouteData.trafficInfo.equals(activity.getResources().getString(R.string.routemaker_trafficsettings_public))) {
                    RouteData.distance[i][j].put("route", RouteData.distance[i][j].get("bus route"));
                    RouteData.distance[i][j].put("time", RouteData.distance[i][j].get("bus time"));
                } else {
                    RouteData.distance[i][j].put("route", RouteData.distance[i][j].get("drive route"));
                    RouteData.distance[i][j].put("time", RouteData.distance[i][j].get("drive time"));
                }
                RouteData.distance[i][j].put("connection status", "no");
            }
        }

        SortedDistance[] sortedDistances = new SortedDistance[spotList.size() * (spotList.size() - 1) / 2];
        for (int i = 0; i < spotList.size(); i++)
            sortedDistances[i] = new SortedDistance();
        for (int i = 0; i < RouteData.distance.length - 1; i++) {
            for (int j = i + 1; j < RouteData.distance.length; j++) {
                sortedDistances[i * RouteData.distance.length - i * (i + 1) / 2 + j - i - 1].distance = (int) RouteData.distance[i][j].get("time");
                sortedDistances[i * RouteData.distance.length - i * (i + 1) / 2 + j - i - 1].i = i;
                sortedDistances[i * RouteData.distance.length - i * (i + 1) / 2 + j - i - 1].j = j;
            }
        }
        Arrays.sort(sortedDistances, new Comparator<SortedDistance>() {
            @Override
            public int compare(SortedDistance lhs, SortedDistance rhs) {
                return lhs.distance - rhs.distance;
            }
        });
        int combinedSpotNum = spotList.size();
        if (combinedSpotNum <= RouteData.dayLength)
            RouteData.warning = "NotBusy";
        for (int i = 0; i < sortedDistances.length; i++) {
            if (combinedSpotNum <= RouteData.dayLength)
                break;
            if (combineTwoSpots(sortedDistances, spotList, i))
                combinedSpotNum--;
        }
        //TODO
//        if (combinedSpotNum > RouteData.dayLength) {
//            RouteData.warning = "TooBusy";
//            for (int i = 0; i < sortedDistances.length; i++) {
//                if (combineTwoSpots(sortedDistances, spotList, i))
//                    combinedSpotNum--;
//            }
//            return failure;
//        }
        int spotIndex = 0;
        ArrayList<Integer> visitedSpotIndex = new ArrayList<>();
        for (int i = 0; i < RouteData.dayLength; i++) {
            RouteData.SpotTemp[] nothing = new RouteData.SpotTemp[3];
            for (int j = 0; j < 3; j++) {
                nothing[i] = new RouteData.SpotTemp();
                nothing[i].period = 3 * i + j;
                RouteData.spotTempInfo.add(nothing[i]);
            }
            int leftMostIndex = -1;
            while (visitedSpotIndex.indexOf(leftMostIndex) != -1 || leftMostIndex == -1) {
                if (leftMostIndex != -1)
                    visitedSpotIndex.add(leftMostIndex);
                spotIndex++;
                leftMostIndex = getOneEndIndex(spotList, spotIndex, -1);
            }
            arrangeCurrentDayPlan(spotList, leftMostIndex, i);
            RouteData.SpotTemp hotel = new RouteData.SpotTemp();
            hotel.setSpotTemp(RouteData.ActivityType.ACCOMMODATION, 3 * i + 2, RouteData.hotelInfo.name, 0, RouteData.hotelInfo.address);
            RouteData.spotTempInfo.add(hotel);
        }
        Collections.sort(RouteData.spotTempInfo, new Comparator<RouteData.SpotTemp>() {
            @Override
            public int compare(RouteData.SpotTemp lhs, RouteData.SpotTemp rhs) {
                return lhs.period - rhs.period;
            }
        });
        for (int i = 0; i < RouteData.spotTempInfo.size(); i++)
            Log.e("infr", RouteData.spotTempInfo.get(i).detail + " " + RouteData.spotTempInfo.get(i).period);
        return success;
    }

    private static void arrangeCurrentDayPlan(ArrayList<RouteData.SpotTemp> spotList, int leftMostIndex, int currDay) {
        final int moringMaxVisitTime = 210, afternoonMaxVisitTime = 270;
        int maxTime = moringMaxVisitTime;
        int periodVisitTime = 0;
        int roadTime = 0;
        int periodOfTheDay = 0;
//        RouteData.SpotTemp nothing = new RouteData.SpotTemp();
//        nothing.period = 3 * currDay + periodOfTheDay;
//        Log.e(nothing.detail,Integer.toString(nothing.period));
//        RouteData.spotTempInfo.add(nothing);
        RouteData.SpotTemp lastSpot = null, currSpot, nextSpot = spotList.get(leftMostIndex);
        do {
            currSpot = nextSpot;
            if (periodVisitTime + roadTime + currSpot.recommendTime < 1.5 * maxTime) {
                periodVisitTime += currSpot.recommendTime + roadTime;
            } else {
                periodVisitTime = currSpot.recommendTime + roadTime;
                periodOfTheDay++;
                if (periodOfTheDay == 2)
                    maxTime = afternoonMaxVisitTime;
                else
                    maxTime = Integer.MAX_VALUE;
//                RouteData.SpotTemp nothing2 = new RouteData.SpotTemp();
//                nothing2.period = 3 * currDay + periodOfTheDay;
//                RouteData.spotTempInfo.add(nothing2);
            }
            currSpot.period = 3 * currDay + periodOfTheDay;
            RouteData.spotTempInfo.add(currSpot);
            nextSpot = currSpot.rightSpot == lastSpot ? currSpot.leftSpot : currSpot.rightSpot;
            lastSpot = currSpot;

        }
        while (nextSpot != null);
    }

    public static ArrayList<RouteData.SpotTemp> regenerateSpotSettings(Activity activity, ArrayList<HashMap<String, String>> newSpots) {
        ArrayList<HashMap<String, String>> spots = (ArrayList<HashMap<String, String>>) RouteData.basicSettingsSpot.clone();
        spots.addAll(newSpots);
        return executeBasicSettings(RouteData.city, spots, RouteData.dayLength, RouteData.trafficInfo, RouteData.dietInfo, activity);
    }

    public static String executeSpotSettings(Activity activity, RouteMakerFragment.MyHandler handler) {
        boolean breakfast = RouteData.dietInfo.contains(activity.getResources().getString(R.string.routemaker_dietsettings_breakfast));
        boolean lunch = RouteData.dietInfo.contains(activity.getResources().getString(R.string.routemaker_dietsettings_lunch));
        boolean dinner = RouteData.dietInfo.contains(activity.getResources().getString(R.string.routemaker_dietsettings_dinner));
        RouteData.setDietTempInfoInstance(RouteData.dayLength * 3);
        int timesToEat = 0;
        if (breakfast)
            timesToEat++;
        if (lunch)
            timesToEat++;
        if (dinner)
            timesToEat++;
        handler.setCount(RouteData.dayLength * timesToEat + 1);
        int index = 0;
        //try to give one advice for each diet, according to the position of its adjusting activities
        for (int i = 0; i < RouteData.dayLength; i++) {
            if (breakfast) {
                while (RouteData.spotTempInfo.get(index).period <= 3 * i || !RouteData.spotTempInfo.get(index).type.equals(RouteData.ActivityType.SPOT)){
                    index++;
                }
                RouteMakerService.getOneNearbyRestaurant(3 * i, RouteData.spotTempInfo.get(index).latitude, RouteData.spotTempInfo.get(index).longitude, handler);

            } else {
                RouteData.dietTempInfo[3 * i] = new RouteData.DietTemp("无",3*i);
            }
            Log.e("details",RouteData.spotTempInfo.get(index).period + " " + RouteData.spotTempInfo.get(index).detail);
            if (lunch) {
                int previousIndex = index;
                while (RouteData.spotTempInfo.get(index).period <= 3 * i + 1) {
                    if (RouteData.spotTempInfo.get(index).period == 3 * i + 1 && RouteData.spotTempInfo.get(index).type.equals(RouteData.ActivityType.SPOT)) {
                        break;
                    }
                    if(RouteData.spotTempInfo.get(index).type.equals(RouteData.ActivityType.SPOT))
                    previousIndex = index;
                    Log.e("previousIndex",Integer.toString(previousIndex));
                    index++;
                }
                RouteMakerService.getOneNearbyRestaurant(3 * i + 1, RouteData.spotTempInfo.get(previousIndex).latitude, RouteData.spotTempInfo.get(previousIndex).longitude, handler);
            } else {
                RouteData.dietTempInfo[3 * i + 1] = new RouteData.DietTemp("无",3*i+1);
            }
            if (dinner) {
                int previousIndex = index;
                while (RouteData.spotTempInfo.get(index).period <= 3 * i + 2 || !RouteData.spotTempInfo.get(index).type.equals(RouteData.ActivityType.SPOT)) {
                    if (RouteData.spotTempInfo.get(index).type.equals(RouteData.ActivityType.SPOT))
                        previousIndex = index;
                    index++;
                }
                RouteMakerService.getOneNearbyRestaurant(3 * i + 2, RouteData.spotTempInfo.get(previousIndex).latitude, RouteData.spotTempInfo.get(previousIndex).longitude, handler);
            } else {
                RouteData.dietTempInfo[3 * i + 2] = new RouteData.DietTemp("无",3*i+2);
            }
        }
        Message m = new Message();
        Bundle b = new Bundle();
        b.putBoolean("minus", true);
        b.putString("source", "spot");
        m.setData(b);
        handler.handleMessage(m);
        return success;
    }

    public static String executeDietSettings(final Activity activity) {
        Clock startTime = new Clock(8, 0);
        final Clock[] currTime = new Clock[1];
        currTime[0] = new Clock(8, 0);
        int index = 0;
        int period = -1;
        RouteData.setSingleEventsInstance();
        String lastLatitude = RouteData.hotelInfo.latitude, lastLongitude = RouteData.hotelInfo.longitude, lastPlace = RouteData.hotelInfo.name, lastAddress = RouteData.hotelInfo.address;
        while (index < RouteData.spotTempInfo.size()) {
            final RouteData.SingleEvent trafficEvent = new RouteData.SingleEvent(), otherEvent = new RouteData.SingleEvent();
            otherEvent.locationInfo = new ArrayList<>() ;
            String finishLatitude, finishLongitude, finishPlace, finishAddress;
            if (RouteData.spotTempInfo.get(index).type == RouteData.ActivityType.NONE)
                index++;
            if (RouteData.spotTempInfo.get(index).period > period && !RouteData.dietTempInfo[RouteData.spotTempInfo.get(index).period].detail.equals("无")) {
                finishLatitude = RouteData.dietTempInfo[period + 1].latitude;
                finishLongitude = RouteData.dietTempInfo[period + 1].longitude;
                finishPlace = RouteData.dietTempInfo[period + 1].detail;
                otherEvent.type = RouteData.ActivityType.DIET;
                otherEvent.day = RouteData.dietTempInfo[period + 1].period / 3;
                finishAddress = RouteData.dietTempInfo[period + 1].address;
                otherEvent.timeLength =  60;
            } else {
                finishLatitude = RouteData.spotTempInfo.get(index).latitude;
                finishLongitude = RouteData.spotTempInfo.get(index).longitude;
                finishPlace = RouteData.spotTempInfo.get(index).detail;
                otherEvent.type = RouteData.spotTempInfo.get(index).type;
                otherEvent.day = RouteData.spotTempInfo.get(index).period / 3;
                finishAddress = RouteData.spotTempInfo.get(index).address;
                otherEvent.timeLength = RouteData.spotTempInfo.get(index).recommendTime;
                index++;
            }
            otherEvent.detail = finishPlace;
            HashMap<String, String> placeInfo = new HashMap<>();
            placeInfo.put("latitude", finishLatitude);
            placeInfo.put("longitude", finishLongitude);
            placeInfo.put("address",finishAddress);
            otherEvent.locationInfo.add(placeInfo);

            if (index < RouteData.spotTempInfo.size() && RouteData.spotTempInfo.get(index).period > period)
                period++;
            //TODO:根据起终点信息和RouteData.trafficInfo确定线路 将其封装在trafficEvent里
            trafficEvent.type = RouteData.ActivityType.TRAFFIC;
            trafficEvent.detail = "traffic";
            RouteData.singleEvents.add(trafficEvent);
            //TODO:设置otherEvent的startTime，finishTime
            RouteData.singleEvents.add(otherEvent);
        }
        for(int i = 0; i < RouteData.singleEvents.size(); i++) {
            Log.e("singleevents",RouteData.singleEvents.get(i).detail);
        }
        return success;
    }

    public static void getTrafficTimes(Activity activity, RouteMakerFragment.MyHandler handler) {
        Log.e("length",Integer.toString(RouteData.singleEvents.size()));
        int index = 0;
        String lastAddress, finishAddress;
        while(index != -1) {
            while (index < RouteData.singleEvents.size() && !RouteData.singleEvents.get(index).type.equals(RouteData.ActivityType.TRAFFIC)){
                index++;
            }
            if(index == RouteData.singleEvents.size())
                break;
            if(index == 0)
                lastAddress = RouteData.hotelInfo.name;
            else
                lastAddress = RouteData.singleEvents.get(index-1).detail;
            finishAddress = RouteData.singleEvents.get(index + 1).detail;
            RouteData.singleEvents.get(index).detail = lastAddress + "-" + finishAddress;
            HashMap<String, String> startPlaceInfo = new HashMap<>();
            startPlaceInfo.put("address",lastAddress);
            RouteData.singleEvents.get(index).locationInfo.add(startPlaceInfo);
            HashMap<String, String> finishPlaceInfo = new HashMap<>();
            finishPlaceInfo.put("address",finishAddress);
            RouteData.singleEvents.get(index).locationInfo.add(finishPlaceInfo);
            index = getSingleTrafficTime(activity,handler,index,lastAddress, finishAddress);
        }
        Message m = new Message();
        Bundle b = new Bundle();
        b.putBoolean("minus", true);
        b.putString("source", "diet");
        m.setData(b);
        handler.handleMessage(m);
    }

    public static int getSingleTrafficTime(final Activity activity, final RouteMakerFragment.MyHandler handler, int index, String lastAddress, String finishAddress) {
        Log.e("index",Integer.toString(index));
        final int trafficIndex = index;
        final RoutePlanSearch mSearch = RoutePlanSearch.newInstance();
        OnGetRoutePlanResultListener listener = new OnGetRoutePlanResultListener() {
            public void onGetWalkingRouteResult(WalkingRouteResult result) {
            }

            public void onGetTransitRouteResult(TransitRouteResult result) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    //TODO: 抛异常
                }
                if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                    //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                    //result.getSuggestAddrInfo()
                }
                if (RouteData.trafficInfo.equals(activity.getResources().getString(R.string.routemaker_trafficsettings_public))) {
//                    RouteData.singleEvents.get(trafficIndex).detail = result.getRouteLines().get(0).getAllStep().toString();
                    int duration = result.getRouteLines().get(0).getDuration() / 60;
                    RouteData.singleEvents.get(trafficIndex).timeLength = duration;

                    Message m = new Message();
                    Bundle b = new Bundle();
                    b.putBoolean("minus", true);
                    b.putString("source", "diet");
                    m.setData(b);
                    handler.handleMessage(m);
                    mSearch.destroy();
                }
            }

            public void onGetDrivingRouteResult(DrivingRouteResult result) {
                if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                    if (RouteData.trafficInfo.equals(activity.getResources().getString(R.string.routemaker_trafficsettings_private))) {
//                        RouteData.singleEvents.get(trafficIndex).detail = result.getRouteLines().get(0).getAllStep().toString();
                        int duration = result.getRouteLines().get(0).getDuration() / 60;
                        RouteData.singleEvents.get(trafficIndex).timeLength = duration;

                        Message m = new Message();
                        Bundle b = new Bundle();
                        b.putBoolean("minus", true);
                        b.putString("source", "diet");
                        m.setData(b);
                        handler.handleMessage(m);
                        mSearch.destroy();
                    }
                }
            }
        };
        mSearch.setOnGetRoutePlanResultListener(listener);
        PlanNode stNode = PlanNode.withCityNameAndPlaceName(RouteData.city, lastAddress);
        PlanNode enNode = PlanNode.withCityNameAndPlaceName(RouteData.city, finishAddress);
        Log.e("traffichint",Integer.toString(index) + lastAddress + finishAddress);
        if (RouteData.trafficInfo.equals(activity.getResources().getString(R.string.routemaker_trafficsettings_private))) {
            mSearch.drivingSearch((new DrivingRoutePlanOption())
                    .from(stNode)
                    .to(enNode));
        } else {
            mSearch.transitSearch((new TransitRoutePlanOption())
                    .from(stNode)
                    .city("北京")
                    .to(enNode));
        }
        index++;
        return index;
    }

    public static void arrangeTimeSettingsTime() {
        final Clock startTime = new Clock(8,0);
        Clock currTime = startTime;
        int index = 0, currDay = 0;
        while(index < RouteData.singleEvents.size()) {
            Log.e(currTime.toString(),Integer.toString(RouteData.singleEvents.get(index).timeLength));
            if(RouteData.singleEvents.get(index).day > currDay)
                currTime = startTime;
            RouteData.singleEvents.get(index).startTime = new Clock(currTime);
            RouteData.singleEvents.get(index).finishTime = new Clock(currTime.add(RouteData.singleEvents.get(index).timeLength));
            currTime = new Clock(RouteData.singleEvents.get(index).finishTime);
            index++;
        }
        for(int i = 0; i < RouteData.singleEvents.size(); i++) {
            Log.e("time",RouteData.singleEvents.get(i).startTime.toString());
        }
    }


    public static String executeFinishSettings(final RouteMakerFragment.MyHandler handler, final Activity activity, String startTime, String name) {
        EztripHttpUtil eztripHttpUtil = new EztripHttpUtil();
        AsyncHttpResponseHandler asyncHttpResponseHandler = new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Toast.makeText(activity,"线路生成成功，您可以在“行程规划”中查看生成的线路",Toast.LENGTH_LONG).show();
                Message m = new Message();
                Bundle b = new Bundle();
                b.putBoolean("minus", true);
                b.putString("source","finish");
                b.putBoolean("success",true);
                m.setData(b);
                handler.handleMessage(m);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(activity,"连接服务器失败",Toast.LENGTH_LONG).show();
                Message m = new Message();
                Bundle b = new Bundle();
                b.putBoolean("minus", true);
                b.putString("source","finish");
                b.putBoolean("success",false);
                m.setData(b);
                handler.handleMessage(m);
            }
        };
        JSONObject parameter = new JSONObject();
        try {
            //TODO: id来源
            parameter.put("id",1);
            parameter.put("name",name);
            parameter.put("city",RouteData.city);
            parameter.put("start_date",startTime);
            parameter.put("day_length",RouteData.dayLength);
            List<Map> eventList = new ArrayList<>();
            for(int i = 0; i < RouteData.singleEvents.size(); i++) {
                Map<String,Object> map = new HashMap<>();
                map.put("day",RouteData.singleEvents.get(i).day);
                map.put("type",RouteData.singleEvents.get(i).type.toString().toLowerCase());
                map.put("start_time",RouteData.singleEvents.get(i).startTime);
                map.put("finish_time",RouteData.singleEvents.get(i).finishTime);
                map.put("detail",RouteData.singleEvents.get(i).detail);
                List<Map> locationList = new ArrayList<>();
                for(int j = 0; j < RouteData.singleEvents.get(i).locationInfo.size(); j++) {
                    Map<String, String> locationInfo = new HashMap<>();
                    locationInfo.put("longitude",RouteData.singleEvents.get(i).locationInfo.get(j).get("longitude"));
                    locationInfo.put("latitude",RouteData.singleEvents.get(i).locationInfo.get(j).get("latitude"));
                    locationInfo.put("address",RouteData.singleEvents.get(i).locationInfo.get(j).get("address"));
                    locationList.add(locationInfo);
                }
                map.put("places",locationList);
                eventList.add(map);
            }
            parameter.put("event",eventList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HashMap<String,Object> request = new HashMap<>();
        request.put("content",parameter);
        EztripHttpUtil.post(URLConstants.SUBMIT_ROUTE,request,asyncHttpResponseHandler);
        return success;
    }

    public static boolean combineTwoSpots(SortedDistance[] sortedDistances, ArrayList<RouteData.SpotTemp> spotList, int index) {
        final int maxVisitTime = 480;
        int i = sortedDistances[index].i, j = sortedDistances[index].j;
        int combinedVisitTime = spotList.get(i).combinedVisitTime + spotList.get(j).combinedVisitTime + sortedDistances[index].distance;
        Log.e("combined", Integer.toString(combinedVisitTime) + " " + index);
        if (combinedVisitTime <= maxVisitTime) {
            if (spotList.get(i).rightSpot != null || spotList.get(j).rightSpot != null)
                return false;
            if (spotList.get(i).leftSpot == null) {
                spotList.get(i).leftSpot = spotList.get(j);
                spotList.get(i).leftRoadTime = sortedDistances[index].distance;
            } else {
                spotList.get(i).rightSpot = spotList.get(j);
                spotList.get(i).rightRoadTime = sortedDistances[index].distance;
            }
            if (spotList.get(j).leftSpot == null) {
                spotList.get(j).leftSpot = spotList.get(i);
                spotList.get(j).leftRoadTime = sortedDistances[index].distance;
            } else {
                spotList.get(j).rightSpot = spotList.get(i);
                spotList.get(j).rightRoadTime = sortedDistances[index].distance;
            }
            updateCombinedTime(i, j, spotList, combinedVisitTime);
            updateCombinedTime(j, i, spotList, combinedVisitTime);
            return true;
        } else
            return false;
    }

    public static void updateCombinedTime(int currIndex, int lastIndex, ArrayList<RouteData.SpotTemp> spotList, int combinedTime) {
        spotList.get(currIndex).combinedVisitTime = combinedTime;
        int nextIndex = spotList.indexOf(spotList.get(currIndex).leftSpot) == lastIndex ? spotList.indexOf(spotList.get(currIndex).rightSpot) : spotList.indexOf(spotList.get(currIndex).leftSpot);
        Log.e("left right", Integer.toString(spotList.indexOf(spotList.get(currIndex).leftSpot)) + " " + Integer.toString(spotList.indexOf(spotList.get(currIndex).rightSpot)));
        if (nextIndex != -1)
            updateCombinedTime(nextIndex, currIndex, spotList, combinedTime);
    }

    public static int getOneEndIndex(ArrayList<RouteData.SpotTemp> spotList, int currIndex, int lastIndex) {
        int nextIndex;
        if (lastIndex == -1)
            nextIndex = spotList.indexOf(spotList.get(currIndex).leftSpot);
        else
            nextIndex = spotList.indexOf(spotList.get(currIndex).leftSpot) == lastIndex ? spotList.indexOf(spotList.get(currIndex).rightSpot) : spotList.indexOf(spotList.get(currIndex).leftSpot);
        if (nextIndex == -1)
            return currIndex;
        else
            return getOneEndIndex(spotList, nextIndex, currIndex);
    }
}

//a class for storing the distance between spotList.get(i) and spotList.get(j)
class SortedDistance {
    public int distance;
    public int i;
    public int j;
}

