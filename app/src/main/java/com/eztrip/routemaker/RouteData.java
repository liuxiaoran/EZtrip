package com.eztrip.routemaker;

import android.text.format.Time;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Steve on 2015/2/5.
 * 暂时存储信息
 */
public class RouteData {
    public String destination;
    public ArrayList<DayEvent> dayEvents;
    public int day;
    public Date startDay;
    public String wayToTravel;

    class DayEvent{
        public ArrayList<SingleEvent> SingleEvents;

        class SingleEvent{
            public int type;
            public Time startTime;
            public Time finishTime;
            public String detail;
        }
    }
}
