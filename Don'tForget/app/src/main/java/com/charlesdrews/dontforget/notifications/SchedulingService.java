package com.charlesdrews.dontforget.notifications;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.charlesdrews.dontforget.R;

import java.util.Calendar;

/**
 * Schedule the notifications service via alarm manager
 * Created by charlie on 4/4/16.
 */
public class SchedulingService extends IntentService {
    private static final String TAG = SchedulingService.class.getSimpleName();
    public static final String NOTIFICATION_TYPE_KEY = "notificationTypeKey";

    private boolean mShowNotifications;
    private AlarmManager mAlarmManager;

    public SchedulingService() {
        super(TAG);
    }
    public SchedulingService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: scheduling notifications...");

        // get notification times from shared prefs
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        mShowNotifications = prefs.getBoolean(getString(R.string.pref_key_notifications_enabled), true);

        String beforeWork = prefs.getString(
                getString(R.string.pref_key_notification_before_work),
                getString(R.string.before_work_default_time)
        );
        String lunchtime = prefs.getString(
                getString(R.string.pref_key_notification_lunchtime),
                getString(R.string.lunchtime_default_time)
        );
        String onTheWayHome = prefs.getString(
                getString(R.string.pref_key_notification_on_the_way_home),
                getString(R.string.on_the_way_home_default_time)
        );
        String evening = prefs.getString(
                getString(R.string.pref_key_notification_evening),
                getString(R.string.evening_default_time)
        );

        // set alarms for each notification
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        setAlarmForNotification(TimeOfDay.BEFORE_WORK, beforeWork);
        setAlarmForNotification(TimeOfDay.LUNCHTIME, lunchtime);
        setAlarmForNotification(TimeOfDay.ON_THE_WAY_HOME, onTheWayHome);
        setAlarmForNotification(TimeOfDay.EVENING, evening);

        //realm.close();
    }

    private void setAlarmForNotification(TimeOfDay timeOfDay, String hourMinString) {
        if (!hourMinString.matches("\\d+:\\d+")) {
            throw new IllegalArgumentException("Illegal value for " + timeOfDay.toString() +
                    " preference: " + hourMinString + "; must be h:m or h:mm");
        }

        String[] parts = hourMinString.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);

        // create intent and pending intent - specify which notification time/type
        Intent intent = new Intent(this, NotificationsService.class);
        intent.putExtra(NOTIFICATION_TYPE_KEY, timeOfDay.getInt());

        // set an action so this intent will not be equivalent to those for other notification types
        intent.setAction(timeOfDay.toString());
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);

        // compare each notification time to current time; if already past, set for tomorrow
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int currHour = calendar.get(Calendar.HOUR_OF_DAY); // 24 hr clock (Calendar.HOUR is 12 hr)
        int currMin = calendar.get(Calendar.MINUTE);

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minutes);
        if (hour < currHour || (hour == currHour && minutes < currMin) ) {
            calendar.add(Calendar.DATE, 1);
        }

        // cancel any existing alarms to avoid duplicates, then schedule new alarm
        mAlarmManager.cancel(pendingIntent);

        // run cancel() either way, but only set new alarm if user enabled notifications
        if (mShowNotifications) {
            // it appears that AlarmManager.INTERVAL_DAY is only recognized by setInexactRepeating()
            long interval = 24L * 60L * 60L * 1000L;
            mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    interval, pendingIntent);
        }
    }
}
