package utils;

import android.app.Activity;

import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by Steve on 2015/2/25.
 * dispose data after each step of routemaker period
 */
public class RouteAutoGenerator {
    public static final String success = "success";
    public static final String failure = "failure";

    public static String executeBasicSettings(String cityName, ArrayList<HashMap<String, String>> spots, int totalDay, String trafficInfo, String dietInfo, Activity activity) {
        RouteData.city = cityName;
        RouteData.trafficInfo = trafficInfo;
        RouteData.dietInfo = dietInfo;
        RouteData.basicSettingsSpot = (ArrayList<HashMap<String, String>>) spots.clone();
        final ArrayList<RouteData.SpotTemp> spotList = new ArrayList<>(spots.size());
        int[] position = new int[spots.size()]; // the position each item in spotList should be sorted by time
        int totalVisitTime = 0;
        for (int i = 0; i < spots.size(); i++) {
            RouteData.SpotTemp spot = new RouteData.SpotTemp();
            spot.setSpotTemp(RouteData.ActivityType.SPOT, -1, spots.get(i).get("name"), RouteMakerService.getVisitTime(spot.detail, activity), spots.get(i).get("address"));
            spotList.add(spot);
            totalVisitTime += spot.recommendTime;
        }
        final ArrayList<HashMap<String, Double>> spotPosition = new ArrayList<>();
        for (int i = 0; i < spotList.size(); i++) {
            final int index = i;
            GeoCoder mSearch = GeoCoder.newInstance();
            OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
                public void onGetGeoCodeResult(GeoCodeResult result) {
                    if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                        //没有检索到结果
                        //TODO:抛异常
                    }
                    //获取地理编码结果
                    HashMap<String, Double> latLngInfo = new HashMap<>();
                    latLngInfo.put("latitude", result.getLocation().latitude);
                    latLngInfo.put("longitude", result.getLocation().longitude);
                    spotPosition.add(index, latLngInfo);
                }

                @Override
                public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
                    if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                        //没有找到检索结果
                    }
                    //获取反向地理编码结果
                }
            };
            mSearch.setOnGetGeoCodeResultListener(listener);
            mSearch.geocode(new GeoCodeOption()
                    .city(RouteData.city)
                    .address(spotList.get(i).address));
            mSearch.destroy();
        }
        final HashMap[][] distance = new HashMap[spotList.size()][];
        for (int i = 0; i < spotList.size(); i++) {
            distance[i] = new HashMap[spotList.size()];
            for (int j = 0; j < spotList.size(); j++)
                distance[i][j] = new HashMap<String, Object>();
        }
        for (int i = 0; i < distance.length - 1; i++) {
            for (int j = i + 1; j < distance.length; j++) {
                final int i1 = 1, j1 = j;
                RoutePlanSearch mSearch = RoutePlanSearch.newInstance();
                OnGetRoutePlanResultListener listener = new OnGetRoutePlanResultListener() {
                    public void onGetWalkingRouteResult(WalkingRouteResult result) {
                        distance[i1][j1].put("walking route", result.getRouteLines().get(0).getAllStep());
                    }

                    public void onGetTransitRouteResult(TransitRouteResult result) {
                        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                            //TODO: 抛异常
                        }
                        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                            //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                            //result.getSuggestAddrInfo()
                        }
                        distance[i1][j1].put("bus route", result.getRouteLines().get(0).getAllStep());
                        distance[i1][j1].put("bus time", result.getRouteLines().get(0).getDuration() / 60);
                    }

                    public void onGetDrivingRouteResult(DrivingRouteResult result) {
                        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                            distance[i1][j1].put("drive route", result.getRouteLines().get(0).getAllStep());
                            distance[i1][j1].put("drive time", result.getRouteLines().get(0).getDuration() / 60);
                        }
                    }
                };
                mSearch.setOnGetRoutePlanResultListener(listener);
                PlanNode stNode = PlanNode.withCityNameAndPlaceName(RouteData.city, spotList.get(i).address);
                PlanNode enNode = PlanNode.withCityNameAndPlaceName(RouteData.city, spotList.get(j).address);
                mSearch.transitSearch((new TransitRoutePlanOption())
                        .from(stNode)
                        .city(RouteData.city)
                        .to(enNode));
                mSearch.destroy();
            }
        }
        for (int i = 0; i < distance.length - 1; i++) {
            for (int j = i + 1; j < distance.length; j++) {
                if (RouteData.trafficInfo.equals(activity.getResources().getString(R.string.routemaker_trafficsettings_public))) {
                    distance[i][j].put("route", distance[i][j].get("bus route"));
                    distance[i][j].put("time", distance[i][j].get("bus time"));
                } else {
                    distance[i][j].put("route", distance[i][j].get("drive route"));
                    distance[i][j].put("time", distance[i][j].get("drive time"));
                }
                distance[i][j].put("connection status", "no");
            }
        }

        SortedDistance[] sortedDistances = new SortedDistance[spotList.size() * (spotList.size() - 1) / 2];
        for (int i = 0; i < spotList.size(); i++)
            sortedDistances[i] = new SortedDistance();
        for (int i = 0; i < distance.length - 1; i++) {
            for (int j = i + 1; j < distance.length; j++) {
                sortedDistances[i * (i - 1) / 2 + j - i - 1].distance = (int) distance[i][j].get("time");
                sortedDistances[i * (i - 1) / 2 + j - i - 1].i = i;
                sortedDistances[i * (i - 1) / 2 + j - i - 1].j = j;
            }
        }
        Arrays.sort(sortedDistances, new Comparator<SortedDistance>() {
            @Override
            public int compare(SortedDistance lhs, SortedDistance rhs) {
                return lhs.distance - rhs.distance;
            }
        });
        int combinedSpotNum = spotList.size();
        if (combinedSpotNum <= totalDay)
            RouteData.warning = "NotBusy";
        for (int i = 0; i < sortedDistances.length; i++) {
            if (combinedSpotNum <= totalDay)
                break;
            if (combineTwoSpots(sortedDistances, spotList, i))
                combinedSpotNum--;
        }
        if (combinedSpotNum > totalDay) {
            RouteData.warning = "TooBusy";
            for (int i = 0; i < sortedDistances.length; i++) {
                if (combineTwoSpots(sortedDistances, spotList, i))
                    combinedSpotNum--;
            }
            return failure;
        }

        for (int i = 0; i < totalDay; i++) {
            int leftMostIndex = getOneEndIndex(spotList, 0, -1);
            arrangeCurrentDayPlan(spotList, leftMostIndex, i);
            RouteData.SpotTemp hotel = new RouteData.SpotTemp();
            hotel.setSpotTemp(RouteData.ActivityType.ACCOMMODATION, 3 * i + 2, RouteData.hotelInfo.name, 0, RouteData.hotelInfo.address);
            RouteData.spotTempInfo.add(hotel);
        }
        return success;
    }

    private static void arrangeCurrentDayPlan(ArrayList<RouteData.SpotTemp> spotList, int leftMostIndex, int currDay) {
        final int moringMaxVisitTime = 210, afternoonMaxVisitTime = 270;
        int maxTime = moringMaxVisitTime;
        int periodVisitTime = 0;
        int roadTime = 0;
        int periodOfTheDay = 1;
        RouteData.SpotTemp nothing = new RouteData.SpotTemp();
        nothing.period = 3 * currDay + periodOfTheDay;
        RouteData.spotTempInfo.add(nothing);
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
                RouteData.SpotTemp nothing2 = new RouteData.SpotTemp();
                nothing.period = 3 * currDay + periodOfTheDay;
                RouteData.spotTempInfo.add(nothing2);
            }
            currSpot.period = 3 * currDay + periodOfTheDay;
            RouteData.spotTempInfo.add(currSpot);
            nextSpot = currSpot.rightSpot == lastSpot ? currSpot.leftSpot : currSpot.rightSpot;
            lastSpot = currSpot;

        }
        while (nextSpot != null);
    }

    public static String regenerateSpotSettings(Activity activity, ArrayList<HashMap<String, String>> newSpots) {
        ArrayList<HashMap<String, String>> spots = (ArrayList<HashMap<String, String>>) RouteData.basicSettingsSpot.clone();
        spots.addAll(newSpots);
        return executeBasicSettings(RouteData.city, spots, RouteData.dayLength, RouteData.trafficInfo, RouteData.dietInfo, activity);
    }

    public static String executeSpotSettings(Activity activity) {
        boolean breakfast = RouteData.dietInfo.contains(activity.getResources().getString(R.string.routemaker_dietsettings_breakfast));
        boolean lunch = RouteData.dietInfo.contains(activity.getResources().getString(R.string.routemaker_dietsettings_lunch));
        boolean dinner = RouteData.dietInfo.contains(activity.getResources().getString(R.string.routemaker_dietsettings_dinner));
        RouteData.setDietTempInfoInstance(RouteData.dayLength * 3);
        int index = 0;
        //try to give one advice for each diet, according to the position of its adjusting activities
        for (int i = 0; i < RouteData.dayLength; i++) {
            if (breakfast) {
                while (RouteData.spotTempInfo.get(index).period <= 3 * i || !RouteData.spotTempInfo.get(index).type.equals(RouteData.ActivityType.SPOT))
                    index++;
                RouteData.dietTempInfo[i] = RouteMakerService.getOneNearbyRestaurant(3 * i, RouteData.spotTempInfo.get(index).latitude, RouteData.spotTempInfo.get(index).longitude);
            } else {
                RouteData.dietTempInfo[i] = new RouteData.DietTemp("无");
            }
            if (lunch) {
                int previousIndex = index;
                while (RouteData.spotTempInfo.get(index).period <= 3 * i + 1 || !RouteData.spotTempInfo.get(index).type.equals(RouteData.ActivityType.SPOT)) {
                    if (RouteData.spotTempInfo.get(index).type.equals(RouteData.ActivityType.SPOT))
                        previousIndex = index;
                    index++;
                }
                RouteData.dietTempInfo[i] = RouteMakerService.getOneNearbyRestaurant(3 * i, RouteData.spotTempInfo.get(previousIndex).latitude, RouteData.spotTempInfo.get(previousIndex).longitude);
            } else {
                RouteData.dietTempInfo[i] = new RouteData.DietTemp("无");
            }
            if (dinner) {
                int previousIndex = index;
                while (RouteData.spotTempInfo.get(index).period <= 3 * i + 2 || !RouteData.spotTempInfo.get(index).type.equals(RouteData.ActivityType.SPOT)) {
                    if (RouteData.spotTempInfo.get(index).type.equals(RouteData.ActivityType.SPOT))
                        previousIndex = index;
                    index++;
                }
                RouteData.dietTempInfo[i] = RouteMakerService.getOneNearbyRestaurant(3 * i, RouteData.spotTempInfo.get(previousIndex).latitude, RouteData.spotTempInfo.get(previousIndex).longitude);
            } else {
                RouteData.dietTempInfo[i] = new RouteData.DietTemp("无");
            }
        }
        return success;
    }

    public static String executeDietSettings(final Activity activity) {
        Clock startTime = new Clock(8, 0);
        final Clock[] currtime = new Clock[1];
        currtime[0] = new Clock(8, 0);
        int index = 0;
        int period = -1;
        String lastLatitude = RouteData.hotelInfo.latitude, lastLongitude = RouteData.hotelInfo.longitude, lastPlace = RouteData.hotelInfo.name, lastAddress = RouteData.hotelInfo.address;
        while (index < RouteData.spotTempInfo.size()) {
            final RouteData.SingleEvent trafficEvent = new RouteData.SingleEvent(), otherEvent = new RouteData.SingleEvent();
            int recommendVisitTime;
            String finishLatitude, finishLongitude, finishPlace, finishAddress;
            if (RouteData.spotTempInfo.get(index).type == RouteData.ActivityType.NONE)
                index++;
            if (RouteData.spotTempInfo.get(index).period > period && !RouteData.dietTempInfo[RouteData.spotTempInfo.get(index).period].detail.equals("无")) {
                finishLatitude = RouteData.dietTempInfo[period + 1].latitude;
                finishLongitude = RouteData.dietTempInfo[period + 1].longitude;
                finishPlace = RouteData.dietTempInfo[period + 1].detail;
                otherEvent.type = RouteData.ActivityType.DIET;
                otherEvent.day = RouteData.dietTempInfo[period + 1].period / 3 + 1;
                finishAddress = RouteData.dietTempInfo[period + 1].address;
                recommendVisitTime = 60;
            } else {
                finishLatitude = RouteData.spotTempInfo.get(index).latitude;
                finishLongitude = RouteData.spotTempInfo.get(index).longitude;
                finishPlace = RouteData.spotTempInfo.get(index).detail;
                otherEvent.type = RouteData.spotTempInfo.get(index).type;
                otherEvent.day = RouteData.spotTempInfo.get(index).period / 3 + 1;
                finishAddress = RouteData.spotTempInfo.get(index).address;
                index++;
                recommendVisitTime = RouteData.spotTempInfo.get(index).recommendTime;
            }
            otherEvent.detail = finishPlace;
            HashMap<String, String> placeInfo = new HashMap<>();
            placeInfo.put("latitude", finishLatitude);
            placeInfo.put("longitude", finishLongitude);
            otherEvent.latitudeAndLongitude.add(placeInfo);
            otherEvent.address = finishAddress;

            if (RouteData.spotTempInfo.get(index).period > period)
                period++;
            //TODO:根据起终点信息和RouteData.trafficInfo确定线路 将其封装在trafficEvent里
            RoutePlanSearch mSearch = RoutePlanSearch.newInstance();
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
                        trafficEvent.detail = result.getRouteLines().get(0).getAllStep().toString();
                        int duration = result.getRouteLines().get(0).getDuration();
                        trafficEvent.startTime = currtime[0];
                        trafficEvent.finishTime = currtime[0].add(new Clock(duration / 3600, duration / 60 % 60));
                        currtime[0] = trafficEvent.finishTime;
                    }
                }

                public void onGetDrivingRouteResult(DrivingRouteResult result) {
                    if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                        if (RouteData.trafficInfo.equals(activity.getResources().getString(R.string.routemaker_trafficsettings_private))) {
                            trafficEvent.detail = result.getRouteLines().get(0).getAllStep().toString();
                            int duration = result.getRouteLines().get(0).getDuration();
                            trafficEvent.startTime = currtime[0];
                            trafficEvent.finishTime = currtime[0].add(new Clock(duration / 3600, duration / 60 % 60));
                            currtime[0] = trafficEvent.finishTime;
                        }
                    }
                }
            };
            mSearch.setOnGetRoutePlanResultListener(listener);
            PlanNode stNode = PlanNode.withCityNameAndPlaceName(RouteData.city, lastAddress);
            PlanNode enNode = PlanNode.withCityNameAndPlaceName(RouteData.city, finishAddress);
            mSearch.transitSearch((new TransitRoutePlanOption())
                    .from(stNode)
                    .city(RouteData.city)
                    .to(enNode));
            mSearch.destroy();
            RouteData.singleEvents.add(trafficEvent);
            //TODO:设置otherEvent的startTime，finishTime
            otherEvent.startTime = currtime[0];
            currtime[0] = currtime[0].add(recommendVisitTime);
            otherEvent.finishTime = currtime[0];
            RouteData.singleEvents.add(otherEvent);
            lastAddress = finishAddress;
        }
        return success;
    }

    public static String executeTimeSettings() {
        return success;
    }

    public static String executeFinishSettings() {
        return success;
    }

    public static boolean combineTwoSpots(SortedDistance[] sortedDistances, ArrayList<RouteData.SpotTemp> spotList, int index) {
        final int maxVisitTime = 480;
        int i = sortedDistances[index].i, j = sortedDistances[index].j;
        int combinedTime = spotList.get(i).recommendTime + spotList.get(j).recommendTime + sortedDistances[index].distance;
        if (combinedTime <= maxVisitTime) {
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
            updateCombinedTime(i, j, spotList, combinedTime);
            updateCombinedTime(j, i, spotList, combinedTime);
            return true;
        } else
            return false;
    }

    public static void updateCombinedTime(int currIndex, int lastIndex, ArrayList<RouteData.SpotTemp> spotList, int combinedTime) {
        spotList.get(currIndex).recommendTime += combinedTime;
        int nextIndex = spotList.indexOf(spotList.get(currIndex).leftSpot) == lastIndex ? spotList.indexOf(spotList.get(currIndex).rightSpot) : spotList.indexOf(spotList.get(currIndex).leftSpot);
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

