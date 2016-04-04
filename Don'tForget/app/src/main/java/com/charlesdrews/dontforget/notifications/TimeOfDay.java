package com.charlesdrews.dontforget.notifications;

/**
 * The four times of day that notifications will be delivered
 * Created by charlie on 4/3/16.
 */
public enum TimeOfDay {
    BEFORE_WORK, LUNCHTIME, ON_THE_WAY_HOME, EVENING;

    private String string;
    static {
        BEFORE_WORK.string = "Before Work";
        LUNCHTIME.string = "Lunchtime";
        ON_THE_WAY_HOME.string = "On the Way Home";
        EVENING.string = "Evening";
    }

    private int integer;
    static {
        BEFORE_WORK.integer = 0;
        LUNCHTIME.integer = 1;
        ON_THE_WAY_HOME.integer = 2;
        EVENING.integer = 3;
    }

    public String toString() {
        return string;
    }

    public int getInt() {
        return integer;
    }

    public static TimeOfDay getTimeOfDay(int intVal) {
        switch (intVal) {
            case 0:
                return BEFORE_WORK;
            case 1:
                return LUNCHTIME;
            case 2:
                return ON_THE_WAY_HOME;
            case 3:
                return EVENING;
            default:
                throw new IllegalArgumentException("intVal must be in the range [0,3] inclusive");
        }
    }

    public static int getMin() { return 0; }
    public static int getMax() { return 3; }
}
