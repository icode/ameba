package ameba.feature;

import ameba.container.event.ShutdownEvent;
import ameba.event.Event;
import ameba.event.EventBus;
import ameba.event.Listener;
import ameba.event.SystemEventBus;
import ameba.lib.LoggerOwner;
import org.glassfish.hk2.api.ServiceLocator;

import javax.inject.Inject;
import javax.ws.rs.core.Feature;

/**
 * <p>Abstract AmebaFeature class.</p>
 *
 * @author icode
 *
 */
public abstract class AmebaFeature extends LoggerOwner implements Feature {

    private static EventBus EVENT_BUS;

    static {
        init();
    }

    @Inject
    private ServiceLocator locator;

    private static void init() {
        EVENT_BUS = EventBus.createMix();
        SystemEventBus.subscribe(ShutdownEvent.class, event -> {
            synchronized (AmebaFeature.class) {
                EVENT_BUS = null;
            }
        });
    }

    private static EventBus getEventBus() {
        if (EVENT_BUS == null) {
            synchronized (AmebaFeature.class) {
                if (EVENT_BUS == null) {
                    init();
                }
            }
        }
        return EVENT_BUS;
    }

    /**
     * <p>publishEvent.</p>
     *
     * @param event a {@link ameba.event.Event} object.
     */
    public static void publishEvent(Event event) {
        getEventBus().publish(event);
    }

    private <E extends Event> void subscribe(Class<E> eventClass, final Listener<E> listener) {
        getEventBus().subscribe(eventClass, listener);
    }

    /**
     * <p>subscribeEvent.</p>
     *
     * @param object a {@link java.lang.Object} object.
     * @since 0.1.6e
     */
    public void subscribeEvent(Object object) {
        if (object instanceof Class) {
            object = locator.createAndInitialize((Class) object);
        } else {
            locator.inject(object);
            locator.postConstruct(object);
        }
        getEventBus().subscribe(object);
    }

    /**
     * <p>subscribeEvent.</p>
     *
     * @param eventClass a {@link java.lang.Class} object.
     * @param listener   a {@link ameba.event.Listener} object.
     * @param <E> a E object.
     */
    protected <E extends Event> void subscribeEvent(Class<E> eventClass, final Listener<E> listener) {
        locator.inject(listener);
        locator.postConstruct(listener);
        subscribe(eventClass, listener);
    }

    /**
     * <p>subscribeEvent.</p>
     *
     * @param eventClass    a {@link java.lang.Class} object.
     * @param listenerClass a {@link java.lang.Class} object.
     * @return a {@link ameba.event.Listener} object.
     * @since 0.1.6e
     * @param <E> a E object.
     */
    protected <E extends Event> Listener subscribeEvent(Class<E> eventClass, final Class<? extends Listener<E>> listenerClass) {
        Listener<E> listener = locator.createAndInitialize(listenerClass);
        subscribe(eventClass, listener);
        return listener;
    }

    /**
     * <p>unsubscribeEvent.</p>
     *
     * @param eventClass a {@link java.lang.Class} object.
     * @param listener   a {@link ameba.event.Listener} object.
     * @param <E> a E object.
     */
    protected <E extends Event> void unsubscribeEvent(Class<E> eventClass, final Listener<E> listener) {
        locator.preDestroy(listener);
        getEventBus().unsubscribe(eventClass, listener);
    }

    /**
     * <p>subscribeSystemEvent.</p>
     *
     * @param eventClass a {@link java.lang.Class} object.
     * @param listener   a {@link ameba.event.Listener} object.
     * @since 0.1.6e
     * @param <E> a E object.
     */
    protected <E extends Event> void subscribeSystemEvent(Class<E> eventClass, final Listener<E> listener) {
        locator.inject(listener);
        locator.postConstruct(listener);
        SystemEventBus.subscribe(eventClass, listener);
    }

    /**
     * <p>unsubscribeSystemEvent.</p>
     *
     * @param eventClass a {@link java.lang.Class} object.
     * @param listener   a {@link ameba.event.Listener} object.
     * @since 0.1.6e
     * @param <E> a E object.
     */
    protected <E extends Event> void unsubscribeSystemEvent(Class<E> eventClass, final Listener<E> listener) {
        locator.preDestroy(listener);
        SystemEventBus.unsubscribe(eventClass, listener);
    }

    /**
     * <p>subscribeSystemEvent.</p>
     *
     * @param eventClass    a {@link java.lang.Class} object.
     * @param listenerClass a {@link java.lang.Class} object.
     * @return a {@link ameba.event.Listener} object.
     * @since 0.1.6e
     * @param <E> a E object.
     */
    protected <E extends Event> Listener subscribeSystemEvent(Class<E> eventClass, final Class<? extends Listener<E>> listenerClass) {
        Listener<E> listener = locator.createAndInitialize(listenerClass);
        SystemEventBus.subscribe(eventClass, listener);
        return listener;
    }
}
