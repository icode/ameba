package ameba.event;

import ameba.container.event.ShutdownEvent;
import co.paralleluniverse.actors.behaviors.EventHandler;
import co.paralleluniverse.actors.behaviors.EventSource;
import co.paralleluniverse.actors.behaviors.EventSourceActor;
import co.paralleluniverse.fibers.RuntimeSuspendExecution;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * <p>Abstract AsyncEventBus class.</p>
 *
 * @author icode
 */
public class AsyncEventBus<Event extends ameba.event.Event> implements EventBus<Event> {
    private static final Logger logger = LoggerFactory.getLogger(AsyncEventBus.class);

    private final Map<Class<? extends Event>, EventSource<? extends Event>> eventSourceMap = Maps.newConcurrentMap();

    @SuppressWarnings("unchecked")
    protected AsyncEventBus() {
        subscribe((Class<Event>) ShutdownEvent.class, event -> shutdown());
    }

    public static <Event extends ameba.event.Event> AsyncEventBus<Event> create() {
        return new AsyncEventBus<>();
    }

    @Override
    @Suspendable
    @SuppressWarnings("unchecked")
    public <E extends Event> void subscribe(Class<E> event, Listener<E> listener) {
        try {
            eventSourceMap.computeIfAbsent(
                    event, k -> new EventSourceActor<E>(AsyncEventBus.class.getName()).spawn()
            ).addHandler(handler(event, listener));
        } catch (SuspendExecution e) {
            throw RuntimeSuspendExecution.of(e);
        } catch (Exception e) {
            logger.error("subscribe event has error", e);
        }
    }

    public <E extends Event> void subscribe(Class<E> event, AsyncListener<E> listener) {
        subscribe(event, (Listener<E>) listener);
    }

    @Override
    @Suspendable
    @SuppressWarnings("unchecked")
    public <E extends Event> void unsubscribe(Class<E> event, Listener<E> listener) {
        EventSource<? extends Event> eventSource = eventSourceMap.get(event);
        if (eventSource != null) {
            try {
                eventSource.removeHandler(handler(event, listener));
            } catch (SuspendExecution e) {
                throw RuntimeSuspendExecution.of(e);
            } catch (InterruptedException e) {
                logger.error("unsubscribe event has error", e);
            }
        }
    }

    @Override
    public <E extends Event> void unsubscribe(Class<E> event) {
        EventSource eventSource = eventSourceMap.remove(event);
        if (eventSource != null) {
            eventSource.shutdown();
        }
    }

    @Override
    @Suspendable
    @SuppressWarnings("all")
    public <E extends Event> void publish(E event) {
        if (event != null) {
            EventSource<Event> eventSource = (EventSource<Event>) eventSourceMap.get(event.getClass());

            if (eventSource != null) {
                try {
                    eventSource.notify(event);
                } catch (SuspendExecution e) {
                    throw RuntimeSuspendExecution.of(e);
                }
            }
        }
    }

    /**
     * shutdown event bus
     */
    public void shutdown() {
        eventSourceMap.values().forEach(EventSource::shutdown);
    }

    private <E extends Event> EventHandler handler(Class<E> event, Listener<E> listener) {
        return new Handler<>(event, listener);
    }

    private class Handler<E extends ameba.event.Event> implements EventHandler<E> {

        private Class<E> event;
        private Listener<E> listener;

        Handler(Class<E> event, Listener<E> listener) {
            this.event = event;
            this.listener = listener;
        }

        @Override
        public void handleEvent(E event) {
            listener.onReceive(event);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Handler handler = (Handler) o;

            if (event != null ? !event.equals(handler.event) : handler.event != null) return false;
            return listener != null ? listener.equals(handler.listener) : handler.listener == null;
        }

        @Override
        public int hashCode() {
            int result = event != null ? event.hashCode() : 0;
            result = 31 * result + (listener != null ? listener.hashCode() : 0);
            return result;
        }
    }
}
