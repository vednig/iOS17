package com.cloudx.ios17.features.shortcuts;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.LauncherApps;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

@TargetApi(Build.VERSION_CODES.O)
public class AddItemActivity extends AppCompatActivity {

    private static final String TAG = "AddItemActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null && getIntent().getAction().equalsIgnoreCase(LauncherApps.ACTION_CONFIRM_PIN_SHORTCUT)) {
            LauncherApps launcherApps = (LauncherApps) getSystemService(Context.LAUNCHER_APPS_SERVICE);
            LauncherApps.PinItemRequest request = launcherApps.getPinItemRequest(getIntent());
            if (request == null) {
                finish();
                return;
            }

            if (request.getRequestType() == LauncherApps.PinItemRequest.REQUEST_TYPE_SHORTCUT) {
                InstallShortcutReceiver.queueShortcut(new ShortcutInfoCompat(request.getShortcutInfo()),
                        this.getApplicationContext());
                request.accept();
                finish();
            }
        }
    }
}
