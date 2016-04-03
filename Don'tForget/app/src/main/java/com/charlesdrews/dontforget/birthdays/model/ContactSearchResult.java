package com.charlesdrews.dontforget.birthdays.model;

/**
 * Created by charlie on 4/3/16.
 */
public class ContactSearchResult {
    private long contactId;
    private String lookupKey, name;

    public ContactSearchResult(int contactId, String lookupKey, String name) {
        this.contactId = contactId;
        this.lookupKey = lookupKey;
        this.name = name;
    }

    public long getContactId() { return contactId; }

    public String getLookupKey() { return lookupKey; }

    public String getName() { return name; }
}
