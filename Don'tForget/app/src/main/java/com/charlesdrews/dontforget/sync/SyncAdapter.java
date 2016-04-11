package com.charlesdrews.dontforget.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.charlesdrews.dontforget.MyApplication;
import com.charlesdrews.dontforget.weather.model.CurrentConditionsRealm;
import com.charlesdrews.dontforget.weather.model.DailyForecastRealm;
import com.charlesdrews.dontforget.weather.model.ForecastDay;
import com.charlesdrews.dontforget.weather.model.HourlyForecastRealm;
import com.charlesdrews.dontforget.weather.model.WeatherResponse;
import com.charlesdrews.dontforget.weather.retrofit.WeatherHelper;
import com.charlesdrews.dontforget.weather.model.HourlyForecast;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Call WeatherUnderground API, save data in Realm db, notify content resolver
 * Created by charlie on 3/22/16.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "SyncAdapter";
    public static final String LOCATION_QUERY_KEY = "locationQueryKey";

    private Context mContext;
    private ContentResolver mContentResolver;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        mContentResolver = mContext.getContentResolver();
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContext = context;
        mContentResolver = mContext.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        //TODO - this is just for testing
        boolean periodic = extras.getBoolean("PERIODIC_SYNC_KEY", false);
        if (periodic) Log.d(TAG, "onPerformSync: this is a PERIODIC sync");

        String query = extras.getString(LOCATION_QUERY_KEY);

        if (query == null || query.isEmpty()) {
            Log.d(TAG, "onPerformSync: query string empty; cannot perform sync");
            mContentResolver.notifyChange(StubProvider.WEATHER_URI_FAILURE, null, false);
            return;
        }

        if (getAndSaveForecastData(query)) {
            Log.d(TAG, "onPerformSync: sync successful");
            mContentResolver.notifyChange(StubProvider.WEATHER_URI_SUCCESS, null, false);
        } else {
            Log.d(TAG, "onPerformSync: sync failed");
            mContentResolver.notifyChange(StubProvider.WEATHER_URI_FAILURE, null, false);
        }
    }

    public boolean getAndSaveForecastData(final String query) {
        Log.d(TAG, "getAndSaveForecastData: making API call w/ query " + query);
        final WeatherResponse weatherResponse = WeatherHelper.getWeatherData(query);

        if (weatherResponse == null) {
            Log.d(TAG, "getAndSaveForecastData: no data rec'd from API");
            return false;
        }

        Log.d(TAG, "getAndSaveForecastData: API call yielded results");
        Realm realm = Realm.getDefaultInstance();

        // update current conditions in database
        CurrentConditionsRealm current = new CurrentConditionsRealm();
        current.setValues(weatherResponse.getCurrent_observation(), query);
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(current);
        realm.commitTransaction();
        Log.d(TAG, "getAndSaveForecastData: current conditions saved");

        // update hourly forecasts in database
        final List<HourlyForecast> hourlyForecasts = weatherResponse.getHourly_forecast();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.clear(HourlyForecastRealm.class);
                for (HourlyForecast forecast : hourlyForecasts) {
                    HourlyForecastRealm hourly = realm.createObject(HourlyForecastRealm.class);
                    hourly.setValues(forecast);
                }
                Log.d(TAG, "getAndSaveForecastData: hourly forecasts saved");
            }
        });

        // update daily forecasts in database
        final List<ForecastDay> forecastDays = weatherResponse.getForecast()
                .getSimpleforecast().getForecastday();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.clear(DailyForecastRealm.class);
                for (ForecastDay forecast : forecastDays) {
                    DailyForecastRealm daily = realm.createObject(DailyForecastRealm.class);
                    daily.setValues(forecast);
                }
                Log.d(TAG, "getAndSaveForecastData: daily forecasts saved");
            }
        });

        realm.close();
        Log.d(TAG, "getAndSaveForecastData: results persisted to db");
        return true;
    }
}
