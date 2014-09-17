package ameba.websocket;

import ameba.websocket.internal.WebSocketBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * @author icode
 */
public class WebSocketFeature implements Feature {

    public static final String WEB_SOCKET_ENABLED_CONF = "websocket.enabled";
    private static final Logger logger = LoggerFactory.getLogger(WebSocketFeature.class);

    @Override
    public boolean configure(FeatureContext context) {
        final Configuration config = context.getConfiguration();

        if (config.isEnabled(this.getClass())) {
            return false;
        }

        if (!"false".equals(config.getProperty(WEB_SOCKET_ENABLED_CONF))) {
            context.register(new WebSocketBinder());
            return true;
        }

        logger.debug("WebSocket 未启用");
        return false;
    }
}
