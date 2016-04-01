package com.charlesdrews.dontforget.weather.model;

/**
 * Created by charlie on 3/31/16.
 */
public class ForecastDay {
    private Day date;
    private TempFahrCel high, low;
    private String conditions, icon_url;
    private int pop, avehumidity;
    private Qpf qpf_allday;
    private Snow snow_allday;

    public Day getDate() {
        return date;
    }

    public TempFahrCel getHigh() {
        return high;
    }

    public TempFahrCel getLow() {
        return low;
    }

    public String getConditions() {
        return conditions;
    }

    public String getIcon_url() {
        return icon_url;
    }

    public int getPop() {
        return pop;
    }

    public int getAvehumidity() {
        return avehumidity;
    }

    public Qpf getQpf_allday() {
        return qpf_allday;
    }

    public Snow getSnow_allday() {
        return snow_allday;
    }
}
