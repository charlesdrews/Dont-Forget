package com.charlesdrews.dontforget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.charlesdrews.dontforget.birthdays.BirthdaysFragment;
import com.charlesdrews.dontforget.birthdays.AddContactBirthday;
import com.charlesdrews.dontforget.notifications.SchedulingService;
import com.charlesdrews.dontforget.tasks.TaskFragment;
import com.charlesdrews.dontforget.weather.WeatherFragment;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener, ViewPager.OnPageChangeListener,
        ProgressBarListener, AddContactBirthday.BirthdayUpdatedListener {
    private static final String TAG = "MainActivity";
    public static final int CONTACTS_PERMISSION_REQUEST_CODE = 123;
    public static final int ACCESS_COARSE_LOCATION_PERMISSION_REQUEST_CODE = 124;

    private ViewPager mViewPager;
    private MyFragmentPagerAdapter mAdapter;
    private FloatingActionButton mFab;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // load default preferences if first time running app & schedule corresponding notifications
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        scheduleNotifications();

        // set up view pager & tab layout
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(this);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(mViewPager);
        }

        // remaining views
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        if (mFab != null) {
            mFab.setOnClickListener(this);
        }

        //TODO - have separate progress bars in each fragment
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
    }

    @Override
    public void onBackPressed() {
        int currTab = mViewPager.getCurrentItem();
        if (currTab > 0) {
            mViewPager.setCurrentItem(currTab - 1);
        } else {
            super.onBackPressed();
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
            case R.id.action_refresh:
                handleRefresh(mViewPager.getCurrentItem());
                break;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void handleRefresh(int currentFragmentPosition) {
        Fragment fragment = mAdapter.getActiveFragment(currentFragmentPosition);
        if (fragment != null) {
            switch (currentFragmentPosition) {

                case MyFragmentPagerAdapter.WEATHER:
                    Log.d(TAG, "handleRefresh: weather");
                    ((WeatherFragment) fragment).startSync(true);
                    break;

                case MyFragmentPagerAdapter.TASKS:
                    Log.d(TAG, "handleRefresh: tasks");
                    ((TaskFragment) fragment).refreshTasks();
                    break;

                case MyFragmentPagerAdapter.BIRTHDAYS:
                    Log.d(TAG, "handleRefresh: birthdays");
                    ((BirthdaysFragment) fragment).syncContacts();
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        String snackbarMessage = null;

        switch (requestCode) {
            case CONTACTS_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // read contacts -> granted
                    snackbarMessage = "Permission to access contacts granted";
                    mViewPager.setCurrentItem(MyFragmentPagerAdapter.BIRTHDAYS);

                    BirthdaysFragment fragment = (BirthdaysFragment) mAdapter
                            .getActiveFragment(MyFragmentPagerAdapter.BIRTHDAYS);
                    fragment.syncContacts();
                } else {

                    // read contacts -> denied
                    snackbarMessage = "Permission to access contacts denied";
                }
                break;
            case ACCESS_COARSE_LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // access device location -> granted
                    snackbarMessage = "Permission to use device location granted";
                    mViewPager.setCurrentItem(MyFragmentPagerAdapter.WEATHER);
                } else {

                    // access device location -> denied
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                    prefs.edit().putBoolean(getString(R.string.pref_key_weather_use_device_location), false).commit();
                    snackbarMessage = "Permission to use device location denied";
                }
                break;
        }

        if (snackbarMessage != null) {
            Snackbar.make(mFab, snackbarMessage, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                handleFabClick(mViewPager.getCurrentItem());
                break;
        }
    }

    private void handleFabClick(int currentFragmentPosition) {
        switch (currentFragmentPosition) {

            case MyFragmentPagerAdapter.TASKS:
                TaskFragment fragment = (TaskFragment) mAdapter
                        .getActiveFragment(MyFragmentPagerAdapter.TASKS);
                fragment.addTask();
                break;

            case MyFragmentPagerAdapter.BIRTHDAYS:
                AddContactBirthday dialog = new AddContactBirthday(this);
                dialog.launchContactSearch();
                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (position == MyFragmentPagerAdapter.WEATHER) {
            // Fade out button if weather fragment selected
            mFab.animate()
                    .alpha(0f)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mFab.setVisibility(View.GONE);
                        }
                    });
        } else if (mFab.getVisibility() == View.GONE) {
            // If tasks or birthday selected and button currently gone, fade it in
            mFab.setVisibility(View.VISIBLE);
            mFab.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .setListener(null);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    //TODO - have separate progress bars in each fragment
    @Override
    public void startProgressBar() {
        mProgressBar.setAlpha(0f);
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.animate().alpha(1f).setDuration(1000);
    }

    //TODO - have separate progress bars in each fragment
    @Override
    public void stopProgressBar() {
        mProgressBar.animate().alpha(0f).setDuration(1000)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onBirthdayUpdated(boolean success, String name) {
        String snackbarMessage;
        mViewPager.setCurrentItem(MyFragmentPagerAdapter.BIRTHDAYS);
        if (success) {
            BirthdaysFragment fragment = (BirthdaysFragment) mAdapter
                    .getActiveFragment(MyFragmentPagerAdapter.BIRTHDAYS);
            fragment.syncContacts();
            snackbarMessage = "Birthday updated for " + name;
        } else {
            snackbarMessage = "Unable to update birthday for " + name;
        }
        Snackbar.make(mViewPager, snackbarMessage, Snackbar.LENGTH_LONG).show();
    }

    public void scheduleNotifications() {
        startService(new Intent(this, SchedulingService.class));
    }
}
