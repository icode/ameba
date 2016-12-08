package ameba.core.event;

import ameba.event.Event;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.ResourceModel;
import org.glassfish.jersey.server.monitoring.ApplicationEvent.Type;

import java.util.Set;

/**
 * <p>ApplicationEvent class.</p>
 *
 * @author icode
 *
 */
public class ApplicationEvent implements Event {

    private org.glassfish.jersey.server.monitoring.ApplicationEvent event;

    /**
     * <p>Constructor for ApplicationEvent.</p>
     *
     * @param event a {@link org.glassfish.jersey.server.monitoring.ApplicationEvent} object.
     */
    public ApplicationEvent(org.glassfish.jersey.server.monitoring.ApplicationEvent event) {
        this.event = event;
    }

    /**
     * <p>getType.</p>
     *
     * @return a {@link org.glassfish.jersey.server.monitoring.ApplicationEvent.Type} object.
     */
    public Type getType() {
        return event.getType();
    }

    /**
     * <p>getResourceConfig.</p>
     *
     * @return a {@link org.glassfish.jersey.server.ResourceConfig} object.
     */
    public ResourceConfig getResourceConfig() {
        return event.getResourceConfig();
    }

    /**
     * <p>getResourceModel.</p>
     *
     * @return a {@link org.glassfish.jersey.server.model.ResourceModel} object.
     */
    public ResourceModel getResourceModel() {
        return event.getResourceModel();
    }

    /**
     * <p>getProviders.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<Class<?>> getProviders() {
        return event.getProviders();
    }

    /**
     * <p>getRegisteredInstances.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<Object> getRegisteredInstances() {
        return event.getRegisteredInstances();
    }

    /**
     * <p>getRegisteredClasses.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<Class<?>> getRegisteredClasses() {
        return event.getRegisteredClasses();
    }
}
