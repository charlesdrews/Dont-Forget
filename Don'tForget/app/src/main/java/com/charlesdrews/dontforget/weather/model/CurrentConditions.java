package com.charlesdrews.dontforget.weather.model;

import io.realm.RealmObject;

/**
 * Created by charlie on 3/31/16.
 */
public class CurrentConditions extends RealmObject {
    private DisplayLocation display_location;
    private String weather, relative_humidity, icon_url;
    private double temp_f, temp_c, heat_index_f, heat_index_c, windchill_f, windchill_c;

    public DisplayLocation getDisplay_location() {
        return display_location;
    }

    public String getWeather() {
        return weather;
    }

    public String getRelative_humidity() {
        return relative_humidity;
    }

    public String getIcon_url() {
        return icon_url;
    }

    public double getTemp_f() {
        return temp_f;
    }

    public double getTemp_c() {
        return temp_c;
    }

    public double getHeat_index_f() {
        return heat_index_f;
    }

    public double getHeat_index_c() {
        return heat_index_c;
    }

    public double getWindchill_f() {
        return windchill_f;
    }

    public double getWindchill_c() {
        return windchill_c;
    }
}
