package ameba.db.dsl;

import ameba.db.dsl.QueryExprMeta.Val;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * <p>Abstract QueryExprInvoker class.</p>
 *
 * @author icode
 *
 */
public abstract class QueryExprInvoker<T> {
    private static final List<Val<?>> EMP_ARGS = Lists.newArrayListWithCapacity(0);

    /**
     * <p>invoke.</p>
     *
     * @param queries     a {@link java.util.List} object.
     * @param invoker     a {@link ameba.db.dsl.QueryExprInvoker} object.
     * @param exprApplier a {@link ameba.db.dsl.ExprApplier} object.
     * @param <T>         a T object.
     */
    public static <T> void invoke(List<QueryExprMeta> queries,
                                  QueryExprInvoker<T> invoker,
                                  ExprApplier<T> exprApplier) {
        for (QueryExprMeta expr : queries) {
            exprApplier.apply(invoker.invoke(expr));
        }
    }

    /**
     * <p>invoke.</p>
     *
     * @param queryExprMeta a {@link ameba.db.dsl.QueryExprMeta} object.
     * @return a T object.
     */
    @SuppressWarnings("unchecked")
    protected T invoke(QueryExprMeta queryExprMeta) {
        List<Val<?>> args = queryExprMeta.arguments();
        if (args == null) {
            args = EMP_ARGS;
        }
        int argCount = args.size();
        Val<T>[] argsArray = new Val[argCount];
        String field = queryExprMeta.field();
        String op = queryExprMeta.operator();
        for (int i = 0; i < argCount; i++) {
            Val<?> argObj = args.get(i);
            if (argObj.object() instanceof QueryExprMeta) {
                argsArray[i] = Val.of(invoke(argObj.meta()));
            } else {
                argsArray[i] = arg(field, op, (Val<T>) argObj, i, argCount, queryExprMeta.parent());
            }
        }

        return expr(field, op, argsArray, queryExprMeta.parent()).expr();
    }

    /**
     * <p>arg.</p>
     *
     * @param field a {@link java.lang.String} object.
     * @param op a {@link java.lang.String} object.
     * @param arg a {@link ameba.db.dsl.QueryExprMeta.Val} object.
     * @param index a int.
     * @param count a int.
     * @param parent a {@link ameba.db.dsl.QueryExprMeta} object.
     * @return a {@link ameba.db.dsl.QueryExprMeta.Val} object.
     */
    protected abstract Val<T> arg(String field, String op, Val<T> arg, int index, int count, QueryExprMeta parent);

    /**
     * <p>expr.</p>
     *
     * @param field a {@link java.lang.String} object.
     * @param op a {@link java.lang.String} object.
     * @param args an array of {@link ameba.db.dsl.QueryExprMeta.Val} objects.
     * @param parent a {@link ameba.db.dsl.QueryExprMeta} object.
     * @return a {@link ameba.db.dsl.QueryExprMeta.Val} object.
     */
    protected abstract Val<T> expr(String field, String op, Val<T>[] args, QueryExprMeta parent);

}
