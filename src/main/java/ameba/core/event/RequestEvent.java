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
 * @author icode
 */
public class RequestEvent implements Event {
    private org.glassfish.jersey.server.monitoring.RequestEvent event;

    public RequestEvent(org.glassfish.jersey.server.monitoring.RequestEvent event) {
        this.event = event;
    }

    public Type getType() {
        return event.getType();
    }

    public org.glassfish.jersey.server.monitoring.RequestEvent.ExceptionCause getExceptionCause() {
        return event.getExceptionCause();
    }

    public Iterable<ContainerResponseFilter> getContainerResponseFilters() {
        return event.getContainerResponseFilters();
    }

    public ContainerRequest getContainerRequest() {
        return event.getContainerRequest();
    }

    public boolean isResponseSuccessfullyMapped() {
        return event.isResponseSuccessfullyMapped();
    }

    public Iterable<ContainerRequestFilter> getContainerRequestFilters() {
        return event.getContainerRequestFilters();
    }

    public boolean isResponseWritten() {
        return event.isResponseWritten();
    }

    public boolean isSuccess() {
        return event.isSuccess();
    }

    public Throwable getException() {
        return event.getException();
    }

    public ExceptionMapper<?> getExceptionMapper() {
        return event.getExceptionMapper();
    }

    public ExtendedUriInfo getUriInfo() {
        return event.getUriInfo();
    }

    public ContainerResponse getContainerResponse() {
        return event.getContainerResponse();
    }
}
