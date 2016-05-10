package ameba.db.dsl;

import java.util.List;

/**
 * @author icode
 */
public abstract class QueryExprGenerator<T> {

    public static <T> void invoke(List<QueryExprMeta> queries,
                                  QueryExprGenerator<T> invoker,
                                  ApplyExpr<T> applyExpr) {
        for (QueryExprMeta expr : queries) {
            applyExpr.apply(invoker.invoke(expr));
        }
    }

    protected T invoke(QueryExprMeta queryExprMeta) {
        List<Object> args = queryExprMeta.arguments();
        Object[] argsArray = new Object[args.size()];
        String field = queryExprMeta.field();
        String op = queryExprMeta.operator();
        for (int i = 0; i < args.size(); i++) {
            Object argObj = args.get(i);
            if (argObj instanceof QueryExprMeta) {
                argsArray[i] = invoke((QueryExprMeta) argObj);
            } else {
                argsArray[i] = arg(field, op, argObj);
            }
        }

        return expr(field, op, argsArray);
    }

    protected abstract Object arg(String field, String op, Object arg);

    protected abstract T expr(String field, String op, Object[] args);

}