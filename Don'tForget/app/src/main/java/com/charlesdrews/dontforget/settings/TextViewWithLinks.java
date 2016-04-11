package com.charlesdrews.dontforget.settings;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Set the TextView's movement method once it is inflated.
 * Created by charlie on 4/10/16.
 */
public class TextViewWithLinks extends TextView {
    public TextViewWithLinks(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        this.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
