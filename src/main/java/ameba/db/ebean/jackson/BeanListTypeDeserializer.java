package ameba.db.ebean.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.ebean.text.json.JsonContext;

import java.io.IOException;

/**
 * Deserialize entity bean collections for a given bean type.
 *
 * @author icode
 *
 */
public class BeanListTypeDeserializer extends JsonDeserializer {

    private final JsonContext jsonContext;

    private final Class<?> beanType;

    /**
     * <p>Constructor for BeanListTypeDeserializer.</p>
     *
     * @param jsonContext a {@link io.ebean.text.json.JsonContext} object.
     * @param beanType    a {@link java.lang.Class} object.
     */
    public BeanListTypeDeserializer(JsonContext jsonContext, Class<?> beanType) {
        this.jsonContext = jsonContext;
        this.beanType = beanType;
    }

    /** {@inheritDoc} */
    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        return jsonContext.toList(beanType, jsonParser);
    }
}
