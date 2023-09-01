package com.cloudx.ios17.features.weather;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.RemoteViews;

import com.cloudx.ios17.core.customviews.RoundedWidgetView;
import com.cloudx.ios17.core.customviews.RoundedWidgetView;
import com.cloudx.ios17.R;
import com.cloudx.ios17.core.customviews.RoundedWidgetView;
import com.cloudx.ios17.core.customviews.RoundedWidgetView;

public class WeatherWidgetHostView extends RoundedWidgetView {

    public WeatherWidgetHostView(Context context) {
        super(context, true);
        LayoutInflater.from(context).inflate(R.layout.layout_weather_info, this);
    }

    @Override
    public void updateAppWidget(RemoteViews remoteViews) {
        // do nothing
    }
}
