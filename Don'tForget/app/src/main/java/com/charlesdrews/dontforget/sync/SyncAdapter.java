package com.charlesdrews.dontforget.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.charlesdrews.dontforget.R;
import com.charlesdrews.dontforget.weather.model.CurrentConditionsRealm;
import com.charlesdrews.dontforget.weather.model.DailyForecast;
import com.charlesdrews.dontforget.weather.model.DailyForecastRealm;
import com.charlesdrews.dontforget.weather.model.ForecastDay;
import com.charlesdrews.dontforget.weather.model.HourlyForecastRealm;
import com.charlesdrews.dontforget.weather.model.WeatherResponse;
import com.charlesdrews.dontforget.weather.retrofit.WeatherHelper;
import com.charlesdrews.dontforget.weather.model.HourlyForecast;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

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
        String query = extras.getString(LOCATION_QUERY_KEY);
        if (query != null) {
            getAndSaveForecastData(query);

            // save sync time to shared preferences
            String syncTimeKey = mContext.getString(R.string.weather_last_sync_time_key);
            PreferenceManager.getDefaultSharedPreferences(mContext)
                    .edit()
                    .putLong(syncTimeKey, System.currentTimeMillis())
                    .commit();

            mContentResolver.notifyChange(StubProvider.WEATHER_URI, null);
        }
    }

    public void getAndSaveForecastData(String query) {
        if (query == null || query.isEmpty() ||
                query.equals(getContext().getString(R.string.weather_static_location_default))) {
            Log.d(TAG, "getAndSaveForecastData: location query string is blank; cannot sync");
            //TODO - ask user to do one or the other - launch settings activity?
            return;
        }

        Log.d(TAG, "getAndSaveForecastData: making API call w/ query " + query);
        final WeatherResponse weatherResponse = WeatherHelper.getWeatherData(query);

        if (weatherResponse == null) {
            Log.d(TAG, "getAndSaveForecastData: no data rec'd from API");
            return;
        }

        Log.d(TAG, "getAndSaveForecastData: API call yielded results");
        Realm realm = Realm.getInstance(new RealmConfiguration.Builder(getContext()).build());

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.clear(CurrentConditionsRealm.class);
                CurrentConditionsRealm current = realm.createObject(CurrentConditionsRealm.class);
                current.setValues(weatherResponse.getCurrent_observation());
                Log.d(TAG, "getAndSaveForecastData: current conditions saved");
            }
        });

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
    }
}
