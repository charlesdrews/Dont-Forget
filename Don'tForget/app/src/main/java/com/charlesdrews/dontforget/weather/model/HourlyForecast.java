package com.charlesdrews.dontforget.weather.model;

import io.realm.RealmObject;

/**
 * Created by charlie on 3/16/16.
 */
public class HourlyForecast extends RealmObject {
    private FctTime FCTTIME;
    private String condition, icon, icon_url;
    private int humidity, pop;
    private Temperature temp, dewpoint, windchill, heatindex, feelslike;
    private Precipitation qpf, snow;

    public FctTime getFCTTIME() {
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

    public Temperature getTemp() {
        return temp;
    }

    public Temperature getDewpoint() {
        return dewpoint;
    }

    public Temperature getWindchill() {
        return windchill;
    }

    public Temperature getHeatindex() {
        return heatindex;
    }

    public Temperature getFeelslike() {
        return feelslike;
    }

    public Precipitation getQpf() {
        return qpf;
    }

    public Precipitation getSnow() {
        return snow;
    }
}
