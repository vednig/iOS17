package com.cloudx.ios17.features.shortcuts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cloudx.ios17.core.database.DatabaseManager;
import com.cloudx.ios17.core.database.DatabaseManager;
import com.cloudx.ios17.core.database.DatabaseManager;
import com.cloudx.ios17.core.database.DatabaseManager;

public class UninstallShortcutReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent data) {
        String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        DatabaseManager databaseManager = DatabaseManager.getManager(context);
        databaseManager.removeShortcut(name);
    }
}
