package ameba.core;

import ameba.container.Container;
import ameba.event.EventBus;
import ameba.event.Listener;

/**
 * @author icode
 */
public abstract class AddOn {
    private static EventBus EVENT_BUS;

    private static void init() {
        EVENT_BUS = EventBus.create();

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

    public abstract void setup(Application application);

    public abstract void done(Application application);
}
