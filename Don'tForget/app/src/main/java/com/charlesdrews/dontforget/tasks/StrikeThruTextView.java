package com.charlesdrews.dontforget.tasks;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/**
 * Add methods to quickly add & remove strike-through from text view
 * Created by charlie on 4/3/16.
 */
public class StrikeThruTextView extends TextView {
    private static final String TAG = StrikeThruTextView.class.getSimpleName();

    public StrikeThruTextView(Context context) {
        super(context);
    }

    public StrikeThruTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StrikeThruTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void addStrikeThru() {
        this.setPaintFlags(this.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    public void removeStrikeThru() {
        this.setPaintFlags(this.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
    }
}
