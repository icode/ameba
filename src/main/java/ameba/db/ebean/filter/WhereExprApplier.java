package ameba.db.ebean.filter;

import ameba.db.dsl.ExprApplier;
import ameba.db.ebean.filter.CommonExprTransformer.DistinctExpression;
import ameba.db.ebean.filter.CommonExprTransformer.HavingExpression;
import ameba.db.ebean.filter.CommonExprTransformer.TextExpression;
import io.ebean.Expression;
import io.ebean.ExpressionList;
import io.ebean.Query;
import io.ebeaninternal.server.expression.AbstractTextExpression;

/**
 * <p>WhereExprApplier class.</p>
 *
 * @author icode
 *
 */
public class WhereExprApplier<O> implements ExprApplier<Expression> {

    private ExpressionList<O> expressionList;

    /**
     * <p>Constructor for WhereExprApplier.</p>
     *
     * @param expressions a {@link io.ebean.ExpressionList} object.
     */
    public WhereExprApplier(ExpressionList<O> expressions) {
        this.expressionList = expressions;
    }

    /**
     * <p>create.</p>
     *
     * @param expressions a {@link io.ebean.ExpressionList} object.
     * @param <O>         a O object.
     * @return a {@link ameba.db.ebean.filter.WhereExprApplier} object.
     */
    public static <O> WhereExprApplier<O> create(ExpressionList<O> expressions) {
        return new WhereExprApplier<>(expressions);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public void apply(Expression expr) {
        if (expr instanceof HavingExpression) {
            ExpressionList having = expressionList.query().having();
            ((HavingExpression) expr).getExpressionList().forEach(having::add);
            return;
        } else if (expr instanceof TextExpression) {
            TextExpression expression = (TextExpression) expr;
            ExpressionList et = expressionList.query().text();
            expression.getExpressionList().forEach(et::add);
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
