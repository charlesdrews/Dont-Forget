package com.charlesdrews.dontforget.birthdays;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;

import com.charlesdrews.dontforget.birthdays.model.ContactSearchResult;

import java.util.Calendar;

/**
 * Build an alert dialog for searching contacts
 * Created by charlie on 4/3/16.
 */
public class AddContactBirthday {
    private static final String TAG = AddContactBirthday.class.getSimpleName();

    private ContactAutoCompleteTextView mAutoComplete;

    public AddContactBirthday() {}

    public void launchContactSearch(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        mAutoComplete = new ContactAutoCompleteTextView(context);
        final ContactSearchAdapter adapter = new ContactSearchAdapter(context, BirthdaysHelper.getAllContacts(context));
        mAutoComplete.setAdapter(adapter);
        mAutoComplete.setThreshold(2);
        mAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ContactSearchResult contact = adapter.getItem(position);
                mAutoComplete.setText(contact.getName());
                mAutoComplete.setSelectedContactId(contact.getContactId());
                mAutoComplete.setSelectedContactlookupKey(contact.getLookupKey());
                mAutoComplete.setSelectedContactName(contact.getName());
            }
        });

        builder.setTitle("Add birthday to contact")
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
                        if (mAutoComplete.getSelectedContactId() != null &&
                                mAutoComplete.getSelectedContactlookupKey() != null &&
                                mAutoComplete.getSelectedContactName() != null) {
                            launchAddBirthday(context,
                                    mAutoComplete.getSelectedContactId(),
                                    mAutoComplete.getSelectedContactlookupKey(),
                                    mAutoComplete.getSelectedContactName()
                            );
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

    public void launchAddBirthday(final Context context, long contactId, final String lookupKey, String name) {
        Log.d(TAG, "launchAddBirthday: contactId " + contactId);

        Uri uri = ContactsContract.Contacts.getLookupUri(contactId, lookupKey);
        final Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setDataAndType(uri, ContactsContract.Contacts.CONTENT_ITEM_TYPE);
        intent.putExtra("finishActivityOnSaveCompleted", true);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Set birthday for " + name);

        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);

        final CheckBox inclYear = new CheckBox(context);
        inclYear.setChecked(true);
        inclYear.setText("Include year");

        final DatePicker datePicker = new DatePicker(context);
        //if (Build.VERSION.SDK_INT > )
        datePicker.setSpinnersShown(true);
        datePicker.setCalendarViewShown(false);

        // set date of DatePicker to contact's birthday if known, else today
        String bday = BirthdaysHelper.getContactBirthdayByLookupKey(context, lookupKey);
        Calendar calendar = Calendar.getInstance();
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
        final View yearSpinner = datePicker.getChildAt(2);
        inclYear.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                yearSpinner.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

        // add views & buttons to dialog
        ll.addView(inclYear);
        ll.addView(datePicker);
        builder.setView(ll)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int[] parts = new int[3];
                        parts[0] = inclYear.isChecked() ? datePicker.getYear() : -1;
                        parts[1] = datePicker.getMonth() + 1; // month is 0-based
                        parts[2] = datePicker.getDayOfMonth();
                        boolean success = BirthdaysHelper
                                .updateBirthdayInContactProvider(context, lookupKey, parts);
                        Log.d(TAG, "onClick: update birthday in contacts success? " + success);
                    }
                });

        builder.show();
    }
}
