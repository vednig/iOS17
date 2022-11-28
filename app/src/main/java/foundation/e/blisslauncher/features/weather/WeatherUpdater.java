package foundation.e.blisslauncher.features.weather;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.location.LocationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;

import foundation.e.blisslauncher.core.Preferences;
import lineageos.weather.LineageWeatherManager;
import lineageos.weather.WeatherInfo;
import lineageos.weather.WeatherLocation;

public class WeatherUpdater {

    private static final String TAG = "WeatherUpdater";
    private static final long DEFAULT_FORCE_REQUEST_PERIOD_IN_MS = 10L * 1000L;

    private final LocationManager mLocationManager;
    private final WeakReference<Context> mWeakContext;
    private Location mGpsLocation;
    private Location mNetworkLocation;
    private long mForceRequestPeriodInMs = DEFAULT_FORCE_REQUEST_PERIOD_IN_MS;

    private static WeatherUpdater mInstance = null;

    public static WeatherUpdater getInstance(@NonNull Context context) {
        if (mInstance == null) {
            mInstance = new WeatherUpdater(context);
        }

        return mInstance;
    }

    private WeatherUpdater(@NonNull Context context) {
        mWeakContext = new WeakReference<>(context);
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public void checkWeatherRequest() {
        Context context = mWeakContext.get();
        long refreshPeriod = Preferences.weatherRefreshIntervalInMs(context);
        long elapsedTime = Math.abs(SystemClock.elapsedRealtime() - Preferences.lastWeatherUpdateTimestamp(context));

        boolean isPeriodicRequestAllowed = refreshPeriod != 0 && elapsedTime >= refreshPeriod;
        if (isPeriodicRequestAllowed) {
            forceWeatherRequest();
        }
    }

    public void forceWeatherRequest() {
        if (canForceWeatherRequest()) {
            updateWeather();
            increaseForceRequestPeriod();
        }
    }

    private boolean canForceWeatherRequest() {
        Context context = mWeakContext.get();

        long elapsedTime = Math.abs(SystemClock.elapsedRealtime() - Preferences.getForceRequestLastTry(context));

        boolean isRequestAllowed = elapsedTime >= mForceRequestPeriodInMs;

        if (isRequestAllowed) {
            Preferences.setForceRequestLastTry(context, SystemClock.elapsedRealtime());
        }

        return isRequestAllowed;
    }

    private void updateWeather() {
        Log.i(TAG, "Updating weather");
        Context context = mWeakContext.get();

        if (Preferences.useCustomWeatherLocation(context)) {
            requestCustomWeatherUpdate(Preferences.getCustomWeatherLocation(context));
        } else {
            fetchNewLocation();
        }
    }

    private void increaseForceRequestPeriod() {
        mForceRequestPeriodInMs = mForceRequestPeriodInMs * 2;

        if (mForceRequestPeriodInMs > Preferences.weatherRefreshIntervalInMs(mWeakContext.get())) {
            mForceRequestPeriodInMs = DEFAULT_FORCE_REQUEST_PERIOD_IN_MS;
        }
    }

    @SuppressLint("MissingPermission")
    private void fetchNewLocation() {
        if (hasMissingPermissions()) {
            Log.e(TAG, "Could not fetch location for missing permission");
            return;
        }

        LocationManagerCompat.getCurrentLocation(mLocationManager, LocationManager.GPS_PROVIDER, null,
                Executors.newFixedThreadPool(1), this::onNewLocationFetched);

        LocationManagerCompat.getCurrentLocation(mLocationManager, LocationManager.NETWORK_PROVIDER, null,
                Executors.newFixedThreadPool(1), this::onNewLocationFetched);
    }

    protected boolean hasMissingPermissions() {
        Context context = mWeakContext.get();

        return ActivityCompat.checkSelfPermission(context,
                permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context,
                        permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }

    protected void requestWeatherUpdate(@NonNull Location location) {
        Log.i(TAG, "Requesting weather info for location: " + location);
        Context context = mWeakContext.get();
        LineageWeatherManager weatherManager = LineageWeatherManager.getInstance(context);
        weatherManager.requestWeatherUpdate(location, (status, weatherInfo) -> notifyUi(context, weatherInfo, status));
    }

    protected void requestCustomWeatherUpdate(@Nullable WeatherLocation location) {
        Log.i(TAG, "Requesting weather info for location: " + location);
        Context context = mWeakContext.get();
        LineageWeatherManager weatherManager = LineageWeatherManager.getInstance(context);
        weatherManager.requestWeatherUpdate(location, (status, weatherInfo) -> notifyUi(context, weatherInfo, status));
    }

    private synchronized void onNewLocationFetched(@Nullable Location location) {
        if (location == null) {
            Log.w(TAG, "Could not fetch any location");
            return;
        }

        Log.i(TAG, "New location fetched:" + location);

        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            mGpsLocation = location;
        } else if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
            mNetworkLocation = location;
        }

        requestWeatherUpdate(getMostRecentLocation());
    }

    private void notifyUi(@NonNull Context context, @Nullable WeatherInfo weatherInfo, int status) {

        if (weatherInfo == null) {
            Log.i(TAG, "WeatherInfo is null. Status reported: " + status);
            return;
        }

        Log.i(TAG, "WeatherInfo=" + weatherInfo);
        long now = SystemClock.elapsedRealtime();
        Preferences.setCachedWeatherInfo(context, now, weatherInfo);
        Preferences.setLastWeatherUpdateTimestamp(context, now);
        Intent updateIntent = new Intent(WeatherUpdateService.ACTION_UPDATE_FINISHED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(updateIntent);
        mForceRequestPeriodInMs = DEFAULT_FORCE_REQUEST_PERIOD_IN_MS;
    }

    private Location getMostRecentLocation() {
        if (mNetworkLocation == null && mGpsLocation == null) {
            throw new IllegalStateException();
        }

        if (mGpsLocation == null) {
            return mNetworkLocation;
        }

        if (mNetworkLocation == null) {
            return mGpsLocation;
        }

        long gpsTime = mGpsLocation.getTime();
        long networkTime = mNetworkLocation.getTime();
        return gpsTime >= networkTime ? mGpsLocation : mNetworkLocation;
    }
}
