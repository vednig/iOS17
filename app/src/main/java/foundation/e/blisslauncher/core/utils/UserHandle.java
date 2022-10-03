/*
 * Copyright 2018 /e/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package foundation.e.blisslauncher.core.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Process;

/**
 * Wrapper class for `android.os.UserHandle` that works with all Android
 * versions
 */
public class UserHandle {
    private final long serial;
    private final Object handle; // android.os.UserHandle on Android 4.2 and newer

    public UserHandle() {
        this(0, null);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public UserHandle(long serial, android.os.UserHandle user) {
        if (user != null && Process.myUserHandle().equals(user)) {
            // For easier processing the current user is also stored as `null`, even
            // if there is multi-user support
            this.serial = 0;
            this.handle = null;
        } else {
            // Store the given user handle
            this.serial = serial;
            this.handle = user;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public android.os.UserHandle getRealHandle() {
        if (this.handle != null) {
            return (android.os.UserHandle) this.handle;
        } else {
            return Process.myUserHandle();
        }
    }

    public boolean isCurrentUser() {
        return (this.handle == null);
    }

    public String addUserSuffixToString(String base, char separator) {
        if (this.handle == null) {
            return base;
        } else {
            return base + separator + this.serial;
        }
    }

    public boolean hasStringUserSuffix(String string, char separator) {
        long serial = 0;

        int index = string.lastIndexOf((int) separator);
        if (index > -1) {
            String serialText = string.substring(index);
            try {
                serial = Long.parseLong(serialText);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return (serial == this.serial);
    }

    public boolean isSameUser(UserHandle userHandle) {
        return userHandle.serial == this.serial;
    }
}
