package com.charlesdrews.dontforget.birthdays;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;

/**
 * Created by charlie on 3/28/16.
 */
public class Birthdays {
    private static final String TAG = "Birthdays";

    public static Cursor getContactsBirthdays(Context context) {
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

        String sortOrder = null;

        ContentResolver contentResolver = context.getContentResolver();
        return contentResolver.query(uri, projection, selection, selectionArgs, sortOrder);
    }

    public static void logBirthdays(Context context) {
        Cursor cursor = getContactsBirthdays(context);

        int nameColNum = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME_PRIMARY) :
                cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME);
        int birthdayColNum = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE);
        
        while (cursor.moveToNext()) {
            String name = cursor.getString(nameColNum);
            String birthday = cursor.getString(birthdayColNum);
            Log.d(TAG, "logBirthdays: " + name + " " + birthday);
        }
    }
}
