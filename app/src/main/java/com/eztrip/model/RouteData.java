package com.eztrip.model;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Steve on 2015/2/5.
 * A class for storing data resulted from route-maker process
 * Single instance mode
 */
public class RouteData {
    public static String city;//Destination of the trip
    public static ArrayList<SingleEvent> singleEvents;//An array which storing one single event of the trip
    public static int dayLength;//The length of the trip
    public static Calendar startDay;//The start doy of the trip(Format:YYYY-MM-DD)
    public static String trafficInfo;//method of traffic during the trip()
    public static String warning;
    public static String dietInfo;
    public enum ActivityType {SPOT, DIET, TRAFFIC, ACCOMMODATION, OTHERS, NONE}//Types of a event

    public static String spotSettingsHint;//hint that is shown when spotSettingsFragment is created

    /**
     * Initializing member variable singleEvents
     *
     * @param dayNum the length of the trip
     */
    public static void setSingleEventsInstance(int dayNum) {
        RouteData.singleEvents = new ArrayList<>();
        for (int i = 0; i < dayNum; i++)
            RouteData.singleEvents.add(new RouteData.SingleEvent());
    }

    /**
     * Class for expressing a event during a trip
     */
    public static class SingleEvent {
        public int day;//the number of the day that is relative to the startDay
        public ActivityType type;//Type of the event
        public String startTime;//the start day of this event ex：11:00
        public String finishTime;//the finish day of this event
        public String detail;//the description of this event
        public String[] places;//relative places


        /**
         * Constructor of Class SingleEvent
         *
         * @param day
         * @param activityType
         * @param startTime
         * @param finishTime
         * @param detail
         * @param places
         */
        public void setSingleEvent(int day, ActivityType activityType, String startTime, String finishTime, String detail, String[] places) {
            this.day = day;
            this.type = activityType;
            this.startTime = startTime;
            this.finishTime = finishTime;
            this.detail = detail;
            this.places = places;
        }
    }

    /**
     * Temporarily store data resulted from generating a plan of spots and accommodation
     * {@link utils.RouteAutoGenerator#executeBasicSettings}
     * At each period of the trip plan, an item named "无" should be added in
     */
    public static SpotTemp[] spotTempInfo;
    /**
     * Temporarily store the count of items each period has.
     * It is defined for showing the list of spotTempInfo
     */
    public static int[] spotTempPeriodItemCount;

    /**
     * Initializing member variables spotTempInfo, spotTempPeriodItemCount
     *
     * @param itemCount numbers of items
     * @param periodNum numbers of period
     */
    public static void setSpotTempInfoInstance(int itemCount, int periodNum) {
        spotTempInfo = new SpotTemp[itemCount + periodNum];
        spotTempPeriodItemCount = new int[periodNum];
        for (int i = 0; i < itemCount + periodNum; i++)
            RouteData.spotTempInfo[i] = new RouteData.SpotTemp();
    }

    public static class SpotTemp {

        /**
         * Constructor of SpotTemp based on an existing instance, global variable spotTempPeriodItemCount remain unchanged.
         *
         * @param spotTemp
         */
        public SpotTemp(SpotTemp spotTemp) {
            this.type = spotTemp.type;
            this.period = spotTemp.period;
            this.detail = spotTemp.detail;
            this.recommendTime = spotTemp.recommendTime;
            //spotTempPeriodItemCount[period]++;
        }

        public SpotTemp() {
        }

        public void setSpotTemp(ActivityType activityType, int period, String detail, int recommendTime) {
            this.type = activityType;
            this.period = period;
            this.detail = detail;
            this.recommendTime = recommendTime;
            spotTempPeriodItemCount[period]++;
        }

        public ActivityType type; //type of this event (ActivityType.ACCOMMODATION or ActivityType.SPOT)
        public int period; //period of the time period the event at that is relative to the morning of the startDay, each day is divided into three periods(morning, afternoon, evening)
        public String detail; //description of this event
        public int recommendTime; //unit : minute
    }

    /**
     * Temporarily store data resulted from generating a plan for diet
     * {@link utils.RouteAutoGenerator#executeSpotSettings}
     */
    public static DietTemp[] dietTempInfo;

    /**
     * Initializing member variable dietTempInfo
     *
     * @param periodNum
     */
    public static void setDietTempInfoInstance(int periodNum) {
        dietTempInfo = new DietTemp[periodNum];
        for (int i = 0; i < periodNum; i++)
            dietTempInfo[i] = new DietTemp();
    }

    public static class DietTemp {
        /**
         * @see com.eztrip.model.RouteData.SpotTemp.period
         */
        public int period;
        public String detail;//description of diet information
    }
}
