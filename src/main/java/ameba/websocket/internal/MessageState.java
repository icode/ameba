package ameba.websocket.internal;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

/**
 * @author icode
 */
public class MessageState {
    private Object message;
    private Boolean last;
    private Throwable throwable;
    private CloseReason closeReason;
    private Session session;
    private EndpointConfig endpointConfig;
    private boolean async;

    private MessageState(Session session, EndpointConfig config) {
        this.session = session;
        this.endpointConfig = config;
    }

    public static Builder builder(Session session, EndpointConfig config) {
        return new Builder(session, config);
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public Object getMessage() {
        return message;
    }

    public Boolean getLast() {
        return last;
    }

    public Session getSession() {
        return session;
    }

    public CloseReason getCloseReason() {
        return closeReason;
    }

    public Boolean isAsync() {
        return async;
    }

    public EndpointConfig getEndpointConfig() {
        return endpointConfig;
    }

    Builder change() {
        return new Builder(this);
    }

    public static class Builder {
        private MessageState state;

        public Builder(Session session, EndpointConfig config) {
            this(new MessageState(session, config));
        }

        private Builder(MessageState state) {
            this.state = state;
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

        public Builder async(Boolean async) {
            state.async = async;
            return this;
        }
    }
}