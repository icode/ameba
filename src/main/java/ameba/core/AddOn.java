package ameba.core;

import ameba.event.Event;
import ameba.event.EventBus;
import ameba.event.Listener;

/**
 * @author icode
 */
public abstract class AddOn {
    private static EventBus EVENT_BUS = EventBus.create();

    public static EventBus getEventBus() {
        return EVENT_BUS;
    }

    protected static <E extends Event> void subscribeEvent(Class<E> eventClass, final Listener<E> listener) {
        EVENT_BUS.subscribe(eventClass, listener);
    }

    protected static <E extends Event> void unsubscribeEvent(Class<E> eventClass, final Listener<E> listener) {
        EVENT_BUS.unsubscribe(eventClass, listener);
    }

    protected static void publishEvent(Event event) {
        EVENT_BUS.publish(event);
    }

    public void setup(Application application) {
    }

    public void done(Application application) {
    }

}
