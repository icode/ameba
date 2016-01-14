package ameba.container.event;

import ameba.container.Container;
import ameba.core.Application;

/**
 * @author icode
 */
public class StartupEvent extends ContainerEvent {
    public StartupEvent(Container container, Application app) {
        super(container, app);
    }
}