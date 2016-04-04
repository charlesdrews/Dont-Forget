package com.charlesdrews.dontforget.notifications;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

/**
 * Schedule the notifications service via alarm manager
 * Created by charlie on 4/4/16.
 */
public class SchedulingService extends IntentService {
    private static final String TAG = SchedulingService.class.getSimpleName();

    public SchedulingService() {
        super(TAG);
    }
    public SchedulingService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: scheduling...");
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent notificationIntent = new Intent(this, NotificationsService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, notificationIntent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 5);

        alarmManager.cancel(pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                60 * 1000, pendingIntent);
    }
}
