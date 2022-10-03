package foundation.e.blisslauncher.core.events;

/** All event classes must inherit from this class. */
public class Event {

    private final int eventType;

    public Event(int eventType) {
        this.eventType = eventType;
    }

    public int getEventType() {
        return eventType;
    }
}
