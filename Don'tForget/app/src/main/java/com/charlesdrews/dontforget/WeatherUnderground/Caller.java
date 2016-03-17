package com.charlesdrews.dontforget.WeatherUnderground;

import android.util.Log;

import com.charlesdrews.dontforget.WeatherUnderground.Model.HourlyForecast;
import com.charlesdrews.dontforget.WeatherUnderground.Model.JsonResponse;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Provides static method to retrieve forecast data from the Weather Underground API
 * (http://www.wunderground.com/weather/api/d/docs) and return HourlyForecast objects
 *
 * Sample call for NYC latitude, longitude
 * http://api.wunderground.com/api/37c7eb855c9fb351/hourly/q/40.743043,-73.981797.json
 *
 * Created by charlie on 3/16/16.
 */
public class Caller {
    private static final String API_KEY = "37c7eb855c9fb351";
    private static final String BASE_URL = "http://api.wunderground.com/api/" + API_KEY + "/";

    private Caller() {}

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
    private static List<HourlyForecast> getHourlyForecasts(String query) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherUndergroundService service = retrofit.create(WeatherUndergroundService.class);

        Call<JsonResponse> call = service.getHourly(query);

        Response<JsonResponse> response = null;
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

    public static void main(String[] args) {
        // testing
        List<HourlyForecast> forecasts = getHourlyForecasts("40.743043,-73.981797");
        if (forecasts != null) {
            for (HourlyForecast forecast : forecasts) {
                System.out.println(forecast.getFCTTIME().getHour());
                System.out.println(forecast.getTemp().getEnglish());
                System.out.println();
            }
        }
    }
}
