package foundation.e.blisslauncher.features.weather;

import android.content.Context;
import android.content.res.Resources;
import cyanogenmod.app.CMContextConstants;
import cyanogenmod.providers.WeatherContract;
import foundation.e.blisslauncher.R;
import java.text.DecimalFormat;

public final class WeatherUtils {

    private static final DecimalFormat sNoDigitsFormat = new DecimalFormat("0");

    // In doubt? See https://en.wikipedia.org/wiki/Points_of_the_compass
    private static final double DIRECTION_NORTH = 23d;
    private static final double DIRECTION_NORTH_EAST = 68d;
    private static final double DIRECTION_EAST = 113d;
    private static final double DIRECTION_SOUTH_EAST = 158d;
    private static final double DIRECTION_SOUTH = 203d;
    private static final double DIRECTION_SOUTH_WEST = 248d;
    private static final double DIRECTION_WEST = 293d;
    private static final double DIRECTION_NORTH_WEST = 338d;

    private static boolean weatherServiceFeatureCached;
    private static boolean weatherServiceAvailable;

    /**
     * Returns a localized string of the wind direction
     *
     * @param context
     *            Application context to access resources
     * @param windDirection
     *            The wind direction in degrees
     * @return The wind direction in string format
     */
    public static String resolveWindDirection(Context context, double windDirection) {
        int resId;

        if (windDirection < 0) {
            resId = R.string.unknown;
        } else if (windDirection < DIRECTION_NORTH) {
            resId = R.string.weather_N;
        } else if (windDirection < DIRECTION_NORTH_EAST) {
            resId = R.string.weather_NE;
        } else if (windDirection < DIRECTION_EAST) {
            resId = R.string.weather_E;
        } else if (windDirection < DIRECTION_SOUTH_EAST) {
            resId = R.string.weather_SE;
        } else if (windDirection < DIRECTION_SOUTH) {
            resId = R.string.weather_S;
        } else if (windDirection < DIRECTION_SOUTH_WEST) {
            resId = R.string.weather_SW;
        } else if (windDirection < DIRECTION_WEST) {
            resId = R.string.weather_W;
        } else if (windDirection < DIRECTION_NORTH_WEST) {
            resId = R.string.weather_NW;
        } else {
            resId = R.string.weather_N;
        }

        return context.getString(resId);
    }

    /**
     * Returns the resource name associated to the supplied weather condition code
     *
     * @param context
     *            Application context to access resources
     * @param conditionCode
     *            The weather condition code
     * @return The resource name if a valid condition code is passed, empty string
     *         otherwise
     */
    public static String resolveWeatherCondition(Context context, int conditionCode) {
        final Resources res = context.getResources();
        final int resId = res.getIdentifier(
                "weather_" + WeatherUtils.addOffsetToConditionCodeFromWeatherContract(conditionCode), "string",
                context.getPackageName());
        if (resId != 0) {
            return res.getString(resId);
        }
        return "";
    }

    private static String getFormattedValue(double value, String unit) {
        if (Double.isNaN(value)) {
            return "-";
        }
        String formatted = sNoDigitsFormat.format(value);
        if (formatted.equals("-0")) {
            formatted = "0";
        }
        return formatted + unit;
    }

    /**
     * Returns a string with the format xx% (where xx is the humidity value
     * provided)
     *
     * @param humidity
     *            The humidity value
     * @return The formatted string if a valid value is provided, "-" otherwise.
     *         Decimals are removed
     */
    public static String formatHumidity(double humidity) {
        return getFormattedValue(humidity, "%");
    }

    /**
     * Returns a localized string of the speed and speed unit
     *
     * @param context
     *            Application context to access resources
     * @param windSpeed
     *            The wind speed
     * @param windSpeedUnit
     *            The speed unit. See
     *            {@link lineageos.providers.WeatherContract.WeatherColumns.WindSpeedUnit}
     * @return The formatted string if a valid speed and speed unit a provided.
     *         {@link R.string#unknown} otherwise
     */
    public static String formatWindSpeed(Context context, double windSpeed, int windSpeedUnit) {
        if (windSpeed < 0) {
            return context.getString(R.string.unknown);
        }

        String localizedSpeedUnit;
        switch (windSpeedUnit) {
            case WeatherContract.WeatherColumns.WindSpeedUnit.MPH :
                localizedSpeedUnit = context.getString(R.string.weather_mph);
                break;
            case WeatherContract.WeatherColumns.WindSpeedUnit.KPH :
                localizedSpeedUnit = context.getString(R.string.weather_kph);
                break;
            default :
                return context.getString(R.string.unknown);
        }
        return getFormattedValue(windSpeed, localizedSpeedUnit);
    }

    /**
     * Helper method to convert miles to kilometers
     *
     * @param miles
     *            The value in miles
     * @return The value in kilometers
     */
    public static double milesToKilometers(double miles) {
        return miles * 1.609344d;
    }

    /**
     * Helper method to convert kilometers to miles
     *
     * @param km
     *            The value in kilometers
     * @return The value in miles
     */
    public static double kilometersToMiles(double km) {
        return km * 0.6214d;
    }

    /**
     * Adds an offset to the condition code reported by the active weather service
     * provider.
     *
     * @param conditionCode
     *            The condition code from the Weather API
     * @return A condition code that correctly maps to our resource IDs
     */
    public static int addOffsetToConditionCodeFromWeatherContract(int conditionCode) {
        if (conditionCode <= WeatherContract.WeatherColumns.WeatherCode.SHOWERS) {
            return conditionCode;
        } else if (conditionCode <= WeatherContract.WeatherColumns.WeatherCode.SCATTERED_THUNDERSTORMS) {
            return conditionCode + 1;
        } else if (conditionCode <= WeatherContract.WeatherColumns.WeatherCode.SCATTERED_SNOW_SHOWERS) {
            return conditionCode + 2;
        } else if (conditionCode <= WeatherContract.WeatherColumns.WeatherCode.ISOLATED_THUNDERSHOWERS) {
            return conditionCode + 3;
        } else {
            return WeatherContract.WeatherColumns.WeatherCode.NOT_AVAILABLE;
        }
    }

    /**
     * Checks if the Lineage Weather service is available in this device
     *
     * @param context
     *            Context to be used
     * @return true if service is available, false otherwise
     */
    public static boolean isWeatherServiceAvailable(Context context) {
        if (!weatherServiceFeatureCached) {
            weatherServiceAvailable = context.getPackageManager()
                    .hasSystemFeature(CMContextConstants.Features.WEATHER_SERVICES);
            weatherServiceFeatureCached = true;
        }
        return weatherServiceAvailable;
    }
}
