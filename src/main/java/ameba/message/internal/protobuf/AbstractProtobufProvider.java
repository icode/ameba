package ameba.message.internal.protobuf;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by icode on 14-4-8.
 */
public class AbstractProtobufProvider {

    protected Class getListGenericType(List list, Type genericType) throws IOException {
        if (genericType instanceof ParameterizedType) {
            ParameterizedType listType = (ParameterizedType) genericType;
            return (Class<?>) listType.getActualTypeArguments()[0];
        } else if (list != null) {
            for (Object o : list) {
                if (o != null) {
                    return o.getClass();
                }
            }
        }
        throw new IOException("Not found list generic type");
    }
}
