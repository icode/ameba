package ameba.db.dsl;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * @author icode
 */
public class QueryExprMeta {
    private String field;
    private String operator;
    private List<Object> arguments;
    private QueryExprMeta parent;

    private QueryExprMeta() {
    }

    public static QueryExprMeta create() {
        return new QueryExprMeta();
    }

    public String operator() {
        return operator;
    }

    public String field() {
        return field;
    }

    public List<Object> arguments() {
        return arguments;
    }

    public QueryExprMeta parent() {
        return parent;
    }

    protected QueryExprMeta field(String field) {
        this.field = field;
        return this;
    }

    protected QueryExprMeta operator(String operator) {
        this.operator = operator;
        return this;
    }

    protected QueryExprMeta arguments(List<Object> arguments) {
        this.arguments = arguments;
        return this;
    }

    protected QueryExprMeta arguments(Object... arguments) {
        if (this.arguments == null) this.arguments = Lists.newArrayList();
        Collections.addAll(this.arguments, arguments);
        return this;
    }

    protected QueryExprMeta parent(QueryExprMeta parent) {
        this.parent = parent;
        return this;
    }

    @Override
    public String toString() {
        return (field == null ? "" : field + ".") +
                operator + "(" +
                (arguments == null ? "" :
                        StringUtils.join(
                                Collections2.transform(arguments,
                                        new Function<Object, String>() {
                                            @Nullable
                                            @Override
                                            public String apply(@Nullable Object input) {
                                                if (input == null) {
                                                    return "nil";
                                                } else if (input instanceof String) {
                                                    return "'" + input + "'";
                                                }
                                                return input.toString();
                                            }
                                        }), "!"
                        )
                ) +
                ")";
    }
}