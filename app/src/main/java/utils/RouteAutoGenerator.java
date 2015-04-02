package utils;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
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
import com.eztrip.model.ScenerySpot;
import com.eztrip.model.TravelBag;
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
    public static GeoCoder[] gSearch;
    //return an ArrayList of SpotTemp based on spots
    public static ArrayList<RouteData.SpotTemp> executeBasicSettings(String cityName, ArrayList<ScenerySpot> spots, int totalDay, String trafficInfo, String dietInfo, final Context context) {
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
            //TODO: 下面被注释掉的的是正式的做法
            //           spot.setSpotTemp(RouteData.ActivityType.SPOT, -1,TravelBag.getInstance().getScenerySpotList().get(i).title, RouteMakerService.getVisitTime(TravelBag.getInstance().getScenerySpotList().get(i).title, activity), TravelBag.getInstance().getScenerySpotList().get(i).address,TravelBag.getInstance().getScenerySpotList().get(i));
            spot.setSpotTemp(RouteData.ActivityType.SPOT, -1, spots.get(i).getTitle(), 120, spots.get(i).getAddress(), spots.get(i));
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
    //get traffic time between every two spots and a hotel information
    public static void getSpotTimeAndHotel(final RouteMakerFragment.MyHandler handler, ArrayList<RouteData.SpotTemp> spotList, final Context context) {
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
                            if (RouteData.trafficInfo.equals(context.getResources().getString(R.string.routemaker_trafficsettings_public))) {
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
                            if (RouteData.trafficInfo.equals(context.getResources().getString(R.string.routemaker_trafficsettings_private))) {
                                Message m = new Message();
                                Bundle b = new Bundle();
                                b.putBoolean("minus", true);
                                b.putString("source", "basic");
                                m.setData(b);
                                handler.handleMessage(m);
                            }
                        }
                    }
                };
                kSearch[i1][j1].setOnGetRoutePlanResultListener(listener);
                PlanNode stNode = PlanNode.withCityNameAndPlaceName(RouteData.city, spotList.get(i).detail);
                PlanNode enNode = PlanNode.withCityNameAndPlaceName(RouteData.city, spotList.get(j).detail);
                if (RouteData.trafficInfo.equals(context.getResources().getString(R.string.routemaker_trafficsettings_private))) {
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
    //try to combine the nearest spots into a plan until the number of the plan is equal to the number of the date
    public static String generateSpotSettingsPlan(ArrayList<RouteData.SpotTemp> spotList, Context context) {
        Log.e("length", Integer.toString(spotList.size()));
        for (int i = 0; i < spotList.size() - 1; i++) {
            for (int j = i + 1; j < spotList.size(); j++) {
                if (RouteData.trafficInfo.equals(context.getResources().getString(R.string.routemaker_trafficsettings_public))) {
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
        for (int i = 0; i < sortedDistances.length; i++)
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
            if (combineTwoSpots(sortedDistances, spotList, i, 1))
                combinedSpotNum--;
        }
        int spotIndex = -1;
        if (combinedSpotNum > RouteData.dayLength) {
            RouteData.warning = "TooBusy";
            for (int i = 0; i < spotList.size(); i++) {
                spotList.get(i).leftSpot = null;
                spotList.get(i).rightSpot = null;
                spotList.get(i).combinedVisitTime = spotList.get(i).recommendTime;
            }
            combinedSpotNum = spotList.size();
            for (int i = 0; i < sortedDistances.length; i++) {
                if (combinedSpotNum <= RouteData.dayLength)
                    break;
                if (combineTwoSpots(sortedDistances, spotList, i, (double) combinedSpotNum / (double) RouteData.dayLength))
                    combinedSpotNum--;
            }
            if (combinedSpotNum > RouteData.dayLength)
                return "failure";
        }
        ArrayList<Integer> visitedSpotIndex = new ArrayList<>();
        for (int i = 0; i < spotList.size(); i++) {
            Log.e("ex", spotList.get(i).detail);
        }
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

            //try to distribute the spots into periods in balance
        }
        Collections.sort(RouteData.spotTempInfo, new Comparator<RouteData.SpotTemp>() {
            @Override
            public int compare(RouteData.SpotTemp lhs, RouteData.SpotTemp rhs) {
                return lhs.period - rhs.period;
            }
        });
        int spotSum = 0;
        for(int i = 0; i < RouteData.spotTempInfo.size(); i++) {
            if(RouteData.spotTempInfo.get(i).type.equals(RouteData.ActivityType.SPOT))
                spotSum++;
        }
        int averagePeriodSpotSum = spotSum / RouteData.dayLength / 2;
        int period = RouteData.dayLength * 3 - 2;
        int currentPeriodSpotSum = 0;
        Log.e("average",Integer.toString(averagePeriodSpotSum));
        for(int i = RouteData.spotTempInfo.size() - 1; i >= 0; i--) {
            if(period == 0)
                break;
            if(RouteData.spotTempInfo.get(i).type.equals(RouteData.ActivityType.SPOT)) {
                RouteData.spotTempInfo.get(i).period = period;
                currentPeriodSpotSum++;
                if(currentPeriodSpotSum == averagePeriodSpotSum) {
                    period = getLastPeriod(period);
                    currentPeriodSpotSum = 0;
                    Log.e("period",Integer.toString(period));
                }
                if(period == 0) break;
            }
        }
        return success;
    }

    private static int getLastPeriod(int currPeriod) {
        if(currPeriod % 3 ==0)
            return currPeriod - 2;
        else
            return currPeriod - 1;
    }
    //try to divide the plan of each day into at least two periods(morning afternoon) or three(add evening)
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
    //deprecated
    public static ArrayList<RouteData.SpotTemp> regenerateSpotSettings(Context context) {
        ArrayList<ScenerySpot> spots = TravelBag.getInstance().getScenerySpotList();
        return executeBasicSettings(RouteData.city, spots, RouteData.dayLength, RouteData.trafficInfo, RouteData.dietInfo, context);
    }
    //try to find a nearest restaurant of the spot at meal times
    public static String executeSpotSettings(Context context, RouteMakerFragment.MyHandler handler) {
        boolean breakfast = RouteData.dietInfo.contains(context.getResources().getString(R.string.routemaker_dietsettings_breakfast));
        boolean lunch = RouteData.dietInfo.contains(context.getResources().getString(R.string.routemaker_dietsettings_lunch));
        boolean dinner = RouteData.dietInfo.contains(context.getResources().getString(R.string.routemaker_dietsettings_dinner));
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
        while(!RouteData.spotTempInfo.get(index).type.equals(RouteData.ActivityType.SPOT))
            index++;
        String lastLatitude = RouteData.hotelInfo.latitude, lastLongitude = RouteData.hotelInfo.longitude;
        //try to give one advice for each diet, according to the position of its adjusting activities
        for (int i = 0; i < RouteData.dayLength; i++) {
            if (breakfast) {
                while (RouteData.spotTempInfo.get(index).period < 3 * i) {
                    index++;
                }
                RouteMakerService.getOneNearbyRestaurant(3 * i, RouteData.hotelInfo.latitude, RouteData.hotelInfo.longitude, handler);
            } else {
                RouteData.dietTempInfo[3 * i] = new RouteData.DietTemp("无", 3 * i);
            }
            Log.e("details", RouteData.spotTempInfo.get(index).period + " " + RouteData.spotTempInfo.get(index).detail);
            if (lunch) {
                int previousIndex = 0;
                while (RouteData.spotTempInfo.get(index).period < 3 * i + 1) {
                    if (RouteData.spotTempInfo.get(index).type.equals(RouteData.ActivityType.SPOT))
                        previousIndex = index;
                    Log.e("previousIndex", Integer.toString(previousIndex));
                    index++;
                    if(index == RouteData.spotTempInfo.size())
                        break;
                }
                if(previousIndex != 0) {
                    lastLatitude = RouteData.spotTempInfo.get(previousIndex).latitude;
                    lastLongitude =  RouteData.spotTempInfo.get(previousIndex).longitude;
                }
                RouteMakerService.getOneNearbyRestaurant(3 * i + 1,lastLatitude, lastLongitude, handler);
            } else {
                RouteData.dietTempInfo[3 * i + 1] = new RouteData.DietTemp("无", 3 * i + 1);
            }
            if (dinner) {
                int previousIndex = 0;
                while (RouteData.spotTempInfo.get(index).period < 3 * i + 2) {
                    if (RouteData.spotTempInfo.get(index).type.equals(RouteData.ActivityType.SPOT))
                        previousIndex = index;
                    index++;
                }
                if(previousIndex != 0) {
                    lastLatitude = RouteData.spotTempInfo.get(previousIndex).latitude;
                    lastLongitude = RouteData.spotTempInfo.get(previousIndex).longitude;
                }else  {
                    while (RouteData.spotTempInfo.get(index).period <= 3 * i + 2) {
                        if (RouteData.spotTempInfo.get(index).type.equals(RouteData.ActivityType.SPOT) || RouteData.spotTempInfo.get(index).type.equals(RouteData.ActivityType.ACCOMMODATION)) {
                            lastLatitude = RouteData.spotTempInfo.get(index).latitude;
                            lastLongitude = RouteData.spotTempInfo.get(index).longitude;
                            break;
                        }
                    }
                }
                RouteMakerService.getOneNearbyRestaurant(3 * i + 2, lastLatitude, lastLongitude, handler);
            } else {
                RouteData.dietTempInfo[3 * i + 2] = new RouteData.DietTemp("无", 3 * i + 2);
            }
            lastLatitude = RouteData.hotelInfo.latitude;
            lastLongitude = RouteData.hotelInfo.longitude;
        }
        Message m = new Message();
        Bundle b = new Bundle();
        b.putBoolean("minus", true);
        b.putString("source", "spot");
        m.setData(b);
        handler.handleMessage(m);
        return success;
    }

    //try to set the visiting plan and the diet plan into an ArrayList of SingleEvents
    public static String executeDietSettings() {
        Clock startTime = new Clock(8, 0);
        final Clock[] currTime = new Clock[1];
        currTime[0] = startTime;
        int spotIndex = 0;
        int dietIndex = 0;
        RouteData.setSingleEventsInstance();
        ArrayList<RouteData.SpotTemp> spotTempArrayList = (ArrayList<RouteData.SpotTemp>) RouteData.spotTempInfo.clone();
        int size = spotTempArrayList.size();
        for (int i = 0; i < size; i++) {
            if (spotTempArrayList.get(i).type.equals(RouteData.ActivityType.NONE) || spotTempArrayList.get(i).detail.equals("无")) {
                spotTempArrayList.remove(i);
                size--;
                i--;
            }
        }
        for (int i = 0; i < spotTempArrayList.size(); i++) {
            if(spotTempArrayList.get(i).type.equals(RouteData.ActivityType.SPOT))
                RouteData.spotTempPeriodItemCount[spotTempArrayList.get(i).period]++;
        }
        while (RouteData.dietTempInfo[dietIndex].detail.equals("无")) {
            dietIndex++;
            if(dietIndex == RouteData.dietTempInfo.length)
                break;
        }
        while (spotIndex < spotTempArrayList.size()) {
            final RouteData.SingleEvent trafficEvent = new RouteData.SingleEvent(), otherEvent = new RouteData.SingleEvent();
            otherEvent.locationInfo = new ArrayList<>();
            String finishLatitude, finishLongitude, finishPlace, finishAddress;
            Log.e("dietIndex",Integer.toString(dietIndex));
            Log.e("spotIndex + period",Integer.toString(spotIndex) + Integer.toString(spotTempArrayList.get(spotIndex).period));
            if (spotTempArrayList.get(spotIndex).period >= dietIndex && !RouteData.dietTempInfo[spotTempArrayList.get(spotIndex).period].detail.equals("无")) {
                finishLatitude = RouteData.dietTempInfo[dietIndex].latitude;
                finishLongitude = RouteData.dietTempInfo[dietIndex].longitude;
                finishPlace = RouteData.dietTempInfo[dietIndex].detail;
                otherEvent.type = RouteData.ActivityType.DIET;
                otherEvent.day = RouteData.dietTempInfo[dietIndex].period / 3;
                otherEvent.moreInfo = RouteData.dietTempInfo[dietIndex];
                otherEvent.period = dietIndex;
                finishAddress = RouteData.dietTempInfo[dietIndex].address;
                otherEvent.timeLength = 60;
                do {
                    dietIndex++;
                    if(dietIndex == RouteData.dietTempInfo.length)
                        break;
                } while (RouteData.dietTempInfo[dietIndex].detail.equals("无"));
            } else {
                finishLatitude = spotTempArrayList.get(spotIndex).latitude;
                finishLongitude = spotTempArrayList.get(spotIndex).longitude;
                finishPlace = spotTempArrayList.get(spotIndex).detail;
                otherEvent.type = spotTempArrayList.get(spotIndex).type;
                otherEvent.day = spotTempArrayList.get(spotIndex).period / 3;
                otherEvent.period = spotTempArrayList.get(spotIndex).period;
                finishAddress = spotTempArrayList.get(spotIndex).address;
                otherEvent.timeLength = spotTempArrayList.get(spotIndex).recommendTime;
                if (spotTempArrayList.get(spotIndex).type.equals(RouteData.ActivityType.SPOT))
                    otherEvent.moreInfo = spotTempArrayList.get(spotIndex).scenerySpot;
                else
                    otherEvent.moreInfo = RouteData.hotelInfo;
                Log.e("event", spotTempArrayList.get(spotIndex).detail + (spotTempArrayList.get(spotIndex).period));
                spotIndex++;
            }

            otherEvent.detail = finishPlace;
            HashMap<String, String> placeInfo = new HashMap<>();
            placeInfo.put("latitude", finishLatitude);
            placeInfo.put("longitude", finishLongitude);
            placeInfo.put("address", finishAddress);
            otherEvent.locationInfo.add(placeInfo);

            trafficEvent.type = RouteData.ActivityType.TRAFFIC;
            trafficEvent.detail = "traffic";
            RouteData.singleEvents.add(trafficEvent);
            RouteData.singleEvents.add(otherEvent);
        }
        for (int i = 0; i < RouteData.singleEvents.size(); i++) {
            Log.e("singleevents", RouteData.singleEvents.get(i).detail);
        }
        return success;
    }

    //get the latitude and the longitude of the spot
    public static void getLatLngInfo(final RouteMakerFragment.MyHandler handler, final Context context) {
        gSearch = new GeoCoder[handler.getCount()];
        int geoIndex = -1;
        for (int i = 0; i < RouteData.spotTempInfo.size(); i++) {
            if (!RouteData.spotTempInfo.get(i).type.equals(RouteData.ActivityType.NONE)) {
                geoIndex++;
                final int index = i;
                gSearch[geoIndex] = GeoCoder.newInstance();
                gSearch[geoIndex].setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
                    @Override
                    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
                        if (geoCodeResult == null || geoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                            Toast.makeText(context, "错误：未定位到地点。", Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            RouteData.spotTempInfo.get(index).latitude = Double.toString(geoCodeResult.getLocation().latitude);
                            RouteData.spotTempInfo.get(index).longitude = Double.toString(geoCodeResult.getLocation().longitude);
                            if (RouteData.spotTempInfo.get(index).type.equals(RouteData.ActivityType.ACCOMMODATION)) {
                                RouteData.hotelInfo.longitude = Double.toString(geoCodeResult.getLocation().longitude);
                                RouteData.hotelInfo.latitude = Double.toString(geoCodeResult.getLocation().latitude);
                            }
                        }
                        Message m = new Message();
                        Bundle b = new Bundle();
                        b.putBoolean("minus", true);
                        b.putString("source", "latlnginfo");
                        m.setData(b);
                        handler.handleMessage(m);
                    }

                    @Override
                    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {

                    }
                });
                gSearch[geoIndex].geocode(new GeoCodeOption().city(RouteData.city).address(RouteData.spotTempInfo.get(i).address));
            }
        }
    }

    //determine the traffic time between every two singleEvent
    public static void getTrafficTimes(Context context, RouteMakerFragment.MyHandler handler) {
        Log.e("length", Integer.toString(RouteData.singleEvents.size()));
        int index = 0;
        String startLatitude, startLongitude, finishLatitude, finishLongitude;
        while (index != -1) {
            while (index < RouteData.singleEvents.size() && !RouteData.singleEvents.get(index).type.equals(RouteData.ActivityType.TRAFFIC)) {
                index++;
            }
            if (index == RouteData.singleEvents.size())
                break;
            String startDetail;
            int relatedPeriod;//the period the traffic event should be in
            if (index == 0) {
                startLatitude = RouteData.hotelInfo.latitude;
                startLongitude = RouteData.hotelInfo.longitude;
                startDetail = RouteData.hotelInfo.name;
                relatedPeriod = 0;
            } else {
                startLatitude = RouteData.singleEvents.get(index - 1).locationInfo.get(0).get("latitude");
                startLongitude = RouteData.singleEvents.get(index - 1).locationInfo.get(0).get("longitude");
                startDetail = RouteData.singleEvents.get(index - 1).detail;
                relatedPeriod = (RouteData.singleEvents.get(index - 1).period == RouteData.singleEvents.get(index + 1).period ? RouteData.singleEvents.get(index - 1).period : (RouteData.singleEvents.get(index - 1).type.equals(RouteData.ActivityType.ACCOMMODATION) ? RouteData.singleEvents.get(index + 1).period : RouteData.singleEvents.get(index - 1).period));
            }
            finishLatitude = RouteData.singleEvents.get(index + 1).locationInfo.get(0).get("latitude");
            finishLongitude = RouteData.singleEvents.get(index + 1).locationInfo.get(0).get("longitude");
            RouteData.singleEvents.get(index).detail = startDetail + "-" + RouteData.singleEvents.get(index + 1).detail;
            RouteData.singleEvents.get(index).locationInfo = new ArrayList<>();
            HashMap<String, String> startPlaceInfo = new HashMap<>();
            startPlaceInfo.put("address", startDetail);
            RouteData.singleEvents.get(index).locationInfo.add(startPlaceInfo);
            HashMap<String, String> finishPlaceInfo = new HashMap<>();
            finishPlaceInfo.put("address", RouteData.singleEvents.get(index + 1).detail);
            RouteData.singleEvents.get(index).locationInfo.add(finishPlaceInfo);
            RouteData.singleEvents.get(index).period = relatedPeriod;
            Log.e("zi", startLatitude + " " + startLongitude + " " + finishLatitude + " " + finishLongitude);
            index = getSingleTrafficTime(context, handler, index, startLatitude, startLongitude, finishLatitude, finishLongitude, relatedPeriod);
        }
        Message m = new Message();
        Bundle b = new Bundle();
        b.putBoolean("minus", true);
        b.putString("source", "diet");
        m.setData(b);
        handler.handleMessage(m);
    }

    public static int getSingleTrafficTime(final Context context, final RouteMakerFragment.MyHandler handler, int index, final String startLatitude, final String startLongitude, final String finishLatitude, final String finishLongitude, final int relatedPeriod) {
        Log.e("index", Integer.toString(index));
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
                if (RouteData.trafficInfo.equals(context.getResources().getString(R.string.routemaker_trafficsettings_public))) {
//                    RouteData.singleEvents.get(trafficIndex).detail = result.getRouteLines().get(0).getAllStep().toString();
                    if( result.getRouteLines()!= null) {
                        int duration = result.getRouteLines().get(0).getDuration() / 60;
                        RouteData.trafficTimeOccupied[relatedPeriod] += duration;
                        RouteData.singleEvents.get(trafficIndex).timeLength = duration;
                        Log.e("duration",Integer.toString(RouteData.singleEvents.get(trafficIndex).timeLength) + " " + Integer.toString(trafficIndex));
                        RouteData.singleEvents.get(trafficIndex).transitRouteLine = result.getRouteLines().get(0);
                    }else {
                        RouteData.singleEvents.get(trafficIndex).timeLength = 0;
                        RouteData.singleEvents.get(trafficIndex).transitRouteLine = null;
                    }
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
                    if (RouteData.trafficInfo.equals(context.getResources().getString(R.string.routemaker_trafficsettings_private))) {
//                        RouteData.singleEvents.get(trafficIndex).detail = result.getRouteLines().get(0).getAllStep().toString();
                        if( result.getRouteLines()!= null) {
                            int duration = result.getRouteLines().get(0).getDuration() / 60;
                            RouteData.trafficTimeOccupied[relatedPeriod] += duration;
                            RouteData.singleEvents.get(trafficIndex).timeLength = duration;
                            Log.e("duration",Integer.toString(RouteData.singleEvents.get(trafficIndex).timeLength) + " " + Integer.toString(trafficIndex));
                            RouteData.singleEvents.get(trafficIndex).drivingRouteLine = result.getRouteLines().get(0);
                        }else {
                            RouteData.singleEvents.get(trafficIndex).timeLength = 0;
                            RouteData.singleEvents.get(trafficIndex).transitRouteLine = null;
                        }
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
        PlanNode stNode = PlanNode.withLocation(new LatLng(Double.parseDouble(startLatitude), Double.parseDouble(startLongitude)));
        PlanNode enNode = PlanNode.withLocation(new LatLng(Double.parseDouble(finishLatitude), Double.parseDouble(finishLongitude)));
        if (RouteData.trafficInfo.equals(context.getResources().getString(R.string.routemaker_trafficsettings_private))) {
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
        setVisitTime();
        final Clock startTime = new Clock(8, 0);
        Clock currTime = startTime;
        int index = 0, currDay = 0;
        while (index < RouteData.singleEvents.size()) {
            Log.e(currTime.toString(), Integer.toString(RouteData.singleEvents.get(index).timeLength));
            if (RouteData.singleEvents.get(index).day > currDay)
                currTime = startTime;
            RouteData.singleEvents.get(index).startTime = new Clock(currTime);
            RouteData.singleEvents.get(index).finishTime = new Clock(currTime.add(RouteData.singleEvents.get(index).timeLength));
            currTime = new Clock(RouteData.singleEvents.get(index).finishTime);
            index++;
        }
    }

    private static void setVisitTime() {
        for(int i = 0; i < RouteData.singleEvents.size(); i++) {
            Log.e("re",Integer.toString(RouteData.singleEvents.get(i).timeLength));
        }
//        int singleEventsIndex = 0;
//        for(int i = 0; i < RouteData.spotTempPeriodItemCount.length; i++) {
//            if(RouteData.spotTempPeriodItemCount[i] > 0) {
//                int averageVisitTime = (getMaxPeriodTime(i) - RouteData.trafficTimeOccupied[i]) /  RouteData.spotTempPeriodItemCount[i];
//                while ( RouteData.spotTempPeriodItemCount[i] > 0) {
//                    while (RouteData.singleEvents.get(singleEventsIndex).period == i && RouteData.singleEvents.get(singleEventsIndex).type.equals(RouteData.ActivityType.SPOT))
//                        singleEventsIndex++;
//                    RouteData.spotTempPeriodItemCount[i]--;
//                    RouteData.singleEvents.get(singleEventsIndex).timeLength = averageVisitTime;
//                }
//            }
//        }
    }

    private static int getMaxPeriodTime(int period) {
        final int morningTime = 240, afternoonTime = 360, eveningTime = 240;
        switch (period % 3) {
            case 0:
                return morningTime;
            case 1:
                return afternoonTime;
            case 2:
                return eveningTime;
        }
        return 0;
    }

    public static String executeFinishSettings(final RouteMakerFragment.MyHandler handler, final Context context, String startTime, String name) {
        EztripHttpUtil eztripHttpUtil = new EztripHttpUtil();
        AsyncHttpResponseHandler asyncHttpResponseHandler = new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Toast.makeText(context, "线路生成成功，您可以在“行程规划”中查看生成的线路", Toast.LENGTH_LONG).show();
                Message m = new Message();
                Bundle b = new Bundle();
                b.putBoolean("minus", true);
                b.putString("source", "finish");
                b.putBoolean("success", true);
                m.setData(b);
                handler.handleMessage(m);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(context, "连接服务器失败", Toast.LENGTH_LONG).show();
                Message m = new Message();
                Bundle b = new Bundle();
                b.putBoolean("minus", true);
                b.putString("source", "finish");
                b.putBoolean("success", false);
                m.setData(b);
                handler.handleMessage(m);
            }
        };
        JSONObject parameter = new JSONObject();
        try {
            //TODO: id来源
            parameter.put("id", 1);
            parameter.put("name", name);
            parameter.put("city", RouteData.city);
            parameter.put("start_date", startTime);
            parameter.put("day_length", RouteData.dayLength);
            List<Map> eventList = new ArrayList<>();
            for (int i = 0; i < RouteData.singleEvents.size(); i++) {
                Map<String, Object> map = new HashMap<>();
                map.put("day", RouteData.singleEvents.get(i).day);
                map.put("type", RouteData.singleEvents.get(i).type.toString().toLowerCase());
                map.put("start_time", RouteData.singleEvents.get(i).startTime);
                map.put("finish_time", RouteData.singleEvents.get(i).finishTime);
                map.put("detail", RouteData.singleEvents.get(i).detail);
                List<Map> locationList = new ArrayList<>();
                for (int j = 0; j < RouteData.singleEvents.get(i).locationInfo.size(); j++) {
                    Map<String, String> locationInfo = new HashMap<>();
                    locationInfo.put("longitude", RouteData.singleEvents.get(i).locationInfo.get(j).get("longitude"));
                    locationInfo.put("latitude", RouteData.singleEvents.get(i).locationInfo.get(j).get("latitude"));
                    locationInfo.put("address", RouteData.singleEvents.get(i).locationInfo.get(j).get("address"));
                    locationList.add(locationInfo);
                }
                map.put("places", locationList);
                eventList.add(map);
            }
            parameter.put("event", eventList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HashMap<String, String> request = new HashMap<>();
        request.put("content", parameter.toString());
        EztripHttpUtil.post(URLConstants.SUBMIT_ROUTE, request, asyncHttpResponseHandler);
        return success;
    }

    public static boolean combineTwoSpots(SortedDistance[] sortedDistances, ArrayList<RouteData.SpotTemp> spotList, int index, double times) {
        final double maxVisitTime = times * 480;
        int i = sortedDistances[index].i, j = sortedDistances[index].j;
        int combinedVisitTime = spotList.get(i).combinedVisitTime + spotList.get(j).combinedVisitTime + sortedDistances[index].distance;
        Log.e("combined", Integer.toString(combinedVisitTime) + " " + index + " " + i + " " + j);
        if (combinedVisitTime <= maxVisitTime) {
            if (spotList.get(i).rightSpot != null || spotList.get(j).rightSpot != null)
                return false;
            if (spotList.get(i).leftSpot != null && spotList.get(j).leftSpot != null) {
                int nearestIndex = spotList.indexOf(spotList.get(i).leftSpot);
                int lastIndex = i;
                while (nearestIndex != -1) {
                    int temp = spotList.indexOf(spotList.get(nearestIndex).leftSpot);
                    if (temp == lastIndex)
                        temp = spotList.indexOf(spotList.get(nearestIndex).rightSpot);
                    if (temp == j)
                        return false;
                    lastIndex = nearestIndex;
                    nearestIndex = temp;
                }
            }
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
            nextIndex = spotList.indexOf(spotList.get(currIndex).rightSpot);
        else
            nextIndex = spotList.indexOf(spotList.get(currIndex).rightSpot) == lastIndex ? spotList.indexOf(spotList.get(currIndex).leftSpot) : spotList.indexOf(spotList.get(currIndex).rightSpot);
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

