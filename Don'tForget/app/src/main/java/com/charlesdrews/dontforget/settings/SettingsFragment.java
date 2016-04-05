package com.charlesdrews.dontforget.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.charlesdrews.dontforget.R;
import com.charlesdrews.dontforget.notifications.SchedulingService;

/**
 * Inflate preferences.xml and listen for changes
 * Created by charlie on 3/29/16.
 */
public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private WeatherLocationPreference mWeatherStaticLocation;
    private TimePickerPreference mBeforeWork, mLunchtime, mOnTheWayHome, mEvening;

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
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_key_weather_static_location))) {
            Preference pref = findPreference(key);
            pref.setSummary(((EditTextPreference) pref).getText());

        } else if (key.equals(getString(R.string.pref_key_notifications_enabled)) ||
                key.equals(getString(R.string.pref_key_notifications_days)) ||
                key.equals(getString(R.string.pref_key_notification_before_work)) ||
                key.equals(getString(R.string.pref_key_notification_lunchtime)) ||
                key.equals(getString(R.string.pref_key_notification_on_the_way_home)) ||
                key.equals(getString(R.string.pref_key_notification_evening))) {
            getActivity().startService(new Intent(getActivity(), SchedulingService.class));
        }
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
}
