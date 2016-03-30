package com.charlesdrews.dontforget.settings;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.TextView;

import com.charlesdrews.dontforget.weather.retrofit.WeatherHelper;
import com.charlesdrews.dontforget.weather.model.Location;

import java.util.List;

/**
 * Created by charlie on 3/29/16.
 */
public class WeatherLocationPreference extends EditTextPreference {
    private static final String TAG = "WeatherLocationPref";

    private AutoCompleteTextView mEditText;
    private List<Location> mSuggestions;
    private WeatherLocationAdapter mAdapter;

    public WeatherLocationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        /*
        EditText editText = mEditText;
        editText.setText(getText());

        ViewParent oldParent = editText.getParent();
        if(oldParent != view) {
            if (oldParent != null) {
                ((ViewGroup) oldParent).removeView(editText);
            }
            onAddEditTextToDialogView(view, editText);
        }
        */
        // save params from existing EditText, then remove it
        EditText editText = (EditText) view.findViewById(android.R.id.edit);
        ViewGroup.LayoutParams params = editText.getLayoutParams();
        ViewGroup viewGroup = (ViewGroup) editText.getParent();
        String currentText = editText.getText().toString();
        viewGroup.removeView(editText);

        // replace with AutoCompleteTextView
        mEditText = new AutoCompleteTextView(getContext());
        mEditText.setLayoutParams(params);
        mEditText.setId(android.R.id.edit);
        mEditText.setText(currentText);
        mEditText.setThreshold(2);
        viewGroup.addView(mEditText);

        // initialize the adapter
        new InitAdapterAsync().execute(getText());
    }

    @Override
    public EditText getEditText() {
        return mEditText;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult && mEditText != null) {
            String value = mEditText.getText().toString();
            if (callChangeListener(value)) {
                setText(value);
            }
        }
    }

    public class WeatherLocationAdapter extends ArrayAdapter<Location> {
        private Context mContext;
        private List<Location> mData;

        public WeatherLocationAdapter(Context context, List<Location> objects) {
            super(context, android.R.layout.simple_dropdown_item_1line, objects);
            mContext = context;
            mData = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext)
                        .inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
            }

            final TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
            textView.setText(mData.get(position).getName());

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEditText.setText(textView.getText());
                    mEditText.dismissDropDown();
                }
            });
            return convertView;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        /**
         * Don't want to do any filtering in this case, just use the results from
         * the Weather Underground AutoComplete API
         * (https://www.wunderground.com/weather/api/d/docs?d=autocomplete-api)
         */
        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) { return null; }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {}
            };
        }
    }

    public class InitAdapterAsync extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            Log.d(TAG, "doInBackground: begin search for init");
            mSuggestions = WeatherHelper.getLocations(params[0]);
            return (mSuggestions != null && mSuggestions.size() > 0);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                Log.d(TAG, "onPostExecute: results rec'd for init");
                //mEditText = new AutoCompleteTextView(mContext);

                mAdapter = new WeatherLocationAdapter(getContext(), mSuggestions);
                mEditText.setAdapter(mAdapter);

                mEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        new LocationSearchAsync().execute(s.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
            }
        }
    }

    public class LocationSearchAsync extends AsyncTask<String, Void, Boolean> {
        private List<Location> mLocationResults;

        @Override
        protected Boolean doInBackground(String... params) {
            Log.d(TAG, "doInBackground: begin search");
            mLocationResults = WeatherHelper.getLocations(params[0]);
            return (mLocationResults != null && mLocationResults.size() > 0);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                Log.d(TAG, "onPostExecute: results rec'd");
                mSuggestions.clear();
                mSuggestions.addAll(mLocationResults);
                mAdapter.notifyDataSetChanged();
            }
        }
    }
}
