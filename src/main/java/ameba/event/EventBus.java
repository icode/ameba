package ameba.event;

import akka.actor.ActorRef;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author icode
 */
public abstract class EventBus {

    private static final Logger logger = LoggerFactory.getLogger(EventBus.class);
    private final SetMultimap<Class<?>, Listener> listeners = HashMultimap.create();
    private final ReadWriteLock subscribersByTypeLock = new ReentrantReadWriteLock();

    private EventBus() {
    }

    public static EventBus createMix() {
        return new Mixed();
    }

    public static EventBus create() {
        return new EventBus() {
        };
    }

    public <E extends Event> void subscribe(Class<E> event, final Listener<E> listener) {
        subscribersByTypeLock.writeLock().lock();
        try {
            listeners.put(event, listener);
        } finally {
            subscribersByTypeLock.writeLock().unlock();
        }
    }

    public <E extends Event> void unsubscribe(Class<E> event, final Listener<E> listener) {
        subscribersByTypeLock.writeLock().lock();
        try {
            listeners.remove(event, listener);
        } finally {
            subscribersByTypeLock.writeLock().unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public void publish(Event event) {
        Set<Listener> listenerSet = Sets.newCopyOnWriteArraySet(listeners.get(event.getClass()));
        for (Listener listener : listenerSet) {
            try {
                listener.onReceive(event);
            } catch (Exception e) {
                logger.error(event.getClass().getName() + " event handler has a error", e);
            }
        }
    }

    public static class Mixed extends EventBus {

        private final AsyncEventBus<Event, ActorRef> asyncEventBus;

        Mixed() {
            asyncEventBus = AsyncEventBus.create();
        }

        public <E extends Event> void subscribe(Class<E> event, final Listener<E> listener) {
            if (listener instanceof AsyncListener) {
                asyncEventBus.subscribe(event, (AsyncListener) listener);
            } else {
                super.subscribe(event, listener);
            }
        }

        public <E extends Event> void unsubscribe(Class<E> event, final Listener<E> listener) {
            if (listener instanceof AsyncListener) {
                asyncEventBus.unsubscribe(event, (AsyncListener) listener);
            } else {
                super.unsubscribe(event, listener);
            }
        }

        public void publish(Event event) {
            if (event == null) return;
            asyncEventBus.publish(event);
            super.publish(event);
        }
    }
}
