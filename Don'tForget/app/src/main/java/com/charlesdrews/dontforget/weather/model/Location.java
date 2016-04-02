package com.charlesdrews.dontforget.weather.model;

/**
 * Created by charlie on 3/29/16.
 */
public class Location {
    private String name, type, c;
    private double lat, lon;

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getC() {
        return c;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }
}
