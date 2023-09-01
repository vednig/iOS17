package com.cloudx.ios17.core.events;

import com.cloudx.ios17.core.utils.UserHandle;
import com.cloudx.ios17.core.utils.UserHandle;
import com.cloudx.ios17.core.utils.UserHandle;
import com.cloudx.ios17.core.utils.UserHandle;

public class AppAddEvent extends Event {

    private String packageName;
    private UserHandle userHandle;

    public static final int TYPE = 600;

    public AppAddEvent(String packageName, UserHandle userHandle) {
        super(TYPE);
        this.packageName = packageName;
        this.userHandle = userHandle;
    }

    public String getPackageName() {
        return packageName;
    }

    public UserHandle getUserHandle() {
        return userHandle;
    }

    @Override
    public String toString() {
        return "AppAddEvent{" + "packageName='" + packageName + '\'' + ", userHandle=" + userHandle + '}';
    }
}
