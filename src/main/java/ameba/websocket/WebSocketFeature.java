package ameba.websocket;

import ameba.websocket.internal.WebSocketBinder;
import ameba.websocket.internal.WebSocketModelProcessor;
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

    private static boolean enabled;

    public static boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean configure(FeatureContext context) {
        final Configuration config = context.getConfiguration();

        if (config.isEnabled(this.getClass())) {
            return false;
        }

        enabled = !"false".equals(config.getProperty(WEB_SOCKET_ENABLED_CONF));

        if (!enabled) {
            logger.debug("WebSocket 未启用");
            return false;
        }

        context.register(new WebSocketBinder());
        context.register(WebSocketModelProcessor.class);

        return true;
    }
}
