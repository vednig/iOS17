package foundation.e.blisslauncher.features.weather.worker;

import android.Manifest.permission;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import foundation.e.blisslauncher.core.Preferences;
import foundation.e.blisslauncher.features.weather.WeatherUpdateService;
import lineageos.weather.LineageWeatherManager;
import lineageos.weather.WeatherInfo;
import lineageos.weather.WeatherLocation;

public abstract class WeatherRequestWorker extends Worker {

    private static final String TAG = "WeatherRequestWorker";

    protected final LocationManager locationManager;

    public WeatherRequestWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @NonNull
    public abstract Result doWork();

    protected static void requestWeatherUpdate(Context context, Location location) {
        Log.i(TAG, "Requesting weather info for location: " + location);
        LineageWeatherManager weatherManager = LineageWeatherManager.getInstance(context);
        weatherManager.requestWeatherUpdate(location, (status, weatherInfo) -> notifyUi(context, weatherInfo, status));
    }

    protected static void requestCustomWeatherUpdate(Context context, WeatherLocation location) {
        Log.i(TAG, "Requesting weather info for location: " + location);
        LineageWeatherManager weatherManager = LineageWeatherManager.getInstance(context);
        weatherManager.requestWeatherUpdate(location, (status, weatherInfo) -> notifyUi(context, weatherInfo, status));
    }

    protected static void notifyUi(@NonNull Context context, @Nullable WeatherInfo weatherInfo, int status) {
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
    }

    protected static boolean hasMissingPermissions(Context context) {
        return ActivityCompat.checkSelfPermission(context,
                permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context,
                        permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }
}
