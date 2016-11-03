package ameba.websocket.internal;

import javax.websocket.MessageHandler;

/**
 * @author icode
 */
interface TypeMessageHandler extends MessageHandler {

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