package com.eztrip.routemaker.data;

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

    public enum ActivityType {SPOT, DIET, TRAFFIC, ACCOMMODATION, OTHERS}

    public static class SingleEvent {
        public int day;
        public ActivityType type;
        public String startTime;//例：11:00
        public String finishTime;
        public String detail;
        public String[] places;//相关地点
    }


    public static SpotTemp[] spotTempInfo;

    public static class SpotTemp {
        public SpotTemp(SpotTemp spotTemp) {
            this.type = spotTemp.type;
            this.period = spotTemp.period;
            this.detail = spotTemp.detail;
        }

        public SpotTemp() {
        }

        public ActivityType type;
        public int period;//每天分3个时间段（早午晚）每过一个时间段，该值加一，如第二天下午，该值为4
        public String detail;
    }

    public static DietTemp[] dietTempInfo;

    public static class DietTemp {
        public int period;//每天分3个时间段（早午晚）每过一个时间段，该值加一，如第二天下午，该值为4
        public String detail;
    }
}
