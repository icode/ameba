package ameba.websocket.internal;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

/**
 * <p>MessageState class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
public class MessageState {
    private Object message;
    private Boolean last;
    private Throwable throwable;
    private CloseReason closeReason;
    private Session session;
    private EndpointConfig endpointConfig;

    private MessageState(Session session, EndpointConfig config) {
        this.session = session;
        this.endpointConfig = config;
    }

    /**
     * <p>builder.</p>
     *
     * @param session a {@link javax.websocket.Session} object.
     * @param config  a {@link javax.websocket.EndpointConfig} object.
     * @return a {@link ameba.websocket.internal.MessageState.Builder} object.
     */
    public static Builder builder(Session session, EndpointConfig config) {
        return new Builder(session, config);
    }

    /**
     * <p>from.</p>
     *
     * @param state a {@link ameba.websocket.internal.MessageState} object.
     * @return a {@link ameba.websocket.internal.MessageState.Builder} object.
     */
    public static Builder from(MessageState state) {
        return new Builder(state, true);
    }

    /**
     * <p>Getter for the field <code>throwable</code>.</p>
     *
     * @return a {@link java.lang.Throwable} object.
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * <p>Getter for the field <code>message</code>.</p>
     *
     * @return a {@link java.lang.Object} object.
     */
    public Object getMessage() {
        return message;
    }

    /**
     * <p>Getter for the field <code>last</code>.</p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean getLast() {
        return last;
    }

    /**
     * <p>Getter for the field <code>session</code>.</p>
     *
     * @return a {@link javax.websocket.Session} object.
     */
    public Session getSession() {
        return session;
    }

    /**
     * <p>Getter for the field <code>closeReason</code>.</p>
     *
     * @return a {@link javax.websocket.CloseReason} object.
     */
    public CloseReason getCloseReason() {
        return closeReason;
    }

    /**
     * <p>Getter for the field <code>endpointConfig</code>.</p>
     *
     * @return a {@link javax.websocket.EndpointConfig} object.
     */
    public EndpointConfig getEndpointConfig() {
        return endpointConfig;
    }

    Builder change() {
        return new Builder(this, false);
    }

    public static class Builder {
        private MessageState state;

        public Builder(Session session, EndpointConfig config) {
            this(new MessageState(session, config), false);
        }

        private Builder(MessageState state, boolean create) {
            if (create) {
                this.state = new MessageState(state.session, state.endpointConfig);
            } else {
                this.state = state;
            }
        }

        public MessageState build() {
            return state;
        }

        public Builder message(Object message) {
            state.message = message;
            return this;
        }

        public Builder last(boolean last) {
            state.last = last;
            return this;
        }

        public Builder throwable(Throwable throwable) {
            state.throwable = throwable;
            return this;
        }

        public Builder closeReason(CloseReason closeReason) {
            state.closeReason = closeReason;
            return this;
        }
    }
}
