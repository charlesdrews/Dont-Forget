package com.charlesdrews.dontforget.weather.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Store last used location String in database with time it was obtained
 * Created by charlie on 4/10/16.
 */
public class LocationRealm extends RealmObject {
    private static final int LOCATION_PRIMARY_KEY = 43; // arbitrary; just needs to always be the same

    // Create a primary key so the object can be updated rather than deleted & recreated
    @PrimaryKey private int id;
    private String locationString;
    private long timeObtainedInMillis;

    public LocationRealm() {
        this.id = LOCATION_PRIMARY_KEY;
        this.timeObtainedInMillis = System.currentTimeMillis();
    }

    public LocationRealm(String locationString) {
        this.id = LOCATION_PRIMARY_KEY;
        this.locationString = locationString;
        this.timeObtainedInMillis = System.currentTimeMillis();
    }

    public int getId() {
        return id;
    }

    public String getLocationString() {
        return locationString;
    }

    public void setLocationString(String locationString) {
        this.locationString = locationString;
    }

    public long getTimeObtainedInMillis() {
        return timeObtainedInMillis;
    }
}
