package com.cloudx.ios17.features.launcher;

import com.cloudx.ios17.core.database.model.LauncherItem;
import com.jakewharton.rxrelay2.BehaviorRelay;

import com.cloudx.ios17.core.database.model.LauncherItem;
import com.cloudx.ios17.core.database.model.LauncherItem;
import com.cloudx.ios17.core.database.model.LauncherItem;

import java.util.List;

public class AppsRepository {

    private static final String TAG = "AppsRepository";
    private BehaviorRelay<List<LauncherItem>> appsRelay;

    private static AppsRepository sAppsRepository;

    private AppsRepository() {
        appsRelay = BehaviorRelay.create();
    }

    public static AppsRepository getAppsRepository() {
        if (sAppsRepository == null) {
            sAppsRepository = new AppsRepository();
        }
        return sAppsRepository;
    }

    public void clearAll() {
        appsRelay = BehaviorRelay.create();
    }

    public void updateAppsRelay(List<LauncherItem> launcherItems) {
        this.appsRelay.accept(launcherItems);
    }

    public BehaviorRelay<List<LauncherItem>> getAppsRelay() {
        return appsRelay;
    }
}
