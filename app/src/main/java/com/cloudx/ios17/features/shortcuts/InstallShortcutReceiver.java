package com.cloudx.ios17.features.shortcuts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.os.UserManager;

import com.cloudx.ios17.BlissLauncher;
import com.cloudx.ios17.core.IconsHandler;
import com.cloudx.ios17.core.Utilities;
import com.cloudx.ios17.core.database.model.ShortcutItem;
import com.cloudx.ios17.core.events.EventRelay;
import com.cloudx.ios17.core.events.ShortcutAddEvent;
import com.cloudx.ios17.core.utils.Constants;
import com.cloudx.ios17.core.utils.UserHandle;
import com.cloudx.ios17.BlissLauncher;
import com.cloudx.ios17.core.IconsHandler;
import com.cloudx.ios17.core.Utilities;
import com.cloudx.ios17.core.database.model.ShortcutItem;
import com.cloudx.ios17.core.events.EventRelay;
import com.cloudx.ios17.core.events.ShortcutAddEvent;
import com.cloudx.ios17.core.utils.Constants;
import com.cloudx.ios17.core.utils.UserHandle;
import com.cloudx.ios17.BlissLauncher;
import com.cloudx.ios17.core.IconsHandler;
import com.cloudx.ios17.core.Utilities;
import com.cloudx.ios17.core.database.model.ShortcutItem;
import com.cloudx.ios17.core.events.EventRelay;
import com.cloudx.ios17.core.events.ShortcutAddEvent;
import com.cloudx.ios17.core.utils.Constants;
import com.cloudx.ios17.core.utils.UserHandle;
import com.cloudx.ios17.BlissLauncher;
import com.cloudx.ios17.core.IconsHandler;
import com.cloudx.ios17.core.Utilities;
import com.cloudx.ios17.core.database.model.ShortcutItem;
import com.cloudx.ios17.core.events.EventRelay;
import com.cloudx.ios17.core.events.ShortcutAddEvent;
import com.cloudx.ios17.core.utils.Constants;
import com.cloudx.ios17.core.utils.UserHandle;
import timber.log.Timber;

import java.io.ByteArrayOutputStream;

public class InstallShortcutReceiver extends BroadcastReceiver {
    private static final String TAG = "InstallShortcutReceiver";

    private static final String ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";

    private static final String LAUNCH_INTENT_KEY = "intent.launch";
    private static final String DEEPSHORTCUT_TYPE_KEY = "isDeepShortcut";
    private static final String APP_SHORTCUT_TYPE_KEY = "isAppShortcut";
    private static final String USER_HANDLE_KEY = "userHandle";
    private static final String NAME_KEY = "name";
    private static final String ICON_KEY = "icon";
    private static final String ICON_RESOURCE_NAME_KEY = "iconResource";
    private static final String ICON_RESOURCE_PACKAGE_NAME_KEY = "iconResourcePackage";

    // The set of shortcuts that are pending install
    private static final String APPS_PENDING_INSTALL = "apps_to_install";

    public static final int NEW_SHORTCUT_BOUNCE_DURATION = 450;
    public static final int NEW_SHORTCUT_STAGGER_DELAY = 85;

    private static final Object sLock = new Object();

    @Override
    public void onReceive(Context context, Intent data) {
        if (!ACTION_INSTALL_SHORTCUT.equals(data.getAction())) {
            return;
        }
        ShortcutItem shortcutItem = createShortcutItem(data, context);
        EventRelay.getInstance().push(new ShortcutAddEvent(shortcutItem));
        /*
         * context.startActivity(new Intent(Intent.ACTION_MAIN)
         * .addCategory(Intent.CATEGORY_HOME) .setPackage(context.getPackageName())
         * .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
         */
    }

    public static void queueShortcut(ShortcutInfoCompat info, Context context) {
        Timber.tag(TAG).d("queueShortcut() called with: info = [" + info + "], context = [" + context + "]");
        UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        ShortcutItem shortcutItem = new ShortcutItem();
        shortcutItem.id = info.getId();
        Timber.tag(TAG).i("queueShortcut: " + shortcutItem.id);
        shortcutItem.user = new UserHandle(userManager.getSerialNumberForUser(info.getUserHandle()),
                info.getUserHandle());
        shortcutItem.packageName = info.getPackage();
        shortcutItem.title = info.getShortLabel().toString();
        shortcutItem.container = Constants.CONTAINER_DESKTOP;
        Drawable icon = DeepShortcutManager.getInstance(context).getShortcutIconDrawable(info,
                context.getResources().getDisplayMetrics().densityDpi);
        shortcutItem.icon = BlissLauncher.getApplication(context).getIconsHandler().convertIcon(icon);
        shortcutItem.launchIntent = info.makeIntent();
        EventRelay.getInstance().push(new ShortcutAddEvent(shortcutItem));
        /*
         * context.startActivity(new Intent(Intent.ACTION_MAIN)
         * .addCategory(Intent.CATEGORY_HOME) .setPackage(context.getPackageName())
         * .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
         */
    }

    private static ShortcutItem createShortcutItem(Intent data, Context context) {
        Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        Parcelable bitmap = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

        if (intent == null) {
            // If the intent is null, we can't construct a valid ShortcutInfo, so we return
            // null
            Timber.tag(TAG).e("Can't construct ShortcutInfo with null intent");
            return null;
        }

        final ShortcutItem item = new ShortcutItem();

        // Only support intents for current user for now. Intents sent from other
        // users wouldn't get here without intent forwarding anyway.
        item.user = new UserHandle();

        Drawable icon = null;
        if (bitmap instanceof Bitmap) {
            icon = IconsHandler.createIconDrawable((Bitmap) bitmap, context);
        } else {
            Parcelable extra = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
            if (extra instanceof Intent.ShortcutIconResource) {
                icon = IconsHandler.createIconDrawable((Intent.ShortcutIconResource) extra, context);
            }
        }
        if (icon == null) {
            icon = BlissLauncher.getApplication(context).getIconsHandler().getFullResDefaultActivityIcon();
        }
        item.packageName = intent.getPackage();
        item.container = Constants.CONTAINER_DESKTOP;
        item.title = Utilities.trim(name);
        item.icon = BlissLauncher.getApplication(context).getIconsHandler().convertIcon(icon);
        if (item.icon != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            convertToBitmap(item.icon).compress(Bitmap.CompressFormat.PNG, 100, baos);
            item.icon_blob = baos.toByteArray();
        }
        item.launchIntent = intent;
        item.launchIntentUri = item.launchIntent.toUri(0);
        item.id = item.packageName + "/" + item.launchIntentUri;
        return item;
    }

    private static Bitmap convertToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
