package com.charlesdrews.dontforget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.charlesdrews.dontforget.birthdays.BirthdaysFragment;
import com.charlesdrews.dontforget.birthdays.AddContactBirthday;
import com.charlesdrews.dontforget.notifications.SchedulingService;
import com.charlesdrews.dontforget.tasks.TaskFragment;
import com.charlesdrews.dontforget.weather.WeatherFragment;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener, ViewPager.OnPageChangeListener,
        AddContactBirthday.BirthdayUpdatedListener {

    private static final String TAG = "MainActivity";
    public static final int CONTACTS_PERMISSION_REQUEST_CODE = 123;
    public static final int ACCESS_COARSE_LOCATION_PERMISSION_REQUEST_CODE = 124;

    private SharedPreferences mPreferences;
    private CoordinatorLayout mRootView;
    private ViewPager mViewPager;
    private MyFragmentPagerAdapter mAdapter;
    private FloatingActionButton mFab;


    //==============================================================================================
    //====== Activity lifecycle methods ============================================================
    //==============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // load default preferences if first time running app & schedule corresponding notifications
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        scheduleNotifications();

        // check if necessary to prompt user to customize settings
        boolean promptUserToVisitSettings = mPreferences
                .getBoolean(getString(R.string.pref_key_suggest_updating_settings), true);
        if (promptUserToVisitSettings) {
            launchSuggestVisitSettingsDialog();
        }

        // set up view pager & tab layout
        mRootView = (CoordinatorLayout) findViewById(R.id.main_activity_root_view);
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
                    mPreferences.edit()
                            .putBoolean(getString(R.string.pref_key_weather_use_device_location), false)
                            .commit();
                    snackbarMessage = "Permission to use device location denied";
                }
                break;
        }

        if (snackbarMessage != null) {
            Snackbar.make(mRootView, snackbarMessage, Snackbar.LENGTH_LONG).show();
        }
    }


    //==============================================================================================
    //========== ViewPager listener callback methods ===============================================
    //==============================================================================================
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


    //==============================================================================================
    //========== Other listener callback methods ===================================================
    //==============================================================================================
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                handleFabClick(mViewPager.getCurrentItem());
                break;
        }
    }

    @Override
    public void onBirthdayUpdated(boolean success, String name, boolean deleted) {
        mViewPager.setCurrentItem(MyFragmentPagerAdapter.BIRTHDAYS);
        String snackbarMessage = null;

        if (deleted && success) {
            snackbarMessage = "Birthday deleted for " + name;

        } else if (!deleted && success) {
            BirthdaysFragment fragment = (BirthdaysFragment) mAdapter
                    .getActiveFragment(MyFragmentPagerAdapter.BIRTHDAYS);
            fragment.syncContacts();
            snackbarMessage = "Birthday updated for " + name;

        } else if (!deleted) {
            snackbarMessage = "Unable to update birthday for " + name;
        }
        // not concerned about unsuccessful delete; that's probably from a contact w/o a birthday

        if (snackbarMessage != null && !snackbarMessage.isEmpty()) {
            Snackbar.make(mRootView, snackbarMessage, Snackbar.LENGTH_LONG).show();
        }
    }


    //==============================================================================================
    //========== Helper methods ====================================================================
    //==============================================================================================
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

    private void handleFabClick(int currentFragmentPosition) {
        switch (currentFragmentPosition) {

            case MyFragmentPagerAdapter.TASKS:
                TaskFragment fragment = (TaskFragment) mAdapter
                        .getActiveFragment(MyFragmentPagerAdapter.TASKS);
                fragment.addOrUpdateTask(null);
                break;

            case MyFragmentPagerAdapter.BIRTHDAYS:
                AddContactBirthday dialog = new AddContactBirthday(this);
                dialog.launchContactSearch();
                break;
        }
    }


    public void scheduleNotifications() {
        startService(new Intent(this, SchedulingService.class));
    }

    private void launchSuggestVisitSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.suggestion_dialog_title))
                .setMessage(getString(R.string.suggestion_dialog_message))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                        mPreferences.edit()
                                .putBoolean(
                                        getString(R.string.pref_key_suggest_updating_settings),
                                        false)
                                .apply();
                    }
                })
                .setNegativeButton("Later", null)
                .setNeutralButton("Don't Show Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPreferences.edit()
                                .putBoolean(
                                        getString(R.string.pref_key_suggest_updating_settings),
                                        false)
                                .apply();
                    }
                });
        builder.show();
    }
}
