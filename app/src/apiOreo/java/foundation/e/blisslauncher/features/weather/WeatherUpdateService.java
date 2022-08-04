package foundation.e.blisslauncher.features.weather;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.lang.ref.WeakReference;
import java.util.Date;

import foundation.e.blisslauncher.R;
import foundation.e.blisslauncher.core.Preferences;
import foundation.e.blisslauncher.core.utils.Constants;
import lineageos.weather.LineageWeatherManager;
import lineageos.weather.WeatherInfo;
import lineageos.weather.WeatherLocation;

public class WeatherUpdateService extends Service {
    private static final String TAG = "WeatherUpdateService";
    private static final boolean D = Constants.DEBUG;

    public static final String ACTION_FORCE_UPDATE =
            "org.indin.blisslauncher.action.FORCE_WEATHER_UPDATE";
    private static final String ACTION_CANCEL_LOCATION_UPDATE =
            "org.indin.blisslauncher.action.CANCEL_LOCATION_UPDATE";

    private static final String ACTION_CANCEL_UPDATE_WEATHER_REQUEST =
            "org.indin.blisslauncher.action.CANCEL_UPDATE_WEATHER_REQUEST";
    private static final long WEATHER_UPDATE_REQUEST_TIMEOUT_MS = 300L * 1000L;

    // Broadcast action for end of update
    public static final String ACTION_UPDATE_FINISHED =
            "org.indin.blisslauncher.action.WEATHER_UPDATE_FINISHED";
    public static final String EXTRA_UPDATE_CANCELLED = "update_cancelled";

    private static final long LOCATION_REQUEST_TIMEOUT = 5L * 60L * 1000L;
    // request for at most 5 minutes
    private static final long OUTDATED_LOCATION_THRESHOLD_MILLIS = 10L * 60L * 1000L; // 10 minutes
    private static final float LOCATION_ACCURACY_THRESHOLD_METERS = 5000;

    private WorkerThread mWorkerThread;
    private Handler mHandler;

    private static final Criteria sLocationCriteria;

    static {
        sLocationCriteria = new Criteria();
        sLocationCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        sLocationCriteria.setCostAllowed(false);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        mWorkerThread = new WorkerThread(getApplicationContext());
        mWorkerThread.start();
        mWorkerThread.prepareHandler();
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (D) Log.v(TAG, "Got intent " + intent);

        if (ACTION_CANCEL_LOCATION_UPDATE.equals(intent.getAction())) {
            WeatherLocationListener.cancel(this);
            if (!mWorkerThread.isProcessing()) {
                stopSelf();
            }
            return START_NOT_STICKY;
        }

        if (ACTION_CANCEL_UPDATE_WEATHER_REQUEST.equals(intent.getAction())) {
            if (mWorkerThread.isProcessing()) {
                mWorkerThread.getHandler().obtainMessage(
                        WorkerThread.MSG_CANCEL_UPDATE_WEATHER_REQUEST).sendToTarget();
                mHandler.post(() -> {
                    final Context context = getApplicationContext();
                    final LineageWeatherManager weatherManager
                            = LineageWeatherManager.getInstance(context);
                    final String activeProviderLabel
                            = weatherManager.getActiveWeatherServiceProviderLabel();
                    final String noData
                            = getString(R.string.weather_cannot_reach_provider,
                            activeProviderLabel);
                    Toast.makeText(context, noData, Toast.LENGTH_SHORT).show();
                });
            }
            stopSelf();
            return START_NOT_STICKY;
        }

        boolean force = ACTION_FORCE_UPDATE.equals(intent.getAction());
        if (!shouldUpdate(force)) {
            Log.d(TAG, "Service started, but shouldn't update ... stopping");
            sendCancelledBroadcast();
            stopSelf();
            return START_NOT_STICKY;
        }

        mWorkerThread.getHandler().obtainMessage(WorkerThread.MSG_ON_NEW_WEATHER_REQUEST)
                .sendToTarget();

        return START_REDELIVER_INTENT;
    }

    private boolean shouldUpdate(boolean force) {
        final LineageWeatherManager weatherManager
                = LineageWeatherManager.getInstance(getApplicationContext());
        if (weatherManager.getActiveWeatherServiceProviderLabel() == null) {
            //Why bother if we don't even have an active provider
            if (D) Log.d(TAG, "No active weather service provider found, skip");
            return false;
        }

        final long interval = Preferences.weatherRefreshIntervalInMs(this);
        if (interval == 0 && !force) {
            if (D) Log.v(TAG, "Interval set to manual and update not forced, skip");
            return false;
        }

        if (!WeatherPreferences.hasLocationPermission(this)) {
            if (D) Log.v(TAG, "Application does not have the location permission, skip");
            return false;
        }


        ConnectivityManager cm = (ConnectivityManager) getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null || !info.isConnected() || !info.isAvailable()) {
            if (D) Log.d(TAG, "Network is not available, skip");
            return false;
        } else {
            if (force) {
                if (D) Log.d(TAG, "Forcing weather update");
                return true;
            } else {
                final long now = SystemClock.elapsedRealtime();
                final long lastUpdate = Preferences.lastWeatherUpdateTimestamp(this);
                final long due = lastUpdate + interval;
                if (D) {
                    Log.d(TAG, "Now " + now + " Last update " + lastUpdate
                            + " interval " + interval);
                }

                if (lastUpdate == 0 || due - now < 0) {
                    if (D) Log.d(TAG, "Should update");
                    return true;
                } else {
                    if (D) Log.v(TAG, "Next weather update due in " + (due - now) + " ms, skip");
                    return false;
                }
            }
        }
    }

    private class WorkerThread extends HandlerThread
            implements LineageWeatherManager.WeatherUpdateRequestListener {

        public static final int MSG_ON_NEW_WEATHER_REQUEST = 1;
        public static final int MSG_ON_WEATHER_REQUEST_COMPLETED = 2;
        public static final int MSG_WEATHER_REQUEST_FAILED = 3;
        public static final int MSG_CANCEL_UPDATE_WEATHER_REQUEST = 4;

        private Handler mHandler;
        private boolean mIsProcessingWeatherUpdate = false;
        private WakeLock mWakeLock;
        private PendingIntent mTimeoutPendingIntent;
        private int mRequestId;
        private final LineageWeatherManager mWeatherManager;
        final private Context mContext;

        public WorkerThread(Context context) {
            super("weather-service-worker");
            mContext = context;
            mWeatherManager = LineageWeatherManager.getInstance(mContext);
        }

        public synchronized void prepareHandler() {
            mHandler = new Handler(getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    if (D) Log.d(TAG, "Msg " + msg.what);
                    switch (msg.what) {
                        case MSG_ON_NEW_WEATHER_REQUEST:
                            onNewWeatherRequest();
                            break;
                        case MSG_ON_WEATHER_REQUEST_COMPLETED:
                            WeatherInfo info = (WeatherInfo) msg.obj;
                            onWeatherRequestCompleted(info);
                            break;
                        case MSG_WEATHER_REQUEST_FAILED:
                            int status = msg.arg1;
                            onWeatherRequestFailed(status);
                            break;
                        case MSG_CANCEL_UPDATE_WEATHER_REQUEST:
                            onCancelUpdateWeatherRequest();
                            break;
                        default:
                            //Unknown message, pass it on...
                            super.handleMessage(msg);
                    }
                }
            };
        }

        private void startTimeoutAlarm() {
            Intent intent = new Intent(mContext, WeatherUpdateService.class);
            intent.setAction(ACTION_CANCEL_UPDATE_WEATHER_REQUEST);

            mTimeoutPendingIntent = PendingIntent.getService(mContext, 0, intent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_CANCEL_CURRENT);

            AlarmManager am = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
            long elapseTime = SystemClock.elapsedRealtime() + WEATHER_UPDATE_REQUEST_TIMEOUT_MS;
            am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, elapseTime, mTimeoutPendingIntent);
            if (D) Log.v(TAG, "Timeout alarm set to expire in " + elapseTime + " ms");
        }

        private void cancelTimeoutAlarm() {
            Log.d(TAG, "cancelTimeoutAlarm() called");
            if (mTimeoutPendingIntent != null) {
                AlarmManager am = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
                am.cancel(mTimeoutPendingIntent);
                mTimeoutPendingIntent = null;
                if (D) Log.v(TAG, "Timeout alarm cancelled");
            }
        }

        public synchronized Handler getHandler() {
            return mHandler;
        }

        @SuppressLint("WakelockTimeout")
        private void onNewWeatherRequest() {
            if (mIsProcessingWeatherUpdate) {
                Log.d(TAG, "Already processing weather update, discarding request...");
                return;
            }

            mIsProcessingWeatherUpdate = true;
            final PowerManager pm
                    = (PowerManager) mContext.getSystemService(POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "blisslauncher:WeatherUpdateService");
            mWakeLock.setReferenceCounted(false);
            if (D) Log.v(TAG, "ACQUIRING WAKELOCK");
            mWakeLock.acquire();

            WeatherLocation customWeatherLocation = null;
            if (Preferences.useCustomWeatherLocation(mContext)) {
                customWeatherLocation = Preferences.getCustomWeatherLocation(mContext);
            }
            if (customWeatherLocation != null) {
                mRequestId = mWeatherManager.requestWeatherUpdate(customWeatherLocation, this);
                if (D) Log.d(TAG, "Request submitted using WeatherLocation");
                startTimeoutAlarm();
            } else {
                final Location location = getCurrentLocation();
                if (location != null) {
                    mRequestId = mWeatherManager.requestWeatherUpdate(location, this);
                    if (D) Log.d(TAG, "Request submitted using Location");
                    startTimeoutAlarm();
                } else {
                    // work with cached location from last request for now
                    // a listener to update it is already scheduled if possible
                    WeatherInfo cachedInfo = Preferences.getCachedWeatherInfo(mContext);
                    if (cachedInfo != null) {
                        mHandler.obtainMessage(MSG_ON_WEATHER_REQUEST_COMPLETED,
                                cachedInfo).sendToTarget();
                        if (D) Log.d(TAG, "Returning cached weather data [ "
                                + cachedInfo.toString() + " ]");
                    } else {
                        mHandler.obtainMessage(MSG_WEATHER_REQUEST_FAILED,
                                LineageWeatherManager.RequestStatus.FAILED, 0).sendToTarget();
                    }
                }
            }
        }

        public void tearDown() {
            if (D) Log.d(TAG, "Tearing down worker thread");
            if (isProcessing()) mWeatherManager.cancelRequest(mRequestId);
            quit();
        }

        public boolean isProcessing() {
            return mIsProcessingWeatherUpdate;
        }

        private Location getCurrentLocation() {
            final LocationManager lm
                    = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            Location location = null;
            try{
                location = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            }catch (SecurityException e){
                e.printStackTrace();
            }
            if (D && location != null) Log.v(TAG, "Current location is " + location + ", accuracy: " + location.getAccuracy());

            if (location != null && location.getAccuracy() > LOCATION_ACCURACY_THRESHOLD_METERS) {
                if (D) Log.d(TAG, "Ignoring inaccurate location");
                location = null;
            }

            // If lastKnownLocation is not present (because none of the apps in the
            // device has requested the current location to the system yet) or outdated,
            // then try to get the current location use the provider that best matches the criteria.
            boolean needsUpdate = location == null;
            if (location != null) {
                long delta = System.currentTimeMillis() - location.getTime();
                needsUpdate = delta > OUTDATED_LOCATION_THRESHOLD_MILLIS;
            }
            if (needsUpdate) {
                if (D) Log.d(TAG, "Getting best location provider");
                String locationProvider = lm.getBestProvider(sLocationCriteria, true);
                if (TextUtils.isEmpty(locationProvider)) {
                    Log.e(TAG, "No available location providers matching criteria.");
                } else {
                    WeatherLocationListener.registerIfNeeded(mContext, locationProvider);
                }
            }
            return location;
        }

        private void onWeatherRequestCompleted(WeatherInfo result) {
            Log.d(TAG, "onWeatherRequestCompleted() called with: result = [" + result + "]");
            cancelTimeoutAlarm();
            long now = SystemClock.elapsedRealtime();
            Preferences.setCachedWeatherInfo(mContext, now, result);
            Preferences.setLastWeatherUpdateTimestamp(mContext, now);
            scheduleUpdate(mContext, Preferences.weatherRefreshIntervalInMs(mContext), false);

            Intent updateIntent = new Intent(WeatherUpdateService.ACTION_UPDATE_FINISHED);
            LocalBroadcastManager.getInstance(WeatherUpdateService.this).sendBroadcast(updateIntent);
            broadcastAndCleanUp(false);
        }

        private void onWeatherRequestFailed(int status) {
            if (D) Log.d(TAG, "Weather refresh failed ["+status+"]");
            cancelTimeoutAlarm();
            if (status == LineageWeatherManager.RequestStatus.ALREADY_IN_PROGRESS) {
                if (D) Log.d(TAG, "A request is already in progress, no need to schedule again");
            } else if (status == LineageWeatherManager.RequestStatus.FAILED) {
                //Something went wrong, let's schedule an update at the next interval from now
                //A force update might happen earlier anyway
                scheduleUpdate(mContext, Preferences.weatherRefreshIntervalInMs(mContext), false);
            } else {
                //Wait until the next update is due
                scheduleNextUpdate(mContext, false);
            }
            broadcastAndCleanUp(true);
        }

        private void onCancelUpdateWeatherRequest() {
            if (D) Log.d(TAG, "Cancelling active weather request");
            if (mIsProcessingWeatherUpdate) {
                cancelTimeoutAlarm();
                mWeatherManager.cancelRequest(mRequestId);
                broadcastAndCleanUp(true);
            }
        }

        private void broadcastAndCleanUp(boolean updateCancelled) {
            Intent finishedIntent = new Intent(ACTION_UPDATE_FINISHED);
            finishedIntent.putExtra(EXTRA_UPDATE_CANCELLED, updateCancelled);
            mContext.sendBroadcast(finishedIntent);

            if (D) Log.d(TAG, "RELEASING WAKELOCK");
            mWakeLock.release();
            mIsProcessingWeatherUpdate = false;
            mContext.stopService(new Intent(mContext, WeatherUpdateService.class));
        }

        @Override
        public void onWeatherRequestCompleted(int state, WeatherInfo weatherInfo) {
            if (state == LineageWeatherManager.RequestStatus.COMPLETED) {
                mHandler.obtainMessage(WorkerThread.MSG_ON_WEATHER_REQUEST_COMPLETED, weatherInfo)
                        .sendToTarget();
            } else {
                mHandler.obtainMessage(WorkerThread.MSG_WEATHER_REQUEST_FAILED, state, 0)
                        .sendToTarget();
            }
        }
    }

    private void sendCancelledBroadcast() {
        Intent finishedIntent = new Intent(ACTION_UPDATE_FINISHED);
        finishedIntent.putExtra(EXTRA_UPDATE_CANCELLED, true);
        sendBroadcast(finishedIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mWorkerThread.tearDown();
    }

    private static class WeatherLocationListener implements LocationListener {
        private WeakReference<Context> mContext;
        private PendingIntent mTimeoutIntent;
        private static WeatherLocationListener sInstance = null;

        @SuppressLint("MissingPermission")
        static void registerIfNeeded(Context context, String provider) {
            synchronized (WeatherLocationListener.class) {
                if (D) Log.d(TAG, "Registering location listener");
                if (sInstance == null) {
                    final Context appContext = context.getApplicationContext();
                    final LocationManager locationManager =
                            (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);

                    // Check location provider after set sInstance, so, if the provider is not
                    // supported, we never enter here again.
                    sInstance = new WeatherLocationListener(appContext);
                    // Check whether the provider is supported.
                    // NOTE!!! Actually only WeatherUpdateService class is calling this function
                    // with the NETWORK_PROVIDER, so setting the instance is safe. We must
                    // change this if this call receive different providers
                    LocationProvider lp = locationManager.getProvider(provider);
                    if (lp != null) {
                        if (D) Log.d(TAG, "LocationManager - Requesting single update");
                        locationManager.requestSingleUpdate(provider, sInstance,
                                appContext.getMainLooper());
                        sInstance.setTimeoutAlarm();
                    }
                }
            }
        }

        static void cancel(Context context) {
            synchronized (WeatherLocationListener.class) {
                if (sInstance != null) {
                    final Context appContext = context.getApplicationContext();
                    final LocationManager locationManager =
                        (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
                    if (D) Log.d(TAG, "Aborting location request after timeout");
                    locationManager.removeUpdates(sInstance);
                    sInstance.cancelTimeoutAlarm();
                    sInstance = null;
                }
            }
        }

        private WeatherLocationListener(Context context) {
            super();
            mContext = new WeakReference<>(context);
        }

        private void setTimeoutAlarm() {
            Intent intent = new Intent(mContext.get(), WeatherUpdateService.class);
            intent.setAction(ACTION_CANCEL_LOCATION_UPDATE);

            mTimeoutIntent = PendingIntent.getService(mContext.get(), 0, intent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_CANCEL_CURRENT);

            AlarmManager am = (AlarmManager) mContext.get().getSystemService(ALARM_SERVICE);
            long elapseTime = SystemClock.elapsedRealtime() + LOCATION_REQUEST_TIMEOUT;
            am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, elapseTime, mTimeoutIntent);
        }

        private void cancelTimeoutAlarm() {
            Log.d(TAG, "cancelTimeoutAlarm2() called");
            if (mTimeoutIntent != null) {
                AlarmManager am = (AlarmManager) mContext.get().getSystemService(ALARM_SERVICE);
                am.cancel(mTimeoutIntent);
                mTimeoutIntent = null;
            }
        }

        @Override
        public void onLocationChanged(Location location) {
            // Now, we have a location to use. Schedule a weather update right now.
            if (D) Log.d(TAG, "The location has changed, schedule an update ");
            synchronized (WeatherLocationListener.class) {
                scheduleUpdate(mContext.get(), 0, true);
                cancelTimeoutAlarm();
                sInstance = null;
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // Now, we have a location to use. Schedule a weather update right now.
            if (D) Log.d(TAG, "The location service has become available, schedule an update ");
            if (status == LocationProvider.AVAILABLE) {
                synchronized (WeatherLocationListener.class) {
                    scheduleUpdate(mContext.get(), 0, true);
                    cancelTimeoutAlarm();
                    sInstance = null;
                }
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            // Not used
        }

        @Override
        public void onProviderDisabled(String provider) {
            // Not used
        }
    }

    private static void scheduleUpdate(Context context, long millisFromNow, boolean force) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long due = SystemClock.elapsedRealtime() + millisFromNow;
        if (D) Log.d(TAG, "Next update scheduled at "
                + new Date(System.currentTimeMillis() + millisFromNow));
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, due, getUpdateIntent(context, force));
    }

    public static void scheduleNextUpdate(Context context, boolean force) {
        if (force) {
            if (D) Log.d(TAG, "Scheduling next update immediately");
            scheduleUpdate(context, 0, true);
        } else {
            final long lastUpdate = Preferences.lastWeatherUpdateTimestamp(context);
            final long interval = Preferences.weatherRefreshIntervalInMs(context);
            final long now = SystemClock.elapsedRealtime();
            long due = (interval + lastUpdate) - now;
            if (due < 0) due = 0;
            if (D) Log.d(TAG, "Scheduling in " + due + " ms");
            scheduleUpdate(context, due, false);
        }
    }

    public static PendingIntent getUpdateIntent(Context context, boolean force) {
        Intent i = new Intent(context, WeatherUpdateService.class);
        if (force) {
            i.setAction(ACTION_FORCE_UPDATE);
        }
        return PendingIntent.getService(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void cancelUpdates(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(getUpdateIntent(context, true));
        am.cancel(getUpdateIntent(context, false));
        WeatherLocationListener.cancel(context);
    }
}
