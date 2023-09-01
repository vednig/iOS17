package com.cloudx.ios17.core.events;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class EventRelayTest {

    @Mock
    AppAddEvent appAddEvent;
    @Mock
    AppChangeEvent appChangeEvent;
    @Mock
    AppRemoveEvent appRemoveEvent;
    @Mock
    ShortcutAddEvent shortcutAddEvent;

    @Mock
    TimeChangedEvent timeChangedEvent;

    @Mock
    EventRelay.EventsObserver eventsObserver;

    private EventRelay eventRelay;

    @Before
    public void setUp() {
        eventsObserver = Mockito.mock(EventRelay.EventsObserver.class);
        eventRelay = mockEventRelay();
    }

    // Just a [hack] to get EventRelay instance as this class is Singleton.
    private EventRelay mockEventRelay() {
        final Constructor<?>[] constructors = EventRelay.class.getDeclaredConstructors();
        constructors[0].setAccessible(true);
        try {
            return (EventRelay) constructors[0].newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Test
    public void testPushingBeforeSubscribe() {
        // Push few events before subscribing
        eventRelay.push(appAddEvent);
        eventRelay.push(appAddEvent);

        // Now subscribe to the relay to confirm if all previously pushed events are
        // being relayed.
        eventRelay.subscribe(eventsObserver);
        verify(eventsObserver, times(2)).accept(appAddEvent);
        verify(eventsObserver).complete(); // Check if complete() gets called.
    }

    @Test
    public void testSubscribeWithoutPushing() {
        eventRelay.subscribe(eventsObserver);
        verify(eventsObserver, times(0)).accept(appAddEvent);
        verify(eventsObserver, times(0)).complete();
    }

    @Test
    public void testPushingAfterSubscribe() {
        // Subscribe first
        eventRelay.subscribe(eventsObserver);
        // Push the event
        eventRelay.push(shortcutAddEvent);
        verify(eventsObserver, times(1)).accept(shortcutAddEvent);
        verify(eventsObserver, times(1)).complete();
    }

    /** This test is done separately to confirm that complete() is not called. */
    @Test
    public void testTimeChangeEventPush() {
        eventRelay.subscribe(eventsObserver);

        // Push the timeChangedEvent
        eventRelay.push(timeChangedEvent);
        verify(eventsObserver).accept(timeChangedEvent);
        verify(eventsObserver, times(0)).complete(); // Should not be called.
    }

    @Test
    public void testUnsubscribe() {

        // Regular push and subscribe
        eventRelay.push(appRemoveEvent);
        eventRelay.push(appChangeEvent);
        eventRelay.subscribe(eventsObserver);
        verify(eventsObserver).accept(appRemoveEvent);
        verify(eventsObserver).accept(appChangeEvent);

        // Unsubscribe the relay
        eventRelay.unsubscribe();

        reset(eventsObserver); // reset eventsObserver to avoid double method count.
        eventRelay.push(appChangeEvent);

        // Expected invocation 0 times.
        verify(eventsObserver, times(0)).accept(appChangeEvent);
        verify(eventsObserver, times(0)).complete();
    }
}
