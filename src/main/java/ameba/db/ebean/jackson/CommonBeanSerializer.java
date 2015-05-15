package ameba.db.ebean.jackson;

import com.avaje.ebean.text.PathProperties;
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

    final JsonContext jsonContext;
    final PathProperties pathProperties;

    /**
     * Construct with the given JsonContext.
     */
    CommonBeanSerializer(JsonContext jsonContext, PathProperties pathProperties) {
        this.jsonContext = jsonContext;
        this.pathProperties = pathProperties;
    }

    /**
     * Serialize entity beans or collections.
     */
    @Override
    public void serialize(T o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        if (pathProperties != null) {
            jsonContext.toJson(o, jsonGenerator, pathProperties);
        } else {
            jsonContext.toJson(o, jsonGenerator);
        }
    }
}
