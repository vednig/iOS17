package com.cloudx.ios17.features.launcher.tasks;

import android.os.AsyncTask;

import com.cloudx.ios17.core.database.LauncherDB;
import com.cloudx.ios17.core.database.model.LauncherItem;
import com.cloudx.ios17.core.migrate.Migration;
import com.cloudx.ios17.features.launcher.AppProvider;
import com.cloudx.ios17.features.widgets.WidgetMigration;
import com.cloudx.ios17.core.database.LauncherDB;
import com.cloudx.ios17.core.database.model.LauncherItem;
import com.cloudx.ios17.core.migrate.Migration;
import com.cloudx.ios17.features.launcher.AppProvider;
import com.cloudx.ios17.features.widgets.WidgetMigration;
import com.cloudx.ios17.core.database.LauncherDB;
import com.cloudx.ios17.core.database.model.LauncherItem;
import com.cloudx.ios17.core.migrate.Migration;
import com.cloudx.ios17.features.launcher.AppProvider;
import com.cloudx.ios17.features.widgets.WidgetMigration;
import com.cloudx.ios17.core.database.LauncherDB;
import com.cloudx.ios17.core.database.model.LauncherItem;
import com.cloudx.ios17.core.migrate.Migration;
import com.cloudx.ios17.features.launcher.AppProvider;
import com.cloudx.ios17.features.widgets.WidgetMigration;

import java.util.List;

public class LoadDatabaseTask extends AsyncTask<Void, Void, List<LauncherItem>> {

    private AppProvider mAppProvider;

    public LoadDatabaseTask() {
        super();
    }

    public void setAppProvider(AppProvider appProvider) {
        this.mAppProvider = appProvider;
    }

    @Override
    protected List<LauncherItem> doInBackground(Void... voids) {
        Migration.migrateSafely(mAppProvider.getContext());
        WidgetMigration.migrateAdvancedPrivacy(mAppProvider.getContext());
        return LauncherDB.getDatabase(mAppProvider.getContext()).launcherDao().getAllItems();
    }

    @Override
    protected void onPostExecute(List<LauncherItem> launcherItems) {
        super.onPostExecute(launcherItems);
        if (mAppProvider != null) {
            mAppProvider.loadDatabaseOver(launcherItems);
        }
    }
}
