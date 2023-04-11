package foundation.e.blisslauncher.features.weather.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.location.LocationManagerCompat;

import java.util.concurrent.Executors;

public class FusedLocationFetcher extends LocationFetcher {

    public FusedLocationFetcher(@NonNull Context context, @NonNull Callback callback) {
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mCallback = callback;
    }

    @Override
    @SuppressLint("MissingPermission")
    public void fetchLocation() {
        if (VERSION.SDK_INT >= VERSION_CODES.S) {
            LocationManagerCompat.getCurrentLocation(mLocationManager, LocationManager.FUSED_PROVIDER, null,
                    Executors.newFixedThreadPool(1), this::onLocationFetched);
        }
    }

    private void onLocationFetched(@Nullable Location location) {
        mCallback.onNewLocation(location);
    }
}
