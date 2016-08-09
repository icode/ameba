package ameba.db.ebean.filter;

import ameba.db.dsl.ExprApplier;
import ameba.db.ebean.filter.CommonExprTransformer.DistinctExpression;
import ameba.db.ebean.filter.CommonExprTransformer.HavingExpression;
import ameba.db.ebean.filter.CommonExprTransformer.TextExpression;
import com.avaje.ebean.Expression;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Query;
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
    @SuppressWarnings("unchecked")
    public void apply(Expression expr) {
        if (expr instanceof HavingExpression) {
            ExpressionList having = expressionList.query().having();
            for (Expression he : ((HavingExpression) expr).getExpressionList()) {
                having.add(he);
            }
            return;
        } else if (expr instanceof TextExpression) {
            TextExpression expression = (TextExpression) expr;
            ExpressionList et = expressionList.query().text();
            for (Expression e : expression.getExpressionList()) {
                et.add(e);
            }
            return;
        } else if (expr instanceof DistinctExpression) {
            expressionList.setDistinct(((DistinctExpression) expr).distinct);
            return;
        } else if (expr instanceof AbstractTextExpression) {
            expressionList.setUseDocStore(true);
        } else if (expr instanceof FilterExpression) {
            FilterExpression filter = (FilterExpression) expr;
            Query query = expressionList.query();
            query.filterMany(filter.getPath()).addAll(filter);
            return;
        }
        expressionList.add(expr);
    }
}
