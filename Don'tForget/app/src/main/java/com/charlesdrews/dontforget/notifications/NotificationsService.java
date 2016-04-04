package com.charlesdrews.dontforget.notifications;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.charlesdrews.dontforget.MainActivity;
import com.charlesdrews.dontforget.R;

/**
 * Deliver notifications to the user based on weather, tasks, and birthdays
 * Created by charlie on 4/4/16.
 */
public class NotificationsService extends IntentService {
    private static final String TAG = NotificationsService.class.getSimpleName();

    public static final String ACTION_SNOOZE = "actionSnooze";

    public NotificationsService() {
        super(TAG);
    }

    public NotificationsService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String action = intent.getAction();
        if (action != null && action.equals(ACTION_SNOOZE)) {
            Log.d(TAG, "onHandleIntent: action snooze");
            //TODO
        }

        // make sure notification type was passed in the intent
        int notificationType = intent.getIntExtra(SchedulingService.NOTIFICATION_TYPE_KEY, -1);
        if (notificationType == -1) {
            Log.d(TAG, "onHandleIntent: notification type not passed to NotifactionService");
            return;
        }

        // create intents for clicking thru to app and for snoozing
        Intent clickIntent = new Intent(this, MainActivity.class);
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent clickPendingIntent = PendingIntent
                .getActivity(this, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent snoozeIntent = new Intent(this, NotificationsService.class);
        snoozeIntent.setAction(ACTION_SNOOZE);
        PendingIntent snoozePendingIntent = PendingIntent.getService(this, 0, snoozeIntent, 0);


        // build the notification
        Log.d(TAG, "onHandleIntent: creating notification for "
                + TimeOfDay.getTimeOfDay(notificationType).toString());
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notifications_active_grey_700_24dp)
                .setContentTitle(getString(R.string.notification_title))
                //TODO - set text
                .setContentText(TimeOfDay.getTimeOfDay(notificationType).toString())
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle()
                        //TODO - set text
                        .bigText("ealriug ergius aerhoxcug uihpsiudfhg aw3iuesydhfg flughdslg kse"))
                .setContentIntent(clickPendingIntent)
                .addAction(R.drawable.ic_snooze_grey_700_24dp,
                        getString(R.string.notification_action_snooze), snoozePendingIntent)
                .setPriority(Notification.PRIORITY_MAX);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(notificationType, builder.build());
    }
}
