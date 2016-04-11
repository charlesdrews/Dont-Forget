package com.charlesdrews.dontforget.tasks;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.NumberPicker;

import com.charlesdrews.dontforget.tasks.model.TaskRealm;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Create a custom picker for selecting the date a task is due
 * Created by charlie on 4/11/16.
 */
public class DayPicker extends NumberPicker {
    private static final int NUM_DAYS_INTO_FUTURE = 60;

    public DayPicker(Context context) {
        super(context);
    }

    public DayPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setup(TaskRealm taskToBeUpdated) {
        setMinValue(0);
        setMaxValue(NUM_DAYS_INTO_FUTURE);
        setWrapSelectorWheel(false);

        // get starting date for picker - the earlier of today or original task date
        Calendar dateToDisplay = Calendar.getInstance();
        dateToDisplay.setTimeInMillis(System.currentTimeMillis());

        Calendar originalDate = Calendar.getInstance();
        if (taskToBeUpdated != null && taskToBeUpdated.getDate() != null) {
            originalDate.setTimeInMillis(taskToBeUpdated.getDate().getTime());

            if (taskToBeUpdated.getDate().before(dateToDisplay.getTime())) {
                dateToDisplay.setTimeInMillis(taskToBeUpdated.getDate().getTime());
            }
        } else {
            originalDate.setTimeInMillis(System.currentTimeMillis());
        }

        // set up String array of days extending from starting date
        String[] days = new String[NUM_DAYS_INTO_FUTURE];
        SimpleDateFormat sdf = new SimpleDateFormat("EEE M/d", Locale.US);

        int indexToPreSet = 0;
        for (int i = 0; i < NUM_DAYS_INTO_FUTURE; i++) {
            days[i] = sdf.format(dateToDisplay.getTime());
            if (sameDay(originalDate, dateToDisplay)) {
                indexToPreSet = i;
            }
            dateToDisplay.add(Calendar.DATE, 1);
        }

        // set values & selected value
        setDisplayedValues(days);
        setValue(indexToPreSet);
    }

    private boolean sameDay(Calendar calA, Calendar calB) {
        return (calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR) &&
                calA.get(Calendar.MONTH) == calB.get(Calendar.MONTH) &&
                calA.get(Calendar.DAY_OF_MONTH) == calB.get(Calendar.DAY_OF_MONTH));
    }
}
