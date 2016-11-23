package ameba.websocket.internal;

import ameba.websocket.WebSocketAddon;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * <p>WebSocketBinder class.</p>
 *
 * @author icode
 * @since 0.1.6e
 * @version $Id: $Id
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
