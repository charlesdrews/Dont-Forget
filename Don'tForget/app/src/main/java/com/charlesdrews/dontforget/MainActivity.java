package com.charlesdrews.dontforget;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private AccountManager mAccountManager;
    private Account mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAccountManager = AccountManager.get(this);
        mAccount = getAccount();
        if (mAccount == null) {
            mAccount = createAccount();
        }

        //TODO - create & launch an async task that looks for weather data in database, and
        //if none present request manual sync, otherwise load data from db into UI
        requestManualSync();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestManualSync();
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
}
