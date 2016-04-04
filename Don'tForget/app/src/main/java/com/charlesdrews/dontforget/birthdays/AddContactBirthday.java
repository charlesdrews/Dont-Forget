package com.charlesdrews.dontforget.birthdays;


import android.animation.LayoutTransition;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;

import com.charlesdrews.dontforget.MainActivity;
import com.charlesdrews.dontforget.R;
import com.charlesdrews.dontforget.birthdays.model.ContactSearchResult;

import java.lang.reflect.Field;
import java.util.Calendar;

/**
 * Build an alert dialog for searching contacts
 * Created by charlie on 4/3/16.
 */
public class AddContactBirthday {
    private static final String TAG = AddContactBirthday.class.getSimpleName();

    private Context mContext;
    private BirthdayUpdatedListener mListener;
    private ContactAutoCompleteTextView mAutoComplete;

    public AddContactBirthday(Context context) {
        mContext = context;
        mListener = (MainActivity) context;
    }

    public void launchContactSearch() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        mAutoComplete = new ContactAutoCompleteTextView(mContext);
        final ContactSearchAdapter adapter = new ContactSearchAdapter(mContext, BirthdaysHelper.getAllContacts(mContext));
        mAutoComplete.setAdapter(adapter);
        mAutoComplete.setThreshold(2);
        mAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ContactSearchResult contact = adapter.getItem(position);
                mAutoComplete.setText(contact.getName());
                mAutoComplete.setSelectedContactlookupKey(contact.getLookupKey());
                mAutoComplete.setSelectedContactName(contact.getName());
            }
        });

        builder.setTitle("Set a contact's birthday")
                .setView(mAutoComplete)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", null);

        final AlertDialog alertDialog = builder.create();

        // set positive button action AFTER showing the alert dialog in order to prevent
        // the alert dialog from automatically closing on any button click
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mAutoComplete.getSelectedContactlookupKey() != null &&
                                mAutoComplete.getSelectedContactName() != null) {
                            launchAddBirthday(mAutoComplete.getSelectedContactlookupKey(),
                                    mAutoComplete.getSelectedContactName());
                            alertDialog.dismiss();
                        } else {
                            mAutoComplete.setError("Please select a contact");
                        }
                    }
                });
            }
        });
        alertDialog.show();
    }

    public void launchAddBirthday(final String lookupKey, final String name) {
        Log.d(TAG, "launchAddBirthday: " + name);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Set birthday for " + name);

        // set up views in dialog body
        View view = LayoutInflater.from(mContext).inflate(R.layout.birthday_picker_body, null);
        final CheckBox inclYear = (CheckBox) view.findViewById(R.id.birthday_picker_checkbox);
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.birthday_datepicker);

        // set date of DatePicker to contact's birthday if known, else today
        String bday = BirthdaysHelper.getContactBirthdayByLookupKey(mContext, lookupKey);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if (bday != null) {
            int[] bdayParts = BirthdaysHelper.getBdayParts(bday);
            if (bdayParts[0] == -1) { // birth year not known
                datePicker.updateDate(calendar.get(Calendar.YEAR), bdayParts[1] - 1, bdayParts[2]); // month is 0-based
            } else {
                datePicker.updateDate(bdayParts[0], bdayParts[1] - 1, bdayParts[2]); // month is 0-based
            }
        } else {
            datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
        }

        // hide year spinner if user unchecks box
        View yearSpinner = null;
        Object yearPicker = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int yearSpinnerId = Resources.getSystem().getIdentifier("year", "id", "android");
            if (yearSpinnerId != 0) {
                yearSpinner = datePicker.findViewById(yearSpinnerId);
            }
        } else {
            Field[] fields = datePicker.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.getName().equals("mYearPicker") || field.getName().equals("mYearSpinner")) {
                    field.setAccessible(true);
                    try {
                        yearPicker = field.get(datePicker);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        yearPicker = null;
                    }
                }
            }
        }
        final View yearView = (yearSpinner != null) ? yearSpinner : (View) yearPicker;
        inclYear.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (yearView != null) {
                    yearView.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                }
            }
        });

        // add layout transition animations to datepicker
        LayoutTransition transition = new LayoutTransition();
        transition.setDuration(500);
        datePicker.setLayoutTransition(transition);

        // add views & buttons to dialog
        builder.setView(view)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int[] parts = new int[3];
                        parts[0] = inclYear.isChecked() ? datePicker.getYear() : -1;
                        parts[1] = datePicker.getMonth() + 1; // month is 0-based
                        parts[2] = datePicker.getDayOfMonth();
                        boolean success = BirthdaysHelper
                                .updateBirthdayInContactProvider(mContext, lookupKey, parts);
                        Log.d(TAG, "onClick: update birthday in contacts success? " + success);
                        mListener.onBirthdayUpdated(success, name);
                    }
                });

        builder.show();
    }

    public interface BirthdayUpdatedListener {
        void onBirthdayUpdated(boolean success, String name);
    }
}
