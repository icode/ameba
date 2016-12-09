package ameba.event;

import ameba.container.event.ShutdownEvent;
import co.paralleluniverse.actors.behaviors.EventHandler;
import co.paralleluniverse.actors.behaviors.EventSource;
import co.paralleluniverse.actors.behaviors.EventSourceActor;
import co.paralleluniverse.fibers.RuntimeSuspendExecution;
import co.paralleluniverse.fibers.SuspendExecution;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Abstract AsyncEventBus class.</p>
 *
 * @author icode
 */
public class AsyncEventBus<E extends Event> implements EventBus<E> {
    private static final Logger logger = LoggerFactory.getLogger(AsyncEventBus.class);

    static {
        System.setProperty("co.paralleluniverse.fibers.disableAgentWarning", "true");
    }

    private final SetMultimap<Class<E>, Handler> handlers = LinkedHashMultimap.create();
    private final EventSource<E> eventSource = new EventSourceActor<E>(AsyncEventBus.class.getName()).spawn();

    @SuppressWarnings("unchecked")
    public AsyncEventBus() {
        subscribe((Class<E>) ShutdownEvent.class, event -> shutdown());
    }

    public static <EV extends Event> AsyncEventBus<EV> create() {
        return new AsyncEventBus<>();
    }

    @Override
    public void subscribe(Class<E> event, Listener<E> listener) {
        try {
            Handler handler = handler(event, listener);
            if (handlers.put(event, handler))
                eventSource.addHandler(handler);
        } catch (SuspendExecution e) {
            throw RuntimeSuspendExecution.of(e);
        } catch (InterruptedException e) {
            logger.error("subscribe event has error", e);
        }
    }

    public void subscribe(Class<E> event, AsyncListener<E> listener) {
        subscribe(event, (Listener<E>) listener);
    }

    @Override
    public void unsubscribe(Class<E> event, Listener<E> listener) {
        try {
            Handler handler = handler(event, listener);
            handlers.remove(event, handler);
            eventSource.removeHandler(handler);
        } catch (SuspendExecution e) {
            throw RuntimeSuspendExecution.of(e);
        } catch (InterruptedException e) {
            logger.error("unsubscribe event has error", e);
        }
    }

    @Override
    public void unsubscribe(Class<E> event) {
        handlers.get(event).forEach(handler -> {
            try {
                eventSource.removeHandler(handler);
            } catch (SuspendExecution e) {
                throw RuntimeSuspendExecution.of(e);
            } catch (InterruptedException e) {
                logger.error("unsubscribe event has error", e);
            }
        });
        handlers.removeAll(event);
    }

    @Override
    public void publish(E event) {
        try {
            eventSource.notify(event);
        } catch (SuspendExecution e) {
            throw RuntimeSuspendExecution.of(e);
        }
    }

    /**
     * shutdown event bus
     */
    public void shutdown() {
        eventSource.shutdown();
    }

    public Handler handler(Class<E> event, Listener<E> listener) {
        return new Handler(event, listener);
    }

    private class Handler implements EventHandler<E> {

        private Class<E> event;
        private Listener<E> listener;

        Handler(Class<E> event, Listener<E> listener) {
            this.event = event;
            this.listener = listener;
        }

        @Override
        public void handleEvent(E e) throws SuspendExecution, InterruptedException {
            if (e != null && e.getClass() == event)
                listener.onReceive(e);
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
