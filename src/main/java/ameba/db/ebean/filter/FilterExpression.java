package ameba.db.ebean.filter;

import io.ebean.ExpressionFactory;
import io.ebean.Query;
import io.ebeaninternal.server.expression.FilterExprPath;
import io.ebeaninternal.server.expression.FilterExpressionList;

/**
 * <p>FilterExpression class.</p>
 *
 * @author icode
 *
 */
public class FilterExpression<T> extends FilterExpressionList<T> {
    private String path;

    /**
     * <p>Constructor for FilterExpression.</p>
     *
     * @param path      a {@link java.lang.String} object.
     * @param expr      a {@link io.ebean.ExpressionFactory} object.
     * @param rootQuery a {@link io.ebean.Query} object.
     */
    public FilterExpression(String path, ExpressionFactory expr, Query<T> rootQuery) {
        super(new FilterExprPath(path), expr, rootQuery);
        this.path = path;
    }

    /**
     * <p>Getter for the field <code>path</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPath() {
        return path;
    }
}
