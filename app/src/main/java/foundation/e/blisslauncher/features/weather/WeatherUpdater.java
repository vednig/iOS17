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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.concurrent.Executors;

import foundation.e.blisslauncher.R;
import foundation.e.blisslauncher.core.Preferences;
import lineageos.weather.LineageWeatherManager;
import lineageos.weather.WeatherInfo;
import lineageos.weather.WeatherLocation;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

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

        if (!Preferences.useCustomWeatherLocation(mWeakContext.get())) {
            reverseGeocodeLocation(getMostRecentLocation());
        }
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

    private void reverseGeocodeLocation(@NonNull Location location) {
        Log.i(TAG, "Reverse geocoding location " + location);

        final String url = "https://api.openweathermap.org/geo/1.0/reverse?lat=" + location.getLatitude() + "&lon="
                + location.getLongitude() + "&limit=1&appid=" + mWeakContext.get().getString(R.string.default_key);

        final OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder().url(url).build();
        okHttpClient.newCall(request).enqueue(mReverseGeocodeCallback);
    }

    private void onReverseGeocoded(@NonNull Response response) {
        final ResponseBody body = response.body();
        if (body == null) {
            Log.w(TAG, "Reverse geocoding response is empty");
            return;
        }

        JsonObject locales;
        try {
            final String json = body.string();
            final JsonArray array = new JsonParser().parse(json).getAsJsonArray();
            locales = array.get(0).getAsJsonObject().getAsJsonObject("local_names");
        } catch (IOException | IllegalStateException | JsonSyntaxException exception) {
            Log.e(TAG, "Exception caught", exception);
            return;
        }

        if (locales == null) {
            Log.e(TAG, "Could not get locales");
            return;
        }

        String countryCode = Locale.getDefault().getCountry().toLowerCase(Locale.ROOT);
        if (!locales.has(countryCode)) {
            final JsonElement jsonElement = locales.get("en");
            if (jsonElement == null) {
                return;
            }

            countryCode = jsonElement.getAsString();
        }

        final JsonElement jsonElement = locales.get(countryCode);
        if (jsonElement == null) {
            return;
        }

        final String city = jsonElement.getAsString();
        notifyUi(city);
    }

    private void notifyUi(@NonNull String city) {
        Context context = mWeakContext.get();
        Preferences.setCachedCity(context, city);
        final Intent intent = new Intent(WeatherUpdateService.ACTION_UPDATE_CITY_FINISHED);
        intent.putExtra(WeatherUpdateService.EXTRA_UPDATE_CITY_KEY, city);
        LocalBroadcastManager.getInstance(mWeakContext.get()).sendBroadcast(intent);
    }

    private final Callback mReverseGeocodeCallback = new Callback() {
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            Log.e(TAG, "Could not reverse geocode location", e);
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            onReverseGeocoded(response);
        }
    };
}
