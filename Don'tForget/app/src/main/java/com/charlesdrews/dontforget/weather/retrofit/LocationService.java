package com.charlesdrews.dontforget.weather.retrofit;

import com.charlesdrews.dontforget.weather.model.LocationResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by charlie on 3/29/16.
 */
public interface LocationService {
    @GET("aq")
    Call<LocationResponse> getLocations(@Query("query") String query);
}
