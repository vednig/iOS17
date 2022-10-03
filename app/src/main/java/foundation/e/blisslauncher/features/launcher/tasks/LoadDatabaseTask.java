package foundation.e.blisslauncher.features.launcher.tasks;

import android.os.AsyncTask;
import foundation.e.blisslauncher.core.database.LauncherDB;
import foundation.e.blisslauncher.core.database.model.LauncherItem;
import foundation.e.blisslauncher.core.migrate.Migration;
import foundation.e.blisslauncher.features.launcher.AppProvider;
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
