package ameba.db.ebean.jackson;

import ameba.db.ebean.EbeanPathProps;
import ameba.db.ebean.EbeanUtils;
import ameba.message.internal.BeanPathProperties;
import com.avaje.ebean.FetchPath;
import com.avaje.ebean.text.PathProperties;
import com.avaje.ebean.text.json.JsonContext;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Serialise entity beans or collections.
 * <p>
 * Simply delegates to Ebean's JsonContext.
 * </p>
 *
 * @author icode
 *
 */
public class CommonBeanSerializer<T> extends JsonSerializer<T> {

    private final JsonContext jsonContext;


    /**
     * Construct with the given JsonContext.
     */
    CommonBeanSerializer(JsonContext jsonContext) {
        this.jsonContext = jsonContext;
    }

    /**
     * only first call return FetchPath
     * <p>
     * and then call is bean sub-path (property)
     *
     * @return fetch path or null
     */
    private FetchPath getPathProperties(JsonGenerator jsonGenerator) {
        FetchPath fetchPath = EbeanUtils.getRequestFetchPath();
        if (fetchPath != null) {
            JsonStreamContext context = jsonGenerator.getOutputContext();
            JsonStreamContext parent = context.getParent();
            if (parent == null) {
                return fetchPath;
            }
            StringBuilder path = new StringBuilder();
            while (parent != null && !parent.inRoot()) {
                if (parent != context.getParent()) {
                    path.insert(0, '.');
                }
                path.insert(0, parent.getCurrentName());
                parent = parent.getParent();
            }
            String fp = path.toString();
            PathProperties fetch = new PathProperties();
            EbeanPathProps src = (EbeanPathProps) fetchPath;
            String cp = fp + ".";
            for (BeanPathProperties.Props prop : src.getPathProps()) {
                String pp = prop.getPath();
                if (pp.equals(fp)) {
                    addToFetchPath(fetch, null, prop);
                } else if (pp.startsWith(cp)) {
                    addToFetchPath(fetch, pp.substring(cp.length()), prop);
                }
            }

            return fetch;
        }
        return null;
    }

    private void addToFetchPath(PathProperties fetchPath, String path, BeanPathProperties.Props prop) {
        for (String p : prop.getProperties()) {
            fetchPath.addToPath(path, p);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Serialize entity beans or collections.
     */
    @Override
    public void serialize(T o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        final FetchPath pathProperties = getPathProperties(jsonGenerator);

        if (pathProperties != null) {
            jsonContext.toJson(o, jsonGenerator, pathProperties);
        } else {
            jsonContext.toJson(o, jsonGenerator);
        }
    }
}
