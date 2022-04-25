package foundation.e.blisslauncher.features.weather;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.RemoteViews;

import foundation.e.blisslauncher.R;
import foundation.e.blisslauncher.core.customviews.RoundedWidgetView;

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
