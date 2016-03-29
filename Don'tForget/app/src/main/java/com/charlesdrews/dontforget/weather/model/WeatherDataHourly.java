package com.charlesdrews.dontforget.weather.model;

import java.util.List;

/**
 * Created by charlie on 3/29/16.
 */
public class WeatherDataHourly extends WeatherData {
    private List<HourlyForecast> hourlyForecasts;

    public WeatherDataHourly(int type, List<HourlyForecast> hourlyForecasts) {
        this.hourlyForecasts = hourlyForecasts;
    }

    public List<HourlyForecast> getHourlyForecasts() { return hourlyForecasts; }
}
