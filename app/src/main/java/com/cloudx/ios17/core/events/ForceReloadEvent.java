package com.cloudx.ios17.core.events;

public class ForceReloadEvent extends Event {
    public static final int TYPE = 801;

    public ForceReloadEvent() {
        super(TYPE);
    }
}
