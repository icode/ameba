package ameba.feature;

import ameba.container.Container;
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
 */
public abstract class AmebaFeature extends LoggerOwner implements Feature {

    private static EventBus EVENT_BUS = init();

    @Inject
    private ServiceLocator locator;

    /**
     * <p>publishEvent.</p>
     *
     * @param event a {@link ameba.event.Event} object.
     */
    public static void publishEvent(Event event) {
        EVENT_BUS.publish(event);
    }

    private static EventBus init() {
        EventBus eventBus = EventBus.createMix();
        SystemEventBus.subscribe(Container.BeginReloadEvent.class,
                new Listener<Container.BeginReloadEvent>() {
                    @Override
                    public void onReceive(Container.BeginReloadEvent event) {
                        EVENT_BUS = EventBus.createMix();
                    }
                });
        return eventBus;
    }

    private <E extends Event> void subscribe(Class<E> eventClass, final Listener<E> listener) {
        EVENT_BUS.subscribe(eventClass, listener);
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
        EVENT_BUS.subscribe(object);
    }

    /**
     * <p>subscribeEvent.</p>
     *
     * @param eventClass a {@link java.lang.Class} object.
     * @param listener   a {@link ameba.event.Listener} object.
     * @param <E>        a E object.
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
     * @param <E>           a E object.
     * @return a {@link ameba.event.Listener} object.
     * @since 0.1.6e
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
     * @param <E>        a E object.
     */
    protected <E extends Event> void unsubscribeEvent(Class<E> eventClass, final Listener<E> listener) {
        locator.preDestroy(listener);
        EVENT_BUS.unsubscribe(eventClass, listener);
    }

    /**
     * <p>subscribeSystemEvent.</p>
     *
     * @param eventClass a {@link java.lang.Class} object.
     * @param listener   a {@link ameba.event.Listener} object.
     * @param <E>        a E object.
     * @since 0.1.6e
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
     * @param <E>        a E object.
     * @since 0.1.6e
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
     * @param <E>           a E object.
     * @return a {@link ameba.event.Listener} object.
     * @since 0.1.6e
     */
    protected <E extends Event> Listener subscribeSystemEvent(Class<E> eventClass, final Class<? extends Listener<E>> listenerClass) {
        Listener<E> listener = locator.createAndInitialize(listenerClass);
        SystemEventBus.subscribe(eventClass, listener);
        return listener;
    }
}
