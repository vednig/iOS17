package foundation.e.blisslauncher.core.customviews;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.view.ContextThemeWrapper;

import foundation.e.blisslauncher.core.utils.ThemesKt;
import foundation.e.blisslauncher.features.weather.WeatherAppWidgetProvider;
import foundation.e.blisslauncher.features.weather.WeatherWidgetHostView;
import foundation.e.blisslauncher.features.widgets.DefaultWidgets;

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
