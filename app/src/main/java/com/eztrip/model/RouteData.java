package com.eztrip.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

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
    public static String dietInfo;//it shows whether breakfast, lunch, and dinner need planning
    public enum ActivityType {SPOT, DIET, TRAFFIC, ACCOMMODATION, OTHERS, NONE}//Types of a event

    public static ArrayList<HashMap<String, String>> basicSettingsSpot;
    public static String spotSettingsHint;//hint that is shown when spotSettingsFragment is created

    public static HashMap[][] distance;//Used in RouteAutoGenerator to store distances between every two spots

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

    public static void addSingleEvent(Object object) {

    }

    /**
     * Class for expressing a event during a trip
     */
    public static class SingleEvent {
        public int day;//the number of the day that is relative to the startDay
        public ActivityType type;//Type of the event
        public Clock startTime;//the start day of this event ex：11:00
        public Clock finishTime;//the finish day of this event
        public String detail;//the description of this event
        public List<HashMap<String, String>> latitudeAndLongitude;//relative latitude and longitude information of this event
        public String address;
        public SingleEvent() {
        }

        public SingleEvent(int day, ActivityType activityType, Clock startTime, Clock finishTime, String detail, List<HashMap<String, String>> latitudeAndLongitude) {
            this.day = day;
            this.type = activityType;
            this.startTime = startTime;
            this.finishTime = finishTime;
            this.detail = detail;
            this.latitudeAndLongitude = latitudeAndLongitude;
        }

        public SingleEvent(int day, ActivityType activityType, Clock startTime, Clock finishTime, String detail) {
            this.day = day;
            this.type = activityType;
            this.startTime = startTime;
            this.finishTime = finishTime;
            this.detail = detail;
        }

    }

    /**
     * Temporarily store data resulted from generating a plan of spots and accommodation
     * {@link utils.RouteAutoGenerator#executeBasicSettings}
     * At each period of the trip plan, an item named "无" should be added in
     */
    public static ArrayList<SpotTemp> spotTempInfo;
    /**
     * Temporarily store the count of items each period has.
     * It is defined for showing the list of spotTempInfo
     */
    public static int[] spotTempPeriodItemCount;

    /**
     * Initializing member variables spotTempInfo, spotTempPeriodItemCount
     *
     * @param itemCount
     *          numbers of items
     * @param day
     *          numbers of day
     */
    public static void setSpotTempInfoInstance(int itemCount, int day) {
        spotTempInfo = new ArrayList<>(itemCount + 6 * day);
        spotTempPeriodItemCount = new int[3 * day];
    }

    public static void setSpotTempPeriodItemCount() {

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
            this.latitude = spotTemp.latitude;
            this.longitude = spotTemp.longitude;
            this.address = spotTemp.address;
            //spotTempPeriodItemCount[period]++;
        }

        public SpotTemp() {
            this.detail = "无";
        }

        public void setSpotTemp(ActivityType activityType, int period, String detail, int recommendTime, String address) {
            this.type = activityType;
            this.period = period;
            this.detail = detail;
            this.recommendTime = recommendTime;
            this.longitude = "0.0";
            this.latitude = "0.0";
            //spotTempPeriodItemCount[period]++;
            this.address = address;
            this.leftSpot = null;
            this.rightSpot = null;
            this.leftRoadTime = 0;
            this.rightRoadTime = 0;
            this.combinedVisitTime = this.recommendTime;
        }

        public ActivityType type; //type of this event (ActivityType.ACCOMMODATION or ActivityType.SPOT)
        public int period; //period of the time period the event at that is relative to the morning of the startDay, each day is divided into three periods(morning, afternoon, evening). Start from 0
        public String detail; //description of this event
        public int recommendTime; //unit : minute
        public String latitude;//the latitude value of the place
        public String longitude;//the longitude of the place
        public String address;//the address of the place
        /**
         * @see utils.RouteAutoGenerator#combineTwoSpots(utils.SortedDistance[], java.util.ArrayList, int)
         */
        public int combinedVisitTime, leftRoadTime, rightRoadTime;
        public SpotTemp leftSpot, rightSpot;
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
        public String latitude;//the latitude value of the place
        public String longitude;//the longitude of the place
        public String address;//the address of the restaurant
        public String phone;//the phone number of the restaurant
        public String imgsrc;// the image url of the restaurant
        public int goodRemarks;//Count of good remarks of the restaurant
        public int commonRemarks;//Count of common remarks of the restaurant
        public int badRemarks;//Count of bad remarks of the restaurant
        public String recommendDishes;//Recommend dishes of the restaurant

        public DietTemp() {
        }

        /**
         * constructors which is used when no diet plan is set
         *
         * @param detail value = "无"
         */
        public DietTemp(String detail, int period) {
            this.detail = detail;
            this.period = period;
        }

        /**
         * constructors which is used when complete information is provided
         *
         * @param period
         * @param detail
         * @param latitude
         * @param longitude
         * @param address
         * @param phone
         * @param imgsrc
         * @param goodRemarks
         * @param commonRemarks
         * @param badRemarks
         * @param recommendDishes
         */
        public DietTemp(int period, String detail, String latitude, String longitude, String address, String phone, String imgsrc, int goodRemarks, int commonRemarks, int badRemarks, String recommendDishes) {
            this.period = period;
            this.detail = detail;
            this.latitude = latitude;
            this.longitude = longitude;
            this.address = address;
            this.phone = phone;
            this.imgsrc = imgsrc;
            this.goodRemarks = goodRemarks;
            this.commonRemarks = commonRemarks;
            this.badRemarks = badRemarks;
            this.recommendDishes = recommendDishes;
        }
    }

    /**
     * the hotel of the trip
     */
    public static Hotel hotelInfo;
    /**
     * class for storing data of the hotel
     */
    public static class Hotel {
        public String name;//the name of the hotel
        public String latitude;//the latitude of the hotel
        public String longitude;//the longitude of the hotel
        public int grade;//the grade level of the hotel (0-5)
        public String intro;//the description of the hotel
        public String address;//the address of the hotel
        public String satisfaction;// the degree of satisfaction to the hotel
        public String imgsrc;//// the image url of the hotel

        public Hotel(String name, String latitude, String longitude, int grade, String intro, String address, String satisfaction, String imgsrc) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
            this.grade = grade;
            this.intro = intro;
            this.address = address;
            this.satisfaction = satisfaction;
            this.imgsrc = imgsrc;
        }

        public Hotel() {
        }
    }

}


