package com.charlesdrews.dontforget.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.charlesdrews.dontforget.R;
import com.charlesdrews.dontforget.weather.WeatherHelper;
import com.charlesdrews.dontforget.weather.model.HourlyForecast;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by charlie on 3/22/16.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "SyncAdapter";
    private ContentResolver mContentResolver;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG, "onPerformSync: making API call");

        String query = null; //"40.743043,-73.981797"
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        // check if geolocation turned on in preferences
        if (prefs.getBoolean(getContext().getString(R.string.pref_key_weather_geo), false)) {
            Log.d(TAG, "onPerformSync: geolocation on");
            //TODO - get last known location, save in query
        } else {
            // if not, then check if a static location has been set
            Log.d(TAG, "onPerformSync: getting static location");
            query = prefs.getString(getContext().getString(R.string.pref_key_weather_static_location), null);
        }

        if (query == null || query.equals(getContext().getString(R.string.weather_static_location_default))) {
            Log.d(TAG, "onPerformSync: geolocation off & static location not set");
            //TODO - ask user to do one or the other
            return;
        }

        List<HourlyForecast> newHourlyForecasts = WeatherHelper.getHourlyForecasts(query);

        if (newHourlyForecasts != null && newHourlyForecasts.size() > 0) {
            Log.d(TAG, "onPerformSync: API call yielded results");

            Realm realm = Realm.getDefaultInstance();
            RealmResults<HourlyForecast> oldHourlyForecasts = realm.where(HourlyForecast.class).findAll();

            realm.beginTransaction();
            oldHourlyForecasts.clear();
            realm.copyToRealm(newHourlyForecasts);
            realm.commitTransaction();

            realm.close();

            Log.d(TAG, "onPerformSync: results persisted to db");
        }
    }
}
