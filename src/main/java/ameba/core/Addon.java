package ameba.core;

import ameba.container.event.ShutdownEvent;
import ameba.event.Event;
import ameba.event.EventBus;
import ameba.event.Listener;
import ameba.event.SystemEventBus;

/**
 * <p>Abstract Addon class.</p>
 *
 * @author icode
 * @since 0.1.6e
 *
 */
public abstract class Addon {
    private static EventBus EVENT_BUS;

    static {
        init();
    }

    protected String version = "1.0.0";

    private static void init() {
        EVENT_BUS = EventBus.createMix();
        SystemEventBus.subscribe(ShutdownEvent.class, event -> {
            synchronized (Addon.class) {
                EVENT_BUS = null;
            }
        });
    }

    private static EventBus getEventBus() {
        if (EVENT_BUS == null) {
            synchronized (Addon.class) {
                if (EVENT_BUS == null) {
                    init();
                }
            }
        }
        return EVENT_BUS;
    }

    /**
     * <p>subscribeEvent.</p>
     *
     * @param eventClass a {@link java.lang.Class} object.
     * @param listener   a {@link ameba.event.Listener} object.
     * @param <E> a E object.
     */
    protected static <E extends Event> void subscribeEvent(Class<E> eventClass, final Listener<E> listener) {
        getEventBus().subscribe(eventClass, listener);
    }

    /**
     * <p>unsubscribeEvent.</p>
     *
     * @param eventClass a {@link java.lang.Class} object.
     * @param listener   a {@link ameba.event.Listener} object.
     * @param <E> a E object.
     */
    protected static <E extends Event> void unsubscribeEvent(Class<E> eventClass, final Listener<E> listener) {
        getEventBus().unsubscribe(eventClass, listener);
    }

    /**
     * <p>subscribeEvent.</p>
     *
     * @param object a {@link java.lang.Object} object.
     */
    public static void subscribeEvent(Object object) {
        getEventBus().subscribe(object);
    }

    /**
     * <p>subscribeSystemEvent.</p>
     *
     * @param eventClass a {@link java.lang.Class} object.
     * @param listener   a {@link ameba.event.Listener} object.
     * @param <E> a E object.
     */
    protected static <E extends Event> void subscribeSystemEvent(Class<E> eventClass, final Listener<E> listener) {
        SystemEventBus.subscribe(eventClass, listener);
    }

    /**
     * <p>unsubscribeSystemEvent.</p>
     *
     * @param eventClass a {@link java.lang.Class} object.
     * @param listener   a {@link ameba.event.Listener} object.
     * @param <E> a E object.
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
        getEventBus().publish(event);
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

    /**
     * <p>isEnabled.</p>
     *
     * @param application a {@link ameba.core.Application} object.
     * @return a boolean.
     */
    public boolean isEnabled(Application application) {
        return true;
    }

}
