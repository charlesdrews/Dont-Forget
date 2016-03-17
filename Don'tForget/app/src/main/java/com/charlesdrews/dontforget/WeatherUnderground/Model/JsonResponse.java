package com.charlesdrews.dontforget.WeatherUnderground.Model;

import java.util.List;

/**
 * Created by charlie on 3/16/16.
 */
public class JsonResponse {
    private List<HourlyForecast> hourly_forecast;

    public List<HourlyForecast> getHourlyForecasts() {
        return hourly_forecast;
    }
}
