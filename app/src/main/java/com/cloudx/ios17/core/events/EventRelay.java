package com.cloudx.ios17.core.events;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EventRelay {

    private static volatile EventRelay sInstance;

    // Queue to store actions if no observer is currently subscribed to listen for
    // events.
    private Queue<Event> events;
    private EventsObserver<Event> observer;

    private EventRelay() {
        events = new ConcurrentLinkedQueue<>();
    }

    public static EventRelay getInstance() {
        if (sInstance == null) {
            synchronized (EventRelay.class) {
                if (sInstance == null) {
                    sInstance = new EventRelay();
                }
            }
        }
        return sInstance;
    }

    public void push(Event event) {
        if (observer != null) {
            observer.accept(event);
            if (!(event instanceof TimeChangedEvent)) {
                observer.complete();
            }
        } else {
            this.events.offer(event);
        }
    }

    public void subscribe(EventsObserver<Event> observer) {
        this.observer = observer;
        Event event = events.poll();
        boolean shouldInvokeComplete = (event != null);

        // Pass all the events to the observer
        while (event != null) {
            this.observer.accept(event);
            event = events.poll();
        }

        if (shouldInvokeComplete)
            this.observer.complete();
    }

    public void unsubscribe() {
        if (this.observer != null) {
            observer.clear();
            observer = null;
        }
    }

    public interface EventsObserver<T> {
        void accept(T event);

        void complete();

        void clear();
    }
}
