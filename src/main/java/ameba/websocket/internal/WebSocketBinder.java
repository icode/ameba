package ameba.websocket.internal;

import ameba.websocket.WebSocketFeature;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.internal.inject.ReferencingFactory;
import org.glassfish.jersey.internal.util.collection.Ref;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * <p>WebSocketBinder class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
public class WebSocketBinder extends AbstractBinder {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        if (WebSocketFeature.isEnabled()) {
            bindFactory(MessageStateReferencingFactory.class).to(MessageState.class)
                    .proxy(false).in(MessageScoped.class);
            bindFactory(ReferencingFactory.<MessageState>referenceFactory())
                    .to(EndpointDelegate.MESSAGE_STATE_TYPE).in(MessageScoped.class);

            bind(new MessageScope()).to(MessageScope.class);
        }
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
