package ameba.container.event;

import ameba.container.Container;
import ameba.core.Application;

/**
 * @author icode
 */
public class BeginShutdownEvent extends ContainerEvent {
    public BeginShutdownEvent(Container container, Application app) {
        super(container, app);
    }
}
