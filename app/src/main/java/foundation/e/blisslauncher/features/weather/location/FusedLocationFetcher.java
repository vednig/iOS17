package foundation.e.blisslauncher.features.weather.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.location.LocationManagerCompat;

import java.util.concurrent.Executors;

import timber.log.Timber;

public class FusedLocationFetcher extends LocationFetcher {

    public FusedLocationFetcher(@NonNull Context context, @NonNull Callback callback) {
        this.context = context;
        this.callback = callback;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void fetchLocation() {
        if (!checkPermission()) {
            Timber.w("Could not fetch location. Missing permission.");
            return;
        }

        if (VERSION.SDK_INT >= VERSION_CODES.S) {
            LocationManagerCompat.getCurrentLocation(locationManager, LocationManager.FUSED_PROVIDER, null,
                    Executors.newFixedThreadPool(1), this::onLocationFetched);
        }
    }

    @RequiresApi(api = VERSION_CODES.S)
    private void onLocationFetched(@Nullable Location location) {
        if (location == null && checkPermission()) {
            location = locationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER);
        }

        callback.onNewLocation(location);
    }
}