package ameba.feature;

import akka.actor.ActorRef;
import ameba.event.Event;
import ameba.event.EventBus;
import ameba.event.Listener;

import javax.ws.rs.core.Feature;

/**
 * @author icode
 */
public abstract class AmebaFeature implements Feature {

    private static final EventBus<Event, ActorRef> EVENT_BUS = EventBus.create("ameba-feature");

    public static EventBus<? extends Event, ActorRef> getEventBus() {
        return EVENT_BUS;
    }

    protected final void addEventListener(Class<? extends Event> eventClass, final Listener listener) {
        EVENT_BUS.subscribe(eventClass, listener);
    }

    protected final void removeEventListener(Class<? extends Event> eventClass, final Listener listener) {
        EVENT_BUS.unsubscribe(eventClass, listener);
    }

    protected final void triggerEvent(Event event) {
        EVENT_BUS.publish(event);
    }

    protected final void subscribeEvent(Class<? extends Event> eventClass, final Listener listener) {

    }

    protected final void unsubscribeEvent(Class<? extends Event> eventClass, final Listener listener) {

    }

    protected final void publishEvent(Event event) {

    }
}
