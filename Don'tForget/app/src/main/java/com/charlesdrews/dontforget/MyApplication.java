package com.charlesdrews.dontforget;

import android.app.Application;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

/**
 * Configure the default Realm instance for the whole application
 *
 * Created by charlie on 3/29/16.
 */
public class MyApplication extends Application {

    public static final int REALM_SCHEMA_VERSION = 1;

    @Override
    public void onCreate() {
        super.onCreate();

        RealmConfiguration config = new RealmConfiguration.Builder(this)
                .schemaVersion(REALM_SCHEMA_VERSION)
                .migration(getMigration())
                .build();

        Realm.setDefaultConfiguration(config);
    }

    private static RealmMigration getMigration() {
        return new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                RealmSchema schema = realm.getSchema();

                // Migrate from version 0 to version 1:
                //  - Add queryString & timeObtainedInMillis to CurrentConditionsRealm
                //  - Create new LocationRealm object
                //  - Add lookupKey to BirthdayRealm
                if (oldVersion == 0) {
                    schema.get("CurrentConditionsRealm")
                            .addPrimaryKey("id")
                            .addField("timeObtainedInMillis", long.class);

                    schema.create("LocationRealm")
                            .addPrimaryKey("id")
                            .addField("locationString", String.class)
                            .addField("timeObtainedInMillis", long.class);

                    schema.get("BirthdayRealm")
                            .addField("lookupKey", String.class);

//                    oldVersion++; // increment before handling additional migrations
                }
            }
        };
    }
}
