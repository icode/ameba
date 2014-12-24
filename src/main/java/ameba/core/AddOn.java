package ameba.core;

import ameba.event.EventBus;

/**
 * @author icode
 */
public abstract class AddOn {
    private static EventBus EVENT_BUS = EventBus.create();

    public static EventBus getEventBus() {
        return EVENT_BUS;
    }

    public void setup(Application application) {}

    public void done(Application application) {}
}
