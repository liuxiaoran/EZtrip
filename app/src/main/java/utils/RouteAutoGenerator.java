package utils;

import android.app.Activity;

import com.eztrip.model.RouteData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

/**
 * Created by Steve on 2015/2/25.
 * 在每一步结束之后处理数据
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


        return success;
    }

    public static String regenerateSpotSettings() {
        return success;
    }

    public static String executeSpotSettings() {
        return success;
    }

    public static String executeDietSettings() {
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
