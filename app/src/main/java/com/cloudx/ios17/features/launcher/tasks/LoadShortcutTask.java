package com.cloudx.ios17.features.launcher.tasks;

import android.os.AsyncTask;
import android.os.Process;

import com.cloudx.ios17.features.launcher.AppProvider;
import com.cloudx.ios17.features.launcher.AppProvider;
import com.cloudx.ios17.features.launcher.AppProvider;
import com.cloudx.ios17.features.shortcuts.DeepShortcutManager;
import com.cloudx.ios17.features.shortcuts.ShortcutInfoCompat;
import com.cloudx.ios17.features.launcher.AppProvider;
import timber.log.Timber;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadShortcutTask extends AsyncTask<Void, Void, Map<String, ShortcutInfoCompat>> {

    private AppProvider mAppProvider;

    private static final String TAG = "LoadShortcutTask";

    public LoadShortcutTask() {
        super();
    }

    public void setAppProvider(AppProvider appProvider) {
        this.mAppProvider = appProvider;
    }

    @Override
    protected Map<String, ShortcutInfoCompat> doInBackground(Void... voids) {
        List<ShortcutInfoCompat> list = DeepShortcutManager.getInstance(mAppProvider.getContext())
                .queryForPinnedShortcuts(null, Process.myUserHandle());
        Timber.tag(TAG).i("doInBackground: %s", list.size());
        Map<String, ShortcutInfoCompat> shortcutInfoMap = new HashMap<>();
        for (ShortcutInfoCompat shortcutInfoCompat : list) {
            shortcutInfoMap.put(shortcutInfoCompat.getId(), shortcutInfoCompat);
        }
        return shortcutInfoMap;
    }

    @Override
    protected void onPostExecute(Map<String, ShortcutInfoCompat> shortcuts) {
        super.onPostExecute(shortcuts);
        if (mAppProvider != null) {
            mAppProvider.loadShortcutsOver(shortcuts);
        }
    }
}
