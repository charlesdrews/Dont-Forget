package com.charlesdrews.dontforget.tasks;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;

import com.charlesdrews.dontforget.DividerItemDecoration;
import com.charlesdrews.dontforget.R;
import com.charlesdrews.dontforget.notifications.TimeOfDay;
import com.charlesdrews.dontforget.tasks.model.TaskRealm;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
                    firstTask.setTaskText("Set some more tasks for yourself! :)");
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
        mRealm.beginTransaction();
        // there is a bug w/ iterators in Realm, so use normal for loop
        for (int i = 0; i < mTasks.size(); i++) {
            TaskRealm task = mTasks.get(i);
            task.setVisible(!task.isCompleted());
        }
        mRealm.commitTransaction();
    }

    public void addTask() {
        Log.d(TAG, "addTask: starting");

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add new task");

        View view = LayoutInflater.from(getContext()).inflate(R.layout.task_adder_body, null);
        final NumberPicker dayPicker = (NumberPicker) view.findViewById(R.id.add_task_day_picker);
        final NumberPicker timePicker = (NumberPicker) view.findViewById(R.id.add_task_time_picker);
        final EditText editText = (EditText) view.findViewById(R.id.add_task_input);

        // set up dayPicker to show today & next 2 weeks
        dayPicker.setMinValue(0);
        dayPicker.setMaxValue(13);
        dayPicker.setWrapSelectorWheel(false);
        String[] days = new String[14];
        SimpleDateFormat sdf = new SimpleDateFormat("EEE M/d", Locale.US);
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < 14; i++) {
            days[i] = sdf.format(calendar.getTime());
            calendar.add(Calendar.DATE, 1);
        }
        dayPicker.setDisplayedValues(days);

        // set up timePicker to show the 4 enum options
        timePicker.setMaxValue(0);
        timePicker.setMaxValue(3);
        timePicker.setWrapSelectorWheel(true);
        String[] times = new String[4];
        for (int i = 0; i < 4; i++) {
            times[i] = TimeOfDay.getTimeOfDay(i).toString();
        }
        timePicker.setDisplayedValues(times);

        // add views & buttons to dialog
        builder.setView(view)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", null);

        // set OK button on click listener after launching so it doesn't auto-dismiss
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (editText.getText().toString().isEmpty()) {
                            editText.setError("Please enter a task");
                            editText.requestFocus();
                        } else {
                            mRealm.beginTransaction();
                            TaskRealm task = mRealm.createObject(TaskRealm.class);

                            Calendar cal = Calendar.getInstance();
                            cal.add(Calendar.DATE, dayPicker.getValue());
                            task.setDate(cal.getTime());

                            task.setTimeOfDay(timePicker.getValue());
                            task.setTaskText(editText.getText().toString());
                            task.setCompleted(false);
                            task.setVisible(true);
                            mRealm.commitTransaction();

                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });
        alertDialog.show();
    }
}
