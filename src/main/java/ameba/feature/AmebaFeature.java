package ameba.feature;

import ameba.container.Container;
import ameba.event.Event;
import ameba.event.EventBus;
import ameba.event.Listener;
import org.glassfish.hk2.api.ServiceLocator;

import javax.inject.Inject;
import javax.ws.rs.core.Feature;

/**
 * @author icode
 */
public abstract class AmebaFeature implements Feature {

    private static EventBus EVENT_BUS;

    private static void init() {
        EVENT_BUS = EventBus.createMix("ameba-feature");

        EVENT_BUS.subscribe(Container.BeginReloadEvent.class,
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
        EVENT_BUS.subscribe(eventClass, listener);
    }

    protected <E extends Event> Listener subscribeEvent(Class<E> eventClass, final Class<? extends Listener<E>> listenerClass) {
        Listener<E> listener = locator.createAndInitialize(listenerClass);
        EVENT_BUS.subscribe(eventClass, listener);
        return listener;
    }

    protected <E extends Event> void unsubscribeEvent(Class<E> eventClass, final Listener<E> listener) {
        EVENT_BUS.unsubscribe(eventClass, listener);
    }

    public static void publishEvent(Event event) {
        EVENT_BUS.publish(event);
    }
}
