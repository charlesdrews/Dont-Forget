package com.charlesdrews.dontforget.weather;


import android.Manifest;
import android.accounts.Account;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.charlesdrews.dontforget.MainActivity;
import com.charlesdrews.dontforget.R;
import com.charlesdrews.dontforget.SettingsActivity;
import com.charlesdrews.dontforget.sync.AccountHelper;
import com.charlesdrews.dontforget.sync.StubProvider;
import com.charlesdrews.dontforget.sync.SyncAdapter;
import com.charlesdrews.dontforget.weather.model.CurrentConditionsRealm;
import com.charlesdrews.dontforget.weather.model.DailyForecastRealm;
import com.charlesdrews.dontforget.weather.model.HourlyForecastRealm;
import com.charlesdrews.dontforget.weather.model.LocationRealm;
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
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Handles retrieval of device location, triggers syncing of weather data, updates views on sync
 */
public class WeatherFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = WeatherFragment.class.getSimpleName();

    private static final long SYNC_THRESHOLD_IN_MINUTES = 30; // for auto syncs when app is open
    private static final long LOCATION_THRESHOLD_IN_MINUTES = 60; // also for auto syncs when app is open
    private static final long PERIODIC_SYNC_INTERVAL_IN_MINUTES = 120; // for background syncs

    private static final long SYNC_THRESHOLD_IN_MILLIS = SYNC_THRESHOLD_IN_MINUTES * 60 * 1000;
    private static final long LOCATION_THRESHOLD_IN_MILLIS = LOCATION_THRESHOLD_IN_MINUTES * 60 * 1000;
    private static final long PERIODIC_SYNC_INTERVAL_IN_SECONDS = PERIODIC_SYNC_INTERVAL_IN_MINUTES * 60;

    private SharedPreferences mPreferences;
    private boolean mUseMetric, mCanUseDeviceLocation;
    private String mStaticLocation;
    private Account mAccount;
    private WeatherContentObserver mContentObserver;
    private GoogleApiClient mGoogleApiClient;
    private Realm mRealm;
    private LocationRealm mLastDeviceLocationUsed;
    private RealmResults<CurrentConditionsRealm> mCurrentConditions;
    private RealmResults<HourlyForecastRealm> mHourlyForecasts;
    private RealmResults<DailyForecastRealm> mDailyForecasts;
    private HourlyRecyclerAdapter mHourlyAdapter;
    private DailyRecyclerAdapter mDailyAdapter;
    private View mRootView;
    private ProgressBar mProgressBar;
    private TextView mProgressText;

    public WeatherFragment() {}


    //==============================================================================================
    //========== Fragment lifecycle methods ========================================================
    //==============================================================================================
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        updateUserPreferences();

        // prepare for data syncs w/ sync account & sync listener (content observer)
        mAccount = AccountHelper.getAccount(getContext());
        mContentObserver = new WeatherContentObserver(new Handler());

        // initialize Google API Client to allow for retrieval of device location when needed
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // pull most recent data from Realm database
        mRealm = Realm.getDefaultInstance();
        mLastDeviceLocationUsed = mRealm.where(LocationRealm.class).findFirstAsync();
        mCurrentConditions = mRealm.where(CurrentConditionsRealm.class)
                .findAllSortedAsync("timeObtainedInMillis");
        mHourlyForecasts = mRealm.where(HourlyForecastRealm.class)
                .findAllSortedAsync("dateTime");
        mDailyForecasts = mRealm.where(DailyForecastRealm.class)
                .findAllSortedAsync("date");

        // initialize recycler view adapters & trigger adapter updates when data changes
        mHourlyAdapter = new HourlyRecyclerAdapter(getContext(), mHourlyForecasts, mUseMetric);
        mDailyAdapter = new DailyRecyclerAdapter(getContext(), mDailyForecasts, mUseMetric);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_weather, container, false);

        mProgressBar = (ProgressBar) mRootView.findViewById(R.id.weather_progress_bar);
        mProgressText = (TextView) mRootView.findViewById(R.id.weather_progress_text);

        updateCurrentConditionsCard(false);

        RecyclerView hourlyRecycler = (RecyclerView) mRootView.findViewById(R.id.weather_hourly_recycler);
        RecyclerView dailyRecycler = (RecyclerView) mRootView.findViewById(R.id.weather_daily_recycler);

        hourlyRecycler.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        dailyRecycler.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        hourlyRecycler.setAdapter(mHourlyAdapter);
        dailyRecycler.setAdapter(mDailyAdapter);

        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // begin listening for data syncs
        getContext().getContentResolver().registerContentObserver(
                Uri.parse(StubProvider.BASE_URI_STRING), true, mContentObserver);

        // begin listening for database changes
        mCurrentConditions.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                Log.d(TAG, "onChange: mCurrentConditions changed");
                if (mCurrentConditions.size() == 0) {
                    mRootView.findViewById(R.id.card_current).setVisibility(View.GONE);
                } else {
                    mRootView.findViewById(R.id.card_current).setVisibility(View.VISIBLE);
                    updateCurrentConditionsCard(true);
                }
            }
        });
        mHourlyForecasts.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                Log.d(TAG, "onChange: hourlyForecasts changed");
                if (mHourlyForecasts.size() == 0) {
                    mRootView.findViewById(R.id.card_hourly).setVisibility(View.GONE);
                } else {
                    mRootView.findViewById(R.id.card_hourly).setVisibility(View.VISIBLE);
                    mHourlyAdapter.notifyDataSetChanged();
                }
            }
        });
        mDailyForecasts.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                Log.d(TAG, "onChange: dailyForecasts changed");
                if (mDailyForecasts.size() == 0) {
                    mRootView.findViewById(R.id.card_daily).setVisibility(View.GONE);
                } else {
                    mRootView.findViewById(R.id.card_daily).setVisibility(View.VISIBLE);
                    mDailyAdapter.notifyDataSetChanged();
                }
            }
        });

        // update user preferences
        boolean previousUseMetricIndicator = mUseMetric;
        updateUserPreferences(); // updates mUseMetric
        if (mUseMetric != previousUseMetricIndicator) {
            // if mUserMetric changed, update views
            mHourlyAdapter.setUseMetric(mUseMetric);
            mDailyAdapter.setUseMetric(mUseMetric);
            updateCurrentConditionsCard(true);
        }

        // initiate a new sync if last sync too long ago, or if never synced before
        if (syncNeeded()) {
            startSync(false); // false -> automatic sync, not sync manually initiated by user
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // stop observer & listeners
        getContext().getContentResolver().unregisterContentObserver(mContentObserver);
        mCurrentConditions.removeChangeListeners();
        mHourlyForecasts.removeChangeListeners();
        mDailyForecasts.removeChangeListeners();
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


    //==============================================================================================
    //========== Google API Client callback methods & location listener ============================
    //==============================================================================================
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
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (lastLocation != null) {
            Log.d(TAG, "onConnected: last location retrieved successfully");
            String locationString = getQueryStringFromLocation(lastLocation);
            requestWeatherSync(locationString);
            saveLocationStringToDatabase(locationString);
        } else {
            // if location not yet available, set up a location request & trigger sync from listener
            Log.d(TAG, "onConnected: last location not available; launching a location request");
            LocationRequest locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                    .setInterval(1)
                    .setFastestInterval(1);
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, locationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        stopProgressBar("onConnectionSuspended");
        Snackbar.make(
                getActivity().findViewById(android.R.id.content),
                getString(R.string.unable_to_get_device_location),
                Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        stopProgressBar("onConnectionFailed");
        Snackbar.make(
                getActivity().findViewById(android.R.id.content),
                getString(R.string.unable_to_get_device_location),
                Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged: new location received");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        String locationString = getQueryStringFromLocation(location);
        requestWeatherSync(locationString);
        saveLocationStringToDatabase(locationString);
    }


    //==============================================================================================
    //========== Helper methods ====================================================================
    //==============================================================================================
    private void updateUserPreferences() {
        mUseMetric = "Metric".equals(mPreferences.getString(
                getString(R.string.pref_key_weather_units),
                getString(R.string.weather_default_unit)));

        mCanUseDeviceLocation = mPreferences.getBoolean(
                getString(R.string.pref_key_weather_use_device_location), true);

        mStaticLocation = mPreferences.getString(
                getString(R.string.pref_key_weather_static_location), null);

    }

    private boolean syncNeeded() {
        if (mCurrentConditions == null) {
            return true;
        }

        if (!mCurrentConditions.isLoaded()) {
            mCurrentConditions.load(); // finish loading synchronously, if necessary
        }

        if (!mCurrentConditions.isValid() || mCurrentConditions.size() == 0) {
            return true;
        }

        CurrentConditionsRealm current = mCurrentConditions.first();
        return (System.currentTimeMillis() - current.getTimeObtainedInMillis() > SYNC_THRESHOLD_IN_MILLIS);
    }

    public void startSync(boolean userTappedRefreshButton) {
        startProgressBar();

        switch (getSyncCase(userTappedRefreshButton)) {
            case NO_NETWORK_CONNECTION:
                Log.d(TAG, "onResume: sync needed, but no internet connection");
                stopProgressBar("startSync - NO_NETWORK_CONNECTION");
                Snackbar.make(
                        getActivity().findViewById(android.R.id.content),
                        getString(R.string.no_internet_connection),
                        Snackbar.LENGTH_LONG)
                        .show();
                break;

            case SYNC_WITH_NEW_DEVICE_LOCATION:
                Log.d(TAG, "onResume: sync needed; new device location needed first");
                setProgressText(getString(R.string.getting_device_location));
                if (mGoogleApiClient.isConnected()) {
//                    mGoogleApiClient.reconnect();
                    onConnected(Bundle.EMPTY);
                } else {
                    mGoogleApiClient.connect();
                }
                break;

            case SYNC_WITH_EXISTING_DEVICE_LOCATION:
                Log.d(TAG, "onResume: sync needed; reuse last-used device location");
                requestWeatherSync(mLastDeviceLocationUsed.getLocationString());
                break;

            case SYNC_WITH_STATIC_LOCATION:
                Log.d(TAG, "onResume: sync needed; use static location");
                requestWeatherSync(mStaticLocation);
                break;

            case NEED_USER_INPUT:
                Log.d(TAG, "onResume: sync needed, but can't use device location & no static location set");
                stopProgressBar("startSync - NEED_USER_INPUT");
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
                break;
        }
    }

    private SyncCase getSyncCase(boolean userTappedRefreshButton) {
        if (!haveInternetConnectivity()) {
            return SyncCase.NO_NETWORK_CONNECTION;
        }

        if (mCanUseDeviceLocation) {
            if (userTappedRefreshButton || newDeviceLocationNeeded()) {
                // always get new location if user manually requested the sync
                return SyncCase.SYNC_WITH_NEW_DEVICE_LOCATION;
            } else {
                return SyncCase.SYNC_WITH_EXISTING_DEVICE_LOCATION;
            }
        }

        if (staticLocationIsValid()) {
            return SyncCase.SYNC_WITH_STATIC_LOCATION;
        }

        return SyncCase.NEED_USER_INPUT;
    }

    private boolean newDeviceLocationNeeded() {
        if (mLastDeviceLocationUsed == null) {
            return true;
        }

        if (!mLastDeviceLocationUsed.isLoaded()) {
            mLastDeviceLocationUsed.load(); // finish loading synchronously, if necessary
        }

        return !mLastDeviceLocationUsed.isValid() ||
                (System.currentTimeMillis() - mLastDeviceLocationUsed.getTimeObtainedInMillis()
                        > LOCATION_THRESHOLD_IN_MILLIS);
    }

    private boolean staticLocationIsValid() {
        return mStaticLocation != null && !mStaticLocation.isEmpty() &&
                !mStaticLocation.equals(getString(R.string.weather_static_location_default));
    }

    private boolean haveInternetConnectivity() {
        ConnectivityManager manager = (ConnectivityManager) getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void updateCurrentConditionsCard(boolean doRotationAnimations) {
        if (mCurrentConditions == null) {
            return;
        }

        if (!mCurrentConditions.isLoaded()) {
            mCurrentConditions.load(); // finish loading synchronously, if necessary
        }

        if (!mCurrentConditions.isValid() || mCurrentConditions.size() == 0) {
            return;
        }

        CurrentConditionsRealm current = mCurrentConditions.first();

        TextView location = (TextView) mRootView.findViewById(R.id.current_location);
        location.setText(String.format("%s, %s", current.getCity(),
                current.getStateAbbrev()));

        Date updateDate = new Date(current.getTimeObtainedInMillis());

        TextView date = (TextView) mRootView.findViewById(R.id.current_date);
        SimpleDateFormat sdf1 = new SimpleDateFormat("EEEE, MMM d", Locale.US);
        date.setText(sdf1.format(updateDate));

        TextView updated = (TextView) mRootView.findViewById(R.id.current_update_time);
        SimpleDateFormat sdf2 = new SimpleDateFormat("'updated at 'h:mm a", Locale.US);
        updated.setText(sdf2.format(updateDate));

        TextView condition = (TextView) mRootView.findViewById(R.id.current_condition);
        condition.setText(current.getCurrConditionDesc());

        TextView temp = (TextView) mRootView.findViewById(R.id.current_temp);
        if (mUseMetric) {
            temp.setText(String.format(Locale.US, "%d°", Math.round(current.getTempCel())));
        } else {
            temp.setText(String.format(Locale.US, "%d°", Math.round(current.getTempFahr())));
        }

        ImageView icon = (ImageView) mRootView.findViewById(R.id.current_icon);
        Picasso.with(getContext()).load(current.getIconUrl()).into(icon);

        if (doRotationAnimations) {
            ObjectAnimator iconAnim = ObjectAnimator.ofFloat(icon, "rotationY", 0f, 360f);
            iconAnim.setDuration(750);
            iconAnim.setInterpolator(new AccelerateDecelerateInterpolator());

            ObjectAnimator tempAnim = ObjectAnimator.ofFloat(temp, "rotationY", 0f, 360f);
            tempAnim.setDuration(750);
            tempAnim.setInterpolator(new AccelerateDecelerateInterpolator());

            iconAnim.start();
            tempAnim.start();
        }

        Log.d(TAG, "updateCurrentConditionsCard: update complete");
    }

    private String getQueryStringFromLocation(Location location) {
        return location.getLatitude() + "," + location.getLongitude();
    }

    private void requestWeatherSync(String locationQuery) {
        Log.d(TAG, "requestWeatherSync: requesting sync for " + locationQuery);
        setProgressText(getString(R.string.retrieving_weather_data));

        String authority = getString(R.string.authority);

        // Cancel any existing syncs first
        ContentResolver.cancelSync(mAccount, authority);

        // Request immediate, one-time sync
        Bundle oneTimeSyncExtras = new Bundle();
        oneTimeSyncExtras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        oneTimeSyncExtras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        oneTimeSyncExtras.putString(SyncAdapter.LOCATION_QUERY_KEY, locationQuery);
        ContentResolver.requestSync(mAccount, authority, oneTimeSyncExtras);

        // Add a periodic, ongoing sync
        ContentResolver.setSyncAutomatically(mAccount, authority, true);
        Bundle periodicSyncExtras = new Bundle();
        periodicSyncExtras.putString(SyncAdapter.LOCATION_QUERY_KEY, locationQuery);
        periodicSyncExtras.putBoolean("PERIODIC_SYNC_KEY", true);
        ContentResolver.addPeriodicSync(mAccount, authority, periodicSyncExtras,
                PERIODIC_SYNC_INTERVAL_IN_SECONDS);
    }

    private void saveLocationStringToDatabase(String locationString) {
        LocationRealm locationRealm = new LocationRealm(locationString);
        mRealm.beginTransaction();
        mRealm.copyToRealmOrUpdate(locationRealm);
        mRealm.commitTransaction();
    }

    private void startProgressBar() {
        mProgressBar.setAlpha(0f);
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.animate().alpha(1f).setDuration(500);
    }

    private void setProgressText(String text) {
        if (mProgressText.getVisibility() == View.VISIBLE) {
            mProgressText.animate().alpha(0f).setDuration(250)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mProgressText.setVisibility(View.GONE);
                        }
                    });
        }
        mProgressText.setText(text);
        mProgressText.setAlpha(0f);
        mProgressText.setVisibility(View.VISIBLE);
        mProgressText.animate().alpha(1f).setDuration(500);
    }

    private void stopProgressBar(String from) {
        mProgressBar.animate().alpha(0f).setDuration(1000)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
        mProgressText.animate().alpha(0f).setDuration(1000)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mProgressText.setVisibility(View.GONE);
                    }
                });
        Log.d(TAG, "stopProgressBar: from " + from);
    }


    //==============================================================================================
    //========== Enumerate the possible cases when a sync is needed ================================
    //==============================================================================================
    private enum SyncCase {
        NO_NETWORK_CONNECTION,              // notify user a connection is needed
        SYNC_WITH_NEW_DEVICE_LOCATION,      // use Google API Client to get new location
        SYNC_WITH_EXISTING_DEVICE_LOCATION, // last location saved in database
        SYNC_WITH_STATIC_LOCATION,          // static location saved in shared prefs
        NEED_USER_INPUT     // user turned off device location & did not provide static location
    }


    //==============================================================================================
    //========== Content Observer - detects when sync adapter finishes a new sync ==================
    //==============================================================================================
    private class WeatherContentObserver extends ContentObserver {
        public WeatherContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            stopProgressBar("ContentObserver - onChange");
            if (uri.compareTo(StubProvider.WEATHER_URI_SUCCESS) == 0) {
                Log.d(TAG, "onChange: sync completed successfully");
            } else {
                Log.d(TAG, "onChange: sync failed");
                Snackbar.make(
                        getActivity().findViewById(android.R.id.content),
                        getString(R.string.error_retrieving_weather_data),
                        Snackbar.LENGTH_LONG)
                        .show();
            }
        }
    }
}
