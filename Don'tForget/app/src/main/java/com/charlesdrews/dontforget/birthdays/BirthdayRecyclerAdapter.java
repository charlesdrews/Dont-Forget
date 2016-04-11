package com.charlesdrews.dontforget.birthdays;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.charlesdrews.dontforget.R;
import com.charlesdrews.dontforget.birthdays.model.BirthdayRealm;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;

/**
 * Bind contact birthdays to the RecyclerView in BirthdaysFragment
 * Created by charlie on 4/2/16.
 */
public class BirthdayRecyclerAdapter
        extends RecyclerView.Adapter<BirthdayRecyclerAdapter.BirthdayViewHolder> {

    private Context mContext;
    private List<BirthdayRealm> mData;
    private ProvidesViewForSnackbar mViewProvider;

    public BirthdayRecyclerAdapter(Context context, List<BirthdayRealm> data,
                                   ProvidesViewForSnackbar viewProvider) {
        mContext = context;
        mData = data;
        mViewProvider = viewProvider;
    }

    @Override
    public BirthdayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.birthday_item, parent, false);
        return new BirthdayViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(final BirthdayViewHolder holder, int position) {
        final BirthdayRealm bday = mData.get(position);

        // set up notifications on/off checkbox
        holder.checkBox.setOnCheckedChangeListener(null); // disable any previous listener
        holder.checkBox.setChecked(bday.isNecessaryToNotify());
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                bday.setNecessaryToNotify(isChecked);
                realm.commitTransaction();
                realm.close();

                String message = "Birthday notifications " + (isChecked ? "ON" : "OFF") +
                        " for " + bday.getName();
                Snackbar.make(mViewProvider.getViewForSnackbar(), message, Snackbar.LENGTH_LONG).show();
            }
        });

        // populate text views
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d", Locale.US);
        String bdayString = sdf.format(bday.getNextBirthday());
        int birthYear = bday.getYearOfBirth();

        if (birthYear == -1) { // don't know birth year / age
            holder.nameAge.setText(bday.getName());
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.setTime(bday.getNextBirthday());
            int yearOfNextBday = calendar.get(Calendar.YEAR); // could be this year or next

            int nextAge = yearOfNextBday - birthYear;

            holder.nameAge.setText(String.format(Locale.US, "%s (%d)", bday.getName(), nextAge));
            bdayString = bdayString + ", " + birthYear;
        }
        holder.date.setText(bdayString);

        // set on click listener
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddContactBirthday dialog = new AddContactBirthday(mContext);
                dialog.launchAddBirthday(bday.getLookupKey(), bday.getName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class BirthdayViewHolder extends RecyclerView.ViewHolder {
        View container;
        CheckBox checkBox;
        TextView nameAge, date;

        public BirthdayViewHolder(View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.birthday_container);
            checkBox = (CheckBox) itemView.findViewById(R.id.birthday_checkbox);
            nameAge = (TextView) itemView.findViewById(R.id.birthday_name_age);
            date = (TextView) itemView.findViewById(R.id.birthday_date);
        }
    }

    public interface ProvidesViewForSnackbar {
        View getViewForSnackbar();
    }
}
