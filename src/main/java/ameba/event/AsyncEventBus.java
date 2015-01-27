package ameba.event;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.japi.LookupEventBus;
import ameba.lib.Akka;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author icode
 */
public abstract class AsyncEventBus<E extends Event, S extends ActorRef> extends LookupEventBus<E, S, Class<? extends E>> {

    private AsyncEventBus() {
    }

    public static AsyncEventBus<Event, ActorRef> create() {
        return new Sub();
    }

    @Override
    public int compareSubscribers(S a, S b) {
        return 0;
    }

    @Override
    public int mapSize() {
        return 128;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends E> classify(E event) {
        return (Class<? extends E>) event.getClass();
    }

    @Override
    public void publish(E event, S subscriber) {
        subscriber.tell(event, ActorRef.noSender());
    }

    public abstract boolean subscribe(Class<? extends E> eventClass, final AsyncListener listener);

    public abstract boolean unsubscribe(Class<? extends E> eventClass, final AsyncListener listener);

    private static class Sub extends AsyncEventBus<Event, ActorRef> {

        private final Map<AsyncListener, ActorRef> actorRefMap = Maps.newConcurrentMap();

        public boolean subscribe(Class<? extends Event> eventClass, final AsyncListener listener) {
            ActorRef actor = Akka.system().actorOf(Props.create(EventHandler.class, listener));
            boolean suc = subscribe(actor, eventClass);
            if (suc)
                actorRefMap.put(listener, actor);
            return suc;
        }

        public boolean unsubscribe(Class<? extends Event> eventClass, final AsyncListener listener) {
            ActorRef actor = actorRefMap.get(listener);
            if (actor != null) {
                boolean suc = unsubscribe(actor, eventClass);
                if (suc)
                    actorRefMap.remove(listener);
                return suc;
            }
            return false;
        }

        public static class EventHandler extends UntypedActor {
            AsyncListener listener;

            public EventHandler(AsyncListener listener) {
                this.listener = listener;
                listener.actor = this;
            }

            @Override
            @SuppressWarnings("unchecked")
            public void onReceive(final Object message) {
                if (message instanceof Event) {
                    listener.onReceive((Event) message);
                } else {
                    unhandled(message);
                }
            }
        }
    }
}
