package com.charlesdrews.dontforget.weather.model;

import io.realm.RealmObject;

/**
 * Flatten out the nested class structure provided by Retrofit for the Weather Underground
 * API response data to enable easy sorting of the Realm objects
 *
 * Created by charlie on 4/1/16.
 */
public class DailyForecastRealm extends RealmObject {
    // date
    private int year, month, dayOfMonth;
    private String monthName, monthAbbrev, weekdayName, weekdayAbbrev;

    // conditions
    private String conditionDesc, iconUrl;
    private int tempHighFahr, tempHighCel, tempLowFahr, tempLowCel;
    private int probOfPrecip, avgHumidity, precipMMs;
    private double precipInches, snowInches, snowCMs;

    public DailyForecastRealm(ForecastDay forecastDay) {
        this.year = forecastDay.getDate().getYear();
        this.month = forecastDay.getDate().getMonth();
        this.dayOfMonth = forecastDay.getDate().getDay();
        this.monthName = forecastDay.getDate().getMonthname();
        this.monthAbbrev = forecastDay.getDate().getMonthname_short();
        this.weekdayName = forecastDay.getDate().getWeekday();
        this.weekdayAbbrev = forecastDay.getDate().getWeekday_short();

        this.conditionDesc = forecastDay.getConditions();
        this.iconUrl = forecastDay.getIcon_url();

        this.tempHighFahr = forecastDay.getHigh().getFahrenheit();
        this.tempHighCel = forecastDay.getHigh().getCelsius();
        this.tempLowFahr = forecastDay.getLow().getFahrenheit();
        this.tempLowCel = forecastDay.getLow().getCelsius();

        this.probOfPrecip = forecastDay.getPop();
        this.avgHumidity = forecastDay.getAvehumidity();

        this.precipInches = forecastDay.getQpf_allday().getIn();
        this.precipMMs = forecastDay.getQpf_allday().getMm();
        this.snowInches = forecastDay.getSnow_allday().getIn();
        this.snowCMs = forecastDay.getSnow_allday().getCm();
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public String getMonthName() {
        return monthName;
    }

    public String getMonthAbbrev() {
        return monthAbbrev;
    }

    public String getWeekdayAbbrev() {
        return weekdayAbbrev;
    }

    public String getWeekdayName() {
        return weekdayName;
    }

    public String getConditionDesc() {
        return conditionDesc;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public int getTempHighFahr() {
        return tempHighFahr;
    }

    public int getTempHighCel() {
        return tempHighCel;
    }

    public int getTempLowFahr() {
        return tempLowFahr;
    }

    public int getTempLowCel() {
        return tempLowCel;
    }

    public int getProbOfPrecip() {
        return probOfPrecip;
    }

    public int getAvgHumidity() {
        return avgHumidity;
    }

    public int getPrecipMMs() {
        return precipMMs;
    }

    public double getPrecipInches() {
        return precipInches;
    }

    public double getSnowInches() {
        return snowInches;
    }

    public double getSnowCMs() {
        return snowCMs;
    }
}
