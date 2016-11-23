package ameba.container.event;

import ameba.container.Container;
import ameba.core.Application;

/**
 * <p>ShutdownEvent class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
public class ShutdownEvent extends ContainerEvent {
    /**
     * <p>Constructor for ShutdownEvent.</p>
     *
     * @param container a {@link ameba.container.Container} object.
     * @param app       a {@link ameba.core.Application} object.
     */
    public ShutdownEvent(Container container, Application app) {
        super(container, app);
    }
}
