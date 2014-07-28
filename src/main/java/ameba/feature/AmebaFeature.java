package ameba.feature;

import akka.actor.ActorRef;
import ameba.event.AsyncEventBus;
import ameba.event.AsyncListener;
import ameba.event.Event;

import javax.ws.rs.core.Feature;

/**
 * @author icode
 */
public abstract class AmebaFeature implements Feature {

    private static final AsyncEventBus<Event, ActorRef> EVENT_BUS = AsyncEventBus.create("ameba-feature");

    public static AsyncEventBus<? extends Event, ActorRef> getEventBus() {
        return EVENT_BUS;
    }

    protected final void addEventListener(Class<? extends Event> eventClass, final AsyncListener listener) {
        EVENT_BUS.subscribe(eventClass, listener);
    }

    protected final void removeEventListener(Class<? extends Event> eventClass, final AsyncListener listener) {
        EVENT_BUS.unsubscribe(eventClass, listener);
    }

    protected final void triggerEvent(Event event) {
        EVENT_BUS.publish(event);
    }

    protected final void subscribeEvent(Class<? extends Event> eventClass, final AsyncListener listener) {

    }

    protected final void unsubscribeEvent(Class<? extends Event> eventClass, final AsyncListener listener) {

    }

    protected final void publishEvent(Event event) {

    }
}
