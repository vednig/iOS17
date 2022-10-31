package foundation.e.blisslauncher.features.weather.worker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkerParameters;

import foundation.e.blisslauncher.core.Preferences;

public class OneShotWeatherRequestWorker extends WeatherRequestWorker {

    public static final String TAG = "OneShotWeatherRequestWorker";

    public OneShotWeatherRequestWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    public static void start(Context context) {
        OneTimeWorkRequest weatherUpdateWorkRequest = new OneTimeWorkRequest.Builder(OneShotWeatherRequestWorker.class)
                .build();

        WorkManager.getInstance(context).enqueueUniqueWork(TAG, ExistingWorkPolicy.KEEP, weatherUpdateWorkRequest);
    }

    @NonNull
    @SuppressLint("MissingPermission")
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        if (isWeatherRequestAllowed(context)) {
            ForceWeatherRequestWorker.start(context);
        }

        return Result.success();
    }

    private static Boolean isWeatherRequestAllowed(Context context) {
        long refreshPeriod = Preferences.weatherRefreshIntervalInMs(context);
        long elapsedTime = SystemClock.elapsedRealtime() - Preferences.lastWeatherUpdateTimestamp(context);

        return refreshPeriod != 0 && elapsedTime >= refreshPeriod;
    }
}
