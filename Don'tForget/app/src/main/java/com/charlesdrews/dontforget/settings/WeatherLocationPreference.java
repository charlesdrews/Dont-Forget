package com.charlesdrews.dontforget.settings;

import android.content.Context;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.charlesdrews.dontforget.weather.model.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by charlie on 3/29/16.
 */
public class WeatherLocationPreference extends EditTextPreference {
    private AutoCompleteTextView mEditText;
    private ArrayList<Location> mSuggestions;
    private WeatherLocationAdapter mAdapter;

    public WeatherLocationPreference(Context context) {
        super(context);
        mEditText = new AutoCompleteTextView(context);
        mEditText.setThreshold(2);

        mSuggestions = new ArrayList<>();
        mAdapter = new WeatherLocationAdapter(
                context, android.R.layout.simple_dropdown_item_1line, mSuggestions);
        mEditText.setAdapter(mAdapter);

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public class WeatherLocationAdapter extends ArrayAdapter<Location> {
        private Context mContext;
        private int mResource;
        private List<Location> mData;

        public WeatherLocationAdapter(Context context, int resource, List<Location> objects) {
            super(context, resource, objects);
            mContext = context;
            mData = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(mResource, parent, false);
            }

            TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
            Location location = mData.get(position);
            textView.setText(location.getName());
            return convertView;
        }

        @Override
        public int getCount() {
            return mData.size();
        }
    }
}
