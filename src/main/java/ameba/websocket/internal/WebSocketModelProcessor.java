package ameba.websocket.internal;

import org.glassfish.jersey.server.model.ModelProcessor;
import org.glassfish.jersey.server.model.ResourceModel;

import javax.ws.rs.core.Configuration;

/**
 * @author icode
 */
public class WebSocketModelProcessor implements ModelProcessor {
    @Override
    public ResourceModel processResourceModel(ResourceModel resourceModel, Configuration configuration) {
        return processModel(resourceModel, false);
    }

    @Override
    public ResourceModel processSubResource(ResourceModel subResourceModel, Configuration configuration) {
        return processModel(subResourceModel, true);
    }

    private ResourceModel processModel(final ResourceModel resourceModel, final boolean subResourceModel) {

        return resourceModel;
    }
}
