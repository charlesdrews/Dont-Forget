package com.charlesdrews.dontforget.birthdays;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;

import com.charlesdrews.dontforget.birthdays.model.BirthdayRealm;
import com.charlesdrews.dontforget.birthdays.model.ContactSearchResult;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Provide static methods to retrieve birthdays from Google Contacts and save to Realm
 * Created by charlie on 3/28/16.
 */
public class BirthdaysHelper {
    private static final String TAG = "BirthdaysHelper";
    private static final Uri URI = ContactsContract.Data.CONTENT_URI;

    public static boolean syncContactsBirthdaysToDb(Context context) {
        Cursor cursor = getContactsBirthdays(context);
        if (cursor == null || cursor.getCount() == 0) {
            return false;
        }
        saveBirthdaysToDb(cursor, context);
        return true;
    }

    private static Cursor getContactsBirthdays(Context context) {
        Log.d(TAG, "getContactsBirthdays: starting");

        String[] projection = new String[] {
                ContactsContract.Data._ID,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                        ContactsContract.Data.DISPLAY_NAME_PRIMARY :
                        ContactsContract.Data.DISPLAY_NAME,
                ContactsContract.Data.CONTACT_ID,
                ContactsContract.CommonDataKinds.Event.CONTACT_ID,
                ContactsContract.CommonDataKinds.Event.START_DATE
        };

        String selection = ContactsContract.Data.MIMETYPE + " = ? AND " +
                ContactsContract.CommonDataKinds.Event.TYPE + " = ?";

        String[] selectionArgs = new String[] {
                ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
                String.valueOf(ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY)
        };

        ContentResolver contentResolver = context.getContentResolver();
        return contentResolver.query(URI, projection, selection, selectionArgs, null);
    }

    public static List<ContactSearchResult> getAllContacts(Context context) {
        Log.d(TAG, "getAllContacts: starting");

        String nameField = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                ContactsContract.Data.DISPLAY_NAME_PRIMARY :
                ContactsContract.Data.DISPLAY_NAME;

        String[] projection = new String[]
                {ContactsContract.Contacts._ID, ContactsContract.Contacts.LOOKUP_KEY, nameField};

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(URI, projection, null, null, nameField);
        return convertCursor(cursor);
    }

    public static String getContactBirthdayByLookupKey(Context context, String lookupKey) {
        String[] projection = new String[] {
                ContactsContract.CommonDataKinds.Event.START_DATE
        };

        String selection = ContactsContract.Contacts.LOOKUP_KEY + " = ? AND " +
                ContactsContract.Data.MIMETYPE + " = ? AND " +
                ContactsContract.CommonDataKinds.Event.TYPE + " = ?";

        String[] selectionArgs = new String[] {lookupKey,
                ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
                String.valueOf(ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY)
        };

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(URI, projection, selection, selectionArgs, null);

        if (cursor != null && cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
        }
        return null;
    }

    /**
     * Update the contact specified by lookupKey with the specified birthday.
     * Input bdayParts should be [y,m,d] with -1 for year if not known.
     * Returns true if update successful, else false.
     */
    public static boolean updateBirthdayInContactProvider(Context context, String lookupKey, int[] bdayParts) {
        Log.d(TAG, "updateBirthdayInContactProvider: starting");
        ContentResolver contentResolver = context.getContentResolver();

        // get Event.CONTACT_ID by Contacts.LOOKUP_KEY
        String[] proj1 = new String[]{ContactsContract.CommonDataKinds.Event.CONTACT_ID};
        String sel1 = ContactsContract.Contacts.LOOKUP_KEY + " = ?";
        String[] selArgs1 = new String[]{lookupKey};
        Cursor cur1 = contentResolver.query(URI, proj1, sel1, selArgs1, null);
        long contactId = -1;
        if (cur1 != null && cur1.moveToFirst()) {
            Log.d(TAG, "updateBirthdayInContactProvider: got CONTACT_ID");
            contactId = cur1.getLong(0);
            cur1.close();
        } else {
            Log.d(TAG, "updateBirthdayInContactProvider: failed to get CONTACT_ID");
            if (cur1 != null) {cur1.close();}
            return false;
        }

        // get Event.RAW_CONTACT_ID by Event.CONTACT_ID
        String[] proj2 = new String[]{ContactsContract.CommonDataKinds.Event.RAW_CONTACT_ID};
        String sel2 = ContactsContract.CommonDataKinds.Event.CONTACT_ID + " = ?";
        String[] selArgs2 = new String[]{String.valueOf(contactId)};
        Cursor cur2 = contentResolver.query(URI, proj2, sel2, selArgs2, null);
        long rawContactId = -1;
        if (cur2 != null && cur2.moveToFirst()) {
            Log.d(TAG, "updateBirthdayInContactProvider: got RAW_CONTACT_ID");
            rawContactId = cur2.getLong(0);
            cur2.close();
        } else {
            Log.d(TAG, "updateBirthdayInContactProvider: failed to get RAW_CONTACT_ID");
            if (cur2 != null) {cur2.close();}
            return false;
        }

        // see if birthday already exists for this contact
        String[] proj3 = new String[]{ContactsContract.Data._ID};
        String sel3 = ContactsContract.Data.LOOKUP_KEY + " = ? AND " +
                ContactsContract.Data.MIMETYPE + " = ? AND " +
                ContactsContract.CommonDataKinds.Event.TYPE + " = ?";
        String[] selArgs3 = new String[] {lookupKey,
                ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
                String.valueOf(ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY)};
        Cursor cur3 = contentResolver.query(URI, proj3, sel3, selArgs3, null);
        long birthdayRowId = -1;
        if (cur3 != null) {
            if (cur3.moveToFirst()) {
                birthdayRowId = cur3.getLong(0);
            }
            cur3.close();
        }

        // update or insert birthday
        ContentValues values = new ContentValues();
        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.Event.TYPE, ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY);
        String bdayString = (bdayParts[0] == -1 ? "-" : bdayParts[0]) + "-" + bdayParts[1] +
                "-" + bdayParts[2];
        values.put(ContactsContract.CommonDataKinds.Event.START_DATE, bdayString);
        if (birthdayRowId >= 0) {
            Log.d(TAG, "updateBirthdayInContactProvider: updating existing birthday");
            int numRowsUpdated = contentResolver.update(URI, values, ContactsContract.Data._ID + " = ?",
                    new String[]{String.valueOf(birthdayRowId)});
            return numRowsUpdated > 0;
        } else {
            Log.d(TAG, "updateBirthdayInContactProvider: inserting new birthday");
            Uri newUri = contentResolver.insert(URI, values);
            return newUri != null;
        }
    }

    private static void saveBirthdaysToDb(Cursor cursor, Context context) {
        Log.d(TAG, "saveBirthdaysToDb: starting");
        Realm realm = Realm.getInstance(new RealmConfiguration.Builder(context).build());

        int idColNum = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID);
        int nameColNum = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME_PRIMARY) :
                cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME);
        int birthdayColNum = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE);

        Calendar today = Calendar.getInstance();
        today.setTimeInMillis(System.currentTimeMillis());
        int currentYear = today.get(Calendar.YEAR);
        int currentMonth = today.get(Calendar.MONTH) + 1; // month is 0-based
        int currentDay = today.get(Calendar.DAY_OF_MONTH);

        while (cursor.moveToNext()) {
            int[] bdayParts = getBdayParts(cursor.getString(birthdayColNum));
            final int birthYear = bdayParts[0]; // will be -1 if not known
            int bdayMonth = bdayParts[1];
            int bdayDay = bdayParts[2];
            int nextBdayYear = currentYear;

            if (bdayMonth < currentMonth || (bdayMonth == currentMonth && bdayDay < currentDay) ) {
                nextBdayYear++; // if already had bday this calendar year
            }

            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.set(nextBdayYear, bdayMonth - 1, bdayDay); // month is 0-based
            final Date nextBday = calendar.getTime();

            final int id = cursor.getInt(idColNum);
            final String name = cursor.getString(nameColNum);

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    BirthdayRealm bday = realm.where(BirthdayRealm.class).equalTo("id", id).findFirst();
                    boolean newToRealm = (bday == null);

                    if (newToRealm) {
                        bday = new BirthdayRealm();
                        bday.setNecessaryToNotify(true); // default to true
                    }

                    bday.setId(id);
                    bday.setName(name);
                    bday.setNextBirthday(nextBday);
                    bday.setYearOfBirth(birthYear); // will be -1 if not known

                    if (newToRealm) {
                        realm.copyToRealm(bday);
                    }
                    //Log.d(TAG, String.format("execute: saved %s %s", name, nextBday.toString()));
                }
            });
        }
        cursor.close();
        realm.close();
    }

    /**
     * Parses birthday from format provided by Contacts Provider into array of parts.
     * @param bday - String from query
     * @return [Y,M,D] if input formatted YYYY-MM-DD] else [-1,M,D] if input formatted --MM-DD
     */
    public static int[] getBdayParts(String bday) {
        int[] yearMonthDay = new int[3];
        String[] parts = bday.split("-");

        if (parts.length > 3) { // year not known; input was --MM-DD; parts is [,,MM,DD]
            yearMonthDay[0] = -1;
            yearMonthDay[1] = Integer.parseInt(parts[2]);
            yearMonthDay[2] = Integer.parseInt(parts[3]);
        } else { // year know; input was YYYY-MM-DD; parts is [YYYY,MM,DD]
            yearMonthDay[0] = Integer.parseInt(parts[0]);
            yearMonthDay[1] = Integer.parseInt(parts[1]);
            yearMonthDay[2] = Integer.parseInt(parts[2]);
        }

        return yearMonthDay;
    }

    private static List<ContactSearchResult> convertCursor(Cursor cursor) {
        String nameField = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                ContactsContract.Data.DISPLAY_NAME_PRIMARY :
                ContactsContract.Data.DISPLAY_NAME;

        HashMap<String, String> contactsMap = new HashMap<>(cursor.getCount());

        while (cursor.moveToNext()) {
            String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
            String name = cursor.getString(cursor.getColumnIndex(nameField));
            ContactSearchResult contact = new ContactSearchResult(lookupKey, name);

            if (name != null && !name.isEmpty()) {
                contactsMap.put(lookupKey, name);
            }
        }

        ArrayList<ContactSearchResult> results = new ArrayList<>(contactsMap.size());
        for (String key : contactsMap.keySet()) {
            results.add(new ContactSearchResult(key, contactsMap.get(key)));
        }

        cursor.close();
        return results;
    }
}
