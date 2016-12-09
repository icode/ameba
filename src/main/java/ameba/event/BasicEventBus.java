package ameba.event;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author icode
 */
public class BasicEventBus<E extends Event> implements EventBus<E> {
    private static final Logger logger = LoggerFactory.getLogger(EventBus.class);
    private final SetMultimap<Class<E>, Listener> listeners = LinkedHashMultimap.create();

    public void subscribe(Class<E> event, final Listener<E> listener) {
        listeners.put(event, listener);
    }

    public void unsubscribe(Class<E> event, final Listener<E> listener) {
        listeners.remove(event, listener);
    }

    public void unsubscribe(Class<E> event) {
        listeners.removeAll(event);
    }

    @SuppressWarnings("unchecked")
    public void publish(E event) {
        Sets.newCopyOnWriteArraySet(listeners.get((Class<E>) event.getClass()))
                .forEach((listener -> {
                    try {
                        listener.onReceive(event);
                    } catch (Exception e) {
                        logger.error(event.getClass().getName() + " event handler has a error", e);
                    }
                }));
    }
}
