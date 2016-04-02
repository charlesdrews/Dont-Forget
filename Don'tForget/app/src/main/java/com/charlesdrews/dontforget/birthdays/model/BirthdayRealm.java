package com.charlesdrews.dontforget.birthdays.model;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Models a birthday entry for the Realm database
 * Created by charlie on 4/2/16.
 */
public class BirthdayRealm extends RealmObject {
    @PrimaryKey private int id;

    private String name;
    private Date nextBirthday;
    private int yearOfBirth;
    private boolean necessaryToNotify;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getNextBirthday() {
        return nextBirthday;
    }

    public void setNextBirthday(Date nextBirthday) {
        this.nextBirthday = nextBirthday;
    }

    public int getYearOfBirth() {
        return yearOfBirth;
    }

    public void setYearOfBirth(int yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

    public boolean isNecessaryToNotify() {
        return necessaryToNotify;
    }

    public void setNecessaryToNotify(boolean necessaryToNotify) {
        this.necessaryToNotify = necessaryToNotify;
    }
}
