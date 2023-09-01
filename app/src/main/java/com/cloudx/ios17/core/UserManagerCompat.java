package com.cloudx.ios17.core;

import android.content.Context;
import android.os.UserHandle;
import java.util.List;

public abstract class UserManagerCompat {
    protected UserManagerCompat() {
    }

    private static final Object sInstanceLock = new Object();
    private static UserManagerCompat sInstance;

    public static UserManagerCompat getInstance(Context context) {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                if (Utilities.ATLEAST_NOUGAT_MR1) {
                    sInstance = new UserManagerCompatVNMr1(context.getApplicationContext());
                } else if (Utilities.ATLEAST_NOUGAT) {
                    sInstance = new UserManagerCompatVN(context.getApplicationContext());
                } else if (Utilities.ATLEAST_MARSHMALLOW) {
                    sInstance = new UserManagerCompatVM(context.getApplicationContext());
                } else {
                    sInstance = new UserManagerCompatVL(context.getApplicationContext());
                }
            }
            return sInstance;
        }
    }

    /** Creates a cache for users. */
    public abstract void enableAndResetCache();

    public abstract List<UserHandle> getUserProfiles();

    public abstract long getSerialNumberForUser(UserHandle user);

    public abstract UserHandle getUserForSerialNumber(long serialNumber);

    public abstract CharSequence getBadgedLabelForUser(CharSequence label, UserHandle user);

    public abstract long getUserCreationTime(UserHandle user);

    public abstract boolean isQuietModeEnabled(UserHandle user);

    public abstract boolean isUserUnlocked(UserHandle user);

    public abstract boolean isDemoUser();
}
