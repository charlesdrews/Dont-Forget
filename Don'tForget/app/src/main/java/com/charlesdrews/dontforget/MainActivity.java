package com.charlesdrews.dontforget;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.charlesdrews.dontforget.birthdays.Birthdays;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int READ_CONTACTS_PERMISSION_REQUEST_CODE = 123;

    private AccountManager mAccountManager;
    private Account mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // set up account for syncing
        mAccountManager = AccountManager.get(this);
        mAccount = getAccount();
        if (mAccount == null) {
            mAccount = createAccount();
        }

        //TODO - create & launch an async task that looks for weather data in database, and
        //if none present request manual sync, otherwise load data from db into UI
        requestManualSync();


        // set up view pager & tab layout
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new TabPagerAdapter(getSupportFragmentManager()));
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestManualSync();

                if (haveReadContactsPermission()) {
                    readContacts();
                } else {
                    requestReadContactsPermission();
                }
            }
        });

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
                //TODO - set up settings activity
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public Account createAccount() {
        Account newAccount = new Account(
                getString(R.string.account),
                getString(R.string.account_type)
        );

        if (mAccountManager.addAccountExplicitly(newAccount, null, null)) {
            Log.d(TAG, "createAccount: success");
        } else {
            Log.d(TAG, "createAccount: failed");
        }

        return newAccount;
    }

    private Account getAccount() {
        // Return the first account of our account_type, or null if none
        Account[] accounts = mAccountManager.getAccountsByType(getString(R.string.account_type));
        if (accounts.length > 0) {
            return accounts[0];
        }
        return null;
    }

    private void requestManualSync() {
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(mAccount, getString(R.string.authority), settingsBundle);
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
        Birthdays.logBirthdays(getApplicationContext());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_CONTACTS_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(findViewById(R.id.main_activity_root_view), "Permission granted", Snackbar.LENGTH_SHORT).show();
            readContacts();
        } else {
            Snackbar.make(findViewById(R.id.main_activity_root_view), "Permission denied", Snackbar.LENGTH_SHORT).show();
        }
    }
}
