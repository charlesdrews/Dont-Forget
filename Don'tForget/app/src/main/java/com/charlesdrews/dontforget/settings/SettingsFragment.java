package com.charlesdrews.dontforget.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;

import com.charlesdrews.dontforget.R;
import com.charlesdrews.dontforget.notifications.SchedulingService;

import java.util.Set;

/**
 * Inflate preferences.xml and listen for changes
 * Created by charlie on 3/29/16.
 */
public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private WeatherLocationPreference mWeatherStaticLocation;
    private TimePickerPreference mBeforeWork, mLunchtime, mOnTheWayHome, mEvening;
    private MultiSelectListPreference mDays;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        mWeatherStaticLocation = (WeatherLocationPreference)
                findPreference(getString(R.string.pref_key_weather_static_location));

        mBeforeWork = (TimePickerPreference)
                findPreference(getString(R.string.pref_key_notification_before_work));
        mLunchtime = (TimePickerPreference)
                findPreference(getString(R.string.pref_key_notification_lunchtime));
        mOnTheWayHome = (TimePickerPreference)
                findPreference(getString(R.string.pref_key_notification_on_the_way_home));
        mEvening = (TimePickerPreference)
                findPreference(getString(R.string.pref_key_notification_evening));

        mDays = (MultiSelectListPreference)
                findPreference(getString(R.string.pref_key_notifications_days));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_key_notifications_enabled))) {
            getActivity().startService(new Intent(getActivity(), SchedulingService.class));

        } else if (key.equals(getString(R.string.pref_key_notifications_days))) {
            getActivity().startService(new Intent(getActivity(), SchedulingService.class));
            updateDaysSummary();

        } else if (key.equals(getString(R.string.pref_key_notification_before_work)) ||
                key.equals(getString(R.string.pref_key_notification_lunchtime)) ||
                key.equals(getString(R.string.pref_key_notification_on_the_way_home)) ||
                key.equals(getString(R.string.pref_key_notification_evening))) {

            getActivity().startService(new Intent(getActivity(), SchedulingService.class));
            Snackbar.make(
                    getActivity().findViewById(android.R.id.content),
                    getString(R.string.notification_prefs_updated),
                    Snackbar.LENGTH_LONG)
                    .show();

        } else if (key.equals(getString(R.string.pref_key_weather_use_device_location))) {
            boolean useDeviceLocation = sharedPreferences.getBoolean(key, false);

            String staticLocation = sharedPreferences.getString(
                    getString(R.string.pref_key_weather_static_location), null);
            boolean staticLocationNotSet = staticLocation == null ||
                    staticLocation.isEmpty() ||
                    staticLocation.equals(getString(R.string.weather_static_location_default));

            if (!useDeviceLocation && staticLocationNotSet) {
                launchLocationNeededAlert();
            }

        } else if (key.equals(getString(R.string.pref_key_weather_static_location))) {
            Preference pref = findPreference(key);

            String staticLocation = ((EditTextPreference) pref).getText();
            pref.setSummary(staticLocation);

            boolean useDeviceLocation = sharedPreferences
                    .getBoolean(getString(R.string.pref_key_weather_use_device_location), false);

            if (staticLocation == null || staticLocation.isEmpty() ||
                    staticLocation.equals(getString(R.string.weather_static_location_default))) {
                if (!useDeviceLocation) {
                    launchLocationNeededAlert();
                }
            } else {

            }
        }

    }

    private void launchLocationNeededAlert() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Location needed")
                .setMessage("Please enable device location or manually enter a location.")
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();

        mWeatherStaticLocation.setSummary(mWeatherStaticLocation.getText());

        SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();

        mBeforeWork.setSummary(getTimeSummary(
                prefs.getString(
                        getString(R.string.pref_key_notification_before_work),
                        getString(R.string.before_work_default_time)
                )
        ));
        mLunchtime.setSummary(getTimeSummary(
                prefs.getString(
                        getString(R.string.pref_key_notification_lunchtime),
                        getString(R.string.lunchtime_default_time)
                )
        ));
        mOnTheWayHome.setSummary(getTimeSummary(
                prefs.getString(
                        getString(R.string.pref_key_notification_on_the_way_home),
                        getString(R.string.on_the_way_home_default_time)
                )
        ));
        mEvening.setSummary(getTimeSummary(
                prefs.getString(
                        getString(R.string.pref_key_notification_evening),
                        getString(R.string.evening_default_time)
                )
        ));

        updateDaysSummary();

        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private String getTimeSummary(String time) {
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        String hourSummary = hour > 12 ? String.valueOf(hour - 12) : String.valueOf(hour);
        String minutesSummary = minutes < 10 ? "0" + minutes : String.valueOf(minutes);
        String amPm = hour < 12 ? " am" : " pm";
        return hourSummary + ":" + minutesSummary + amPm;
    }

    private void updateDaysSummary() {
        String[] allDays = getResources().getStringArray(R.array.notification_days);
        StringBuilder builder = new StringBuilder();
        Set<String> selectedDays = mDays.getValues();

        for (String day : allDays) {
            if (selectedDays.contains(day)) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                builder.append(day.substring(0, 3));
            }
        }
        mDays.setSummary(builder.toString());
    }
}
