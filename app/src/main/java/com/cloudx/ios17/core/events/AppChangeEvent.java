package com.cloudx.ios17.core.events;

import com.cloudx.ios17.core.utils.UserHandle;
import com.cloudx.ios17.core.utils.UserHandle;
import com.cloudx.ios17.core.utils.UserHandle;
import com.cloudx.ios17.core.utils.UserHandle;

public class AppChangeEvent extends Event {

    private String packageName;
    private UserHandle userHandle;

    public static final int TYPE = 601;

    public AppChangeEvent(String packageName, UserHandle userHandle) {
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
}
