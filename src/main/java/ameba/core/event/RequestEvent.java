package ameba.core.event;

import ameba.event.Event;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.monitoring.RequestEvent.Type;

import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * <p>RequestEvent class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
public class RequestEvent implements Event {
    private org.glassfish.jersey.server.monitoring.RequestEvent event;

    /**
     * <p>Constructor for RequestEvent.</p>
     *
     * @param event a {@link org.glassfish.jersey.server.monitoring.RequestEvent} object.
     */
    public RequestEvent(org.glassfish.jersey.server.monitoring.RequestEvent event) {
        this.event = event;
    }

    /**
     * <p>getType.</p>
     *
     * @return a {@link org.glassfish.jersey.server.monitoring.RequestEvent.Type} object.
     */
    public Type getType() {
        return event.getType();
    }

    /**
     * <p>getExceptionCause.</p>
     *
     * @return a {@link org.glassfish.jersey.server.monitoring.RequestEvent.ExceptionCause} object.
     */
    public org.glassfish.jersey.server.monitoring.RequestEvent.ExceptionCause getExceptionCause() {
        return event.getExceptionCause();
    }

    /**
     * <p>getContainerResponseFilters.</p>
     *
     * @return a {@link java.lang.Iterable} object.
     */
    public Iterable<ContainerResponseFilter> getContainerResponseFilters() {
        return event.getContainerResponseFilters();
    }

    /**
     * <p>getContainerRequest.</p>
     *
     * @return a {@link org.glassfish.jersey.server.ContainerRequest} object.
     */
    public ContainerRequest getContainerRequest() {
        return event.getContainerRequest();
    }

    /**
     * <p>isResponseSuccessfullyMapped.</p>
     *
     * @return a boolean.
     */
    public boolean isResponseSuccessfullyMapped() {
        return event.isResponseSuccessfullyMapped();
    }

    /**
     * <p>getContainerRequestFilters.</p>
     *
     * @return a {@link java.lang.Iterable} object.
     */
    public Iterable<ContainerRequestFilter> getContainerRequestFilters() {
        return event.getContainerRequestFilters();
    }

    /**
     * <p>isResponseWritten.</p>
     *
     * @return a boolean.
     */
    public boolean isResponseWritten() {
        return event.isResponseWritten();
    }

    /**
     * <p>isSuccess.</p>
     *
     * @return a boolean.
     */
    public boolean isSuccess() {
        return event.isSuccess();
    }

    /**
     * <p>getException.</p>
     *
     * @return a {@link java.lang.Throwable} object.
     */
    public Throwable getException() {
        return event.getException();
    }

    /**
     * <p>getExceptionMapper.</p>
     *
     * @return a {@link javax.ws.rs.ext.ExceptionMapper} object.
     */
    public ExceptionMapper<?> getExceptionMapper() {
        return event.getExceptionMapper();
    }

    /**
     * <p>getUriInfo.</p>
     *
     * @return a {@link org.glassfish.jersey.server.ExtendedUriInfo} object.
     */
    public ExtendedUriInfo getUriInfo() {
        return event.getUriInfo();
    }

    /**
     * <p>getContainerResponse.</p>
     *
     * @return a {@link org.glassfish.jersey.server.ContainerResponse} object.
     */
    public ContainerResponse getContainerResponse() {
        return event.getContainerResponse();
    }
}
