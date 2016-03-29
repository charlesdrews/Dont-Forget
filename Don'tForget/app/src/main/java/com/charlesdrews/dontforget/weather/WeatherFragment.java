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
import com.charlesdrews.dontforget.weather.WeatherRecyclerAdapter;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
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
        ArrayList<WeatherRecyclerAdapter.WeatherData> data = new ArrayList<>(3);
        WeatherRecyclerAdapter adapter = new WeatherRecyclerAdapter(getActivity(), data);
        mRecycler.setAdapter(adapter);
    }
}
