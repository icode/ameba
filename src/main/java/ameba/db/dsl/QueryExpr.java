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
public class QueryExpr {
    private String field;
    private String operator;
    private List<Object> arguments;
    private QueryExpr parent;

    private QueryExpr() {
    }

    public static QueryExpr create() {
        return new QueryExpr();
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

    public QueryExpr parent() {
        return parent;
    }

    protected QueryExpr field(String field) {
        this.field = field;
        return this;
    }

    protected QueryExpr operator(String operator) {
        this.operator = operator;
        return this;
    }

    protected QueryExpr arguments(List arguments) {
        this.arguments = arguments;
        return this;
    }

    protected QueryExpr arguments(Object... arguments) {
        if (this.arguments == null) this.arguments = Lists.newArrayList();
        Collections.addAll(this.arguments, arguments);
        return this;
    }

    protected QueryExpr parent(QueryExpr parent) {
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
                                                return input == null ? "nil" : input.toString();
                                            }
                                        }), "!"
                        )
                ) +
                ")";
    }
}