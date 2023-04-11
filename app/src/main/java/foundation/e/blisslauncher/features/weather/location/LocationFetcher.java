package foundation.e.blisslauncher.features.weather.location;

import android.location.Location;
import android.location.LocationManager;

import androidx.annotation.Nullable;

public abstract class LocationFetcher {

    protected LocationManager mLocationManager;
    protected Callback mCallback;

    public interface Callback {
        void onNewLocation(@Nullable Location location);
    }

    public abstract void fetchLocation();
}
