package com.charlesdrews.dontforget.birthdays;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;

import com.charlesdrews.dontforget.birthdays.model.BirthdayRealm;

import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Provide static methods to retrieve birthdays from Google Contacts and save to Realm
 * Created by charlie on 3/28/16.
 */
public class BirthdaysHelper {
    private static final String TAG = "BirthdaysHelper";

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
        Uri uri = ContactsContract.Data.CONTENT_URI;

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
        return contentResolver.query(uri, projection, selection, selectionArgs, null);
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
                    Log.d(TAG, String.format("execute: saved %s %s", name, nextBday.toString()));
                }
            });
        }
        cursor.close();
        realm.close();
    }

    private static int[] getBdayParts(String bday) {
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
}
