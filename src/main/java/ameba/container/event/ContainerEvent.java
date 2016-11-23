package ameba.container.event;

import ameba.container.Container;
import ameba.core.Application;
import ameba.event.Event;

/**
 * <p>ContainerEvent class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
public class ContainerEvent implements Event {
    private Container container;
    private Application app;

    /**
     * <p>Constructor for ContainerEvent.</p>
     *
     * @param container a {@link ameba.container.Container} object.
     * @param app       a {@link ameba.core.Application} object.
     */
    public ContainerEvent(Container container, Application app) {
        this.container = container;
        this.app = app;
    }

    /**
     * <p>Getter for the field <code>container</code>.</p>
     *
     * @return a {@link ameba.container.Container} object.
     */
    public Container getContainer() {
        return container;
    }

    /**
     * <p>Getter for the field <code>app</code>.</p>
     *
     * @return a {@link ameba.core.Application} object.
     */
    public Application getApp() {
        return app;
    }
}
