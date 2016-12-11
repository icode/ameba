package ameba.event;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author icode
 */
public class BasicEventBus<Event extends ameba.event.Event> implements EventBus<Event> {
    private static final Logger logger = LoggerFactory.getLogger(EventBus.class);
    private final Map<Class<? extends Event>, CopyOnWriteArrayList<Listener<? extends Event>>> listeners
            = Maps.newConcurrentMap();

    public <E extends Event> void subscribe(Class<E> event, final Listener<E> listener) {
        listeners.computeIfAbsent(event, k -> Lists.newCopyOnWriteArrayList()).add(listener);
    }

    public <E extends Event> void unsubscribe(Class<E> event, final Listener<E> listener) {
        CopyOnWriteArrayList<Listener<? extends Event>> ls = listeners.get(event);
        if (ls != null)
            ls.remove(listener);
    }

    public <E extends Event> void unsubscribe(Class<E> event) {
        listeners.remove(event);
    }

    @SuppressWarnings("unchecked")
    public <E extends Event> void publish(E event) {
        CopyOnWriteArrayList ls = listeners.get(event.getClass());
        if (ls != null) {
            ls.forEach(listener -> {
                try {
                    ((Listener<E>) listener).onReceive(event);
                } catch (Exception e) {
                    logger.error(event.getClass().getName() + " event handler has a error", e);
                }
            });
        }
    }
}
