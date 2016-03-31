package com.charlesdrews.dontforget.weather;


import android.Manifest;
import android.accounts.Account;
import android.content.ContentResolver;
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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.charlesdrews.dontforget.MainActivity;
import com.charlesdrews.dontforget.R;
import com.charlesdrews.dontforget.sync.AccountHelper;
import com.charlesdrews.dontforget.sync.StubProvider;
import com.charlesdrews.dontforget.sync.SyncAdapter;
import com.charlesdrews.dontforget.weather.model.HourlyForecast;
import com.charlesdrews.dontforget.weather.model.WeatherData;
import com.charlesdrews.dontforget.weather.model.WeatherDataHourly;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

public class WeatherFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = WeatherFragment.class.getSimpleName();
    private static final long SYNC_THRESHOLD_IN_MINUTES = 15;

    private WeatherContentObserver mContentObserver;
    private Account mAccount;
    private GoogleApiClient mGoogleApiClient;
    private Realm mRealm;
    private ArrayList<WeatherData> mData;
    private WeatherRecyclerAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public WeatherFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAccount = AccountHelper.getAccount(getContext());

        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        pullWeatherDataFromDb();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_weather, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView
                .findViewById(R.id.weather_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        RecyclerView recycler = (RecyclerView) rootView.findViewById(R.id.weather_recycler_view);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        mAdapter = new WeatherRecyclerAdapter(getContext(), mData);
        recycler.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mRealm == null || mRealm.isClosed()) {
            mRealm = Realm.getDefaultInstance();
        }

        // listen for data syncs
        mContentObserver = new WeatherContentObserver(new Handler());
        getContext().getContentResolver().registerContentObserver(
                Uri.parse(StubProvider.BASE_URI_STRING), true, mContentObserver);

        // get time of last weather data sync
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        long lastSyncTime = prefs.getLong(getString(R.string.weather_last_sync_time_key), -1);

        // if no last sync, or sync was not within threshold, initiate new sync
        if (lastSyncTime == -1 ||
                System.currentTimeMillis() - lastSyncTime > SYNC_THRESHOLD_IN_MINUTES * 1000) {
            initiateWeatherSync();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        getContext().getContentResolver().unregisterContentObserver(mContentObserver);

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

        if (mRealm != null && !mRealm.isClosed()) {
            mRealm.close();
        }
    }

    @Override
    public void onRefresh() {
        initiateWeatherSync();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected: connected to GoogleApiClient");

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
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (lastLocation != null) {
            Log.d(TAG, "onConnected: last location retrieved successfully");
            requestWeatherSync(getQueryStringFromLocation(lastLocation));
        } else {
        // if location not yet available, set up a location request & trigger sync from listener
            Log.d(TAG, "onConnected: last location not available; launching a location request");
            LocationRequest locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                    .setInterval(0)
                    .setFastestInterval(0);
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, locationRequest, this);
            // requestWeatherSync() is called from onLocationChanged
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Snackbar.make(
                getActivity().findViewById(android.R.id.content),
                "Unable to get device location",
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
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
        requestWeatherSync(getQueryStringFromLocation(location));
    }

    private void pullWeatherDataFromDb() {
        //TODO - make this async?
        if (mData == null) {
            mData = new ArrayList<>(3);
        } else {
            mData.clear();
        }

        mRealm = Realm.getDefaultInstance();

        //TODO - populate ArrayList w/ actual data
        mData.add(0, new WeatherData()); // current weather

        // get hourly forecast data from Realm db
        RealmResults<HourlyForecast> hourlyForecasts =
                mRealm.where(HourlyForecast.class).findAll();
        if (hourlyForecasts != null && hourlyForecasts.size() > 0) {
            //TODO - sort the results
            mData.add(1, new WeatherDataHourly(1, hourlyForecasts));
        } else {
            mData.add(1, new WeatherData());
        }

        mData.add(2, new WeatherData()); // daily weather

        if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private void initiateWeatherSync() {
        // use device location or static location per user prefs
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean useDeviceLocation = prefs.getBoolean(getString(R.string.pref_key_weather_geo), false);

        if (useDeviceLocation) {
            if (mGoogleApiClient.isConnected()) {
                // can't call connect() w/o disconnecting first, so call callback manually
                onConnected(null); // onConnected() calls requestWeatherSync()
            } else {
                mGoogleApiClient.connect();
            }
        } else {
            // if user disabled geolocation in preferences, use static location, if set
            String staticLocation = prefs
                    .getString(getString(R.string.pref_key_weather_static_location), null);

            if (staticLocation == null || staticLocation.isEmpty() ||
                    staticLocation.equals(getString(R.string.weather_static_location_default))) {
                //TODO - notify use to either enable geolocation or enter static location
                //redirect to Settings activity???
                return;
            }
            requestWeatherSync(staticLocation);
        }
    }

    private String getQueryStringFromLocation(Location location) {
        return location.getLatitude() + "," + location.getLongitude();
    }

    private void requestWeatherSync(String locationQuery) {
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        settingsBundle.putString(SyncAdapter.LOCATION_QUERY_KEY, locationQuery);
        ContentResolver.requestSync(mAccount, getString(R.string.authority), settingsBundle);
    }

    private class WeatherContentObserver extends ContentObserver {
        public WeatherContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            Log.d(TAG, "onChange: ContentObserver triggered");
            //TODO - make this async?
            pullWeatherDataFromDb();
            mAdapter.notifyDataSetChanged();
        }
    }
}
