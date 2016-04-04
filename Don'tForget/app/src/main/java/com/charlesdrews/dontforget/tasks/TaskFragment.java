package com.charlesdrews.dontforget.tasks;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.charlesdrews.dontforget.DividerItemDecoration;
import com.charlesdrews.dontforget.R;
import com.charlesdrews.dontforget.notifications.TimeOfDay;
import com.charlesdrews.dontforget.tasks.model.TaskRealm;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;


/**
 * A simple {@link Fragment} subclass.
 */
public class TaskFragment extends Fragment {
    private static final String TAG = TaskFragment.class.getSimpleName();

    private Realm mRealm;
    private RealmResults<TaskRealm> mTasks;
    private TaskRecyclerAdapter mAdapter;

    public TaskFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mRealm == null || mRealm.isClosed()) {
            mRealm = Realm.getDefaultInstance();
        }

        mTasks = mRealm.where(TaskRealm.class)
                .equalTo("visible", true)
                .findAllSortedAsync(
                        "completed", Sort.ASCENDING,
                        "date", Sort.ASCENDING,
                        "timeOfDay", Sort.ASCENDING
                );

        mAdapter = new TaskRecyclerAdapter(getContext(), mTasks);

        mTasks.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                Log.d(TAG, "onChange: mTasks changed");
                if (mTasks.isEmpty()) {
                    mRealm.beginTransaction();
                    TaskRealm firstTask = mRealm.createObject(TaskRealm.class);
                    firstTask.setDate(new Date());
                    firstTask.setTimeOfDay(TimeOfDay.LUNCHTIME.getInt());
                    firstTask.setTaskText("Your first task");
                    firstTask.setCompleted(false);
                    firstTask.setVisible(true);
                    mRealm.commitTransaction();
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_task, container, false);

        RecyclerView recycler = (RecyclerView) rootView.findViewById(R.id.task_recycler);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(mAdapter);
        recycler.addItemDecoration(new DividerItemDecoration(getContext()));

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mRealm != null && !mRealm.isClosed()) {
            mRealm.close();
        }
    }

    public void refreshTasks() {
        mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (TaskRealm task : mTasks) {
                    if (task.isCompleted()) {
                        task.setVisible(false);
                    }
                }
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess: tasks refreshed");
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                Log.d(TAG, "onError: unable to refresh tasks");
            }
        });
    }
}
