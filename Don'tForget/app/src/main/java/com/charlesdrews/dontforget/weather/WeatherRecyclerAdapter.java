package com.charlesdrews.dontforget.weather;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.charlesdrews.dontforget.R;
import com.charlesdrews.dontforget.weather.model.WeatherData;
import com.charlesdrews.dontforget.weather.model.WeatherDataHourly;

import java.util.List;

/**
 * Created by charlie on 3/28/16.
 */
public class WeatherRecyclerAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int WEATHER_CURRENT = 0;
    private static final int WEATHER_HOURLY = 1;
    private static final int WEATHER_DAILY = 2;

    private Context mContext;
    private List<WeatherData> mData;

    public WeatherRecyclerAdapter(Context context, List<WeatherData> data) {
        mContext = context;
        mData = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case WEATHER_CURRENT:
                view = LayoutInflater.from(mContext).inflate(R.layout.weather_current, parent, false);
                return new CurrentWeatherViewHolder(view);
            case WEATHER_HOURLY:
                view = LayoutInflater.from(mContext).inflate(R.layout.weather_hourly, parent, false);
                return new HourlyWeatherViewHolder(view);
            case WEATHER_DAILY:
                view = LayoutInflater.from(mContext).inflate(R.layout.weather_daily, parent, false);
                return new DailyWeatherViewHolder(view);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        WeatherData data = mData.get(position);

        //TODO - update these
        switch (position) {
            case WEATHER_CURRENT:
                CurrentWeatherViewHolder currentHolder = (CurrentWeatherViewHolder) holder;
                currentHolder.title.setText("Current conditions");
                break;
            case WEATHER_HOURLY:
                HourlyWeatherViewHolder hourlyHolder = (HourlyWeatherViewHolder) holder;
                //hourlyHolder.title.setText("Hourly forecast");
                WeatherDataHourly dataHourly = (WeatherDataHourly) data;
                hourlyHolder.title.setText(dataHourly.getHourlyForecasts().get(0).getCondition());
                //TODO - extend RealmBaseAdapter to make a list here
                break;
            case WEATHER_DAILY:
                DailyWeatherViewHolder dailyHolder = (DailyWeatherViewHolder) holder;
                dailyHolder.title.setText("10-day forecast");
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return 3; //mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        //0 = WEATHER_CURRENT, 1 = WEATHER_HOURLY, 2 = WEATHER_DAILY
        return position;
    }

    public class WeatherViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        public WeatherViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.weather_title);
        }
    }
    public class CurrentWeatherViewHolder extends WeatherViewHolder {
        public CurrentWeatherViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class HourlyWeatherViewHolder extends WeatherViewHolder {
        public HourlyWeatherViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class DailyWeatherViewHolder extends WeatherViewHolder {
        public DailyWeatherViewHolder(View itemView) {
            super(itemView);
        }
    }
}
