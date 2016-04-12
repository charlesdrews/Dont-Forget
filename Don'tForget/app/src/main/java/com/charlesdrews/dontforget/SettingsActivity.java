package com.charlesdrews.dontforget;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.charlesdrews.dontforget.settings.SettingsFragment;

/**
 * Show the settings fragment, and allow links to be opened in a browser
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

    public void openBrowser(View view) {
        String url = (String) view.getTag();

        Intent intent = new Intent()
                .setAction(Intent.ACTION_VIEW)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .setData(Uri.parse(url));

        startActivity(intent);
    }
}
