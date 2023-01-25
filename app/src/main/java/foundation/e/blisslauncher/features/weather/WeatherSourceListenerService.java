package foundation.e.blisslauncher.features.weather;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import foundation.e.blisslauncher.core.Preferences;
import foundation.e.blisslauncher.core.utils.Constants;
import lineageos.weather.LineageWeatherManager;
import timber.log.Timber;

public class WeatherSourceListenerService extends Service
        implements
            LineageWeatherManager.WeatherServiceProviderChangeListener {

    private static final String TAG = WeatherSourceListenerService.class.getSimpleName();
    private static final boolean D = Constants.DEBUG;
    private Context mContext;
    private volatile boolean mRegistered;

    @Override
    public void onWeatherServiceProviderChanged(String providerLabel) {
        if (D)
            Timber.tag(TAG).d("Weather Source changed %s", providerLabel);
        Preferences.setWeatherSource(mContext, providerLabel);
        Preferences.setCachedWeatherInfo(mContext, 0, null);
        // The data contained in WeatherLocation is tightly coupled to the weather
        // provider
        // that generated that data, so we need to clear the cached weather location and
        // let the new
        // weather provider regenerate the data if the user decides to use custom
        // location again
        Preferences.setCustomWeatherLocationCity(mContext, null);
        Preferences.setCustomWeatherLocation(mContext, null);
        Preferences.setUseCustomWeatherLocation(mContext, false);

        if (providerLabel != null) {
            mContext.startService(new Intent(mContext, WeatherUpdateService.class)
                    .putExtra(WeatherUpdateService.ACTION_FORCE_UPDATE, true));
        }
    }

    @Override
    public void onCreate() {
        mContext = getApplicationContext();
        final LineageWeatherManager weatherManager = LineageWeatherManager.getInstance(mContext);
        weatherManager.registerWeatherServiceProviderChangeListener(this);
        mRegistered = true;
        if (D)
            Timber.tag(TAG).d("Listener registered");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mRegistered) {
            final LineageWeatherManager weatherManager = LineageWeatherManager.getInstance(mContext);
            weatherManager.unregisterWeatherServiceProviderChangeListener(this);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
