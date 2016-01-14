package ameba.container.event;

import ameba.container.Container;
import ameba.core.Application;

/**
 * @author icode
 */
public class ReloadedEvent extends ContainerEvent {
    public ReloadedEvent(Container container, Application app) {
        super(container, app);
    }
}