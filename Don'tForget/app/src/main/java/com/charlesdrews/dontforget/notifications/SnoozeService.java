package com.charlesdrews.dontforget.notifications;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Allow notifications to be snoozed
 * Created by charlie on 4/4/16.
 */
public class SnoozeService extends IntentService {
    private static final String TAG = SnoozeService.class.getSimpleName();

    private static final int SNOOZE_MINUTES = 1; //TODO

    public SnoozeService() {
        super(TAG);
    }

    public SnoozeService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: snoozing...");

        int notificationType = intent.getIntExtra(SchedulingService.NOTIFICATION_TYPE_KEY, -1);

        if (notificationType == -1) {
            return; // cannot relaunch notification with invalid type
        }

        // dismiss the notification that brought us here
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(notificationType);
        Toast.makeText(
                SnoozeService.this,
                String.format("Snoozing for %d minutes", SNOOZE_MINUTES),
                Toast.LENGTH_SHORT)
                .show();

        // create intent and pending intent - specify which notification time/type
        Intent snoozeIntent = new Intent(this, NotificationsService.class);
        snoozeIntent.putExtra(SchedulingService.NOTIFICATION_TYPE_KEY, notificationType);

        // set an action so this intent will not be equivalent to those for other notification types
        snoozeIntent.setAction(TimeOfDay.getTimeOfDay(notificationType).toString());
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, snoozeIntent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MINUTE, SNOOZE_MINUTES);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }
}
