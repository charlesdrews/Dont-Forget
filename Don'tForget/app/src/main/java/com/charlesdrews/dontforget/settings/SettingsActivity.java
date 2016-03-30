package com.charlesdrews.dontforget.settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.charlesdrews.dontforget.settings.SettingsFragment;

/**
 * Created by charlie on 3/29/16.
 */
public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
