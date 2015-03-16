package ameba.feature;

import ameba.container.Container;
import ameba.core.Application;
import ameba.event.Event;
import ameba.event.EventBus;
import ameba.event.Listener;
import ameba.event.SystemEventBus;
import com.google.common.collect.Lists;
import org.glassfish.hk2.api.ServiceLocator;

import javax.inject.Inject;
import javax.ws.rs.core.Feature;
import java.util.List;

/**
 * @author icode
 */
public abstract class AmebaFeature implements Feature {

    private static EventBus EVENT_BUS = EventBus.createMix();
    private static List<Class<? extends Event>> listeners;

    @Inject
    private ServiceLocator locator;
    @Inject
    private Application application;

    public static void publishEvent(Event event) {
        EVENT_BUS.publish(event);
    }

    private void initDev() {
        if (listeners == null) {
            listeners = Lists.newArrayList();
            SystemEventBus.subscribe(Container.BeginReloadEvent.class,
                    new Listener<Container.BeginReloadEvent>() {
                        @Override
                        public void onReceive(Container.BeginReloadEvent event) {
                            if (listeners != null) {
                                for (Class ev : listeners) {
                                    EVENT_BUS.unsubscribe(ev);
                                }
                                listeners.clear();
                            }
                        }
                    });
        }
    }

    private <E extends Event> void subscribe(Class<E> eventClass, final Listener<E> listener) {
        if (application.getMode().isDev()) {
            initDev();
            listeners.add(eventClass);
        }
        EVENT_BUS.subscribe(eventClass, listener);
    }

    public void subscribeEvent(Object object) {
        if (object instanceof Class) {
            object = locator.createAndInitialize((Class) object);
        } else {
            locator.inject(object);
            locator.postConstruct(object);
        }
        EVENT_BUS.subscribe(object);
    }

    protected <E extends Event> void subscribeEvent(Class<E> eventClass, final Listener<E> listener) {
        locator.inject(listener);
        locator.postConstruct(listener);
        subscribe(eventClass, listener);
    }

    protected <E extends Event> Listener subscribeEvent(Class<E> eventClass, final Class<? extends Listener<E>> listenerClass) {
        Listener<E> listener = locator.createAndInitialize(listenerClass);
        subscribe(eventClass, listener);
        return listener;
    }

    protected <E extends Event> void unsubscribeEvent(Class<E> eventClass, final Listener<E> listener) {
        locator.preDestroy(listener);
        if (application.getMode().isDev()) {
            listeners.remove(eventClass);
        }
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
}
