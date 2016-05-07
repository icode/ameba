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
public class QueryInfo {
    private String column;
    private String operator;
    private List<Object> arguments;
    private QueryInfo parent;

    private QueryInfo() {
    }

    public static QueryInfo create() {
        return new QueryInfo();
    }

    public String operator() {
        return operator;
    }

    public String column() {
        return column;
    }

    public List<Object> arguments() {
        return arguments;
    }

    public QueryInfo parent() {
        return parent;
    }

    protected QueryInfo column(String column) {
        this.column = column;
        return this;
    }

    protected QueryInfo operator(String operator) {
        this.operator = operator;
        return this;
    }

    protected QueryInfo arguments(List arguments) {
        this.arguments = arguments;
        return this;
    }

    protected QueryInfo arguments(Object... arguments) {
        if (this.arguments == null) this.arguments = Lists.newArrayList();
        Collections.addAll(this.arguments, arguments);
        return this;
    }

    protected QueryInfo parent(QueryInfo parent) {
        this.parent = parent;
        return this;
    }

    @Override
    public String toString() {
        return (column == null ? "" : column + ".") +
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