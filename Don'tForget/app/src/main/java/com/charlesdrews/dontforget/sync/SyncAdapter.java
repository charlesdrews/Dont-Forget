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
import com.charlesdrews.dontforget.weather.model.CurrentConditions;
import com.charlesdrews.dontforget.weather.model.WeatherData;
import com.charlesdrews.dontforget.weather.retrofit.WeatherHelper;
import com.charlesdrews.dontforget.weather.model.HourlyForecast;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
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
            Log.d(TAG, "getAndSaveForecastData: location query string is blank");
            //TODO - ask user to do one or the other - launch settings activity?
            return;
        }

        Log.d(TAG, "getAndSaveForecastData: making API call w/ query " + query);
        WeatherData weatherData = WeatherHelper.getWeatherData(query);

        if (weatherData == null) { return; }

        Log.d(TAG, "getAndSaveForecastData: API call yielded results");
        Realm realm = Realm.getDefaultInstance();

        if (weatherData.getCurrent_observation() != null) {
            Log.d(TAG, "getAndSaveForecastData: saving current conditions");
            realm.beginTransaction();
            realm.copyToRealm(weatherData.getCurrent_observation());
            realm.commitTransaction();
        }

        if (weatherData.getHourly_forecast() != null
                && weatherData.getHourly_forecast().size() > 0) {
            RealmResults<HourlyForecast> oldHourlyForecasts = realm.where(HourlyForecast.class).findAll();

            Log.d(TAG, "getAndSaveForecastData: saving hourly forecast");
            realm.beginTransaction();
            oldHourlyForecasts.clear();
            realm.copyToRealm(weatherData.getHourly_forecast());
            realm.commitTransaction();

        }

        if (weatherData.getForecast() != null) {

            Log.d(TAG, "getAndSaveForecastData: saving daily forecast");
            realm.beginTransaction();
            realm.copyToRealm(weatherData.getForecast());
            realm.commitTransaction();
        }

        realm.close();
        Log.d(TAG, "getAndSaveForecastData: results persisted to db");
    }
}
