package ameba.db.dsl;

import ameba.db.dsl.QueryExprMeta.Val;
import org.glassfish.jersey.spi.Contract;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;

/**
 * <p>ExprTransformer interface.</p>
 *
 * @author icode
 *
 */
@Contract
@ConstrainedTo(RuntimeType.SERVER)
public interface ExprTransformer<T, V extends QueryExprInvoker> extends Transformer<Transformed<Val<T>>> {
    /**
     * <p>transform.</p>
     *
     * @param field    a {@link java.lang.String} object.
     * @param operator a {@link java.lang.String} object.
     * @param arg      an array of {@link ameba.db.dsl.QueryExprMeta.Val} objects.
     * @param invoker  a V object.
     * @param parent   a {@link ameba.db.dsl.QueryExprMeta} object.
     * @return a {@link ameba.db.dsl.Transformed} object.
     */
    Transformed<Val<T>> transform(String field, String operator, Val<T>[] arg, V invoker, QueryExprMeta parent);
}
