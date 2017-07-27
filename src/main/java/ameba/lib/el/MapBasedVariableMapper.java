package ameba.lib.el;

import com.google.common.collect.Maps;

import javax.el.ValueExpression;
import javax.el.VariableMapper;
import java.util.Collections;
import java.util.Map;

/**
 * @author icode
 */
public class MapBasedVariableMapper extends VariableMapper {
    private Map<String, ValueExpression> map = Collections.emptyMap();

    public ValueExpression resolveVariable(String variable) {
        return this.map.get(variable);
    }

    public ValueExpression setVariable(String variable, ValueExpression expression) {
        if (this.map.isEmpty()) {
            this.map = Maps.newHashMap();
        }

        return this.map.put(variable, expression);
    }
}
