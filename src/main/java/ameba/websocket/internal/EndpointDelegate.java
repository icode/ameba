package ameba.websocket.internal;

import org.glassfish.jersey.server.ExtendedResourceContext;
import org.glassfish.jersey.server.model.ResourceMethod;

import javax.inject.Inject;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

/**
 * @author icode
 */
public class EndpointDelegate extends Endpoint {

    @Inject
    private ExtendedResourceContext resourceContext;

    private ResourceMethod resourceMethod;

    protected ExtendedResourceContext getResourceContext() {
        return resourceContext;
    }

    public ResourceMethod getResourceMethod() {
        return resourceMethod;
    }

    protected void setResourceMethod(ResourceMethod resourceMethod) {
        this.resourceMethod = resourceMethod;
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        session.addMessageHandler(new MessageHandler.Whole() {
            @Override
            public void onMessage(Object message) {

            }
        });
    }
}
