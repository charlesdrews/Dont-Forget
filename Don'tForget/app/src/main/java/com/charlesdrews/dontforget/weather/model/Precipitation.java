package com.charlesdrews.dontforget.weather.model;

import io.realm.RealmObject;

/**
 * Created by charlie on 3/16/16.
 */
public class Precipitation extends RealmObject {
    private double english, metric;

    public double getEnglish() {
        return english;
    }

    public double getMetric() {
        return metric;
    }
}
