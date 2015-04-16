package ameba.db.ebean.jackson;

import com.avaje.ebean.text.json.JsonContext;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Deserialize entity bean collections for a given bean type.
 */
public class BeanListTypeDeserializer extends JsonDeserializer {

    final JsonContext jsonContext;

    final Class<?> beanType;

    public BeanListTypeDeserializer(JsonContext jsonContext, Class<?> beanType) {
        this.jsonContext = jsonContext;
        this.beanType = beanType;
    }

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        return jsonContext.toList(beanType, jsonParser);
    }
}
