package ameba.websocket.sockjs.frame;

import ameba.util.Assert;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Represents a SockJS frame. Provides factory methods to create SockJS frames.
 *
 * @author icode
 */
public class SockJsFrame {

    /**
     * Constant <code>CHARSET</code>
     */
    public static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final SockJsFrame OPEN_FRAME = new SockJsFrame("o");

    private static final SockJsFrame HEARTBEAT_FRAME = new SockJsFrame("h");

    private static final SockJsFrame CLOSE_GO_AWAY_FRAME = closeFrame(3000, "Go away!");

    private static final SockJsFrame CLOSE_ANOTHER_CONNECTION_OPEN_FRAME = closeFrame(2010, "Another connection still open");


    private final SockJsFrameType type;

    private final String content;


    /**
     * Create a new instance frame with the given frame content.
     *
     * @param content the content, must be a non-empty and represent a valid SockJS frame
     */
    public SockJsFrame(String content) {
        Assert.isBlank(content);
        if ("o".equals(content)) {
            this.type = SockJsFrameType.OPEN;
            this.content = content;
        } else if ("h".equals(content)) {
            this.type = SockJsFrameType.HEARTBEAT;
            this.content = content;
        } else if (content.charAt(0) == 'a') {
            this.type = SockJsFrameType.MESSAGE;
            this.content = (content.length() > 1 ? content : "a[]");
        } else if (content.charAt(0) == 'm') {
            this.type = SockJsFrameType.MESSAGE;
            this.content = (content.length() > 1 ? content : "null");
        } else if (content.charAt(0) == 'c') {
            this.type = SockJsFrameType.CLOSE;
            this.content = (content.length() > 1 ? content : "c[]");
        } else {
            throw new IllegalArgumentException("Unexpected SockJS frame type in content=\"" + content + "\"");
        }
    }

    /**
     * <p>openFrame.</p>
     *
     * @return a {@link ameba.websocket.sockjs.frame.SockJsFrame} object.
     */
    public static SockJsFrame openFrame() {
        return OPEN_FRAME;
    }

    /**
     * <p>heartbeatFrame.</p>
     *
     * @return a {@link ameba.websocket.sockjs.frame.SockJsFrame} object.
     */
    public static SockJsFrame heartbeatFrame() {
        return HEARTBEAT_FRAME;
    }

    /**
     * <p>messageFrame.</p>
     *
     * @param codec    a {@link ameba.websocket.sockjs.frame.SockJsMessageCodec} object.
     * @param messages a {@link java.lang.String} object.
     * @return a {@link ameba.websocket.sockjs.frame.SockJsFrame} object.
     */
    public static SockJsFrame messageFrame(SockJsMessageCodec codec, String... messages) {
        String encoded = codec.encode(messages);
        return new SockJsFrame(encoded);
    }

    /**
     * <p>closeFrameGoAway.</p>
     *
     * @return a {@link ameba.websocket.sockjs.frame.SockJsFrame} object.
     */
    public static SockJsFrame closeFrameGoAway() {
        return CLOSE_GO_AWAY_FRAME;
    }

    /**
     * <p>closeFrameAnotherConnectionOpen.</p>
     *
     * @return a {@link ameba.websocket.sockjs.frame.SockJsFrame} object.
     */
    public static SockJsFrame closeFrameAnotherConnectionOpen() {
        return CLOSE_ANOTHER_CONNECTION_OPEN_FRAME;
    }

    /**
     * <p>closeFrame.</p>
     *
     * @param code   a int.
     * @param reason a {@link java.lang.String} object.
     * @return a {@link ameba.websocket.sockjs.frame.SockJsFrame} object.
     */
    public static SockJsFrame closeFrame(int code, String reason) {
        return new SockJsFrame("c[" + code + ",\"" + reason + "\"]");
    }


    /**
     * Return the SockJS frame type.
     *
     * @return a {@link ameba.websocket.sockjs.frame.SockJsFrameType} object.
     */
    public SockJsFrameType getType() {
        return this.type;
    }

    /**
     * Return the SockJS frame content, never {@code null}.
     *
     * @return a {@link java.lang.String} object.
     */
    public String getContent() {
        return this.content;
    }

    /**
     * Return the SockJS frame content as a byte array.
     *
     * @return an array of byte.
     */
    public byte[] getContentBytes() {
        return this.content.getBytes(CHARSET);
    }

    /**
     * Return data contained in a SockJS "message" and "close" frames. Otherwise
     * for SockJS "open" and "close" frames, which do not contain data, return
     * {@code null}.
     *
     * @return a {@link java.lang.String} object.
     */
    public String getFrameData() {
        if (SockJsFrameType.OPEN == getType() || SockJsFrameType.HEARTBEAT == getType()) {
            return null;
        } else {
            return getContent().substring(1);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        return this == other
                || other instanceof SockJsFrame
                && (this.type.equals(((SockJsFrame) other).type) && this.content.equals(((SockJsFrame) other).content));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.content.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String result = this.content;
        if (result.length() > 80) {
            result = result.substring(0, 80) + "...(truncated)";
        }
        return "SockJsFrame content='" + result.replace("\n", "\\n").replace("\r", "\\r") + "'";
    }

}
