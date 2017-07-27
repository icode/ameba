package ameba.lib.el;

import com.google.common.collect.Maps;

import javax.el.FunctionMapper;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

/**
 * @author icode
 */
public class MapBasedFunctionMapper extends FunctionMapper {
    private static final String FUNCTION_NAME_SEPARATOR = ":";
    private Map<String, Method> map = Collections.emptyMap();

    public MapBasedFunctionMapper() {
    }

    public Method resolveFunction(String prefix, String localName) {
        return this.map.get(prefix + FUNCTION_NAME_SEPARATOR + localName);
    }

    public void setFunction(String prefix, String localName, Method method) {
        if (this.map.isEmpty()) {
            this.map = Maps.newHashMap();
        }

        this.map.put(prefix + FUNCTION_NAME_SEPARATOR + localName, method);
    }
}
