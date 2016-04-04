package com.charlesdrews.dontforget.notifications.model;

import com.charlesdrews.dontforget.notifications.TimeOfDay;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Save user's preferred notification times
 * Created by charlie on 4/4/16.
 */
public class NotificationTimeRealm extends RealmObject {
    @PrimaryKey private int notificationId;
    private int notificationHour;
    private int notificationMinutes;

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        if (notificationId < TimeOfDay.getMin() || notificationId > TimeOfDay.getMax()) {
            throw new IllegalArgumentException(
                    String.format("argument value must be between %d and %d",
                            TimeOfDay.getMin(), TimeOfDay.getMax()));
        }
        this.notificationId = notificationId;
    }

    public int getNotificationHour() {
        return notificationHour;
    }

    public void setNotificationHour(int notificationHour) {
        if (notificationHour < 0 || notificationHour > 23) {
            throw new IllegalArgumentException(
                    String.format("argument value must be between %d and %d", 0, 23));
        }
        this.notificationHour = notificationHour;
    }

    public int getNotificationMinutes() {
        return notificationMinutes;
    }

    public void setNotificationMinutes(int notificationMinutes) {
        if (notificationMinutes < 0 || notificationMinutes > 59) {
            throw new IllegalArgumentException(
                    String.format("argument value must be between %d and %d", 0, 59));
        }
        this.notificationMinutes = notificationMinutes;
    }
}
