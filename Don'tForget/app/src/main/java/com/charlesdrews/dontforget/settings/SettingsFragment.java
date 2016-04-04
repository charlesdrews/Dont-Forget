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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        mWeatherStaticLocation = (WeatherLocationPreference)
                findPreference(getString(R.string.pref_key_weather_static_location));
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
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
