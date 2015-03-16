package com.eztrip.model;

/**
 * Created by Steve on 2015/3/15.
 * class for showing and calculating time(format : hh:mm)
 */

public class Clock {

    public int hour;
    public int minute;

    public Clock(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    public Clock add(Clock anotherClock) {
        this.hour += anotherClock.hour;
        this.minute += anotherClock.minute;
        if (minute > 59) {
            hour++;
            minute -= 60;
        }
        this.hour = (hour > 23) ? hour - 23 : hour;
        return this;
    }

    public String toString() {
        return new StringBuilder(hour < 10 ? "0" + Integer.toString(hour) : Integer.toString(hour)).append(":").append(minute < 60 ? "0" + Integer.toString(minute) : Integer.toString(minute)).toString();
    }
}
