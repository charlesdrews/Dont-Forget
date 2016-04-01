package com.charlesdrews.dontforget.weather.model;

import io.realm.RealmObject;

/**
 * Flatten out the nested class structure provided by Retrofit for the Weather Underground
 * API response data to enable easy sorting of the Realm objects
 *
 * Created by charlie on 4/1/16.
 */
public class CurrentConditionsRealm extends RealmObject {
    // Display location
    private String fullName, city, stateAbbrev, stateName, country, zip;

    // Current conditions
    private String currConditionDesc, relativeHumidity, iconUrl;
    private double tempFahr, tempCel, heatIndexFahr, heatIndexCel, windchillFahr, windchillCel;

    public CurrentConditionsRealm(CurrentConditions currentConditions) {
        this.fullName = currentConditions.getDisplay_location().getFull();
        this.city = currentConditions.getDisplay_location().getCity();
        this.stateAbbrev = currentConditions.getDisplay_location().getState();
        this.stateName = currentConditions.getDisplay_location().getState_name();
        this.country = currentConditions.getDisplay_location().getCountry();
        this.zip = currentConditions.getDisplay_location().getZip();

        this.currConditionDesc = currentConditions.getWeather();
        this.relativeHumidity = currentConditions.getRelative_humidity();
        this.iconUrl = currentConditions.getIcon_url();

        this.tempFahr = currentConditions.getTemp_f();
        this.tempCel = currentConditions.getTemp_c();
        this.heatIndexFahr = currentConditions.getHeat_index_f();
        this.heatIndexCel = currentConditions.getHeat_index_c();
        this.windchillFahr = currentConditions.getWindchill_f();
        this.windchillCel = currentConditions.getWindchill_c();
    }

    public String getFullName() {
        return fullName;
    }

    public String getCity() {
        return city;
    }

    public String getStateAbbrev() {
        return stateAbbrev;
    }

    public String getStateName() {
        return stateName;
    }

    public String getCountry() {
        return country;
    }

    public String getZip() {
        return zip;
    }

    public String getCurrConditionDesc() {
        return currConditionDesc;
    }

    public String getRelativeHumidity() {
        return relativeHumidity;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public double getTempFahr() {
        return tempFahr;
    }

    public double getTempCel() {
        return tempCel;
    }

    public double getHeatIndexFahr() {
        return heatIndexFahr;
    }

    public double getHeatIndexCel() {
        return heatIndexCel;
    }

    public double getWindchillFahr() {
        return windchillFahr;
    }

    public double getWindchillCel() {
        return windchillCel;
    }
}
