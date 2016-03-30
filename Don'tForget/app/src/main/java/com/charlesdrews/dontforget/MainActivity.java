package com.charlesdrews.dontforget;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.charlesdrews.dontforget.birthdays.BirthdaysHelper;
import com.charlesdrews.dontforget.settings.SettingsActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int READ_CONTACTS_PERMISSION_REQUEST_CODE = 123;
    private static final int ACCESS_COARSE_LOCATION_PERMISSION_REQUEST_CODE = 124;
    public static final String REQUEST_LOCATION_PERMISSION_KEY = "requestLocationPermissionKey";
    public static final String WEATHER_LAST_SYNC_TIME_KEY = "weatherLastSyncTimeKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // check if requestPermissions needs to be called
        if (getIntent().hasExtra(REQUEST_LOCATION_PERMISSION_KEY) &&
                getIntent().getBooleanExtra(REQUEST_LOCATION_PERMISSION_KEY, false)) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    ACCESS_COARSE_LOCATION_PERMISSION_REQUEST_CODE
            );
        }

        //TODO - create & launch an async task that looks for weather data in database, and
        //if none present request manual sync, otherwise load data from db into UI
        //requestManualSync();


        // set up view pager & tab layout
        ViewPager viewPager;
        if ( (viewPager = (ViewPager) findViewById(R.id.view_pager)) != null ) {
            viewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager()));
        }
        TabLayout tabLayout;
        if ( (tabLayout = (TabLayout) findViewById(R.id.tab_layout)) != null ) {
            tabLayout.setupWithViewPager(viewPager);
        }

        FloatingActionButton fab;
        if ( (fab = (FloatingActionButton) findViewById(R.id.fab)) != null ) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (haveReadContactsPermission()) {
                        readContacts();
                    } else {
                        requestReadContactsPermission();
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private boolean haveReadContactsPermission() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED);
    }

    private void requestReadContactsPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
            Snackbar.make(
                    findViewById(R.id.main_activity_root_view),
                    "Need permission to view contacts in order to provide birthday reminders",
                    Snackbar.LENGTH_LONG
            ).show();
        }
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.READ_CONTACTS},
                READ_CONTACTS_PERMISSION_REQUEST_CODE
        );
    }

    private void readContacts() {
        //TODO - replace with function that does something other than log the birthdays
        BirthdaysHelper.logBirthdays(getApplicationContext());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        String snackbarMessage = null;

        switch (requestCode) {
            case READ_CONTACTS_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    snackbarMessage = "Permission to read contacts granted";
                    readContacts();
                } else {
                    snackbarMessage = "Permission to read contacts denied";
                }
                break;
            case ACCESS_COARSE_LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    snackbarMessage = "Permission to use device location granted";
                    //TODO - reload WeatherFragment
                } else {
                    snackbarMessage = "Permission to use device location denied";
                }
                break;
        }

        if (snackbarMessage != null) {
            Snackbar.make(findViewById(android.R.id.content), snackbarMessage, Snackbar.LENGTH_SHORT)
                    .show();
        }
    }
}
