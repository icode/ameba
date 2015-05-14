package ameba.db.ebean.jackson;

import com.avaje.ebean.text.json.JsonContext;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;

/**
 * Finds JsonDeserializer implementations for entity beans or entity bean collections.
 */
class FindDeserializers extends Deserializers.Base {

    final JsonContext jsonContext;

    /**
     * Construct with the given JsonContext.
     */
    FindDeserializers(JsonContext jsonContext) {
        this.jsonContext = jsonContext;
    }

    @Override
    public JsonDeserializer<?> findBeanDeserializer(JavaType type,
                                                    DeserializationConfig config,
                                                    BeanDescription beanDesc) throws JsonMappingException {

        if (jsonContext.isSupportedType(type.getRawClass())) {
            return new BeanTypeDeserializer(jsonContext, type.getRawClass());
        }
        return null;
    }

    @Override
    public JsonDeserializer<?> findCollectionDeserializer(CollectionType type,
                                                          DeserializationConfig config,
                                                          BeanDescription beanDesc,
                                                          TypeDeserializer elementTypeDeserializer,
                                                          JsonDeserializer<?> elementDeserializer)
            throws JsonMappingException {
        // todo this is a ebean bug
//        Class clazz = type.getContentType().getRawClass();
//        if (Collection.class.isAssignableFrom(type.getRawClass())
//                && jsonContext.isSupportedType(clazz)) {
//            return new BeanListTypeDeserializer(jsonContext, clazz);
//        }
        return null;
    }


}
