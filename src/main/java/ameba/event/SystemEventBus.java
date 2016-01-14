package ameba.event;

import ameba.container.event.ShutdownEvent;

/**
 * <p>SystemEventBus class.</p>
 *
 * @author icode
 */
public class SystemEventBus {

    private static EventBus EVENT_BUS;

    static {
        init();
    }

    private SystemEventBus() {
    }

    private static void init() {
        EVENT_BUS = EventBus.createMix();
        EVENT_BUS.subscribe(ShutdownEvent.class, new Listener<ShutdownEvent>() {
            @Override
            public void onReceive(ShutdownEvent event) {
                synchronized (SystemEventBus.class) {
                    EVENT_BUS = null;
                }
            }
        });
    }

    private static EventBus getEventBus() {
        if (EVENT_BUS == null) {
            synchronized (SystemEventBus.class) {
                if (EVENT_BUS == null) {
                    init();
                }
            }
        }
        return EVENT_BUS;
    }

    /**
     * <p>subscribe.</p>
     *
     * @param event    a {@link java.lang.Class} object.
     * @param listener a {@link ameba.event.Listener} object.
     * @param <E>      a E object.
     */
    public static <E extends Event> void subscribe(Class<E> event, final Listener<E> listener) {
        getEventBus().subscribe(event, listener);
    }

    /**
     * <p>subscribe.</p>
     *
     * @param object a {@link java.lang.Object} object.
     * @since 0.1.6e
     */
    public static void subscribe(Object object) {
        getEventBus().subscribe(object);
    }

    /**
     * <p>unsubscribe.</p>
     *
     * @param event    a {@link java.lang.Class} object.
     * @param listener a {@link ameba.event.Listener} object.
     * @param <E>      a E object.
     */
    public static <E extends Event> void unsubscribe(Class<E> event, final Listener<E> listener) {
        getEventBus().unsubscribe(event, listener);
    }

    /**
     * <p>publish.</p>
     *
     * @param event a {@link ameba.event.Event} object.
     */
    public static void publish(Event event) {
        getEventBus().publish(event);
    }
}
