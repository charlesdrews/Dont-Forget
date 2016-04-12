package com.charlesdrews.dontforget.notifications;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;

import com.charlesdrews.dontforget.MainActivity;
import com.charlesdrews.dontforget.R;
import com.charlesdrews.dontforget.birthdays.model.BirthdayRealm;
import com.charlesdrews.dontforget.tasks.model.TaskRealm;
import com.charlesdrews.dontforget.weather.model.CurrentConditionsRealm;
import com.charlesdrews.dontforget.weather.model.DailyForecastRealm;
import com.charlesdrews.dontforget.weather.model.HourlyForecastRealm;

import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Deliver notifications to the user based on weather, tasks, and birthdays
 * Created by charlie on 4/4/16.
 */
public class NotificationsService extends IntentService {
    private static final String TAG = NotificationsService.class.getSimpleName();

    private SharedPreferences mPrefs;
    private Realm mRealm;
    private Date mNow;

    public NotificationsService() {
        super(TAG);
    }

    public NotificationsService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mNow = new Date(System.currentTimeMillis());

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

        /* TODO - this isn't working
        Intent snoozeIntent = new Intent(this, SnoozeService.class);
        snoozeIntent.putExtra(SchedulingService.NOTIFICATION_TYPE_KEY, notificationType);
        snoozeIntent.setAction(ACTION_SNOOZE);
        PendingIntent snoozePendingIntent = PendingIntent
                .getService(this, 0, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        */

        // get notification message text - helper methods use Realm
        mRealm = Realm.getDefaultInstance();
        String weatherText = getWeatherText(notificationType);
        String taskText = getTaskText(notificationType);
        String birthdayText = getBirthdayText();
        mRealm.close();

        // assemble the notification message
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append(weatherText);
        if (taskText.length() > 0) {
            messageBuilder.append("\n\n")
                    .append(Html.fromHtml("&#9745;").toString()) // checkbox
                    .append(" ")
                    .append(taskText);
        }
        if (birthdayText.length() >0) {
            messageBuilder.append("\n\n")
                    .append(Html.fromHtml("&#127874;").toString()) // birthday cake
                    .append(" ")
                    .append(birthdayText);
        }
        String message = messageBuilder.toString();

        // build the notification
        Log.d(TAG, "onHandleIntent: creating notification for "
                + TimeOfDay.getTimeOfDay(notificationType).toString());
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ribbon_white)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(clickPendingIntent)
//                .addAction(R.drawable.ic_snooze_grey_700_24dp,
//                        getString(R.string.notification_action_snooze), snoozePendingIntent)
                .setPriority(Notification.PRIORITY_MAX);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(notificationType, builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //mRealm.close();
        // I hate to leak memory, but this close() causes a lot of Realm "incorrect thread" errors
    }

    /**
     * Weather included in all notifications; also all will include precipitation info
     *   - Before work notification: show current weather plus daily high
     *   - Lunchtime notification: show current weather plus daily high
     *   - On the way home notification: show current weather
     *   - Evening notification: show tomorrow's forecast
     */
    private String getWeatherText(int notificationType) {
        // grab weather data from db
        CurrentConditionsRealm current = mRealm.where(CurrentConditionsRealm.class).findFirst();
        RealmResults<HourlyForecastRealm> hourlyForecasts = mRealm.where(HourlyForecastRealm.class)
                .findAllSorted("dateTime");
        RealmResults<DailyForecastRealm> dailyForecasts = mRealm.where(DailyForecastRealm.class)
                .findAllSorted("date");

        // check if data present
        if (current == null || hourlyForecasts == null || hourlyForecasts.size() == 0 ||
                dailyForecasts == null || dailyForecasts.size() == 0) {
            Log.d(TAG, "getWeatherText: no weather data in database");
            return getString(R.string.weather_unavailable);
        }

        // get today & tomorrow
        DailyForecastRealm today = null, tomorrow = null;
        for (int i = 0; i < dailyForecasts.size() - 1; i++) {
            if (sameDate(dailyForecasts.get(i).getDate(), mNow)) {
                today = dailyForecasts.get(i);
                tomorrow = dailyForecasts.get(i + 1);
                break;
            }
        }
        if (today == null || (notificationType == TimeOfDay.EVENING.getInt() && tomorrow == null)) {
            return getString(R.string.weather_unavailable);
        }

        // get weather unit preference
        String weatherUnits = mPrefs.getString(getString(R.string.pref_key_weather_units),
                getString(R.string.weather_default_unit));
        boolean useMetric = "Metric".equals(weatherUnits);

        // get info for message
        long currTemp = Math.round(useMetric ? current.getTempCel() : current.getTempFahr());
        int todayHigh = useMetric ? today.getTempHighCel() : today.getTempHighFahr();
        int tomorrowHigh = useMetric ? tomorrow.getTempHighCel() : tomorrow.getTempHighFahr();
        int tomorrowLow = useMetric ? tomorrow.getTempLowCel() : tomorrow.getTempLowFahr();

        // for today's precipitation, only look at rest of day (in case rain is over for the day)
        int probPrecipToday = -1;
        for (int i = 0; i < hourlyForecasts.size(); i++) {
            HourlyForecastRealm hour = hourlyForecasts.get(i);
            Date hourDateTime = hour.getDateTime();

            if (hourDateTime.after(mNow)) {
                if (sameDate(hourDateTime, mNow) && hour.getProbOfPrecip() > probPrecipToday) {
                    probPrecipToday = hour.getProbOfPrecip();
                } else if (!sameDate(hourDateTime, mNow)) {
                    // stop once you get past today
                    break;
                }
            }
        }
        if (probPrecipToday == -1) {
            probPrecipToday = today.getProbOfPrecip();
        }

        int probPrecipTomorrow = tomorrow.getProbOfPrecip();

        String precipToday;
        if (today.getSnowInches() > 0.0 || today.getConditionDesc().toLowerCase().contains("snow")) {
            precipToday = Html.fromHtml("&#10052;").toString() + probPrecipToday + "% | "; // snowflake
        } else {
            precipToday = Html.fromHtml("&#128167;").toString() + probPrecipToday + "% | "; // droplet
        }

        String precipTomorrow;
        if (tomorrow.getSnowInches() > 0.0 || tomorrow.getConditionDesc().toLowerCase().contains("snow")) {
            precipTomorrow = Html.fromHtml("&#10052;").toString() + probPrecipTomorrow + "% | "; // snowflake
        } else {
            precipTomorrow = Html.fromHtml("&#128167;").toString() + probPrecipTomorrow + "% | "; // droplet
        }

        if (notificationType == TimeOfDay.BEFORE_WORK.getInt() ||
                notificationType == TimeOfDay.LUNCHTIME.getInt()) {
            return precipToday + "Currently " + currTemp + "° | High " + todayHigh + "°";
        } else if (notificationType == TimeOfDay.ON_THE_WAY_HOME.getInt()) {
            return precipToday + "Currently " + currTemp + "°";
        } else {
            return "Tomorrow: " + precipTomorrow + "High " + tomorrowHigh + "° | Low " + tomorrowLow + "°";
        }
    }

    /**
     * Tasks are included on their specified date and time
     */
    private String getTaskText(int notificationType) {
        // grab sorted task list
        RealmResults<TaskRealm> tasks = mRealm.where(TaskRealm.class).equalTo("completed", false)
                .findAllSorted("date", Sort.ASCENDING, "timeOfDay", Sort.ASCENDING);

        // scan thru tasks, include any w/ past or current date & time
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < tasks.size(); i++) {
            TaskRealm task = tasks.get(i);
            Date taskDate = task.getDate();
            int taskTimeOfDay = task.getTimeOfDay();
            boolean isToday = sameDate(taskDate, mNow);

            if (taskDate.before(mNow) || (isToday && taskTimeOfDay <= notificationType)) {
                if (builder.length() > 0) {
                    builder.append(" | ");
                }
                builder.append(task.getTaskText());
            } else if (taskDate.after(mNow) || (isToday && taskTimeOfDay > notificationType)) {
                // stop once you get past today
                break;
            }
        }

        return builder.toString();
    }

    /**
     * Birthdays included in all notifications:
     *   - For all notification times, list any birthdays that day
     */
    private String getBirthdayText() {
        // grab sorted birthday list
        RealmResults<BirthdayRealm> birthdays = mRealm.where(BirthdayRealm.class)
                .equalTo("necessaryToNotify", true).findAllSorted("nextBirthday");

        // scan thru birthdays, include any matching today
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < birthdays.size(); i++) {
            BirthdayRealm birthday = birthdays.get(i);

            if (sameDate(birthday.getNextBirthday(), mNow)) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                builder.append(birthday.getName());
                if (birthday.getYearOfBirth() > 0) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    int currYear = calendar.get(Calendar.YEAR);
                    int age = currYear - birthday.getYearOfBirth();
                    builder.append(" (").append(String.valueOf(age)).append(")");
                }
            } else if (birthday.getNextBirthday().after(mNow)) {
                // stop once you get past today
                break;
            }
        }

        return builder.toString();
    }

    /**
     * In case minutes/seconds/milliseconds differ, just check year/month/day
     */
    private boolean sameDate(Date a, Date b) {
        Calendar calA = Calendar.getInstance();
        Calendar calB = Calendar.getInstance();

        calA.setTimeInMillis(a.getTime());
        calB.setTimeInMillis(b.getTime());

        return (calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR) &&
                calA.get(Calendar.MONTH) == calB.get(Calendar.MONTH) &&
                calA.get(Calendar.DAY_OF_MONTH) == calB.get(Calendar.DAY_OF_MONTH));
    }
}
