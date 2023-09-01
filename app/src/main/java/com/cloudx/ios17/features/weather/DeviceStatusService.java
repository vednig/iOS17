package com.cloudx.ios17.features.weather;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;

import com.cloudx.ios17.core.utils.Constants;
import com.cloudx.ios17.core.utils.Constants;
import com.cloudx.ios17.core.utils.Constants;
import com.cloudx.ios17.core.utils.Constants;
import timber.log.Timber;

public class DeviceStatusService extends Service {

    private static final String TAG = DeviceStatusService.class.getSimpleName();
    private static final boolean D = Constants.DEBUG;

    private BroadcastReceiver mDeviceStatusListenerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // Network connection has changed, make sure the weather update service knows
            // about it
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                boolean hasConnection = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

                if (D)
                    Timber.tag(TAG).d("Got connectivity change, has connection: " + hasConnection);

                Intent i = new Intent(context, WeatherUpdateService.class);
                if (hasConnection) {
                    context.startService(i);
                } else {
                    context.stopService(i);
                }
            }
        }
    };

    @Override
    public void onCreate() {
        IntentFilter deviceStatusFilter = new IntentFilter();
        deviceStatusFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        deviceStatusFilter.addAction(Intent.ACTION_SCREEN_OFF);
        deviceStatusFilter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(mDeviceStatusListenerReceiver, deviceStatusFilter);
    }

    @Override
    public void onDestroy() {
        if (D)
            Timber.tag(TAG).d("Stopping service");
        unregisterReceiver(mDeviceStatusListenerReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (D)
            Timber.tag(TAG).d("Starting service");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
