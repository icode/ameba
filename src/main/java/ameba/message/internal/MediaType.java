package ameba.message.internal;

import java.util.Map;

/**
 * <p>MediaType class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
public class MediaType extends javax.ws.rs.core.MediaType {
    /**
     * Constant <code>APPLICATION_PROTOBUF="application/x-protobuf"</code>
     */
    public static final String APPLICATION_PROTOBUF = "application/x-protobuf";
    /**
     * Constant <code>APPLICATION_PROTOBUF_TYPE</code>
     */
    public static final javax.ws.rs.core.MediaType APPLICATION_PROTOBUF_TYPE = new MediaType("application", "x-protobuf");
    /**
     * Constant <code>APPLICATION_JSON_PATCH="application/json-patch+json"</code>
     */
    public static final String APPLICATION_JSON_PATCH = "application/json-patch+json";
    /**
     * Constant <code>APPLICATION_JSON_PATCH_TYPE</code>
     */
    public static final javax.ws.rs.core.MediaType APPLICATION_JSON_PATCH_TYPE = new MediaType("application", "json-patch+json");

    /**
     * <p>Constructor for MediaType.</p>
     *
     * @param type       a {@link java.lang.String} object.
     * @param subtype    a {@link java.lang.String} object.
     * @param parameters a {@link java.util.Map} object.
     * @since 0.1.6e
     */
    public MediaType(String type, String subtype, Map<String, String> parameters) {
        super(type, subtype, parameters);
    }

    /**
     * <p>Constructor for MediaType.</p>
     *
     * @param type    a {@link java.lang.String} object.
     * @param subtype a {@link java.lang.String} object.
     * @since 0.1.6e
     */
    public MediaType(String type, String subtype) {
        super(type, subtype);
    }

    /**
     * <p>Constructor for MediaType.</p>
     *
     * @param type    a {@link java.lang.String} object.
     * @param subtype a {@link java.lang.String} object.
     * @param charset a {@link java.lang.String} object.
     * @since 0.1.6e
     */
    public MediaType(String type, String subtype, String charset) {
        super(type, subtype, charset);
    }

    /**
     * <p>Constructor for MediaType.</p>
     */
    public MediaType() {
    }
}
