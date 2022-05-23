package foundation.e.blisslauncher.features.notification;

import static foundation.e.blisslauncher.util.SettingsCache.NOTIFICATION_BADGING_URI;

import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.util.Collections;

import foundation.e.blisslauncher.core.utils.ListUtil;
import foundation.e.blisslauncher.util.SettingsCache;

/**
 * Created by falcon on 14/3/18.
 */

public class NotificationService extends NotificationListenerService {

    private static boolean sIsConnected = false;

    NotificationRepository mNotificationRepository;

    private boolean mDotsEnabled;
    private SettingsCache mSettingsCache;
    private SettingsCache.OnChangeListener mNotificationSettingsChangedListener;

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationRepository = NotificationRepository.getNotificationRepository();

        // Register an observer to rebind the notification listener when dots are re-enabled.
        mSettingsCache = SettingsCache.INSTANCE.get(this);
        mNotificationSettingsChangedListener = this::onNotificationSettingsChanged;
        mSettingsCache.register(NOTIFICATION_BADGING_URI,
                mNotificationSettingsChangedListener);
        onNotificationSettingsChanged(mSettingsCache.getValue(NOTIFICATION_BADGING_URI));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSettingsCache.unregister(NOTIFICATION_BADGING_URI, mNotificationSettingsChangedListener);
        mNotificationRepository.updateNotification(Collections.emptyList());
    }

    private void onNotificationSettingsChanged(boolean areNotificationDotsEnabled) {
        mDotsEnabled = areNotificationDotsEnabled;
        if (!areNotificationDotsEnabled && sIsConnected) {
            requestUnbind();
            updateNotifications();
        }
    }

    @Override
    public void onListenerConnected() {
        sIsConnected = true;
        updateNotifications();
    }

    @Override
    public void onListenerDisconnected() {
        sIsConnected = false;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        updateNotifications();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        updateNotifications();
    }

    private void updateNotifications() {
        if (!mDotsEnabled) {
            mNotificationRepository.updateNotification(Collections.emptyList());
            return;
        }
        mNotificationRepository.updateNotification(ListUtil.asSafeList(getActiveNotifications()));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
