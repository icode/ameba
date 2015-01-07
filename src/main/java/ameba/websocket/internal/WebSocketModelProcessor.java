package ameba.websocket.internal;

import ameba.websocket.WebSocket;
import ameba.websocket.WebSocketException;
import ameba.websocket.WebSocketFeature;
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
        ResourceModel result = processModel(resourceModel);
        return result != null ? result : resourceModel;
    }

    @Override
    public ResourceModel processSubResource(ResourceModel subResourceModel, Configuration configuration) {
        return subResourceModel;
    }

    private ResourceModel processModel(final ResourceModel resourceModel) {
        ResourceModel.Builder modelBuilder = new ResourceModel.Builder(false);

        for (Resource resource : resourceModel.getRootResources()) {
            for (Class clazz : resource.getHandlerClasses()) {
                WebSocket webSocketConf = (WebSocket) clazz.getAnnotation(WebSocket.class);
                if (webSocketConf != null) {
                    logger.trace("find web socket dispatcher in {} class", clazz);
                /*try {
                    container.addEndpoint(new DefaultServerEndpointConfig(serviceLocator,
                            getResourcePath(res), webSocketConf));
                } catch (DeploymentException e) {
                    throw new WebSocketException(e);
                }*/
                }
            }
        }
        for (RuntimeResource resource : resourceModel.getRuntimeResourceModel().getRuntimeResources()) {
            Resource newResource = processResource(resource);
            if (hasResource(newResource))
                modelBuilder.addResource(newResource);
        }
        return modelBuilder.build();
    }

    private boolean hasResource(Resource resource) {
        return resource != null && (resource.getResourceMethods().size() > 0
                || resource.getResourceLocator() != null
                || resource.getChildResources().size() > 0);
    }

    private Resource processResource(RuntimeResource resource) {
        List<Resource> resourceList = resource.getResources();
        Resource firstResource = resourceList.get(0);
        Resource.Builder resourceBuilder = Resource.builder(firstResource.getPath());


        for (ResourceMethod resourceMethod : resource.getResourceMethods()) {
            addResourceMethod(resourceBuilder, resourceMethod);
        }
        if (resource.getResourceLocator() != null) {
            addResourceMethod(resourceBuilder, resource.getResourceLocator());
        }

        for (RuntimeResource child : resource.getChildRuntimeResources()) {
            Resource rs = processResource(child);
            if (hasResource(rs))
                resourceBuilder.addChildResource(rs);
        }

        return resourceBuilder.build();
    }

    private void addResourceMethod(Resource.Builder resourceBuilder, ResourceMethod resourceMethod) {
        if (resourceMethod.getInvocable().getHandlingMethod().isAnnotationPresent(WebSocket.class)) {
            if (WebSocketFeature.isEnabled())
                processWebSocketEndpoint(resourceMethod);
        } else {
            resourceBuilder.addMethod(resourceMethod);
        }
    }

    private String getResourcePath(Resource resource) {
        StringBuilder path = new StringBuilder();
        if (resource != null)
            do {
                if (path.length() != 0 && path.charAt(0) != '/') path.insert(0, "/");
                if (!resource.getPath().equals("/"))
                    path.insert(0, resource.getPath());
            } while ((resource = resource.getParent()) != null);
        return path.toString();
    }

    private String getResourcePath(ResourceMethod resourceMethod) {
        return getResourcePath(resourceMethod.getParent());
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
                container.addEndpoint(new DefaultServerEndpointConfig(serviceLocator, resourceMethod,
                        getResourcePath(resourceMethod), webSocketConf));
            } catch (DeploymentException e) {
                throw new WebSocketException(e);
            }
        }
    }
}
