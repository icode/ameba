package ameba.event;

/**
 * <p>SystemEventBus class.</p>
 *
 * @author icode
 */
public class SystemEventBus {

    private static EventBus EVENT_BUS = EventBus.createMix();

    private SystemEventBus() {
    }

    /**
     * <p>subscribe.</p>
     *
     * @param event    a {@link java.lang.Class} object.
     * @param listener a {@link ameba.event.Listener} object.
     * @param <E>      a E object.
     */
    public static <E extends Event> void subscribe(Class<E> event, final Listener<E> listener) {
        EVENT_BUS.subscribe(event, listener);
    }

    /**
     * <p>subscribe.</p>
     *
     * @param object a {@link java.lang.Object} object.
     * @since 0.1.6e
     */
    public static void subscribe(Object object) {
        EVENT_BUS.subscribe(object);
    }

    /**
     * <p>unsubscribe.</p>
     *
     * @param event a {@link java.lang.Class} object.
     * @param listener a {@link ameba.event.Listener} object.
     * @param <E> a E object.
     */
    public static <E extends Event> void unsubscribe(Class<E> event, final Listener<E> listener) {
        EVENT_BUS.unsubscribe(event, listener);
    }

    /**
     * <p>publish.</p>
     *
     * @param event a {@link ameba.event.Event} object.
     */
    public static void publish(Event event) {
        EVENT_BUS.publish(event);
    }
}
