package ameba.websocket.internal;

import ameba.websocket.WebSocketSession;

import javax.websocket.CloseReason;
import javax.websocket.Extension;
import javax.websocket.Session;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * @author icode
 */
public class NativeWebSocketSession implements WebSocketSession {

    private Session session;


    @Override
    public String getId() {
        return null;
    }

    @Override
    public URI getUri() {
        return null;
    }

    @Override
    public HttpHeaders getHandshakeHeaders() {
        return null;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public <T> T getAttribute(String key) {
        return null;
    }

    @Override
    public void setAttribute(String key, Object value) {

    }

    @Override
    public Principal getPrincipal() {
        return null;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return null;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return null;
    }

    @Override
    public String getAcceptedProtocol() {
        return null;
    }

    @Override
    public int getTextMessageSizeLimit() {
        return 0;
    }

    @Override
    public void setTextMessageSizeLimit(int messageSizeLimit) {

    }

    @Override
    public int getBinaryMessageSizeLimit() {
        return 0;
    }

    @Override
    public void setBinaryMessageSizeLimit(int messageSizeLimit) {

    }

    @Override
    public List<Extension> getExtensions() {
        return null;
    }

    @Override
    public void sendMessage(Object message) throws IOException {

    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public void close(CloseReason reason) throws IOException {

    }
}
