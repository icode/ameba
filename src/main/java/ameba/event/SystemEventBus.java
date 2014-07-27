package ameba.event;

import akka.actor.ActorRef;

/**
 * @author icode
 */
public class SystemEventBus {

    private static final EventBus<Event, ActorRef> EVENT_BUS = EventBus.create("ameba-sys");

    public static void subscribe(Class<? extends Event> event, final Listener listener) {
        EVENT_BUS.subscribe(event, listener);
    }

    public static void unsubscribe(Class<? extends Event> event, final Listener listener) {
        EVENT_BUS.unsubscribe(event, listener);
    }

    public static void publish(Event event) {
        EVENT_BUS.publish(event);
    }
}
