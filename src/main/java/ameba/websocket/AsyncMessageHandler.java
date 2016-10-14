package ameba.websocket;

import javax.websocket.MessageHandler;

/**
 * @author icode
 */
interface AsyncMessageHandler extends MessageHandler.Partial {

    /**
     * Get type of handled message.
     *
     * @return type of handled message.
     */
    Class<?> getType();

    /**
     * Get max message size allowed for this message handler.
     *
     * @return max message size.
     * @see javax.websocket.OnMessage#maxMessageSize()
     */
    long getMaxMessageSize();
}

