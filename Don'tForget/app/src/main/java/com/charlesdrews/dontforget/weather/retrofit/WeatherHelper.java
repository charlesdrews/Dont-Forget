package com.charlesdrews.dontforget.weather.retrofit;

import com.charlesdrews.dontforget.weather.model.HourlyForecast;
import com.charlesdrews.dontforget.weather.model.HourlyForecastResponse;
import com.charlesdrews.dontforget.weather.model.Location;
import com.charlesdrews.dontforget.weather.model.LocationResponse;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.List;

import io.realm.RealmObject;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Provides static method to retrieve forecast data from the WeatherHelper Underground API
 * (http://www.wunderground.com/weather/api/d/docs) and return HourlyForecast objects
 *
 * Sample call for NYC latitude, longitude
 * http://api.wunderground.com/api/37c7eb855c9fb351/hourly/q/40.743043,-73.981797.json
 *
 * Created by charlie on 3/16/16.
 */
public class WeatherHelper {
    private static final String API_KEY = "37c7eb855c9fb351";
    private static final String FORECAST_BASE_URL = "http://api.wunderground.com/api/" + API_KEY + "/";
    private static final String LOCATIONS_BASE_URL = "http://autocomplete.wunderground.com/";

    private WeatherHelper() {}

    /**
     * Retrieves a list of HourlyForecast objects from the WeatherUnderground API. This method
     * must be called in a async task on a worker thread and NOT on the UI thread.
     *
     * Formatting options for query string:
     * CA/San_Francisco	                    US state/city
     * 60290	                            US zipcode
     * Australia/Sydney	                    country/city
     * 37.8,-122.4	                        latitude,longitude
     * KJFK	                                airport code
     * pws:KCASANFR70	                    PWS id
     * autoip	                            AutoIP address location
     * autoip.json?geo_ip=38.102.136.138    specific IP address location
     *
     * @param query - location query string
     * @return List of HourlyForecast objects, or null if no response or error response
     */
    public static List<HourlyForecast> getHourlyForecasts(String query) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(FORECAST_BASE_URL)
                .addConverterFactory(getGsonConverterFactoryForRealm())
                .build();

        WeatherService service = retrofit.create(WeatherService.class);
        Call<HourlyForecastResponse> call = service.getHourly(query);

        Response<HourlyForecastResponse> response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (response != null && response.body() != null) {
            return response.body().getHourlyForecasts();
        } else {
            return null;
        }
    }

    /**
     * Retrieves a list of locations matching query
     */
    public static List<Location> getLocations(String query) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(LOCATIONS_BASE_URL)
                .addConverterFactory(getGsonConverterFactoryForRealm())
                .build();

        LocationService service = retrofit.create(LocationService.class);
        Call<LocationResponse> call = service.getLocations(query);

        Response<LocationResponse> response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (response != null && response.body() != null) {
            return response.body().getRESULTS();
        } else {
            return null;
        }
    }

    public static GsonConverterFactory getGsonConverterFactoryForRealm() {
        Gson gson = new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaringClass().equals(RealmObject.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .create();

        return GsonConverterFactory.create(gson);
    }

    public static void main(String[] args) {
        // testing
        /*
        List<HourlyForecast> forecasts = getHourlyForecasts("40.743043,-73.981797");
        if (forecasts != null) {
            for (HourlyForecast forecast : forecasts) {
                System.out.println(forecast.getFCTTIME().getHour());
                System.out.println(forecast.getTemp().getEnglish());
                System.out.println();
            }
        }
        */
        List<Location> locations = getLocations("New York");
        if (locations != null) {
            for (Location location : locations) {
                System.out.println(location.getName());
            }
        }
    }
}
