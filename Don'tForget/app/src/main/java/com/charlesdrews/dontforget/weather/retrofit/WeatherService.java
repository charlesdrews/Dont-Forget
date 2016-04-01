package com.charlesdrews.dontforget.weather.retrofit;

import com.charlesdrews.dontforget.weather.model.WeatherResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Interface for calling the Weather Underground API
 * http://www.wunderground.com/weather/api/d/docs
 *
 * Created by charlie on 3/16/16.
 */
public interface WeatherService {
    @GET("conditions/hourly/forecast10day/q/{query}.json")
    Call<WeatherResponse> getWeather(@Path("query") String query);
}
