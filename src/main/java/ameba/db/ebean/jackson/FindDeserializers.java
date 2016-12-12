package ameba.db.ebean.jackson;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import io.ebean.text.json.JsonContext;

import java.util.Collection;

/**
 * Finds JsonDeserializer implementations for entity beans or entity bean collections.
 */
class FindDeserializers extends Deserializers.Base {

    private final JsonContext jsonContext;

    /**
     * Construct with the given JsonContext.
     */
    FindDeserializers(JsonContext jsonContext) {
        this.jsonContext = jsonContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonDeserializer<?> findBeanDeserializer(JavaType type,
                                                    DeserializationConfig config,
                                                    BeanDescription beanDesc) throws JsonMappingException {

        if (jsonContext.isSupportedType(type.getRawClass())) {
            return new BeanTypeDeserializer(jsonContext, type.getRawClass());
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public JsonDeserializer<?> findCollectionDeserializer(CollectionType type,
                                                          DeserializationConfig config,
                                                          BeanDescription beanDesc,
                                                          TypeDeserializer elementTypeDeserializer,
                                                          JsonDeserializer<?> elementDeserializer)
            throws JsonMappingException {
        Class clazz = type.getContentType().getRawClass();
        if (Collection.class.isAssignableFrom(type.getRawClass())
                && jsonContext.isSupportedType(clazz)) {
            return new BeanListTypeDeserializer(jsonContext, clazz);
        }
        return null;
    }


}
