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

    public static String executeBasicSettings(String cityName, ArrayList<HashMap<String, String>> spots, int totalDay, String trafficInfo, String dietInfo, Activity activity) {
        RouteData.city = cityName;
        RouteData.trafficInfo = trafficInfo;
        RouteData.dietInfo = dietInfo;
        final ArrayList<RouteData.SpotTemp> spotList = new ArrayList<>(spots.size());
        int[] position = new int[spots.size()]; // the position each item in spotList should be sorted by time
        int totalVisitTime = 0;
        for (int i = 0; i < spots.size(); i++) {
            RouteData.SpotTemp spot = new RouteData.SpotTemp();
            spot.detail = spots.get(i).get("name");
            spot.address = spots.get(i).get("address");
            spot.recommendTime = RouteMakerService.getVisitTime(spot.detail, activity);
            spot.period = -1;
            spot.leftSpot = -1;
            spot.rightSpot = -1;
            spot.combinedVisitTime = spot.recommendTime;
            spot.type = RouteData.ActivityType.SPOT;
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
                            distance[i1][j1].put("bus route", result.getRouteLines().get(0).getAllStep());
                            distance[i1][j1].put("bus time", result.getRouteLines().get(0).getDuration() / 60);
                        }
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
        if (combinedSpotNum > totalDay)
            RouteData.warning = "TooBusy";
//        double averageTime = (double) totalVisitTime / (double) totalDay; //average time for trip in one day, it is used for arranging the plan in a balance
//
//        Collections.sort(spotList, new Comparator<RouteData.SpotTemp>() {
//            @Override
//            public int compare(RouteData.SpotTemp lhs, RouteData.SpotTemp rhs) {
//                return ((Integer) lhs.recommendTime).compareTo((Integer) rhs.recommendTime);
//            }
//        });
//        if (totalDay >= spotList.size()) {
//            for (int i = 0; i < position.length; i++) {
//                position[i] = i + 1;
//            }
//            RouteData.spotSettingsHint = "NotBusy";
//        } else {
//            int index = spotList.size() - 1;
//            int day = 1;
//            while (spotList.get(index).recommendTime >= averageTime) {
//                index--;
//                position[spotList.size() - index] = day;
//                day++;
//                totalVisitTime -= spotList.get(index).recommendTime;
//            }
//            int plecesLeft = spotList.size() - day + 1;
//            int daysLeft = totalDay - day + 1;
//            double newAverageVisitTime = totalVisitTime / daysLeft;
//            while (plecesLeft > 0) {
//                double averageSpotPerDay = (double) (plecesLeft) / (double) (day);
//                int placesCountInOneDay = 1;
//                int totalVisitTimeInOneDay = 0;
//                while ((double) placesCountInOneDay < averageSpotPerDay) {
//                    placesCountInOneDay++;
//                    while (totalVisitTimeInOneDay != -1) {
//                        totalVisitTimeInOneDay = addNextSpotForOneDay(index, placesCountInOneDay, averageSpotPerDay, position, spotList, totalVisitTimeInOneDay, newAverageVisitTime);
//                    }
//                }
//            }
//        }
//        //sort the spots in random order
//        int[] temp = new int[spotList.size()];
//        for (int i = 0; i < spotList.size(); i++) {
//            temp[i] = i + 1;
//        }
//        Random random = new Random();
//        for (int i = 0; i < spotList.size(); i++) {
//            int p = random.nextInt(spotList.size());
//            int tmp = temp[i];
//            temp[i] = temp[p];
//            temp[p] = tmp;
//        }
//        for (int i = 0; i < position.length; i++) {
//            position[i] = position[temp[i]];
//        }
//        //get a hotel
//
//        //unit : minute
//        final int morningVisitTimeMax = 180;
//        final int afternoonVisitTimeMax = 240;
//        final int eveningVisitTimeMax = 180;
//        RouteData.setSpotTempInfoInstance(spotList.size() + totalDay, 3 * totalDay);
//        int spotTempInfoIndex = 0;
//
//        for (int i = 0; i < totalDay; i++) {
//            ArrayList<Integer> spotIndex = new ArrayList<>();
//            for (int j = 0; j < spotList.size(); j++) {
//                if (position[j] == i)
//                    spotIndex.add(j);
//            }
//            //add spots
//            spotTempInfoIndex = addSpotsToAPeriod(spotIndex, spotList, morningVisitTimeMax, 3 * i, spotTempInfoIndex);
//            spotTempInfoIndex = addSpotsToAPeriod(spotIndex, spotList, afternoonVisitTimeMax, 3 * i + 1, spotTempInfoIndex);
//            spotTempInfoIndex = addSpotsToAPeriod(spotIndex, spotList, eveningVisitTimeMax, 3 * i + 2, spotTempInfoIndex);
//            //add hotel
//            RouteData.spotTempInfo[spotTempInfoIndex].setSpotTemp(RouteData.ActivityType.ACCOMMODATION, 3 * i + 2, RouteData.hotelInfo.name, 0);
//            spotTempInfoIndex++;
//        }
        return success;
    }

    public static String regenerateSpotSettings() {
        return success;
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
                while (RouteData.spotTempInfo[index].period <= 3 * i || !RouteData.spotTempInfo[index].type.equals(RouteData.ActivityType.SPOT))
                    index++;
                RouteData.dietTempInfo[i] = RouteMakerService.getOneNearbyRestaurant(3 * i, RouteData.spotTempInfo[index].latitude, RouteData.spotTempInfo[index].longitude);
            } else {
                RouteData.dietTempInfo[i] = new RouteData.DietTemp("无");
            }
            if (lunch) {
                int previousIndex = index;
                while (RouteData.spotTempInfo[index].period <= 3 * i + 1 || !RouteData.spotTempInfo[index].type.equals(RouteData.ActivityType.SPOT)) {
                    if (RouteData.spotTempInfo[index].type.equals(RouteData.ActivityType.SPOT))
                        previousIndex = index;
                    index++;
                }
                RouteData.dietTempInfo[i] = RouteMakerService.getOneNearbyRestaurant(3 * i, RouteData.spotTempInfo[previousIndex].latitude, RouteData.spotTempInfo[previousIndex].longitude);
            } else {
                RouteData.dietTempInfo[i] = new RouteData.DietTemp("无");
            }
            if (dinner) {
                int previousIndex = index;
                while (RouteData.spotTempInfo[index].period <= 3 * i + 2 || !RouteData.spotTempInfo[index].type.equals(RouteData.ActivityType.SPOT)) {
                    if (RouteData.spotTempInfo[index].type.equals(RouteData.ActivityType.SPOT))
                        previousIndex = index;
                    index++;
                }
                RouteData.dietTempInfo[i] = RouteMakerService.getOneNearbyRestaurant(3 * i, RouteData.spotTempInfo[previousIndex].latitude, RouteData.spotTempInfo[previousIndex].longitude);
            } else {
                RouteData.dietTempInfo[i] = new RouteData.DietTemp("无");
            }
        }
        return success;
    }

    public static String executeDietSettings() {
        Clock startTime = new Clock(8, 0);
        Clock currtime = new Clock(8, 0);
        int index = 0;
        int period = -1;
        String lastLatitude = RouteData.hotelInfo.latitude, lastLongitude = RouteData.hotelInfo.longitude, lastPlace = RouteData.hotelInfo.name;
        while (index < RouteData.spotTempInfo.length) {
            RouteData.SingleEvent trafficEvent = new RouteData.SingleEvent(), otherEvent = new RouteData.SingleEvent();
            String finishLatitude, finishLongitude, finishPlace;
            if (RouteData.spotTempInfo[period].type == RouteData.ActivityType.NONE)
                index++;
            if (RouteData.spotTempInfo[period].period > period && !RouteData.dietTempInfo[RouteData.spotTempInfo[period].period].detail.equals("无")) {
                finishLatitude = RouteData.dietTempInfo[period + 1].latitude;
                finishLongitude = RouteData.dietTempInfo[period + 1].longitude;
                finishPlace = RouteData.dietTempInfo[period + 1].detail;
                otherEvent.type = RouteData.ActivityType.DIET;
                otherEvent.day = RouteData.dietTempInfo[period].period / 3 + 1;
            } else {
                finishLatitude = RouteData.spotTempInfo[index].latitude;
                finishLongitude = RouteData.spotTempInfo[index].longitude;
                finishPlace = RouteData.spotTempInfo[index].detail;
                otherEvent.type = RouteData.spotTempInfo[index].type;
                otherEvent.day = RouteData.spotTempInfo[period].period / 3 + 1;
                index++;
            }
            otherEvent.detail = finishPlace;
            HashMap<String, String> placeInfo = new HashMap<>();
            placeInfo.put("latitude", finishLatitude);
            placeInfo.put("longitude", finishLongitude);
            otherEvent.latitudeAndLongitude.add(placeInfo);

            if (RouteData.spotTempInfo[period].period > period)
                period++;
            //TODO:根据起终点信息和RouteData.trafficInfo确定线路 将其封装在trafficEvent里
            RouteData.singleEvents.add(trafficEvent);
            //TODO:设置otherEvent的startTime，finishTime
            RouteData.singleEvents.add(otherEvent);
        }
        return success;
    }

    public static String executeTimeSettings() {
        return success;
    }

    public static String executeFinishSettings() {
        return success;
    }

//    private static int addNextSpotForOneDay(int index, int spotNo, double averageSpotNo, int[] position, ArrayList<RouteData.SpotTemp> spots, int totalVisitTime, double newAverageVisitTime) {
//        int direction = (spotNo % 2 == 1) ? -1 : 1;
//        int start = (spotNo % 2 == 1) ? index : 0;
//        if (Math.abs((double) spotNo - averageSpotNo) > 1.0000000) {
//            while (position[start] == 0)
//                start = start + direction;
//            position[start] = spots.size() - index;
//            totalVisitTime += spots.get(start).recommendTime;
//            return totalVisitTime;
//        } else {
//            double offset;
//            int lastIndex;
//            do {
//                while (position[start] == 0)
//                    start = start + direction;
//                offset = (double) spots.get(start).recommendTime - newAverageVisitTime;
//                lastIndex = start;
//                start = start + direction;
//            }
//            while ((spots.get(start).recommendTime - newAverageVisitTime) * offset > 0);
//            if (Math.abs((double) spots.get(lastIndex).recommendTime - newAverageVisitTime) < Math.abs((double) spots.get(lastIndex).recommendTime - newAverageVisitTime))
//                position[lastIndex] = spots.size() - index;
//            else
//                position[start] = spots.size() - index;
//            return -1;
//        }
//    }
//
//    private static int addSpotsToAPeriod(ArrayList<Integer> spotTempInfoIndex, ArrayList<RouteData.SpotTemp> spotList, int maxTime, int period, int index) {
//        int usedTime = 0;
//        RouteData.spotTempInfo[index].detail = "无";
//        RouteData.spotTempInfo[index].period = period;
//        index++;
//        while (spotTempInfoIndex.size() > 0) {
//            int indexToRemove = 0;
//            boolean flag = false;
//            for (int i = 0; i < spotTempInfoIndex.size(); i++) {
//                int spotTime = spotList.get(spotTempInfoIndex.get(i)).recommendTime;
//                if (spotTime + usedTime < maxTime) {
//                    flag = true;
//                    indexToRemove = i;
//                    break;
//                }
//            }
//            if (!flag)
//                return period + 1;
//            spotList.get(spotTempInfoIndex.get(indexToRemove)).period = period;
//            RouteData.spotTempInfo[index] = spotList.get(spotTempInfoIndex.get(indexToRemove));
//            index++;
//            spotTempInfoIndex.remove(indexToRemove);
//        }
//        return index;
//    }

    public static boolean combineTwoSpots(SortedDistance[] sortedDistances, ArrayList<RouteData.SpotTemp> spotList, int index) {
        final int maxVisitTime = 480;
        int i = sortedDistances[index].i, j = sortedDistances[index].j;
        int combinedTime = spotList.get(i).recommendTime + spotList.get(j).recommendTime + sortedDistances[index].distance;
        if (combinedTime <= maxVisitTime) {
            if (spotList.get(i).rightSpot != -1 || spotList.get(j).rightSpot != -1)
                return false;
            if (spotList.get(i).leftSpot == -1)
                spotList.get(i).leftSpot = j;
            else
                spotList.get(i).rightSpot = j;
            if (spotList.get(j).leftSpot == -1)
                spotList.get(j).leftSpot = i;
            else
                spotList.get(j).rightSpot = i;
            updateCombinedTime(i, j, spotList, combinedTime);
            updateCombinedTime(j, i, spotList, combinedTime);
        }
        return true;
    }

    public static void updateCombinedTime(int currIndex, int lastIndex, ArrayList<RouteData.SpotTemp> spotList, int combinedTime) {
        spotList.get(currIndex).recommendTime += combinedTime;
        int nextIndex = spotList.get(currIndex).leftSpot == lastIndex ? spotList.get(currIndex).rightSpot : spotList.get(currIndex).leftSpot;
        if (nextIndex != -1)
            updateCombinedTime(nextIndex, currIndex, spotList, combinedTime);
    }
}

//a class for storing the distance between spotList.get(i) and spotList.get(j)
class SortedDistance {
    public int distance;
    public int i;
    public int j;
}

