package foundation.e.blisslauncher.features.weather;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Locale;

import foundation.e.blisslauncher.R;
import foundation.e.blisslauncher.core.Preferences;
import foundation.e.blisslauncher.features.weather.location.FusedLocationFetcher;
import foundation.e.blisslauncher.features.weather.location.LocationFetcher;
import foundation.e.blisslauncher.features.weather.location.NetworkGpsLocationFetcher;
import lineageos.weather.LineageWeatherManager;
import lineageos.weather.WeatherInfo;
import lineageos.weather.WeatherLocation;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;

public class WeatherUpdater {

    private static final String TAG = "WeatherUpdater";
    private static final long DEFAULT_FORCE_REQUEST_PERIOD_IN_MS = 10L * 1000L;

    private final WeakReference<Context> mWeakContext;
    private long mForceRequestPeriodInMs = DEFAULT_FORCE_REQUEST_PERIOD_IN_MS;

    private long mLastWeatherUpdateTimeStamp = 0;
    private long mForceRequestLastTry = 0;

    private static WeatherUpdater mInstance = null;

    public static WeatherUpdater getInstance(@NonNull Context context) {
        if (mInstance == null) {
            mInstance = new WeatherUpdater(context);
        }

        return mInstance;
    }

    private WeatherUpdater(@NonNull Context context) {
        mWeakContext = new WeakReference<>(context);
    }

    public void checkWeatherRequest() {
        Context context = mWeakContext.get();
        long refreshPeriod = Preferences.weatherRefreshIntervalInMs(context);
        long elapsedTime = Math.abs(SystemClock.elapsedRealtime() - mLastWeatherUpdateTimeStamp);

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

        long systemTime = SystemClock.elapsedRealtime();
        long elapsedTime = Math.abs(systemTime - mForceRequestLastTry);
        boolean isRequestAllowed = elapsedTime >= mForceRequestPeriodInMs;

        if (isRequestAllowed) {
            mForceRequestLastTry = systemTime;
        }

        return isRequestAllowed;
    }

    private void updateWeather() {
        Timber.tag(TAG).i("Updating weather");
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
            Timber.tag(TAG).e("Could not fetch location for missing permission");
            return;
        }

        LocationFetcher locationFetcher = VERSION.SDK_INT >= VERSION_CODES.S
                ? new FusedLocationFetcher(mWeakContext.get(), this::onNewLocationFetched)
                : new NetworkGpsLocationFetcher(mWeakContext.get(), this::onNewLocationFetched);

        try {
            locationFetcher.fetchLocation();
        } catch (Exception exception) {
            Timber.tag(TAG).w(exception, "Could not fetch location");
        }
    }

    protected boolean hasMissingPermissions() {
        Context context = mWeakContext.get();

        return ActivityCompat.checkSelfPermission(context,
                permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context,
                        permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }

    protected void requestWeatherUpdate(@NonNull Location location) {
        Timber.tag(TAG).i("Requesting weather info for location: %s", location);
        Context context = mWeakContext.get();
        LineageWeatherManager weatherManager = LineageWeatherManager.getInstance(context);
        weatherManager.requestWeatherUpdate(location, (status, weatherInfo) -> notifyUi(context, weatherInfo, status));
    }

    protected void requestCustomWeatherUpdate(@Nullable WeatherLocation location) {
        if (location == null) {
            Timber.tag(TAG).w("Custom location is null. Cannot request weather");
            return;
        }

        Timber.tag(TAG).i("Requesting weather info for location: %s", location);
        Context context = mWeakContext.get();
        LineageWeatherManager weatherManager = LineageWeatherManager.getInstance(context);
        weatherManager.requestWeatherUpdate(location, (status, weatherInfo) -> notifyUi(context, weatherInfo, status));
    }

    private synchronized void onNewLocationFetched(@Nullable Location location) {
        if (location == null) {
            Timber.tag(TAG).i("Could not fetch any location");
            return;
        }

        Timber.tag(TAG).i("New location fetched:%s", location);

        requestWeatherUpdate(location);

        if (!Preferences.useCustomWeatherLocation(mWeakContext.get())) {
            reverseGeocodeLocation(location);
        } else {
            Timber.tag(TAG).w("Do not reverse geocode location. User is using a custom location.");
        }
    }

    private void notifyUi(@NonNull Context context, @Nullable WeatherInfo weatherInfo, int status) {

        if (weatherInfo == null) {
            Timber.tag(TAG).i("WeatherInfo is null. Status reported: %s", status);
            return;
        }

        Timber.tag(TAG).i("WeatherInfo=%s", weatherInfo);

        long now = SystemClock.elapsedRealtime();
        Preferences.setCachedWeatherInfo(context, now, weatherInfo);
        mLastWeatherUpdateTimeStamp = now;
        Intent updateIntent = new Intent(WeatherUpdateService.ACTION_UPDATE_FINISHED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(updateIntent);
        mForceRequestPeriodInMs = DEFAULT_FORCE_REQUEST_PERIOD_IN_MS;
    }

    private void reverseGeocodeLocation(@NonNull Location location) {
        Timber.tag(TAG).i("Reverse geocoding location %s", location);

        final String url = "https://api.openweathermap.org/geo/1.0/reverse?lat=" + location.getLatitude() + "&lon="
                + location.getLongitude() + "&limit=1&appid=" + mWeakContext.get().getString(R.string.default_key);

        final OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder().url(url).build();
        okHttpClient.newCall(request).enqueue(mReverseGeocodeCallback);
    }

    private void onReverseGeocoded(@NonNull Response response) {
        final ResponseBody body = response.body();
        if (body == null) {
            Timber.tag(TAG).w("Reverse geocoding response is empty");
            return;
        }

        JsonObject locales;
        JsonObject root;
        String defaultCityName;
        try {
            final String json = body.string();
            final JsonArray array = new JsonParser().parse(json).getAsJsonArray();

            root = array.get(0).getAsJsonObject();
            locales = root.getAsJsonObject("local_names");
            defaultCityName = root.get("name").getAsString();
        } catch (IOException | IllegalStateException | JsonSyntaxException exception) {
            Timber.tag(TAG).e(exception, "Exception caught");
            return;
        }

        if (defaultCityName == null) {
            Timber.tag(TAG).w("Could not get default city name");
            return;
        }

        if (locales == null) {
            Timber.tag(TAG).i("Could not get locales. Fallbacking to default city name %s", defaultCityName);

            notifyUi(defaultCityName);
            return;
        }

        String countryCode = Locale.getDefault().getCountry().toLowerCase(Locale.ROOT);
        final JsonElement jsonElement = locales.get(countryCode);
        if (jsonElement == null) {
            Timber.tag(TAG).i("Could not get city name in country code: %s. Fallbacking to default city name %s",
                    countryCode, defaultCityName);

            notifyUi(defaultCityName);
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
            Timber.tag(TAG).e(e, "Could not reverse geocode location");
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            onReverseGeocoded(response);
        }
    };
}
