package ameba.websocket.internal;

import ameba.websocket.WebSocket;
import ameba.websocket.WebSocketExcption;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.ws.rs.core.Configuration;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author icode
 */
@Priority(10000)
public class WebSocketModelProcessor implements ModelProcessor {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketModelProcessor.class);

    @Inject
    private ServiceLocator serviceLocator;
    @Inject
    private ServerContainer container;

    @Override
    public ResourceModel processResourceModel(ResourceModel resourceModel, Configuration configuration) {
        ResourceModel result = processModel(resourceModel.getRuntimeResourceModel());
        return result != null ? result : resourceModel;
    }

    @Override
    public ResourceModel processSubResource(ResourceModel subResourceModel, Configuration configuration) {
        return subResourceModel;
    }

    private ResourceModel processModel(final RuntimeResourceModel resourceModel) {
        for (RuntimeResource resource : resourceModel.getRuntimeResources()) {
            for (RuntimeResource child : resource.getChildRuntimeResources()) {
                List<ResourceMethod> resourceMethods = child.getResourceMethods();
                if (resourceMethods.isEmpty()) {
                    ResourceMethod resourceMethod = child.getResourceLocator();
                    if (resourceMethod != null)
                        processResource(child.getFullPathRegex(), resourceMethod);
                } else {
                    for (ResourceMethod resourceMethod : resourceMethods) {
                        processResource(child.getFullPathRegex(), resourceMethod);
                    }
                }
            }
        }
        return null;
    }

    private void processResource(String path, ResourceMethod resourceMethod) {
        Invocable invocation = resourceMethod.getInvocable();
        Method handlingMethod = invocation.getHandlingMethod();
        WebSocket webSocketConf = handlingMethod.getAnnotation(WebSocket.class);
        if (webSocketConf == null) {
            webSocketConf = invocation.getDefinitionMethod().getAnnotation(WebSocket.class);
        }
        if (webSocketConf != null) {
            logger.trace("find web socket in {} class, method {}", handlingMethod.getDeclaringClass().getName(), handlingMethod.toGenericString());
            try {
                container.addEndpoint(new DefaultServerEndpointConfig(serviceLocator, EndpointDelegate.class, path, webSocketConf));
            } catch (DeploymentException e) {
                throw new WebSocketExcption(e);
            }
        }
    }
}
