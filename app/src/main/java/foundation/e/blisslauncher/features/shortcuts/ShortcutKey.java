package foundation.e.blisslauncher.features.shortcuts;

import android.content.ComponentName;
import android.content.Intent;
import android.os.UserHandle;
import foundation.e.blisslauncher.core.database.model.ShortcutItem;
import foundation.e.blisslauncher.core.utils.ComponentKey;

/**
 * A key that uniquely identifies a shortcut using its package, id, and user
 * handle.
 */
public class ShortcutKey extends ComponentKey {

    public ShortcutKey(String packageName, UserHandle user, String id) {
        // Use the id as the class name.
        super(new ComponentName(packageName, id), user);
    }

    public String getId() {
        return componentName.getClassName();
    }

    public static ShortcutKey fromInfo(ShortcutInfoCompat shortcutInfo) {
        return new ShortcutKey(shortcutInfo.getPackage(), shortcutInfo.getUserHandle(), shortcutInfo.getId());
    }

    public static ShortcutKey fromItem(ShortcutItem shortcutItem) {
        return new ShortcutKey(shortcutItem.packageName, shortcutItem.user.getRealHandle(), shortcutItem.id);
    }

    public static ShortcutKey fromIntent(Intent intent, UserHandle user) {
        String shortcutId = intent.getStringExtra(ShortcutInfoCompat.EXTRA_SHORTCUT_ID);
        return new ShortcutKey(intent.getPackage(), user, shortcutId);
    }
}
