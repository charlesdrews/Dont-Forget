package com.charlesdrews.dontforget.weather.model;

import java.util.List;

/**
 * Created by charlie on 3/16/16.
 */
public class WeatherResponse {
    private CurrentConditions current_observation;
    private List<HourlyForecast> hourly_forecast;
    private DailyForecast forecast;

    public CurrentConditions getCurrent_observation() {
        return current_observation;
    }

    public List<HourlyForecast> getHourly_forecast() {
        return hourly_forecast;
    }

    public DailyForecast getForecast() {
        return forecast;
    }
}
