package com.charlesdrews.dontforget.weather.model;

import java.util.List;

/**
 * Created by charlie on 3/16/16.
 */
public class HourlyForecastResponse {
    private List<HourlyForecast> hourly_forecast;

    public List<HourlyForecast> getHourlyForecasts() {
        return hourly_forecast;
    }
}
