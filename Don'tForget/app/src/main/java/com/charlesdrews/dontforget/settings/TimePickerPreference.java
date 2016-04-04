package com.charlesdrews.dontforget.settings;

import android.content.Context;
import android.os.Build;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

/**
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
            String timeAmPm = hour > 12 ? (hour - 12) + ":" + minutes + " pm" :
                    hour + ":" + minutes + " am";

            if (callChangeListener(time)) {
                persistString(time);
                setSummary(timeAmPm);
            }
        }
    }
}
