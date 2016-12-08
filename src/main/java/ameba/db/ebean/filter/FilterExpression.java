package ameba.db.ebean.filter;

import com.avaje.ebean.ExpressionFactory;
import com.avaje.ebean.Query;
import com.avaje.ebeaninternal.server.expression.FilterExprPath;
import com.avaje.ebeaninternal.server.expression.FilterExpressionList;

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
     * @param expr      a {@link com.avaje.ebean.ExpressionFactory} object.
     * @param rootQuery a {@link com.avaje.ebean.Query} object.
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
