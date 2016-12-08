package ameba.db.dsl;

import ameba.db.dsl.QueryExprMeta.Val;
import org.glassfish.jersey.spi.Contract;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;

/**
 * <p>ExprArgTransformer interface.</p>
 *
 * @author icode
 *
 */
@Contract
@ConstrainedTo(RuntimeType.SERVER)
public interface ExprArgTransformer<O, V extends QueryExprInvoker> extends Transformer<Transformed<Val<O>>> {
    /**
     * <p>transform.</p>
     *
     * @param field    a {@link java.lang.String} object.
     * @param operator a {@link java.lang.String} object.
     * @param arg      a {@link ameba.db.dsl.QueryExprMeta.Val} object.
     * @param index    a int.
     * @param count    a int.
     * @param invoker  a V object.
     * @param parent   a {@link ameba.db.dsl.QueryExprMeta} object.
     * @return a {@link ameba.db.dsl.Transformed} object.
     */
    Transformed<Val<O>> transform(String field, String operator, Val<O> arg,
                                  int index, int count, V invoker, QueryExprMeta parent);
}
