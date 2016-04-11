package com.charlesdrews.dontforget.settings;

import android.content.Context;
import android.os.Build;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

/**
 * Custom time-picking preference widget with TimePicker dialog & update of summary on dialog close
 * Created by charlie on 4/4/16.
 */
public class TimePickerPreference extends DialogPreference {
    private TimePicker mTimePicker;

    public TimePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setPositiveButtonText("Set");
        setNegativeButtonText("Cancel");
    }

    @Override
    protected View onCreateDialogView() {
        mTimePicker = new TimePicker(getContext());

        String summary = getSummary().toString().split(" ")[0];     // e.g. "8:00 am" -> "8:00"
        String ampm = getSummary().toString().split(" ")[1];        // e.g. "8:00 am" -> "am"

        if (!summary.isEmpty() && summary.contains(":")) {
            int hour = Integer.parseInt(summary.split(":")[0]);     // e.g. "8:00" -> "8"
            int minute = Integer.parseInt(summary.split(":")[1]);   // e.g. "8:00" -> "00"

            if (ampm.toLowerCase().contains("pm") && hour < 12) {
                hour += 12;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mTimePicker.setHour(hour);
                mTimePicker.setMinute(minute);
            } else {
                mTimePicker.setCurrentHour(hour);
                mTimePicker.setCurrentMinute(minute);
            }
        }
        mTimePicker.setIs24HourView(false);
        return mTimePicker;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            int hour, minutes;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                hour = mTimePicker.getHour();
                minutes = mTimePicker.getMinute();
            } else {
                hour = mTimePicker.getCurrentHour();
                minutes = mTimePicker.getCurrentMinute();
            }

            String time = hour + ":" + minutes;

            String hourSummary = hour > 12 ? String.valueOf(hour - 12) : String.valueOf(hour);
            String minutesSummary = minutes < 10 ? "0" + minutes : String.valueOf(minutes);
            String amPm = hour < 12 ? " am" : " pm";
            hourSummary = (hour == 0) ? "12" : hourSummary;
            String timeSummary = hourSummary + ":" + minutesSummary + amPm;

            if (callChangeListener(time)) {
                persistString(time);
                setSummary(timeSummary);
            }
        }
    }
}
