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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.charlesdrews.dontforget.MainActivity;
import com.charlesdrews.dontforget.R;
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

import io.realm.Realm;
import io.realm.RealmResults;

public class WeatherFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = WeatherFragment.class.getSimpleName();
    private static final long SYNC_THRESHOLD_IN_MINUTES = 15;

    private SharedPreferences mPreferences;
    private WeatherContentObserver mContentObserver;
    private Account mAccount;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Realm mRealm;
    private CurrentConditionsRealm mCurrentConditions;
    private RealmResults<HourlyForecastRealm> mHourlyForecasts;
    private RealmResults<DailyForecastRealm> mDailyForecasts;

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
        View rootView = inflater.inflate(R.layout.fragment_weather, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.weather_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // listen for data syncs
        getContext().getContentResolver().registerContentObserver(
                Uri.parse(StubProvider.BASE_URI_STRING), true, mContentObserver);

        updateViewsWithDataFromDb();

        if (syncNeeded()) {
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
    public void onRefresh() {
        startSync();
    }

    private void stopRefreshingAnimation() {
        if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
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
        mCurrentConditions = mRealm.where(CurrentConditionsRealm.class).findFirst();
        Log.d(TAG, "execute: retrieved current conditions from Realm");

        mHourlyForecasts = mRealm.where(HourlyForecastRealm.class).findAllSorted("dateTime");
        Log.d(TAG, "execute: retrieved hourly forecasts from Realm");

        mDailyForecasts = mRealm.where(DailyForecastRealm.class).findAllSorted("date");
        Log.d(TAG, "execute: retrieved daily forecasts from Realm");

        //TODO
        Log.d(TAG, "updateViewsWithDataFromDb: laieurghleadiuha;ehiorulaedksjfhasldi");

        stopRefreshingAnimation();
    }

    private boolean syncNeeded() {
        long lastSyncTime = mPreferences.getLong(getString(R.string.weather_last_sync_time_key), -1);

        // return true if no last sync, or sync was not within threshold
        return lastSyncTime == -1 ||
                (System.currentTimeMillis() - lastSyncTime > SYNC_THRESHOLD_IN_MINUTES * 1000);
    }

    private void startSync() {
        // use device location or static location per user prefs
        boolean okToUseDeviceLocation = mPreferences.getBoolean(getString(R.string.pref_key_weather_geo), false);

        if (okToUseDeviceLocation) {
            Log.d(TAG, "startSync: using geolocation");

            // if already have location, use it
            if (mLastLocation != null) {
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
                Log.d(TAG, "startSync: geolocation off & no static location set!");
                //TODO - notify use to either enable geolocation or enter static location - redirect to Settings activity???
            }
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

        // Triggered when a sync is completed
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            Log.d(TAG, "onChange: ContentObserver triggered");
            updateViewsWithDataFromDb();
        }
    }
}
