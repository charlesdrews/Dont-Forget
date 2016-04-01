package com.charlesdrews.dontforget.weather.model;

/**
 * Created by charlie on 3/16/16.
 */
public class HourlyForecast {
    private Hour FCTTIME;
    private String condition, icon_url;
    private int humidity, pop;
    private TempEngMetric temp, windchill, heatindex;
    private Precipitation qpf, snow;

    public Hour getFCTTIME() {
        return FCTTIME;
    }

    public String getCondition() {
        return condition;
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

    public TempEngMetric getWindchill() {
        return windchill;
    }

    public TempEngMetric getHeatindex() {
        return heatindex;
    }

    public Precipitation getQpf() {
        return qpf;
    }

    public Precipitation getSnow() {
        return snow;
    }
}
