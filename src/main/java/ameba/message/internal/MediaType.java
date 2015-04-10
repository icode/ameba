package ameba.message.internal;

import java.util.Map;

/**
 * @author icode
 */
public class MediaType extends javax.ws.rs.core.MediaType {
    public static final String APPLICATION_PROTOBUF = "application/x-protobuf";
    public static final javax.ws.rs.core.MediaType APPLICATION_PROTOBUF_TYPE = new MediaType("application", "x-protobuf");
    public static final String APPLICATION_JSON_PATCH = "application/json-patch+json";
    public static final javax.ws.rs.core.MediaType APPLICATION_JSON_PATCH_TYPE = new MediaType("application", "json-patch+json");

    public MediaType(String type, String subtype, Map<String, String> parameters) {
        super(type, subtype, parameters);
    }
    public MediaType(String type, String subtype) {
        super(type, subtype);
    }

    public MediaType(String type, String subtype, String charset) {
        super(type, subtype, charset);
    }
    public MediaType() {
    }
}
