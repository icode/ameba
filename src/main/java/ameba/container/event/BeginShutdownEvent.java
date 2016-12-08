package ameba.container.event;

import ameba.container.Container;
import ameba.core.Application;

/**
 * <p>BeginShutdownEvent class.</p>
 *
 * @author icode
 *
 */
public class BeginShutdownEvent extends ContainerEvent {
    /**
     * <p>Constructor for BeginShutdownEvent.</p>
     *
     * @param container a {@link ameba.container.Container} object.
     * @param app       a {@link ameba.core.Application} object.
     */
    public BeginShutdownEvent(Container container, Application app) {
        super(container, app);
    }
}
