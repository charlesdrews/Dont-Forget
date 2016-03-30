package com.charlesdrews.dontforget.weather;


import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.charlesdrews.dontforget.MainActivity;
import com.charlesdrews.dontforget.R;
import com.charlesdrews.dontforget.sync.StubProvider;
import com.charlesdrews.dontforget.sync.SyncAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


public class WeatherFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = WeatherFragment.class.getSimpleName();
    private static final long SYNC_THRESHOLD_IN_MINUTES = 15;

    private Context mContext;
    private WeatherContentObserver mContentObserver;
    private AccountManager mAccountManager;
    private Account mAccount;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private RecyclerView mRecycler;

    public WeatherFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = container.getContext();
        View rootView = inflater.inflate(R.layout.fragment_weather, container, false);
        mRecycler = (RecyclerView) rootView.findViewById(R.id.weather_recycler_view);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set up account for syncing
        mAccountManager = AccountManager.get(mContext);
        mAccount = getAccount();
        if (mAccount == null) {
            mAccount = createAccount();
        }

        /*
        mRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        //TODO - populate ArrayList w/ actual data
        ArrayList<WeatherData> data = new ArrayList<>(3);
        data.add(0, new WeatherData()); // current weather

        // get hourly forecast data from Realm db
        Realm realm = Realm.getDefaultInstance();
        //TODO - sort the results
        RealmResults<HourlyForecast> hourlyForecasts = realm.where(HourlyForecast.class)
                .findAll();
        data.add(1, new WeatherDataHourly(1, hourlyForecasts));

        data.add(2, new WeatherData()); // daily weather

        WeatherRecyclerAdapter adapter = new WeatherRecyclerAdapter(getActivity(), data);
        mRecycler.setAdapter(adapter);
        */
    }

    //TODO - refresh data on pullDown gesture


    @Override
    public void onResume() {
        super.onResume();

        mContentObserver = new WeatherContentObserver(new Handler());
        mContext.getContentResolver().registerContentObserver(
                Uri.parse(StubProvider.BASE_URI_STRING), true, mContentObserver);

        // get time of last weather data sync
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        long lastSyncTime = prefs.getLong(MainActivity.WEATHER_LAST_SYNC_TIME_KEY, -1);

        // if last sync was within the threshold, return now without a new sync
        if (lastSyncTime > 0 &&
                System.currentTimeMillis() - lastSyncTime < SYNC_THRESHOLD_IN_MINUTES * 1000) {
            return;
        }

        // otherwise, request a new sync - use device location or static location per user prefs
        boolean useDeviceLocation = prefs.getBoolean(getString(R.string.pref_key_weather_geo), false);
        if (useDeviceLocation) {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect(); // sync triggered from onConnected()
        } else {
            String staticLocation = prefs
                    .getString(getString(R.string.pref_key_weather_static_location), null);

            if (staticLocation == null || staticLocation.isEmpty() ||
                    staticLocation.equals(getString(R.string.weather_static_location_default))) {
                //TODO - notify use to either enable geolocation or enter static location
                //redirect to Settings activity???
                return;
            }

            requestNewSync(staticLocation);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mContext.getContentResolver().unregisterContentObserver(mContentObserver);
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected: connected to GoogleApiClient");

        // if permission not granted, launch MainActivity and tell it to call requestPermissions()
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG, "onConnected: permission NOT granted - start MainActivity");
            Intent intent = new Intent(mContext, MainActivity.class);
            intent.putExtra(MainActivity.REQUEST_LOCATION_PERMISSION_KEY, true);
            mContext.startActivity(intent);
            return;
        }

        // otherwise, get last known location & call weather API
        Log.d(TAG, "onConnected: permission granted, getting last location");
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        // if location is available, request new sync using location data
        if (lastLocation != null) {
            Log.d(TAG, "onConnected: last location retrieved successfully");
            requestNewSync(getQueryStringFromLocation(lastLocation));
        // if location not yet available, set up a location request & trigger sync from listener
        } else {
            Log.d(TAG, "onConnected: last location was null; request location updates");
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                    .setInterval(5 * 1000)         // 5 seconds, in milliseconds
                    .setFastestInterval(1 * 1000);  // 1 second, in milliseconds
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        //TODO
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //TODO
    }

    @Override
    public void onLocationChanged(Location location) {
        requestNewSync(getQueryStringFromLocation(location));
    }

    public Account createAccount() {
        Account newAccount = new Account(
                getString(R.string.account),
                getString(R.string.account_type)
        );

        if (mAccountManager.addAccountExplicitly(newAccount, null, null)) {
            Log.d(TAG, "createAccount: success");
        } else {
            Log.d(TAG, "createAccount: failed");
            //TODO - try a second time?
        }

        return newAccount;
    }

    private Account getAccount() {
        // Return the first account of type account_type, or null if none
        Account[] accounts = mAccountManager.getAccountsByType(getString(R.string.account_type));
        if (accounts.length > 0) {
            return accounts[0];
        }
        return null;
    }

    public String getQueryStringFromLocation(Location location) {
        return location.getLatitude() + "," + location.getLongitude();
    }

    public void requestNewSync(String locationQuery) {
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
            //TODO - update views
        }
    }
}
