package ameba.container.event;

import ameba.container.Container;
import ameba.core.Application;

/**
 * @author icode
 */
public class ShutdownEvent extends ContainerEvent {
    public ShutdownEvent(Container container, Application app) {
        super(container, app);
    }
}