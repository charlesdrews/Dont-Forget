package com.charlesdrews.dontforget.weather.model;

import java.util.Calendar;
import java.util.Date;

import io.realm.RealmObject;

/**
 * Flatten out the nested class structure provided by Retrofit for the Weather Underground
 * API response data to enable easy sorting of the Realm objects
 *
 * Created by charlie on 4/1/16.
 */
public class HourlyForecastRealm extends RealmObject {
    // date & time
    private Date dateTime;
    boolean isDst;
    private String monthName, monthAbbrev, weekdayName, weekdayAbbrev, ampm;

    // conditions
    private String conditionDesc, iconUrl;
    private int humidity, probOfPrecip;
    private int tempFahr, tempCel, windchillFahr, windChillCel, heatIndexFahr, headIndexCel;
    private double precipInches, precipMMs, snowInches, snowCMs;

    public HourlyForecastRealm() {}

    public void setValues(HourlyForecast hourlyForecast) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(
                hourlyForecast.getFCTTIME().getYear(),
                hourlyForecast.getFCTTIME().getMon() - 1, // month is 0-based
                hourlyForecast.getFCTTIME().getMday(),
                hourlyForecast.getFCTTIME().getHour(),
                hourlyForecast.getFCTTIME().getMin()
        );
        this.dateTime = calendar.getTime();

        this.isDst = hourlyForecast.getFCTTIME().getIsdst() == 1;
        this.monthName = hourlyForecast.getFCTTIME().getMonth_name();
        this.monthAbbrev = hourlyForecast.getFCTTIME().getMonth_name_abbrev();
        this.weekdayName = hourlyForecast.getFCTTIME().getWeekday_name();
        this.weekdayAbbrev = hourlyForecast.getFCTTIME().getWeekday_name_abbrev();
        this.ampm = hourlyForecast.getFCTTIME().getAmpm();

        this.conditionDesc = hourlyForecast.getCondition();
        this.iconUrl = hourlyForecast.getIconUrl();
        this.humidity = hourlyForecast.getHumidity();
        this.probOfPrecip = hourlyForecast.getPop();

        this.tempFahr = hourlyForecast.getTemp().getEnglish();
        this.tempCel = hourlyForecast.getTemp().getMetric();
        this.windchillFahr = hourlyForecast.getWindchill().getEnglish();
        this.windChillCel = hourlyForecast.getWindchill().getMetric();
        this.heatIndexFahr = hourlyForecast.getHeatindex().getEnglish();
        this.headIndexCel = hourlyForecast.getHeatindex().getMetric();

        this.precipInches = hourlyForecast.getQpf().getEnglish();
        this.precipMMs = hourlyForecast.getQpf().getMetric();
        this.snowInches = hourlyForecast.getSnow().getEnglish();
        this.snowCMs = hourlyForecast.getSnow().getMetric();
    }

    public Date getDateTime() {
        return dateTime;
    }

    public boolean isDst() {
        return isDst;
    }

    public String getMonthName() {
        return monthName;
    }

    public String getMonthAbbrev() {
        return monthAbbrev;
    }

    public String getWeekdayName() {
        return weekdayName;
    }

    public String getWeekdayAbbrev() {
        return weekdayAbbrev;
    }

    public String getAmpm() {
        return ampm;
    }

    public String getConditionDesc() {
        return conditionDesc;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public int getHumidity() {
        return humidity;
    }

    public int getProbOfPrecip() {
        return probOfPrecip;
    }

    public int getTempFahr() {
        return tempFahr;
    }

    public int getTempCel() {
        return tempCel;
    }

    public int getWindchillFahr() {
        return windchillFahr;
    }

    public int getWindChillCel() {
        return windChillCel;
    }

    public int getHeatIndexFahr() {
        return heatIndexFahr;
    }

    public int getHeadIndexCel() {
        return headIndexCel;
    }

    public double getPrecipInches() {
        return precipInches;
    }

    public double getPrecipMMs() {
        return precipMMs;
    }

    public double getSnowInches() {
        return snowInches;
    }

    public double getSnowCMs() {
        return snowCMs;
    }
}
