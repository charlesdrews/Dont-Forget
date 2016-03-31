package com.charlesdrews.dontforget.weather.model;

import io.realm.RealmObject;

/**
 * Created by charlie on 3/31/16.
 */
public class TempFahrCel extends RealmObject {
    private int fahrenheit, celsius;

    public int getFahrenheit() {
        return fahrenheit;
    }

    public int getCelsius() {
        return celsius;
    }
}
