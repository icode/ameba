package ameba.db.dsl;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author icode
 */
public abstract class QueryExprInvoker<T> {
    private static final List<Object> EMP_ARGS = Lists.newArrayListWithCapacity(0);

    public static <T> void invoke(List<QueryExprMeta> queries,
                                  QueryExprInvoker<T> invoker,
                                  ExprApplier<T> exprApplier) {
        for (QueryExprMeta expr : queries) {
            exprApplier.apply(invoker.invoke(expr));
        }
    }

    protected T invoke(QueryExprMeta queryExprMeta) {
        List<Object> args = queryExprMeta.arguments();
        if (args == null) {
            args = EMP_ARGS;
        }
        int argCount = args.size();
        Object[] argsArray = new Object[argCount];
        String field = queryExprMeta.field();
        String op = queryExprMeta.operator();
        for (int i = 0; i < argCount; i++) {
            Object argObj = args.get(i);
            if (argObj instanceof QueryExprMeta) {
                argsArray[i] = invoke((QueryExprMeta) argObj);
            } else {
                argsArray[i] = arg(field, op, argObj, i, argCount);
            }
        }

        return expr(field, op, argsArray);
    }

    protected abstract Object arg(String field, String op, Object arg, int index, int count);

    protected abstract T expr(String field, String op, Object[] args);

}