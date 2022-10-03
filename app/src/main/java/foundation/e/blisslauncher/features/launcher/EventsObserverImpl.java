package foundation.e.blisslauncher.features.launcher;

import android.util.Log;
import foundation.e.blisslauncher.core.events.AppAddEvent;
import foundation.e.blisslauncher.core.events.AppChangeEvent;
import foundation.e.blisslauncher.core.events.AppRemoveEvent;
import foundation.e.blisslauncher.core.events.Event;
import foundation.e.blisslauncher.core.events.EventRelay;
import foundation.e.blisslauncher.core.events.ForceReloadEvent;
import foundation.e.blisslauncher.core.events.ShortcutAddEvent;
import foundation.e.blisslauncher.core.events.TimeChangedEvent;
import java.util.Calendar;

public class EventsObserverImpl implements EventRelay.EventsObserver<Event> {

    private static final String TAG = "EventsObserverImpl";

    private LauncherActivity launcherActivity;

    public EventsObserverImpl(LauncherActivity activity) {
        this.launcherActivity = activity;
    }

    @Override
    public void accept(Event event) {
        Log.i(TAG, "accept: " + event.getEventType());
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
