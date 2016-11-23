package ameba.container.event;

import ameba.container.Container;
import ameba.core.Application;

/**
 * <p>BeginReloadEvent class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
public class BeginReloadEvent extends ContainerEvent {

    /**
     * <p>Constructor for BeginReloadEvent.</p>
     *
     * @param container a {@link ameba.container.Container} object.
     * @param app       a {@link ameba.core.Application} object.
     */
    public BeginReloadEvent(Container container, Application app) {
        super(container, app);
    }
}
