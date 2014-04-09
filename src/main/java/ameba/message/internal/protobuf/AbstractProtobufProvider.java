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
            return (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
        } else if (list != null) {
            if (list.size() == 0) return Object.class;

            for (Object o : list) {
                if (o != null) {
                    return o.getClass();
                }
            }

            return Object.class;
        }
        throw new IOException("Not found list generic type");
    }
}
