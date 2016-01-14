package ameba.container.event;

import ameba.container.Container;
import ameba.core.Application;

/**
 * @author icode
 */
public class StartEvent extends ContainerEvent {

    public StartEvent(Container container, Application app) {
        super(container, app);
    }
}