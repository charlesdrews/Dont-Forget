package com.charlesdrews.dontforget.birthdays;

import android.content.Context;
import android.widget.AutoCompleteTextView;

/**
 * Add fields to standard AutoCompleteTextView to keep track of person selected
 * Created by charlie on 4/3/16.
 */
public class ContactAutoCompleteTextView extends AutoCompleteTextView {
    private Long selectedContactId;
    private String selectedContactlookupKey, selectedContactName;

    public ContactAutoCompleteTextView(Context context) {
        super(context);
    }

    public Long getSelectedContactId() {
        return selectedContactId;
    }

    public void setSelectedContactId(Long selectedContactId) {
        this.selectedContactId = selectedContactId;
    }

    public String getSelectedContactlookupKey() {
        return selectedContactlookupKey;
    }

    public void setSelectedContactlookupKey(String selectedContactlookupKey) {
        this.selectedContactlookupKey = selectedContactlookupKey;
    }

    public String getSelectedContactName() {
        return selectedContactName;
    }

    public void setSelectedContactName(String selectedContactName) {
        this.selectedContactName = selectedContactName;
    }
}
