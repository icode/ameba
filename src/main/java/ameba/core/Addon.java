package ameba.core;

import ameba.container.Container;
import ameba.event.Event;
import ameba.event.EventBus;
import ameba.event.Listener;
import ameba.event.SystemEventBus;

/**
 * <p>Abstract Addon class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
public abstract class Addon {
    private static EventBus EVENT_BUS = init();
    protected String version = "1.0.0";

    private static EventBus init() {
        EventBus eventBus = EventBus.createMix();
        SystemEventBus.subscribe(Container.BeginReloadEvent.class, new Listener<Container.BeginReloadEvent>() {
            @Override
            public void onReceive(Container.BeginReloadEvent event) {
                EVENT_BUS = init();
            }
        });
        return eventBus;
    }

    /**
     * <p>subscribeEvent.</p>
     *
     * @param eventClass a {@link java.lang.Class} object.
     * @param listener   a {@link ameba.event.Listener} object.
     * @param <E>        a E object.
     */
    protected static <E extends Event> void subscribeEvent(Class<E> eventClass, final Listener<E> listener) {
        EVENT_BUS.subscribe(eventClass, listener);
    }

    /**
     * <p>unsubscribeEvent.</p>
     *
     * @param eventClass a {@link java.lang.Class} object.
     * @param listener   a {@link ameba.event.Listener} object.
     * @param <E>        a E object.
     */
    protected static <E extends Event> void unsubscribeEvent(Class<E> eventClass, final Listener<E> listener) {
        EVENT_BUS.unsubscribe(eventClass, listener);
    }

    /**
     * <p>subscribeEvent.</p>
     *
     * @param object a {@link java.lang.Object} object.
     */
    public static void subscribeEvent(Object object) {
        EVENT_BUS.subscribe(object);
    }

    /**
     * <p>subscribeSystemEvent.</p>
     *
     * @param eventClass a {@link java.lang.Class} object.
     * @param listener   a {@link ameba.event.Listener} object.
     * @param <E>        a E object.
     */
    protected static <E extends Event> void subscribeSystemEvent(Class<E> eventClass, final Listener<E> listener) {
        SystemEventBus.subscribe(eventClass, listener);
    }

    /**
     * <p>unsubscribeSystemEvent.</p>
     *
     * @param eventClass a {@link java.lang.Class} object.
     * @param listener   a {@link ameba.event.Listener} object.
     * @param <E>        a E object.
     */
    protected static <E extends Event> void unsubscribeSystemEvent(Class<E> eventClass, final Listener<E> listener) {
        SystemEventBus.unsubscribe(eventClass, listener);
    }

    /**
     * <p>subscribeSystemEvent.</p>
     *
     * @param object a {@link java.lang.Object} object.
     */
    public static void subscribeSystemEvent(Object object) {
        SystemEventBus.subscribe(object);
    }

    /**
     * <p>publishEvent.</p>
     *
     * @param event a {@link ameba.event.Event} object.
     */
    public static void publishEvent(Event event) {
        EVENT_BUS.publish(event);
    }

    /**
     * <p>Getter for the field <code>version</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getVersion() {
        return version;
    }

    /**
     * <p>setup.</p>
     *
     * @param application a {@link ameba.core.Application} object.
     */
    public void setup(Application application) {
    }

    /**
     * <p>done.</p>
     *
     * @param application a {@link ameba.core.Application} object.
     */
    public void done(Application application) {
    }

    public boolean isEnabled(Application application) {
        return true;
    }

}
