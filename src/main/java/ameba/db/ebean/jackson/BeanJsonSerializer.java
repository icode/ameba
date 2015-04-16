package ameba.db.ebean.jackson;

import ameba.db.ebean.EbeanUtils;
import com.avaje.ebean.text.json.JsonContext;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * JsonSerializer for beans.
 */
public class BeanJsonSerializer<T> extends JsonSerializer<T> {

    final JsonContext jsonContext;

    public BeanJsonSerializer(JsonContext jsonContext) {
        this.jsonContext = jsonContext;
    }

    @Override
    public void serialize(T o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonContext.toJson(o, jsonGenerator, EbeanUtils.getCurrentRequestPathProperties());
    }
}
