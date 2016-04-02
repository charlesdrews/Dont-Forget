package com.charlesdrews.dontforget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.ScrollView;
import android.widget.Toast;

import com.charlesdrews.dontforget.birthdays.BirthdaysFragment;
import com.charlesdrews.dontforget.settings.SettingsActivity;
import com.charlesdrews.dontforget.weather.WeatherFragment;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener, ViewPager.OnPageChangeListener {
    private static final String TAG = "MainActivity";
    public static final int READ_CONTACTS_PERMISSION_REQUEST_CODE = 123;
    public static final int ACCESS_COARSE_LOCATION_PERMISSION_REQUEST_CODE = 124;

    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private MyFragmentPagerAdapter mAdapter;
    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // load default preferences if first time running app
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // set up view pager & tab layout
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(this);

        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        if (mTabLayout != null) {
            mTabLayout.setupWithViewPager(mViewPager);
        }

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
                    ((WeatherFragment) fragment).startSync();
                    break;
                case MyFragmentPagerAdapter.TASKS:
                    Log.d(TAG, "handleRefresh: tasks");
                    //TODO ((TaskFragment) fragment).
                    break;
                case MyFragmentPagerAdapter.BIRTHDAYS:
                    Log.d(TAG, "handleRefresh: birthdays");
                    ((BirthdaysFragment) fragment).syncContacts();
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        String snackbarMessage = null;

        switch (requestCode) {
            case READ_CONTACTS_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    snackbarMessage = "Permission to read contacts granted";
                    mViewPager.setCurrentItem(MyFragmentPagerAdapter.BIRTHDAYS);
                } else {
                    snackbarMessage = "Permission to read contacts denied";
                }
                break;
            case ACCESS_COARSE_LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    snackbarMessage = "Permission to use device location granted";
                    mViewPager.setCurrentItem(MyFragmentPagerAdapter.WEATHER);
                } else {
                    snackbarMessage = "Permission to use device location denied";
                }
                break;
        }

        if (snackbarMessage != null) {
            Snackbar.make(mFab, snackbarMessage, Snackbar.LENGTH_SHORT)
                    .show();
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
                Log.d(TAG, "handleFabClick: tasks");
                //TODO
                break;
            case MyFragmentPagerAdapter.BIRTHDAYS:
                Log.d(TAG, "handleFabClick: birthdays");
                //TODO
                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        Log.d(TAG, "onPageSelected: " + position);

        /*
        //TODO - this doesn't work
        View fragRootView = mAdapter.getActiveFragment(position).getView();
        if (fragRootView != null && fragRootView instanceof ScrollView) {
            Log.d(TAG, "onPageSelected: trying to scroll");
            ((ScrollView) fragRootView).smoothScrollTo(0, 0);
        }
        */

        if (position == MyFragmentPagerAdapter.WEATHER) {
            // Fade out button if weather fragment selected
            mFab.animate()
                    .alpha(0f)
                    .setDuration(250)
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
}
