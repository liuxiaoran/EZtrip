package utils;

import android.app.Activity;

import com.eztrip.R;
import com.eztrip.model.Clock;
import com.eztrip.model.RouteData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by Steve on 2015/2/25.
 * dispose data after each step of routemaker period
 */
public class RouteAutoGenerator {
    public static final String success = "success";

    public static String executeBasicSettings(String cityName, ArrayList<String> spots, int totalDay, String trafficInfo, String dietInfo, Activity activity) {
        RouteData.city = cityName;
        RouteData.trafficInfo = trafficInfo;
        RouteData.dietInfo = dietInfo;
        ArrayList<RouteData.SpotTemp> spotList = new ArrayList<>(spots.size());
        int[] position = new int[spots.size()]; // the position each item in spotList should be sorted by time
        int totalVisitTime = 0;
        for (int i = 0; i < spots.size(); i++) {
            RouteData.SpotTemp spot = new RouteData.SpotTemp();
            spot.detail = spots.get(i);
            spot.recommendTime = RouteMakerService.getVisitTime(spot.detail, activity);
            spot.period = -1;
            spot.type = RouteData.ActivityType.SPOT;
            spotList.add(spot);
            totalVisitTime += spot.recommendTime;
        }
        double averageTime = (double) totalVisitTime / (double) totalDay; //average time for trip in one day, it is used for arranging the plan in a balance

        Collections.sort(spotList, new Comparator<RouteData.SpotTemp>() {
            @Override
            public int compare(RouteData.SpotTemp lhs, RouteData.SpotTemp rhs) {
                return ((Integer) lhs.recommendTime).compareTo((Integer) rhs.recommendTime);
            }
        });
        if (totalDay >= spotList.size()) {
            for (int i = 0; i < position.length; i++) {
                position[i] = i + 1;
            }
            RouteData.spotSettingsHint = "NotBusy";
        } else {
            int index = spotList.size() - 1;
            int day = 1;
            while (spotList.get(index).recommendTime >= averageTime) {
                index--;
                position[spotList.size() - index] = day;
                day++;
                totalVisitTime -= spotList.get(index).recommendTime;
            }
            int plecesLeft = spotList.size() - day + 1;
            int daysLeft = totalDay - day + 1;
            double newAverageVisitTime = totalVisitTime / daysLeft;
            while (plecesLeft > 0) {
                double averageSpotPerDay = (double) (plecesLeft) / (double) (day);
                int placesCountInOneDay = 1;
                int totalVisitTimeInOneDay = 0;
                while ((double) placesCountInOneDay < averageSpotPerDay) {
                    placesCountInOneDay++;
                    while (totalVisitTimeInOneDay != -1) {
                        totalVisitTimeInOneDay = addNextSpotForOneDay(index, placesCountInOneDay, averageSpotPerDay, position, spotList, totalVisitTimeInOneDay, newAverageVisitTime);
                    }
                }
            }
        }
        //sort the spots in random order
        int[] temp = new int[spotList.size()];
        for (int i = 0; i < spotList.size(); i++) {
            temp[i] = i + 1;
        }
        Random random = new Random();
        for (int i = 0; i < spotList.size(); i++) {
            int p = random.nextInt(spotList.size());
            int tmp = temp[i];
            temp[i] = temp[p];
            temp[p] = tmp;
        }
        for (int i = 0; i < position.length; i++) {
            position[i] = position[temp[i]];
        }

        //TODO; 将一天的计划分到上午、下午、晚上3个时间段，尽可能分在上午或下午
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

    private static int addNextSpotForOneDay(int index, int spotNo, double averageSpotNo, int[] position, ArrayList<RouteData.SpotTemp> spots, int totalVisitTime, double newAverageVisitTime) {
        int direction = (spotNo % 2 == 1) ? -1 : 1;
        int start = (spotNo % 2 == 1) ? index : 0;
        if (Math.abs((double) spotNo - averageSpotNo) > 1.0000000) {
            while (position[start] == 0)
                start = start + direction;
            position[start] = spots.size() - index;
            totalVisitTime += spots.get(start).recommendTime;
            return totalVisitTime;
        } else {
            double offset;
            int lastIndex;
            do {
                while (position[start] == 0)
                    start = start + direction;
                offset = (double) spots.get(start).recommendTime - newAverageVisitTime;
                lastIndex = start;
                start = start + direction;
            }
            while ((spots.get(start).recommendTime - newAverageVisitTime) * offset > 0);
            if (Math.abs((double) spots.get(lastIndex).recommendTime - newAverageVisitTime) < Math.abs((double) spots.get(lastIndex).recommendTime - newAverageVisitTime))
                position[lastIndex] = spots.size() - index;
            else
                position[start] = spots.size() - index;
            return -1;
        }
    }
}


