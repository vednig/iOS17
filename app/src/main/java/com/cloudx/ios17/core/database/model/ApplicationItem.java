package com.cloudx.ios17.core.database.model;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;

import com.cloudx.ios17.core.utils.Constants;
import com.cloudx.ios17.core.utils.UserHandle;
import com.cloudx.ios17.core.utils.Constants;
import com.cloudx.ios17.core.utils.UserHandle;
import com.cloudx.ios17.core.utils.Constants;
import com.cloudx.ios17.core.utils.UserHandle;
import com.cloudx.ios17.core.utils.Constants;
import com.cloudx.ios17.core.utils.UserHandle;

public class ApplicationItem extends LauncherItem {

    public static final int FLAG_SYSTEM_UNKNOWN = 0;
    public static final int FLAG_SYSTEM_YES = 1 << 0;
    public static final int FLAG_SYSTEM_NO = 1 << 1;

    public ComponentName componentName;

    /** Indicates if the app is a system app or not. */
    public int isSystemApp;

    public static final int TYPE_CLOCK = 745;
    public static final int TYPE_CALENDAR = 746;
    public static final int TYPE_DEFAULT = 111;

    public boolean isDisabled = false;

    /**
     * Indicates the type of app item ie. Clock or Calendar (in case of none, It
     * will be )
     */
    public int appType;

    public ApplicationItem() {
        itemType = Constants.ITEM_TYPE_APPLICATION;
    }

    /** Must not hold the Context. */
    public ApplicationItem(LauncherActivityInfo info, UserHandle user) {
        itemType = Constants.ITEM_TYPE_APPLICATION;
        this.componentName = info.getComponentName();
        this.user = user;
        this.id = user.addUserSuffixToString(this.componentName.flattenToString(), '/');
        this.container = NO_ID;

        launchIntent = makeLaunchIntent(info);

        isSystemApp = (info.getApplicationInfo().flags & ApplicationInfo.FLAG_SYSTEM) == 0
                ? FLAG_SYSTEM_NO
                : FLAG_SYSTEM_YES;
    }

    public static Intent makeLaunchIntent(LauncherActivityInfo info) {
        return makeLaunchIntent(info.getComponentName());
    }

    public static Intent makeLaunchIntent(ComponentName cn) {
        return new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setComponent(cn)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
    }
}
