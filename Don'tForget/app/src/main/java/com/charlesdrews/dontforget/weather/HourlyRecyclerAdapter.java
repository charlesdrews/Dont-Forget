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
import com.charlesdrews.dontforget.weather.model.HourlyForecastRealm;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by charlie on 4/1/16.
 */
public class HourlyRecyclerAdapter extends RecyclerView.Adapter<HourlyRecyclerAdapter.HourlyViewHolder> {
    private List<HourlyForecastRealm> mData;
    private boolean mUseMetric;
    private Context mContext;

    public HourlyRecyclerAdapter(List<HourlyForecastRealm> data, boolean useMetric) {
        mData = data;
        mUseMetric = useMetric;
    }

    @Override
    public HourlyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.hourly_item, parent, false);
        return new HourlyViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(HourlyViewHolder holder, int position) {
        HourlyForecastRealm forecast = mData.get(position);

        SimpleDateFormat sdf = new SimpleDateFormat("h a", Locale.US);
        holder.time.setText(sdf.format(forecast.getDateTime()).toLowerCase());

        if (mUseMetric) {
            holder.temp.setText(String.format("%d°", forecast.getTempCel()));
        } else {
            holder.temp.setText(String.format("%d°", forecast.getTempFahr()));
        }

        // use droplet or snowflake to precede probability of precipitation, depending on temperature
        String format;
        if (forecast.getTempFahr() > 32) {
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

    public class HourlyViewHolder extends RecyclerView.ViewHolder {
        TextView time, temp, probPrecip;
        ImageView icon;

        public HourlyViewHolder(View itemView) {
            super(itemView);
            time = (TextView) itemView.findViewById(R.id.hourly_time);
            temp = (TextView) itemView.findViewById(R.id.hourly_temp);
            probPrecip = (TextView) itemView.findViewById(R.id.hourly_prob_precip);
            icon = (ImageView) itemView.findViewById(R.id.hourly_icon);
        }
    }
}
