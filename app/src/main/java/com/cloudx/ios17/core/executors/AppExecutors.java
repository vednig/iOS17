package com.cloudx.ios17.core.executors;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppExecutors {
    private static final AppExecutors ourInstance = new AppExecutors();
    private ExecutorService diskExecutor;
    private ExecutorService appExecutor;
    private Executor searchExecutor;
    private ExecutorService shortcutExecutor;

    public static AppExecutors getInstance() {
        return ourInstance;
    }

    private AppExecutors() {
        diskExecutor = Executors.newSingleThreadExecutor();
        appExecutor = Executors.newSingleThreadExecutor();
        shortcutExecutor = Executors.newSingleThreadExecutor();
    }

    public ExecutorService diskIO() {
        return diskExecutor;
    }

    public ExecutorService appIO() {
        return appExecutor;
    }

    public ExecutorService shortcutIO() {
        return shortcutExecutor;
    }
}
