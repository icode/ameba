package ameba.websocket;

import org.glassfish.jersey.server.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.lang.reflect.Method;

/**
 * @author icode
 */
public class WebSocketFeature implements Feature {

    private static Logger logger = LoggerFactory.getLogger(WebSocketFeature.class);

    @Override
    public boolean configure(FeatureContext context) {

        context.register(new ModelProcessor() {
            @Override
            public ResourceModel processResourceModel(ResourceModel resourceModel, Configuration configuration) {

                resourceModel.accept(new AbstractResourceModelVisitor() {
                    @Override
                    public void visitInvocable(Invocable invocable) {
                        Method method = invocable.getHandlingMethod();
                        WebSocket webSocketConf = method.getAnnotation(WebSocket.class);
                        if (webSocketConf != null) {
                            logger.trace("find WebSocket handler {}.{}", method.getDeclaringClass(), method.toGenericString());
                        }
                    }

                    @Override
                    public void visitResourceMethod(ResourceMethod method) {
                        Invocable invocable = method.getInvocable();
                        Method m = invocable.getHandlingMethod();
                        WebSocket webSocketConf = m.getAnnotation(WebSocket.class);
                        if (webSocketConf != null) {
                            logger.trace("find WebSocket handler {}.{}", m.getDeclaringClass(), m.toGenericString());
                        }
                    }
                });

                return resourceModel;
            }

            @Override
            public ResourceModel processSubResource(ResourceModel subResourceModel, Configuration configuration) {
                return subResourceModel;
            }
        });

        return true;
    }
}
