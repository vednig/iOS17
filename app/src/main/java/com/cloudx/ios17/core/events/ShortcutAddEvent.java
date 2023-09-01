package com.cloudx.ios17.core.events;

import com.cloudx.ios17.core.database.model.ShortcutItem;

public class ShortcutAddEvent extends Event {
    private ShortcutItem mShortcutItem;

    public static final int TYPE = 603;

    public ShortcutAddEvent(ShortcutItem shortcutItem) {
        super(TYPE);
        this.mShortcutItem = shortcutItem;
    }

    public ShortcutItem getShortcutItem() {
        return mShortcutItem;
    }
}
