package foundation.e.blisslauncher.features.weather.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.location.LocationManagerCompat;

import java.util.concurrent.Executors;

public class NetworkGpsLocationFetcher extends LocationFetcher {

    private Location gpsLocation;
    private Location networkLocation;

    public NetworkGpsLocationFetcher(@NonNull Context context, @NonNull Callback callback) {
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mCallback = callback;
    }

    @Override
    @SuppressLint("MissingPermission")
    public void fetchLocation() {
        LocationManagerCompat.getCurrentLocation(mLocationManager, LocationManager.GPS_PROVIDER, null,
                Executors.newFixedThreadPool(1), this::onLocationFetched);

        LocationManagerCompat.getCurrentLocation(mLocationManager, LocationManager.NETWORK_PROVIDER, null,
                Executors.newFixedThreadPool(1), this::onLocationFetched);
    }

    private void onLocationFetched(@Nullable Location location) {

        if (location == null) {
            return;
        }

        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            gpsLocation = location;
        } else if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
            networkLocation = location;
        }

        mCallback.onNewLocation(getMostRecentLocation());
    }

    private Location getMostRecentLocation() {
        if (networkLocation == null && gpsLocation == null) {
            throw new IllegalStateException();
        }

        if (gpsLocation == null) {
            return networkLocation;
        }

        if (networkLocation == null) {
            return gpsLocation;
        }

        long gpsTime = gpsLocation.getTime();
        long networkTime = networkLocation.getTime();
        return gpsTime >= networkTime ? gpsLocation : networkLocation;
    }

}
