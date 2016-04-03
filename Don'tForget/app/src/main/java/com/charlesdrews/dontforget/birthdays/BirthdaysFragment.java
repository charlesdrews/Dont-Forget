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
import io.realm.RealmChangeListener;
import io.realm.RealmResults;


public class BirthdaysFragment extends Fragment
        implements BirthdayRecyclerAdapter.ProvidesViewForSnackbar {

    private static final String TAG = BirthdaysFragment.class.getSimpleName();

    private View mRootView;
    private Realm mRealm;
    private RealmResults<BirthdayRealm> mBirthdays;
    private RecyclerView mRecycler;
    private BirthdayRecyclerAdapter mAdapter;

    public BirthdaysFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!haveReadContactsPermission()) {
            requestReadContactsPermission();
        }

        if (mRealm == null || mRealm.isClosed()) {
            mRealm = Realm.getDefaultInstance();
        }

        mBirthdays = mRealm.where(BirthdayRealm.class).findAllSorted("nextBirthday");

        mAdapter = new BirthdayRecyclerAdapter(getContext(), mBirthdays, this);

        mBirthdays.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                Log.d(TAG, "onChange: mBirthdays changed");
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_birthdays, container, false);

        mRecycler = (RecyclerView) mRootView.findViewById(R.id.birthday_recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecycler.setAdapter(mAdapter);
        mRecycler.addItemDecoration(new DividerItemDecoration(getContext()));

        return mRootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mRealm != null && !mRealm.isClosed()) {
            mRealm.close();
        }
    }

    private boolean haveReadContactsPermission() {
        return (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) ==
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

    @Override
    public View getViewFoSnackbar() {
        return mRootView;
    }

    public class SyncContactsAsyncTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ((MainActivity) getActivity()).startProgressBar();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return BirthdaysHelper.syncContactsBirthdaysToDb(getContext());
        }

        @Override
        protected void onPostExecute(Boolean successful) {
            super.onPostExecute(successful);
            ((MainActivity) getActivity()).stopProgressBar();
        }
    }
}
