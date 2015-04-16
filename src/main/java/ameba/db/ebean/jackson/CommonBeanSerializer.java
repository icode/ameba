package ameba.db.ebean.jackson;

import ameba.db.ebean.EbeanUtils;
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

    /**
     * Construct with the given JsonContext.
     */
    CommonBeanSerializer(JsonContext jsonContext) {
        this.jsonContext = jsonContext;
    }

    /**
     * Serialize entity beans or collections.
     */
    @Override
    public void serialize(T o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        // could look to pass/use the active view
        jsonContext.toJson(o, jsonGenerator, EbeanUtils.getCurrentRequestPathProperties());
    }
}
