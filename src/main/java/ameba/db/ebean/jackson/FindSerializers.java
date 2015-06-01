package ameba.db.ebean.jackson;

import com.avaje.ebean.text.json.JsonContext;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.glassfish.hk2.api.ServiceLocator;

import javax.inject.Inject;
import java.util.Collection;

/**
 * Finds JsonSerializer's for entity bean types or entity bean collection types.
 */
class FindSerializers extends Serializers.Base {
    final JsonContext jsonContext;

    @Inject
    private ServiceLocator locator;

    FindSerializers(JsonContext jsonContext) {
        this.jsonContext = jsonContext;
    }

    @Override
    public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {

        if (jsonContext.isSupportedType(type.getRawClass())) {
            return createSerializer();
        }

        return null;
    }

    @Override
    public JsonSerializer<?> findCollectionSerializer(SerializationConfig config, CollectionType type, BeanDescription
            beanDesc, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {

        if (Collection.class.isAssignableFrom(type.getRawClass())
                && jsonContext.isSupportedType(type.getContentType().getRawClass())) {
            return createSerializer();
        }

        return null;
    }

    private JsonSerializer createSerializer() {
        JsonSerializer serializer = new CommonBeanSerializer(jsonContext);
        locator.postConstruct(serializer);
        locator.inject(serializer);
        return serializer;
    }

}
