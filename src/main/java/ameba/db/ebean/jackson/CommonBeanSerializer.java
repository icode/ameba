package ameba.db.ebean.jackson;

import ameba.db.ebean.EbeanUtils;
import com.avaje.ebean.text.PathProperties;
import com.avaje.ebean.text.json.JsonContext;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.glassfish.jersey.server.ContainerRequest;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;

/**
 * Serialise entity beans or collections.
 * <p>
 * Simply delegates to Ebean's JsonContext.
 * </p>
 */
public class CommonBeanSerializer<T> extends JsonSerializer<T> {

    private static final String REQ_PATH_PROPS = FindSerializers.class + ".currentRequestPathProperties";
    private final JsonContext jsonContext;
    @Inject
    private Provider<ContainerRequest> requestProvider;


    /**
     * Construct with the given JsonContext.
     */
    CommonBeanSerializer(JsonContext jsonContext) {
        this.jsonContext = jsonContext;
    }

    private PathProperties getPathProperties() {
        if (requestProvider.get().getProperty(REQ_PATH_PROPS) != null) return null;
        PathProperties pathProperties = EbeanUtils.getCurrentRequestPathProperties();
        requestProvider.get().setProperty(REQ_PATH_PROPS, pathProperties);
        return pathProperties;
    }

    /**
     * Serialize entity beans or collections.
     */
    @Override
    public void serialize(T o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        final PathProperties pathProperties = getPathProperties();

        if (pathProperties != null) {
            jsonContext.toJson(o, jsonGenerator, pathProperties);
        } else {
            jsonContext.toJson(o, jsonGenerator);
        }
    }
}
