package foundation.e.blisslauncher.features.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import foundation.e.blisslauncher.BlissLauncher;
import foundation.e.blisslauncher.R;
import foundation.e.blisslauncher.core.customviews.RoundedWidgetView;
import foundation.e.blisslauncher.core.utils.ThemesKt;
import foundation.e.blisslauncher.features.launcher.LauncherActivity;
import timber.log.Timber;

public class WidgetViewBuilder {

    private static final String TAG = "WidgetViewBuilder";

    public static final String WIDGET_OPTION_DARK_TEXT = "foundation.e.blisslauncher.WIDGET_OPTION_DARK_TEXT";

    public static RoundedWidgetView create(LauncherActivity launcherActivity,
            @NonNull RoundedWidgetView roundedWidgetView) {
        if (BlissLauncher.getApplication(launcherActivity).getAppWidgetHost() == null)
            return null;
        roundedWidgetView.post(
                () -> updateWidgetOption(launcherActivity, roundedWidgetView, roundedWidgetView.getAppWidgetInfo()));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        int margin = launcherActivity.getResources().getDimensionPixelSize(R.dimen.widget_margin);
        layoutParams.setMargins(0, margin, 0, margin);
        roundedWidgetView.setLayoutParams(layoutParams);

        roundedWidgetView.setOnLongClickListener(v -> {
            AppWidgetProviderInfo widgetProviderInfo = roundedWidgetView.getAppWidgetInfo();
            if ((widgetProviderInfo.resizeMode
                    & AppWidgetProviderInfo.RESIZE_VERTICAL) == AppWidgetProviderInfo.RESIZE_VERTICAL) {
                launcherActivity.showWidgetResizeContainer(roundedWidgetView);
            } else {
                Timber.tag(TAG).i(launcherActivity.getString(R.string.widget_is_not_resizable));
            }
            return true;
        });

        return roundedWidgetView;
    }

    private static void updateWidgetOption(Context context, RoundedWidgetView roundedWidgetView,
            AppWidgetProviderInfo info) {
        Bundle newOps = new Bundle();
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,
                BlissLauncher.getApplication(context).getDeviceProfile().getMaxWidgetWidth());
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,
                BlissLauncher.getApplication(context).getDeviceProfile().getMaxWidgetWidth());
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, info.minHeight);
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,
                BlissLauncher.getApplication(context).getDeviceProfile().getMaxWidgetHeight());
        newOps.putBoolean(WIDGET_OPTION_DARK_TEXT, ThemesKt.isWorkspaceDarkText(context));
        roundedWidgetView.updateAppWidgetOptions(newOps);
    }
}
