package foundation.e.blisslauncher.features.weather.worker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.location.LocationManagerCompat;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkerParameters;

import java.util.concurrent.Executors;

import foundation.e.blisslauncher.core.Preferences;

public class ForceWeatherRequestWorker extends WeatherRequestWorker {

    private static final String TAG = "ForceWeatherRequestWorker";

    private Location mGpsLocation;
    private Location mNetworkLocation;

    public ForceWeatherRequestWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    public static void start(Context context) {
        OneTimeWorkRequest weatherUpdateWorkRequest = new OneTimeWorkRequest.Builder(ForceWeatherRequestWorker.class)
                .build();

        WorkManager.getInstance(context).enqueueUniqueWork(TAG, ExistingWorkPolicy.KEEP, weatherUpdateWorkRequest);
    }

    @NonNull
    @Override
    public Result doWork() {

        Context context = getApplicationContext();

        if (Preferences.useCustomWeatherLocation(context)) {
            requestCustomWeatherUpdate(context, Preferences.getCustomWeatherLocation(context));
        } else {
            fetchNewLocation(context);
        }

        return Result.success();
    }

    @SuppressLint("MissingPermission")
    private void fetchNewLocation(Context context) {
        if (hasMissingPermissions(context)) {
            Log.e(TAG, "Could not fetch location for missing permission");
            return;
        }

        LocationManagerCompat.getCurrentLocation(locationManager, LocationManager.GPS_PROVIDER, null,
                Executors.newFixedThreadPool(1), this::onNewLocationFetched);

        LocationManagerCompat.getCurrentLocation(locationManager, LocationManager.NETWORK_PROVIDER, null,
                Executors.newFixedThreadPool(1), this::onNewLocationFetched);
    }

    private synchronized void onNewLocationFetched(Location location) {
        if (location == null) {
            return;
        }

        Log.i(TAG, "New location fetched:" + location);

        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            mGpsLocation = location;
        } else if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
            mNetworkLocation = location;
            requestWeatherUpdate(getApplicationContext(), getMostRecentLocation());
        }
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
