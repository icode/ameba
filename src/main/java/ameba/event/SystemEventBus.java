package ameba.event;

import ameba.container.Container;

/**
 * <p>SystemEventBus class.</p>
 *
 * @author icode
 */
public class SystemEventBus {

    private static EventBus EVENT_BUS = init();

    private SystemEventBus() {
    }

    private static EventBus init() {
        EventBus eventBus = EventBus.createMix();
        eventBus.subscribe(Container.BeginReloadEvent.class, new Listener<Container.BeginReloadEvent>() {
            @Override
            public void onReceive(Container.BeginReloadEvent event) {
                EVENT_BUS = init();
            }
        });
        return eventBus;
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
     * @param event    a {@link java.lang.Class} object.
     * @param listener a {@link ameba.event.Listener} object.
     * @param <E>      a E object.
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
