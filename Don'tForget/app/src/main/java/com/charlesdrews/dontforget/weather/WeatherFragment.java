package com.charlesdrews.dontforget.weather;


import android.Manifest;
import android.accounts.Account;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.charlesdrews.dontforget.MainActivity;
import com.charlesdrews.dontforget.R;
import com.charlesdrews.dontforget.settings.SettingsActivity;
import com.charlesdrews.dontforget.sync.AccountHelper;
import com.charlesdrews.dontforget.sync.StubProvider;
import com.charlesdrews.dontforget.sync.SyncAdapter;
import com.charlesdrews.dontforget.weather.model.CurrentConditionsRealm;
import com.charlesdrews.dontforget.weather.model.DailyForecastRealm;
import com.charlesdrews.dontforget.weather.model.HourlyForecastRealm;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;

public class WeatherFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = WeatherFragment.class.getSimpleName();
    private static final long SYNC_THRESHOLD_IN_MINUTES = 15;
    private static final long SYNC_THRESHOLD_IN_MILLIS = SYNC_THRESHOLD_IN_MINUTES * 60 * 1000;

    private SharedPreferences mPreferences;
    private WeatherContentObserver mContentObserver;
    private Account mAccount;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private View mRootView;
    private Realm mRealm;

    public WeatherFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        mContentObserver = new WeatherContentObserver(new Handler());

        mAccount = AccountHelper.getAccount(getContext());

        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if (mRealm == null || mRealm.isClosed()) {
            mRealm = Realm.getDefaultInstance();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_weather, container, false);

        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // listen for data syncs
        getContext().getContentResolver().registerContentObserver(
                Uri.parse(StubProvider.BASE_URI_STRING), true, mContentObserver);

        updateViewsWithDataFromDb();

        if (syncNeeded()) {
            Log.d(TAG, "onResume: sync needed");
            startSync();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // stop listening for data syncs
        getContext().getContentResolver().unregisterContentObserver(mContentObserver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.disconnect();
            } else if (mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                mGoogleApiClient.disconnect();
            }
        }

        if (mRealm != null && !mRealm.isClosed()) {
            mRealm.close();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected: connected to GoogleApiClient");

        // check if app has permission to use device location
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onConnected: permission NOT granted; starting request for permission");
            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MainActivity.ACCESS_COARSE_LOCATION_PERMISSION_REQUEST_CODE
            );
            return;
        }
        Log.d(TAG, "onConnected: permission granted");

        // if location is available, request new sync using location data
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.d(TAG, "onConnected: last location retrieved successfully");
            requestWeatherSync(getQueryStringFromLocation(mLastLocation));
        } else {
            // if location not yet available, set up a location request & trigger sync from listener
            Log.d(TAG, "onConnected: last location not available; launching a location request");
            LocationRequest locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                    .setInterval(0)
                    .setFastestInterval(0);
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, locationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        showConnectionProblemSnackbar();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        showConnectionProblemSnackbar();
    }

    private void showConnectionProblemSnackbar() {
        Snackbar.make(
                getActivity().findViewById(android.R.id.content),
                "Unable to get device location",
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged: location rec'd");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mLastLocation = location;
        requestWeatherSync(getQueryStringFromLocation(location));
    }

    private void updateViewsWithDataFromDb() {
        CurrentConditionsRealm currentConditions = mRealm
                .where(CurrentConditionsRealm.class)
                .findFirst();
        RealmResults<HourlyForecastRealm> hourlyForecasts = mRealm
                .where(HourlyForecastRealm.class)
                .findAllSorted("dateTime");
        RealmResults<DailyForecastRealm> dailyForecasts = mRealm
                .where(DailyForecastRealm.class)
                .findAllSorted("date");

        boolean useMetric = "Metric".equals(mPreferences.getString(
                getString(R.string.pref_key_weather_units),
                getString(R.string.weather_default_unit)));

        // set up current conditions card if data available
        if (currentConditions != null) {
            TextView location = (TextView) mRootView.findViewById(R.id.current_location);
            location.setText(String.format("%s, %s", currentConditions.getCity(),
                    currentConditions.getStateAbbrev()));

            long lastSyncTime = mPreferences.getLong(getString(R.string.weather_last_sync_time_key), -1);
            Date updateDate = new Date(lastSyncTime);

            TextView date = (TextView) mRootView.findViewById(R.id.current_date);
            SimpleDateFormat sdf1 = new SimpleDateFormat("EEEE, MMM d", Locale.US);
            date.setText(sdf1.format(updateDate));

            TextView updated = (TextView) mRootView.findViewById(R.id.current_update_time);
            SimpleDateFormat sdf2 = new SimpleDateFormat("'updated at 'h:m a", Locale.US);
            updated.setText(sdf2.format(updateDate));

            TextView condition = (TextView) mRootView.findViewById(R.id.current_condition);
            condition.setText(currentConditions.getCurrConditionDesc());

            TextView temp = (TextView) mRootView.findViewById(R.id.current_temp);
            if (useMetric) {
                temp.setText(String.format("%d°", Math.round(currentConditions.getTempCel())));
            } else {
                temp.setText(String.format("%d°", Math.round(currentConditions.getTempFahr())));
            }

            ImageView icon = (ImageView) mRootView.findViewById(R.id.current_icon);
            RotateAnimation anim = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setDuration(250);
            Picasso.with(getContext()).load(currentConditions.getIconUrl()).into(icon);
            icon.startAnimation(anim);
        }

        //TODO - don't create a new recycler, adapter, layout manager on every update!

        // set up hourly forecasts card if data available
        if (hourlyForecasts != null && hourlyForecasts.size() > 0) {
            RecyclerView hourlyRecycler = (RecyclerView) mRootView
                    .findViewById(R.id.weather_hourly_recycler);
            hourlyRecycler.setLayoutManager(
                    new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            hourlyRecycler.setAdapter(new HourlyRecyclerAdapter(hourlyForecasts, useMetric));
        }

        // set up daily forecasts card if data available
        if (dailyForecasts != null && dailyForecasts.size() > 0) {
            RecyclerView dailyRecycler = (RecyclerView) mRootView
                    .findViewById(R.id.weather_daily_recycler);
            dailyRecycler.setLayoutManager(
                    new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            dailyRecycler.setAdapter(new DailyRecyclerAdapter(dailyForecasts, useMetric));
        }

        //TODO - stop a progress bar
    }

    private boolean syncNeeded() {
        long lastSyncTime = mPreferences.getLong(getString(R.string.weather_last_sync_time_key), -1);

        // return true if no last sync, or sync was not within threshold
        Log.d(TAG, "syncNeeded: millis since last sync " + (System.currentTimeMillis() - lastSyncTime));
        Log.d(TAG, "syncNeeded: threshold " + SYNC_THRESHOLD_IN_MILLIS);
        return lastSyncTime == -1 ||
                (System.currentTimeMillis() - lastSyncTime > SYNC_THRESHOLD_IN_MILLIS);
    }

    public void startSync() {
        // use device location or static location per user prefs
        boolean okToUseDeviceLocation = mPreferences.getBoolean(getString(R.string.pref_key_weather_geo), false);

        if (okToUseDeviceLocation) {
            Log.d(TAG, "startSync: using geolocation");

            // if already have location, use it
            if (mLastLocation != null) {
                Log.d(TAG, "startSync: already have location");
                requestWeatherSync(getQueryStringFromLocation(mLastLocation));
            } else if (mGoogleApiClient.isConnected()) {
                // can't call connect() w/o disconnecting first, so call callback manually
                onConnected(null);
            } else {
                mGoogleApiClient.connect();
            }
        } else {
            // if user disabled geolocation in preferences, use static location, if set
            Log.d(TAG, "startSync: using static location");
            String staticLocation = mPreferences
                    .getString(getString(R.string.pref_key_weather_static_location), null);

            if (staticLocation != null && !staticLocation.isEmpty() &&
                    !staticLocation.equals(getString(R.string.weather_static_location_default))) {
                requestWeatherSync(staticLocation);
            } else {
                // alert user to enable geolocation or enter a static location
                Log.d(TAG, "startSync: geolocation off & no static location set!");
                new AlertDialog.Builder(getContext())
                        .setTitle("Location needed")
                        .setMessage("Please enable device location or manually enter a location.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(getContext(), SettingsActivity.class));
                            }
                        })
                        .show();
            }
        }
    }

    private String getQueryStringFromLocation(Location location) {
        return location.getLatitude() + "," + location.getLongitude();
    }

    private void requestWeatherSync(String locationQuery) {
        Log.d(TAG, "requestWeatherSync: requesting sync for " + locationQuery);
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        settingsBundle.putString(SyncAdapter.LOCATION_QUERY_KEY, locationQuery);
        ContentResolver.requestSync(mAccount, getString(R.string.authority), settingsBundle);

        //TODO - start a progress bar
    }

    private class WeatherContentObserver extends ContentObserver {
        public WeatherContentObserver(Handler handler) {
            super(handler);
        }

        // Triggered when a sync is completed
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            Log.d(TAG, "onChange: ContentObserver triggered");
            updateViewsWithDataFromDb();
        }
    }
}
