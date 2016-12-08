package ameba.container.event;

import ameba.container.Container;
import ameba.core.Application;

/**
 * <p>StartEvent class.</p>
 *
 * @author icode
 *
 */
public class StartEvent extends ContainerEvent {

    /**
     * <p>Constructor for StartEvent.</p>
     *
     * @param container a {@link ameba.container.Container} object.
     * @param app       a {@link ameba.core.Application} object.
     */
    public StartEvent(Container container, Application app) {
        super(container, app);
    }
}
