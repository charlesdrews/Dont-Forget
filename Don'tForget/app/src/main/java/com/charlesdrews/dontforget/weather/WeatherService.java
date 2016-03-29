package com.charlesdrews.dontforget.weather;

import com.charlesdrews.dontforget.weather.model.HourlyForecastResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Interface for calling the hourly forecast feature of the WeatherHelper Underground API
 * http://www.wunderground.com/weather/api/d/docs
 *
 * Created by charlie on 3/16/16.
 */
public interface WeatherService {
    @GET("hourly/q/{query}.json")
    Call<HourlyForecastResponse> getHourly(@Path("query") String query);
}
