package ameba.container.event;

import ameba.container.Container;
import ameba.core.Application;
import ameba.event.Event;

/**
 * @author icode
 */
public class ContainerEvent implements Event {
    private Container container;
    private Application app;

    public ContainerEvent(Container container, Application app) {
        this.container = container;
        this.app = app;
    }

    public Container getContainer() {
        return container;
    }

    public Application getApp() {
        return app;
    }
}