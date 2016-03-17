package com.charlesdrews.dontforget.WeatherUnderground;

import com.charlesdrews.dontforget.WeatherUnderground.Model.JsonResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Interface for calling the hourly forecast feature of the Weather Underground API
 * http://www.wunderground.com/weather/api/d/docs
 *
 * Created by charlie on 3/16/16.
 */
public interface WeatherUndergroundService {
    @GET("hourly/q/{query}.json")
    Call<JsonResponse> getHourly(@Path("query") String query);
}
