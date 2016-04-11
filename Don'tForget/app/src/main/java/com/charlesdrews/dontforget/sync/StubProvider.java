package com.charlesdrews.dontforget.sync;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Stub implementation of ContentProvider
 *
 * Created by charlie on 3/22/16.
 */
public class StubProvider extends ContentProvider {
    public static final String AUTHORITY = "com.charlesdrews.dontforget.sync.StubProvider";
    public static final String BASE_URI_STRING = "content://" + AUTHORITY;
    public static final String WEATHER_PATH = "WEATHER";
    public static final Uri WEATHER_URI_SUCCESS = Uri.parse(BASE_URI_STRING + "/" + WEATHER_PATH + "/SUCCESS");
    public static final Uri WEATHER_URI_FAILURE = Uri.parse(BASE_URI_STRING + "/" + WEATHER_PATH + "/FAILURE");

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
