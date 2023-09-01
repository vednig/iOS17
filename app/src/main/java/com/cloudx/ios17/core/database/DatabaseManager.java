package com.cloudx.ios17.core.database;

import android.content.Context;
import android.widget.GridLayout;

import com.cloudx.ios17.core.utils.Constants;
import com.cloudx.ios17.core.utils.Constants;
import com.cloudx.ios17.core.customviews.BlissFrameLayout;
import com.cloudx.ios17.core.database.daos.WidgetDao;
import com.cloudx.ios17.core.database.model.FolderItem;
import com.cloudx.ios17.core.database.model.LauncherItem;
import com.cloudx.ios17.core.database.model.WidgetItem;
import com.cloudx.ios17.core.executors.AppExecutors;
import com.cloudx.ios17.core.utils.Constants;
import com.cloudx.ios17.core.utils.Constants;
import io.reactivex.Single;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatabaseManager {

    private AppExecutors mAppExecutors;

    private static final String TAG = "DatabaseManager";

    private static volatile DatabaseManager INSTANCE;
    private Context mContext;

    private DatabaseManager(Context context) {
        this.mContext = context;
        mAppExecutors = AppExecutors.getInstance();
    }

    public static DatabaseManager getManager(Context context) {
        if (INSTANCE == null) {
            synchronized (DatabaseManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DatabaseManager(context);
                }
            }
        }
        return INSTANCE;
    }

    public void removeLauncherItem(String itemId) {
        mAppExecutors.diskIO().execute(() -> LauncherDB.getDatabase(mContext).launcherDao().delete(itemId));
    }

    public void saveLayouts(List<GridLayout> pages, GridLayout dock) {
        List<GridLayout> tempPages = pages;
        GridLayout tempDock = dock;
        mAppExecutors.diskIO().execute(() -> saveLauncherItems(tempPages, tempDock));
    }

    private void saveLauncherItems(final List<GridLayout> pages, final GridLayout dock) {
        List<LauncherItem> items = new ArrayList<>();
        for (int i = 0; i < dock.getChildCount(); i++) {
            LauncherItem launcherItem = ((BlissFrameLayout) dock.getChildAt(i)).getLauncherItem();
            if (launcherItem.itemType == Constants.ITEM_TYPE_FOLDER) {
                FolderItem folderItem = (FolderItem) launcherItem;
                folderItem.screenId = -1;
                folderItem.cell = i;
                folderItem.container = Constants.CONTAINER_HOTSEAT;
                items.add(launcherItem);

                for (int j = 0; j < folderItem.items.size(); j++) {
                    LauncherItem item = folderItem.items.get(j);
                    item.screenId = -1;
                    item.container = Long.parseLong(folderItem.id);
                    item.cell = j;
                    items.add(item);
                }
            } else {
                launcherItem.screenId = -1;
                launcherItem.container = Constants.CONTAINER_HOTSEAT;
                launcherItem.cell = i;
                items.add(launcherItem);
            }
        }

        for (int i = 0; i < pages.size(); i++) {
            GridLayout gridLayout = pages.get(i);
            for (int j = 0; j < gridLayout.getChildCount(); j++) {
                LauncherItem launcherItem = ((BlissFrameLayout) gridLayout.getChildAt(j)).getLauncherItem();
                if (launcherItem.itemType == Constants.ITEM_TYPE_FOLDER) {
                    FolderItem folderItem = (FolderItem) launcherItem;
                    folderItem.screenId = i;
                    folderItem.cell = j;
                    folderItem.container = Constants.CONTAINER_DESKTOP;
                    items.add(folderItem);
                    for (int k = 0; k < folderItem.items.size(); k++) {
                        LauncherItem item = folderItem.items.get(k);
                        item.screenId = -1;
                        item.container = Long.parseLong(folderItem.id);
                        item.cell = k;
                        items.add(item);
                    }
                } else {
                    launcherItem.screenId = i;
                    launcherItem.container = Constants.CONTAINER_DESKTOP;
                    launcherItem.cell = j;
                    items.add(launcherItem);
                }
            }
        }
        LauncherDB.getDatabase(mContext).launcherDao().insertAll(items);
    }

    public void removeShortcut(String name) {
        mAppExecutors.diskIO().execute(() -> LauncherDB.getDatabase(mContext).launcherDao().deleteShortcut(name));
    }

    // Already invoked in a disk io thread, so no need to execute in separate thread
    // here.
    public void migrateComponent(String old_component_name, String new_component_name) {
        LauncherDB.getDatabase(mContext).launcherDao().delete(new_component_name);
        LauncherDB.getDatabase(mContext).launcherDao().updateComponent(old_component_name, new_component_name);
    }

    public void insertWidget(WidgetItem widgetItem) {
        mAppExecutors.diskIO().execute(() -> LauncherDB.getDatabase(mContext).widgetDao().insert(widgetItem));
    }

    public void saveWidgetHeight(int id, int height) {
        mAppExecutors.diskIO().execute(() -> LauncherDB.getDatabase(mContext).widgetDao().updateHeight(id, height));
    }

    public void removeWidget(int id) {
        mAppExecutors.diskIO().execute(() -> LauncherDB.getDatabase(mContext).widgetDao().delete(id));
    }

    public Single<List<WidgetItem>> getWidgets(int[] widgetIds) {
        return Single.defer(() -> {
            Set<Integer> missingIds = new HashSet<>();
            for (int widgetId : widgetIds) {
                missingIds.add(widgetId);
            }
            WidgetDao widgetDao = LauncherDB.getDatabase(mContext).widgetDao();
            List<WidgetItem> widgets = widgetDao.getAll();
            for (WidgetItem widget : widgets) {
                missingIds.remove(widget.id);
            }
            for (int id : missingIds) {
                WidgetItem item = new WidgetItem(id);
                widgets.add(item);
                widgetDao.insert(item);
            }
            widgets.sort(
                    Comparator.<WidgetItem>comparingInt(widget -> widget.order).thenComparingInt(widget -> widget.id));
            return Single.just(widgets);
        });
    }
}
