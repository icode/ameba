package ameba.db.ebean.jackson;

import ameba.core.Requests;
import ameba.db.ebean.EbeanUtils;
import com.avaje.ebean.FetchPath;
import com.avaje.ebean.text.json.JsonContext;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Serialise entity beans or collections.
 * <p>
 * Simply delegates to Ebean's JsonContext.
 * </p>
 */
public class CommonBeanSerializer<T> extends JsonSerializer<T> {

    private static final String REQ_PATH_PROPS = FindSerializers.class + ".currentRequestPathProperties";
    private final JsonContext jsonContext;


    /**
     * Construct with the given JsonContext.
     */
    CommonBeanSerializer(JsonContext jsonContext) {
        this.jsonContext = jsonContext;
    }

    /**
     * only first call return FetchPath
     * <p>
     * and then call is bean sub-path (property)
     *
     * @return fetch path or null
     */
    private FetchPath getPathProperties() {
        if (Requests.getProperty(REQ_PATH_PROPS) != null) return null;
        FetchPath pathProperties = EbeanUtils.getCurrentRequestPathProperties();
        Requests.setProperty(REQ_PATH_PROPS, pathProperties);
        return pathProperties;
    }

    /**
     * Serialize entity beans or collections.
     */
    @Override
    public void serialize(T o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        final FetchPath pathProperties = getPathProperties();

        if (pathProperties != null) {
            jsonContext.toJson(o, jsonGenerator, pathProperties);
        } else {
            jsonContext.toJson(o, jsonGenerator);
        }
    }
}
