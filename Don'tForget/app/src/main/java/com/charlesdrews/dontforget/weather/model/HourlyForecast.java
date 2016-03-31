package com.charlesdrews.dontforget.weather.model;

import io.realm.RealmObject;

/**
 * Created by charlie on 3/16/16.
 */
public class HourlyForecast extends RealmObject {
    private Hour FCTTIME;
    private String condition, icon, icon_url;
    private int humidity, pop;
    private TempEngMetric temp, dewpoint, windchill, heatindex, feelslike;
    private Precipitation qpf, snow;

    public Hour getFCTTIME() {
        return FCTTIME;
    }

    public String getCondition() {
        return condition;
    }

    public String getIcon() {
        return icon;
    }

    public String getIconUrl() {
        return icon_url;
    }

    public int getHumidity() {
        return humidity;
    }

    public int getPop() {
        return pop;
    }

    public TempEngMetric getTemp() {
        return temp;
    }

    public TempEngMetric getDewpoint() {
        return dewpoint;
    }

    public TempEngMetric getWindchill() {
        return windchill;
    }

    public TempEngMetric getHeatindex() {
        return heatindex;
    }

    public TempEngMetric getFeelslike() {
        return feelslike;
    }

    public Precipitation getQpf() {
        return qpf;
    }

    public Precipitation getSnow() {
        return snow;
    }
}
