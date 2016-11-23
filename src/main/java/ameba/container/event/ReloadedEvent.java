package ameba.container.event;

import ameba.container.Container;
import ameba.core.Application;

/**
 * <p>ReloadedEvent class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
public class ReloadedEvent extends ContainerEvent {
    /**
     * <p>Constructor for ReloadedEvent.</p>
     *
     * @param container a {@link ameba.container.Container} object.
     * @param app       a {@link ameba.core.Application} object.
     */
    public ReloadedEvent(Container container, Application app) {
        super(container, app);
    }
}
