package ameba.message.internal.protobuf;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by icode on 14-4-8.
 */
@Provider
@Produces("application/x-protobuf")
public class ProtobufMessageBodyWriter extends AbstractProtobufProvider implements MessageBodyWriter<Object> {

    @Override
    public boolean isWriteable(Class aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    public long getSize(Object m, Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    public void writeTo(Object m, Class<?> type, Type genericType, Annotation[] annotations,
                        MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        LinkedBuffer buffer = LinkedBuffer.allocate(1024);
        if (List.class.isInstance(m)) {
            Schema schema = RuntimeSchema.getSchema(getListGenericType((List) m, genericType));
            ProtobufIOUtil.writeListTo(entityStream, (List) m, schema, buffer);
        } else {
            Schema schema = RuntimeSchema.getSchema(type);
            ProtobufIOUtil.writeTo(entityStream, m, schema, buffer);
        }
    }

}

