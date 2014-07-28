package ameba.event;

import akka.actor.ActorRef;

/**
 * @author icode
 */
public abstract class EventBus {

    private EventBus() {
    }

    public static EventBus create(String busName) {
        return new Sub(busName);
    }

    public abstract void subscribe(Class<? extends Event> event, final Listener listener);

    public abstract void unsubscribe(Class<? extends Event> event, final Listener listener);

    public abstract void publish(Event event);

    public static class Sub extends EventBus {
        private final AsyncEventBus<Event, ActorRef> asyncEventBus;
        private final com.google.common.eventbus.EventBus syncEventBus;

        Sub(String busName) {
            asyncEventBus = AsyncEventBus.create(busName);
            syncEventBus = new com.google.common.eventbus.EventBus(busName);
        }

        public void subscribe(Class<? extends Event> event, final Listener listener) {
            if (listener instanceof AsyncListener) {
                asyncEventBus.subscribe(event, (AsyncListener) listener);
            } else {
                syncEventBus.register(listener);
            }
        }

        public void unsubscribe(Class<? extends Event> event, final Listener listener) {
            if (listener instanceof AsyncListener) {
                asyncEventBus.unsubscribe(event, (AsyncListener) listener);
            } else {
                syncEventBus.unregister(listener);
            }
        }

        public void publish(Event event) {
            asyncEventBus.publish(event);
            syncEventBus.post(event);
        }
    }
}
