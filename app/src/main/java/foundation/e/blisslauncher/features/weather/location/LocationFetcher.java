package foundation.e.blisslauncher.features.weather.location;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

public abstract class LocationFetcher {

    protected LocationManager locationManager;
    protected Callback callback;

    protected Context context;

    public interface Callback {
        void onNewLocation(@Nullable Location location);
    }

    public abstract void fetchLocation();

    protected boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(context,
                permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context,
                        permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}
