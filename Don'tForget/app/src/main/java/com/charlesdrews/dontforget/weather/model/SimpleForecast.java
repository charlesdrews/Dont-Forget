package com.charlesdrews.dontforget.weather.model;

import java.util.List;

import io.realm.RealmObject;

/**
 * Created by charlie on 3/31/16.
 */
public class SimpleForecast extends RealmObject {
    private List<ForecastDay> forecastday;

    public List<ForecastDay> getForecastday() {
        return forecastday;
    }
}
