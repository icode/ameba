package ameba.websocket.internal;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.internal.inject.ReferencingFactory;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.server.model.ModelProcessor;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * @author icode
 */
public class WebSocketBinder extends AbstractBinder {
    @Override
    protected void configure() {
        bind(WebSocketModelProcessor.class).to(ModelProcessor.class).in(Singleton.class);

        bindFactory(MessageStateReferencingFactory.class).to(MessageState.class)
                .proxy(false).in(MessageScoped.class);
        bindFactory(ReferencingFactory.<MessageState>referenceFactory())
                .to(EndpointDelegate.MESSAGE_STATE_TYPE).in(MessageScoped.class);

        bind(new MessageScope()).to(MessageScope.class);
    }


    /**
     * Referencing factory for WebSocket message.
     */
    private static class MessageStateReferencingFactory extends ReferencingFactory<MessageState> {
        @Inject
        public MessageStateReferencingFactory(final Provider<Ref<MessageState>> referenceFactory) {
            super(referenceFactory);
        }
    }

}
