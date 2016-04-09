package com.charlesdrews.dontforget.weather;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by charlie on 4/8/16.
 */
public class WeatherSyncService extends IntentService {
    private static final String TAG = WeatherSyncService.class.getSimpleName();

    public WeatherSyncService() {
        super(TAG);
    }

    public WeatherSyncService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
