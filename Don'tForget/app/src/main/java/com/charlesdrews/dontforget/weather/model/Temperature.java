package com.charlesdrews.dontforget.weather.model;

import io.realm.RealmObject;

/**
 * Created by charlie on 3/16/16.
 */
public class Temperature extends RealmObject {
    private int english, metric;

    public int getEnglish() {
        return english;
    }

    public int getMetric() {
        return metric;
    }
}
