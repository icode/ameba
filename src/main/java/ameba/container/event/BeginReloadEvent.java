package ameba.container.event;

import ameba.container.Container;
import ameba.core.Application;

/**
 * @author icode
 */
public class BeginReloadEvent extends ContainerEvent {

    public BeginReloadEvent(Container container, Application app) {
        super(container, app);
    }
}