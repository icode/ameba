package ameba.container.event;

import ameba.container.Container;
import ameba.core.Application;

/**
 * <p>StartupEvent class.</p>
 *
 * @author icode
 *
 */
public class StartupEvent extends ContainerEvent {
    /**
     * <p>Constructor for StartupEvent.</p>
     *
     * @param container a {@link ameba.container.Container} object.
     * @param app       a {@link ameba.core.Application} object.
     */
    public StartupEvent(Container container, Application app) {
        super(container, app);
    }
}
