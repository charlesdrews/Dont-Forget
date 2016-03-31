package com.charlesdrews.dontforget.weather.model;

import io.realm.RealmObject;

/**
 * Created by charlie on 3/31/16.
 */
public class DailyForecast extends RealmObject {
    private SimpleForecast simpleforecast;

    public SimpleForecast getSimpleforecast() {
        return simpleforecast;
    }
}
