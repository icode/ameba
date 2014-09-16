package ameba.websocket;

import ameba.websocket.internal.WebSocketBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * @author icode
 */
public class WebSocketFeature implements Feature {

    private static Logger logger = LoggerFactory.getLogger(WebSocketFeature.class);

    @Override
    public boolean configure(FeatureContext context) {

//        final Configuration config = context.getConfiguration();
//
//        if (!config.isRegistered(WebSocketBinder.class)) {
            context.register(new WebSocketBinder());
            return true;
//        }
//
//        return false;
    }
}
