package com.charlesdrews.dontforget.weather.model;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Model last known device location (lat/long converted to String) with time obtained
 * Created by charlie on 4/9/16.
 */
public class LocationRealm extends RealmObject {
    @Required private String locationString;
    @Required private Long locationTimeInMillis;

    public LocationRealm() {}

    public LocationRealm(String locationString, long locationTimeInMillis) {
        this.locationString = locationString;
        this.locationTimeInMillis = locationTimeInMillis;
    }

    public Long getLocationTimeInMillis() {
        return locationTimeInMillis;
    }

    public void setLocationTimeInMillis(Long locationTimeInMillis) {
        this.locationTimeInMillis = locationTimeInMillis;
    }

    public String getLocationString() {
        return locationString;
    }

    public void setLocationString(String locationString) {
        this.locationString = locationString;
    }
}
