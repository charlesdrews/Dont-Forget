package com.charlesdrews.dontforget.weather;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.charlesdrews.dontforget.R;
import com.charlesdrews.dontforget.weather.model.DailyForecastRealm;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by charlie on 4/2/16.
 */
public class DailyRecyclerAdapter extends RecyclerView.Adapter<DailyRecyclerAdapter.DailyViewHolder> {
    private List<DailyForecastRealm> mData;
    private boolean mUseMetric;
    private Context mContext;

    public DailyRecyclerAdapter(List<DailyForecastRealm> data, boolean useMetric) {
        mData = data;
        mUseMetric = useMetric;
    }

    @Override
    public DailyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.daily_item, parent, false);
        return new DailyViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(DailyViewHolder holder, int position) {
        DailyForecastRealm forecast = mData.get(position);

        SimpleDateFormat sdf = new SimpleDateFormat("EEE d", Locale.US);
        holder.date.setText(sdf.format(forecast.getDate()).toLowerCase());

        if (mUseMetric) {
            holder.temps.setText(String.format("%d°/%d°", forecast.getTempHighCel(), forecast.getTempLowCel()));
        } else {
            holder.temps.setText(String.format("%d°/%d°", forecast.getTempHighFahr(), forecast.getTempLowFahr()));
        }

        // use droplet or snowflake to precede probability of precipitation, depending on temperature
        String format;
        if (forecast.getTempHighFahr() > 32) {
            format = Html.fromHtml("&#128167;").toString() + "%d%%"; // droplet
        } else {
            format = Html.fromHtml("&#10052;").toString() + "%d%%"; // snowflake
        }
        holder.probPrecip.setText(String.format(format, forecast.getProbOfPrecip()));

        Picasso.with(mContext).load(forecast.getIconUrl()).into(holder.icon);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class DailyViewHolder extends RecyclerView.ViewHolder {
        TextView date, temps, probPrecip;
        ImageView icon;

        public DailyViewHolder(View itemView) {
            super(itemView);
            date = (TextView) itemView.findViewById(R.id.daily_date);
            temps = (TextView) itemView.findViewById(R.id.daily_temps);
            probPrecip = (TextView) itemView.findViewById(R.id.daily_prob_precip);
            icon = (ImageView) itemView.findViewById(R.id.daily_icon);
        }
    }
}
