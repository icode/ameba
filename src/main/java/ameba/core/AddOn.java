package ameba.core;

import ameba.event.Event;
import ameba.event.EventBus;
import ameba.event.Listener;
import ameba.event.SystemEventBus;

/**
 * @author icode
 */
public abstract class AddOn {
    private static EventBus EVENT_BUS = EventBus.createMix();

    protected static <E extends Event> void subscribeEvent(Class<E> eventClass, final Listener<E> listener) {
        EVENT_BUS.subscribe(eventClass, listener);
    }

    protected static <E extends Event> void unsubscribeEvent(Class<E> eventClass, final Listener<E> listener) {
        EVENT_BUS.unsubscribe(eventClass, listener);
    }

    protected static <E extends Event> void subscribeSystemEvent(Class<E> eventClass, final Listener<E> listener) {
        SystemEventBus.subscribe(eventClass, listener);
    }

    protected static <E extends Event> void unsubscribeSystemEvent(Class<E> eventClass, final Listener<E> listener) {
        SystemEventBus.unsubscribe(eventClass, listener);
    }


    public static void publishEvent(Event event) {
        EVENT_BUS.publish(event);
    }

    public void setup(Application application) {
    }

    public void done(Application application) {
    }

}
