package ameba.websocket.internal;

import ameba.websocket.WebSocketAddon;
import org.glassfish.jersey.internal.inject.AbstractBinder;

/**
 * <p>WebSocketBinder class.</p>
 *
 * @author icode
 * @since 0.1.6e
 *
 */
public class WebSocketBinder extends AbstractBinder {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        if (WebSocketAddon.isEnabled()) {

        }
    }
}
