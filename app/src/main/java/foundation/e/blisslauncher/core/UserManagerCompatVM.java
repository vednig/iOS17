package foundation.e.blisslauncher.core;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.UserHandle;

@TargetApi(Build.VERSION_CODES.M)
public class UserManagerCompatVM extends UserManagerCompatVL {

    UserManagerCompatVM(Context context) {
        super(context);
    }

    @Override
    public long getUserCreationTime(UserHandle user) {
        return mUserManager.getUserCreationTime(user);
    }
}
