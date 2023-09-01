package com.cloudx.ios17.features.launcher;

import com.cloudx.ios17.core.events.AppAddEvent;
import com.cloudx.ios17.core.events.AppChangeEvent;
import com.cloudx.ios17.core.events.AppRemoveEvent;
import com.cloudx.ios17.core.events.Event;
import com.cloudx.ios17.core.events.EventRelay;
import com.cloudx.ios17.core.events.ForceReloadEvent;
import com.cloudx.ios17.core.events.ShortcutAddEvent;
import com.cloudx.ios17.core.events.TimeChangedEvent;
import com.cloudx.ios17.core.events.AppAddEvent;
import com.cloudx.ios17.core.events.AppChangeEvent;
import com.cloudx.ios17.core.events.AppRemoveEvent;
import com.cloudx.ios17.core.events.Event;
import com.cloudx.ios17.core.events.EventRelay;
import com.cloudx.ios17.core.events.ForceReloadEvent;
import com.cloudx.ios17.core.events.ShortcutAddEvent;
import com.cloudx.ios17.core.events.TimeChangedEvent;
import com.cloudx.ios17.core.events.AppAddEvent;
import com.cloudx.ios17.core.events.AppChangeEvent;
import com.cloudx.ios17.core.events.AppRemoveEvent;
import com.cloudx.ios17.core.events.Event;
import com.cloudx.ios17.core.events.EventRelay;
import com.cloudx.ios17.core.events.ForceReloadEvent;
import com.cloudx.ios17.core.events.ShortcutAddEvent;
import com.cloudx.ios17.core.events.TimeChangedEvent;
import com.cloudx.ios17.core.events.AppAddEvent;
import com.cloudx.ios17.core.events.AppChangeEvent;
import com.cloudx.ios17.core.events.AppRemoveEvent;
import com.cloudx.ios17.core.events.Event;
import com.cloudx.ios17.core.events.EventRelay;
import com.cloudx.ios17.core.events.ForceReloadEvent;
import com.cloudx.ios17.core.events.ShortcutAddEvent;
import com.cloudx.ios17.core.events.TimeChangedEvent;
import timber.log.Timber;

import java.util.Calendar;

public class EventsObserverImpl implements EventRelay.EventsObserver<Event> {

    private static final String TAG = "EventsObserverImpl";

    private LauncherActivity launcherActivity;

    public EventsObserverImpl(LauncherActivity activity) {
        this.launcherActivity = activity;
    }

    @Override
    public void accept(Event event) {
        Timber.tag(TAG).i("accept: %s", event.getEventType());
        switch (event.getEventType()) {
            case AppAddEvent.TYPE :
                launcherActivity.onAppAddEvent((AppAddEvent) event);
                break;
            case AppChangeEvent.TYPE :
                launcherActivity.onAppChangeEvent((AppChangeEvent) event);
                break;
            case AppRemoveEvent.TYPE :
                launcherActivity.onAppRemoveEvent((AppRemoveEvent) event);
                break;
            case ShortcutAddEvent.TYPE :
                launcherActivity.onShortcutAddEvent((ShortcutAddEvent) event);
                break;
            case TimeChangedEvent.TYPE :
                launcherActivity.updateAllCalendarIcons(Calendar.getInstance());
                break;
            case ForceReloadEvent.TYPE :
                launcherActivity.forceReload();
                break;
        }
    }

    @Override
    public void complete() {
        // BlissLauncher.getApplication(launcherActivity).getAppProvider().reload();
    }

    @Override
    public void clear() {
        this.launcherActivity = null;
    }
}
