package ameba.websocket.internal;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

/**
 * @author icode
 */
public class MessageState {
    private ThreadLocal<Object> message = new ThreadLocal<Object>();
    private ThreadLocal<Boolean> last = new ThreadLocal<Boolean>();
    private ThreadLocal<Throwable> throwable = new ThreadLocal<Throwable>();
    private ThreadLocal<CloseReason> closeReason = new ThreadLocal<CloseReason>();
    private Session session;
    private EndpointConfig endpointConfig;

    private MessageState(Session session, EndpointConfig config) {
        this.session = session;
        this.endpointConfig = config;
    }

    public static Builder builder(Session session, EndpointConfig config) {
        return new Builder(session, config);
    }

    public Throwable getThrowable() {
        return throwable.get();
    }

    public Object getMessage() {
        return message.get();
    }

    public Boolean getLast() {
        return last.get();
    }

    public Session getSession() {
        return session;
    }

    public CloseReason getCloseReason() {
        return closeReason.get();
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
            state.message.set(message);
            return this;
        }

        public Builder last(boolean last) {
            state.last.set(last);
            return this;
        }

        public Builder throwable(Throwable throwable) {
            state.throwable.set(throwable);
            return this;
        }

        public Builder closeReason(CloseReason closeReason) {
            state.closeReason.set(closeReason);
            return this;
        }
    }
}