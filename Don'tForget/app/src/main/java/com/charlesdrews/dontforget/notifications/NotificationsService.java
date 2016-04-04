package com.charlesdrews.dontforget.notifications;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * Deliver notifications to the user based on weather, tasks, and birthdays
 * Created by charlie on 4/4/16.
 */
public class NotificationsService extends IntentService {
    private static final String TAG = NotificationsService.class.getSimpleName();

    public NotificationsService() {
        super(TAG);
    }

    public NotificationsService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: notifying...");
    }
}
