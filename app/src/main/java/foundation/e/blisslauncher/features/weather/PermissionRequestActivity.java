package foundation.e.blisslauncher.features.weather;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
import androidx.annotation.NonNull;

public class PermissionRequestActivity extends Activity {

    private static final String RESULT_RECEIVER_EXTRA = "result_receiver";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private ResultReceiver mResultReceiver;
    private int mResult = RESULT_CANCELED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (hasLocationPermission()) {
            finish();
            return;
        }

        mResultReceiver = getIntent().getParcelableExtra(RESULT_RECEIVER_EXTRA);
        if (mResultReceiver == null) {
            finish();
            return;
        }

        String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    public boolean hasLocationPermission() {
        return checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mResult = RESULT_OK;
            }
        }
        finish();
    }

    @Override
    public void finish() {
        if (mResultReceiver != null) {
            mResultReceiver.send(mResult, null);
        }
        super.finish();
    }
}
