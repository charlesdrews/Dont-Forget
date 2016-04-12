package com.charlesdrews.dontforget.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Log;

import com.charlesdrews.dontforget.R;

/**
 * Create account for sync adapter
 * Created by charlie on 3/31/16.
 */
public class AccountHelper {
    private static final String TAG = AccountHelper.class.getSimpleName();
    private static final int MAX_ATTEMPTS = 3;

    public static Account getAccount(Context context) {
        AccountManager accountManager = AccountManager.get(context);

        // Return the first account of type account_type, or null if none
        Account[] accounts = accountManager.getAccountsByType(context.getString(R.string.account_type));
        if (accounts.length > 0) {
            return accounts[0];
        }
        return createAccount(context, accountManager);
    }

    private static Account createAccount(Context context, AccountManager accountManager) {
        Account newAccount = new Account(
                context.getString(R.string.account),
                context.getString(R.string.account_type)
        );

        int attempts = 0;
        while (true) {
            try {
                accountManager.addAccountExplicitly(newAccount, null, null);
                Log.d(TAG, "createAccount: success");
                break;
            } catch (Exception e) {
                if (++attempts == MAX_ATTEMPTS) {
                    throw e;
                }
                Log.d(TAG, "createAccount: retrying after error: " + e.getMessage());
            }
        }

        return newAccount;
    }
}
