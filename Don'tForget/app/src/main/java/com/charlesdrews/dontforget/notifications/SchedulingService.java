package com.charlesdrews.dontforget.notifications;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import com.charlesdrews.dontforget.notifications.model.NotificationTimeRealm;

import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Schedule the notifications service via alarm manager
 * Created by charlie on 4/4/16.
 */
public class SchedulingService extends IntentService {
    private static final String TAG = SchedulingService.class.getSimpleName();
    public static final String NOTIFICATION_TYPE_KEY = "notificationTypeKey";

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

        // initialize
        Realm realm = Realm.getInstance(new RealmConfiguration.Builder(this).build());
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // get notification times from db
        NotificationTimeRealm beforeWork = realm.where(NotificationTimeRealm.class)
                .equalTo("notificationId", TimeOfDay.BEFORE_WORK.getInt()).findFirst();
        NotificationTimeRealm lunchtime = realm.where(NotificationTimeRealm.class)
                .equalTo("notificationId", TimeOfDay.LUNCHTIME.getInt()).findFirst();
        NotificationTimeRealm onTheWayHome = realm.where(NotificationTimeRealm.class)
                .equalTo("notificationId", TimeOfDay.ON_THE_WAY_HOME.getInt()).findFirst();
        NotificationTimeRealm evening = realm.where(NotificationTimeRealm.class)
                .equalTo("notificationId", TimeOfDay.EVENING.getInt()).findFirst();

        // if any notifications are not set, use defaults
        if (beforeWork == null) {
            beforeWork = new NotificationTimeRealm();
            beforeWork.setNotificationId(TimeOfDay.BEFORE_WORK.getInt());
            beforeWork.setNotificationHour(8);
            beforeWork.setNotificationMinutes(0);

            realm.beginTransaction();
            realm.copyToRealm(beforeWork);
            realm.commitTransaction();
        }
        if (lunchtime == null) {
            lunchtime = new NotificationTimeRealm();
            lunchtime.setNotificationId(TimeOfDay.LUNCHTIME.getInt());
            lunchtime.setNotificationHour(12);
            lunchtime.setNotificationMinutes(0);

            realm.beginTransaction();
            realm.copyToRealm(lunchtime);
            realm.commitTransaction();
        }
        if (onTheWayHome == null) {
            onTheWayHome = new NotificationTimeRealm();
            onTheWayHome.setNotificationId(TimeOfDay.ON_THE_WAY_HOME.getInt());
            onTheWayHome.setNotificationHour(17); // 5 pm
            onTheWayHome.setNotificationMinutes(0);

            realm.beginTransaction();
            realm.copyToRealm(onTheWayHome);
            realm.commitTransaction();
        }
        if (evening == null) {
            evening = new NotificationTimeRealm();
            evening.setNotificationId(TimeOfDay.EVENING.getInt());
            evening.setNotificationHour(19); // 7 pm
            evening.setNotificationMinutes(0);

            realm.beginTransaction();
            realm.copyToRealm(evening);
            realm.commitTransaction();
        }

        // set alarms for each notification
        setAlarmForNotification(beforeWork);
        setAlarmForNotification(lunchtime);
        setAlarmForNotification(onTheWayHome);
        setAlarmForNotification(evening);

        realm.close();
    }

    private void setAlarmForNotification(NotificationTimeRealm notification) {

        // create intent and pending intent - specify which notification time/type
        Intent intent = new Intent(this, NotificationsService.class);
        intent.putExtra(NOTIFICATION_TYPE_KEY, notification.getNotificationId());

        // set an action so this intent will not be equivalent to those for other notification types
        intent.setAction(TimeOfDay.getTimeOfDay(notification.getNotificationId()).toString());
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);

        // compare each notification time to current time; if already past, set for tomorrow
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int currHour = calendar.get(Calendar.HOUR_OF_DAY); // 24 hr clock (Calendar.HOUR is 12 hr)
        int currMin = calendar.get(Calendar.MINUTE);

        calendar.set(Calendar.HOUR_OF_DAY, notification.getNotificationHour());
        calendar.set(Calendar.MINUTE, notification.getNotificationMinutes());
        if (notification.getNotificationHour() < currHour ||
                (notification.getNotificationHour() == currHour &&
                        notification.getNotificationMinutes() < currMin)
                ) {
            calendar.add(Calendar.DATE, 1);
        }

        // cancel any existing alarms to avoid duplicates and schedule new alarm
        mAlarmManager.cancel(pendingIntent);

        //TODO ****************************
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 5 + 10 * notification.getNotificationId());

        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);
    }
}
