package ameba.db.ebean.jackson;

import com.avaje.ebean.text.json.JsonContext;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Deserialize entity beans of a given type.
 */
public class BeanTypeDeserializer extends JsonDeserializer {

    final JsonContext jsonContext;

    final Class<?> beanType;

    public BeanTypeDeserializer(JsonContext jsonContext, Class<?> beanType) {
        this.jsonContext = jsonContext;
        this.beanType = beanType;
    }

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        return jsonContext.toBean(beanType, jsonParser);
    }
}
