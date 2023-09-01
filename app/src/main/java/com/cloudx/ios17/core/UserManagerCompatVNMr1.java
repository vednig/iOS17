package com.cloudx.ios17.core;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.N_MR1)
public class UserManagerCompatVNMr1 extends UserManagerCompatVN {

    UserManagerCompatVNMr1(Context context) {
        super(context);
    }

    @Override
    public boolean isDemoUser() {
        return mUserManager.isDemoUser();
    }
}
