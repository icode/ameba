package ameba.db.ebean.filter;

import ameba.db.dsl.ExprApplier;
import com.avaje.ebean.Expression;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.expression.AbstractTextExpression;

/**
 * @author icode
 */
public class WhereExprApplier<O> implements ExprApplier<Expression> {

    private ExpressionList<O> expressionList;

    public WhereExprApplier(ExpressionList<O> expressions) {
        this.expressionList = expressions;
    }

    public static <O> WhereExprApplier<O> create(ExpressionList<O> expressions) {
        return new WhereExprApplier<>(expressions);
    }

    @Override
    public void apply(Expression expr) {
        if (expr instanceof AbstractTextExpression) {
            SpiQuery query = (SpiQuery) expressionList.query();
            if (!query.isUseDocStore()) {
                expressionList.setUseDocStore(true);
            }
        } else if (expr instanceof FilterExpression) {
            SpiQuery<?> query = (SpiQuery) expressionList.query();
            FilterExpression<?> filter = (FilterExpression) expr;
            query.setFilterMany(filter.getPath(), filter);
            return;
        }
        expressionList.add(expr);
    }
}
