package ameba.db.ebean.jackson;

import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.text.json.JsonContext;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.type.CollectionType;

/**
 * Finds and returns Serializer for Ebean entity beans or BeanCollection.
 */
public class BeanSerializers extends Serializers.Base {

    final JsonContext jsonContext;

    final BeanJsonSerializer serialiser;

    public BeanSerializers(JsonContext jsonContext) {

        this.jsonContext = jsonContext;
        this.serialiser = new BeanJsonSerializer(jsonContext);
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

        if (type.getRawClass().isAssignableFrom(BeanCollection.class)) {
            return serialiser;
        }

        return null;
    }

}
