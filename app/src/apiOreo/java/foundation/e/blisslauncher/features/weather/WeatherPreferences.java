package foundation.e.blisslauncher.features.weather;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import foundation.e.blisslauncher.R;
import foundation.e.blisslauncher.core.Preferences;
import foundation.e.blisslauncher.core.utils.Constants;
import lineageos.weather.LineageWeatherManager;


public class WeatherPreferences extends PreferenceActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        LineageWeatherManager.WeatherServiceProviderChangeListener {
    private static final String TAG = "WeatherPreferences";
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private SwitchPreference mUseCustomLoc;
    private EditTextPreference mCustomWeatherLoc;
    private SwitchPreference mUseMetric;
    private IconSelectionPreference mIconSet;
    private SwitchPreference mUseCustomlocation;
    private Context mContext;
    private Runnable mPostResumeRunnable;
    private PreferenceScreen mWeatherSource;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(Constants.PREF_NAME);
        addPreferencesFromResource(R.xml.preferences_weather);
        mContext = this;

        // Load networkItems that need custom summaries etc.
        mUseCustomLoc = (SwitchPreference) findPreference(Constants.WEATHER_USE_CUSTOM_LOCATION);
        mCustomWeatherLoc = (EditTextPreference) findPreference(
                Constants.WEATHER_CUSTOM_LOCATION_CITY);
        mIconSet = (IconSelectionPreference) findPreference(Constants.WEATHER_ICONS);
        mUseMetric = (SwitchPreference) findPreference(Constants.WEATHER_USE_METRIC);
        mUseCustomlocation = (SwitchPreference) findPreference(
                Constants.WEATHER_USE_CUSTOM_LOCATION);
        mWeatherSource = (PreferenceScreen) findPreference(Constants.WEATHER_SOURCE);
        mWeatherSource.setOnPreferenceChangeListener((preference, o) -> {
            if (Preferences.getWeatherSource(mContext) != null) {
                mWeatherSource.notifyDependencyChange(false);
            } else {
                mWeatherSource.notifyDependencyChange(true);
            }
            return false;
        });

        // At first placement/start default the use of Metric units based on locale
        // If we had a previously set value already, this will just reset the same value
        Boolean defValue = Preferences.useMetricUnits(mContext);
        Preferences.setUseMetricUnits(mContext, defValue);
        mUseMetric.setChecked(defValue);

        if (!mUseCustomLoc.isChecked()) {
            if (!hasLocationPermission(this)) {
                String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
                requestPermissions(permissions, LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (!lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    showDialog();
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(
                this);

        if (mPostResumeRunnable != null) {
            mPostResumeRunnable.run();
            mPostResumeRunnable = null;
        }

        final LineageWeatherManager weatherManager = LineageWeatherManager.getInstance(mContext);
        weatherManager.registerWeatherServiceProviderChangeListener(this);

        mWeatherSource.setEnabled(true);

        updateLocationSummary();
        updateIconSetSummary();
        updateWeatherProviderSummary(getWeatherProviderName());
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
                this);
        final LineageWeatherManager weatherManager = LineageWeatherManager.getInstance(mContext);
        weatherManager.unregisterWeatherServiceProviderChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mUseCustomlocation.isChecked()
                && Preferences.getCustomWeatherLocationCity(mContext) == null) {
            //The user decided to toggle the custom location switch, but forgot to set a custom
            //location, we need to go back to geo location
            Preferences.setUseCustomWeatherLocation(mContext, false);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
            pref.setSummary(listPref.getEntry());
        }

        boolean needWeatherUpdate = false;
        boolean forceWeatherUpdate = false;

        if (pref == mUseCustomLoc || pref == mCustomWeatherLoc) {
            updateLocationSummary();
        }

        if (pref == mIconSet) {
            updateIconSetSummary();
        }

        if (pref == mUseMetric) {
            // The display format of the temperatures have changed
            // Force a weather update to refresh the display
            forceWeatherUpdate = true;
        }

        if (TextUtils.equals(key, Constants.WEATHER_SOURCE)) {
            // The weather source changed, invalidate the custom location settings and change
            // back to GeoLocation to force the user to specify a new custom location if needed
            Preferences.setCustomWeatherLocationCity(mContext, null);
            Preferences.setCustomWeatherLocation(mContext, null);
            Preferences.setUseCustomWeatherLocation(mContext, false);
            mUseCustomlocation.setChecked(false);
            updateLocationSummary();
        }

        if (key.equals(Constants.WEATHER_USE_CUSTOM_LOCATION)) {
            if (!mUseCustomLoc.isChecked() || (mUseCustomLoc.isChecked() &&
                    Preferences.getCustomWeatherLocation(mContext) != null)) {
                forceWeatherUpdate = true;
            }
        }

        if (key.equals(Constants.WEATHER_CUSTOM_LOCATION_CITY) && mUseCustomLoc.isChecked()) {
            forceWeatherUpdate = true;
        }

        if (key.equals(Constants.WEATHER_REFRESH_INTERVAL)) {
            needWeatherUpdate = true;
        }

        mWeatherSource.setEnabled(true);
        if (Preferences.getWeatherSource(mContext) != null) {
            mWeatherSource.notifyDependencyChange(false);
        } else {
            mWeatherSource.notifyDependencyChange(true);
        }

        if (Constants.DEBUG) {
            Log.v(TAG, "Preference " + key + " changed, need update " +
                    needWeatherUpdate + " force update " + forceWeatherUpdate);
        }

        if ((needWeatherUpdate || forceWeatherUpdate)) {
            Intent updateIntent = new Intent(mContext, WeatherUpdateService.class);
            if (forceWeatherUpdate) {
                updateIntent.setAction(WeatherUpdateService.ACTION_FORCE_UPDATE);
            }
            mContext.startService(updateIntent);
        }
    }

    public static boolean hasLocationPermission(Context context) {
        return context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    //===============================================================================================
    // Utility classes and supporting methods
    //===============================================================================================

    private void updateLocationSummary() {
        if (mUseCustomLoc.isChecked()) {
            String location = Preferences.getCustomWeatherLocationCity(mContext);
            if (location == null) {
                location = getResources().getString(R.string.unknown);
            }
            mCustomWeatherLoc.setSummary(location);
        } else {
            if (!hasLocationPermission(mContext)) {
                String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};
                requestPermissions(permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
            mCustomWeatherLoc.setSummary(R.string.weather_geolocated);
        }
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final Dialog dialog;

        // Build and show the dialog
        builder.setTitle(R.string.weather_retrieve_location_dialog_title);
        builder.setMessage(R.string.weather_retrieve_location_dialog_message);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.weather_retrieve_location_dialog_enable_button,
                (dialog1, whichButton) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(intent, 203);
                });
        builder.setNegativeButton(R.string.cancel, null);
        dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 203) {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                Toast.makeText(this, getString(R.string.toast_custom_location),
                        Toast.LENGTH_SHORT).show();
            } else {
                startService(new Intent(this, WeatherUpdateService.class)
                        .putExtra(WeatherUpdateService.ACTION_FORCE_UPDATE, true));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void updateIconSetSummary() {
        if (mIconSet != null) {
            mIconSet.setSummary(mIconSet.getEntry());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We only get here if user tried to enable the preference,
                // hence safe to turn it on after permission is granted
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (!lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    showDialog();
                } else {
                    startService(new Intent(this, WeatherUpdateService.class)
                            .putExtra(WeatherUpdateService.ACTION_FORCE_UPDATE, true));
                }
            }
        }
    }

    @Override
    public void onWeatherServiceProviderChanged(String providerName) {
        updateWeatherProviderSummary(providerName);
    }

    private void updateWeatherProviderSummary(String providerName) {
        if (providerName != null) {
            mWeatherSource.setSummary(providerName);
            Preferences.setWeatherSource(mContext, providerName);
        } else {
            mWeatherSource.setSummary(R.string.weather_source_not_selected);
            Preferences.setWeatherSource(mContext, null);
        }

        if (providerName != null) {
            mWeatherSource.notifyDependencyChange(false);
        } else {
            mWeatherSource.notifyDependencyChange(true);
        }
    }

    private String getWeatherProviderName() {
        final LineageWeatherManager weatherManager = LineageWeatherManager.getInstance(mContext);
        return weatherManager.getActiveWeatherServiceProviderLabel();
    }
}
