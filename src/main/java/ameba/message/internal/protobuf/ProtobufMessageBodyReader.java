package ameba.message.internal.protobuf;

import ameba.message.internal.MediaType;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by icode on 14-4-8.
 */
@Provider
@Consumes(MediaType.APPLICATION_PROTOBUF)
public class ProtobufMessageBodyReader extends AbstractProtobufProvider implements MessageBodyReader<Object> {

    @Override
    public boolean isReadable(Class aClass, Type type, Annotation[] annotations, javax.ws.rs.core.MediaType mediaType) {
        return true;
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations,
                           javax.ws.rs.core.MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
                           InputStream entityStream) throws IOException, WebApplicationException {
        if (List.class.isAssignableFrom(type)) {
            Schema schema = RuntimeSchema.getSchema(getListGenericType((List) null, genericType));
            return ProtobufIOUtil.parseListFrom(entityStream, schema);
        } else {
            Schema schema = RuntimeSchema.getSchema(type);
            try {
                ProtobufIOUtil.mergeFrom(entityStream, type.newInstance(), schema);
            } catch (InstantiationException e) {
                throw new WebApplicationException(e);
            } catch (IllegalAccessException e) {
                throw new WebApplicationException(e);
            }
        }
        return null;
    }
}
