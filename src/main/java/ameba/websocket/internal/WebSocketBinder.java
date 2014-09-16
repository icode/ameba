package ameba.websocket.internal;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.model.ModelProcessor;

import javax.inject.Singleton;

/**
 * @author icode
 */
public class WebSocketBinder extends AbstractBinder {
    @Override
    protected void configure() {
        bind(WebSocketModelProcessor.class).to(ModelProcessor.class).in(Singleton.class);
    }
}
