package com.cloudx.ios17.core;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.UserHandle;

@TargetApi(Build.VERSION_CODES.N)
public class UserManagerCompatVN extends UserManagerCompatVM {

    UserManagerCompatVN(Context context) {
        super(context);
    }

    @Override
    public boolean isQuietModeEnabled(UserHandle user) {
        return mUserManager.isQuietModeEnabled(user);
    }

    @Override
    public boolean isUserUnlocked(UserHandle user) {
        return mUserManager.isUserUnlocked(user);
    }
}
