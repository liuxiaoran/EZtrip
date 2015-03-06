package com.eztrip.model;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Steve on 2015/2/5.
 * 暂时存储信息
 */
public class RouteData {
    public static String destination;
    public static ArrayList<SingleEvent> singleEvents;
    public static int dayLength;
    public static Calendar startDay;
    public static String wayToTravel;
    public static String warning;

    public enum ActivityType {SPOT, DIET, TRAFFIC, ACCOMMODATION, OTHERS, NONE}

    public static void setSingleEventsInstance(int dayNum) {
        RouteData.singleEvents = new ArrayList<>();
        for (int i = 0; i < dayNum; i++)
            RouteData.singleEvents.add(new RouteData.SingleEvent());
    }

    public static class SingleEvent {
        public int day;
        public ActivityType type;
        public String startTime;//例：11:00
        public String finishTime;
        public String detail;
        public String[] places;//相关地点

        public void setSingleEvent(int day, ActivityType activityType, String startTime, String finishTime, String detail, String[] places) {
            this.day = day;
            this.type = activityType;
            this.startTime = startTime;
            this.finishTime = finishTime;
            this.detail = detail;
            this.places = places;
        }
    }


    public static SpotTemp[] spotTempInfo;
    public static int[] spotTempPeriodItemCount;

    public static void setSpotTempInfoInstance(int itemCount, int periodNum) {
        spotTempInfo = new SpotTemp[itemCount + periodNum];
        spotTempPeriodItemCount = new int[periodNum];
        for (int i = 0; i < itemCount + periodNum; i++)
            RouteData.spotTempInfo[i] = new RouteData.SpotTemp();
    }

    public static class SpotTemp {
        //根据已有SpotTemp实例构建新的，spotTempPeriodItemCount不改变
        public SpotTemp(SpotTemp spotTemp) {
            this.type = spotTemp.type;
            this.period = spotTemp.period;
            this.detail = spotTemp.detail;
            //spotTempPeriodItemCount[period]++;
        }

        public SpotTemp() {
        }

        public void setSpotTemp(ActivityType activityType, int period, String detail) {
            this.type = activityType;
            this.period = period;
            this.detail = detail;
            spotTempPeriodItemCount[period]++;
        }

        public ActivityType type;
        public int period;//每天分3个时间段（早午晚）每过一个时间段，该值加一，如第二天下午，该值为4
        public String detail;
    }

    public static DietTemp[] dietTempInfo;

    public static void setDietTempInfoInstance(int periodNum) {
        dietTempInfo = new DietTemp[periodNum];
        for (int i = 0; i < periodNum; i++)
            dietTempInfo[i] = new DietTemp();
    }

    public static class DietTemp {
        public int period;//每天分3个时间段（早午晚）每过一个时间段，该值加一，如第二天下午，该值为4
        public String detail;
    }
}
