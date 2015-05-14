package ameba.db.ebean.jackson;

import com.avaje.ebean.text.json.JsonContext;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.util.Collection;

/**
 * Finds JsonSerializer's for entity bean types or entity bean collection types.
 */
class FindSerializers extends Serializers.Base {

    final JsonContext jsonContext;

    final CommonBeanSerializer serialiser;

    FindSerializers(JsonContext jsonContext) {
        this.jsonContext = jsonContext;
        this.serialiser = new CommonBeanSerializer(jsonContext);
    }

    @Override
    public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {

        if (jsonContext.isSupportedType(type.getRawClass())) {
            return serialiser;
        }

        return null;
    }

    @Override
    public JsonSerializer<?> findCollectionSerializer(SerializationConfig config, CollectionType type, BeanDescription
            beanDesc, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {

        if (Collection.class.isAssignableFrom(type.getRawClass())
                && jsonContext.isSupportedType(type.getContentType().getRawClass())) {
            return serialiser;
        }

        return null;
    }

}
