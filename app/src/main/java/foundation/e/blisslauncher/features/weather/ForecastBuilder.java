package foundation.e.blisslauncher.features.weather;

import static lineageos.providers.WeatherContract.WeatherColumns.TempUnit.CELSIUS;
import static lineageos.providers.WeatherContract.WeatherColumns.TempUnit.FAHRENHEIT;
import static lineageos.providers.WeatherContract.WeatherColumns.WindSpeedUnit.KPH;
import static lineageos.providers.WeatherContract.WeatherColumns.WindSpeedUnit.MPH;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import foundation.e.blisslauncher.R;
import foundation.e.blisslauncher.core.Preferences;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import lineageos.weather.WeatherInfo;
import lineageos.weather.util.WeatherUtils;

public class ForecastBuilder {

    private static final String TAG = "ForecastBuilder";

    /**
     * This method is used to build the forecast panel
     *
     * @param context
     *            Context to be used
     * @param weatherPanel
     *            a view that will contain the forecast
     * @param w
     *            the Weather info object that contains the forecast data
     */
    @SuppressLint("InflateParams")
    public static void buildLargePanel(Context context, View weatherPanel, WeatherInfo w) {
        if (weatherPanel == null) {
            Log.d(TAG, "Invalid view passed");
            return;
        }

        int color = Preferences.weatherFontColor(context);
        final boolean useMetric = Preferences.useMetricUnits(context);
        double temp = w.getTemperature();
        double todayLow = w.getTodaysLow();
        double todayHigh = w.getTodaysHigh();

        int tempUnit = w.getTemperatureUnit();
        if (tempUnit == FAHRENHEIT && useMetric) {
            temp = WeatherUtils.fahrenheitToCelsius(temp);
            todayLow = WeatherUtils.fahrenheitToCelsius(todayLow);
            todayHigh = WeatherUtils.fahrenheitToCelsius(todayHigh);
            tempUnit = CELSIUS;
        } else if (tempUnit == CELSIUS && !useMetric) {
            temp = WeatherUtils.celsiusToFahrenheit(temp);
            todayLow = WeatherUtils.celsiusToFahrenheit(todayLow);
            todayHigh = WeatherUtils.celsiusToFahrenheit(todayHigh);
            tempUnit = FAHRENHEIT;
        }

        // Set the current conditions
        // Weather Image
        ImageView weatherImage = weatherPanel.findViewById(R.id.weather_image);
        String iconsSet = Preferences.getWeatherIconSet(context);
        weatherImage.setImageBitmap(WeatherIconUtils.getWeatherIconBitmap(context, iconsSet, color,
                w.getConditionCode(), WeatherIconUtils.getNextHigherDensity(context)));

        // City
        TextView textCity = weatherPanel.findViewById(R.id.weather_city);
        String city;

        if (Preferences.useCustomWeatherLocation(context)) {
            city = w.getCity();
        } else {
            city = Preferences.getCachedCity(context, w.getCity());
        }

        textCity.setText(city);

        // Weather Condition
        TextView weatherCondition = weatherPanel.findViewById(R.id.weather_condition);
        weatherCondition.setText(foundation.e.blisslauncher.features.weather.WeatherUtils
                .resolveWeatherCondition(context, w.getConditionCode()));

        // Weather Temps
        TextView weatherTemp = weatherPanel.findViewById(R.id.weather_current_temperature);
        weatherTemp.setText(WeatherUtils.formatTemperature(temp, tempUnit));

        // Weather Temps Panel additional networkItems
        final String low = WeatherUtils.formatTemperature(todayLow, tempUnit);
        final String high = WeatherUtils.formatTemperature(todayHigh, tempUnit);
        TextView weatherLowHigh = weatherPanel.findViewById(R.id.weather_low_high);
        weatherLowHigh.setText(String.format("%s / %s", low, high));

        double windSpeed = w.getWindSpeed();
        int windSpeedUnit = w.getWindSpeedUnit();
        if (windSpeedUnit == MPH && useMetric) {
            windSpeedUnit = KPH;
            windSpeed = foundation.e.blisslauncher.features.weather.WeatherUtils.milesToKilometers(windSpeed);
        } else if (windSpeedUnit == KPH && !useMetric) {
            windSpeedUnit = MPH;
            windSpeed = foundation.e.blisslauncher.features.weather.WeatherUtils.kilometersToMiles(windSpeed);
        }

        // Humidity and Wind
        TextView weatherHumWind = weatherPanel.findViewById(R.id.weather_chance_rain);
        weatherHumWind.setText(String.format("%s, %s %s",
                foundation.e.blisslauncher.features.weather.WeatherUtils.formatHumidity(w.getHumidity()),
                foundation.e.blisslauncher.features.weather.WeatherUtils.formatWindSpeed(context, windSpeed,
                        windSpeedUnit),
                foundation.e.blisslauncher.features.weather.WeatherUtils.resolveWindDirection(context,
                        w.getWindDirection())));
        LinearLayout forecastView = weatherPanel.findViewById(R.id.forecast_view);
        buildSmallPanel(context, forecastView, w);
    }

    /**
     * This method is used to build the small, horizontal forecasts panel
     *
     * @param context
     *            Context to be used
     * @param smallPanel
     *            a horizontal {@link LinearLayout} that will contain the forecasts
     * @param w
     *            the Weather info object that contains the forecast data
     */
    @SuppressLint("InflateParams")
    private static void buildSmallPanel(Context context, LinearLayout smallPanel, WeatherInfo w) {
        if (smallPanel == null) {
            Log.d(TAG, "Invalid view passed");
            return;
        }

        // Get things ready
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int color = Preferences.weatherFontColor(context);
        final boolean useMetric = Preferences.useMetricUnits(context);

        smallPanel.removeAllViews();
        List<WeatherInfo.DayForecast> forecasts = w.getForecasts();
        if (forecasts.size() <= 1) {
            smallPanel.setVisibility(View.GONE);
            return;
        }

        smallPanel.setVisibility(View.VISIBLE);
        TimeZone MyTimezone = TimeZone.getDefault();
        Calendar calendar = new GregorianCalendar(MyTimezone);
        int weatherTempUnit = w.getTemperatureUnit();
        int numForecasts = forecasts.size();
        int itemSidePadding = context.getResources().getDimensionPixelSize(R.dimen.forecast_item_padding_side);

        // Iterate through the Forecasts
        for (int count = 0; count < numForecasts; count++) {
            WeatherInfo.DayForecast d = forecasts.get(count);

            // Load the views
            assert inflater != null;
            View forecastItem = inflater.inflate(R.layout.item_weather_forecast, null);

            // The day of the week
            TextView day = forecastItem.findViewById(R.id.forecast_day);
            day.setText(calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()));
            calendar.roll(Calendar.DAY_OF_WEEK, true);

            // Weather Image
            ImageView image = forecastItem.findViewById(R.id.weather_image);
            String iconsSet = Preferences.getWeatherIconSet(context);
            final int resId = WeatherIconUtils.getWeatherIconResource(context, iconsSet, d.getConditionCode());
            if (resId != 0) {
                image.setImageResource(resId);
            } else {
                image.setImageBitmap(
                        WeatherIconUtils.getWeatherIconBitmap(context, iconsSet, color, d.getConditionCode()));
            }

            // Temperatures
            double lowTemp = d.getLow();
            double highTemp = d.getHigh();
            int tempUnit = weatherTempUnit;
            if (weatherTempUnit == FAHRENHEIT && useMetric) {
                lowTemp = lineageos.weather.util.WeatherUtils.fahrenheitToCelsius(lowTemp);
                highTemp = lineageos.weather.util.WeatherUtils.fahrenheitToCelsius(highTemp);
                tempUnit = CELSIUS;
            } else if (weatherTempUnit == CELSIUS && !useMetric) {
                lowTemp = lineageos.weather.util.WeatherUtils.celsiusToFahrenheit(lowTemp);
                highTemp = lineageos.weather.util.WeatherUtils.celsiusToFahrenheit(highTemp);
                tempUnit = FAHRENHEIT;
            }
            String dayLow = lineageos.weather.util.WeatherUtils.formatTemperature(lowTemp, tempUnit);
            String dayHigh = WeatherUtils.formatTemperature(highTemp, tempUnit);
            TextView temps = forecastItem.findViewById(R.id.weather_temps);
            temps.setText(String.format("%s\n%s", dayLow, dayHigh));

            // Add the view
            smallPanel.addView(forecastItem,
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            // Add a divider to the right for all but the last view
            if (count < numForecasts - 1) {
                View divider = new View(context);
                smallPanel.addView(divider,
                        new LinearLayout.LayoutParams(itemSidePadding, LinearLayout.LayoutParams.MATCH_PARENT));
            }
        }

        smallPanel.setOnClickListener(v -> {
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("foundation.e.weather");
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchIntent);
            }
        });
    }
}
