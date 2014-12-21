package ameba.event;

import ameba.container.Container;

/**
 * @author icode
 */
public class SystemEventBus {

    private static EventBus EVENT_BUS;

    private SystemEventBus() {
    }

    private static void init() {
        EVENT_BUS = EventBus.createMix("ameba-sys");

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

    public static <E extends Event> void subscribe(Class<E> event, final Listener<E> listener) {
        EVENT_BUS.subscribe(event, listener);
    }

    public static <E extends Event> void unsubscribe(Class<E> event, final Listener<E> listener) {
        EVENT_BUS.unsubscribe(event, listener);
    }

    public static void publish(Event event) {
        EVENT_BUS.publish(event);
    }
}
