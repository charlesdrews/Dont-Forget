package com.charlesdrews.dontforget.birthdays;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.charlesdrews.dontforget.DividerItemDecoration;
import com.charlesdrews.dontforget.MainActivity;
import com.charlesdrews.dontforget.R;
import com.charlesdrews.dontforget.birthdays.model.BirthdayRealm;

import java.util.List;

import io.realm.Realm;


public class BirthdaysFragment extends Fragment {
    private static final String TAG = BirthdaysFragment.class.getSimpleName();

    private View mRootView;
    private Context mContext;
    private Realm mRealm;
    private RecyclerView mRecycler;
    private BirthdayRecyclerAdapter mAdapter;
    private List<BirthdayRealm> mBirthdays;
    private int mNumTimeUpdateOfViewsTriggeredSync = 0;

    public BirthdaysFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mRealm == null || mRealm.isClosed()) {
            mRealm = Realm.getDefaultInstance();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = container.getContext();
        mRootView = inflater.inflate(R.layout.fragment_birthdays, container, false);
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        updateViewsWithBirthdaysFromDb();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mRealm != null && !mRealm.isClosed()) {
            mRealm.close();
        }
    }

    private boolean haveReadContactsPermission() {
        return (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED);
    }

    private void requestReadContactsPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_CONTACTS)) {
            Snackbar.make(
                    getActivity().findViewById(R.id.fab),
                    "Need permission to view contacts in order to provide birthday reminders",
                    Snackbar.LENGTH_LONG
            ).show();
        }
        ActivityCompat.requestPermissions(
                getActivity(),
                new String[]{Manifest.permission.READ_CONTACTS},
                MainActivity.READ_CONTACTS_PERMISSION_REQUEST_CODE
        );
    }

    public void syncContacts() {
        new SyncContactsAsyncTask().execute();
    }

    public class SyncContactsAsyncTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!haveReadContactsPermission()) {
                requestReadContactsPermission();
                cancel(true);
            }
            //TODO - start a progress bar
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return BirthdaysHelper.syncContactsBirthdaysToDb(mContext);
        }

        @Override
        protected void onPostExecute(Boolean successful) {
            super.onPostExecute(successful);

            if (successful) {
                updateViewsWithBirthdaysFromDb();
            }
            //TODO - stop a progress bar
        }
    }

    private void updateViewsWithBirthdaysFromDb() {
        mBirthdays = mRealm.where(BirthdayRealm.class)
                .findAllSorted("nextBirthday");

        if (mBirthdays != null && mBirthdays.size() > 0) {
            Log.d(TAG, "updateViewsWithBirthdaysFromDb: birthdays pulled from db");
            if (mRecycler == null) {
                mRecycler = (RecyclerView) mRootView.findViewById(R.id.birthday_recycler);

                mAdapter = new BirthdayRecyclerAdapter(mBirthdays);
                mRecycler.setAdapter(mAdapter);

                mRecycler.setLayoutManager(new LinearLayoutManager(mContext));

                mRecycler.addItemDecoration(new DividerItemDecoration(mContext));
            } else {
                mAdapter.notifyDataSetChanged();
            }
        } else {
            Log.d(TAG, "updateViewsWithBirthdaysFromDb: no birthdays found in db");
            if (++mNumTimeUpdateOfViewsTriggeredSync <= 1) {
                // careful not to start an infinite loop w/ SyncContactsAsyncTask and this
                syncContacts();
            }
        }
    }
}
