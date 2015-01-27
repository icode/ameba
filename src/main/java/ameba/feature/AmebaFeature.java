package ameba.feature;

import ameba.container.Container;
import ameba.event.Event;
import ameba.event.EventBus;
import ameba.event.Listener;
import ameba.event.SystemEventBus;
import org.glassfish.hk2.api.ServiceLocator;

import javax.inject.Inject;
import javax.ws.rs.core.Feature;

/**
 * @author icode
 */
public abstract class AmebaFeature implements Feature {

    private static EventBus EVENT_BUS;

    private static void init() {
        EVENT_BUS = EventBus.createMix();

        SystemEventBus.subscribe(Container.BeginReloadEvent.class,
                new Listener<Container.BeginReloadEvent>() {
                    @Override
                    public void onReceive(Container.BeginReloadEvent event) {
                        init();
                    }
                });
    }

    static {
        init();
    }

    @Inject
    private ServiceLocator locator;

    protected <E extends Event> void subscribeEvent(Class<E> eventClass, final Listener<E> listener) {
        locator.inject(listener);
        locator.postConstruct(listener);
        EVENT_BUS.subscribe(eventClass, listener);
    }

    protected <E extends Event> Listener subscribeEvent(Class<E> eventClass, final Class<? extends Listener<E>> listenerClass) {
        Listener<E> listener = locator.createAndInitialize(listenerClass);
        EVENT_BUS.subscribe(eventClass, listener);
        return listener;
    }

    protected <E extends Event> void unsubscribeEvent(Class<E> eventClass, final Listener<E> listener) {
        locator.preDestroy(listener);
        EVENT_BUS.unsubscribe(eventClass, listener);
    }

    protected <E extends Event> void subscribeSystemEvent(Class<E> eventClass, final Listener<E> listener) {
        locator.inject(listener);
        locator.postConstruct(listener);
        SystemEventBus.subscribe(eventClass, listener);
    }

    protected <E extends Event> void unsubscribeSystemEvent(Class<E> eventClass, final Listener<E> listener) {
        locator.preDestroy(listener);
        SystemEventBus.unsubscribe(eventClass, listener);
    }

    protected <E extends Event> Listener subscribeSystemEvent(Class<E> eventClass, final Class<? extends Listener<E>> listenerClass) {
        Listener<E> listener = locator.createAndInitialize(listenerClass);
        SystemEventBus.subscribe(eventClass, listener);
        return listener;
    }

    public static void publishEvent(Event event) {
        EVENT_BUS.publish(event);
    }
}
