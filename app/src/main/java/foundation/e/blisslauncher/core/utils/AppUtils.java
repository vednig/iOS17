/*
 * Copyright 2018 /e/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package foundation.e.blisslauncher.core.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserManager;
import androidx.annotation.Nullable;
import foundation.e.blisslauncher.BlissLauncher;
import foundation.e.blisslauncher.R;
import foundation.e.blisslauncher.core.IconsHandler;
import foundation.e.blisslauncher.core.database.model.ApplicationItem;
import foundation.e.blisslauncher.features.launcher.AppProvider;
import timber.log.Timber;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AppUtils {

    private static final String TAG = "AppUtils";
    private static volatile LauncherApps sLauncherApps;

    /** Uses the PackageManager to find all launchable apps. */
    @SuppressLint("CheckResult")
    public static Map<String, ApplicationItem> loadAll(Context context) {

        UserManager manager = (UserManager) context.getSystemService(Context.USER_SERVICE);

        if (sLauncherApps == null) {
            sLauncherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        }
        IconsHandler iconsHandler = BlissLauncher.getApplication(context).getIconsHandler();
        Map<String, ApplicationItem> appArrayMap = new LinkedHashMap<>();

        // Handle multi-profile support introduced in Android 5 (#542)
        for (android.os.UserHandle profile : manager.getUserProfiles()) {
            UserHandle user = new UserHandle(manager.getSerialNumberForUser(profile), profile);
            Timber.tag(TAG).i("totalAppsBefore: %s", sLauncherApps.getActivityList(null, profile).size());
            List<LauncherActivityInfo> infos = sLauncherApps.getActivityList(null, profile);
            for (LauncherActivityInfo activityInfo : infos) {
                ApplicationInfo appInfo = activityInfo.getApplicationInfo();
                if (AppProvider.DISABLED_PACKAGES.contains(appInfo.packageName)) {
                    continue;
                }
                ApplicationItem applicationItem = new ApplicationItem(activityInfo, user);
                applicationItem.icon = iconsHandler.getDrawableIconForPackage(activityInfo, user);
                String componentName = activityInfo.getComponentName().toString();
                applicationItem.appType = iconsHandler.isClock(componentName)
                        ? ApplicationItem.TYPE_CLOCK
                        : (iconsHandler.isCalendar(componentName)
                                ? ApplicationItem.TYPE_CALENDAR
                                : ApplicationItem.TYPE_DEFAULT);
                applicationItem.title = activityInfo.getLabel().toString();
                applicationItem.container = Constants.CONTAINER_DESKTOP;
                if (appInfo.packageName.equalsIgnoreCase("com.generalmagic.magicearth")) {
                    applicationItem.title = context.getString(R.string.app_name_maps);
                }
                applicationItem.packageName = appInfo.packageName;
                appArrayMap.put(applicationItem.id, applicationItem);
            }
        }
        Timber.tag(TAG).i("Total Apps Loaded: %s", appArrayMap.size());
        return appArrayMap;
    }

    @Nullable
    public static String getPackageNameForIntent(Intent intent, PackageManager pm) {
        List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
        if (activities.size() == 0)
            return null;
        ActivityInfo activity = activities.get(0).activityInfo;
        return activity.applicationInfo.packageName;
    }

    public static ApplicationItem createAppItem(Context context, String packageName, UserHandle userHandle) {
        if (AppProvider.DISABLED_PACKAGES.contains(packageName)) {
            return null;
        }
        if (sLauncherApps == null) {
            sLauncherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        }

        IconsHandler iconsHandler = BlissLauncher.getApplication(context).getIconsHandler();

        List<LauncherActivityInfo> launcherActivityInfos = sLauncherApps.getActivityList(packageName,
                userHandle.getRealHandle());
        if (launcherActivityInfos == null || launcherActivityInfos.size() == 0) {
            return null;
        }

        LauncherActivityInfo launcherActivityInfo = launcherActivityInfos.get(0);
        if (launcherActivityInfo != null) {
            ApplicationItem applicationItem = new ApplicationItem(launcherActivityInfo, userHandle);
            ApplicationInfo appInfo = launcherActivityInfo.getApplicationInfo();
            applicationItem.icon = iconsHandler.getDrawableIconForPackage(launcherActivityInfo, userHandle);
            String componentName = launcherActivityInfo.getComponentName().toString();
            applicationItem.appType = iconsHandler.isClock(componentName)
                    ? ApplicationItem.TYPE_CLOCK
                    : (iconsHandler.isCalendar(componentName)
                            ? ApplicationItem.TYPE_CALENDAR
                            : ApplicationItem.TYPE_DEFAULT);
            applicationItem.title = launcherActivityInfo.getLabel().toString();
            applicationItem.container = Constants.CONTAINER_DESKTOP;
            if (appInfo.packageName.equalsIgnoreCase("com.generalmagic.magicearth")) {
                applicationItem.title = context.getString(R.string.app_name_maps);
            }
            applicationItem.packageName = appInfo.packageName;
            return applicationItem;
        }

        return null;
    }
}
