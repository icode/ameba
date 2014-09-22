package ameba.websocket.internal;

import ameba.websocket.WebSocket;
import ameba.websocket.WebSocketException;
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
        ResourceModel result = processModel(resourceModel);
        return result != null ? result : resourceModel;
    }

    @Override
    public ResourceModel processSubResource(ResourceModel subResourceModel, Configuration configuration) {
        return subResourceModel;
    }

    private ResourceModel processModel(final ResourceModel resourceModel) {
        ResourceModel.Builder modelBuilder = new ResourceModel.Builder(false);
        for (RuntimeResource resource : resourceModel.getRuntimeResourceModel().getRuntimeResources()) {
            Resource newResource = processResource(resource);
            if (newResource.getChildResources().size() > 0)
                modelBuilder.addResource(newResource);
        }
        return modelBuilder.build();
    }

    private Resource processResource(RuntimeResource resource) {
        Resource.Builder resourceBuilder = Resource.builder(resource.getRegex());
        for (ResourceMethod resourceMethod : resource.getResourceMethods()) {
            addResourceMethod(resourceBuilder, resourceMethod);
        }
        if (resource.getResourceLocator() != null) {
            addResourceMethod(resourceBuilder, resource.getResourceLocator());
        }

        for (RuntimeResource child : resource.getChildRuntimeResources()) {
            Resource rs = processResource(child);
            if (rs.getResourceMethods().size() > 0
                    || rs.getResourceLocator() != null
                    || rs.getChildResources().size() > 0)
                resourceBuilder.addChildResource(rs);
        }

        return resourceBuilder.build();
    }

    private void addResourceMethod(Resource.Builder resourceBuilder, ResourceMethod resourceMethod) {
        if (resourceMethod.getInvocable().getHandlingMethod().isAnnotationPresent(WebSocket.class)) {
            processWebSocketEndpoint(resourceMethod);
        } else {
            resourceBuilder.addMethod(resourceMethod);
        }
    }

    private void processWebSocketEndpoint(ResourceMethod resourceMethod) {
        Invocable invocation = resourceMethod.getInvocable();
        Method handlingMethod = invocation.getHandlingMethod();
        WebSocket webSocketConf = handlingMethod.getAnnotation(WebSocket.class);
        if (webSocketConf == null) {
            webSocketConf = invocation.getDefinitionMethod().getAnnotation(WebSocket.class);
        }
        if (webSocketConf != null) {
            logger.trace("find web socket in {} class, method {}",
                    handlingMethod.getDeclaringClass().getName(), handlingMethod.toGenericString());
            try {
                StringBuilder path = new StringBuilder();
                Resource resource = resourceMethod.getParent();
                if (resource != null)
                    do {
                        if (path.length() != 0) path.insert(0, "/");
                        path.insert(0, resource.getPath());
                    } while ((resource = resource.getParent()) != null);
                container.addEndpoint(new DefaultServerEndpointConfig(serviceLocator, resourceMethod, path.toString(), webSocketConf));
            } catch (DeploymentException e) {
                throw new WebSocketException(e);
            }
        }
    }
}
