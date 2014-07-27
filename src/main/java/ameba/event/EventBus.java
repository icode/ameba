package ameba.event;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.japi.LookupEventBus;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author icode
 */
public abstract class EventBus<E extends Event, S extends ActorRef> extends LookupEventBus<E, S, Class<? extends E>> {

    public static EventBus<Event, ActorRef> create(String actorSysName) {
        return new Sub(actorSysName);
    }

    @Override
    public int compareSubscribers(S a, S b) {
        return 1;
    }

    @Override
    public int mapSize() {
        return 0;
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

    public abstract boolean subscribe(Class<? extends E> eventClass, final Listener listener);

    public abstract boolean unsubscribe(Class<? extends E> eventClass, final Listener listener);

    private static class Sub extends EventBus<Event, ActorRef> {

        private final ActorSystem actorSystem;
        private final Map<Listener, ActorRef> actorRefMap = Maps.newHashMap();

        Sub(String actorSysName) {
            actorSystem = ActorSystem.create(actorSysName);
        }

        public boolean subscribe(Class<? extends Event> eventClass, final Listener listener) {
            ActorRef actor = actorSystem.actorOf(Props.create(EventHandler.class, listener));
            boolean suc = subscribe(actor, eventClass);
            if (suc)
                actorRefMap.put(listener, actor);
            return suc;
        }

        public boolean unsubscribe(Class<? extends Event> eventClass, final Listener listener) {
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
            Listener listener;

            public EventHandler(Listener listener) {
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
