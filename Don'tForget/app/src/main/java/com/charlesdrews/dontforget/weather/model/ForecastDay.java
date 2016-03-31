package com.charlesdrews.dontforget.weather.model;

import io.realm.RealmObject;

/**
 * Created by charlie on 3/31/16.
 */
public class ForecastDay extends RealmObject {
    private Day date;
    private TempFahrCel high, low;
    private String conditions, icon_url;
    private int pop, avehumidity;

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
}
