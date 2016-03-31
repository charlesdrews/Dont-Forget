package com.charlesdrews.dontforget.weather.model;

import io.realm.RealmObject;

/**
 * Created by charlie on 3/31/16.
 */
public class Day extends RealmObject {
    private int year, month, day, monthname, monthname_short, weekday_short, weekday;

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public int getMonthname() {
        return monthname;
    }

    public int getMonthname_short() {
        return monthname_short;
    }

    public int getWeekday_short() {
        return weekday_short;
    }

    public int getWeekday() {
        return weekday;
    }
}
