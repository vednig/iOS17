package com.cloudx.ios17.core.events;

public class TimeChangedEvent extends Event {

    public static final int TYPE = 701;

    public TimeChangedEvent() {
        super(TYPE);
    }
}
