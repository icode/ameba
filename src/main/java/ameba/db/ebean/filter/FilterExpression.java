package ameba.db.ebean.filter;

import com.avaje.ebean.ExpressionFactory;
import com.avaje.ebean.Query;
import com.avaje.ebeaninternal.server.expression.FilterExprPath;
import com.avaje.ebeaninternal.server.expression.FilterExpressionList;

/**
 * @author icode
 */
public class FilterExpression<T> extends FilterExpressionList<T> {
    private String path;

    public FilterExpression(String path, ExpressionFactory expr, Query<T> rootQuery) {
        super(new FilterExprPath(path), expr, rootQuery);
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
