package ameba.event;

import akka.actor.ActorRef;
import com.google.common.eventbus.EventBus;

/**
 * @author icode
 */
public class SystemEventBus {

    private static final AsyncEventBus<Event, ActorRef> ASYNC_EVENT_BUS = AsyncEventBus.create("ameba-sys");
    private static final EventBus = new EventBus();

    public static void subscribe(Class<? extends Event> event, final AsyncListener listener) {
        ASYNC_EVENT_BUS.subscribe(event, listener);
    }

    public static void unsubscribe(Class<? extends Event> event, final AsyncListener listener) {
        ASYNC_EVENT_BUS.unsubscribe(event, listener);
    }

    public static void publish(Event event) {
        ASYNC_EVENT_BUS.publish(event);
    }
}
