package ameba.core.event;

import ameba.event.Event;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.ResourceModel;
import org.glassfish.jersey.server.monitoring.ApplicationEvent.Type;

import java.util.Set;

/**
 * @author icode
 */
public class ApplicationEvent implements Event {

    private org.glassfish.jersey.server.monitoring.ApplicationEvent event;

    public ApplicationEvent(org.glassfish.jersey.server.monitoring.ApplicationEvent event) {
        this.event = event;
    }

    public Type getType() {
        return event.getType();
    }

    public ResourceConfig getResourceConfig() {
        return event.getResourceConfig();
    }

    public ResourceModel getResourceModel() {
        return event.getResourceModel();
    }

    public Set<Class<?>> getProviders() {
        return event.getProviders();
    }

    public Set<Object> getRegisteredInstances() {
        return event.getRegisteredInstances();
    }

    public Set<Class<?>> getRegisteredClasses() {
        return event.getRegisteredClasses();
    }
}