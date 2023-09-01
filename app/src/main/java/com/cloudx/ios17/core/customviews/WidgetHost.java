package com.cloudx.ios17.core.customviews;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.view.ContextThemeWrapper;

import com.cloudx.ios17.features.weather.WeatherAppWidgetProvider;
import com.cloudx.ios17.features.weather.WeatherWidgetHostView;
import com.cloudx.ios17.features.weather.WeatherAppWidgetProvider;
import com.cloudx.ios17.features.weather.WeatherWidgetHostView;
import com.cloudx.ios17.core.utils.ThemesKt;
import com.cloudx.ios17.features.weather.WeatherAppWidgetProvider;
import com.cloudx.ios17.features.weather.WeatherWidgetHostView;
import com.cloudx.ios17.features.widgets.DefaultWidgets;
import com.cloudx.ios17.features.weather.WeatherAppWidgetProvider;
import com.cloudx.ios17.features.weather.WeatherWidgetHostView;

public class WidgetHost extends AppWidgetHost {

    public WidgetHost(Context context, int hostId) {
        super(context, hostId);
    }

    @Override
    protected AppWidgetHostView onCreateView(Context context, int appWidgetId, AppWidgetProviderInfo appWidget) {
        if (appWidget.provider.equals(WeatherAppWidgetProvider.COMPONENT_NAME)) {
            Context themedContext = new ContextThemeWrapper(context, ThemesKt.getActivityThemeRes(context));
            return new WeatherWidgetHostView(themedContext);
        }
        ComponentName provider = appWidget.provider;
        boolean isDefaultWidget = DefaultWidgets.INSTANCE.getWidgets().contains(provider);
        return new RoundedWidgetView(context, isDefaultWidget);
    }

    @Override
    public void stopListening() {
        super.stopListening();
        clearViews();
    }
}
