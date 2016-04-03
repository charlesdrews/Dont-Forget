package com.charlesdrews.dontforget.birthdays;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.charlesdrews.dontforget.birthdays.model.ContactSearchResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Bind contact search results to dropdown list; filter by user query
 * Created by charlie on 4/3/16.
 */
public class ContactSearchAdapter extends ArrayAdapter<ContactSearchResult> {
    private Context mContext;
    private List<ContactSearchResult> mContactList, mSuggestions;

    public ContactSearchAdapter(Context context, List<ContactSearchResult> contacts) {
        super(context, android.R.layout.simple_dropdown_item_1line, contacts);
        mContext = context;
        mContactList = contacts;
        mSuggestions = new ArrayList<>(contacts.size());
        mSuggestions.addAll(contacts);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
        }

        TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
        ContactSearchResult contact = mSuggestions.get(position);
        if (contact != null) {
            textView.setText(contact.getName());
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return mSuggestions.size();
    }

    @Override
    public ContactSearchResult getItem(int position) {
        return mSuggestions.get(position);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                if (constraint != null) {
                    ArrayList<ContactSearchResult> suggestions = new ArrayList<>();
                    for (ContactSearchResult contact : mContactList) {
                        if (contact.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            suggestions.add(contact);
                        }
                    }
                    results.values = suggestions;
                    results.count = suggestions.size();
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mSuggestions.clear();
                if (results != null && results.count > 0) {
                    // populate suggestions list w/ filter results, if any
                    mSuggestions.addAll( (List<ContactSearchResult>) results.values );
                } else {
                    // else populate with all contacts
                    mSuggestions.addAll(mContactList);
                }
                notifyDataSetChanged();
            }
        };
    }
}
