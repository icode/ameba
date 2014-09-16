package ameba.websocket.internal;

import org.glassfish.jersey.server.ExtendedResourceContext;

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
    ExtendedResourceContext resourceContext;

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        session.addMessageHandler(new MessageHandler.Whole() {
            @Override
            public void onMessage(Object message) {

            }
        });
    }
}
