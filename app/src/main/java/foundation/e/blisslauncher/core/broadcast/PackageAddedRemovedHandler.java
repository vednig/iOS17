package foundation.e.blisslauncher.core.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import foundation.e.blisslauncher.BlissLauncher;
import foundation.e.blisslauncher.core.events.AppAddEvent;
import foundation.e.blisslauncher.core.events.AppChangeEvent;
import foundation.e.blisslauncher.core.events.AppRemoveEvent;
import foundation.e.blisslauncher.core.events.EventRelay;
import foundation.e.blisslauncher.core.utils.UserHandle;
import timber.log.Timber;

public class PackageAddedRemovedHandler extends BroadcastReceiver {

    private static final String TAG = "PackageAddedRemovedHand";

    public static void handleEvent(Context ctx, String action, String packageName, UserHandle user, boolean replacing) {

        if (!Process.myUserHandle().equals(user.getRealHandle())) {
            return;
        }
        Timber.tag(TAG).d("handleEvent() called with: ctx = [" + ctx + "], action = [" + action + "], packageName = ["
                + packageName + "], user = [" + user + "], replacing = [" + replacing + "]");
        // Insert into history new packages (not updated ones)
        if ("android.intent.action.PACKAGE_ADDED".equals(action) && !replacing) {
            Intent launchIntent = ctx.getPackageManager().getLaunchIntentForPackage(packageName);
            if (launchIntent == null) { // for some plugin app
                return;
            }

            BlissLauncher.getApplication(ctx).resetIconsHandler();

            AppAddEvent event = new AppAddEvent(packageName, user);
            EventRelay.getInstance().push(event);
            BlissLauncher.getApplication(ctx).getAppProvider().reload(false);
        }

        if ("android.intent.action.PACKAGE_CHANGED".equalsIgnoreCase(action)) {
            Intent launchIntent = ctx.getPackageManager().getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                BlissLauncher.getApplication(ctx).getIconsHandler()
                        .resetIconDrawableForPackage(launchIntent.getComponent(), user);
            }

            BlissLauncher.getApplication(ctx).resetIconsHandler();

            AppChangeEvent event = new AppChangeEvent(packageName, user);
            EventRelay.getInstance().push(event);
            BlissLauncher.getApplication(ctx).getAppProvider().reload(false);
        }
        if ("android.intent.action.PACKAGE_REMOVED".equals(action) && !replacing) {
            AppRemoveEvent event = new AppRemoveEvent(packageName, user);
            EventRelay.getInstance().push(event);
            BlissLauncher.getApplication(ctx).getAppProvider().reload(false);
        }

        if ("android.intent.action.MEDIA_MOUNTED".equals(action)) {
            Intent launchIntent = ctx.getPackageManager().getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                BlissLauncher.getApplication(ctx).getIconsHandler()
                        .resetIconDrawableForPackage(launchIntent.getComponent(), user);
                AppChangeEvent appChangeEvent = new AppChangeEvent(packageName, user);
                EventRelay.getInstance().push(appChangeEvent);
            }
        }
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        String packageName = intent.getData().getSchemeSpecificPart();
        if (packageName.equalsIgnoreCase(ctx.getPackageName())) {
            return;
        }
        handleEvent(ctx, intent.getAction(), packageName, new UserHandle(),
                intent.getBooleanExtra(Intent.EXTRA_REPLACING, false));
    }
}
