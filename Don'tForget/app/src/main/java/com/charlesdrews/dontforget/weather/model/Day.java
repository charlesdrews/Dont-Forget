package com.charlesdrews.dontforget.weather.model;

/**
 * Created by charlie on 3/31/16.
 */
public class Day {
    private int year, month, day;
    private String monthname, monthname_short, weekday_short, weekday;

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public String getMonthname() {
        return monthname;
    }

    public String getMonthname_short() {
        return monthname_short;
    }

    public String getWeekday_short() {
        return weekday_short;
    }

    public String getWeekday() {
        return weekday;
    }
}
