package ameba.db.ebean.filter;

import ameba.db.dsl.ApplyExpr;
import com.avaje.ebean.Expression;
import com.avaje.ebean.ExpressionList;

/**
 * @author icode
 */
public class ApplyWhereExpr<T> implements ApplyExpr<Expression> {

    private ExpressionList<T> expressionList;

    public ApplyWhereExpr(ExpressionList<T> expressions) {
        this.expressionList = expressions;
    }

    public static <T> ApplyWhereExpr<T> create(ExpressionList<T> expressions) {
        return new ApplyWhereExpr<>(expressions);
    }

    @Override
    public void apply(Expression expr) {
        expressionList.add(expr);
    }
}
