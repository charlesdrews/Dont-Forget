package com.charlesdrews.dontforget.weather;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.charlesdrews.dontforget.R;
import com.charlesdrews.dontforget.weather.model.HourlyForecast;
import com.charlesdrews.dontforget.weather.model.WeatherData;
import com.charlesdrews.dontforget.weather.model.WeatherDataHourly;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;


public class WeatherFragment extends Fragment {
    private Context mContext;
    private RecyclerView mRecycler;

    public WeatherFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = container.getContext();
        View rootView = inflater.inflate(R.layout.fragment_weather, container, false);
        mRecycler = (RecyclerView) rootView.findViewById(R.id.weather_recycler_view);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));


        //TODO - populate ArrayList w/ actual data
        ArrayList<WeatherData> data = new ArrayList<>(3);
        data.add(0, new WeatherData()); // current weather

        // get hourly forecast data from Realm db
        Realm realm = Realm.getDefaultInstance();
        //TODO - sort the results
        RealmResults<HourlyForecast> hourlyForecasts = realm.where(HourlyForecast.class)
                .findAll();
        data.add(1, new WeatherDataHourly(1, hourlyForecasts));

        data.add(2, new WeatherData()); // daily weather

        WeatherRecyclerAdapter adapter = new WeatherRecyclerAdapter(getActivity(), data);
        mRecycler.setAdapter(adapter);
    }

    //TODO - refresh data on pullDown gesture
}
