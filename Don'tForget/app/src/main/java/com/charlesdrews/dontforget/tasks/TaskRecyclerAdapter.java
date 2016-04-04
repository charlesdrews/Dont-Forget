package com.charlesdrews.dontforget.tasks;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.charlesdrews.dontforget.R;
import com.charlesdrews.dontforget.notifications.TimeOfDay;
import com.charlesdrews.dontforget.tasks.model.TaskRealm;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;

/**
 * Bind tasks to the task fragment recycler view
 * Created by charlie on 4/3/16.
 */
public class TaskRecyclerAdapter extends RecyclerView.Adapter<TaskRecyclerAdapter.TaskViewHolder> {
    private static final String TAG = TaskRecyclerAdapter.class.getSimpleName();

    private Context mContext;
    private List<TaskRealm> mTasks;

    public TaskRecyclerAdapter(Context context, List<TaskRealm> tasks) {
        mContext = context;
        mTasks = tasks;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(final TaskViewHolder holder, int position) {
        final TaskRealm task = mTasks.get(position);

        holder.taskText.setText(task.getTaskText());
        holder.timeOfDay.setText(TimeOfDay.getTimeOfDay(task.getTimeOfDay()).toString());
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d", Locale.US);
        holder.date.setText(sdf.format(task.getDate()));

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(task.isCompleted());

        if (task.isCompleted()) {
            holder.taskText.addStrikeThru();
            holder.timeOfDay.addStrikeThru();
            holder.date.addStrikeThru();
        } else {
            holder.taskText.removeStrikeThru();
            holder.timeOfDay.removeStrikeThru();
            holder.date.removeStrikeThru();
        }

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                task.setCompleted(isChecked);
                realm.commitTransaction();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTasks.size();
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        StrikeThruTextView taskText, timeOfDay, date;

        public TaskViewHolder(View itemView) {
            super(itemView);
            checkBox = (CheckBox) itemView.findViewById(R.id.task_checkbox);
            taskText = (StrikeThruTextView) itemView.findViewById(R.id.task_text);
            timeOfDay = (StrikeThruTextView) itemView.findViewById(R.id.task_time_of_day);
            date = (StrikeThruTextView) itemView.findViewById(R.id.task_date);
        }
    }
}
