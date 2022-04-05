package foundation.e.blisslauncher.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import cyanogenmod.weather.WeatherInfo;
import cyanogenmod.weather.WeatherLocation;
import foundation.e.blisslauncher.core.utils.Constants;

public class Preferences {

    /**
     * Weather related keys and constants.
     */
    private static final String WEATHER_LOCATION_CITY_ID = "city_id";
    private static final String WEATHER_LOCATION_CITY_NAME = "city_name";
    private static final String WEATHER_LOCATION_STATE = "state";
    private static final String WEATHER_LOCATION_POSTAL_CODE = "postal_code";
    private static final String WEATHER_LOCATION_COUNTRY_ID = "country_id";
    private static final String WEATHER_LOCATION_COUNTRY_NAME = "country_name";

    private static final String WEATHER_INFO_CITY = "city";
    private static final String WEATHER_INFO_CONDITION_CODE = "condition_code";
    private static final String WEATHER_INFO_TEMPERATURE = "temperature";
    private static final String WEATHER_INFO_TEMPERATURE_UNIT = "temperature_unit";
    private static final String WEATHER_INFO_TIMESTAMP = "timestamp";
    private static final String WEATHER_INFO_HUMIDITY = "humidity";
    private static final String WEATHER_INFO_TODAYS_HIGH = "todays_high";
    private static final String WEATHER_INFO_TODAYS_LOW = "todays_low";
    private static final String WEATHER_INFO_WIND_SPEED = "wind_speed";
    private static final String WEATHER_INFO_WIND_SPEED_UNIT = "wind_speed_unit";
    private static final String WEATHER_INFO_WIND_SPEED_DIRECTION = "wind_speed_direction";
    private static final String WEATHER_INFO_FORECAST = "forecasts";

    private static final String DAY_FORECAST_CONDITION_CODE = "condition_code";
    private static final String DAY_FORECAST_LOW = "low";
    private static final String DAY_FORECAST_HIGH = "high";

    private static final String NOTIFICATION_ACCESS = "notification_access";

    private static final String ENABLE_LOCATION = "enable_location";

    private static final String ACTION_USAGE = "foundation.e.blisslauncher.ACTION_USAGE";

    private static final String CURRENT_MIGRATION_VERSION = "current_migration_version";

    private static final String ADDED_ECLOUD_WIDGET = "added_ecloud_widget";
    private static final String ADDED_PRIVACY_WIDGET = "added_privacy_widget";

    private Preferences() {
    }

    public static boolean isFirstWeatherUpdate(Context context) {
        return getPrefs(context).getBoolean(Constants.WEATHER_FIRST_UPDATE, true);
    }

    public static int weatherFontColor(Context context) {
        int color = Color.parseColor(getPrefs(context).getString(Constants.WEATHER_FONT_COLOR,
                Constants.DEFAULT_LIGHT_COLOR));
        return color;
    }

    public static String getWeatherIconSet(Context context) {
        return getPrefs(context).getString(Constants.WEATHER_ICONS, "color");
    }

    public static boolean useMetricUnits(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        boolean defValue = !(locale.equals(Locale.US)
                || locale.toString().equals("ms_MY") // Malaysia
                || locale.toString().equals("si_LK") // Sri Lanka
        );
        return getPrefs(context).getBoolean(Constants.WEATHER_USE_METRIC, defValue);
    }

    public static void setUseMetricUnits(Context context, boolean value) {
        getPrefs(context).edit().putBoolean(Constants.WEATHER_USE_METRIC, value).apply();
    }

    public static long weatherRefreshIntervalInMs(Context context) {
        String value = getPrefs(context).getString(Constants.WEATHER_REFRESH_INTERVAL, "60");
        return Long.parseLong(value) * 60L * 1000L;
    }

    public static boolean useCustomWeatherLocation(Context context) {
        return getPrefs(context).getBoolean(Constants.WEATHER_USE_CUSTOM_LOCATION, false);
    }

    public static void setUseCustomWeatherLocation(Context context, boolean value) {
        getPrefs(context).edit().putBoolean(Constants.WEATHER_USE_CUSTOM_LOCATION, value).apply();
    }

    public static String getCustomWeatherLocationCity(Context context) {
        return getPrefs(context).getString(Constants.WEATHER_CUSTOM_LOCATION_CITY, null);
    }

    public static void setCustomWeatherLocationCity(Context context, String city) {
        getPrefs(context).edit().putString(Constants.WEATHER_CUSTOM_LOCATION_CITY, city).apply();
    }

    public static boolean setCustomWeatherLocation(Context context,
            WeatherLocation weatherLocation) {
        if (weatherLocation == null) {
            getPrefs(context).edit()
                    .remove(Constants.WEATHER_CUSTOM_LOCATION).apply();
            return true;
        }
        try {
            JSONObject jsonObject = weatherLocationToJSON(weatherLocation);
            getPrefs(context).edit()
                    .putString(Constants.WEATHER_CUSTOM_LOCATION, jsonObject.toString()).apply();
            return true;
        } catch (JSONException e) {
            // We're here because weatherLocationToJSON() or jsonObject.toString() failed.
            // Either way, it means the pref was not updated
            return false;
        }
    }

    public static WeatherLocation getCustomWeatherLocation(Context context) {
        String weatherLocation = getPrefs(context)
                .getString(Constants.WEATHER_CUSTOM_LOCATION, null);

        if (weatherLocation == null) {
            return null;
        }

        try {
            JSONObject jsonObject = new JSONObject(weatherLocation);
            return JSONToWeatherLocation(jsonObject);
        } catch (JSONException e) {
            return null;
        }
    }

    private static WeatherLocation JSONToWeatherLocation(JSONObject jsonObject)
            throws JSONException {
        String cityId;
        String cityName;
        String state;
        String postalCode;
        String countryId;
        String countryName;

        cityId = jsonObject.getString(WEATHER_LOCATION_CITY_ID);
        cityName = jsonObject.getString(WEATHER_LOCATION_CITY_NAME);
        state = jsonObject.getString(WEATHER_LOCATION_STATE);
        postalCode = jsonObject.getString(WEATHER_LOCATION_POSTAL_CODE);
        countryId = jsonObject.getString(WEATHER_LOCATION_COUNTRY_ID);
        countryName = jsonObject.getString(WEATHER_LOCATION_COUNTRY_NAME);

        //We need at least city id and city name to build a WeatherLocation
        if (cityId == null && cityName == null) {
            return null;
        }

        WeatherLocation.Builder location = new WeatherLocation.Builder(cityId, cityName);
        if (countryId != null) location.setCountryId(countryId);
        if (countryName != null) location.setCountry(countryName);
        if (state != null) location.setState(state);
        if (postalCode != null) location.setPostalCode(postalCode);

        return location.build();
    }

    private static JSONObject weatherLocationToJSON(WeatherLocation location) throws JSONException {
        return new JSONObject()
                .put(WEATHER_LOCATION_CITY_ID, location.getCityId())
                .put(WEATHER_LOCATION_CITY_NAME, location.getCity())
                .put(WEATHER_LOCATION_STATE, location.getState())
                .put(WEATHER_LOCATION_POSTAL_CODE, location.getPostalCode())
                .put(WEATHER_LOCATION_COUNTRY_ID, location.getCountryId())
                .put(WEATHER_LOCATION_COUNTRY_NAME, location.getCountry());
    }

    public static void setCachedWeatherInfo(Context context, long timestamp, WeatherInfo info) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putLong(Constants.WEATHER_LAST_UPDATE, timestamp);
        if (info != null) {
            // We now have valid weather data to display
            JSONObject jsonObject = new JSONObject();
            boolean serialized = false;
            try {
                //These members always return a value that can be parsed
                jsonObject
                        .put(WEATHER_INFO_CITY, info.getCity())
                        .put(WEATHER_INFO_CONDITION_CODE, info.getConditionCode())
                        .put(WEATHER_INFO_TEMPERATURE, info.getTemperature())
                        .put(WEATHER_INFO_TEMPERATURE_UNIT, info.getTemperatureUnit())
                        .put(WEATHER_INFO_TIMESTAMP, info.getTimestamp());

                // Handle special cases. JSONObject.put(key, double) does not allow
                // Double.NaN, so we store it as a string. JSONObject.getDouble() will parse the
                // "NaN" string and return Double.NaN, which is what we want
                double humidity = info.getHumidity();
                jsonObject.put(WEATHER_INFO_HUMIDITY, Double.isNaN(humidity) ? "NaN" : humidity);

                double todaysHigh = info.getTodaysHigh();
                jsonObject.put(WEATHER_INFO_TODAYS_HIGH, Double.isNaN(todaysHigh)
                        ? "NaN" : todaysHigh);

                double todaysLow = info.getTodaysLow();
                jsonObject.put(WEATHER_INFO_TODAYS_LOW, Double.isNaN(todaysLow)
                        ? "NaN" : todaysLow);

                double windSpeed = info.getWindSpeed();
                double windDirection = info.getWindDirection();
                jsonObject.put(WEATHER_INFO_WIND_SPEED, Double.isNaN(windSpeed) ? "NaN" : windSpeed)
                        .put(WEATHER_INFO_WIND_SPEED_UNIT, info.getWindSpeedUnit())
                        .put(WEATHER_INFO_WIND_SPEED_DIRECTION, Double.isNaN(windDirection)
                                ? "NaN" : windDirection);

                JSONArray forecastArray = new JSONArray();
                for (WeatherInfo.DayForecast forecast : info.getForecasts()) {
                    JSONObject jsonForecast = new JSONObject()
                            .put(DAY_FORECAST_CONDITION_CODE, forecast.getConditionCode());

                    double low = forecast.getLow();
                    jsonForecast.put(DAY_FORECAST_LOW, Double.isNaN(low) ? "NaN" : low);
                    double high = forecast.getHigh();
                    jsonForecast.put(DAY_FORECAST_HIGH, Double.isNaN(high) ? "NaN" : high);
                    forecastArray.put(jsonForecast);
                }
                jsonObject.put(WEATHER_INFO_FORECAST, forecastArray);
                serialized = true;
            } catch (JSONException e) {
                // We're here because something went wrong while creating the JSON object.
                // The code below will check for success and proceed accordingly
            }
            if (serialized) {
                editor.putString(Constants.WEATHER_DATA, jsonObject.toString());
                editor.putBoolean(Constants.WEATHER_FIRST_UPDATE, false);
            }
        } else {
            editor.remove(Constants.WEATHER_DATA);
        }
        editor.apply();
    }

    public static long lastWeatherUpdateTimestamp(Context context) {
        return getPrefs(context).getLong(Constants.WEATHER_LAST_UPDATE, 0);
    }

    public static void setLastWeatherUpdateTimestamp(Context context, long timestamp) {
        getPrefs(context).edit().putLong(Constants.WEATHER_LAST_UPDATE, timestamp).apply();
    }

    public static WeatherInfo getCachedWeatherInfo(Context context) {
        final String cachedInfo = getPrefs(context).getString(Constants.WEATHER_DATA, null);

        if (cachedInfo == null) return null;

        String city;
        int conditionCode;
        double temperature;
        int tempUnit;
        double humidity;
        double windSpeed;
        double windDirection;
        double todaysHigh;
        double todaysLow;
        int windSpeedUnit;
        long timestamp;
        ArrayList<WeatherInfo.DayForecast> forecastList = new ArrayList<>();

        try {
            JSONObject cached = new JSONObject(cachedInfo);
            city = cached.getString(WEATHER_INFO_CITY);
            conditionCode = cached.getInt(WEATHER_INFO_CONDITION_CODE);
            temperature = cached.getDouble(WEATHER_INFO_TEMPERATURE);
            tempUnit = cached.getInt(WEATHER_INFO_TEMPERATURE_UNIT);
            humidity = cached.getDouble(WEATHER_INFO_HUMIDITY);
            windSpeed = cached.getDouble(WEATHER_INFO_WIND_SPEED);
            windDirection = cached.getDouble(WEATHER_INFO_WIND_SPEED_DIRECTION);
            windSpeedUnit = cached.getInt(WEATHER_INFO_WIND_SPEED_UNIT);
            timestamp = cached.getLong(WEATHER_INFO_TIMESTAMP);
            todaysHigh = cached.getDouble(WEATHER_INFO_TODAYS_HIGH);
            todaysLow = cached.getDouble(WEATHER_INFO_TODAYS_LOW);
            JSONArray forecasts = cached.getJSONArray(WEATHER_INFO_FORECAST);
            for (int indx = 0; indx < forecasts.length(); indx++) {
                JSONObject forecast = forecasts.getJSONObject(indx);
                double low;
                double high;
                int code;
                low = forecast.getDouble(DAY_FORECAST_LOW);
                high = forecast.getDouble(DAY_FORECAST_HIGH);
                code = forecast.getInt(DAY_FORECAST_CONDITION_CODE);
                WeatherInfo.DayForecast.Builder f = new WeatherInfo.DayForecast.Builder(code);
                if (!Double.isNaN(low)) f.setLow(low);
                if (!Double.isNaN(high)) f.setHigh(high);
                forecastList.add(f.build());
            }
            WeatherInfo.Builder weatherInfo = new WeatherInfo.Builder(city, temperature, tempUnit)
                    .setWeatherCondition(conditionCode)
                    .setTimestamp(timestamp);

            if (!Double.isNaN(humidity)) weatherInfo.setHumidity(humidity);
            if (!Double.isNaN(windSpeed) && !Double.isNaN(windDirection)) {
                weatherInfo.setWind(windSpeed, windDirection, windSpeedUnit);
            }
            if (forecastList.size() > 0) weatherInfo.setForecast(forecastList);
            if (!Double.isNaN(todaysHigh)) weatherInfo.setTodaysHigh(todaysHigh);
            if (!Double.isNaN(todaysLow)) weatherInfo.setTodaysLow(todaysLow);
            return weatherInfo.build();
        } catch (JSONException e) {
        }
        return null;
    }

    public static void setWeatherSource(Context context, String source) {
        getPrefs(context).edit().putString(Constants.WEATHER_SOURCE, source).apply();
    }

    public static String getWeatherSource(Context context) {
        return getPrefs(context).getString(Constants.WEATHER_SOURCE, null);
    }

    public static void setUserCreationTime(Context context, String key) {
        getPrefs(context).edit().putLong(key, System.currentTimeMillis()).apply();
    }

    public static boolean shouldOpenUsageAccess(Context context) {
        return getPrefs(context).getBoolean(ACTION_USAGE, true);
    }

    public static void setNotOpenUsageAccess(Context context) {
        getPrefs(context).edit().putBoolean(ACTION_USAGE, false).apply();
    }

    public static boolean shouldAskForNotificationAccess(Context context) {
        return getPrefs(context).getBoolean(NOTIFICATION_ACCESS, true);
    }

    public static void setNotToAskForNotificationAccess(Context context) {
        getPrefs(context).edit().putBoolean(NOTIFICATION_ACCESS, false).apply();
    }

    public static int getCurrentMigrationVersion(Context context){
        return getPrefs(context).getInt(CURRENT_MIGRATION_VERSION, 0);
    }

    public static void setCurrentMigrationVersion(Context context, int version){
        getPrefs(context).edit().putInt(CURRENT_MIGRATION_VERSION, version).apply();
    }

    public static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void setEnableLocation(Context context) {
        getPrefs(context).edit().putBoolean(ENABLE_LOCATION, true).apply();
    }

    public static boolean getEnableLocation(
            Context context) {
        return getPrefs(context).getBoolean(ENABLE_LOCATION, false);
    }

    public static void setAddedEcloudWidget(Context context) {
        getPrefs(context).edit().putBoolean(ADDED_ECLOUD_WIDGET, true).apply();
    }

    public static boolean getAddedEcloudWidget(Context context) {
        return getPrefs(context).getBoolean(ADDED_ECLOUD_WIDGET, false);
    }

    public static void setAddedPrivacyWidget(Context context) {
        getPrefs(context).edit().putBoolean(ADDED_PRIVACY_WIDGET, true).apply();
    }

    public static boolean getAddedPrivacyWidget(Context context) {
        return getPrefs(context).getBoolean(ADDED_PRIVACY_WIDGET, false);
    }
}
