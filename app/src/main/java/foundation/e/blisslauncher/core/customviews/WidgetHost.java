package foundation.e.blisslauncher.core.customviews;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;

import foundation.e.blisslauncher.features.weather.WeatherAppWidgetProvider;
import foundation.e.blisslauncher.features.weather.WeatherWidgetHostView;

public class WidgetHost extends AppWidgetHost {

    private ComponentName mWeatherWidgetProvider;

    public WidgetHost(Context context, int hostId) {
        super(context, hostId);
        mWeatherWidgetProvider = new ComponentName(context, WeatherAppWidgetProvider.class);
    }

    @Override
    protected AppWidgetHostView onCreateView(Context context, int appWidgetId, AppWidgetProviderInfo appWidget) {
        if (appWidget.provider.equals(mWeatherWidgetProvider)) {
            return new WeatherWidgetHostView(context);
        }
        return new RoundedWidgetView(context);
    }

    @Override
    public void stopListening() {
        super.stopListening();
        clearViews();
    }
}
