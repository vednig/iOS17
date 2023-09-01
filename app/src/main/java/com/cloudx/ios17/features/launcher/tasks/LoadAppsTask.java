package com.cloudx.ios17.features.launcher.tasks;

import android.os.AsyncTask;

import com.cloudx.ios17.core.database.model.ApplicationItem;
import com.cloudx.ios17.core.utils.AppUtils;
import com.cloudx.ios17.features.launcher.AppProvider;
import com.cloudx.ios17.core.database.model.ApplicationItem;
import com.cloudx.ios17.core.utils.AppUtils;
import com.cloudx.ios17.features.launcher.AppProvider;
import com.cloudx.ios17.core.database.model.ApplicationItem;
import com.cloudx.ios17.core.utils.AppUtils;
import com.cloudx.ios17.features.launcher.AppProvider;
import com.cloudx.ios17.core.database.model.ApplicationItem;
import com.cloudx.ios17.core.utils.AppUtils;
import com.cloudx.ios17.features.launcher.AppProvider;

import java.util.Map;

public class LoadAppsTask extends AsyncTask<Void, Void, Map<String, ApplicationItem>> {

    private AppProvider mAppProvider;

    public LoadAppsTask() {
        super();
    }

    public void setAppProvider(AppProvider appProvider) {
        this.mAppProvider = appProvider;
    }

    @Override
    protected Map<String, ApplicationItem> doInBackground(Void... voids) {
        return AppUtils.loadAll(mAppProvider.getContext());
    }

    @Override
    protected void onPostExecute(Map<String, ApplicationItem> appItemPair) {
        super.onPostExecute(appItemPair);
        if (mAppProvider != null) {
            mAppProvider.loadAppsOver(appItemPair);
        }
    }
}
