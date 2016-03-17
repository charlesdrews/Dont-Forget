package com.charlesdrews.dontforget.WeatherUnderground.Model;

/**
 * Created by charlie on 3/16/16.
 */
public class FctTime {
    private int year, mon, mday, hour, min, isdst;
    private String month_name, month_name_abbrev, weekday_name, weekday_name_abbrev, ampm;

    public int getYear() {
        return year;
    }

    public int getMon() {
        return mon;
    }

    public int getMday() {
        return mday;
    }

    public int getHour() {
        return hour;
    }

    public int getMin() {
        return min;
    }

    public int getIsdst() {
        return isdst;
    }

    public String getMonth_name() {
        return month_name;
    }

    public String getMonth_name_abbrev() {
        return month_name_abbrev;
    }

    public String getWeekday_name() {
        return weekday_name;
    }

    public String getWeekday_name_abbrev() {
        return weekday_name_abbrev;
    }

    public String getAmpm() {
        return ampm;
    }
}
