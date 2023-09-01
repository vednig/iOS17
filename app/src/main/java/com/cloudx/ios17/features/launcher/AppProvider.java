package com.cloudx.ios17.features.launcher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.os.UserManager;
import android.provider.MediaStore;
import android.util.LongSparseArray;

import com.cloudx.ios17.BlissLauncher;
import com.cloudx.ios17.core.Utilities;
import com.cloudx.ios17.core.broadcast.PackageAddedRemovedHandler;
import com.cloudx.ios17.core.database.DatabaseManager;
import com.cloudx.ios17.core.database.model.ApplicationItem;
import com.cloudx.ios17.core.database.model.FolderItem;
import com.cloudx.ios17.core.database.model.LauncherItem;
import com.cloudx.ios17.core.database.model.ShortcutItem;
import com.cloudx.ios17.core.executors.AppExecutors;
import com.cloudx.ios17.core.utils.AppUtils;
import com.cloudx.ios17.core.utils.Constants;
import com.cloudx.ios17.core.utils.GraphicsUtil;
import com.cloudx.ios17.core.utils.MultiHashMap;
import com.cloudx.ios17.core.utils.UserHandle;
import com.cloudx.ios17.BlissLauncher;
import com.cloudx.ios17.core.Utilities;
import com.cloudx.ios17.core.broadcast.PackageAddedRemovedHandler;
import com.cloudx.ios17.core.database.DatabaseManager;
import com.cloudx.ios17.core.database.model.ApplicationItem;
import com.cloudx.ios17.core.database.model.FolderItem;
import com.cloudx.ios17.core.database.model.LauncherItem;
import com.cloudx.ios17.core.database.model.ShortcutItem;
import com.cloudx.ios17.core.executors.AppExecutors;
import com.cloudx.ios17.core.utils.AppUtils;
import com.cloudx.ios17.core.utils.Constants;
import com.cloudx.ios17.core.utils.GraphicsUtil;
import com.cloudx.ios17.core.utils.MultiHashMap;
import com.cloudx.ios17.core.utils.UserHandle;
import com.cloudx.ios17.BlissLauncher;
import com.cloudx.ios17.R;
import com.cloudx.ios17.core.Utilities;
import com.cloudx.ios17.core.broadcast.PackageAddedRemovedHandler;
import com.cloudx.ios17.core.database.DatabaseManager;
import com.cloudx.ios17.core.database.model.ApplicationItem;
import com.cloudx.ios17.core.database.model.FolderItem;
import com.cloudx.ios17.core.database.model.LauncherItem;
import com.cloudx.ios17.core.database.model.ShortcutItem;
import com.cloudx.ios17.core.executors.AppExecutors;
import com.cloudx.ios17.core.utils.AppUtils;
import com.cloudx.ios17.core.utils.Constants;
import com.cloudx.ios17.core.utils.GraphicsUtil;
import com.cloudx.ios17.core.utils.MultiHashMap;
import com.cloudx.ios17.core.utils.UserHandle;
import com.cloudx.ios17.features.launcher.tasks.LoadAppsTask;
import com.cloudx.ios17.features.launcher.tasks.LoadDatabaseTask;
import com.cloudx.ios17.features.launcher.tasks.LoadShortcutTask;
import com.cloudx.ios17.features.shortcuts.DeepShortcutManager;
import com.cloudx.ios17.features.shortcuts.ShortcutInfoCompat;
import com.cloudx.ios17.BlissLauncher;
import com.cloudx.ios17.core.Utilities;
import com.cloudx.ios17.core.broadcast.PackageAddedRemovedHandler;
import com.cloudx.ios17.core.database.DatabaseManager;
import com.cloudx.ios17.core.database.model.ApplicationItem;
import com.cloudx.ios17.core.database.model.FolderItem;
import com.cloudx.ios17.core.database.model.LauncherItem;
import com.cloudx.ios17.core.database.model.ShortcutItem;
import com.cloudx.ios17.core.executors.AppExecutors;
import com.cloudx.ios17.core.utils.AppUtils;
import com.cloudx.ios17.core.utils.Constants;
import com.cloudx.ios17.core.utils.GraphicsUtil;
import com.cloudx.ios17.core.utils.MultiHashMap;
import com.cloudx.ios17.core.utils.UserHandle;
import timber.log.Timber;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

// TODO: Find better solution instead of excessively using volatile and synchronized.
//  - and use RxJava instead of bad async tasks.
public class AppProvider {

    /** Represents all applications that is to be shown in Launcher */
    List<LauncherItem> mLauncherItems;

    /** Represents networkItems stored in database. */
    private List<LauncherItem> mDatabaseItems;

    /** Represents all applications installed in device. */
    private Map<String, ApplicationItem> mApplicationItems;

    /** Represents all shortcuts which user has created. */
    private Map<String, ShortcutInfoCompat> mShortcutInfoCompats;

    private boolean appsLoaded = false;
    private boolean shortcutsLoaded = false;
    private boolean databaseLoaded = false;

    private AppsRepository mAppsRepository;

    private static final String MICROG_PACKAGE = "com.google.android.gms";
    private static final String MUPDF_PACKAGE = "com.artifex.mupdf.mini.app";
    private static final String PDF_VIEWER_PACKAGE = "foundation.e.pdfviewer";
    private static final String OPENKEYCHAIN_PACKAGE = "org.sufficientlysecure.keychain";
    private static final String LIBREOFFICE_PACKAGE = "org.documentfoundation.libreoffice";
    private static final String LIBREOFFICE_PACKAGE2 = "org.example.libreoffice";
    private static final String SIM_TOOLKIT = "com.android.stk";

    public static HashSet<String> DISABLED_PACKAGES = new HashSet<>();

    private MultiHashMap<UserHandle, String> pendingPackages = new MultiHashMap<>();

    static {
        DISABLED_PACKAGES.add(MICROG_PACKAGE);
        DISABLED_PACKAGES.add(MUPDF_PACKAGE);
        DISABLED_PACKAGES.add(PDF_VIEWER_PACKAGE);
        DISABLED_PACKAGES.add(OPENKEYCHAIN_PACKAGE);
        DISABLED_PACKAGES.add(LIBREOFFICE_PACKAGE);
        DISABLED_PACKAGES.add(LIBREOFFICE_PACKAGE2);
        DISABLED_PACKAGES.add(SIM_TOOLKIT);
    }

    private static final String TAG = "AppProvider";
    private Context mContext;
    private static AppProvider sInstance;
    private boolean isLoading;
    private boolean mStopped;
    private boolean isSdCardReady;

    private AppsRepository appsRepository;

    private AppProvider(Context context) {
        this.mContext = context;
        this.appsRepository = AppsRepository.getAppsRepository();
        isLoading = false;
        initialise();
    }

    private void initialise() {
        final UserManager manager = (UserManager) mContext.getSystemService(Context.USER_SERVICE);
        assert manager != null;

        final LauncherApps launcher = (LauncherApps) mContext.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        assert launcher != null;

        launcher.registerCallback(new LauncherApps.Callback() {
            @Override
            public void onPackageRemoved(String packageName, android.os.UserHandle user) {
                if (packageName.equalsIgnoreCase(MICROG_PACKAGE) || packageName.equalsIgnoreCase(MUPDF_PACKAGE)) {
                    return;
                }

                PackageAddedRemovedHandler.handleEvent(mContext, "android.intent.action.PACKAGE_REMOVED", packageName,
                        new UserHandle(manager.getSerialNumberForUser(user), user), false);
            }

            @Override
            public void onPackageAdded(String packageName, android.os.UserHandle user) {
                if (packageName.equalsIgnoreCase(MICROG_PACKAGE) || packageName.equalsIgnoreCase(MUPDF_PACKAGE)) {
                    return;
                }

                PackageAddedRemovedHandler.handleEvent(mContext, "android.intent.action.PACKAGE_ADDED", packageName,
                        new UserHandle(manager.getSerialNumberForUser(user), user), false);
            }

            @Override
            public void onPackageChanged(String packageName, android.os.UserHandle user) {
                if (packageName.equalsIgnoreCase(MICROG_PACKAGE) || packageName.equalsIgnoreCase(MUPDF_PACKAGE)) {
                    return;
                }

                PackageAddedRemovedHandler.handleEvent(mContext, "android.intent.action.PACKAGE_CHANGED", packageName,
                        new UserHandle(manager.getSerialNumberForUser(user), user), true);
            }

            @Override
            public void onPackagesAvailable(String[] packageNames, android.os.UserHandle user, boolean replacing) {
                Timber.tag(TAG).d("onPackagesAvailable() called with: packageNames = [" + Arrays.toString(packageNames)
                        + "], user = [" + user + "], replacing = [" + replacing + "]");
                for (String packageName : packageNames) {
                    PackageAddedRemovedHandler.handleEvent(mContext, "android.intent.action.MEDIA_MOUNTED", packageName,
                            new UserHandle(manager.getSerialNumberForUser(user), user), false);
                }
            }

            @Override
            public void onPackagesUnavailable(String[] packageNames, android.os.UserHandle user, boolean replacing) {
                Timber.tag(TAG).d("onPackagesUnavailable() called with: packageNames = ["
                        + Arrays.toString(packageNames) + "], user = [" + user + "], replacing = [" + replacing + "]");
                PackageAddedRemovedHandler.handleEvent(mContext, "android.intent.action.MEDIA_UNMOUNTED", null,
                        new UserHandle(manager.getSerialNumberForUser(user), user), false);
            }

            @Override
            public void onPackagesSuspended(String[] packageNames, android.os.UserHandle user) {
                Timber.tag(TAG).d("onPackagesSuspended() called with: packageNames = [" + Arrays.toString(packageNames)
                        + "], user = [" + user + "]");
            }

            @Override
            public void onPackagesUnsuspended(String[] packageNames, android.os.UserHandle user) {
                super.onPackagesUnsuspended(packageNames, user);
                Timber.tag(TAG).d("onPackagesUnsuspended() called with: packageNames = ["
                        + Arrays.toString(packageNames) + "], user = [" + user + "]");
            }
        });

        mAppsRepository = AppsRepository.getAppsRepository();
    }

    public static AppProvider getInstance(Context context) {
        if (sInstance == null) {
            synchronized (AppProvider.class) {
                if (sInstance == null) {
                    sInstance = new AppProvider(context);
                    sInstance.reload(true);
                }
            }
        }
        return sInstance;
    }

    public Context getContext() {
        return mContext;
    }

    public synchronized void reload(boolean force) {
        Timber.tag(TAG).d("reload() called");

        isSdCardReady = Utilities.isBootCompleted();

        if (!force && mLauncherItems != null && mLauncherItems.size() > 0) {
            mAppsRepository.updateAppsRelay(mLauncherItems);
        }

        initializeAppLoading(new LoadAppsTask());
        if (Utilities.ATLEAST_OREO) {
            initializeShortcutsLoading(new LoadShortcutTask());
        } else {
            shortcutsLoaded = true; // will be loaded from database automatically.
        }
        initializeDatabaseLoading(new LoadDatabaseTask());
    }

    private synchronized void initializeAppLoading(LoadAppsTask loader) {
        Timber.tag(TAG).d("initializeAppLoading() called with: loader = [" + loader + "]");
        appsLoaded = false;
        loader.setAppProvider(this);
        loader.executeOnExecutor(AppExecutors.getInstance().appIO());
    }

    private synchronized void initializeShortcutsLoading(LoadShortcutTask loader) {
        Timber.tag(TAG).d("initializeShortcutsLoading() called with: loader = [" + loader + "]");
        shortcutsLoaded = false;
        loader.setAppProvider(this);
        loader.executeOnExecutor(AppExecutors.getInstance().shortcutIO());
    }

    private synchronized void initializeDatabaseLoading(LoadDatabaseTask loader) {
        Timber.tag(TAG).d("initializeDatabaseLoading() called with: loader = [" + loader + "]");
        databaseLoaded = false;
        loader.setAppProvider(this);
        loader.executeOnExecutor(AppExecutors.getInstance().diskIO());
    }

    public synchronized void loadAppsOver(Map<String, ApplicationItem> appItemsPair) {
        Timber.tag(TAG).d("loadAppsOver() called %s", mStopped);
        mApplicationItems = appItemsPair;
        appsLoaded = true;
        handleAllProviderLoaded();
    }

    public synchronized void loadShortcutsOver(Map<String, ShortcutInfoCompat> shortcuts) {
        Timber.tag(TAG).d("loadShortcutsOver() called with: shortcuts = [" + shortcuts + "]" + mStopped);
        mShortcutInfoCompats = shortcuts;
        shortcutsLoaded = true;
        handleAllProviderLoaded();
    }

    public synchronized void loadDatabaseOver(List<LauncherItem> databaseItems) {
        Timber.tag(TAG).d("loadDatabaseOver() called with: databaseItems = [" + Thread.currentThread().getName() + "]"
                + mStopped);
        this.mDatabaseItems = databaseItems;
        databaseLoaded = true;
        handleAllProviderLoaded();
    }

    private synchronized void handleAllProviderLoaded() {
        if (appsLoaded && shortcutsLoaded && databaseLoaded) {
            if (mDatabaseItems == null || mDatabaseItems.size() <= 0) {
                mLauncherItems = prepareDefaultLauncherItems();
            } else {
                mLauncherItems = prepareLauncherItems();
            }
            mAppsRepository.updateAppsRelay(mLauncherItems);
        }
    }

    private List<LauncherItem> prepareLauncherItems() {
        Timber.tag(TAG).d("prepareLauncherItems() called");

        /** Indices of folder in {@link #mLauncherItems}. */
        LongSparseArray<Integer> foldersIndex = new LongSparseArray<>();
        List<LauncherItem> mLauncherItems = new ArrayList<>();
        Collection<ApplicationItem> applicationItems = mApplicationItems.values();

        Timber.tag(TAG).i("Total number of apps: %s", applicationItems.size());
        Timber.tag(TAG).i("Total number of items in database: %s", mDatabaseItems.size());
        for (LauncherItem databaseItem : mDatabaseItems) {
            if (databaseItem.itemType == Constants.ITEM_TYPE_APPLICATION) {
                ApplicationItem applicationItem = mApplicationItems.get(databaseItem.id);
                if (applicationItem == null) {
                    UserHandle userHandle = new UserHandle();
                    if ((isAppOnSdcard(databaseItem.packageName, userHandle) || !isSdCardReady)
                            && !DISABLED_PACKAGES.contains(databaseItem.packageName)) {
                        Timber.tag(TAG).d("Missing package: %s", databaseItem.packageName);
                        Timber.tag(TAG).d("Is App on Sdcard %s", isAppOnSdcard(databaseItem.packageName, userHandle));
                        Timber.tag(TAG).d("Is Sdcard ready %s", isSdCardReady);

                        pendingPackages.addToList(userHandle, databaseItem.packageName);
                        applicationItem = new ApplicationItem();
                        applicationItem.id = databaseItem.id;
                        applicationItem.title = databaseItem.title;
                        applicationItem.user = userHandle;
                        applicationItem.componentName = databaseItem.getTargetComponent();
                        applicationItem.packageName = databaseItem.packageName;
                        applicationItem.icon = getContext().getDrawable(R.drawable.default_icon);
                        applicationItem.isDisabled = true;
                    } else {
                        DatabaseManager.getManager(mContext).removeLauncherItem(databaseItem.id);
                        continue;
                    }
                }

                applicationItem.container = databaseItem.container;
                applicationItem.screenId = databaseItem.screenId;
                applicationItem.cell = databaseItem.cell;
                applicationItem.keyId = databaseItem.keyId;
                if (applicationItem.container == Constants.CONTAINER_DESKTOP
                        || applicationItem.container == Constants.CONTAINER_HOTSEAT) {
                    mLauncherItems.add(applicationItem);
                } else {
                    Integer index = foldersIndex.get(applicationItem.container);
                    if (index != null) {
                        FolderItem folderItem = (FolderItem) mLauncherItems.get(index);
                        folderItem.items.add(applicationItem);
                    } else {
                        Timber.tag("AppProvider").e("folder not found for item: %s", applicationItem.id);
                    }
                }
            } else if (databaseItem.itemType == Constants.ITEM_TYPE_SHORTCUT) {
                ShortcutItem shortcutItem;
                if (Utilities.ATLEAST_OREO) {
                    shortcutItem = prepareShortcutForOreo(databaseItem);
                } else {
                    shortcutItem = prepareShortcutForNougat(databaseItem);
                }

                if (shortcutItem == null) {
                    DatabaseManager.getManager(mContext).removeLauncherItem(databaseItem.id);
                    continue;
                }

                if (shortcutItem.container == Constants.CONTAINER_DESKTOP
                        || shortcutItem.container == Constants.CONTAINER_HOTSEAT) {
                    mLauncherItems.add(shortcutItem);
                } else {
                    FolderItem folderItem = (FolderItem) mLauncherItems.get(foldersIndex.get(shortcutItem.container));
                    if (folderItem.items == null) {
                        folderItem.items = new ArrayList<>();
                    }
                    folderItem.items.add(shortcutItem);
                }
            } else if (databaseItem.itemType == Constants.ITEM_TYPE_FOLDER) {
                FolderItem folderItem = new FolderItem();
                folderItem.id = databaseItem.id;
                folderItem.title = databaseItem.title;
                folderItem.container = databaseItem.container;
                folderItem.cell = databaseItem.cell;
                folderItem.items = new ArrayList<>();
                folderItem.screenId = databaseItem.screenId;
                foldersIndex.put(Long.parseLong(folderItem.id), mLauncherItems.size());
                mLauncherItems.add(folderItem);
            }
        }

        List<Integer> folderItemsIndex = new ArrayList<>();
        for (int i = 0; i < foldersIndex.size(); i++) {
            int itemIndex = foldersIndex.get(foldersIndex.keyAt(i));
            folderItemsIndex.add(itemIndex);
        }
        Collections.sort(folderItemsIndex);
        for (int i = folderItemsIndex.size() - 1; i >= 0; i--) {
            int itemIndex = folderItemsIndex.get(i);
            FolderItem folderItem = (FolderItem) mLauncherItems.get(itemIndex);
            if (folderItem.items == null || folderItem.items.size() == 0) {
                DatabaseManager.getManager(mContext).removeLauncherItem(folderItem.id);
                mLauncherItems.remove(itemIndex);
            } else {
                folderItem.icon = new GraphicsUtil(mContext).generateFolderIcon(mContext, folderItem);
            }
        }

        applicationItems.removeAll(mDatabaseItems);
        List<ApplicationItem> mutableList = new ArrayList<>(applicationItems);
        Collections.sort(mutableList, (app1, app2) -> {
            Collator collator = Collator.getInstance();
            return collator.compare(app1.title.toString(), app2.title.toString());
        });
        mLauncherItems.addAll(mutableList);
        return mLauncherItems;
    }

    private boolean isAppOnSdcard(String packageName, UserHandle userHandle) {
        ApplicationInfo info = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                info = ((LauncherApps) mContext.getSystemService(Context.LAUNCHER_APPS_SERVICE)).getApplicationInfo(
                        packageName, PackageManager.MATCH_UNINSTALLED_PACKAGES, userHandle.getRealHandle());
                return info != null && (info.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0;
            } else {
                info = getContext().getPackageManager().getApplicationInfo(packageName,
                        PackageManager.MATCH_UNINSTALLED_PACKAGES);
                return info != null && info.enabled;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private ShortcutItem prepareShortcutForNougat(LauncherItem databaseItem) {
        ShortcutItem shortcutItem = new ShortcutItem();
        shortcutItem.id = databaseItem.id;
        shortcutItem.packageName = databaseItem.packageName;
        shortcutItem.title = databaseItem.title.toString();
        shortcutItem.icon_blob = databaseItem.icon_blob;
        Bitmap bitmap = BitmapFactory.decodeByteArray(databaseItem.icon_blob, 0, databaseItem.icon_blob.length);
        shortcutItem.icon = new BitmapDrawable(mContext.getResources(), bitmap);
        shortcutItem.launchIntent = databaseItem.getIntent();
        shortcutItem.launchIntentUri = databaseItem.launchIntentUri;
        shortcutItem.container = databaseItem.container;
        shortcutItem.screenId = databaseItem.screenId;
        shortcutItem.cell = databaseItem.cell;
        shortcutItem.user = new UserHandle();
        return shortcutItem;
    }

    private ShortcutItem prepareShortcutForOreo(LauncherItem databaseItem) {
        ShortcutInfoCompat info = mShortcutInfoCompats.get(databaseItem.id);
        if (info == null) {
            Timber.tag(TAG).d("prepareShortcutForOreo() called with: databaseItem = [" + databaseItem + "]");
            return null;
        }

        ShortcutItem shortcutItem = new ShortcutItem();
        shortcutItem.id = info.getId();
        shortcutItem.packageName = info.getPackage();
        shortcutItem.title = info.getShortLabel().toString();
        Drawable icon = DeepShortcutManager.getInstance(mContext).getShortcutIconDrawable(info,
                mContext.getResources().getDisplayMetrics().densityDpi);
        shortcutItem.icon = BlissLauncher.getApplication(mContext).getIconsHandler().convertIcon(icon);
        shortcutItem.launchIntent = info.makeIntent();
        shortcutItem.container = databaseItem.container;
        shortcutItem.screenId = databaseItem.screenId;
        shortcutItem.cell = databaseItem.cell;
        shortcutItem.user = new UserHandle();
        return shortcutItem;
    }

    private List<LauncherItem> prepareDefaultLauncherItems() {
        List<LauncherItem> mLauncherItems = new ArrayList<>();
        List<LauncherItem> pinnedItems = new ArrayList<>();
        PackageManager pm = mContext.getPackageManager();
        Intent[] intents = {new Intent(Intent.ACTION_DIAL), new Intent(Intent.ACTION_VIEW, Uri.parse("sms:")),
                new Intent(Intent.ACTION_VIEW, Uri.parse("http:")), new Intent(MediaStore.ACTION_IMAGE_CAPTURE)};
        for (int i = 0; i < intents.length; i++) {
            String packageName = AppUtils.getPackageNameForIntent(intents[i], pm);
            LauncherApps launcherApps = (LauncherApps) mContext.getSystemService(Context.LAUNCHER_APPS_SERVICE);
            List<LauncherActivityInfo> list = launcherApps.getActivityList(packageName, Process.myUserHandle());
            for (LauncherActivityInfo launcherActivityInfo : list) {
                ApplicationItem applicationItem = mApplicationItems
                        .get(launcherActivityInfo.getComponentName().flattenToString());
                if (applicationItem != null) {
                    applicationItem.container = Constants.CONTAINER_HOTSEAT;
                    applicationItem.cell = i;
                    pinnedItems.add(applicationItem);
                    break;
                }
            }
        }

        for (Map.Entry<String, ApplicationItem> stringApplicationItemEntry : mApplicationItems.entrySet()) {
            if (!pinnedItems.contains(stringApplicationItemEntry.getValue())) {
                mLauncherItems.add(stringApplicationItemEntry.getValue());
            }
        }

        mLauncherItems.sort((app1, app2) -> {
            Collator collator = Collator.getInstance();
            return collator.compare(app1.title.toString(), app2.title.toString());
        });

        mLauncherItems.addAll(pinnedItems);
        return mLauncherItems;
    }

    public AppsRepository getAppsRepository() {
        return appsRepository;
    }

    public void clear() {
        this.sInstance = null;
        mLauncherItems = new ArrayList<>();
        mAppsRepository.updateAppsRelay(Collections.emptyList());
    }

    public synchronized boolean isRunning() {
        return !mStopped;
    }
}
