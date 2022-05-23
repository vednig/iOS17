package foundation.e.blisslauncher;

import static foundation.e.blisslauncher.util.SettingsCache.NOTIFICATION_BADGING_URI;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;

import foundation.e.blisslauncher.core.DeviceProfile;
import foundation.e.blisslauncher.core.IconsHandler;
import foundation.e.blisslauncher.core.blur.BlurWallpaperProvider;
import foundation.e.blisslauncher.core.customviews.WidgetHost;
import foundation.e.blisslauncher.features.launcher.AppProvider;
import foundation.e.blisslauncher.features.notification.NotificationService;
import foundation.e.blisslauncher.util.SettingsCache;

public class BlissLauncher extends Application {
    private IconsHandler iconsPackHandler;
    private DeviceProfile deviceProfile;

    private AppProvider mAppProvider;

    private static WidgetHost sAppWidgetHost;
    private static AppWidgetManager sAppWidgetManager;

    @Override
    public void onCreate() {
        super.onCreate();
        sAppWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        sAppWidgetHost = new WidgetHost(getApplicationContext(),
                R.id.APPWIDGET_HOST_ID);
        sAppWidgetHost.startListening();

        connectAppProvider();
        BlurWallpaperProvider.Companion.getInstance(this);

        SettingsCache settingsCache = SettingsCache.INSTANCE.get(this);
        SettingsCache.OnChangeListener notificationLister = this::onNotificationSettingsChanged;
        settingsCache.register(NOTIFICATION_BADGING_URI, notificationLister);
        onNotificationSettingsChanged(settingsCache.getValue(NOTIFICATION_BADGING_URI));
    }

    private void onNotificationSettingsChanged(boolean areNotificationDotsEnabled) {
        if (areNotificationDotsEnabled) {
            NotificationService.requestRebind(new ComponentName(
                    this, NotificationService.class));
        }
    }

    public static BlissLauncher getApplication(Context context) {
        return (BlissLauncher) context.getApplicationContext();
    }

    public DeviceProfile getDeviceProfile() {
        if (deviceProfile == null) {
            deviceProfile = new DeviceProfile(this);
        }
        return deviceProfile;
    }

    public void resetDeviceProfile() {
        deviceProfile = new DeviceProfile(this);
    }

    public IconsHandler getIconsHandler() {
        if (iconsPackHandler == null) {
            iconsPackHandler = new IconsHandler(this);
        }

        return iconsPackHandler;
    }

    public void resetIconsHandler() {
        iconsPackHandler = new IconsHandler(this);
    }

    private void connectAppProvider() {
        mAppProvider = AppProvider.getInstance(this);
    }

    public AppProvider getAppProvider() {
        if (mAppProvider == null) {
            connectAppProvider();
        }
        return mAppProvider;
    }

    public WidgetHost getAppWidgetHost() {
        return sAppWidgetHost;
    }

    public AppWidgetManager getAppWidgetManager() {
        return sAppWidgetManager;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        sAppWidgetHost.stopListening();
        sAppWidgetHost = null;
    }

    public static long getLongPressTimeout() {
        return 500;
    }
}
