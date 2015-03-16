package ameba.event;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.japi.LookupEventBus;
import ameba.lib.Akka;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        return a.compareTo(b);
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

    public abstract void unsubscribe(Class<? extends E> eventClass);

    private static class Sub extends AsyncEventBus<Event, ActorRef> {

        private final EventActorMap eventActorMap = new EventActorMap();

        public boolean subscribe(Class<? extends Event> eventClass, final AsyncListener listener) {
            final ActorRef actor = Akka.system().actorOf(Props.create(EventHandler.class, listener));
            boolean suc = subscribe(actor, eventClass);
            if (suc) {
                eventActorMap.put(eventClass, listener, actor);
            }
            return suc;
        }

        public boolean unsubscribe(Class<? extends Event> eventClass, final AsyncListener listener) {
            Map<AsyncListener, ActorRef> eventEntry = eventActorMap.get(eventClass);
            if (eventEntry != null) {
                ActorRef actorRef = eventEntry.get(listener);
                if (actorRef != null) {
                    boolean suc = unsubscribe(actorRef, eventClass);
                    if (suc) {
                        eventEntry.remove(listener);
                    }
                    return suc;
                }
            }

            return false;
        }

        @Override
        public void unsubscribe(Class<? extends Event> eventClass) {
            Map<AsyncListener, ActorRef> eventEntries = eventActorMap.get(eventClass);
            if (eventEntries != null) {
                for (ActorRef actorRef : eventEntries.values()) {
                    unsubscribe(actorRef, eventClass);
                }
                eventActorMap.remove(eventClass);
            }
        }

        private static class EventActorMap extends ConcurrentHashMap<Class<? extends Event>, Map<AsyncListener, ActorRef>> {

            public Map<AsyncListener, ActorRef> put(Class<? extends Event> key, AsyncListener listener, ActorRef actorRef) {
                Map<AsyncListener, ActorRef> o = get(key);
                if (o == null) {
                    o = Maps.newConcurrentMap();
                    put(key, o);
                }
                o.put(listener, actorRef);
                return o;
            }

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
