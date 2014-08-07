package ameba.feature;

import ameba.event.Event;
import ameba.event.EventBus;
import ameba.event.Listener;

import javax.ws.rs.core.Feature;

/**
 * @author icode
 */
public abstract class AmebaFeature implements Feature {

    private static final EventBus EVENT_BUS = EventBus.createMix("ameba-feature");

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

    public static void preInit(){

    }
}
