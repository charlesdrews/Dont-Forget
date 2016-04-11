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

import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;


/**
 * View tasks & perform CRUD operations for task objects
 */
public class TaskFragment extends Fragment implements TaskRecyclerAdapter.OnSelectTaskListener {

    private static final String TAG = TaskFragment.class.getSimpleName();

    private Realm mRealm;
    private RealmResults<TaskRealm> mTasks;
    private TaskRecyclerAdapter mAdapter;

    public TaskFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

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

        mAdapter = new TaskRecyclerAdapter(getContext(), mTasks, this);

        mTasks.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                Log.d(TAG, "onChange: mTasks changed");
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshTasks();
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
        for (int i = mTasks.size() - 1; i >= 0; i--) {
            TaskRealm task = mTasks.get(i);
            task.setVisible(!task.isCompleted());
        }
        mRealm.commitTransaction();
    }

    public void addOrUpdateTask(final TaskRealm taskToUpdate) {
        Log.d(TAG, "addOrUpdateTask: starting");

        // set up view for dialog body
        View view = LayoutInflater.from(getContext()).inflate(R.layout.task_adder_body, null);
        final DayPicker dayPicker = (DayPicker) view.findViewById(R.id.add_task_day_picker);
        final NumberPicker timePicker = (NumberPicker) view.findViewById(R.id.add_task_time_picker);
        final EditText editText = (EditText) view.findViewById(R.id.add_task_input);

        // set up DayPicker
        dayPicker.setup(taskToUpdate);

        // set up timePicker to show the enum options
        timePicker.setMinValue(TimeOfDay.getMin());
        timePicker.setMaxValue(TimeOfDay.getMax());
        timePicker.setWrapSelectorWheel(true);
        String[] times = new String[TimeOfDay.getCount()];
        for (int i = 0; i < TimeOfDay.getCount(); i++) {
            times[i] = TimeOfDay.getTimeOfDay(i).toString();
        }
        timePicker.setDisplayedValues(times);

        // set timePicker and editText initial values if available
        if (taskToUpdate != null) {

            int timeOfDay = taskToUpdate.getTimeOfDay();
            if (timeOfDay >= TimeOfDay.getMin() && timeOfDay <= TimeOfDay.getMax()) {
                timePicker.setValue(taskToUpdate.getTimeOfDay());
            }

            String taskText = taskToUpdate.getTaskText();
            if (taskText != null && !taskText.isEmpty()) {
                editText.setText(taskText);
            }
        }

        // Initialize dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setView(view)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", null);

        if (taskToUpdate != null) {
            builder.setTitle("Update task")
                    .setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mRealm.beginTransaction();
                            taskToUpdate.setVisible(false);
                            mRealm.commitTransaction();
                        }
                    });
        } else {
            builder.setTitle("Add new task");
        }

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
                            TaskRealm task;
                            if (taskToUpdate != null) {
                                task = taskToUpdate;
                            } else {
                                task = mRealm.createObject(TaskRealm.class);
                            }

                            Calendar cal = Calendar.getInstance();
                            cal.setTimeInMillis(System.currentTimeMillis());
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

    @Override
    public void onTaskSelect(TaskRealm task) {
        addOrUpdateTask(task);
    }
}
