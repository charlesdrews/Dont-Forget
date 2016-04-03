package com.charlesdrews.dontforget.birthdays.model;

/**
 * Created by charlie on 4/3/16.
 */
public class ContactSearchResult {
    private String lookupKey, name;

    public ContactSearchResult(String lookupKey, String name) {
        this.lookupKey = lookupKey;
        this.name = name;
    }

    public String getLookupKey() { return lookupKey; }

    public String getName() { return name; }
}
