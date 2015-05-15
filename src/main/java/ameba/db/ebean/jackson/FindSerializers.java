package ameba.db.ebean.jackson;

import ameba.db.ebean.EbeanUtils;
import com.avaje.ebean.text.PathProperties;
import com.avaje.ebean.text.json.JsonContext;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.glassfish.jersey.server.ContainerRequest;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Collection;

/**
 * Finds JsonSerializer's for entity bean types or entity bean collection types.
 */
class FindSerializers extends Serializers.Base {

    private static final String REQ_PATH_PROPS = FindSerializers.class + ".currentRequestPathProperties";
    final JsonContext jsonContext;
    @Inject
    private Provider<ContainerRequest> requestProvider;

    FindSerializers(JsonContext jsonContext) {
        this.jsonContext = jsonContext;
    }

    @Override
    public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {

        if (jsonContext.isSupportedType(type.getRawClass())) {
            return new CommonBeanSerializer(jsonContext, getPathProperties());
        }

        return null;
    }

    @Override
    public JsonSerializer<?> findCollectionSerializer(SerializationConfig config, CollectionType type, BeanDescription
            beanDesc, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {

        if (Collection.class.isAssignableFrom(type.getRawClass())
                && jsonContext.isSupportedType(type.getContentType().getRawClass())) {
            return new CommonBeanSerializer(jsonContext, getPathProperties());
        }

        return null;
    }


    private PathProperties getPathProperties() {
        if (requestProvider.get().getProperty(REQ_PATH_PROPS) != null) return null;
        PathProperties pathProperties = EbeanUtils.getCurrentRequestPathProperties();
        requestProvider.get().setProperty(REQ_PATH_PROPS, pathProperties);
        return pathProperties;
    }
}
