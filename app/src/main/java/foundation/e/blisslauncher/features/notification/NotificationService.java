package foundation.e.blisslauncher.features.notification;

import static foundation.e.blisslauncher.BlissLauncher.NOTIFICATION_BADGING_URI;

import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import foundation.e.blisslauncher.core.utils.ListUtil;
import java.util.Collections;

/** Created by falcon on 14/3/18. */
public class NotificationService extends NotificationListenerService {

    private static boolean sIsConnected = false;

    NotificationRepository mNotificationRepository;

    private boolean mAreDotsDisabled;
    private final ContentObserver mNotificationSettingsObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            onNotificationSettingsChanged();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationRepository = NotificationRepository.getNotificationRepository();

        getContentResolver().registerContentObserver(NOTIFICATION_BADGING_URI, false, mNotificationSettingsObserver);
        onNotificationSettingsChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(mNotificationSettingsObserver);
        mNotificationRepository.updateNotification(Collections.emptyList());
    }

    private void onNotificationSettingsChanged() {
        mAreDotsDisabled = Settings.Secure.getInt(getContentResolver(), NOTIFICATION_BADGING_URI.getLastPathSegment(),
                1) != 1;
        if (mAreDotsDisabled && sIsConnected) {
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
        if (mAreDotsDisabled) {
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
